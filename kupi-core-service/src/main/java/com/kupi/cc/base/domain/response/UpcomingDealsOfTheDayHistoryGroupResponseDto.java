package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Deal;
import lombok.Getter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Upcoming deals of the day history group response Dto
 */
@Getter
public class UpcomingDealsOfTheDayHistoryGroupResponseDto extends ResponseDto {

    private DateResponseDto validOn;
    private List<BasicDealResponseDto> dealGroup;

    public UpcomingDealsOfTheDayHistoryGroupResponseDto(long timeStamp, String timeZone, List<Deal> dealList,
                                                        Map<String, MerchantBusinessResponseDto> merchantResponseDtoMap) {
        this.validOn = new DateResponseDto(timeStamp, timeZone, new Date(timeStamp));
        this.dealGroup = new ArrayList<>();
        for (Deal deal : dealList) {
            var basicDealResponseDto =
                    new BasicDealResponseDto(deal, merchantResponseDtoMap.get(deal.getMerchantId()), timeZone);
            dealGroup.add(basicDealResponseDto);
        }
    }
}


