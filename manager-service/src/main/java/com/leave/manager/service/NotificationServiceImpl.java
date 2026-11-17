
package com.leave.manager.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leave.manager.dto.response.NotificationResponse;
import com.leave.manager.entity.ManagerDecision;
import com.leave.manager.entity.NotificationLog;
import com.leave.manager.enums.NotificationStatus;
import com.leave.manager.enums.NotificationType;
import com.leave.manager.exception.InvalidDecisionException;
import com.leave.manager.repository.NotificationLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    private static final Set<String> ALLOWED_LEAVE_EVENTS =
            Set.of("LEAVE_APPLIED", "LEAVE_CANCELLED");

    private final JavaMailSender javaMailSender;
    private final NotificationLogRepository notificationLogRepository;

    @Override
    @Transactional
    public void sendEmail(
            ManagerDecision decision,
            Long employeeId,
            String recipientEmail,
            String subject,
            String message) {

        validateNotification(decision, employeeId, message);

        if (recipientEmail == null || recipientEmail.isBlank()
                || subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "Recipient email and subject are required"
            );
        }

        NotificationLog notificationLog = createDecisionNotification(
                decision,
                employeeId,
                NotificationType.EMAIL
        );

        notificationLog.setMessage(message);
        notificationLog.setNotificationStatus(NotificationStatus.PENDING);

        notificationLogRepository.saveAndFlush(notificationLog);

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(recipientEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);

            javaMailSender.send(mailMessage);

            notificationLog.setNotificationStatus(NotificationStatus.SENT);
            notificationLog.setSentAt(LocalDateTime.now());
            notificationLog.setErrorMessage(null);

            notificationLogRepository.save(notificationLog);

        } catch (MailException exception) {

            log.error(
                    "Email delivery failed for recipient: {}",
                    recipientEmail,
                    exception
            );

            notificationLog.setNotificationStatus(NotificationStatus.FAILED);

            String errorMessage = exception.getMessage();

            if (errorMessage != null
                    && errorMessage.length() > MAX_ERROR_MESSAGE_LENGTH) {
                errorMessage = errorMessage.substring(
                        0, MAX_ERROR_MESSAGE_LENGTH
                );
            }

            notificationLog.setErrorMessage(errorMessage);
            notificationLogRepository.save(notificationLog);
        }
    }

    @Override
    @Transactional
    public void sendInAppNotification(
            ManagerDecision decision,
            Long employeeId,
            String message) {

        validateNotification(decision, employeeId, message);

        NotificationLog notificationLog = createDecisionNotification(
                decision,
                employeeId,
                NotificationType.IN_APP
        );

        notificationLog.setMessage(message);
        notificationLog.setNotificationStatus(NotificationStatus.SENT);
        notificationLog.setSentAt(LocalDateTime.now());

        notificationLogRepository.save(notificationLog);
    }

    // Create employee-facing notification linked to a manager decision.
    private NotificationLog createDecisionNotification(
            ManagerDecision decision,
            Long employeeId,
            NotificationType notificationType) {

        NotificationLog notificationLog = new NotificationLog();

        notificationLog.setManagerDecision(decision);
        notificationLog.setLeaveRequestId(decision.getLeaveRequestId());
        notificationLog.setEmployeeId(employeeId);
        notificationLog.setManagerId(null);
        notificationLog.setNotificationType(notificationType);

        return notificationLog;
    }

    // Create manager-facing notification for apply/cancel events.
    @Override
    @Transactional
    public void createManagerLeaveEvent(
            Long managerId,
            Long employeeId,
            Long leaveRequestId,
            String eventType,
            String message) {

        validateManagerId(managerId);
        validateEmployeeId(employeeId);
        validateLeaveRequestId(leaveRequestId);

        if (eventType == null
                || !ALLOWED_LEAVE_EVENTS.contains(eventType)) {
            throw new IllegalArgumentException(
                    "Invalid leave event type"
            );
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Notification message is required"
            );
        }

        NotificationLog notificationLog = new NotificationLog();

        // No manager decision exists for an apply/cancel event.
        notificationLog.setManagerDecision(null);

        notificationLog.setManagerId(managerId);
        notificationLog.setEmployeeId(employeeId);
        notificationLog.setLeaveRequestId(leaveRequestId);

        notificationLog.setNotificationType(NotificationType.IN_APP);
        notificationLog.setMessage(message);

        notificationLog.setNotificationStatus(NotificationStatus.SENT);
        notificationLog.setSentAt(LocalDateTime.now());
        notificationLog.setIsRead(false);

        notificationLogRepository.save(notificationLog);

        log.info(
                "Manager notification created. Event: {}, Manager ID: {}, Employee ID: {}, Leave ID: {}",
                eventType,
                managerId,
                employeeId,
                leaveRequestId
        );
    }

    private void validateNotification(
            ManagerDecision decision,
            Long employeeId,
            String message) {

        if (decision == null
                || decision.getId() == null
                || decision.getLeaveRequestId() == null) {
            throw new IllegalArgumentException(
                    "Valid manager decision is required"
            );
        }

        validateEmployeeId(employeeId);

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Notification message is required"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getEmployeeNotifications(
            Long employeeId) {

        validateEmployeeId(employeeId);

        return notificationLogRepository
                .findByEmployeeIdAndManagerIdIsNullAndNotificationTypeOrderByCreatedAtDesc(
                        employeeId,
                        NotificationType.IN_APP
                )
                .stream()
                .filter(notification ->
                        notification.getNotificationStatus()
                                == NotificationStatus.SENT)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long employeeId) {

        validateEmployeeId(employeeId);

        return notificationLogRepository
                .countByEmployeeIdAndManagerIdIsNullAndNotificationTypeAndNotificationStatusAndIsReadFalse(
                        employeeId,
                        NotificationType.IN_APP,
                        NotificationStatus.SENT
                );
    }

    @Override
    @Transactional
    public void markNotificationAsRead(
            Long notificationId,
            Long employeeId) {

        validateNotificationId(notificationId);
        validateEmployeeId(employeeId);

        NotificationLog notification = notificationLogRepository
                .findByIdAndEmployeeIdAndManagerIdIsNullAndNotificationType(
                        notificationId,
                        employeeId,
                        NotificationType.IN_APP
                )
                .orElseThrow(() ->
                        new InvalidDecisionException(
                                "Employee notification not found."
                        )
                );

        if (notification.getNotificationStatus()
                != NotificationStatus.SENT) {
            throw new InvalidDecisionException(
                    "Only sent notifications can be marked as read."
            );
        }

        notification.setIsRead(true);
        notificationLogRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getManagerNotifications(
            Long managerId) {

        validateManagerId(managerId);

        log.info(
                "Fetching leave event notifications for manager ID: {}",
                managerId
        );

        List<NotificationLog> notifications =
                notificationLogRepository
                        .findByManagerIdAndNotificationTypeAndNotificationStatusOrderByCreatedAtDesc(
                                managerId,
                                NotificationType.IN_APP,
                                NotificationStatus.SENT
                        );

        log.info(
                "Found {} notification records for manager ID: {}",
                notifications.size(),
                managerId
        );

        return notifications.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void markManagerNotificationAsRead(
            Long notificationId,
            Long managerId) {

        validateNotificationId(notificationId);
        validateManagerId(managerId);

        NotificationLog notification = notificationLogRepository
                .findByIdAndManagerIdAndNotificationType(
                        notificationId,
                        managerId,
                        NotificationType.IN_APP
                )
                .orElseThrow(() ->
                        new InvalidDecisionException(
                                "Manager notification not found."
                        )
                );

        if (notification.getNotificationStatus()
                != NotificationStatus.SENT) {
            throw new InvalidDecisionException(
                    "Only sent notifications can be marked as read."
            );
        }

        if (Boolean.TRUE.equals(notification.getIsRead())) {
            log.info(
                    "Notification {} is already marked as read.",
                    notificationId
            );
            return;
        }

        notification.setIsRead(true);
        notificationLogRepository.save(notification);

        log.info(
                "Manager {} marked notification {} as read.",
                managerId,
                notificationId
        );
    }

    private NotificationResponse toResponse(
            NotificationLog notification) {

        return new NotificationResponse(
                notification.getId(),
                notification.getLeaveRequestId(),
                notification.getEmployeeId(),
                notification.getMessage(),
                notification.getNotificationType(),
                notification.getNotificationStatus(),
                notification.getIsRead(),
                notification.getCreatedAt(),
                notification.getSentAt()
        );
    }

    private void validateNotificationId(Long notificationId) {

        if (notificationId == null || notificationId <= 0) {
            throw new InvalidDecisionException(
                    "Invalid notification ID."
            );
        }
    }

    private void validateEmployeeId(Long employeeId) {

        if (employeeId == null || employeeId <= 0) {
            throw new InvalidDecisionException(
                    "Invalid employee ID."
            );
        }
    }

    private void validateManagerId(Long managerId) {

        if (managerId == null || managerId <= 0) {
            throw new InvalidDecisionException(
                    "Invalid manager ID."
            );
        }
    }

    private void validateLeaveRequestId(Long leaveRequestId) {

        if (leaveRequestId == null || leaveRequestId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid leave request ID"
            );
        }
    }
}
