
package com.leave.manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.leave.manager.entity.NotificationLog;
import com.leave.manager.enums.NotificationStatus;
import com.leave.manager.enums.NotificationType;

/**
 * Repository interface for managing NotificationLog entities.
 * Provides database operations and custom query methods
 * for tracking employee notifications.
 */
@Repository
public interface NotificationLogRepository
        extends JpaRepository<NotificationLog, Long> {

    /**
     * Retrieves all notification logs for a specific leave request.
     */
    List<NotificationLog> findByLeaveRequestId(Long leaveRequestId);

    /**
     * Retrieves all notification logs for a specific employee.
     */
    List<NotificationLog> findByEmployeeId(Long employeeId);

    /**
     * Retrieves notification logs based on delivery status.
     */
    List<NotificationLog> findByNotificationStatus(
            NotificationStatus notificationStatus);

    /**
     * Retrieves notification logs based on notification type.
     */
    List<NotificationLog> findByNotificationType(
            NotificationType notificationType);

    /**
     * Retrieves notification logs for an employee and delivery status.
     */
    List<NotificationLog> findByEmployeeIdAndNotificationStatus(
            Long employeeId,
            NotificationStatus notificationStatus);

    /**
     * Retrieves all in-app notifications for an employee,
     * newest notifications first.
     */
    List<NotificationLog> findByEmployeeIdAndNotificationTypeOrderByCreatedAtDesc(
            Long employeeId,
            NotificationType notificationType);

    /**
     * Counts unread, successfully delivered in-app notifications.
     */
    long countByEmployeeIdAndNotificationTypeAndNotificationStatusAndIsReadFalse(
            Long employeeId,
            NotificationType notificationType,
            NotificationStatus notificationStatus);

    /**
     * Retrieves a specific in-app notification belonging to an employee.
     */
    Optional<NotificationLog> findByIdAndEmployeeIdAndNotificationType(
            Long id,
            Long employeeId,
            NotificationType notificationType);
}
