package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Merchant Deals ResponseDto
 */
@Getter
@AllArgsConstructor
public class MerchantDealsResponseDto {

    private MerchantBusinessResponseDto merchant;
    private List<NotificationDealResponseDto> deals;
}
