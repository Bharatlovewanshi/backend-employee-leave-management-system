
package com.leave.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.leave.admin.entity.AuditLog;
import com.leave.admin.enums.AuditAction;
import com.leave.admin.repository.AuditLogRepository;
import com.leave.admin.service.impl.AuditLogServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @Test
    void testCreateAuditLog() {

        AuditLog auditLog = new AuditLog();
        auditLog.setAdminUserId(1L);
        auditLog.setAction(AuditAction.UPDATE);
        auditLog.setEntityType("LEAVE");
        auditLog.setEntityReference(501L);
        auditLog.setOldValue("PENDING");
        auditLog.setNewValue("APPROVED");
        auditLog.setReason("Approved by admin");

        when(auditLogRepository.save(auditLog))
                .thenReturn(auditLog);

        AuditLog result = auditLogService.createAuditLog(auditLog);

        assertNotNull(result);
        assertEquals("LEAVE", result.getEntityType());
        assertEquals("APPROVED", result.getNewValue());

        verify(auditLogRepository).save(auditLog);
    }

    @Test
    void testGetAllAuditLogs() {

        List<AuditLog> auditLogs = List.of(
                new AuditLog(),
                new AuditLog()
        );

        when(auditLogRepository.findAll())
                .thenReturn(auditLogs);

        List<AuditLog> result = auditLogService.getAllAuditLogs();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(auditLogRepository).findAll();
    }

    @Test
    void testGetAuditLogsByEntity() {

        List<AuditLog> auditLogs = List.of(new AuditLog());

        when(auditLogRepository
                .findByEntityTypeAndEntityReference("LEAVE", 501L))
                .thenReturn(auditLogs);

        List<AuditLog> result =
                auditLogService.getAuditLogsByEntity("LEAVE", 501L);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(auditLogRepository)
                .findByEntityTypeAndEntityReference("LEAVE", 501L);
    }
}
