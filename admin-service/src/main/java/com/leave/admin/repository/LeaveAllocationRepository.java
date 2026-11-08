package com.leave.admin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.leave.admin.entity.LeaveAllocation;

public interface LeaveAllocationRepository
        extends JpaRepository<LeaveAllocation, Long> {

    List<LeaveAllocation> findByEmployeeId(Long employeeId);

    List<LeaveAllocation> findByYear(Integer year);

    Optional<LeaveAllocation> findByEmployeeIdAndLeaveTypeIdAndYear(
            Long employeeId,
            Long leaveTypeId,
            Integer year);

    boolean existsByEmployeeIdAndLeaveTypeIdAndYear(
            Long employeeId,
            Long leaveTypeId,
            Integer year);
}