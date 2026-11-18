package com.kupi.cc.location.domain.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Merchant Deals ResponseDto
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MerchantDealsResponseDto {

    private MerchantBusinessResponseDto merchant;
    private List<NotificationDealResponseDto> deals;
}