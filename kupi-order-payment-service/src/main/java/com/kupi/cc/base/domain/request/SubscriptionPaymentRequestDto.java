package com.kupi.cc.base.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionPaymentRequestDto extends RequestDto {
    private static final long serialVersionUID = -1526104573026058244L;
    private SubscriptionValidationRequestDto subscriptionValidationRequestDto;
    private String paymentBrokerId;

    @Override
    public boolean isRequiredAvailable() {
        return subscriptionValidationRequestDto.isRequiredAvailable() && isNonEmpty(paymentBrokerId);
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}