package com.kupi.cc.auth.domain.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpeningHoursRequestDto {

    private String monday;
    private String tuesday;
    private String wednesday;
    private String thursday;
    private String friday;
    private String saturday;
    private String sunday;

    public boolean isValid() {
        return monday != null &&
                tuesday != null &&
                wednesday != null &&
                thursday != null &&
                friday != null &&
                saturday != null &&
                sunday != null;
    }
}
