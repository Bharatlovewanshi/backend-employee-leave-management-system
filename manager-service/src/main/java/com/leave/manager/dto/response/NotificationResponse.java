package com.leave.manager.dto.response;

import java.time.LocalDateTime;

import com.leave.manager.enums.NotificationStatus;
import com.leave.manager.enums.NotificationType;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Response DTO containing employee notification details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;

    private Long leaveRequestId;

    private Long employeeId;

    private String message;

    private NotificationType notificationType;

    private NotificationStatus notificationStatus;

    private Boolean isRead;

    private LocalDateTime createdAt;

    private LocalDateTime sentAt;
}
