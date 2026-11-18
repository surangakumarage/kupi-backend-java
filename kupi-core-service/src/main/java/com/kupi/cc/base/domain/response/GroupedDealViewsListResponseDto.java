package com.kupi.cc.base.domain.response;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class GroupedDealViewsListResponseDto extends PageResponseDto {

    private final List<GroupedDealViewsResponseDto> views = new ArrayList<>();

    public GroupedDealViewsListResponseDto(int totalItems, int page, int size, List<DealViewCountResponse> responseList,
                                           Map<String, MerchantBusinessResponseDto> merchantBusinessResponseDtoMap) {
        super(totalItems, page, size);
        for (DealViewCountResponse dealViewCountResponse : responseList) {
            views.add(new GroupedDealViewsResponseDto(dealViewCountResponse.getDeal(),
                    merchantBusinessResponseDtoMap.get(dealViewCountResponse.getDeal().getMerchantId()),
                    dealViewCountResponse.getViewCount()));
        }
    }
}
