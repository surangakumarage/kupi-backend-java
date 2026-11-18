package com.kupi.cc.base.domain.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CheckoutRequestDto extends RequestDto {
    private String userId;
    private String subscriptionId;
    private String email;


    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(userId) && isNonEmpty(subscriptionId) && isNonEmpty(email);
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
