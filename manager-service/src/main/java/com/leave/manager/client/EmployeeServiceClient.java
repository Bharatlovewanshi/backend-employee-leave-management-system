package com.leave.manager.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.leave.manager.dto.response.PendingLeaveResponse;

/**
 * Feign client for communicating with Employee Service.
 * Handles REST API calls related to employee leave requests.
 */
@FeignClient(name = "employee-service", url = "${employee.service.url}"
)
public interface EmployeeServiceClient {

    /**
     * Retrieves all pending leave requests assigned to a manager.
     *
     * @param managerId unique ID of the manager
     * @return list of pending leave requests
     */
    @GetMapping("/api/leaves/pending/{managerId}")
    List<PendingLeaveResponse> getPendingLeaves(@PathVariable("managerId") Long managerId);
}