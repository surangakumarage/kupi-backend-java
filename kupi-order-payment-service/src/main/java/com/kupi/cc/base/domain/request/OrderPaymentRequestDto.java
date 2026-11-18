package com.kupi.cc.base.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderPaymentRequestDto extends RequestDto{

    private static final long serialVersionUID = -2463580846767466619L;
    private OrderCreateRequestDto orderCreateRequestDto;
    private PaymentBrokerRequestDto paymentBrokerRequestDto;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
