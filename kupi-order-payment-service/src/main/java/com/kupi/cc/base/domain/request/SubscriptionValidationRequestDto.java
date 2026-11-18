package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.UserType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionValidationRequestDto extends RequestDto {
    private static final long serialVersionUID = 3962156945096996023L;

    private String subscriptionPlanId;
    private UserType userType;

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(subscriptionPlanId) && userType != null;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
