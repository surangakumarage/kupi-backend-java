package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantTableResponseDto extends ResponseDto   {

    private String id;
    private String imageUrl;
    private String name;
    private String businessStatus;
    private String profileStatus;
    private String joinedOn;
    private int totalDeals;
    private int activeDeals;


    public MerchantTableResponseDto(AuthMerchantListResponseDto authMerchantListResponseDto,int totalDeals, int activeDeals) {
        this.id = authMerchantListResponseDto.getProfile().getId();
        this.imageUrl = authMerchantListResponseDto.getBusiness().getImageUrl();
        this.name = (authMerchantListResponseDto.getBusiness().getName() != null
                && !authMerchantListResponseDto.getBusiness().getName().isBlank())
                ? authMerchantListResponseDto.getBusiness().getName()
                : authMerchantListResponseDto.getProfile().getName();
        this.businessStatus = authMerchantListResponseDto.getBusiness().getApprovalStatus().name();
        this.profileStatus = authMerchantListResponseDto.getProfile().getApprovalStatus().name();
        this.joinedOn = authMerchantListResponseDto.getProfile().getJoinedOn().getDisplayDate();
        this.totalDeals = totalDeals;
        this.activeDeals = activeDeals;
    }
}
