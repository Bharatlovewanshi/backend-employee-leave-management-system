
package com.leave.manager.service;

import java.util.List;

import com.leave.manager.dto.response.NotificationResponse;
import com.leave.manager.entity.ManagerDecision;

/**
 * Service interface for managing employee notifications.
 *
 * Supports email and in-app notifications.
 */
public interface NotificationService {

    /**
     * Sends an email notification to an employee.
     *
     * @param decision manager decision details
     * @param employeeId employee receiving the notification
     * @param recipientEmail employee email address
     * @param subject email subject
     * @param message email content
     */
    void sendEmail(ManagerDecision decision, Long employeeId,
            String recipientEmail, String subject, String message);

    /**
     * Creates an in-app notification for an employee.
     *
     * @param decision manager decision details
     * @param employeeId employee receiving the notification
     * @param message notification content
     */
    void sendInAppNotification(ManagerDecision decision, Long employeeId, String message);

    /**
     * Retrieves all sent in-app notifications for an employee.
     *
     * @param employeeId employee ID
     * @return list of employee notifications
     */
    List<NotificationResponse> getEmployeeNotifications(Long employeeId);

    /**
     * Retrieves the unread in-app notification count for an employee.
     *
     * @param employeeId employee ID
     * @return unread notification count
     */
    long getUnreadNotificationCount(Long employeeId);

    /**
     * Marks an employee's in-app notification as read.
     *
     * @param notificationId notification ID
     * @param employeeId employee ID
     */
    void markNotificationAsRead(Long notificationId, Long employeeId);
    
    List<NotificationResponse> getManagerNotifications(Long managerId);
}
