
package com.leave.employee.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.leave.employee.dto.request.LeaveApplicationRequest;
import com.leave.employee.dto.response.LeaveResponse;
import com.leave.employee.dto.response.LeaveSummaryResponse;
import com.leave.employee.entity.Employee;
import com.leave.employee.entity.LeaveApplication;
import com.leave.employee.enums.LeaveStatus;
import com.leave.employee.repository.EmployeeRepository;
import com.leave.employee.repository.LeaveRepository;

@Service
@Transactional
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveService(LeaveRepository leaveRepository,
            EmployeeRepository employeeRepository) {
        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
    }

    private Employee getAuthenticatedEmployee(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee profile not found"));
    }

    public LeaveResponse applyLeave(String email,
            LeaveApplicationRequest request) {

        Employee employee = getAuthenticatedEmployee(email);

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End date cannot be before start date");
        }

        LeaveApplication leave = new LeaveApplication();
        leave.setEmployee(employee);
        leave.setLeaveType(request.getLeaveType());
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason());
        leave.setStatus(LeaveStatus.PENDING);

        LeaveApplication savedLeave = leaveRepository.save(leave);

        return convertToResponse(savedLeave);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getMyLeaveHistory(String email) {

        Employee employee = getAuthenticatedEmployee(email);

        return leaveRepository
                .findByEmployee_IdOrderByAppliedAtDesc(employee.getId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LeaveResponse getLeaveDetails(String email, Long leaveId) {

        Employee employee = getAuthenticatedEmployee(email);

        LeaveApplication leave = leaveRepository
                .findByIdAndEmployee_Id(leaveId, employee.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Leave application not found"));

        return convertToResponse(leave);
    }

    public LeaveResponse cancelLeave(String email, Long leaveId) {

        Employee employee = getAuthenticatedEmployee(email);

        LeaveApplication leave = leaveRepository
                .findByIdAndEmployee_Id(leaveId, employee.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Leave application not found"));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only pending leave applications can be cancelled");
        }

        leave.setStatus(LeaveStatus.CANCELLED);

        LeaveApplication updatedLeave = leaveRepository.save(leave);

        return convertToResponse(updatedLeave);
    }

    @Transactional(readOnly = true)
    public LeaveSummaryResponse getLeaveSummary(String email) {

        Employee employee = getAuthenticatedEmployee(email);
        Long employeeId = employee.getId();

        long total = leaveRepository.countByEmployee_Id(employeeId);
        long pending = leaveRepository.countByEmployee_IdAndStatus(
                employeeId, LeaveStatus.PENDING);
        long approved = leaveRepository.countByEmployee_IdAndStatus(
                employeeId, LeaveStatus.APPROVED);
        long rejected = leaveRepository.countByEmployee_IdAndStatus(
                employeeId, LeaveStatus.REJECTED);
        long cancelled = leaveRepository.countByEmployee_IdAndStatus(
                employeeId, LeaveStatus.CANCELLED);

        return new LeaveSummaryResponse(
                total, pending, approved, rejected, cancelled);
    }

    private LeaveResponse convertToResponse(LeaveApplication leave) {

        LeaveResponse response = new LeaveResponse();

        Employee employee = leave.getEmployee();

        response.setLeaveRequestId(leave.getId());
        response.setEmployeeId(employee.getId());
        response.setEmployeeName(
                employee.getFirstName() + " " + employee.getLastName());
        response.setLeaveType(leave.getLeaveType());
        response.setStartDate(leave.getStartDate());
        response.setEndDate(leave.getEndDate());
        response.setReason(leave.getReason());
        response.setStatus(leave.getStatus());
        response.setAppliedAt(leave.getAppliedAt());

        return response;
    }
}
