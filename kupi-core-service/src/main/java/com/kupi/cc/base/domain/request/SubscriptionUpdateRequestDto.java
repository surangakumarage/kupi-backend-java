package com.kupi.cc.base.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionUpdateRequestDto extends SubscriptionPlanRequestDto {

    private String id;

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(id) && super.isRequiredAvailable();
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
