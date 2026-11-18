package com.kupi.cc.base.domain.request;

import lombok.Getter;
import lombok.Setter;

/**
 * PaymentBrokerUpdateRequestDto
 */
@Getter
@Setter
public class PaymentBrokerUpdateRequestDto extends PaymentBrokerRequestDto {
    private static final long serialVersionUID = -2458986507044737522L;
    private String id;

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(id) && super.isRequiredAvailable();
    }
}
