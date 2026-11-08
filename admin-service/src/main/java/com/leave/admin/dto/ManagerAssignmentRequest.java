
package com.leave.admin.dto;

import jakarta.validation.constraints.NotNull;

public class ManagerAssignmentRequest {

    @NotNull(message = "Manager ID is required")
    private Long managerId;

    public ManagerAssignmentRequest() {
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }
}
