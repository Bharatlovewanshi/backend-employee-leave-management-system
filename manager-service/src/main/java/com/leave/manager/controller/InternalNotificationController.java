
package com.leave.manager.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.leave.manager.dto.request.ManagerNotificationRequest;
import com.leave.manager.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/internal/notifications")
@RequiredArgsConstructor
@Slf4j
public class InternalNotificationController {

    private final NotificationService notificationService;

    @Value("${internal.service.secret}")
    private String internalServiceSecret;

    /**
     * Receives leave application/cancellation events
     * from Employee Service.
     */
    @PostMapping("/leave-events")
    public ResponseEntity<String> receiveLeaveEvent(
            @RequestHeader(value = "X-INTERNAL-SECRET", required = false)
            String suppliedSecret,
            @RequestBody ManagerNotificationRequest request) {

        // Validate service-to-service authentication.
        if (!isValidSecret(suppliedSecret)) {
            log.warn("Rejected internal notification request: invalid secret");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid internal service authentication.");
        }

        if (request == null
                || request.getManagerId() == null
                || request.getEmployeeId() == null
                || request.getLeaveRequestId() == null
                || request.getEventType() == null
                || request.getMessage() == null
                || request.getMessage().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Invalid leave notification request.");
        }

        notificationService.createManagerLeaveEvent(
                request.getManagerId(),
                request.getEmployeeId(),
                request.getLeaveRequestId(),
                request.getEventType(),
                request.getMessage()
        );

        log.info(
                "Manager leave notification created. Manager ID: {}, Employee ID: {}, Leave ID: {}, Event: {}",
                request.getManagerId(),
                request.getEmployeeId(),
                request.getLeaveRequestId(),
                request.getEventType()
        );

        return ResponseEntity.ok(
                "Manager notification created successfully."
        );
    }

    private boolean isValidSecret(String suppliedSecret) {

        if (suppliedSecret == null
                || suppliedSecret.isBlank()
                || internalServiceSecret == null
                || internalServiceSecret.isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
                internalServiceSecret.getBytes(StandardCharsets.UTF_8),
                suppliedSecret.getBytes(StandardCharsets.UTF_8)
        );
    }
}
