package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Merchant Business Response DTO with Categories.
 * Reused pattern from location service, adapted to core-service response types.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantBusinessWithCategoriesResponseDto extends MerchantBusinessResponseDto {

    private List<BasicCategoryResponseDto> categories;
    private List<MerchantSliderImageItemResponseDto> sliderImages;
    private int activeDealsCount;

    public MerchantBusinessWithCategoriesResponseDto(MerchantBusinessResponseDto merchant,
                                                     List<BasicCategoryResponseDto> categories) {
        this.setId(merchant.getId());
        this.setName(merchant.getName());
        this.setImageUrl(merchant.getImageUrl());
        this.setApprovalStatus(merchant.getApprovalStatus());
        this.setActive(merchant.isActive());
        this.setUserType(merchant.getUserType());
        this.setJoinedOn(merchant.getJoinedOn());
        this.setEmail(merchant.getEmail());
        this.categories = categories;
        this.sliderImages = null;
        this.activeDealsCount = 0;
    }
}



