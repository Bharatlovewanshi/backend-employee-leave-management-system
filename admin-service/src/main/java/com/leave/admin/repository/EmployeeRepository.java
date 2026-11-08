
package com.leave.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.leave.admin.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmail(String email);

    // Find all employees assigned to a particular manager
    List<Employee> findByManagerId(Long managerId);

    // Check whether a manager has any assigned employees
    boolean existsByManagerId(Long managerId);
}
