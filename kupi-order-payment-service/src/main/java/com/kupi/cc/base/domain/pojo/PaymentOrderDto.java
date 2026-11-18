package com.kupi.cc.base.domain.pojo;

import com.kupi.cc.base.domain.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class PaymentOrderDto implements BaseDto, Serializable {
    private static final long serialVersionUID = 5567869081426973629L;

    private String orderId;
    private String authorizationRedirectUrl;
    private PaymentOrderState state;
    private String payableId;
    private String paymentBrokerId;
    private String request;
    private String response;

    public PaymentOrderDto(String orderId, String approvalLink) {
        this.orderId = orderId;
        this.authorizationRedirectUrl = approvalLink;
        this.state = PaymentOrderState.ORDER_CREATED;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }

    public enum PaymentOrderState {
        ORDER_CREATED,
        CAPTURED
    }
}
