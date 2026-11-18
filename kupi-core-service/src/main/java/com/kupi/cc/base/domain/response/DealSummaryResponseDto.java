package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Deal summary response dto
 */
@Setter
@Getter
public class DealSummaryResponseDto extends ResponseDto {
    private String merchantId;
    private long totalDeals;
    private long couponDeals;
    private long specialOfferDeals;

    public DealSummaryResponseDto(Map<String, Long> dealSummary, String merchantId) {
        this.merchantId = merchantId;
        this.totalDeals = dealSummary.get("totalDeals");
        this.couponDeals = dealSummary.get("couponDeals");
        this.specialOfferDeals = dealSummary.get("specialOfferDeals");
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
