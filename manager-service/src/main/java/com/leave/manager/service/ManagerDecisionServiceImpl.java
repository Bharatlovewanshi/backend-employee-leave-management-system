
package com.leave.manager.service;

import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leave.manager.client.EmployeeServiceClient;
import com.leave.manager.dto.request.ManagerDecisionRequest;
import com.leave.manager.dto.response.ManagerDecisionResponse;
import com.leave.manager.dto.response.PendingLeaveResponse;
import com.leave.manager.entity.ManagerDecision;
import com.leave.manager.enums.DecisionType;
import com.leave.manager.exception.InvalidDecisionException;
import com.leave.manager.exception.ServiceCommunicationException;
import com.leave.manager.repository.ManagerDecisionRepository;
import com.leave.manager.security.JwtService;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerDecisionServiceImpl implements ManagerDecisionService {

    private final ManagerDecisionRepository managerDecisionRepository;
    private final EmployeeServiceClient employeeServiceClient;
    private final NotificationService notificationService;
    private final JwtService jwtService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Processes manager approval or rejection.
     */
    @Override
    @Transactional
    public ManagerDecisionResponse processDecision(
            ManagerDecisionRequest request,
            String authorization) {

        // 1. Validate request
        validateRequest(request);

        // 2. Extract actual manager ID from database
        Long managerId = extractManagerId(authorization);

        Long leaveRequestId = request.getLeaveRequestId();

        DecisionType decisionType = request.getDecision();

        // 3. Validate rejection reason
        if (decisionType == DecisionType.REJECTED
                && (request.getRejectionReason() == null
                || request.getRejectionReason().isBlank())) {

            throw new InvalidDecisionException(
                    "Rejection reason is required when rejecting a leave request"
            );
        }

        // 4. Check whether a decision already exists
        if (managerDecisionRepository
                .findByLeaveRequestId(leaveRequestId)
                .isPresent()) {

            throw new InvalidDecisionException(
                    "A decision already exists for leave request ID: "
                            + leaveRequestId
            );
        }

        // 5. Verify that the leave is pending and assigned to this manager
        PendingLeaveResponse pendingLeave = getPendingLeave(
                leaveRequestId,
                authorization
        );

        validatePendingLeave(pendingLeave, leaveRequestId);

        // 6. Update leave status in Employee Service first
        PendingLeaveResponse updatedLeave;

        try {

            updatedLeave = employeeServiceClient.decideLeave(
                    leaveRequestId,
                    decisionType.name(),
                    authorization
            );

            log.info(
                    "Employee Service updated leave request {} to {}",
                    leaveRequestId,
                    decisionType
            );

        } catch (FeignException exception) {

            log.error(
                    "Employee Service decision update failed. "
                            + "Leave ID: {}, HTTP Status: {}, Response: {}",
                    leaveRequestId,
                    exception.status(),
                    exception.contentUTF8(),
                    exception
            );

            throw new ServiceCommunicationException(
                    "Unable to update leave status in Employee Service. "
                            + "HTTP Status: " + exception.status(),
                    exception
            );
        }

        if (updatedLeave == null) {

            throw new ServiceCommunicationException(
                    "Employee Service returned an empty response after updating the leave"
            );
        }

        // 7. Save manager decision only after Employee Service succeeds
        ManagerDecision decision = new ManagerDecision();

        decision.setLeaveRequestId(leaveRequestId);
        decision.setManagerId(managerId);
        decision.setDecision(decisionType);

        decision.setRejectionReason(
                decisionType == DecisionType.REJECTED
                        ? request.getRejectionReason().trim()
                        : null
        );

        ManagerDecision savedDecision;

        try {

            savedDecision = managerDecisionRepository.saveAndFlush(decision);

        } catch (DataIntegrityViolationException exception) {

            log.error(
                    "Duplicate decision detected for leave request {}",
                    leaveRequestId,
                    exception
            );

            throw new InvalidDecisionException(
                    "A decision already exists for leave request ID: "
                            + leaveRequestId
            );
        }

        // 8. Send notifications
        sendDecisionNotifications(savedDecision, updatedLeave);

        log.info(
                "Manager {} successfully processed leave request {} with decision {}",
                managerId,
                leaveRequestId,
                decisionType
        );

        // 9. Return response
        return mapToResponse(savedDecision, authorization);
    }

    /**
     * Validates the decision request.
     */
    private void validateRequest(ManagerDecisionRequest request) {

        if (request == null
                || request.getLeaveRequestId() == null
                || request.getLeaveRequestId() <= 0
                || request.getDecision() == null) {

            throw new InvalidDecisionException(
                    "Valid leave request ID and decision are required"
            );
        }

        if (request.getDecision() != DecisionType.APPROVED
                && request.getDecision() != DecisionType.REJECTED) {

            throw new InvalidDecisionException(
                    "Invalid leave decision"
            );
        }
    }

    /**
     * Retrieves a pending leave request assigned to the authenticated manager.
     */
    private PendingLeaveResponse getPendingLeave(
            Long leaveRequestId,
            String authorization) {

        try {

            List<PendingLeaveResponse> pendingLeaves =
                    employeeServiceClient.getPendingLeaves(authorization);

            if (pendingLeaves == null) {

                throw new ServiceCommunicationException(
                        "Employee Service returned an empty pending leave response"
                );
            }

            return pendingLeaves.stream()
                    .filter(Objects::nonNull)
                    .filter(leave ->
                            leaveRequestId.equals(leave.getLeaveRequestId()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidDecisionException(
                            "Pending leave request not found for this manager"
                    ));

        } catch (FeignException exception) {

            log.error(
                    "Failed to retrieve pending leave from Employee Service. "
                            + "Leave ID: {}, HTTP Status: {}, Response: {}",
                    leaveRequestId,
                    exception.status(),
                    exception.contentUTF8(),
                    exception
            );

            throw new ServiceCommunicationException(
                    "Unable to retrieve employee leave details from Employee Service. "
                            + "HTTP Status: " + exception.status(),
                    exception
            );
        }
    }

    /**
     * Validates the pending leave response.
     */
    private void validatePendingLeave(
            PendingLeaveResponse leave,
            Long leaveRequestId) {

        if (leave == null
                || leave.getLeaveRequestId() == null
                || !leaveRequestId.equals(leave.getLeaveRequestId())
                || leave.getEmployeeId() == null
                || leave.getEmployeeId() <= 0) {

            throw new ServiceCommunicationException(
                    "Employee Service returned incomplete leave details"
            );
        }
    }

    /**
     * Sends in-app and email notifications.
     */
    private void sendDecisionNotifications(
            ManagerDecision decision,
            PendingLeaveResponse leave) {

        String decisionStatus = decision.getDecision().name();

        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("Dear ")
                .append(leave.getEmployeeName() == null
                        ? "Employee"
                        : leave.getEmployeeName())
                .append(",\n\n");

        messageBuilder.append("Your leave request has been ")
                .append(decisionStatus.toLowerCase())
                .append(".\n\n");

        messageBuilder.append("Leave Request Details:\n")
                .append("Request ID: ")
                .append(decision.getLeaveRequestId())
                .append("\n")
                .append("Leave Type: ")
                .append(leave.getLeaveType())
                .append("\n")
                .append("Start Date: ")
                .append(leave.getStartDate())
                .append("\n")
                .append("End Date: ")
                .append(leave.getEndDate())
                .append("\n")
                .append("Decision: ")
                .append(decisionStatus)
                .append("\n");

        if (decision.getDecision() == DecisionType.REJECTED) {

            messageBuilder.append("Rejection Reason: ")
                    .append(decision.getRejectionReason())
                    .append("\n");
        }

        messageBuilder.append("\nRegards,\n")
                .append("Employee Leave Management System");

        String message = messageBuilder.toString();

        // In-app notification
        try {

            notificationService.sendInAppNotification(
                    decision,
                    leave.getEmployeeId(),
                    message
            );

        } catch (RuntimeException exception) {

            log.error(
                    "Failed to create in-app notification for leave request {}",
                    decision.getLeaveRequestId(),
                    exception
            );
        }

        // Check employee email
        if (leave.getEmployeeEmail() == null
                || leave.getEmployeeEmail().isBlank()) {

            log.warn(
                    "Employee email is missing for leave request {}. "
                            + "Email notification skipped.",
                    decision.getLeaveRequestId()
            );

            return;
        }

        // Email notification
        String subject = "Leave Request #"
                + decision.getLeaveRequestId()
                + " - " + decisionStatus;

        try {

            notificationService.sendEmail(
                    decision,
                    leave.getEmployeeId(),
                    leave.getEmployeeEmail(),
                    subject,
                    message
            );

        } catch (RuntimeException exception) {

            log.error(
                    "Failed to send email notification for leave request {}",
                    decision.getLeaveRequestId(),
                    exception
            );
        }
    }

    /**
     * Retrieves pending leave requests for the authenticated manager.
     */
    @Override
    public List<PendingLeaveResponse> getPendingLeaveRequests(
            String authorization) {

        // Validate manager identity
        extractManagerId(authorization);

        try {

            List<PendingLeaveResponse> response =
                    employeeServiceClient.getPendingLeaves(authorization);

            return response == null ? List.of() : response;

        } catch (FeignException exception) {

            log.error(
                    "Failed to retrieve pending leaves. HTTP Status: {}, Response: {}",
                    exception.status(),
                    exception.contentUTF8(),
                    exception
            );

            throw new ServiceCommunicationException(
                    "Unable to retrieve pending leave requests from Employee Service. "
                            + "HTTP Status: " + exception.status(),
                    exception
            );
        }
    }

    /**
     * Retrieves decision history for the authenticated manager only.
     */
    @Override
    public List<ManagerDecisionResponse> getManagerDecisions(
            String authorization) {

        Long managerId = extractManagerId(authorization);

        return managerDecisionRepository.findByManagerId(managerId)
                .stream()
                .map(decision -> mapToResponse(decision, authorization))
                .toList();
    }

    /**
     * Extracts the actual manager ID from admin_managers
     * using the email present in the authenticated JWT.
     *
     * JWT userId is not the same as admin_managers.manager_id.
     */
    private Long extractManagerId(String authorization) {

        if (authorization == null
                || !authorization.startsWith("Bearer ")
                || authorization.substring(7).isBlank()) {

            throw new InvalidDecisionException(
                    "Valid Bearer token is required"
            );
        }

        try {

            String token = authorization.substring(7).trim();

            // Validate JWT signature, expiration and claims.
            if (!jwtService.isTokenValid(token)
                    || !"MANAGER".equals(jwtService.extractRole(token))) {

                throw new InvalidDecisionException(
                        "Only authenticated managers can perform this operation"
                );
            }

            // Extract email from JWT subject.
            String email = jwtService.extractEmail(token);

            if (email == null || email.isBlank()) {

                throw new InvalidDecisionException(
                        "Manager email is missing from authentication token"
                );
            }

            // Resolve manager_id using manager email.
            Long managerId = jdbcTemplate.queryForObject(
                    "SELECT manager_id FROM admin_managers WHERE email = ?",
                    Long.class,
                    email
            );

            if (managerId == null || managerId <= 0) {

                throw new InvalidDecisionException(
                        "Invalid manager ID"
                );
            }

            log.info(
                    "Authenticated manager resolved: email={}, managerId={}",
                    email,
                    managerId
            );

            return managerId;

        } catch (InvalidDecisionException exception) {

            throw exception;

        } catch (EmptyResultDataAccessException exception) {

            throw new InvalidDecisionException(
                    "Manager profile not found for authenticated email"
            );

        } catch (Exception exception) {

            log.error(
                    "Failed to resolve manager identity",
                    exception
            );

            throw new InvalidDecisionException(
                    "Invalid authentication token or manager profile"
            );
        }
    }

    /**
     * Converts entity into response DTO.
     */
private ManagerDecisionResponse mapToResponse(
        ManagerDecision decision,
        String authorization) {

    ManagerDecisionResponse response = new ManagerDecisionResponse();

    response.setId(decision.getId());
    response.setLeaveRequestId(decision.getLeaveRequestId());
    response.setManagerId(decision.getManagerId());
    response.setDecision(decision.getDecision());
    response.setRejectionReason(decision.getRejectionReason());
    response.setDecisionAt(decision.getDecisionAt());

    try {
        PendingLeaveResponse leave =
                employeeServiceClient.getLeaveDetails(
                        decision.getLeaveRequestId(),
                        authorization
                );

        if (leave != null) {
            response.setEmployeeId(leave.getEmployeeId());
            response.setEmployeeName(leave.getEmployeeName());
            response.setEmployeeEmail(leave.getEmployeeEmail());
            response.setLeaveType(leave.getLeaveType());
            response.setStartDate(leave.getStartDate());
            response.setEndDate(leave.getEndDate());
            response.setReason(leave.getReason());
            response.setStatus(leave.getStatus());
        }

    } catch (FeignException exception) {
        log.error(
                "Unable to fetch leave details for request {}. HTTP status: {}, response: {}",
                decision.getLeaveRequestId(),
                exception.status(),
                exception.contentUTF8(),
                exception
        );
    }

    return response;
}
}
