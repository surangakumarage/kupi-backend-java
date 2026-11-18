package com.kupi.cc.base.domain.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.text.DecimalFormat;


/**
 * Price response dto tpo show deal price
 */
@Setter
@Getter
public class PriceResponseDto extends ResponseDto {

    private static final String USD = "$";
    private static final String SAVE = "Save $";
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private Double originalPrice;
    private String originalDisplayPrice;
    private Double currentPrice;
    private String currentDisplayPrice;
    private Double savedAmount;
    private String savedDisplayAmount;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DiscountPercentageDto discountPercentage;

    public PriceResponseDto(Double price, Double deductionValue, Double deductionPercentage) {
        if (price != null) {
            this.originalPrice = price;
            this.originalDisplayPrice = USD + df.format(price);
            if (deductionValue != null) {
                this.currentPrice = price - deductionValue;
                this.savedAmount = deductionValue;
            } else {
                this.currentPrice = price - (price * deductionPercentage / 100);
                this.savedAmount = price * deductionPercentage / 100;
            }
            this.currentDisplayPrice = USD + df.format(currentPrice);
            this.savedDisplayAmount = SAVE + df.format(savedAmount);
            
            // Calculate and set discount percentage
            this.discountPercentage = new DiscountPercentageDto(this.originalPrice, this.currentPrice);
        }
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
