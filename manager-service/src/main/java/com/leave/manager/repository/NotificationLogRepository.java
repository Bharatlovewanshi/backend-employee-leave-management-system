package com.leave.manager.repository;

import java.util.List;

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
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    /**
     * Retrieves all notification logs for a specific leave request.
     *
     * @param leaveRequestId unique ID of the leave request
     * @return list of notification logs
     */
    List<NotificationLog> findByLeaveRequestId(Long leaveRequestId);

    /**
     * Retrieves all notification logs for a specific recipient.
     *
     * @param recipientId unique ID of the notification recipient
     * @return list of notification logs
     */
    List<NotificationLog> findByRecipientId(Long recipientId);

    /**
     * Retrieves notification logs based on their delivery status.
     *
     * @param notificationStatus status of the notification
     * @return list of matching notification logs
     */
    List<NotificationLog> findByNotificationStatus(NotificationStatus notificationStatus);

    /**
     * Retrieves notification logs based on their notification type.
     *
     * @param notificationType type of notification (EMAIL, SMS, or IN_APP)
     * @return list of matching notification logs
     */
    List<NotificationLog> findByNotificationType(NotificationType notificationType);

    /**
     * Retrieves notification logs for a specific recipient and status.
     *
     * @param recipientId unique ID of the recipient
     * @param notificationStatus status of the notification
     * @return list of matching notification logs
     */
    List<NotificationLog> findByRecipientIdAndNotificationStatus(
            Long recipientId, NotificationStatus notificationStatus);
}