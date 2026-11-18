package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.domain.entity.DealsOfTheDayEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DealsOfTheDayDetailEventResponseDto extends DealsOfTheDayBasicEventResponseDto {

    private String id;
    private BasicMerchantBusinessResponseDto merchant;

    public DealsOfTheDayDetailEventResponseDto(DealsOfTheDayEvent dealsOfTheDayEvent, String timeZone,
                                               List<DealSearchIndex> dealSearchIndexList,
                                               BusinessMerchantResponseDto businessMerchantResponseDto) {
        super(createDealSet(dealSearchIndexList, timeZone), dealsOfTheDayEvent.getValidOn(), timeZone);
        this.id = dealsOfTheDayEvent.getId();
        this.merchant = new BasicMerchantBusinessResponseDto(businessMerchantResponseDto);
    }

    /**
     * This method returns BasicDealResponseDto Set.
     *
     * @param dealSearchIndexList dealSearchIndex list
     * @param timeZone            time zone
     * @return BasicDealResponseDto Set
     */
    private static List<BasicDealResponseDto> createDealSet(List<DealSearchIndex> dealSearchIndexList, String timeZone) {
        List<BasicDealResponseDto> dealResponseDtoSet = new ArrayList<>();
        for (var dealSearch : dealSearchIndexList) {
            dealResponseDtoSet.add(new BasicDealResponseDto(dealSearch, timeZone));
        }
        return dealResponseDtoSet;
    }
}
