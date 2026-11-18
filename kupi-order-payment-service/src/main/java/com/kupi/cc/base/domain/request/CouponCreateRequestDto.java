package com.kupi.cc.base.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import static com.kupi.cc.base.util.DomainUtils.isValidExpiryDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CouponCreateRequestDto extends RequestDto {

    private static final long serialVersionUID = -6714289776974951747L;
    private String couponId;
    private String dealId;
    private String merchantId;
    private BigDecimal couponPrice;
    private long dealExpiryDate;
    private long couponExpiryDate;

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(couponId) && isNonEmpty(merchantId) && isNonEmpty(dealId) &&
                couponPrice.doubleValue() > 0 &&
                isValidExpiryDate(dealExpiryDate) && isValidExpiryDate(couponExpiryDate);
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
