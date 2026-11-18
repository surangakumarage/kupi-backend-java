package com.kupi.cc.base.domain.response;


import com.kupi.cc.base.domain.entity.DealSearchIndex;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bulk Merchant Deals ResponseDto
 */
@Getter
public class BulkMerchantDealsResponseDto extends ResponseDto {

    private final List<MerchantDealsResponseDto> dealGroups = new ArrayList<>();

    public BulkMerchantDealsResponseDto(List<DealSearchIndex> dealSearchIndexList,
                                        Map<String, MerchantBusinessResponseDto> merchantBusinessResponseDtoMap) {

        for (Map.Entry<String, MerchantBusinessResponseDto> merchant : merchantBusinessResponseDtoMap.entrySet()) {
            List<NotificationDealResponseDto> deals = new ArrayList<>();
            for (DealSearchIndex dealSearchIndex : dealSearchIndexList) {
                if (dealSearchIndex.getStoreId().equals(merchant.getKey()))
                    deals.add(new NotificationDealResponseDto(dealSearchIndex));
            }
            if (!deals.isEmpty()) {
                MerchantDealsResponseDto merchantDealsResponseDto =
                        new MerchantDealsResponseDto(merchant.getValue(), deals);
                dealGroups.add(merchantDealsResponseDto);
            }
        }
    }
}
