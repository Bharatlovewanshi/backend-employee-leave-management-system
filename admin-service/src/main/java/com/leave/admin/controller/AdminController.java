
package com.leave.admin.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.leave.admin.dto.AdminLeaveRequest;
import com.leave.admin.dto.LeaveRecordResponse;
import com.leave.admin.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Retrieve all leave requests or filter by status
    @GetMapping("/leaves")
    public ResponseEntity<List<LeaveRecordResponse>> getAllLeaveRequests(
            @RequestParam(required = false) String status) {

        if (status != null) {
            return ResponseEntity.ok(
                    adminService.getLeaveRequestsByStatus(status));
        }

        return ResponseEntity.ok(
                adminService.getAllLeaveRequests());
    }

    // Retrieve leave requests by employee ID
    @GetMapping("/leaves/employee/{employeeId}")
    public ResponseEntity<List<LeaveRecordResponse>> getLeaveRequestsByEmployeeId(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                adminService.getLeaveRequestsByEmployeeId(employeeId));
    }

    // Retrieve leave request by ID
    @GetMapping("/leaves/{leaveId}")
    public ResponseEntity<LeaveRecordResponse> getLeaveRequestById(
            @PathVariable Long leaveId) {

        return ResponseEntity.ok(
                adminService.getLeaveRequestById(leaveId));
    }

    // Approve leave request
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

    // Reject leave request
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

    // Download all leave requests as a CSV report
    @GetMapping(value = "/reports/leaves", produces = "text/csv")
    public ResponseEntity<String> downloadLeaveReport() {

        List<LeaveRecordResponse> leaves =
                adminService.getAllLeaveRequests();

        StringBuilder csv = new StringBuilder();

        csv.append("Leave ID,Employee ID,Employee Name,Leave Type,")
           .append("Start Date,End Date,Reason,Status\n");

        for (LeaveRecordResponse leave : leaves) {

            csv.append(leave.getLeaveId()).append(",")
               .append(leave.getEmployeeId()).append(",")
               .append(leave.getEmployeeName()).append(",")
               .append(leave.getLeaveType()).append(",")
               .append(leave.getStartDate()).append(",")
               .append(leave.getEndDate()).append(",")
               .append("\"")
               .append(leave.getReason() == null ? "" :
                       leave.getReason().replace("\"", "\"\""))
               .append("\",")
               .append(leave.getStatus())
               .append("\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=leave-report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString());
    }
}
