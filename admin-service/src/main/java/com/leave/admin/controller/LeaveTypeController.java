package com.leave.admin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.leave.admin.entity.LeaveType;
import com.leave.admin.service.LeaveTypeService;

@RestController
@RequestMapping("/api/admin/leave-types")
@CrossOrigin(origins = "http://localhost:5173")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    public LeaveTypeController(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    // Get all leave types
    @GetMapping
    public ResponseEntity<List<LeaveType>> getAllLeaveTypes() {
        return ResponseEntity.ok(leaveTypeService.getAllLeaveTypes());
    }

    // Add a leave type
    @PostMapping
    public ResponseEntity<LeaveType> addLeaveType(
            @RequestBody LeaveType leaveType) {

        LeaveType savedLeaveType =
                leaveTypeService.addLeaveType(leaveType);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedLeaveType);
    }

    // Update a leave type
    @PutMapping("/{leaveTypeId}")
    public ResponseEntity<LeaveType> updateLeaveType(
            @PathVariable Long leaveTypeId,
            @RequestBody LeaveType leaveType) {

        LeaveType updatedLeaveType =
                leaveTypeService.updateLeaveType(leaveTypeId, leaveType);

        return ResponseEntity.ok(updatedLeaveType);
    }

    // Delete a leave type
    @DeleteMapping("/{leaveTypeId}")
    public ResponseEntity<Void> deleteLeaveType(
            @PathVariable Long leaveTypeId) {

        leaveTypeService.deleteLeaveType(leaveTypeId);

        return ResponseEntity.noContent().build();
    }
}