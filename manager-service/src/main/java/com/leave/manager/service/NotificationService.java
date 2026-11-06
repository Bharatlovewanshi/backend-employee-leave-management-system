package com.leave.manager.service;

/**
 * Service interface for managing employee notifications.
 * Defines operations for sending email, SMS, and in-app notifications.
 */
public interface NotificationService {

    /**
     * Sends an email notification to an employee.
     *
     * @param recipientEmail email address of the employee
     * @param subject subject of the email
     * @param message email message content
     */
    void sendEmail(String recipientEmail, String subject, String message);

    /**
     * Creates an in-app notification for an employee.
     *
     * @param recipientId unique ID of the employee
     * @param message notification message content
     */
    void sendInAppNotification(Long recipientId, String message);
}