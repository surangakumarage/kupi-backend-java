package com.kupi.cc.base.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCaptureRequestDto extends RequestDto{
    private static final long serialVersionUID = -7024755642350138298L;

    private String paymentId;
    private String payerId;

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(paymentId) && isNonEmpty(payerId);
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
