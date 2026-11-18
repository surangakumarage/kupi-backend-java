package com.kupi.cc.base.domain.response.dashboardV2;

import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class MerchantSummaryResponseDto extends ResponseDto {

    private int totalActiveDeals;
    private int dealsOfTheDay;
    private int activeCoupon ;
    private int redeemedCoupon;
    private BigDecimal totalEarnings;

    public MerchantSummaryResponseDto(int totalActiveDeals, int dealsOfTheDay, int activeCoupon, int redeemedCoupon) {
        this.totalActiveDeals = totalActiveDeals;
        this.dealsOfTheDay = dealsOfTheDay;
        this.activeCoupon = activeCoupon;
        this.redeemedCoupon = redeemedCoupon;
    }

    public MerchantSummaryResponseDto(int totalActiveDeals, int dealsOfTheDay, int activeCoupon, int redeemedCoupon, BigDecimal totalEarnings) {
        this.totalActiveDeals = totalActiveDeals;
        this.dealsOfTheDay = dealsOfTheDay;
        this.activeCoupon = activeCoupon;
        this.redeemedCoupon = redeemedCoupon;
        this.totalEarnings = totalEarnings;
    }
}
