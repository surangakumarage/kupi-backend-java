package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.CountryCode;
import com.kupi.cc.base.enums.CurrencyType;
import com.kupi.cc.base.enums.PaymentBrokerName;
import com.kupi.cc.base.enums.PaymentOptionType;
import lombok.Getter;
import lombok.Setter;

/**
 * PaymentBrokerRequestDto
 */
@Getter
@Setter
public class PaymentBrokerRequestDto extends RequestDto {

    private static final long serialVersionUID = -2762374569445994063L;
    private PaymentBrokerName name;
    private CountryCode countryCode;
    private PaymentOptionType paymentOptionType;
    private CurrencyType currencyType;


    @Override
    public boolean isRequiredAvailable() {
        return name != null && paymentOptionType != null;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
