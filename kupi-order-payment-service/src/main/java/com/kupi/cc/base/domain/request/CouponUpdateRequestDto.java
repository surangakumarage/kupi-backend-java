package com.kupi.cc.base.domain.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import static com.kupi.cc.base.util.DomainUtils.isValidExpiryDate;

@Getter
@Setter
public class CouponUpdateRequestDto extends RequestDto{

    private static final long serialVersionUID = -4297739658392072087L;
    private String id;
    private String couponId;
    private String dealId;
    private String merchantId;
    private BigDecimal couponPrice;
    private long dealExpiryDate;
    private long couponExpiryDate;
    private boolean deleted;

    /**
     * check availability of all required attributes in coupon update request DTO
     * coupon id can be null -> order update request DTO might contain new coupons, which needs to be added as new coupon
     */
    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(couponId) && isNonEmpty(dealId) && isNonEmpty(merchantId)
                && couponPrice.doubleValue() > 0
                && isValidExpiryDate(couponExpiryDate) && isValidExpiryDate(dealExpiryDate);
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
