
package com.leave.admin.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "holidays")
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long holidayId;

    @Column(nullable = false)
    private String holidayName;

    @Column(nullable = false, unique = true)
    private LocalDate holidayDate;

    public Holiday() {
    }

    public Holiday(String holidayName, LocalDate holidayDate) {
        this.holidayName = holidayName;
        this.holidayDate = holidayDate;
    }

    public Long getHolidayId() {
        return holidayId;
    }

    public void setHolidayId(Long holidayId) {
        this.holidayId = holidayId;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public void setHolidayName(String holidayName) {
        this.holidayName = holidayName;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(LocalDate holidayDate) {
        this.holidayDate = holidayDate;
    }
}
