package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Extended Deals of the day history response Dto with dealStatus
 */
@Getter
@Setter
public class ExtendedDealsOfTheDayBasicEventResponseDto extends ResponseDto {

    private DateResponseDto validOn;
    private List<ExtendedDealResponseDto> deals;

    public ExtendedDealsOfTheDayBasicEventResponseDto(long timeStamp, String timeZone, List<ExtendedDealResponseDto> deals) {
        this.validOn = new DateResponseDto(timeStamp, timeZone, new Date(timeStamp));
        this.deals = deals != null ? deals : new ArrayList<>();
    }
}

