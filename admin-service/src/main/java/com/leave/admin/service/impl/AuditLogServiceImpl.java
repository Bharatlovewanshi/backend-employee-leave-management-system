
package com.leave.admin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.leave.admin.entity.AuditLog;
import com.leave.admin.repository.AuditLogRepository;
import com.leave.admin.service.AuditLogService;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    // Constructor injection
    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // Create and save an audit log
    @Override
    public AuditLog createAuditLog(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    // Retrieve all audit logs
    @Override
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }
}
