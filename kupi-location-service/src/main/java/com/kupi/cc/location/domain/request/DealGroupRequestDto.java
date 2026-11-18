package com.kupi.cc.location.domain.request;

import com.kupi.cc.location.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.location.domain.response.NotificationDealResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Deal Group RequestDto
 */
@Getter
@Setter
public class DealGroupRequestDto extends RequestDto {


    private BasicUserRequestDto merchant;
    private List<BasicDealRequestDto> deals;

    public DealGroupRequestDto(MerchantBusinessResponseDto merchant, List<NotificationDealResponseDto> deals) {
        this.merchant = new BasicUserRequestDto(merchant.getId(), merchant.getImageUrl());
        this.deals = convertDeals(deals);
    }

    private List<BasicDealRequestDto> convertDeals(List<NotificationDealResponseDto> deals) {

        List<BasicDealRequestDto> basicDealsList = new ArrayList<>();
        for (NotificationDealResponseDto deal : deals) {
            basicDealsList.add(new BasicDealRequestDto(deal.getId(), deal.getTitle(), deal.getSubTitle(),
                    deal.getCoverImage(), deal.getDealSource()));
        }
        return basicDealsList;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}