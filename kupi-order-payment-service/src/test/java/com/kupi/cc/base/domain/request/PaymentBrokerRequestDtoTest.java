package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.CountryCode;
import com.kupi.cc.base.enums.CurrencyType;
import com.kupi.cc.base.enums.PaymentBrokerName;
import com.kupi.cc.base.enums.PaymentOptionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link PaymentBrokerRequestDto} class
 */
class PaymentBrokerRequestDtoTest {

    /**
     * Start of tests for required fields exists
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        PaymentBrokerRequestDto paymentBrokerRequestDto = getSamplePaymentBrokerRequestDto();
        assertTrue(paymentBrokerRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_PaymentBrokerNameIsNull() {
        PaymentBrokerRequestDto paymentBrokerRequestDto = getSamplePaymentBrokerRequestDto();
        paymentBrokerRequestDto.setName(null);
        assertFalse(paymentBrokerRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_PaymentOptionTypeIsNull() {
        PaymentBrokerRequestDto paymentBrokerRequestDto = getSamplePaymentBrokerRequestDto();
        paymentBrokerRequestDto.setPaymentOptionType(null);
        assertFalse(paymentBrokerRequestDto.isRequiredAvailable());
    }

    /**
     * This method returns a sample payment broker request dto
     *
     * @return PaymentBrokerRequestDto
     */
    private PaymentBrokerRequestDto getSamplePaymentBrokerRequestDto() {
        PaymentBrokerRequestDto paymentBrokerRequestDto = new PaymentBrokerRequestDto();
        paymentBrokerRequestDto.setName(PaymentBrokerName.PAYPAL);
        paymentBrokerRequestDto.setCountryCode(CountryCode.ARE);
        paymentBrokerRequestDto.setPaymentOptionType(PaymentOptionType.EXTERNAL);
        paymentBrokerRequestDto.setCurrencyType(CurrencyType.USD);
        return paymentBrokerRequestDto;
    }
}