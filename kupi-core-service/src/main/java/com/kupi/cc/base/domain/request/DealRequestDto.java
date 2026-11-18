package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.DeductionType;
import com.kupi.cc.base.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Deal request dto
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DealRequestDto extends RequestDto {

    private static final int NETWORK_DELAY_IN_MILLISECONDS = 3600;
    private String title;
    private String subTitle;
    private String description;
    private String termsAndConditions;
    private List<String> imageUrls;
    private String coverImage;
    private Double originalPrice;
    private int quantity;
    private DeductionType deductionType;
    private Double deductionAmount;
    private Double deductionPercentage;
    private Double couponPrice;
    private long validFrom;
    private long expiredOn;
    private String startTime;
    private String endTime;
    private String merchantId;
    private List<String> brandIds;
    private List<String> categoryIds;
    private String promoTypeId;
    private String shopId;
    private boolean isCouponAvailable;
    private boolean freeCoupons;
    /**
     * Publish action: "DRAFT" to save as draft, "PUBLISH" to publish immediately.
     * If not provided, defaults to "DRAFT" for backward compatibility.
     */
    private String publishAction;

    /**
     * List of validity periods for coupon creation.
     * Each period represents a date, time range, and number of coupons to create.
     * Only used when isCouponAvailable = true.
     */
    private List<ValidityPeriodDto> validityPeriods;

    @Override
    public String toLogJson() {
        return toJson();
    }

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(title) && isNonEmpty(subTitle) && isNonEmpty(termsAndConditions)
                && isNonEmpty(merchantId) && categoryIds != null && !categoryIds.isEmpty() && deductionType != null
                && isNonEmpty(coverImage) && isDeductionValueAvailable() && !imageUrls.isEmpty() && isCouponFieldsValid();
    }

    /**
     * This method checks if coupon fields are valid.
     *
     * @return true/false
     */
    private boolean isCouponFieldsValid() {
        return (isCouponAvailable && ((freeCoupons && couponPrice == null) ||
                (!freeCoupons && couponPrice != null && couponPrice > 0))) ||
                (!isCouponAvailable && !freeCoupons && couponPrice == null);
    }

    /**
     * This method checks if required fields are available for user type.
     *
     * @param userType MERCHANT/BANK
     * @return true/false
     */
    public boolean isRequiredAvailable(UserType userType) {
        if (userType.equals(UserType.BANK)) {
            return isRequiredAvailable() && isNonEmpty(shopId);
        }
        return isRequiredAvailable();
    }

    /**
     * This method checks validFrom date is in future or not.
     * NETWORK_DELAY_IN_MILLISECONDS (1 minute) was introduce to discard the network delay.
     *
     * @return true / false
     */
    public boolean isValidStartDate() {
        return validFrom > System.currentTimeMillis() - NETWORK_DELAY_IN_MILLISECONDS;
    }

    /**
     * This method checks expireDate is in future or not
     *
     * @return true / false
     */
    public boolean isValidExpireDate() {
        return expiredOn > validFrom;
    }

    /**
     * This method validate all prices.
     *
     * @return true / false
     */
    public boolean isValidPrice() {
        return originalPrice != null && originalPrice > 0 && isValidAmountWithCouponPrice();
    }

    /**
     * This method checks if deduction amount & coupon amount are valid.
     *
     * @return true/ false
     */
    public boolean isValidAmountWithCouponPrice() {
        if (isCouponAvailable && !freeCoupons) {
            return originalPrice > deductionAmount + couponPrice;
        }
        return true;
    }

    /**
     * This method checks the brand id is available or not.
     *
     * @return true / false
     */
    public boolean isBrandIdsAvailable() {
        return brandIds != null && !brandIds.isEmpty();
    }

    /**
     * This method checks the amount or percentage values according to the deductionType
     */
    public boolean isDeductionValueAvailable() {
        if (deductionType.equals(DeductionType.AMOUNT)) {
            return deductionAmount != null;
        } else {
            return deductionPercentage != null;
        }
    }

    /**
     * This method checks the amount or percentage values are valid values.
     */
    public boolean isValidDeductionValue() {
        if (deductionType.equals(DeductionType.AMOUNT)) {
            return isValidDeductionAmount();
        } else {
            return isValidDeductionPercentage();
        }
    }

    /**
     * This method returns true when deduction amount is valid.
     *
     * @return true/ false
     */
    private boolean isValidDeductionAmount() {
        if (originalPrice != null) {
            return originalPrice > 0 && deductionAmount > 0 && deductionAmount <= originalPrice;
        } else {
            return deductionAmount > 0;
        }
    }

    /**
     * This method returns true when deduction percentage is valid.
     *
     * @return true/ false
     */
    private boolean isValidDeductionPercentage() {
        if (originalPrice == null || originalPrice > 0) {
            return deductionPercentage > 0 && deductionPercentage <= 100;
        } else {
            return false;
        }
    }
}
