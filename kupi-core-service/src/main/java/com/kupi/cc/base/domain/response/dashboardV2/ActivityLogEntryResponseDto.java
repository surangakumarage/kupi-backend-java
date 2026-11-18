package com.kupi.cc.base.domain.response.dashboardV2;

import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Activity Log Entry Response DTO
 * Represents a single activity entry in the activity log
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogEntryResponseDto extends ResponseDto {

    /**
     * Activity type: COUPON_REDEEMED, DEAL_CREATED, DEAL_UPDATED
     */
    private String activityType;

    /**
     * Description of the activity
     */
    private String description;

    /**
     * Deal title (for coupon redemptions and deal activities)
     */
    private String dealTitle;

    /**
     * Deal ID
     */
    private String dealId;

    /**
     * Amount earned (for coupon redemptions only)
     */
    private BigDecimal amount;

    /**
     * Timestamp in milliseconds
     */
    private long timestamp;

    /**
     * Formatted time ago string (e.g., "2 mins ago", "3 hours ago")
     */
    private String timeAgo;
}

