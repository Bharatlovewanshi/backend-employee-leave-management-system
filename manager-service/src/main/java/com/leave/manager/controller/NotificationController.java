
package com.leave.manager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.leave.manager.dto.response.NotificationResponse;
import com.leave.manager.security.JwtService;
import com.leave.manager.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtService jwtService;

    /**
     * Get notifications for the authenticated employee.
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getEmployeeNotifications(
            Authentication authentication) {

        Long employeeId =
                jwtService.extractUserIdFromAuthentication(authentication);

        return ResponseEntity.ok(
                notificationService.getEmployeeNotifications(employeeId)
        );
    }

    /**
     * Get unread notification count.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadNotificationCount(
            Authentication authentication) {

        Long employeeId =
                jwtService.extractUserIdFromAuthentication(authentication);

        return ResponseEntity.ok(
                notificationService.getUnreadNotificationCount(employeeId)
        );
    }

    /**
     * Mark notification as read.
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<String> markNotificationAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {

        Long employeeId =
                jwtService.extractUserIdFromAuthentication(authentication);

        notificationService.markNotificationAsRead(
                notificationId, employeeId
        );

        return ResponseEntity.ok(
                "Notification marked as read successfully."
        );
    }
    
    /**
     * Get notification delivery records for the authenticated manager.
     */
    @GetMapping("/manager")
    public ResponseEntity<List<NotificationResponse>> getManagerNotifications(
            Authentication authentication) {

        Long managerId =
                jwtService.extractManagerIdFromAuthentication(authentication);

        return ResponseEntity.ok(
                notificationService.getManagerNotifications(managerId)
        );
    }
}
