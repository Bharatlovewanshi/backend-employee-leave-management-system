
package com.leave.admin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.leave.admin.dto.AdminDashboardResponse;
import com.leave.admin.dto.EmployeeResponse;
import com.leave.admin.dto.LeaveRecordResponse;
import com.leave.admin.service.AdminDashboardService;
import com.leave.admin.service.AdminService;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final AdminService adminService;

    public AdminDashboardServiceImpl(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    public AdminDashboardResponse getDashboardSummary() {

        // Get employee records from AdminService
        List<EmployeeResponse> employees =
                adminService.getAllEmployees();

        // Get leave records from AdminService
        List<LeaveRecordResponse> leaves =
                adminService.getAllLeaveRequests();

        int totalEmployees = employees.size();
        int totalLeaveRequests = leaves.size();

        int approvedLeaves = (int) leaves.stream()
                .filter(leave -> "APPROVED".equalsIgnoreCase(
                        leave.getStatus()))
                .count();

        int pendingLeaves = (int) leaves.stream()
                .filter(leave -> "PENDING".equalsIgnoreCase(
                        leave.getStatus()))
                .count();

        int rejectedLeaves = (int) leaves.stream()
                .filter(leave -> "REJECTED".equalsIgnoreCase(
                        leave.getStatus()))
                .count();

        int cancelledLeaves = (int) leaves.stream()
                .filter(leave -> "CANCELLED".equalsIgnoreCase(
                        leave.getStatus()))
                .count();

        return new AdminDashboardResponse(
                totalEmployees,
                totalLeaveRequests,
                approvedLeaves,
                pendingLeaves,
                rejectedLeaves,
                cancelledLeaves
        );
    }
}
