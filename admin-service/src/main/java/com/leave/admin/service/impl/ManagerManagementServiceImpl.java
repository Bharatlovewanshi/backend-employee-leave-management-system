
package com.leave.admin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leave.admin.dto.ManagerRequest;
import com.leave.admin.dto.ManagerResponse;
import com.leave.admin.entity.AuditLog;
import com.leave.admin.entity.Manager;
import com.leave.admin.enums.AuditAction;
import com.leave.admin.exception.AdminConflictException;
import com.leave.admin.exception.AdminOperationException;
import com.leave.admin.repository.EmployeeRepository;
import com.leave.admin.repository.ManagerRepository;
import com.leave.admin.service.AuditLogService;
import com.leave.admin.service.ManagerManagementService;

@Service
@Transactional
public class ManagerManagementServiceImpl
        implements ManagerManagementService {

    private final ManagerRepository managerRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;

    public ManagerManagementServiceImpl(
            ManagerRepository managerRepository,
            EmployeeRepository employeeRepository,
            AuditLogService auditLogService) {

        this.managerRepository = managerRepository;
        this.employeeRepository = employeeRepository;
        this.auditLogService = auditLogService;
    }

    // CREATE MANAGER
    @Override
    public ManagerResponse createManager(ManagerRequest request) {

        if (managerRepository.existsByEmail(request.getEmail())) {
            throw new AdminOperationException(
                    "Manager already exists with email: " + request.getEmail());
        }

        Manager manager = new Manager();

        manager.setManagerName(request.getManagerName());
        manager.setEmail(request.getEmail());
        manager.setDesignation(request.getDesignation());

        Manager savedManager = managerRepository.save(manager);

        createManagerAuditLog(
                savedManager.getManagerId(),
                AuditAction.CREATE,
                null,
                savedManager.getManagerName());

        return convertToResponse(savedManager);
    }

    // GET ALL MANAGERS
    @Override
    @Transactional(readOnly = true)
    public List<ManagerResponse> getAllManagers() {

        return managerRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // GET MANAGER BY ID
    @Override
    @Transactional(readOnly = true)
    public ManagerResponse getManagerById(Long managerId) {

        Manager manager = findManager(managerId);

        return convertToResponse(manager);
    }

    // UPDATE MANAGER
    @Override
    public ManagerResponse updateManager(
            Long managerId, ManagerRequest request) {

        Manager manager = findManager(managerId);

        boolean emailBelongsToAnotherManager =
                managerRepository.existsByEmail(request.getEmail())
                && !manager.getEmail().equalsIgnoreCase(request.getEmail());

        if (emailBelongsToAnotherManager) {
            throw new AdminOperationException(
                    "Email already belongs to another manager");
        }

        String oldValue = manager.getManagerName();

        manager.setManagerName(request.getManagerName());
        manager.setEmail(request.getEmail());
        manager.setDesignation(request.getDesignation());

        Manager updatedManager = managerRepository.save(manager);

        createManagerAuditLog(
                updatedManager.getManagerId(),
                AuditAction.UPDATE,
                oldValue,
                updatedManager.getManagerName());

        return convertToResponse(updatedManager);
    }

    // DELETE MANAGER
    @Override
    public void deleteManager(Long managerId) {

        Manager manager = findManager(managerId);

        // Check whether employees are assigned to this manager
        boolean hasEmployees =
                employeeRepository.existsByManagerId(managerId);

        if (hasEmployees) {
            throw new AdminConflictException(
                    "Cannot delete manager with ID: " + managerId
                    + ". Please reassign all employees before deleting.");
        }

        // Create audit log only when deletion is allowed
        createManagerAuditLog(
                manager.getManagerId(),
                AuditAction.DELETE,
                manager.getManagerName(),
                null);

        managerRepository.delete(manager);
    }

    // FIND MANAGER
    private Manager findManager(Long managerId) {

        return managerRepository.findById(managerId)
                .orElseThrow(() -> new AdminOperationException(
                        "Manager not found with ID: " + managerId));
    }

    // CONVERT ENTITY TO RESPONSE DTO
    private ManagerResponse convertToResponse(Manager manager) {

        return new ManagerResponse(
                manager.getManagerId(),
                manager.getManagerName(),
                manager.getEmail(),
                manager.getDesignation());
    }

    // CREATE MANAGER AUDIT LOG
    private void createManagerAuditLog(
            Long managerId,
            AuditAction action,
            String oldValue,
            String newValue) {

        AuditLog auditLog = new AuditLog();

        // Temporary admin ID; replace with authenticated admin identity.
        auditLog.setAdminUserId(1L);

        auditLog.setAction(action);
        auditLog.setEntityType("MANAGER");
        auditLog.setEntityReference(managerId);
        auditLog.setOldValue(oldValue);
        auditLog.setNewValue(newValue);

        auditLog.setReason(
                "Manager " + action.name().toLowerCase() + " by admin");

        auditLogService.createAuditLog(auditLog);
    }
}
