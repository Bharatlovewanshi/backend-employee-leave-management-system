
package com.leave.manager.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerNotificationRequest {

    private Long managerId;
    private Long employeeId;
    private Long leaveRequestId;
    private String eventType;
    private String message;
}
