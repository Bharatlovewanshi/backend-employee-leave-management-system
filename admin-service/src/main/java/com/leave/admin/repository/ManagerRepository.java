
package com.leave.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.leave.admin.entity.Manager;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Long> {

    boolean existsByEmail(String email);

    // Check whether a manager has assigned employees
    boolean existsByManagerId(Long managerId);
}
