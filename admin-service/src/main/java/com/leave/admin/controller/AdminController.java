
package com.leave.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.leave.admin.dto.AdminLeaveRequest;
import com.leave.admin.dto.EmployeeResponse;
import com.leave.admin.dto.LeaveRecordResponse;
import com.leave.admin.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        return ResponseEntity.ok(adminService.getAllEmployees());
    }

    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                adminService.getEmployeeById(employeeId));
    }

    @GetMapping("/leaves")
    public ResponseEntity<List<LeaveRecordResponse>> getAllLeaveRequests() {
        return ResponseEntity.ok(
                adminService.getAllLeaveRequests());
    }

    @GetMapping("/leaves/{leaveId}")
    public ResponseEntity<LeaveRecordResponse> getLeaveRequestById(
            @PathVariable Long leaveId) {
        return ResponseEntity.ok(
                adminService.getLeaveRequestById(leaveId));
    }

    // Approve leave request with validation
    @PostMapping("/leaves/{leaveId}/approve")
    public ResponseEntity<LeaveRecordResponse> approveLeaveRequest(
            @PathVariable Long leaveId,
            @Valid @RequestBody AdminLeaveRequest request) {

        LeaveRecordResponse response =
                adminService.approveLeaveRequest(
                        leaveId,
                        request.getAdminUserId());

        return ResponseEntity.ok(response);
    }

    // Reject leave request with validation
    @PostMapping("/leaves/{leaveId}/reject")
    public ResponseEntity<LeaveRecordResponse> rejectLeaveRequest(
            @PathVariable Long leaveId,
            @Valid @RequestBody AdminLeaveRequest request) {

        LeaveRecordResponse response =
                adminService.rejectLeaveRequest(
                        leaveId,
                        request.getAdminUserId(),
                        request.getReason());

        return ResponseEntity.ok(response);
    }
}
