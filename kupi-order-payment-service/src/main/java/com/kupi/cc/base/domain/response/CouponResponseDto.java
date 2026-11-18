package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Coupon;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CouponResponseDto extends ResponseDto {
    private static final long serialVersionUID = -5362798440015805430L;

    private String id;
    private String couponId;
    private String dealId;
    private String merchantId;
    private BigDecimal couponPrice;
    private DateResponseDto dealExpiryDate;
    private DateResponseDto couponExpireDate;


    public CouponResponseDto(Coupon coupon, String timeZone) {
        this.id = coupon.getId();
        this.couponId = coupon.getCouponId();
        this.dealId = coupon.getDealId();
        this.merchantId = coupon.getMerchantId();
        this.couponPrice = coupon.getCouponPrice();
        this.dealExpiryDate = new DateResponseDto(coupon.getDealExpiryDate(), timeZone);
        this.couponExpireDate = new DateResponseDto(coupon.getCouponExpiryDate(), timeZone);
    }

}
