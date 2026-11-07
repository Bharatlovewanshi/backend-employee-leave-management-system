package com.leave.manager.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.leave.manager.dto.request.ManagerDecisionRequest;
import com.leave.manager.dto.response.ApiResponse;
import com.leave.manager.dto.response.ManagerDecisionResponse;
import com.leave.manager.dto.response.PendingLeaveResponse;
import com.leave.manager.service.ManagerDecisionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing employee leave approval and rejection.
 * Provides APIs for processing manager decisions and retrieving leave requests.
 */
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/manager/decisions")
@RequiredArgsConstructor
public class ManagerDecisionController {

    private final ManagerDecisionService managerDecisionService;

    /**
     * Processes a manager's decision to approve or reject a leave request.
     *
     * @param request manager decision details
     * @return API response containing the saved decision
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ManagerDecisionResponse>> processDecision(
            @Valid @RequestBody ManagerDecisionRequest request) {

        ManagerDecisionResponse response =
                managerDecisionService.processDecision(request);

        ApiResponse<ManagerDecisionResponse> apiResponse =
                new ApiResponse<>(
                        true,
                        "Leave decision processed successfully",
                        response
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Retrieves all pending leave requests assigned to a manager.
     *
     * @param managerId unique ID of the manager
     * @return API response containing pending leave requests
     */
    @GetMapping("/pending/{managerId}")
    public ResponseEntity<ApiResponse<List<PendingLeaveResponse>>> getPendingLeaveRequests(
            @PathVariable("managerId") Long managerId) {

        List<PendingLeaveResponse> response =
                managerDecisionService.getPendingLeaveRequests(managerId);

        ApiResponse<List<PendingLeaveResponse>> apiResponse =
                new ApiResponse<>(
                        true,
                        "Pending leave requests retrieved successfully",
                        response
                );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Retrieves all leave decisions made by a specific manager.
     *
     * @param managerId unique ID of the manager
     * @return API response containing manager decisions
     */
    @GetMapping("/manager/{managerId}")
    public ResponseEntity<ApiResponse<List<ManagerDecisionResponse>>> getManagerDecisions(
            @PathVariable("managerId") Long managerId) {

        List<ManagerDecisionResponse> response =
                managerDecisionService.getManagerDecisions(managerId);

        ApiResponse<List<ManagerDecisionResponse>> apiResponse =
                new ApiResponse<>(
                        true,
                        "Manager decisions retrieved successfully",
                        response
                );

        return ResponseEntity.ok(apiResponse);
    }
}