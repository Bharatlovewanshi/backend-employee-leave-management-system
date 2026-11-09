
package com.leave.admin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.leave.admin.dto.EmployeeRequest;
import com.leave.admin.dto.EmployeeResponse;
import com.leave.admin.dto.ManagerAssignmentRequest;
import com.leave.admin.dto.ManagerResponse;
import com.leave.admin.service.EmployeeManagementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/employees")
public class EmployeeManagementController {

    private final EmployeeManagementService employeeService;

    public EmployeeManagementController(
            EmployeeManagementService employeeService) {

        this.employeeService = employeeService;
    }

    // CREATE EMPLOYEE
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.createEmployee(request));
    }

    // GET ALL EMPLOYEES
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {

        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // GET EMPLOYEE BY ID
    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                employeeService.getEmployeeById(employeeId));
    }

    // UPDATE EMPLOYEE
    @PutMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeRequest request) {

        return ResponseEntity.ok(
                employeeService.updateEmployee(employeeId, request));
    }

    // DELETE EMPLOYEE
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> deleteEmployee(
            @PathVariable Long employeeId) {

        employeeService.deleteEmployee(employeeId);

        return ResponseEntity.noContent().build();
    }

    // ASSIGN OR REASSIGN MANAGER
    @PutMapping("/{employeeId}/manager")
    public ResponseEntity<EmployeeResponse> assignManager(
            @PathVariable Long employeeId,
            @Valid @RequestBody ManagerAssignmentRequest request) {

        return ResponseEntity.ok(
                employeeService.assignManager(employeeId, request));
    }

    // GET MANAGER ASSIGNED TO EMPLOYEE
    @GetMapping("/{employeeId}/manager")
    public ResponseEntity<ManagerResponse> getAssignedManager(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                employeeService.getAssignedManager(employeeId));
    }

    // GET ALL EMPLOYEES UNDER A MANAGER
    @GetMapping("/../managers/{managerId}/employees")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByManager(
            @PathVariable Long managerId) {

        return ResponseEntity.ok(
                employeeService.getEmployeesByManager(managerId));
    }
}
