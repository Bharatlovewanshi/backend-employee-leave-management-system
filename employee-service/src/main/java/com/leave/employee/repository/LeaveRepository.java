
package com.leave.employee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.leave.employee.entity.LeaveApplication;
import com.leave.employee.enums.LeaveStatus;

public interface LeaveRepository extends JpaRepository<LeaveApplication, Long> {

    // Get all leave applications of an employee, newest first
    List<LeaveApplication> findByEmployee_IdOrderByAppliedAtDesc(Long employeeId);

    // Find a particular leave belonging to a particular employee
    Optional<LeaveApplication> findByIdAndEmployee_Id(Long id, Long employeeId);

    // Count all leave applications of an employee
    long countByEmployee_Id(Long employeeId);

    // Count leave applications by employee and status
    long countByEmployee_IdAndStatus(Long employeeId, LeaveStatus status);
}
