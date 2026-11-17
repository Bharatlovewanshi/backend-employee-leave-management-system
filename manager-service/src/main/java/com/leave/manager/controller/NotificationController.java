
package com.leave.manager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.leave.manager.dto.response.NotificationResponse;
import com.leave.manager.exception.InvalidDecisionException;
import com.leave.manager.security.JwtService;
import com.leave.manager.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtService jwtService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Get notifications for the authenticated employee.
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getEmployeeNotifications(
            Authentication authentication) {

    	Long userId =
    		    jwtService.extractUserIdFromAuthentication(authentication);

    		Long employeeId = jdbcTemplate.queryForObject(
    		    "SELECT employee_id FROM admin_employees WHERE user_id = ?",
    		    Long.class,
    		    userId
    		);
    		
        return ResponseEntity.ok(
                notificationService.getEmployeeNotifications(employeeId)
        );
    }

    /**
     * Get unread notification count for employee.
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
     * Mark employee notification as read.
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {

        Long userId = jwtService.extractUserIdFromAuthentication(authentication);

        Long employeeId = jdbcTemplate.queryForObject(
                "SELECT employee_id FROM admin_employees WHERE user_id = ?",
                Long.class,
                userId
        );

        notificationService.markNotificationAsRead(notificationId, employeeId);

        return ResponseEntity.ok("Notification marked as read");
    }

    /**
     * Get notification delivery records for authenticated manager.
     */
    @GetMapping("/manager")
    public ResponseEntity<List<NotificationResponse>> getManagerNotifications(
            Authentication authentication) {

        Long managerId = getAuthenticatedManagerId(authentication);

        log.info(
                "Fetching notifications for manager ID: {}",
                managerId
        );

        return ResponseEntity.ok(
                notificationService.getManagerNotifications(managerId)
        );
    }

    /**
     * Mark manager notification as read.
     */
    @PatchMapping("/manager/{notificationId}/read")
    public ResponseEntity<String> markManagerNotificationAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {

        Long managerId = getAuthenticatedManagerId(authentication);

        notificationService.markManagerNotificationAsRead(
                notificationId,
                managerId
        );

        return ResponseEntity.ok(
                "Manager notification marked as read successfully."
        );
    }

    /**
     * Resolve the authenticated manager's profile ID.
     */
    private Long getAuthenticatedManagerId(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getAuthorities().stream()
                        .noneMatch(authority ->
                                "ROLE_MANAGER".equals(authority.getAuthority()))) {

            throw new InvalidDecisionException(
                    "Manager authentication is required"
            );
        }

        String email = authentication.getName();

        log.info("Resolving manager profile for email: {}", email);

        Long managerId;

        try {
            managerId = jdbcTemplate.queryForObject(
                    "SELECT manager_id FROM admin_managers WHERE email = ?",
                    Long.class,
                    email
            );
        } catch (org.springframework.dao.EmptyResultDataAccessException exception) {
            throw new InvalidDecisionException(
                    "Manager profile not found"
            );
        }

        if (managerId == null || managerId <= 0) {
            throw new InvalidDecisionException(
                    "Manager profile not found"
            );
        }

        return managerId;
    }
}
