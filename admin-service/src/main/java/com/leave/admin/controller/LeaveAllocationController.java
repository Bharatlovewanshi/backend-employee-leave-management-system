package com.leave.admin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.leave.admin.entity.LeaveAllocation;
import com.leave.admin.service.LeaveAllocationService;

@RestController
@RequestMapping("/api/admin/leave-allocations")
@CrossOrigin(origins = "http://localhost:5173")
public class LeaveAllocationController {

    private final LeaveAllocationService allocationService;

    public LeaveAllocationController(
            LeaveAllocationService allocationService) {
        this.allocationService = allocationService;
    }

    // Allocate leave to an employee
    @PostMapping
    public ResponseEntity<LeaveAllocation> allocateLeave(
            @RequestBody LeaveAllocation allocation) {

        LeaveAllocation savedAllocation =
                allocationService.allocateLeave(allocation);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedAllocation);
    }

    // Get all leave allocations
    @GetMapping
    public ResponseEntity<List<LeaveAllocation>> getAllAllocations() {
        return ResponseEntity.ok(
                allocationService.getAllAllocations());
    }

    // Get allocations by employee ID
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveAllocation>> getByEmployee(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                allocationService.getAllocationsByEmployee(employeeId));
    }

    // Get allocations by year
    @GetMapping("/year/{year}")
    public ResponseEntity<List<LeaveAllocation>> getByYear(
            @PathVariable Integer year) {

        return ResponseEntity.ok(
                allocationService.getAllocationsByYear(year));
    }
}