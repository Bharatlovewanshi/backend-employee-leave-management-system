
package com.leave.admin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import com.leave.admin.entity.AuditLog;
import com.leave.admin.service.AuditLogService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    // Constructor injection
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    // Create a new audit log
    @PostMapping
    public ResponseEntity<AuditLog> createAuditLog(
            @Valid @RequestBody AuditLog auditLog) {

        AuditLog savedAuditLog =
                auditLogService.createAuditLog(auditLog);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedAuditLog);
    }

    // Retrieve all audit logs
    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {

        List<AuditLog> auditLogs =
                auditLogService.getAllAuditLogs();

        return ResponseEntity.ok(auditLogs);
    }
 // Retrieve audit logs for a specific entity
    @GetMapping("/{entityType}/{entityReference}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityReference) {

        List<AuditLog> auditLogs =
                auditLogService.getAuditLogsByEntity(
                        entityType,
                        entityReference
                );

        return ResponseEntity.ok(auditLogs);
    }
}
