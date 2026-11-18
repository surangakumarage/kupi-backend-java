package com.kupi.cc.location.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Merchant Deals with Location ResponseDto
 * Includes merchant deals and branch location information
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MerchantDealsWithLocationResponseDto {

    private MerchantBusinessResponseDto merchant;
    private List<NotificationDealResponseDto> deals;
    private List<LocationResponseDto> branchLocations;
}


