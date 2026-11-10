
package com.leave.manager.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
 */
@Validated
@RestController
@RequestMapping("/api/manager/decisions")
@RequiredArgsConstructor
public class ManagerDecisionController {

    private final ManagerDecisionService managerDecisionService;

    /**
     * Processes a manager's decision.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ManagerDecisionResponse>> processDecision(
            @Valid @RequestBody ManagerDecisionRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        ManagerDecisionResponse response =
                managerDecisionService.processDecision(
                        request,
                        authorization
                );

        ApiResponse<ManagerDecisionResponse> apiResponse =
                new ApiResponse<>(
                        true,
                        "Leave decision processed successfully",
                        response
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiResponse);
    }

    /**
     * Retrieves pending leave requests for the authenticated manager.
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<PendingLeaveResponse>>> getPendingLeaveRequests(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        List<PendingLeaveResponse> response =
                managerDecisionService.getPendingLeaveRequests(
                        authorization
                );

        ApiResponse<List<PendingLeaveResponse>> apiResponse =
                new ApiResponse<>(
                        true,
                        "Pending leave requests retrieved successfully",
                        response
                );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Retrieves decision history for the authenticated manager only.
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ManagerDecisionResponse>>> getManagerDecisions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        List<ManagerDecisionResponse> response =
                managerDecisionService.getManagerDecisions(
                        authorization
                );

        ApiResponse<List<ManagerDecisionResponse>> apiResponse =
                new ApiResponse<>(
                        true,
                        "Manager decision history retrieved successfully",
                        response
                );

        return ResponseEntity.ok(apiResponse);
    }
}
