
package com.leave.admin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leave.admin.dto.EmployeeRequest;
import com.leave.admin.dto.EmployeeResponse;
import com.leave.admin.dto.ManagerAssignmentRequest;
import com.leave.admin.dto.ManagerResponse;
import com.leave.admin.entity.AuditLog;
import com.leave.admin.entity.Employee;
import com.leave.admin.entity.Manager;
import com.leave.admin.enums.AuditAction;
import com.leave.admin.exception.AdminOperationException;
import com.leave.admin.repository.EmployeeRepository;
import com.leave.admin.repository.ManagerRepository;
import com.leave.admin.service.AuditLogService;
import com.leave.admin.service.EmployeeManagementService;

@Service
@Transactional
public class EmployeeManagementServiceImpl
        implements EmployeeManagementService {

    private final EmployeeRepository employeeRepository;
    private final ManagerRepository managerRepository;
    private final AuditLogService auditLogService;

    public EmployeeManagementServiceImpl(
            EmployeeRepository employeeRepository,
            ManagerRepository managerRepository,
            AuditLogService auditLogService) {

        this.employeeRepository = employeeRepository;
        this.managerRepository = managerRepository;
        this.auditLogService = auditLogService;
    }

    // CREATE EMPLOYEE
    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {

        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new AdminOperationException(
                    "Employee already exists with email: "
                            + request.getEmail());
        }

        Employee employee = new Employee();
        employee.setEmployeeName(request.getEmployeeName());
        employee.setEmail(request.getEmail());
        employee.setDesignation(request.getDesignation());

        Employee savedEmployee = employeeRepository.save(employee);

        createEmployeeAuditLog(
                savedEmployee.getEmployeeId(),
                AuditAction.CREATE,
                null,
                savedEmployee.getEmployeeName());

        return convertToResponse(savedEmployee);
    }

    // GET ALL EMPLOYEES
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {

        return employeeRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // GET EMPLOYEE BY ID
    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long employeeId) {

        Employee employee = findEmployee(employeeId);

        return convertToResponse(employee);
    }

    // UPDATE EMPLOYEE
    @Override
    public EmployeeResponse updateEmployee(
            Long employeeId,
            EmployeeRequest request) {

        Employee employee = findEmployee(employeeId);

        boolean emailBelongsToAnotherEmployee =
                employeeRepository.existsByEmail(request.getEmail())
                && !employee.getEmail().equalsIgnoreCase(
                        request.getEmail());

        if (emailBelongsToAnotherEmployee) {
            throw new AdminOperationException(
                    "Email already belongs to another employee");
        }

        String oldValue = employee.getEmployeeName();

        employee.setEmployeeName(request.getEmployeeName());
        employee.setEmail(request.getEmail());
        employee.setDesignation(request.getDesignation());

        Employee updatedEmployee = employeeRepository.save(employee);

        createEmployeeAuditLog(
                updatedEmployee.getEmployeeId(),
                AuditAction.UPDATE,
                oldValue,
                updatedEmployee.getEmployeeName());

        return convertToResponse(updatedEmployee);
    }

    // DELETE EMPLOYEE
    @Override
    public void deleteEmployee(Long employeeId) {

        Employee employee = findEmployee(employeeId);

        createEmployeeAuditLog(
                employee.getEmployeeId(),
                AuditAction.DELETE,
                employee.getEmployeeName(),
                null);

        employeeRepository.delete(employee);
    }

    // ASSIGN OR REASSIGN MANAGER
    @Override
    public EmployeeResponse assignManager(
            Long employeeId,
            ManagerAssignmentRequest request) {

        Employee employee = findEmployee(employeeId);

        Long managerId = request.getManagerId();

        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new AdminOperationException(
                        "Manager not found with ID: " + managerId));

        Long oldManagerId = employee.getManagerId();

        employee.setManagerId(manager.getManagerId());

        Employee updatedEmployee = employeeRepository.save(employee);

        AuditLog auditLog = new AuditLog();

        auditLog.setAdminUserId(1L);
        auditLog.setAction(AuditAction.UPDATE);
        auditLog.setEntityType("EMPLOYEE");
        auditLog.setEntityReference(employeeId);
        auditLog.setOldValue(
                oldManagerId == null ? null : oldManagerId.toString());
        auditLog.setNewValue(manager.getManagerId().toString());
        auditLog.setReason(
                oldManagerId == null
                        ? "Manager assigned to employee by admin"
                        : "Employee manager reassigned by admin");

        auditLogService.createAuditLog(auditLog);

        return convertToResponse(updatedEmployee);
    }

    // GET MANAGER ASSIGNED TO AN EMPLOYEE
    @Override
    @Transactional(readOnly = true)
    public ManagerResponse getAssignedManager(Long employeeId) {

        Employee employee = findEmployee(employeeId);

        Long managerId = employee.getManagerId();

        if (managerId == null) {
            throw new AdminOperationException(
                    "No manager assigned to employee ID: " + employeeId);
        }

        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new AdminOperationException(
                        "Manager not found with ID: " + managerId));

        return convertManagerToResponse(manager);
    }

    // GET ALL EMPLOYEES UNDER A MANAGER
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByManager(Long managerId) {

        managerRepository.findById(managerId)
                .orElseThrow(() -> new AdminOperationException(
                        "Manager not found with ID: " + managerId));

        return employeeRepository.findByManagerId(managerId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // FIND EMPLOYEE
    private Employee findEmployee(Long employeeId) {

        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AdminOperationException(
                        "Employee not found with ID: " + employeeId));
    }

    // CONVERT EMPLOYEE ENTITY TO RESPONSE DTO
    private EmployeeResponse convertToResponse(Employee employee) {

        return new EmployeeResponse(
                employee.getEmployeeId(),
                employee.getEmployeeName(),
                employee.getEmail(),
                employee.getDesignation(),
                employee.getManagerId());
    }

    // CONVERT MANAGER ENTITY TO RESPONSE DTO
    private ManagerResponse convertManagerToResponse(Manager manager) {

        return new ManagerResponse(
                manager.getManagerId(),
                manager.getManagerName(),
                manager.getEmail(),
                manager.getDesignation());
    }

    // CREATE EMPLOYEE AUDIT LOG
    private void createEmployeeAuditLog(
            Long employeeId,
            AuditAction action,
            String oldValue,
            String newValue) {

        AuditLog auditLog = new AuditLog();

        // Temporary admin ID; replace with authenticated admin identity.
        auditLog.setAdminUserId(1L);
        auditLog.setAction(action);
        auditLog.setEntityType("EMPLOYEE");
        auditLog.setEntityReference(employeeId);
        auditLog.setOldValue(oldValue);
        auditLog.setNewValue(newValue);
        auditLog.setReason(
                "Employee " + action.name().toLowerCase()
                        + " by admin");

        auditLogService.createAuditLog(auditLog);
    }
}
