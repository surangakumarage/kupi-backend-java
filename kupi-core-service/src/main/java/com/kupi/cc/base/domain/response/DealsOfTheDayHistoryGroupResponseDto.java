package com.kupi.cc.base.domain.response;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Deals of the day history group response Dto
 */
@Getter
public class DealsOfTheDayHistoryGroupResponseDto extends PageResponseDto {

    private static final int HISTORY_PAGE_SIZE = 1;
    private final List<DealsOfTheDayBasicEventResponseDto> dealGroups = new ArrayList<>();


    public DealsOfTheDayHistoryGroupResponseDto(int totalItems, int page,
                                                List<DealsOfTheDayBasicEventResponseDto> responseDto) {
        super(totalItems, totalItems, page, HISTORY_PAGE_SIZE);
        if (!responseDto.isEmpty())
            dealGroups.add(responseDto.get(page));
    }
}
