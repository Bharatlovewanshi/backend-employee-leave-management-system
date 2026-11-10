
package com.leave.employee.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.leave.employee.dto.request.LeaveApplicationRequest;
import com.leave.employee.dto.response.LeaveResponse;
import com.leave.employee.dto.response.LeaveSummaryResponse;
import com.leave.employee.service.LeaveService;

@RestController
@RequestMapping("/api/employees/me/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    // Apply for leave
    @PostMapping
    public ResponseEntity<LeaveResponse> applyLeave(
            @Valid @RequestBody LeaveApplicationRequest request,
            Authentication authentication) {

        LeaveResponse response = leaveService.applyLeave(
                authentication.getName(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get logged-in employee's leave history
    @GetMapping
    public ResponseEntity<List<LeaveResponse>> getMyLeaveHistory(
            Authentication authentication) {

        return ResponseEntity.ok(
                leaveService.getMyLeaveHistory(authentication.getName()));
    }

    // Get leave summary
    @GetMapping("/summary")
    public ResponseEntity<LeaveSummaryResponse> getLeaveSummary(
            Authentication authentication) {

        return ResponseEntity.ok(
                leaveService.getLeaveSummary(authentication.getName()));
    }

    // Get particular leave details
    @GetMapping("/{leaveId}")
    public ResponseEntity<LeaveResponse> getLeaveDetails(
            @PathVariable Long leaveId,
            Authentication authentication) {

        return ResponseEntity.ok(
                leaveService.getLeaveDetails(
                        authentication.getName(), leaveId));
    }

    // Cancel pending leave
    @PatchMapping("/{leaveId}/cancel")
    public ResponseEntity<LeaveResponse> cancelLeave(
            @PathVariable Long leaveId,
            Authentication authentication) {

        return ResponseEntity.ok(
                leaveService.cancelLeave(
                        authentication.getName(), leaveId));
    }
}
