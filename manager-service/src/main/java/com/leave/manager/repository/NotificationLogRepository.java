
package com.leave.manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.leave.manager.entity.NotificationLog;
import com.leave.manager.enums.NotificationStatus;
import com.leave.manager.enums.NotificationType;

@Repository
public interface NotificationLogRepository
        extends JpaRepository<NotificationLog, Long> {

    // Find all notification logs for a leave request.
    List<NotificationLog> findByLeaveRequestId(Long leaveRequestId);

    // Find all notification logs for an employee.
    List<NotificationLog> findByEmployeeId(Long employeeId);

    // Find notifications by delivery status.
    List<NotificationLog> findByNotificationStatus(
            NotificationStatus notificationStatus
    );

    // Find notifications by type.
    List<NotificationLog> findByNotificationType(
            NotificationType notificationType
    );

    // Find employee notifications by delivery status.
    List<NotificationLog> findByEmployeeIdAndNotificationStatus(
            Long employeeId,
            NotificationStatus notificationStatus
    );

    // Employee-facing in-app notifications only.
    // managerId must be NULL.
    List<NotificationLog>
    findByEmployeeIdAndManagerIdIsNullAndNotificationTypeOrderByCreatedAtDesc(
            Long employeeId,
            NotificationType notificationType
    );

    // Count unread employee-facing notifications only.
    long countByEmployeeIdAndManagerIdIsNullAndNotificationTypeAndNotificationStatusAndIsReadFalse(
            Long employeeId,
            NotificationType notificationType,
            NotificationStatus notificationStatus
    );

    // Find a specific employee-facing notification.
    Optional<NotificationLog>
    findByIdAndEmployeeIdAndManagerIdIsNullAndNotificationType(
            Long id,
            Long employeeId,
            NotificationType notificationType
    );

    // Manager-facing in-app notifications only.
    List<NotificationLog>
    findByManagerIdAndNotificationTypeAndNotificationStatusOrderByCreatedAtDesc(
            Long managerId,
            NotificationType notificationType,
            NotificationStatus notificationStatus
    );

    // Find a specific notification belonging to a manager.
    Optional<NotificationLog>
    findByIdAndManagerIdAndNotificationType(
            Long id,
            Long managerId,
            NotificationType notificationType
    );
}
