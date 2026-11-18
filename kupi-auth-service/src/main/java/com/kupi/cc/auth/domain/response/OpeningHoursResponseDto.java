package com.kupi.cc.auth.domain.response;

import com.kupi.cc.auth.domain.entity.OpeningHours;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpeningHoursResponseDto {

    private String monday;
    private String tuesday;
    private String wednesday;
    private String thursday;
    private String friday;
    private String saturday;
    private String sunday;

    public OpeningHoursResponseDto(OpeningHours openingHours) {
        if (openingHours != null) {
            this.monday = openingHours.getMonday();
            this.tuesday = openingHours.getTuesday();
            this.wednesday = openingHours.getWednesday();
            this.thursday = openingHours.getThursday();
            this.friday = openingHours.getFriday();
            this.saturday = openingHours.getSaturday();
            this.sunday = openingHours.getSunday();
        }
    }
}
