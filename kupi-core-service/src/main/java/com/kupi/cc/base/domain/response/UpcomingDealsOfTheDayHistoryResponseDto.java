package com.kupi.cc.base.domain.response;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Upcoming deals of the day history response Dto
 */
@Getter
public class UpcomingDealsOfTheDayHistoryResponseDto extends ResponseDto {

    private final List<UpcomingDealsOfTheDayHistoryGroupResponseDto> dods = new ArrayList<>();

    public UpcomingDealsOfTheDayHistoryResponseDto(
            List<UpcomingDealsOfTheDayHistoryGroupResponseDto> responseDtoList) {
        if (responseDtoList != null && !responseDtoList.isEmpty()) {
            dods.addAll(responseDtoList);
        }
    }
}


