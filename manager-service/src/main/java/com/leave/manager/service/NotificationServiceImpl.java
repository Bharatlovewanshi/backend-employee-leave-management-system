
package com.leave.manager.service;

import java.time.LocalDateTime;
import java.util.List;

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

        NotificationLog notificationLog = createNotificationLog(
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

        NotificationLog notificationLog = createNotificationLog(
                decision,
                employeeId,
                NotificationType.IN_APP
        );

        notificationLog.setMessage(message);
        notificationLog.setNotificationStatus(NotificationStatus.SENT);
        notificationLog.setSentAt(LocalDateTime.now());

        notificationLogRepository.save(notificationLog);
    }

    private NotificationLog createNotificationLog(
            ManagerDecision decision,
            Long employeeId,
            NotificationType notificationType) {

        NotificationLog notificationLog = new NotificationLog();

        notificationLog.setManagerDecision(decision);
        notificationLog.setLeaveRequestId(decision.getLeaveRequestId());
        notificationLog.setEmployeeId(employeeId);
        notificationLog.setNotificationType(notificationType);

        return notificationLog;
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

        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException(
                    "Valid employee ID is required"
            );
        }

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
                .findByEmployeeIdAndNotificationTypeOrderByCreatedAtDesc(
                        employeeId,
                        NotificationType.IN_APP
                )
                .stream()
                .filter(notification ->
                        notification.getNotificationStatus()
                                == NotificationStatus.SENT)
                .map(notification -> new NotificationResponse(
                        notification.getId(),
                        notification.getLeaveRequestId(),
                        notification.getEmployeeId(),
                        notification.getMessage(),
                        notification.getNotificationType(),
                        notification.getNotificationStatus(),
                        notification.getIsRead(),
                        notification.getCreatedAt(),
                        notification.getSentAt()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long employeeId) {

        validateEmployeeId(employeeId);

        return notificationLogRepository
                .countByEmployeeIdAndNotificationTypeAndNotificationStatusAndIsReadFalse(
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

        if (notificationId == null || notificationId <= 0) {
            throw new InvalidDecisionException("Invalid notification ID.");
        }

        validateEmployeeId(employeeId);

        NotificationLog notification = notificationLogRepository
                .findByIdAndEmployeeIdAndNotificationType(
                        notificationId,
                        employeeId,
                        NotificationType.IN_APP
                )
                .orElseThrow(() ->
                        new InvalidDecisionException("Notification not found.")
                );

        if (notification.getNotificationStatus() != NotificationStatus.SENT) {
            throw new InvalidDecisionException(
                    "Only sent notifications can be marked as read."
            );
        }

        notification.setIsRead(true);
        notificationLogRepository.save(notification);
    }

    private void validateEmployeeId(Long employeeId) {

        if (employeeId == null || employeeId <= 0) {
            throw new InvalidDecisionException("Invalid employee ID.");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getManagerNotifications(Long managerId) {

        if (managerId == null || managerId <= 0) {
            throw new InvalidDecisionException("Invalid manager ID.");
        }

        log.info("Fetching notification logs for manager ID: {}", managerId);

        List<NotificationLog> notifications =
                notificationLogRepository
                        .findByManagerDecision_ManagerIdOrderByCreatedAtDesc(
                                managerId
                        );

        log.info("Found {} notification records for manager ID: {}",
                notifications.size(), managerId);

        return notifications.stream()
                .map(notification -> new NotificationResponse(
                        notification.getId(),
                        notification.getLeaveRequestId(),
                        notification.getEmployeeId(),
                        notification.getMessage(),
                        notification.getNotificationType(),
                        notification.getNotificationStatus(),
                        notification.getIsRead(),
                        notification.getCreatedAt(),
                        notification.getSentAt()
                ))
                .toList();
    }
}
