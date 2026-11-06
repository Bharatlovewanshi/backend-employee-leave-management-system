package com.leave.manager.dto.request;

import com.leave.manager.enums.DecisionType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Request DTO for submitting a manager's leave approval or rejection decision.
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
    private Long leaveRequestId;

    /**
     * Unique ID of the manager making the decision.
     */
    @NotNull(message = "Manager ID is required")
    private Long managerId;

    /**
     * Decision made by the manager (APPROVED or REJECTED).
     */
    @NotNull(message = "Decision type is required")
    private DecisionType decisionType;

    /**
     * Optional comments provided by the manager.
     */
    @Size(max = 500, message = "Comments cannot exceed 500 characters")
    private String comments;
}