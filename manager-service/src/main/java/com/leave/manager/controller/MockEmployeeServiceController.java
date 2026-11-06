package com.leave.manager.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import com.leave.manager.dto.response.PendingLeaveResponse;

/**
 * Provides temporary Employee Service responses for local testing.
 */
@RestController
@RequestMapping("/mock-employee/api/leaves")
@Profile("mock-employee")
public class MockEmployeeServiceController {

    /**
     * Returns sample pending leave requests.
     *
     * @param managerId manager identifier
     * @return list of sample pending leave requests
     */
    @GetMapping("/pending/{managerId}")
    public List<PendingLeaveResponse> getPendingLeaves(
            @PathVariable Long managerId) {

        PendingLeaveResponse response = new PendingLeaveResponse();

        response.setLeaveRequestId(101L);
        response.setEmployeeId(201L);
        response.setEmployeeName("Rahul Sharma");
        response.setLeaveType("CASUAL");
        response.setStartDate(LocalDate.of(2026, 11, 10));
        response.setEndDate(LocalDate.of(2026, 11, 12));
        response.setReason("Personal work");
        response.setStatus("PENDING");

        return List.of(response);
    }
}