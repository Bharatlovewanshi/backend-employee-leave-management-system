
package com.leave.admin.dto;

import java.time.LocalDate;

public class HolidayRequest {

    private Long holidayId;
    private String holidayName;
    private LocalDate holidayDate;

    public HolidayRequest() {
    }

    public HolidayRequest(Long holidayId, String holidayName, LocalDate holidayDate) {
        this.holidayId = holidayId;
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
