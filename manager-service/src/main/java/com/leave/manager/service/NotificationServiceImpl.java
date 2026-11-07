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

/**
 * Implementation of NotificationService.
 * Handles email and in-app notifications for employee leave decisions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender javaMailSender;
    private final NotificationLogRepository notificationLogRepository;

    /**
     * Sends an email notification to an employee and records its status.
     *
     * @param decision manager decision details
     * @param employeeId employee receiving the notification
     * @param recipientEmail employee email address
     * @param subject email subject
     * @param message email content
     */
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
                    "Recipient email and subject are required");
        }

        // Create notification log
        NotificationLog notificationLog = createNotificationLog(
                decision,
                employeeId,
                NotificationType.EMAIL
        );
        notificationLog.setMessage(message);

        // Save initial notification status
        notificationLog.setNotificationStatus(NotificationStatus.PENDING);
        notificationLogRepository.saveAndFlush(notificationLog);

        try {
            // Create email
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(recipientEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);

            // Send email
            javaMailSender.send(mailMessage);

            // Update notification status
            notificationLog.setNotificationStatus(NotificationStatus.SENT);
            notificationLog.setSentAt(LocalDateTime.now());
            notificationLog.setErrorMessage(null);

            notificationLogRepository.save(notificationLog);

        } catch (MailException exception) {

            // Log the actual SMTP exception for debugging
            log.error("Email delivery failed for recipient: {}", recipientEmail, exception);

            // Record email delivery failure
            notificationLog.setNotificationStatus(NotificationStatus.FAILED);
            notificationLog.setErrorMessage(exception.getMessage());

            notificationLogRepository.save(notificationLog);
            
        }
    }

    /**
     * Creates and stores an in-app notification for an employee.
     *
     * @param decision manager decision details
     * @param employeeId employee receiving the notification
     * @param message notification content
     */
    @Override
    @Transactional
    public void sendInAppNotification(
            ManagerDecision decision,
            Long employeeId,
            String message) {

        validateNotification(decision, employeeId, message);

        // Create in-app notification log
        NotificationLog notificationLog = createNotificationLog(
                decision,
                employeeId,
                NotificationType.IN_APP
        );
        notificationLog.setMessage(message);
        
        // Store notification
        notificationLog.setNotificationStatus(NotificationStatus.SENT);
        notificationLog.setSentAt(LocalDateTime.now());

        notificationLogRepository.save(notificationLog);
    }

    /**
     * Creates a notification log with common details.
     *
     * @param decision manager decision
     * @param employeeId employee receiving notification
     * @param notificationType notification channel
     * @return notification log entity
     */
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

    /**
     * Validates common notification details.
     */
    private void validateNotification(
            ManagerDecision decision,
            Long employeeId,
            String message) {

        if (decision == null || decision.getId() == null
                || decision.getLeaveRequestId() == null) {
            throw new IllegalArgumentException(
                    "Valid manager decision is required");
        }

        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException(
                    "Valid employee ID is required");
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Notification message is required");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getEmployeeNotifications(Long employeeId) {

        if (employeeId == null || employeeId <= 0) {
            throw new InvalidDecisionException("Invalid employee ID.");
        }

        return notificationLogRepository
                .findByEmployeeIdAndNotificationTypeOrderByCreatedAtDesc(
                        employeeId, NotificationType.IN_APP)
                .stream()
                .filter(notification ->
                        notification.getNotificationStatus() == NotificationStatus.SENT)
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

        if (employeeId == null || employeeId <= 0) {
            throw new InvalidDecisionException("Invalid employee ID.");
        }

        return notificationLogRepository
                .countByEmployeeIdAndNotificationTypeAndNotificationStatusAndIsReadFalse(
                        employeeId,
                        NotificationType.IN_APP,
                        NotificationStatus.SENT
                );
    }

    @Override
    @Transactional
    public void markNotificationAsRead(Long notificationId, Long employeeId) {

        if (notificationId == null || notificationId <= 0
                || employeeId == null || employeeId <= 0) {
            throw new InvalidDecisionException("Invalid notification or employee ID.");
        }

        var notification = notificationLogRepository
                .findByIdAndEmployeeIdAndNotificationType(
                        notificationId,
                        employeeId,
                        NotificationType.IN_APP
                )
                .orElseThrow(() ->
                        new InvalidDecisionException("Notification not found."));

        if (notification.getNotificationStatus() != NotificationStatus.SENT) {
            throw new InvalidDecisionException(
                    "Only sent notifications can be marked as read.");
        }

        notification.setIsRead(true);
        notificationLogRepository.save(notification);
    }
}