package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StripeSessionResponseDto extends ResponseDto{
    private String id;
    private String customerEmail;
    private Long amountTotal;
    private String status;
    private String subscriptionId;

    public StripeSessionResponseDto(StripeSessionResponseDto stripeSessionResponseDto) {
    this.id = stripeSessionResponseDto.getId();
    this.customerEmail = stripeSessionResponseDto.getCustomerEmail();
    this.amountTotal = stripeSessionResponseDto.getAmountTotal();
    this.status = stripeSessionResponseDto.getStatus();
    this.subscriptionId = stripeSessionResponseDto.getSubscriptionId();
    }
}
