
package com.leave.admin.service;

import java.util.List;

import com.leave.admin.dto.EmployeeRequest;
import com.leave.admin.dto.EmployeeResponse;
import com.leave.admin.dto.ManagerAssignmentRequest;
import com.leave.admin.dto.ManagerResponse;

public interface EmployeeManagementService {

    // Employee CRUD operations
    EmployeeResponse createEmployee(EmployeeRequest request);

    List<EmployeeResponse> getAllEmployees();

    EmployeeResponse getEmployeeById(Long employeeId);

    EmployeeResponse updateEmployee(Long employeeId, EmployeeRequest request);

    void deleteEmployee(Long employeeId);

    // Manager assignment operations
    EmployeeResponse assignManager(
            Long employeeId,
            ManagerAssignmentRequest request
    );

    ManagerResponse getAssignedManager(Long employeeId);

    List<EmployeeResponse> getEmployeesByManager(Long managerId);
}
