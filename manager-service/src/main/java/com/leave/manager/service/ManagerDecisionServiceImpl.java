package com.leave.manager.service;

import java.util.List;

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

import feign.FeignException;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of ManagerDecisionService.
 * Handles business logic for employee leave approval,
 * rejection, and manager decision retrieval.
 */
@Service
@RequiredArgsConstructor
public class ManagerDecisionServiceImpl implements ManagerDecisionService {

    private final ManagerDecisionRepository managerDecisionRepository;
    private final EmployeeServiceClient employeeServiceClient;

    /**
     * Processes a manager's decision to approve or reject a leave request.
     *
     * @param request manager decision details
     * @return saved manager decision response
     * @throws InvalidDecisionException if request is invalid or a decision already exists
     */
    @Override
    @Transactional
    public ManagerDecisionResponse processDecision(ManagerDecisionRequest request) {

        // Validate request
        if (request == null
                || request.getLeaveRequestId() == null
                || request.getLeaveRequestId() <= 0
                || request.getManagerId() == null
                || request.getManagerId() <= 0
                || request.getDecisionType() == null) {

            throw new InvalidDecisionException(
                    "Valid leave request ID, manager ID, and decision type are required");
        }

        // Validate decision type
        if (request.getDecisionType() != DecisionType.APPROVED
                && request.getDecisionType() != DecisionType.REJECTED) {

            throw new InvalidDecisionException("Invalid leave decision");
        }

        // Check whether a decision already exists
        if (managerDecisionRepository
                .findByLeaveRequestId(request.getLeaveRequestId())
                .isPresent()) {

            throw new InvalidDecisionException(
                    "A decision already exists for leave request ID: "
                            + request.getLeaveRequestId());
        }

        // Create manager decision entity
        ManagerDecision decision = new ManagerDecision();

        decision.setLeaveRequestId(request.getLeaveRequestId());
        decision.setManagerId(request.getManagerId());
        decision.setDecisionType(request.getDecisionType());
        decision.setComments(request.getComments());

        // Save decision in Manager Service database
        ManagerDecision savedDecision =
                managerDecisionRepository.save(decision);

        // Convert entity into response DTO
        return mapToResponse(savedDecision);
    }

    /**
     * Retrieves all pending leave requests assigned to a manager
     * from Employee Service using OpenFeign.
     *
     * @param managerId unique ID of the manager
     * @return list of pending leave requests
     * @throws InvalidDecisionException if manager ID is invalid
     * @throws ServiceCommunicationException if Employee Service communication fails
     */
    @Override
    public List<PendingLeaveResponse> getPendingLeaveRequests(Long managerId) {

        // Validate manager ID
        if (managerId == null || managerId <= 0) {
            throw new InvalidDecisionException("Invalid manager ID");
        }

        try {
            // Fetch pending leave requests from Employee Service
            return employeeServiceClient.getPendingLeaves(managerId);

        } catch (FeignException exception) {

            throw new ServiceCommunicationException(
                    "Unable to retrieve pending leave requests from Employee Service",
                    exception
            );
        }
    }

    /**
     * Retrieves all leave decisions made by a specific manager.
     *
     * @param managerId unique ID of the manager
     * @return list of manager decision responses
     * @throws InvalidDecisionException if manager ID is invalid
     */
    @Override
    public List<ManagerDecisionResponse> getManagerDecisions(Long managerId) {

        // Validate manager ID
        if (managerId == null || managerId <= 0) {
            throw new InvalidDecisionException("Invalid manager ID");
        }

        // Fetch decisions and convert them into response DTOs
        return managerDecisionRepository.findByManagerId(managerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
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
        response.setDecisionType(decision.getDecisionType());
        response.setComments(decision.getComments());
        response.setDecisionDate(decision.getDecisionDate());

        return response;
    }
}
