package com.leave.manager.dto.response;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Response DTO containing pending employee leave request details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PendingLeaveResponse {

    /**
     * Unique ID of the employee leave request.
     */
    private Long leaveRequestId;

    /**
     * Unique ID of the employee who submitted the leave request.
     */
    private Long employeeId;

    /**
     * Name of the employee.
     */
    private String employeeName;

    /**
     * Type of leave requested by the employee.
     */
    private String leaveType;

    /**
     * Start date of the requested leave.
     */
    private LocalDate startDate;

    /**
     * End date of the requested leave.
     */
    private LocalDate endDate;

    /**
     * Reason provided by the employee for requesting leave.
     */
    private String reason;

    /**
     * Current status of the leave request.
     */
    private String status;
}