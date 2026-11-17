
package com.leave.manager.service;

import static org.mockito.Mockito.lenient;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import com.leave.manager.client.EmployeeServiceClient;
import com.leave.manager.dto.request.ManagerDecisionRequest;
import com.leave.manager.dto.response.ManagerDecisionResponse;
import com.leave.manager.dto.response.PendingLeaveResponse;
import com.leave.manager.entity.ManagerDecision;
import com.leave.manager.enums.DecisionType;
import com.leave.manager.exception.InvalidDecisionException;
import com.leave.manager.exception.ServiceCommunicationException;
import com.leave.manager.repository.ManagerDecisionRepository;
import com.leave.manager.security.JwtService;

import feign.FeignException;

@ExtendWith(MockitoExtension.class)
class ManagerDecisionServiceTest {

    private static final String TOKEN = "test.jwt.token";
    private static final String AUTHORIZATION = "Bearer " + TOKEN;
    private static final String MANAGER_EMAIL = "manager@example.com";

    @Mock
    private ManagerDecisionRepository managerDecisionRepository;

    @Mock
    private EmployeeServiceClient employeeServiceClient;

    @Mock
    private NotificationService notificationService;

    @Mock
    private JwtService jwtService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ManagerDecisionServiceImpl managerDecisionService;

    @BeforeEach
    void setUpAuthentication() {

        lenient().when(jwtService.isTokenValid(TOKEN))
                .thenReturn(true);

        lenient().when(jwtService.extractRole(TOKEN))
                .thenReturn("MANAGER");

        lenient().when(jwtService.extractEmail(TOKEN))
                .thenReturn(MANAGER_EMAIL);

        lenient().when(jdbcTemplate.queryForObject(
                eq("SELECT manager_id FROM admin_managers WHERE email = ?"),
                eq(Long.class),
                eq(MANAGER_EMAIL)
        )).thenReturn(301L);
    }

    // Common pending leave request
    private PendingLeaveResponse createPendingLeave() {

        PendingLeaveResponse leave = new PendingLeaveResponse();

        leave.setLeaveRequestId(105L);
        leave.setEmployeeId(205L);
        leave.setEmployeeName("Mohit Sharma");
        leave.setEmployeeEmail("test@example.com");
        leave.setLeaveType("CASUAL");
        leave.setStartDate(LocalDate.of(2026, 11, 12));
        leave.setEndDate(LocalDate.of(2026, 11, 13));
        leave.setReason("Personal work");
        leave.setStatus("PENDING");

        return leave;
    }

 // Common decision request
    private ManagerDecisionRequest createDecisionRequest(
            Long leaveRequestId,
            DecisionType decisionType) {

        ManagerDecisionRequest request = new ManagerDecisionRequest();

        request.setLeaveRequestId(leaveRequestId);
        request.setDecision(decisionType);

        return request;
    }

    // Test 1: Successful leave approval
    @Test
    void shouldApproveLeaveSuccessfully() {

        ManagerDecisionRequest request =
                createDecisionRequest(105L, DecisionType.APPROVED);

        PendingLeaveResponse pendingLeave = createPendingLeave();

        PendingLeaveResponse updatedLeave = createPendingLeave();
        updatedLeave.setStatus("APPROVED");

        when(managerDecisionRepository.findByLeaveRequestId(105L))
                .thenReturn(Optional.empty());

        when(employeeServiceClient.getPendingLeaves(AUTHORIZATION))
                .thenReturn(List.of(pendingLeave));

        when(employeeServiceClient.decideLeave(
                105L,
                "APPROVED",
                AUTHORIZATION
        )).thenReturn(updatedLeave);

        when(employeeServiceClient.getLeaveDetails(
                105L,
                AUTHORIZATION
        )).thenReturn(updatedLeave);

        when(managerDecisionRepository.saveAndFlush(
                any(ManagerDecision.class)
        )).thenAnswer(invocation -> {

            ManagerDecision decision = invocation.getArgument(0);

            decision.setId(8L);
            decision.setDecisionAt(LocalDateTime.now());

            return decision;
        });

        ManagerDecisionResponse response =
                managerDecisionService.processDecision(
                        request,
                        AUTHORIZATION
                );

        assertNotNull(response);
        assertEquals(8L, response.getId());
        assertEquals(105L, response.getLeaveRequestId());
        assertEquals(301L, response.getManagerId());
        assertEquals(DecisionType.APPROVED, response.getDecision());
        assertEquals(205L, response.getEmployeeId());

        // Verify employee leave status was updated.
        verify(employeeServiceClient).decideLeave(
                105L,
                "APPROVED",
                AUTHORIZATION
        );

        // Verify manager decision was saved.
        verify(managerDecisionRepository)
                .saveAndFlush(any(ManagerDecision.class));

        // Employee receives in-app notification.
        verify(notificationService).sendInAppNotification(
                any(ManagerDecision.class),
                eq(205L),
                anyString()
        );

        // Employee receives email notification.
        verify(notificationService).sendEmail(
                any(ManagerDecision.class),
                eq(205L),
                eq("test@example.com"),
                anyString(),
                anyString()
        );

        // No manager apply/cancel notification should be created here.
        verify(notificationService, never()).createManagerLeaveEvent(
                anyLong(),
                anyLong(),
                anyLong(),
                anyString(),
                anyString()
        );
    }

    // Test 2: Rejection without reason
    @Test
    void shouldThrowExceptionWhenRejectionReasonIsMissing() {

        ManagerDecisionRequest request =
                createDecisionRequest(106L, DecisionType.REJECTED);

        request.setRejectionReason(" ");

        assertThrows(
                InvalidDecisionException.class,
                () -> managerDecisionService.processDecision(
                        request,
                        AUTHORIZATION
                )
        );

        verifyNoInteractions(employeeServiceClient);

        verify(managerDecisionRepository, never())
                .saveAndFlush(any(ManagerDecision.class));

        verifyNoInteractions(notificationService);
    }

    // Test 3: Duplicate decision prevention
    @Test
    void shouldThrowExceptionWhenDecisionAlreadyExists() {

        ManagerDecisionRequest request =
                createDecisionRequest(105L, DecisionType.APPROVED);

        ManagerDecision existingDecision = new ManagerDecision();

        existingDecision.setId(8L);
        existingDecision.setLeaveRequestId(105L);

        when(managerDecisionRepository.findByLeaveRequestId(105L))
                .thenReturn(Optional.of(existingDecision));

        assertThrows(
                InvalidDecisionException.class,
                () -> managerDecisionService.processDecision(
                        request,
                        AUTHORIZATION
                )
        );

        verify(employeeServiceClient, never())
                .getPendingLeaves(anyString());

        verify(employeeServiceClient, never())
                .decideLeave(anyLong(), anyString(), anyString());

        verify(managerDecisionRepository, never())
                .saveAndFlush(any(ManagerDecision.class));

        verifyNoInteractions(notificationService);
    }

    // Test 4: Pending leave request not found
    @Test
    void shouldThrowExceptionWhenPendingLeaveNotFound() {

        ManagerDecisionRequest request =
                createDecisionRequest(999L, DecisionType.APPROVED);

        when(managerDecisionRepository.findByLeaveRequestId(999L))
                .thenReturn(Optional.empty());

        when(employeeServiceClient.getPendingLeaves(AUTHORIZATION))
                .thenReturn(List.of());

        assertThrows(
                InvalidDecisionException.class,
                () -> managerDecisionService.processDecision(
                        request,
                        AUTHORIZATION
                )
        );

        verify(employeeServiceClient)
                .getPendingLeaves(AUTHORIZATION);

        verify(employeeServiceClient, never())
                .decideLeave(anyLong(), anyString(), anyString());

        verify(managerDecisionRepository, never())
                .saveAndFlush(any(ManagerDecision.class));

        verifyNoInteractions(notificationService);
    }

    // Test 5: Employee Service unavailable
    @Test
    void shouldThrowExceptionWhenEmployeeServiceIsUnavailable() {

        ManagerDecisionRequest request =
                createDecisionRequest(107L, DecisionType.APPROVED);

        when(managerDecisionRepository.findByLeaveRequestId(107L))
                .thenReturn(Optional.empty());

        FeignException exception = mock(FeignException.class);

        when(exception.status()).thenReturn(503);
        when(exception.contentUTF8())
                .thenReturn("Employee Service unavailable");

        when(employeeServiceClient.getPendingLeaves(AUTHORIZATION))
                .thenThrow(exception);

        assertThrows(
                ServiceCommunicationException.class,
                () -> managerDecisionService.processDecision(
                        request,
                        AUTHORIZATION
                )
        );

        verify(employeeServiceClient)
                .getPendingLeaves(AUTHORIZATION);

        verify(employeeServiceClient, never())
                .decideLeave(anyLong(), anyString(), anyString());

        verify(managerDecisionRepository, never())
                .saveAndFlush(any(ManagerDecision.class));

        verifyNoInteractions(notificationService);
    }

    // Test 6: Invalid JWT role
    @Test
    void shouldRejectNonManagerUser() {

        ManagerDecisionRequest request =
                createDecisionRequest(105L, DecisionType.APPROVED);

        when(jwtService.extractRole(TOKEN))
                .thenReturn("EMPLOYEE");

        assertThrows(
                InvalidDecisionException.class,
                () -> managerDecisionService.processDecision(
                        request,
                        AUTHORIZATION
                )
        );

        verifyNoInteractions(employeeServiceClient);
        verifyNoInteractions(notificationService);

        verify(managerDecisionRepository, never())
                .saveAndFlush(any(ManagerDecision.class));
    }

    // Test 7: Invalid or missing authorization token
    @Test
    void shouldRejectMissingAuthorizationToken() {

        ManagerDecisionRequest request =
                createDecisionRequest(105L, DecisionType.APPROVED);

        assertThrows(
                InvalidDecisionException.class,
                () -> managerDecisionService.processDecision(
                        request,
                        null
                )
        );

        verifyNoInteractions(employeeServiceClient);
        verifyNoInteractions(notificationService);
    }
}
