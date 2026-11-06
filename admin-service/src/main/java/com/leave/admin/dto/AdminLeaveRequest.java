
package com.leave.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AdminLeaveRequest {

    @NotNull(message = "Admin user ID is required")
    @Positive(message = "Admin user ID must be positive")
    private Long adminUserId;

    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    public AdminLeaveRequest() {
    }

    public AdminLeaveRequest(Long adminUserId, String reason) {
        this.adminUserId = adminUserId;
        this.reason = reason;
    }

    public Long getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(Long adminUserId) {
        this.adminUserId = adminUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
