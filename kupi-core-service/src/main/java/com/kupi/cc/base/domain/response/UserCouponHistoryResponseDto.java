package com.kupi.cc.base.domain.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kupi.cc.base.domain.entity.Coupon;
import com.kupi.cc.base.domain.entity.Deal;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

/**
 * UserCouponHistoryPageResponseDto
 */
@Setter
@Getter
public class UserCouponHistoryResponseDto extends ResponseDto {

    private BasicDealCouponResponseDto deal;
    private String couponCode;
    private CouponPriceResponseDto couponPrice;
    private boolean isRedeemed;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DateResponseDto redeemOn;
    private DateResponseDto expiryOn;
    private DateResponseDto purchasedOn;
    private Double savedAmount;
    private Double productPrice;
    private MerchantBusinessResponseDto merchant;

    public UserCouponHistoryResponseDto(Coupon coupon, String timeZone, MerchantBusinessResponseDto merchant) {
        Deal couponDeal = coupon.getDeal();
        this.deal = new BasicDealCouponResponseDto(couponDeal);
        this.couponCode = coupon.getCouponCode();
        this.isRedeemed = coupon.isRedeemed();
        this.redeemOn = (null != coupon.getRedeemedOn() && coupon.getRedeemedOn() > 0) ?
                new DateResponseDto(coupon.getRedeemedOn(), timeZone, new Date(coupon.getRedeemedOn())) : null;
        this.expiryOn = new DateResponseDto(coupon.getExpiryDate(), timeZone, new Date(coupon.getExpiryDate()));
        this.purchasedOn = new DateResponseDto(coupon.getPurchasedOn(), timeZone, new Date(coupon.getPurchasedOn()));
        this.couponPrice = new CouponPriceResponseDto(couponDeal.getCouponPrice());
        this.savedAmount = couponDeal.getDeductionAmount();
        this.productPrice = couponDeal.isFreeCoupons() ?
                couponDeal.getOriginalPrice() - couponDeal.getDeductionAmount() :
                couponDeal.getOriginalPrice() - couponDeal.getDeductionAmount() - couponDeal.getCouponPrice();
        this.merchant = merchant;
    }
}
