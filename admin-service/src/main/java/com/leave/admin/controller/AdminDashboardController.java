
package com.leave.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.leave.admin.dto.AdminDashboardResponse;
import com.leave.admin.service.AdminDashboardService;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    // Constructor injection
    public AdminDashboardController(
            AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    // Get admin dashboard summary
    @GetMapping
    public ResponseEntity<AdminDashboardResponse> getDashboardSummary() {

        AdminDashboardResponse response =
                adminDashboardService.getDashboardSummary();

        return ResponseEntity.ok(response);
    }
}
