
package com.leave.manager.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.leave.manager.dto.response.PendingLeaveResponse;
import com.leave.manager.dto.response.ManagerEmployeeResponse;
import com.leave.manager.dto.response.ManagerLeaveHistoryResponse;

/**
 * Feign client for communicating with Employee Service.
 */
@FeignClient(
    name = "employee-service",
    url = "${employee.service.url}"
)
public interface EmployeeServiceClient {

    /**
     * Retrieves pending leave requests assigned to
     * the authenticated manager.
     */
    @GetMapping("/api/manager/leaves/pending")
    List<PendingLeaveResponse> getPendingLeaves(
            @RequestHeader("Authorization") String authorization
    );

    /**
     * Approves or rejects a leave application
     * in Employee Service.
     */
    @PatchMapping("/api/manager/leaves/{leaveId}/decision")
    PendingLeaveResponse decideLeave(
            @PathVariable("leaveId") Long leaveId,
            @RequestParam("decision") String decision,
            @RequestHeader("Authorization") String authorization
    );

    /**
     * Retrieves details of a specific leave request.
     */
    @GetMapping("/api/manager/leaves/{leaveId}")
    PendingLeaveResponse getLeaveDetails(
            @PathVariable("leaveId") Long leaveId,
            @RequestHeader("Authorization") String authorization
    );

    /**
     * Retrieves employees assigned to the authenticated manager.
     */
    @GetMapping("/api/manager/leaves/employees")
    List<ManagerEmployeeResponse> getMyEmployees(
            @RequestHeader("Authorization") String authorization
    );

    /**
     * Retrieves complete leave history for an assigned employee.
     */
    @GetMapping("/api/manager/leaves/employees/{employeeUserId}/history")
    List<ManagerLeaveHistoryResponse> getEmployeeLeaveHistory(
            @PathVariable("employeeUserId") Long employeeUserId,
            @RequestHeader("Authorization") String authorization
    );
}
