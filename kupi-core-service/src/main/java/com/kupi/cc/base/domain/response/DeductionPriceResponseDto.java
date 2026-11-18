package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.text.DecimalFormat;

/**
 * DeductionPriceResponseDto
 */
@Setter
@Getter
public class DeductionPriceResponseDto extends ResponseDto {
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static final String USD = "$";

    private Double deductionAmount;
    private String displayDeductionAmount;

    public DeductionPriceResponseDto(Double deductionAmount) {
        if (deductionAmount != null) {
            this.deductionAmount = deductionAmount;
            this.displayDeductionAmount = USD + df.format(deductionAmount);
        }
    }
}
