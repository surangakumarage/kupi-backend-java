package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.UserType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscribeValidationRequestDto extends RequestDto {

    private String subscriptionPlanId;
    private UserType userType;

    @Override
    public String toLogJson() {
        return toJson();
    }

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(subscriptionPlanId) && userType != null;
    }
}
