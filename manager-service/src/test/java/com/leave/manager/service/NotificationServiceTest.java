
package com.leave.manager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

        verify(notificationLogRepository)
                .saveAndFlush(any(NotificationLog.class));

        verify(javaMailSender)
                .send(any(SimpleMailMessage.class));

        verify(notificationLogRepository).save(
                argThat(log ->
                        log.getNotificationStatus() == NotificationStatus.SENT
                        && log.getNotificationType() == NotificationType.EMAIL
                        && log.getEmployeeId().equals(205L)
                        && log.getManagerId() == null
                )
        );
    }

    // Test 2: Email failure should be logged without throwing
    @Test
    void shouldMarkEmailNotificationAsFailed() {

        ManagerDecision decision = createDecision();

        doThrow(new MailAuthenticationException(
                "SMTP authentication failed"))
                .when(javaMailSender)
                .send(any(SimpleMailMessage.class));

        notificationService.sendEmail(
                decision,
                205L,
                "employee@example.com",
                "Leave Approved",
                "Your leave request has been approved."
        );

        verify(notificationLogRepository)
                .saveAndFlush(any(NotificationLog.class));

        verify(notificationLogRepository).save(
                argThat(log ->
                        log.getNotificationStatus() == NotificationStatus.FAILED
                        && log.getNotificationType() == NotificationType.EMAIL
                        && log.getEmployeeId().equals(205L)
                        && log.getErrorMessage() != null
                )
        );
    }

    // Test 3: Successful employee in-app notification
    @Test
    void shouldCreateEmployeeInAppNotificationSuccessfully() {

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
                        && log.getManagerId() == null
                        && log.getManagerDecision() == decision
                )
        );
    }

    // Test 4: Retrieve only employee notifications
    @Test
    void shouldRetrieveEmployeeNotifications() {

        NotificationLog notification = new NotificationLog();

        notification.setId(10L);
        notification.setLeaveRequestId(105L);
        notification.setEmployeeId(205L);
        notification.setManagerId(null);
        notification.setNotificationType(NotificationType.IN_APP);
        notification.setNotificationStatus(NotificationStatus.SENT);
        notification.setMessage(
                "Your leave request has been approved."
        );
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        when(notificationLogRepository
                .findByEmployeeIdAndManagerIdIsNullAndNotificationTypeOrderByCreatedAtDesc(
                        205L,
                        NotificationType.IN_APP
                ))
                .thenReturn(List.of(notification));

        List<NotificationResponse> response =
                notificationService.getEmployeeNotifications(205L);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(10L, response.get(0).getId());
        assertEquals(105L, response.get(0).getLeaveRequestId());
        assertEquals(205L, response.get(0).getEmployeeId());
        assertEquals(
                NotificationType.IN_APP,
                response.get(0).getNotificationType()
        );
        assertFalse(response.get(0).getIsRead());

        verify(notificationLogRepository)
                .findByEmployeeIdAndManagerIdIsNullAndNotificationTypeOrderByCreatedAtDesc(
                        205L,
                        NotificationType.IN_APP
                );
    }

    // Test 5: Retrieve unread employee notification count
    @Test
    void shouldGetUnreadNotificationCount() {

        when(notificationLogRepository
                .countByEmployeeIdAndManagerIdIsNullAndNotificationTypeAndNotificationStatusAndIsReadFalse(
                        205L,
                        NotificationType.IN_APP,
                        NotificationStatus.SENT
                ))
                .thenReturn(3L);

        long count =
                notificationService.getUnreadNotificationCount(205L);

        assertEquals(3L, count);

        verify(notificationLogRepository)
                .countByEmployeeIdAndManagerIdIsNullAndNotificationTypeAndNotificationStatusAndIsReadFalse(
                        205L,
                        NotificationType.IN_APP,
                        NotificationStatus.SENT
                );
    }

    // Test 6: Mark employee notification as read
    @Test
    void shouldMarkEmployeeNotificationAsRead() {

        NotificationLog notification = new NotificationLog();

        notification.setId(10L);
        notification.setEmployeeId(205L);
        notification.setManagerId(null);
        notification.setNotificationType(NotificationType.IN_APP);
        notification.setNotificationStatus(NotificationStatus.SENT);
        notification.setIsRead(false);

        when(notificationLogRepository
                .findByIdAndEmployeeIdAndManagerIdIsNullAndNotificationType(
                        10L,
                        205L,
                        NotificationType.IN_APP
                ))
                .thenReturn(Optional.of(notification));

        notificationService.markNotificationAsRead(10L, 205L);

        assertTrue(notification.getIsRead());

        verify(notificationLogRepository).save(notification);
    }

    // Test 7: Invalid employee notification ID
    @Test
    void shouldThrowExceptionWhenEmployeeNotificationDoesNotExist() {

        when(notificationLogRepository
                .findByIdAndEmployeeIdAndManagerIdIsNullAndNotificationType(
                        999L,
                        205L,
                        NotificationType.IN_APP
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidDecisionException.class,
                () -> notificationService.markNotificationAsRead(
                        999L,
                        205L
                )
        );

        verify(notificationLogRepository, never())
                .save(any(NotificationLog.class));
    }

    // Test 8: Create manager notification for new leave application
    @Test
    void shouldCreateManagerNotificationForLeaveApplication() {

        notificationService.createManagerLeaveEvent(
                301L,
                205L,
                105L,
                "LEAVE_APPLIED",
                "Employee has submitted a new leave request."
        );

        verify(notificationLogRepository).save(
                argThat(log ->
                        log.getManagerId().equals(301L)
                        && log.getEmployeeId().equals(205L)
                        && log.getLeaveRequestId().equals(105L)
                        && log.getManagerDecision() == null
                        && log.getNotificationType() == NotificationType.IN_APP
                        && log.getNotificationStatus() == NotificationStatus.SENT
                        && Boolean.FALSE.equals(log.getIsRead())
                )
        );
    }

    // Test 9: Create manager notification for leave cancellation
    @Test
    void shouldCreateManagerNotificationForLeaveCancellation() {

        notificationService.createManagerLeaveEvent(
                301L,
                205L,
                105L,
                "LEAVE_CANCELLED",
                "Employee has cancelled a leave request."
        );

        verify(notificationLogRepository).save(
                argThat(log ->
                        log.getManagerId().equals(301L)
                        && log.getEmployeeId().equals(205L)
                        && log.getLeaveRequestId().equals(105L)
                        && log.getManagerDecision() == null
                        && log.getNotificationType() == NotificationType.IN_APP
                        && log.getNotificationStatus() == NotificationStatus.SENT
                        && Boolean.FALSE.equals(log.getIsRead())
                )
        );
    }

    // Test 10: Retrieve manager notifications
    @Test
    void shouldRetrieveManagerNotifications() {

        NotificationLog notification = new NotificationLog();

        notification.setId(20L);
        notification.setManagerId(301L);
        notification.setEmployeeId(205L);
        notification.setLeaveRequestId(105L);
        notification.setManagerDecision(null);
        notification.setNotificationType(NotificationType.IN_APP);
        notification.setNotificationStatus(NotificationStatus.SENT);
        notification.setMessage(
                "Employee has submitted a new leave request."
        );
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        when(notificationLogRepository
                .findByManagerIdAndNotificationTypeAndNotificationStatusOrderByCreatedAtDesc(
                        301L,
                        NotificationType.IN_APP,
                        NotificationStatus.SENT
                ))
                .thenReturn(List.of(notification));

        List<NotificationResponse> response =
                notificationService.getManagerNotifications(301L);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(20L, response.get(0).getId());
        assertEquals(105L, response.get(0).getLeaveRequestId());
        assertEquals(205L, response.get(0).getEmployeeId());
        assertEquals(
                NotificationType.IN_APP,
                response.get(0).getNotificationType()
        );
    }

    // Test 11: Mark manager notification as read
    @Test
    void shouldMarkManagerNotificationAsRead() {

        NotificationLog notification = new NotificationLog();

        notification.setId(20L);
        notification.setManagerId(301L);
        notification.setEmployeeId(205L);
        notification.setNotificationType(NotificationType.IN_APP);
        notification.setNotificationStatus(NotificationStatus.SENT);
        notification.setIsRead(false);

        when(notificationLogRepository
                .findByIdAndManagerIdAndNotificationType(
                        20L,
                        301L,
                        NotificationType.IN_APP
                ))
                .thenReturn(Optional.of(notification));

        notificationService.markManagerNotificationAsRead(20L, 301L);

        assertTrue(notification.getIsRead());

        verify(notificationLogRepository).save(notification);
    }

    // Test 12: Manager cannot mark another manager's notification as read
    @Test
    void shouldNotAllowManagerToReadAnotherManagersNotification() {

        when(notificationLogRepository
                .findByIdAndManagerIdAndNotificationType(
                        20L,
                        999L,
                        NotificationType.IN_APP
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidDecisionException.class,
                () -> notificationService.markManagerNotificationAsRead(
                        20L,
                        999L
                )
        );

        verify(notificationLogRepository, never())
                .save(any(NotificationLog.class));
    }
}
