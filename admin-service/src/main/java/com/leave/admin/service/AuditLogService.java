package com.leave.admin.service;

import java.util.List;
import com.leave.admin.entity.AuditLog;

public interface AuditLogService {

    AuditLog createAuditLog(AuditLog auditLog);

    List<AuditLog> getAllAuditLogs();

    List<AuditLog> getAuditLogsByEntity(
            String entityType,
            Long entityReference
    );
}