
package com.leave.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.leave.admin.dto.EmployeeResponse;
import com.leave.admin.dto.LeaveRecordResponse;
import com.leave.admin.entity.AuditLog;
import com.leave.admin.entity.Employee;
import com.leave.admin.exception.AdminOperationException;
import com.leave.admin.repository.EmployeeRepository;
import com.leave.admin.service.AuditLogService;
import com.leave.admin.service.impl.AdminServiceImpl;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    private Employee employee1;
    private Employee employee2;
    private Employee employee3;

    @BeforeEach
    void setUp() {

        employee1 = new Employee();
        employee1.setEmployeeId(1001L);
        employee1.setEmployeeName("Anjali");
        employee1.setEmail("anjali@gmail.com");
        employee1.setDesignation("Developer");
        employee1.setManagerId(null);

        employee2 = new Employee();
        employee2.setEmployeeId(1002L);
        employee2.setEmployeeName("Rahul");
        employee2.setEmail("rahul@gmail.com");
        employee2.setDesignation("Tester");
        employee2.setManagerId(null);

        employee3 = new Employee();
        employee3.setEmployeeId(1003L);
        employee3.setEmployeeName("Priya");
        employee3.setEmail("priya@gmail.com");
        employee3.setDesignation("Analyst");
        employee3.setManagerId(null);
    }

    @Test
    void testGetAllEmployees() {

        when(employeeRepository.findAll())
                .thenReturn(List.of(employee1, employee2, employee3));

        List<EmployeeResponse> employees =
                adminService.getAllEmployees();

        assertNotNull(employees);
        assertEquals(3, employees.size());
        assertEquals("Anjali", employees.get(0).getEmployeeName());
    }

    @Test
    void testGetEmployeeById() {

        when(employeeRepository.findById(1001L))
                .thenReturn(Optional.of(employee1));

        EmployeeResponse employee =
                adminService.getEmployeeById(1001L);

        assertNotNull(employee);
        assertEquals("Anjali", employee.getEmployeeName());
        assertEquals(1001L, employee.getEmployeeId());
    }

    @Test
    void testGetEmployeeByInvalidId() {

        when(employeeRepository.findById(9999L))
                .thenReturn(Optional.empty());

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
