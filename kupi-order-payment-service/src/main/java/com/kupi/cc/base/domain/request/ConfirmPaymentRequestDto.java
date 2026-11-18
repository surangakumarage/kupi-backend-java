package com.kupi.cc.base.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmPaymentRequestDto extends RequestDto{

    private static final long serialVersionUID = 2146877422814882273L;
    private PaymentBrokerRequestDto paymentBrokerRequestDto;
    private PaymentSummaryRequestDto paymentSummaryRequestDto;


    @Override
    public String toLogJson() {
        return toJson();
    }
}
