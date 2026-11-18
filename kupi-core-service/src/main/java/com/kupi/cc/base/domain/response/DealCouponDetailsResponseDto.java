package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Deal coupon details response Dto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DealCouponDetailsResponseDto extends ResponseDto {

    private String title;
    private String subTitle;
    private int totalCoupons;
    private int availableCoupons;
    private int purchasedCoupons;

    public DealCouponDetailsResponseDto(String title, String subTitle, int totalCoupons, int availableCoupons) {
        this.title = title;
        this.subTitle = subTitle;
        this.totalCoupons = totalCoupons;
        this.availableCoupons = availableCoupons;
        this.purchasedCoupons = totalCoupons - availableCoupons;
    }
}

