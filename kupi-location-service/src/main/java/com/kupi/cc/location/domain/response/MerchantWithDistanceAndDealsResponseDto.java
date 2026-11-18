package com.kupi.cc.location.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Merchant with Distance and Active Deals Count Response DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantWithDistanceAndDealsResponseDto {

    private MerchantBusinessWithCategoriesResponseDto merchant;
    /**
     * Distance from the user's location to the nearest merchant branch, in kilometers.
     */
    private Double distanceKm;
    /**
     * Distance from the user's location to the nearest merchant branch, in meters.
     */
    private Double distanceInMeters;
    /**
     * Count of active deals for this merchant.
     */
    private Integer activeDealsCount;
}

