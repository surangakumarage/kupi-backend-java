package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.domain.entity.DealSearchIndex;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Extended Deal Response DTO with additional fields for deal status and earnings
 */
@Setter
@Getter
public class ExtendedDealResponseDto extends BasicDealResponseDto {

    private String dealStatus;
    private BigDecimal earnings;

    public ExtendedDealResponseDto(DealSearchIndex dealSearchIndex, String timeZone,
                                   BigDecimal earnings, String dealStatus) {
        super(dealSearchIndex, timeZone);
        this.earnings = earnings != null ? earnings.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.dealStatus = dealStatus;
    }

    public ExtendedDealResponseDto(Deal deal, BasicMerchantBusinessResponseDto basicMerchantBusinessResponseDto,
                                   String timeZone, BigDecimal earnings, String dealStatus) {
        super(deal, basicMerchantBusinessResponseDto, timeZone);
        this.earnings = earnings != null ? earnings.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.dealStatus = dealStatus;
    }
}

