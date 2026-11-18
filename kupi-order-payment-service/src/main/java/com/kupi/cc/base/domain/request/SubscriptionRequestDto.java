package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SubscriptionRequestDto extends RequestDto {
    private static final long serialVersionUID = -3730265601086608586L;

    private String tempSubscriptionId;
    private String subscriptionPlanId;
    private UserType userType;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
