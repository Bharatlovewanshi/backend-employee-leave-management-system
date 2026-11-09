
package com.leave.admin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.leave.admin.dto.AdminDashboardResponse;
import com.leave.admin.dto.EmployeeResponse;
import com.leave.admin.dto.LeaveRecordResponse;
import com.leave.admin.repository.ManagerRepository;
import com.leave.admin.service.AdminDashboardService;
import com.leave.admin.service.AdminService;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final AdminService adminService;
    private final ManagerRepository managerRepository;

    public AdminDashboardServiceImpl(
            AdminService adminService,
            ManagerRepository managerRepository) {

        this.adminService = adminService;
        this.managerRepository = managerRepository;
    }

    @Override
    public AdminDashboardResponse getDashboardSummary() {

        // Get employee records from AdminService
        List<EmployeeResponse> employees =
                adminService.getAllEmployees();

        // Get leave records from AdminService
        List<LeaveRecordResponse> leaves =
                adminService.getAllLeaveRequests();

        // Get total managers from ManagerRepository
        long totalManagers = managerRepository.count();

        long totalEmployees = employees.size();
        long totalLeaveRequests = leaves.size();

        long approvedLeaves = leaves.stream()
                .filter(leave -> "APPROVED".equalsIgnoreCase(
                        leave.getStatus()))
                .count();

        long pendingLeaves = leaves.stream()
                .filter(leave -> "PENDING".equalsIgnoreCase(
                        leave.getStatus()))
                .count();

        long rejectedLeaves = leaves.stream()
                .filter(leave -> "REJECTED".equalsIgnoreCase(
                        leave.getStatus()))
                .count();

        long cancelledLeaves = leaves.stream()
                .filter(leave -> "CANCELLED".equalsIgnoreCase(
                        leave.getStatus()))
                .count();

        return new AdminDashboardResponse(
                totalEmployees,
                totalManagers,
                totalLeaveRequests,
                approvedLeaves,
                pendingLeaves,
                rejectedLeaves,
                cancelledLeaves
        );
    }
}