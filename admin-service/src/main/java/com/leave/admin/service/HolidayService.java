package com.leave.admin.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.leave.admin.entity.Holiday;
import com.leave.admin.repository.HolidayRepository;

@Service
public class HolidayService {

    private final HolidayRepository holidayRepository;

    public HolidayService(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    // Get all holidays
    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
    }

    // Add a new holiday
    public Holiday addHoliday(Holiday holiday) {

        if (holidayRepository.existsByHolidayDate(holiday.getHolidayDate())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Holiday already exists for this date");
        }

        return holidayRepository.save(holiday);
    }

    // Update an existing holiday
    public Holiday updateHoliday(Long holidayId, Holiday updatedHoliday) {

        Holiday existingHoliday = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Holiday not found"));

        if (holidayRepository.existsByHolidayDateAndHolidayIdNot(
                updatedHoliday.getHolidayDate(), holidayId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Another holiday already exists for this date");
        }

        existingHoliday.setHolidayName(updatedHoliday.getHolidayName());
        existingHoliday.setHolidayDate(updatedHoliday.getHolidayDate());

        return holidayRepository.save(existingHoliday);
    }

    // Delete a holiday
    public void deleteHoliday(Long holidayId) {

        if (!holidayRepository.existsById(holidayId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Holiday not found");
        }

        holidayRepository.deleteById(holidayId);
    }
}