
package com.leave.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.leave.admin.dto.EmployeeResponse;
import com.leave.admin.dto.LeaveRecordResponse;
import com.leave.admin.entity.AuditLog;
import com.leave.admin.exception.AdminOperationException;
import com.leave.admin.service.AuditLogService;
import com.leave.admin.service.impl.AdminServiceImpl;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        // Mockito injects the mocked AuditLogService.
        // AdminServiceImpl initializes its sample leave records.
    }

    @Test
    void testGetAllEmployees() {

        List<EmployeeResponse> employees =
                adminService.getAllEmployees();

        assertNotNull(employees);
        assertEquals(3, employees.size());
    }

    @Test
    void testGetEmployeeById() {

        EmployeeResponse employee =
                adminService.getEmployeeById(1001L);

        assertNotNull(employee);
        assertEquals("Anjali", employee.getEmployeeName());
    }

    @Test
    void testGetEmployeeByInvalidId() {

        assertThrows(
                AdminOperationException.class,
                () -> adminService.getEmployeeById(9999L)
        );
    }

    @Test
    void testGetAllLeaveRequests() {

        List<LeaveRecordResponse> leaves =
                adminService.getAllLeaveRequests();

        assertNotNull(leaves);
        assertEquals(3, leaves.size());
    }

    @Test
    void testGetLeaveRequestById() {

        LeaveRecordResponse leave =
                adminService.getLeaveRequestById(501L);

        assertNotNull(leave);
        assertEquals("PENDING", leave.getStatus());
    }

    @Test
    void testGetLeaveRequestByInvalidId() {

        assertThrows(
                AdminOperationException.class,
                () -> adminService.getLeaveRequestById(999L)
        );
    }

    @Test
    void testApproveLeaveRequest() {

        LeaveRecordResponse response =
                adminService.approveLeaveRequest(501L, 1L);

        assertEquals("APPROVED", response.getStatus());

        verify(auditLogService).createAuditLog(
                org.mockito.ArgumentMatchers.any(AuditLog.class)
        );
    }

    @Test
    void testRejectLeaveRequest() {

        LeaveRecordResponse response =
                adminService.rejectLeaveRequest(
                        501L, 1L, "Insufficient leave balance"
                );

        assertEquals("REJECTED", response.getStatus());
        assertEquals(
                "Insufficient leave balance",
                response.getReason()
        );

        verify(auditLogService).createAuditLog(
                org.mockito.ArgumentMatchers.any(AuditLog.class)
        );
    }

    @Test
    void testRejectLeaveRequestWithoutReason() {

        assertThrows(
                AdminOperationException.class,
                () -> adminService.rejectLeaveRequest(501L, 1L, " ")
        );
    }
}
