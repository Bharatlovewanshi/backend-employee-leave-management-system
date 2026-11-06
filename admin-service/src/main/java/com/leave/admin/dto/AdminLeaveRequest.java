
package com.leave.admin.dto;

public class AdminLeaveRequest {

    private Long adminUserId;
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
