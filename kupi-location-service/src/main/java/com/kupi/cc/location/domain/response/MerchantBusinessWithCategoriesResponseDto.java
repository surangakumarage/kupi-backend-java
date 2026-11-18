package com.kupi.cc.location.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Merchant Business Response DTO with Categories
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantBusinessWithCategoriesResponseDto extends MerchantBusinessResponseDto {

    private List<BasicCategoryResponseDto> categories;

    public MerchantBusinessWithCategoriesResponseDto(MerchantBusinessResponseDto merchant, List<BasicCategoryResponseDto> categories) {
        this.setId(merchant.getId());
        this.setName(merchant.getName());
        this.setImageUrl(merchant.getImageUrl());
        this.setApprovalStatus(merchant.getApprovalStatus());
        this.setActive(merchant.isActive());
        this.setUserType(merchant.getUserType());
        this.setJoinedOn(merchant.getJoinedOn());
        this.categories = categories;
    }
}

