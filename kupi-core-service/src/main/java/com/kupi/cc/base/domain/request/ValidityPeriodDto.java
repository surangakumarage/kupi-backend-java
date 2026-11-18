package com.kupi.cc.base.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Validity Period DTO
 * Represents a single validity period with date, time range, and coupon count
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidityPeriodDto {

    /**
     * Date for this validity period (format: yyyy-MM-dd)
     */
    private String date;

    /**
     * Start time for this period (format: HH:MM, e.g., "09:00")
     */
    private String startTime;

    /**
     * End time for this period (format: HH:MM, e.g., "18:00")
     */
    private String endTime;

    /**
     * Number of coupons to create for this validity period
     */
    private Integer couponCount;

    /**
     * Validates that all required fields are present
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return date != null && !date.trim().isEmpty()
                && startTime != null && !startTime.trim().isEmpty()
                && endTime != null && !endTime.trim().isEmpty()
                && couponCount != null && couponCount > 0;
    }
}

