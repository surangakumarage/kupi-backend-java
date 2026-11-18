package com.kupi.notification.dto.sse.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Payload for coupon redemption event
 * Used for COUPON_REDEEMED events
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRedemptionPayload {

    private String couponId;
    private String couponCode;
    private String dealId;
    private String dealTitle;
    private String merchantId;
    private String merchantName;
    private String userId;                // User who created/purchased the coupon
    private LocalDateTime redeemedAt;
    private String redeemedByMerchantId;  // Merchant who redeemed the coupon
    private Double redemptionValue;
}
