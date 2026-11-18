package com.kupi.notification.dto.sse.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for deal-related events
 * Used for DEAL_CREATED, DEAL_APPROVED, DEAL_REJECTED, DEAL_STATUS_CHANGED events
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealEventPayload {

    private String dealId;
    private String dealTitle;
    private String dealDescription;
    private String merchantId;
    private String merchantName;
    private String imageUrl;
    private String dealStatus;           // NEW, APPROVED, REJECTED, EXPIRED, ACTIVE
    private Double originalPrice;
    private Double discountedPrice;
    private String discountPercentage;
}
