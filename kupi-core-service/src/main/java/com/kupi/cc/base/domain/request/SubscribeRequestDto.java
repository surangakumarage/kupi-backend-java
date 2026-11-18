package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.UserType;
import lombok.Getter;
import lombok.Setter;

/**
 * request should have validated and payment should have made before proceeding to subscription
 * prerequisites are completed, order-payment service provides with a temporary-subscription-id,
 * use same temp subscription id to save subscription details here (don't generate a new id)
 */
@Getter
@Setter
public class SubscribeRequestDto extends RequestDto {
    private String tempSubscriptionId;
    private String subscriptionPlanId;
    private UserType userType;

    @Override
    public String toLogJson() {
        return toJson();
    }

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(subscriptionPlanId) && isNonEmpty(tempSubscriptionId) && userType != null;
    }
}
