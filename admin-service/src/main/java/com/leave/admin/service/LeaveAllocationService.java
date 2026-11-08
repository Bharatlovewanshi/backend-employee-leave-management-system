package com.leave.admin.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.leave.admin.entity.Employee;
import com.leave.admin.entity.LeaveType;
import com.leave.admin.entity.LeaveAllocation;
import com.leave.admin.repository.EmployeeRepository;
import com.leave.admin.repository.LeaveTypeRepository;
import com.leave.admin.repository.LeaveAllocationRepository;

@Service
public class LeaveAllocationService {

    private final LeaveAllocationRepository allocationRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    public LeaveAllocationService(
            LeaveAllocationRepository allocationRepository,
            EmployeeRepository employeeRepository,
            LeaveTypeRepository leaveTypeRepository) {

        this.allocationRepository = allocationRepository;
        this.employeeRepository = employeeRepository;
        this.leaveTypeRepository = leaveTypeRepository;
    }

    // Allocate leave to an employee
    public LeaveAllocation allocateLeave(LeaveAllocation allocation) {

        if (allocation.getTotalAllocatedDays() == null
                || allocation.getTotalAllocatedDays() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Allocated days must be greater than zero");
        }

        if (allocation.getYear() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Year is required");
        }

        Employee employee = employeeRepository
                .findById(allocation.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found"));

        LeaveType leaveType = leaveTypeRepository
                .findById(allocation.getLeaveTypeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Leave type not found"));

        boolean alreadyAllocated =
                allocationRepository.existsByEmployeeIdAndLeaveTypeIdAndYear(
                        allocation.getEmployeeId(),
                        allocation.getLeaveTypeId(),
                        allocation.getYear());

        if (alreadyAllocated) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Leave already allocated for this employee and year");
        }

        allocation.setUsedDays(0);
        allocation.setRemainingDays(allocation.getTotalAllocatedDays());

        return allocationRepository.save(allocation);
    }

    // Get all allocations
    public List<LeaveAllocation> getAllAllocations() {
        return allocationRepository.findAll();
    }

    // Get allocations for one employee
    public List<LeaveAllocation> getAllocationsByEmployee(Long employeeId) {

        if (!employeeRepository.existsById(employeeId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Employee not found");
        }

        return allocationRepository.findByEmployeeId(employeeId);
    }

    // Get allocations for a particular year
    public List<LeaveAllocation> getAllocationsByYear(Integer year) {
        return allocationRepository.findByYear(year);
    }
}