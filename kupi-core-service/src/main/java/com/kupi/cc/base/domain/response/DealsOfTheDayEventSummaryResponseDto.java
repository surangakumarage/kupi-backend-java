package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Deals of the day event summary response Dto
 */
@AllArgsConstructor
@Getter
public class DealsOfTheDayEventSummaryResponseDto extends ResponseDto {

    private long noOfBankEvents;
    private long noOfMerchantEvents;
    private long noOfTotalEvents;
    private long noOfBanks;
    private long noOfMerchants;
}
