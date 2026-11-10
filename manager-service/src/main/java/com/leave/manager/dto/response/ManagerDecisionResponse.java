package com.leave.manager.dto.response;

import java.time.LocalDateTime;

import com.leave.manager.enums.DecisionType;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Response DTO containing manager leave decision details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDecisionResponse {

    /**
     * Unique ID of the manager decision.
     */
    private Long id;

    /**
     * Unique ID of the employee leave request.
     */
    private Long leaveRequestId;

    /**
     * Unique ID of the manager who made the decision.
     */
    private Long managerId;

    /**
     * Decision made by the manager (APPROVED or REJECTED).
     */
    private DecisionType decision;

    /**
     * Reason provided by the manager when rejecting a leave request.
     */
    private String rejectionReason;

    /**
     * Date and time when the decision was made.
     */
    private LocalDateTime decisionAt;
    
    private Long employeeId;

    private String employeeName;
}