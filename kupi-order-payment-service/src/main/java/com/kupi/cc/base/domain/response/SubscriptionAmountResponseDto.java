package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Getter
@Setter
@NoArgsConstructor
public class SubscriptionAmountResponseDto extends ResponseDto {
    private static final long serialVersionUID = -3154116378414142771L;

    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static final String RS = "Rs. ";

    private BigDecimal amount;
    private String displayAmount;

    public SubscriptionAmountResponseDto(BigDecimal amount) {
        this.amount = amount;
        this.displayAmount = RS + df.format(amount);
    }

}
