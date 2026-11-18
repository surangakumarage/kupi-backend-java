package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Coupon;
import com.kupi.cc.base.enums.CouponPurchaseStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * Basic coupon response Dto
 */
@Getter
@Setter
public class BasicCouponResponseDto extends ResponseDto {

    private String id;
    private String dealId;
    private String dealTitle;
    private boolean couponActive;
    private String couponCode;
    private boolean redeemed;
    private DateResponseDto purchasedOn;
    private DateResponseDto redeemedOn;
    private DateResponseDto expiredOn;
    private boolean expired;
    private BigDecimal amount;
    private BasicUserResponseDto user;
    private CouponPurchaseStatus couponPurchaseStatus;
    private BasicDealResponseDto deal;

    public BasicCouponResponseDto(Coupon coupon, String timeZone, BusinessMerchantResponseDto merchantResponseDto) {
        this.id = coupon.getId();
        this.dealId = coupon.getDeal().getId();
        this.dealTitle = coupon.getDeal().getTitle();
        this.couponActive = coupon.isCouponActive();
        this.couponCode = coupon.getCouponCode();
        this.redeemed = coupon.isRedeemed();
        this.purchasedOn = null == coupon.getPurchasedOn() ? null :
                new DateResponseDto(coupon.getPurchasedOn(), timeZone, new Date(coupon.getPurchasedOn()));
        this.redeemedOn = null == coupon.getRedeemedOn() ? null :
                new DateResponseDto(coupon.getRedeemedOn(), timeZone, new Date(coupon.getRedeemedOn()));
        this.expiredOn = new DateResponseDto(coupon.getExpiryDate(), timeZone, new Date(coupon.getExpiryDate()));
        this.expired = this.expiredOn.getMilliseconds() < System.currentTimeMillis();
        this.amount = null == coupon.getPrice() ? null : BigDecimal.valueOf(coupon.getPrice());
        this.couponPurchaseStatus = coupon.getCouponPurchaseStatus();
        this.deal = new BasicDealResponseDto(coupon.getDeal(),
                merchantResponseDto != null ? new BasicMerchantBusinessResponseDto(merchantResponseDto) : null,
                timeZone);
    }

    public BasicCouponResponseDto(Coupon coupon, BasicUserResponseDto user, String timeZone) {
        this(coupon, timeZone,null);
        this.user = user;
    }
}
