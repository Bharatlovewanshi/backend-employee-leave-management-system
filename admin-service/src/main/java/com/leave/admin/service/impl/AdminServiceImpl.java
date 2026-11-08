
package com.leave.admin.service.impl;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leave.admin.dto.EmployeeResponse;
import com.leave.admin.dto.LeaveRecordResponse;
import com.leave.admin.entity.AuditLog;
import com.leave.admin.enums.AuditAction;
import com.leave.admin.exception.AdminOperationException;
import com.leave.admin.repository.EmployeeRepository;
import com.leave.admin.service.AdminService;
import com.leave.admin.service.AuditLogService;

@Service
public class AdminServiceImpl implements AdminService {

    private final Map<Long, LeaveRecordResponse> leaveRequests =
            new LinkedHashMap<>();

    private final AuditLogService auditLogService;
    private final EmployeeRepository employeeRepository;

    public AdminServiceImpl(
            AuditLogService auditLogService,
            EmployeeRepository employeeRepository) {

        this.auditLogService = auditLogService;
        this.employeeRepository = employeeRepository;

        // Temporary mock leave request data
        leaveRequests.put(501L, new LeaveRecordResponse(
                501L, 1001L, "Anjali", "CASUAL",
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 11),
                "Personal work", "PENDING"
        ));

        leaveRequests.put(502L, new LeaveRecordResponse(
                502L, 1002L, "Rahul", "SICK",
                LocalDate.of(2026, 11, 12),
                LocalDate.of(2026, 11, 13),
                "Medical appointment", "APPROVED"
        ));

        leaveRequests.put(503L, new LeaveRecordResponse(
                503L, 1003L, "Priya", "ANNUAL",
                LocalDate.of(2026, 11, 16),
                LocalDate.of(2026, 11, 18),
                "Family vacation", "REJECTED"
        ));
    }

    // Retrieve all employees from database
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {

        return employeeRepository.findAll()
                .stream()
                .map(employee -> new EmployeeResponse(
                        employee.getEmployeeId(),
                        employee.getEmployeeName(),
                        employee.getEmail(),
                        employee.getDesignation(),
                        employee.getManagerId()
                ))
                .toList();
    }

    // Retrieve employee by ID from database
    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long employeeId) {

        return employeeRepository.findById(employeeId)
                .map(employee -> new EmployeeResponse(
                        employee.getEmployeeId(),
                        employee.getEmployeeName(),
                        employee.getEmail(),
                        employee.getDesignation(),
                        employee.getManagerId()
                ))
                .orElseThrow(() -> new AdminOperationException(
                        "Employee not found with ID: " + employeeId
                ));
    }

    // Retrieve all leave requests
    @Override
    public List<LeaveRecordResponse> getAllLeaveRequests() {

        return List.copyOf(leaveRequests.values());
    }

    // Retrieve leave requests by status
    @Override
    public List<LeaveRecordResponse> getLeaveRequestsByStatus(
            String status) {

        if (status == null || status.isBlank()) {
            throw new AdminOperationException(
                    "Leave status is required");
        }

        return leaveRequests.values()
                .stream()
                .filter(leave -> status.equalsIgnoreCase(
                        leave.getStatus()))
                .toList();
    }

    // Retrieve leave requests by employee ID
    @Override
    public List<LeaveRecordResponse> getLeaveRequestsByEmployeeId(
            Long employeeId) {

        if (employeeId == null) {
            throw new AdminOperationException(
                    "Employee ID is required");
        }

        return leaveRequests.values()
                .stream()
                .filter(leave -> employeeId.equals(
                        leave.getEmployeeId()))
                .toList();
    }

    // Retrieve leave request by ID
    @Override
    public LeaveRecordResponse getLeaveRequestById(Long leaveId) {

        LeaveRecordResponse leave = leaveRequests.get(leaveId);

        if (leave == null) {
            throw new AdminOperationException(
                    "Leave request not found with ID: " + leaveId
            );
        }

        return leave;
    }

    // Approve leave request and create audit log
    @Override
    @Transactional
    public LeaveRecordResponse approveLeaveRequest(
            Long leaveId, Long adminUserId) {

        LeaveRecordResponse leave = getLeaveRequestById(leaveId);

        if (!"PENDING".equals(leave.getStatus())) {
            throw new AdminOperationException(
                    "Only pending leave requests can be approved"
            );
        }

        String oldStatus = leave.getStatus();
        leave.setStatus("APPROVED");

        AuditLog auditLog = new AuditLog();
        auditLog.setAdminUserId(adminUserId);
        auditLog.setAction(AuditAction.UPDATE);
        auditLog.setEntityType("LEAVE");
        auditLog.setEntityReference(leaveId);
        auditLog.setOldValue(oldStatus);
        auditLog.setNewValue("APPROVED");
        auditLog.setReason("Leave approved by admin");

        auditLogService.createAuditLog(auditLog);

        return leave;
    }

    // Reject leave request and create audit log
    @Override
    @Transactional
    public LeaveRecordResponse rejectLeaveRequest(
            Long leaveId, Long adminUserId, String reason) {

        LeaveRecordResponse leave = getLeaveRequestById(leaveId);

        if (!"PENDING".equals(leave.getStatus())) {
            throw new AdminOperationException(
                    "Only pending leave requests can be rejected"
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new AdminOperationException(
                    "Rejection reason is required"
            );
        }

        String oldStatus = leave.getStatus();
        leave.setStatus("REJECTED");
        leave.setReason(reason);

        AuditLog auditLog = new AuditLog();
        auditLog.setAdminUserId(adminUserId);
        auditLog.setAction(AuditAction.UPDATE);
        auditLog.setEntityType("LEAVE");
        auditLog.setEntityReference(leaveId);
        auditLog.setOldValue(oldStatus);
        auditLog.setNewValue("REJECTED");
        auditLog.setReason(reason);

        auditLogService.createAuditLog(auditLog);

        return leave;
    }
}
