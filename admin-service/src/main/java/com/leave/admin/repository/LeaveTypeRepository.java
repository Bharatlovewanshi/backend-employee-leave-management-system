package com.leave.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.leave.admin.entity.LeaveType;

public interface LeaveTypeRepository
        extends JpaRepository<LeaveType, Long> {

    boolean existsByLeaveTypeName(String leaveTypeName);

    boolean existsByLeaveTypeNameAndLeaveTypeIdNot(
            String leaveTypeName, Long leaveTypeId);
}