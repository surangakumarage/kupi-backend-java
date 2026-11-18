package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Deal;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Deals of the day history response Dto
 */
@Getter
@Setter
public class DealsOfTheDayBasicEventResponseDto extends ResponseDto {

    private DateResponseDto validOn;
    private List<BasicDealResponseDto> deals;

    public DealsOfTheDayBasicEventResponseDto(long timeStamp, String timeZone, List<Deal> dealList,
                                              Map<String, MerchantBusinessResponseDto> merchantResponseDtoMap) {

        this.validOn = new DateResponseDto(timeStamp, timeZone, new Date(timeStamp));
        this.deals = new ArrayList<>();
        for (Deal deal : dealList) {
            var basicDealResponseDto = new BasicDealResponseDto(deal,
                    merchantResponseDtoMap.get(deal.getMerchantId()), timeZone);
            deals.add(basicDealResponseDto);
        }
    }

    public DealsOfTheDayBasicEventResponseDto(List<BasicDealResponseDto> deals, Date validOnDate, String timeZone) {
        this.validOn = new DateResponseDto(validOnDate.getTime(), timeZone, new Date(validOnDate.getTime()));
        this.deals = deals;
    }
}
