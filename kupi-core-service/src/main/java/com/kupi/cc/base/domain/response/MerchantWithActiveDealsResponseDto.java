package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Merchant with Active Deals Count Response DTO.
 * Used for listing merchants (by category) together with number of active deals.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantWithActiveDealsResponseDto extends ResponseDto {

    private MerchantBusinessWithCategoriesResponseDto merchant;
    /**
     * Count of active deals for this merchant.
     */
    private Integer activeDealsCount;

    @Override
    public String toLogJson() {
        return toJson();
    }
}



