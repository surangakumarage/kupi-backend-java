package com.kupi.cc.location.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Merchant Deals Count with Location ResponseDto
 * Includes merchant with deal count and branch location information
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MerchantDealsCountWithLocationResponseDto {

    private MerchantBusinessWithCategoriesResponseDto merchant;
    private Integer dealCount;
    private List<LocationResponseDto> branchLocations;
}

