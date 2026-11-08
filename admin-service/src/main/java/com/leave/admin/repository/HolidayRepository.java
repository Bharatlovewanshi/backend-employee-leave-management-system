package com.leave.admin.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.leave.admin.entity.Holiday;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    boolean existsByHolidayDate(LocalDate holidayDate);

    boolean existsByHolidayDateAndHolidayIdNot(
            LocalDate holidayDate, Long holidayId);
}