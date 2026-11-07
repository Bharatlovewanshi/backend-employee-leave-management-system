
package com.leave.manager.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import com.leave.manager.dto.response.PendingLeaveResponse;
import com.leave.manager.repository.ManagerDecisionRepository;

@RestController
@RequestMapping("/mock-employee/api/leaves")
@Profile("mock-employee")
public class MockEmployeeServiceController {

    private final ManagerDecisionRepository managerDecisionRepository;

    public MockEmployeeServiceController(
            ManagerDecisionRepository managerDecisionRepository) {
        this.managerDecisionRepository = managerDecisionRepository;
    }

    @GetMapping("/pending/{managerId}")
    public List<PendingLeaveResponse> getPendingLeaves(
            @PathVariable Long managerId) {

        PendingLeaveResponse response1 = new PendingLeaveResponse();

        response1.setLeaveRequestId(105L);
        response1.setEmployeeId(205L);
        response1.setEmployeeName("Mohit Sharma");
        response1.setEmployeeEmail("lovewanshibharat22@gmail.com");
        response1.setLeaveType("CASUAL");
        response1.setStartDate(LocalDate.of(2026, 11, 12));
        response1.setEndDate(LocalDate.of(2026, 11, 13));
        response1.setReason("Personal work");
        response1.setStatus("PENDING");

        PendingLeaveResponse response2 = new PendingLeaveResponse();

        response2.setLeaveRequestId(106L);
        response2.setEmployeeId(206L);
        response2.setEmployeeName("Rahul Verma");
        response2.setEmployeeEmail("rahul.verma@example.com");
        response2.setLeaveType("SICK");
        response2.setStartDate(LocalDate.of(2026, 11, 17));
        response2.setEndDate(LocalDate.of(2026, 11, 18));
        response2.setReason("Medical checkup");
        response2.setStatus("PENDING");

        PendingLeaveResponse response3 = new PendingLeaveResponse();

        response3.setLeaveRequestId(107L);
        response3.setEmployeeId(207L);
        response3.setEmployeeName("Amit Kumar");
        response3.setEmployeeEmail("amit.kumar@example.com");
        response3.setLeaveType("CASUAL");
        response3.setStartDate(LocalDate.of(2026, 11, 20));
        response3.setEndDate(LocalDate.of(2026, 11, 21));
        response3.setReason("Personal travel");
        response3.setStatus("PENDING");

        List<PendingLeaveResponse> mockLeaves =
                List.of(response1, response2, response3);

        // Return only leave requests without an existing decision.
        return mockLeaves.stream()
                .filter(leave ->
                        managerDecisionRepository
                                .findByLeaveRequestId(
                                        leave.getLeaveRequestId())
                                .isEmpty())
                .toList();
    }
}
