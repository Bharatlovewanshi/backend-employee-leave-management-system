
package com.leave.admin.service.impl;

import org.springframework.stereotype.Service;

import com.leave.admin.dto.AdminDashboardResponse;
import com.leave.admin.service.AdminDashboardService;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Override
    public AdminDashboardResponse getDashboardSummary() {

        // Temporary values until Employee and Leave Service APIs
        // are integrated.

        return new AdminDashboardResponse(
                0,  // Total employees
                0,  // Total leave requests
                0,  // Approved leaves
                0,  // Pending leaves
                0,  // Rejected leaves
                0   // Cancelled leaves
        );
    }
}
