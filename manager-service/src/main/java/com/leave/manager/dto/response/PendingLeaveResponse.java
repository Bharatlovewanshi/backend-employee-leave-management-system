
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

    private Long leaveRequestId;

    private Long employeeId;

    private String employeeName;

    private String employeeEmail;

    private String leaveType;

    private LocalDate startDate;

    private LocalDate endDate;

    private String reason;

    private String status;
}
