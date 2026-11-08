
package com.leave.admin.service;

import java.util.List;

import com.leave.admin.dto.EmployeeResponse;
import com.leave.admin.dto.LeaveRecordResponse;

public interface AdminService {

    // Retrieve all employees
    List<EmployeeResponse> getAllEmployees();

    // Retrieve employee by ID
    EmployeeResponse getEmployeeById(Long employeeId);

    // Retrieve all leave requests
    List<LeaveRecordResponse> getAllLeaveRequests();

    // Retrieve leave request by ID
    LeaveRecordResponse getLeaveRequestById(Long leaveId);

    // Retrieve leave requests by status
    List<LeaveRecordResponse> getLeaveRequestsByStatus(String status);

    // Retrieve leave requests by employee ID
    List<LeaveRecordResponse> getLeaveRequestsByEmployeeId(Long employeeId);

    // Approve leave request
    LeaveRecordResponse approveLeaveRequest(
            Long leaveId, Long adminUserId);

    // Reject leave request
    LeaveRecordResponse rejectLeaveRequest(
            Long leaveId, Long adminUserId, String reason);
}
