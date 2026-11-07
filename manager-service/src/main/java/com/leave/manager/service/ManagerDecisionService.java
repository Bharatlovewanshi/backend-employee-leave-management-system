package com.leave.manager.service;

import java.util.List;

import com.leave.manager.dto.request.ManagerDecisionRequest;
import com.leave.manager.dto.response.ManagerDecisionResponse;
import com.leave.manager.dto.response.PendingLeaveResponse;

/**
 * Service interface for managing employee leave approval
 * and rejection operations.
 */
public interface ManagerDecisionService {

    /**
     * Processes a manager's decision to approve or reject a leave request.
     *
     * @param request manager decision details
     * @return processed manager decision response
     */
    ManagerDecisionResponse processDecision(ManagerDecisionRequest request);

    /**
     * Retrieves all pending leave requests assigned to a manager.
     *
     * @param managerId unique ID of the manager
     * @return list of pending leave requests
     */
    List<PendingLeaveResponse> getPendingLeaveRequests(Long managerId);

    /**
     * Retrieves all decisions made by a specific manager.
     *
     * @param managerId unique ID of the manager
     * @return list of manager decision responses
     */
    List<ManagerDecisionResponse> getManagerDecisions(Long managerId);
}