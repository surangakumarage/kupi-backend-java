package com.kupi.cc.base.domain.response;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Extended Upcoming deals of the day history response Dto with dealStatus
 */
@Getter
public class ExtendedUpcomingDealsOfTheDayHistoryResponseDto extends ResponseDto {

    private final List<ExtendedUpcomingDealsOfTheDayHistoryGroupResponseDto> dods = new ArrayList<>();

    public ExtendedUpcomingDealsOfTheDayHistoryResponseDto(
            List<ExtendedUpcomingDealsOfTheDayHistoryGroupResponseDto> responseDtoList) {
        if (responseDtoList != null && !responseDtoList.isEmpty()) {
            dods.addAll(responseDtoList);
        }
    }
}

