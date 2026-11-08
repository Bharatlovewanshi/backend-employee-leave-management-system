package com.leave.admin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.leave.admin.entity.Holiday;
import com.leave.admin.service.HolidayService;

@RestController
@RequestMapping("/api/admin/holidays")
@CrossOrigin(origins = "http://localhost:5173")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    // Get all holidays
    @GetMapping
    public ResponseEntity<List<Holiday>> getAllHolidays() {
        return ResponseEntity.ok(holidayService.getAllHolidays());
    }

    // Add a holiday
    @PostMapping
    public ResponseEntity<Holiday> addHoliday(@RequestBody Holiday holiday) {
        Holiday savedHoliday = holidayService.addHoliday(holiday);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedHoliday);
    }

    // Update a holiday
    @PutMapping("/{holidayId}")
    public ResponseEntity<Holiday> updateHoliday(
            @PathVariable Long holidayId,
            @RequestBody Holiday holiday) {

        Holiday updatedHoliday =
                holidayService.updateHoliday(holidayId, holiday);

        return ResponseEntity.ok(updatedHoliday);
    }

    // Delete a holiday
    @DeleteMapping("/{holidayId}")
    public ResponseEntity<Void> deleteHoliday(
            @PathVariable Long holidayId) {

        holidayService.deleteHoliday(holidayId);
        return ResponseEntity.noContent().build();
    }
}