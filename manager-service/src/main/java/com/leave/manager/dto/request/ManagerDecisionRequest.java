
package com.leave.manager.dto.request;

import com.leave.manager.enums.DecisionType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for submitting a manager's leave approval
 * or rejection decision.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDecisionRequest {

    /**
     * Unique ID of the employee leave request.
     */
    @NotNull(message = "Leave request ID is required")
    @Positive(message = "Leave request ID must be greater than zero")
    private Long leaveRequestId;

    /**
     * Decision made by the manager (APPROVED or REJECTED).
     */
    @NotNull(message = "Decision is required")
    private DecisionType decision;

    /**
     * Reason provided by the manager when rejecting a leave request.
     */
    @Size(max = 500, message = "Rejection reason cannot exceed 500 characters")
    private String rejectionReason;
}
