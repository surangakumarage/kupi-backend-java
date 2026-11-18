package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.PaymentBroker;
import com.kupi.cc.base.enums.CountryCode;
import com.kupi.cc.base.enums.CurrencyType;
import com.kupi.cc.base.enums.PaymentBrokerName;
import com.kupi.cc.base.enums.PaymentOptionType;
import lombok.Getter;
import lombok.Setter;

/**
 * PaymentBrokerResponseDto
 */
@Getter
@Setter
public class PaymentBrokerResponseDto extends ResponseDto {
    private static final long serialVersionUID = 8538120000483040952L;

    private String id;
    private PaymentBrokerName name;
    private CountryCode countyCode;
    private PaymentOptionType paymentOptionType;
    private CurrencyType currencyType;
    private boolean isActive;

    public PaymentBrokerResponseDto(PaymentBroker paymentBroker) {
        this.id = paymentBroker.getId();
        this.name = paymentBroker.getName();
        this.countyCode = paymentBroker.getCountryCode();
        this.paymentOptionType = paymentBroker.getPaymentOptionType();
        this.currencyType = paymentBroker.getCurrencyType();
        this.isActive = paymentBroker.isActive();
    }
}
