package com.leave.manager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.leave.manager.client.EmployeeServiceClient;
import com.leave.manager.dto.request.ManagerDecisionRequest;
import com.leave.manager.dto.response.ManagerDecisionResponse;
import com.leave.manager.dto.response.PendingLeaveResponse;
import com.leave.manager.entity.ManagerDecision;
import com.leave.manager.enums.DecisionType;
import com.leave.manager.exception.InvalidDecisionException;
import com.leave.manager.exception.ServiceCommunicationException;
import com.leave.manager.repository.ManagerDecisionRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of ManagerDecisionService. Handles business logic for employee
 * leave approval, rejection, and manager decision retrieval.
 */
@Service
@RequiredArgsConstructor
public class ManagerDecisionServiceImpl implements ManagerDecisionService {

	private final ManagerDecisionRepository managerDecisionRepository;
	private final EmployeeServiceClient employeeServiceClient;
	private final NotificationService notificationService;

	/**
	 * Processes a manager's decision to approve or reject a leave request.
	 *
	 * @param request manager decision details
	 * @return saved manager decision response
	 */

	@Override
	public ManagerDecisionResponse processDecision(ManagerDecisionRequest request) {

		// 1. Validate request
		if (request == null || request.getLeaveRequestId() == null || request.getLeaveRequestId() <= 0
				|| request.getManagerId() == null || request.getManagerId() <= 0 || request.getDecision() == null) {

			throw new InvalidDecisionException("Valid leave request ID, manager ID, and decision are required");
		}

		// 2. Validate decision type
		if (request.getDecision() != DecisionType.APPROVED && request.getDecision() != DecisionType.REJECTED) {

			throw new InvalidDecisionException("Invalid leave decision");
		}

		// 3. Validate rejection reason
		if (request.getDecision() == DecisionType.REJECTED
				&& (request.getRejectionReason() == null || request.getRejectionReason().isBlank())) {

			throw new InvalidDecisionException("Rejection reason is required when rejecting a leave request");
		}

		// 4. Check whether a decision already exists
		if (managerDecisionRepository.findByLeaveRequestId(request.getLeaveRequestId()).isPresent()) {

			throw new InvalidDecisionException(
					"A decision already exists for leave request ID: " + request.getLeaveRequestId());
		}

		// 5. Retrieve pending leave details from Employee Service
		PendingLeaveResponse pendingLeave;

		try {
			pendingLeave = employeeServiceClient.getPendingLeaves(request.getManagerId()).stream()
					.filter(leave -> leave.getLeaveRequestId().equals(request.getLeaveRequestId())).findFirst()
					.orElseThrow(
							() -> new InvalidDecisionException("Pending leave request not found for this manager"));

		} catch (FeignException exception) {
			throw new ServiceCommunicationException("Unable to retrieve employee leave details from Employee Service",
					exception);
		}

		// 6. Create manager decision entity
		ManagerDecision decision = new ManagerDecision();

		decision.setLeaveRequestId(request.getLeaveRequestId());
		decision.setManagerId(request.getManagerId());
		decision.setDecision(request.getDecision());

		decision.setRejectionReason(
				request.getDecision() == DecisionType.REJECTED ? request.getRejectionReason().trim() : null);

		// 7. Save decision in Manager Service database
		ManagerDecision savedDecision = managerDecisionRepository.saveAndFlush(decision);

		// 8. Prepare notification message
		String decisionStatus = savedDecision.getDecision().toString();

		StringBuilder messageBuilder = new StringBuilder();

		messageBuilder.append("Dear ")
		        .append(pendingLeave.getEmployeeName())
		        .append(",\n\n");

		messageBuilder.append("Your leave request has been ")
		        .append(decisionStatus.toLowerCase())
		        .append(".\n\n");

		messageBuilder.append("Leave Request Details:\n")
		        .append("Request ID: ")
		        .append(savedDecision.getLeaveRequestId())
		        .append("\n")
		        .append("Leave Type: ")
		        .append(pendingLeave.getLeaveType())
		        .append("\n")
		        .append("Start Date: ")
		        .append(pendingLeave.getStartDate())
		        .append("\n")
		        .append("End Date: ")
		        .append(pendingLeave.getEndDate())
		        .append("\n")
		        .append("Decision: ")
		        .append(decisionStatus)
		        .append("\n");

		if (savedDecision.getDecision() == DecisionType.REJECTED) {
		    messageBuilder.append("Rejection Reason: ")
		            .append(savedDecision.getRejectionReason())
		            .append("\n");
		}

		messageBuilder.append("\nRegards,\n")
		        .append("Employee Leave Management System");

		String message = messageBuilder.toString();

		// 9. Save in-app notification
		notificationService.sendInAppNotification(
		        savedDecision,
		        pendingLeave.getEmployeeId(),
		        message
		);

		// 10. Send email notification
		String subject = "Leave Request #" + savedDecision.getLeaveRequestId()
		        + " - " + decisionStatus;

		notificationService.sendEmail(
		        savedDecision,
		        pendingLeave.getEmployeeId(),
		        pendingLeave.getEmployeeEmail(),
		        subject,
		        message
		);
		// 11. Convert entity into response DTO
		return mapToResponse(savedDecision);
	}

	/**
	 * Retrieves all pending leave requests assigned to a manager from Employee
	 * Service using OpenFeign.
	 *
	 * @param managerId unique ID of the manager
	 * @return list of pending leave requests
	 */
	@Override
	public List<PendingLeaveResponse> getPendingLeaveRequests(Long managerId) {

		// Validate manager ID
		if (managerId == null || managerId <= 0) {
			throw new InvalidDecisionException("Invalid manager ID");
		}

		try {
			return employeeServiceClient.getPendingLeaves(managerId);

		} catch (FeignException exception) {

			throw new ServiceCommunicationException("Unable to retrieve pending leave requests from Employee Service",
					exception);
		}
	}

	/**
	 * Retrieves all leave decisions made by a specific manager.
	 *
	 * @param managerId unique ID of the manager
	 * @return list of manager decision responses
	 */
	@Override
	public List<ManagerDecisionResponse> getManagerDecisions(Long managerId) {

		// Validate manager ID
		if (managerId == null || managerId <= 0) {
			throw new InvalidDecisionException("Invalid manager ID");
		}

		return managerDecisionRepository.findByManagerId(managerId).stream().map(this::mapToResponse).toList();
	}

	/**
	 * Converts a ManagerDecision entity into a response DTO.
	 *
	 * @param decision manager decision entity
	 * @return manager decision response
	 */
	private ManagerDecisionResponse mapToResponse(ManagerDecision decision) {

		ManagerDecisionResponse response = new ManagerDecisionResponse();

		response.setId(decision.getId());
		response.setLeaveRequestId(decision.getLeaveRequestId());
		response.setManagerId(decision.getManagerId());
		response.setDecision(decision.getDecision());
		response.setRejectionReason(decision.getRejectionReason());
		response.setDecisionAt(decision.getDecisionAt());

		return response;
	}
}