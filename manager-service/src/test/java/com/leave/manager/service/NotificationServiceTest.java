package com.leave.manager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.leave.manager.dto.response.NotificationResponse;
import com.leave.manager.entity.ManagerDecision;
import com.leave.manager.entity.NotificationLog;
import com.leave.manager.enums.DecisionType;
import com.leave.manager.enums.NotificationStatus;
import com.leave.manager.enums.NotificationType;
import com.leave.manager.exception.InvalidDecisionException;
import com.leave.manager.repository.NotificationLogRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    // Common test decision
    private ManagerDecision createDecision() {
        ManagerDecision decision = new ManagerDecision();
        decision.setId(1L);
        decision.setLeaveRequestId(105L);
        decision.setManagerId(301L);
        decision.setDecision(DecisionType.APPROVED);
        return decision;
    }

    // Test 1: Successful email notification
    @Test
    void shouldSendEmailSuccessfully() {

        ManagerDecision decision = createDecision();

        notificationService.sendEmail(
                decision,
                205L,
                "employee@example.com",
                "Leave Approved",
                "Your leave request has been approved."
        );

        verify(notificationLogRepository).saveAndFlush(
                any(NotificationLog.class));

        verify(javaMailSender).send(
                any(SimpleMailMessage.class));

        verify(notificationLogRepository).save(
                argThat(log ->
                        log.getNotificationStatus() == NotificationStatus.SENT
                        && log.getNotificationType() == NotificationType.EMAIL
                        && log.getEmployeeId().equals(205L)
                )
        );
    }

    // Test 2: Email failure should be logged without throwing
    @Test
    void shouldMarkEmailNotificationAsFailed() {

        ManagerDecision decision = createDecision();

        doThrow(new MailAuthenticationException("SMTP authentication failed"))
                .when(javaMailSender)
                .send(any(SimpleMailMessage.class));

        notificationService.sendEmail(
                decision,
                205L,
                "employee@example.com",
                "Leave Approved",
                "Your leave request has been approved."
        );

        verify(notificationLogRepository).saveAndFlush(
                any(NotificationLog.class));

        verify(notificationLogRepository).save(
                argThat(log ->
                        log.getNotificationStatus() == NotificationStatus.FAILED
                        && log.getNotificationType() == NotificationType.EMAIL
                        && log.getErrorMessage() != null
                )
        );
    }

    // Test 3: Successful in-app notification
    @Test
    void shouldCreateInAppNotificationSuccessfully() {

        ManagerDecision decision = createDecision();

        notificationService.sendInAppNotification(
                decision,
                205L,
                "Your leave request has been approved."
        );

        verify(notificationLogRepository).save(
                argThat(log ->
                        log.getEmployeeId().equals(205L)
                        && log.getLeaveRequestId().equals(105L)
                        && log.getNotificationType() == NotificationType.IN_APP
                        && log.getNotificationStatus() == NotificationStatus.SENT
                        && Boolean.FALSE.equals(log.getIsRead())
                )
        );
    }

    // Test 4: Retrieve employee notifications
    @Test
    void shouldRetrieveEmployeeNotifications() {

        NotificationLog notification = new NotificationLog();
        notification.setId(10L);
        notification.setLeaveRequestId(105L);
        notification.setEmployeeId(205L);
        notification.setNotificationType(NotificationType.IN_APP);
        notification.setNotificationStatus(NotificationStatus.SENT);
        notification.setMessage("Your leave request has been approved.");
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        when(notificationLogRepository
                .findByEmployeeIdAndNotificationTypeOrderByCreatedAtDesc(
                        205L, NotificationType.IN_APP))
                .thenReturn(List.of(notification));

        List<NotificationResponse> response =
                notificationService.getEmployeeNotifications(205L);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(10L, response.get(0).getId());
        assertEquals(105L, response.get(0).getLeaveRequestId());
        assertEquals(205L, response.get(0).getEmployeeId());
        assertEquals(NotificationType.IN_APP,
                response.get(0).getNotificationType());
        assertFalse(response.get(0).getIsRead());
    }

    // Test 5: Retrieve unread notification count
    @Test
    void shouldGetUnreadNotificationCount() {

        when(notificationLogRepository
                .countByEmployeeIdAndNotificationTypeAndNotificationStatusAndIsReadFalse(
                        205L,
                        NotificationType.IN_APP,
                        NotificationStatus.SENT))
                .thenReturn(3L);

        long count =
                notificationService.getUnreadNotificationCount(205L);

        assertEquals(3L, count);

        verify(notificationLogRepository).countByEmployeeIdAndNotificationTypeAndNotificationStatusAndIsReadFalse(
                205L,
                NotificationType.IN_APP,
                NotificationStatus.SENT
        );
    }

    // Test 6: Mark notification as read
    @Test
    void shouldMarkNotificationAsRead() {

        NotificationLog notification = new NotificationLog();
        notification.setId(10L);
        notification.setEmployeeId(205L);
        notification.setNotificationType(NotificationType.IN_APP);
        notification.setNotificationStatus(NotificationStatus.SENT);
        notification.setIsRead(false);

        when(notificationLogRepository
                .findByIdAndEmployeeIdAndNotificationType(
                        10L, 205L, NotificationType.IN_APP))
                .thenReturn(Optional.of(notification));

        notificationService.markNotificationAsRead(10L, 205L);

        assertTrue(notification.getIsRead());

        verify(notificationLogRepository).save(notification);
    }

    // Test 7: Invalid notification ID
    @Test
    void shouldThrowExceptionWhenNotificationDoesNotExist() {

        when(notificationLogRepository
                .findByIdAndEmployeeIdAndNotificationType(
                        999L, 205L, NotificationType.IN_APP))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidDecisionException.class,
                () -> notificationService.markNotificationAsRead(
                        999L, 205L)
        );

        verify(notificationLogRepository, never())
                .save(any(NotificationLog.class));
    }
}
