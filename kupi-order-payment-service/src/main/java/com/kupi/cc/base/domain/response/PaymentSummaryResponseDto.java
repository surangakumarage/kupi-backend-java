package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentSummaryResponseDto extends ResponseDto{
    private static final long serialVersionUID = 6523136130865109376L;

    private BigDecimal totalTax;
    private BigDecimal totalDeliveryCharge;
    private BigDecimal payByVoucher;
    private BigDecimal payByCoupon;
}
