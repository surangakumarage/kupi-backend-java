package com.kupi.cc.base.domain.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentSummaryRequestDto extends RequestDto{

    private static final long serialVersionUID = -8868229925063079340L;

    private BigDecimal totalTax;
    private BigDecimal totalDeliveryCharge;
    private BigDecimal payByVoucher;
    private BigDecimal payByCoupon;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
