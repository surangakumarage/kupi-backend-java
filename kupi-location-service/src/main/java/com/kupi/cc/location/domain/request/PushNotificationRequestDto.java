package com.kupi.cc.location.domain.request;

import com.kupi.cc.location.domain.response.BulkMerchantDealsResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationRequestDto extends RequestDto {

    private String userId;
    private List<DealGroupRequestDto> dealGroups;

    public PushNotificationRequestDto(String userId, BulkMerchantDealsResponseDto bulkMerchantDealsResponseDto) {
        this.userId = userId;
        this.dealGroups = convertToDealGroupRequest(bulkMerchantDealsResponseDto);
    }

    private List<DealGroupRequestDto> convertToDealGroupRequest(BulkMerchantDealsResponseDto bulkMerchantDealsResponseDto) {
        List<DealGroupRequestDto> dealGroupList = new ArrayList<>();

        bulkMerchantDealsResponseDto.getDealGroups().forEach(merchantDeal ->
                dealGroupList.add(new DealGroupRequestDto(merchantDeal.getMerchant(), merchantDeal.getDeals()))
        );
        return dealGroupList;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}