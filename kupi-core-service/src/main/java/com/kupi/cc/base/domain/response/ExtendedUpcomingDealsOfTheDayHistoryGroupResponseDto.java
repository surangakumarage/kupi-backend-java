package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Extended Upcoming deals of the day history group response Dto with dealStatus
 */
@Getter
@Setter
public class ExtendedUpcomingDealsOfTheDayHistoryGroupResponseDto extends ResponseDto {

    private DateResponseDto validOn;
    private List<ExtendedDealResponseDto> dealGroup;

    public ExtendedUpcomingDealsOfTheDayHistoryGroupResponseDto(long timeStamp, String timeZone, List<ExtendedDealResponseDto> deals) {
        this.validOn = new DateResponseDto(timeStamp, timeZone, new Date(timeStamp));
        this.dealGroup = deals != null ? deals : new ArrayList<>();
    }
}

