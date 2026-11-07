package com.leave.manager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.leave.manager.dto.response.NotificationResponse;
import com.leave.manager.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/manager/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get all in-app notifications for an employee.
     */
    @GetMapping("/{employeeId}")
    public ResponseEntity<List<NotificationResponse>> getEmployeeNotifications(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                notificationService.getEmployeeNotifications(employeeId)
        );
    }

    /**
     * Get unread notification count for an employee.
     */
    @GetMapping("/{employeeId}/unread-count")
    public ResponseEntity<Long> getUnreadNotificationCount(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                notificationService.getUnreadNotificationCount(employeeId)
        );
    }

    /**
     * Mark an employee's notification as read.
     */
    @PatchMapping("/{employeeId}/{notificationId}/read")
    public ResponseEntity<String> markNotificationAsRead(
            @PathVariable Long employeeId,
            @PathVariable Long notificationId) {

        notificationService.markNotificationAsRead(notificationId, employeeId);

        return ResponseEntity.ok("Notification marked as read successfully.");
    }
}
