package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.SubscriptionStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionPlanStatusRequestDto extends RequestDto {

    private String id;
    private SubscriptionStatus status;

    @Override
    public String toLogJson() {
        return toJson();
    }

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(id) && status != null;
    }
}
