package com.kupi.cc.base.domain.response;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Extended Deals of the day history group response Dto with dealStatus
 */
@Getter
public class ExtendedDealsOfTheDayHistoryGroupResponseDto extends PageResponseDto {

    private static final int HISTORY_PAGE_SIZE = 1;
    private final List<ExtendedDealsOfTheDayBasicEventResponseDto> dealGroups = new ArrayList<>();


    public ExtendedDealsOfTheDayHistoryGroupResponseDto(int totalItems, int page,
                                                List<ExtendedDealsOfTheDayBasicEventResponseDto> responseDto) {
        super(totalItems, totalItems, page, HISTORY_PAGE_SIZE);
        if (!responseDto.isEmpty())
            dealGroups.add(responseDto.get(page));
    }
}

