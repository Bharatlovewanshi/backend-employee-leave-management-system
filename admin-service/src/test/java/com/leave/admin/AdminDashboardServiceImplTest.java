
package com.leave.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.leave.admin.dto.AdminDashboardResponse;
import com.leave.admin.dto.EmployeeResponse;
import com.leave.admin.dto.LeaveRecordResponse;
import com.leave.admin.repository.ManagerRepository;
import com.leave.admin.service.AdminService;
import com.leave.admin.service.impl.AdminDashboardServiceImpl;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceImplTest {

    @Mock
    private AdminService adminService;

    @Mock
    private ManagerRepository managerRepository;

    @InjectMocks
    private AdminDashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {

        List<EmployeeResponse> employees = List.of(
                new EmployeeResponse(
                        1001L, "Anjali",
                        "anjali@gmail.com", "Developer", null),
                new EmployeeResponse(
                        1002L, "Rahul",
                        "rahul@gmail.com", "Tester", null),
                new EmployeeResponse(
                        1003L, "Priya",
                        "priya@gmail.com", "Manager", null)
        );

        List<LeaveRecordResponse> leaves = List.of(
                new LeaveRecordResponse(
                        501L, 1001L, "Anjali", "CASUAL",
                        null, null, "Personal work", "APPROVED"
                ),
                new LeaveRecordResponse(
                        502L, 1002L, "Rahul", "SICK",
                        null, null, "Medical appointment", "PENDING"
                ),
                new LeaveRecordResponse(
                        503L, 1003L, "Priya", "ANNUAL",
                        null, null, "Family vacation", "REJECTED"
                )
        );

        when(adminService.getAllEmployees())
                .thenReturn(employees);

        when(adminService.getAllLeaveRequests())
                .thenReturn(leaves);

        // Mock total manager count
        when(managerRepository.count())
                .thenReturn(2L);
    }

    @Test
    void testDashboardSummary() {

        AdminDashboardResponse response =
                dashboardService.getDashboardSummary();

        assertNotNull(response);

        assertEquals(3, response.getTotalEmployees());
        assertEquals(2, response.getTotalManagers());
        assertEquals(3, response.getTotalLeaveRequests());

        assertEquals(1, response.getApprovedLeaves());
        assertEquals(1, response.getPendingLeaves());
        assertEquals(1, response.getRejectedLeaves());
        assertEquals(0, response.getCancelledLeaves());
    }
}
