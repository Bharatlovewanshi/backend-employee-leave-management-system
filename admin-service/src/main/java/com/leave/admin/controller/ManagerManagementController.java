
package com.leave.admin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.leave.admin.dto.ManagerRequest;
import com.leave.admin.dto.ManagerResponse;
import com.leave.admin.dto.EmployeeResponse;
import com.leave.admin.service.ManagerManagementService;
import com.leave.admin.service.EmployeeManagementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/managers")
public class ManagerManagementController {

    private final ManagerManagementService managerService;
    private final EmployeeManagementService employeeService;

    public ManagerManagementController(
            ManagerManagementService managerService,
            EmployeeManagementService employeeService) {

        this.managerService = managerService;
        this.employeeService = employeeService;
    }

    // CREATE MANAGER
    @PostMapping
    public ResponseEntity<ManagerResponse> createManager(
            @Valid @RequestBody ManagerRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(managerService.createManager(request));
    }

    // GET ALL MANAGERS
    @GetMapping
    public ResponseEntity<List<ManagerResponse>> getAllManagers() {

        return ResponseEntity.ok(managerService.getAllManagers());
    }

    // GET MANAGER BY ID
    @GetMapping("/{managerId}")
    public ResponseEntity<ManagerResponse> getManagerById(
            @PathVariable Long managerId) {

        return ResponseEntity.ok(
                managerService.getManagerById(managerId));
    }

    // UPDATE MANAGER
    @PutMapping("/{managerId}")
    public ResponseEntity<ManagerResponse> updateManager(
            @PathVariable Long managerId,
            @Valid @RequestBody ManagerRequest request) {

        return ResponseEntity.ok(
                managerService.updateManager(managerId, request));
    }

    // DELETE MANAGER
    @DeleteMapping("/{managerId}")
    public ResponseEntity<Void> deleteManager(
            @PathVariable Long managerId) {

        managerService.deleteManager(managerId);

        return ResponseEntity.noContent().build();
    }

    // GET ALL EMPLOYEES UNDER A MANAGER
    @GetMapping("/{managerId}/employees")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByManager(
            @PathVariable Long managerId) {

        return ResponseEntity.ok(
                employeeService.getEmployeesByManager(managerId));
    }
}
