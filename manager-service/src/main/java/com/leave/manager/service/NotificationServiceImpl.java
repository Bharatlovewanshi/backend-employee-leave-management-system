package com.leave.manager.service;

import java.time.LocalDateTime;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.leave.manager.entity.NotificationLog;
import com.leave.manager.enums.NotificationStatus;
import com.leave.manager.enums.NotificationType;
import com.leave.manager.exception.ServiceCommunicationException;
import com.leave.manager.repository.NotificationLogRepository;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of NotificationService.
 * Handles email and in-app notifications for employee leave decisions.
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender javaMailSender;
    private final NotificationLogRepository notificationLogRepository;

    /**
     * Sends an email notification to an employee.
     *
     * @param recipientEmail email address of the employee
     * @param subject subject of the email
     * @param message email message content
     * @throws IllegalArgumentException if email details are invalid
     * @throws ServiceCommunicationException if email delivery fails
     */
    @Override
    public void sendEmail(String recipientEmail, String subject, String message) {

        // Validate email details
        if (recipientEmail == null || recipientEmail.isBlank()
                || subject == null || subject.isBlank()
                || message == null || message.isBlank()) {

            throw new IllegalArgumentException("Email details cannot be empty");
        }

        try {
            // Create email message
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            mailMessage.setTo(recipientEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);

            // Send email
            javaMailSender.send(mailMessage);

        } catch (MailException exception) {

            throw new ServiceCommunicationException(
                    "Failed to send email notification",
                    exception
            );
        }
    }

    /**
     * Creates and stores an in-app notification for an employee.
     *
     * @param recipientId unique ID of the employee receiving the notification
     * @param message notification message content
     * @throws IllegalArgumentException if recipient ID or message is invalid
     */
    @Override
    public void sendInAppNotification(Long recipientId, String message) {

        // Validate notification details
        if (recipientId == null || recipientId <= 0
                || message == null || message.isBlank()) {

            throw new IllegalArgumentException(
                    "Recipient ID and notification message are required");
        }

        // Create notification log
        NotificationLog notificationLog = new NotificationLog();

        notificationLog.setRecipientId(recipientId);
        notificationLog.setMessage(message);
        notificationLog.setNotificationType(NotificationType.IN_APP);
        notificationLog.setNotificationStatus(NotificationStatus.SENT);
        notificationLog.setSentAt(LocalDateTime.now());

        // Save notification in database
        notificationLogRepository.save(notificationLog);
    }
}
