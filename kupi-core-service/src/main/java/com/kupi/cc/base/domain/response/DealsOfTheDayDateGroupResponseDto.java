package com.kupi.cc.base.domain.response;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Deals of the day date group response Dto for today/upcoming API.
 */
@Getter
public class DealsOfTheDayDateGroupResponseDto extends ResponseDto {

    private DateResponseDto validOn;
    private List<BasicDealResponseDto> dealGroup = new ArrayList<>();

    public DealsOfTheDayDateGroupResponseDto(DealsOfTheDayBasicEventResponseDto basicEventResponseDto) {
        this.validOn = basicEventResponseDto.getValidOn();
        if (basicEventResponseDto.getDeals() != null) {
            this.dealGroup.addAll(basicEventResponseDto.getDeals());
        }
    }
}


