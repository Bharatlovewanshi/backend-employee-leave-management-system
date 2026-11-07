package com.leave.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.leave.manager.client.EmployeeServiceClient;
import com.leave.manager.dto.request.ManagerDecisionRequest;
import com.leave.manager.dto.response.ManagerDecisionResponse;
import com.leave.manager.dto.response.PendingLeaveResponse;
import com.leave.manager.entity.ManagerDecision;
import com.leave.manager.enums.DecisionType;
import com.leave.manager.exception.InvalidDecisionException;
import com.leave.manager.exception.ServiceCommunicationException;
import com.leave.manager.repository.ManagerDecisionRepository;

import feign.FeignException;
import feign.Request;

@ExtendWith(MockitoExtension.class)
class ManagerDecisionServiceTest {

    @Mock
    private ManagerDecisionRepository managerDecisionRepository;

    @Mock
    private EmployeeServiceClient employeeServiceClient;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ManagerDecisionServiceImpl managerDecisionService;

    // Test 1: Successful leave approval
    @Test
    void shouldApproveLeaveSuccessfully() {

        ManagerDecisionRequest request = new ManagerDecisionRequest();
        request.setLeaveRequestId(105L);
        request.setManagerId(301L);
        request.setDecision(DecisionType.APPROVED);

        PendingLeaveResponse pendingLeave = new PendingLeaveResponse();
        pendingLeave.setLeaveRequestId(105L);
        pendingLeave.setEmployeeId(205L);
        pendingLeave.setEmployeeName("Mohit Sharma");
        pendingLeave.setEmployeeEmail("test@example.com");
        pendingLeave.setLeaveType("CASUAL");
        pendingLeave.setStartDate(LocalDate.of(2026, 11, 12));
        pendingLeave.setEndDate(LocalDate.of(2026, 11, 13));
        pendingLeave.setReason("Personal work");
        pendingLeave.setStatus("PENDING");

        when(managerDecisionRepository.findByLeaveRequestId(105L))
                .thenReturn(Optional.empty());

        when(employeeServiceClient.getPendingLeaves(301L))
                .thenReturn(List.of(pendingLeave));

        when(managerDecisionRepository.saveAndFlush(any(ManagerDecision.class)))
                .thenAnswer(invocation -> {
                    ManagerDecision decision = invocation.getArgument(0);
                    decision.setId(8L);
                    decision.setDecisionAt(LocalDateTime.now());
                    return decision;
                });

        ManagerDecisionResponse response =
                managerDecisionService.processDecision(request);

        assertNotNull(response);
        assertEquals(8L, response.getId());
        assertEquals(105L, response.getLeaveRequestId());
        assertEquals(301L, response.getManagerId());
        assertEquals(DecisionType.APPROVED, response.getDecision());

        verify(notificationService).sendInAppNotification(
                any(ManagerDecision.class), eq(205L), anyString());

        verify(notificationService).sendEmail(
                any(ManagerDecision.class),
                eq(205L),
                eq("test@example.com"),
                anyString(),
                anyString());
    }

    // Test 2: Rejection without reason
    @Test
    void shouldThrowExceptionWhenRejectionReasonIsMissing() {

        ManagerDecisionRequest request = new ManagerDecisionRequest();
        request.setLeaveRequestId(106L);
        request.setManagerId(301L);
        request.setDecision(DecisionType.REJECTED);
        request.setRejectionReason(" ");

        assertThrows(
                InvalidDecisionException.class,
                () -> managerDecisionService.processDecision(request)
        );

        verifyNoInteractions(employeeServiceClient);
        verify(managerDecisionRepository, never())
                .saveAndFlush(any(ManagerDecision.class));
        verifyNoInteractions(notificationService);
    }

    // Test 3: Duplicate decision prevention
    @Test
    void shouldThrowExceptionWhenDecisionAlreadyExists() {

        ManagerDecisionRequest request = new ManagerDecisionRequest();
        request.setLeaveRequestId(105L);
        request.setManagerId(301L);
        request.setDecision(DecisionType.APPROVED);

        ManagerDecision existingDecision = new ManagerDecision();
        existingDecision.setId(8L);
        existingDecision.setLeaveRequestId(105L);

        when(managerDecisionRepository.findByLeaveRequestId(105L))
                .thenReturn(Optional.of(existingDecision));

        assertThrows(
                InvalidDecisionException.class,
                () -> managerDecisionService.processDecision(request)
        );

        verify(employeeServiceClient, never()).getPendingLeaves(anyLong());

        verify(managerDecisionRepository, never())
                .saveAndFlush(any(ManagerDecision.class));

        verifyNoInteractions(notificationService);
    }

    // Test 4: Pending leave request not found
    @Test
    void shouldThrowExceptionWhenPendingLeaveNotFound() {

        ManagerDecisionRequest request = new ManagerDecisionRequest();
        request.setLeaveRequestId(999L);
        request.setManagerId(301L);
        request.setDecision(DecisionType.APPROVED);

        when(managerDecisionRepository.findByLeaveRequestId(999L))
                .thenReturn(Optional.empty());

        when(employeeServiceClient.getPendingLeaves(301L))
                .thenReturn(List.of());

        assertThrows(
                InvalidDecisionException.class,
                () -> managerDecisionService.processDecision(request)
        );

        verify(managerDecisionRepository, never())
                .saveAndFlush(any(ManagerDecision.class));

        verifyNoInteractions(notificationService);
    }

    // Test 5: Employee Service unavailable
    @Test
    void shouldThrowExceptionWhenEmployeeServiceIsUnavailable() {

        ManagerDecisionRequest request = new ManagerDecisionRequest();
        request.setLeaveRequestId(107L);
        request.setManagerId(301L);
        request.setDecision(DecisionType.APPROVED);

        when(managerDecisionRepository.findByLeaveRequestId(107L))
                .thenReturn(Optional.empty());

        Request feignRequest = Request.create(
                Request.HttpMethod.GET,
                "/api/leaves/pending/301",
                Map.of(),
                null,
                null,
                null
        );

        when(employeeServiceClient.getPendingLeaves(301L))
                .thenThrow(new FeignException.ServiceUnavailable(
                        "Employee Service unavailable",
                        feignRequest,
                        null,
                        Map.of()
                ));

        assertThrows(
                ServiceCommunicationException.class,
                () -> managerDecisionService.processDecision(request)
        );

        verify(managerDecisionRepository, never())
                .saveAndFlush(any(ManagerDecision.class));

        verifyNoInteractions(notificationService);
    }
}
