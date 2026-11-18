package com.kupi.cc.base.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSubscriptionRequestDto extends RequestDto {

    private String userId;
    private String subscriptionPlanId;
    private String stripeSubscriptionId;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
