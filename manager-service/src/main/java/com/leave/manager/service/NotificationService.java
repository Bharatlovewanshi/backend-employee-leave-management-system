
package com.leave.manager.service;

import java.util.List;

import com.leave.manager.dto.response.NotificationResponse;
import com.leave.manager.entity.ManagerDecision;

public interface NotificationService {

    void sendEmail(
            ManagerDecision decision,
            Long employeeId,
            String recipientEmail,
            String subject,
            String message
    );

    void sendInAppNotification(
            ManagerDecision decision,
            Long employeeId,
            String message
    );

    // Employee notification operations
    List<NotificationResponse> getEmployeeNotifications(Long employeeId);

    long getUnreadNotificationCount(Long employeeId);

    void markNotificationAsRead(
            Long notificationId,
            Long employeeId
    );

    // Manager notification operations
    List<NotificationResponse> getManagerNotifications(Long managerId);

    void markManagerNotificationAsRead(
            Long notificationId,
            Long managerId
    );

    // Create a manager notification for employee leave events
    void createManagerLeaveEvent(
            Long managerId,
            Long employeeId,
            Long leaveRequestId,
            String eventType,
            String message
    );
}
