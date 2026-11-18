package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * SubscriptionAmountResponseDto
 */
@Getter
@Setter
public class SubscriptionAmountResponseDto extends ResponseDto {

    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static final String USD = "$";

    private BigDecimal amount;
    private String displayAmount;

    public SubscriptionAmountResponseDto(BigDecimal amount) {
        this.amount = amount;
        this.displayAmount = USD + df.format(amount);
    }
}
