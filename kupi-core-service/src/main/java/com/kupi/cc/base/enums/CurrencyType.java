package com.kupi.cc.base.enums;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Currency Type
 */
@Getter
public enum CurrencyType {

    AUD("Australian Dollar", "AU$"),
    LKR("Sri Lankan Rupee", "₨"),
    USD("US dollar", "$");

    private final String currency;
    private final String sign;

    CurrencyType(String currency, String sign) {
        this.currency = currency;
        this.sign = sign;
    }

    /***
     * This method trims decimal value of subscriptionAmount
     *
     * @param subscriptionAmount subscriptionAmount
     * @return subscriptionAmount
     */
    public BigDecimal getSubscriptionAmount(BigDecimal subscriptionAmount) {
        BigDecimal integerValue = BigDecimal.valueOf(subscriptionAmount.intValue());
        return subscriptionAmount.subtract(integerValue).compareTo(BigDecimal.valueOf(0)) == 0 ?
                integerValue : subscriptionAmount;
    }
}
