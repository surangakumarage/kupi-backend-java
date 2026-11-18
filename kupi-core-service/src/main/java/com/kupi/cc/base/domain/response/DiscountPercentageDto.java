package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.text.DecimalFormat;

/**
 * Discount Percentage DTO
 * Contains discount percentage information calculated from original and current prices
 */
@Setter
@Getter
public class DiscountPercentageDto extends ResponseDto {

    private static final DecimalFormat df = new DecimalFormat("0");
    private static final String OFF_SUFFIX = "% OFF";
    
    private Double offPercentage;
    private String offPercentageDisplayText;

    /**
     * Constructor that calculates discount percentage from original and current prices
     *
     * @param originalPrice the original price
     * @param currentPrice  the current/discounted price
     */
    public DiscountPercentageDto(Double originalPrice, Double currentPrice) {
        if (originalPrice != null && currentPrice != null && originalPrice > 0) {
            // Calculate percentage: ((original - current) / original) * 100
            double discount = originalPrice - currentPrice;
            if (discount > 0) {
                this.offPercentage = (discount / originalPrice) * 100;
                // Round to nearest integer for display
                this.offPercentageDisplayText = df.format(this.offPercentage) + OFF_SUFFIX;
            } else {
                this.offPercentage = 0.0;
                this.offPercentageDisplayText = "0% OFF";
            }
        }
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}

