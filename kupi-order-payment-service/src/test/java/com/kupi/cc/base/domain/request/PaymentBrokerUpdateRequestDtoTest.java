package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.CountryCode;
import com.kupi.cc.base.enums.CurrencyType;
import com.kupi.cc.base.enums.PaymentBrokerName;
import com.kupi.cc.base.enums.PaymentOptionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link PaymentBrokerUpdateRequestDto} class
 */
class PaymentBrokerUpdateRequestDtoTest {

    private static final String PAYMENT_BROKER_UPDATE_ID = "PAYMENT_BROKER_UPDATE_ID";

    /**
     * Start of tests for required fields exists
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        PaymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto =
                getSamplePaymentBrokerUpdateRequestDto(PAYMENT_BROKER_UPDATE_ID);
        assertTrue(paymentBrokerUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_IdIsEmpty() {
        PaymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto = getSamplePaymentBrokerUpdateRequestDto("");
        assertFalse(paymentBrokerUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_IdIsNull() {
        PaymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto = getSamplePaymentBrokerUpdateRequestDto(null);
        assertFalse(paymentBrokerUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_PaymentBrokerNameIsNull() {
        PaymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto =
                getSamplePaymentBrokerUpdateRequestDto(PAYMENT_BROKER_UPDATE_ID);
        paymentBrokerUpdateRequestDto.setName(null);
        assertFalse(paymentBrokerUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_PaymentOptionTypeIsNull() {
        PaymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto =
                getSamplePaymentBrokerUpdateRequestDto(PAYMENT_BROKER_UPDATE_ID);
        paymentBrokerUpdateRequestDto.setPaymentOptionType(null);
        assertFalse(paymentBrokerUpdateRequestDto.isRequiredAvailable());
    }

    /**
     * This method returns a sample payment broker request dto
     *
     * @return PaymentBrokerRequestDto
     */
    private PaymentBrokerUpdateRequestDto getSamplePaymentBrokerUpdateRequestDto(String id) {
        PaymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto = new PaymentBrokerUpdateRequestDto();
        paymentBrokerUpdateRequestDto.setId(id);
        paymentBrokerUpdateRequestDto.setName(PaymentBrokerName.PAYPAL);
        paymentBrokerUpdateRequestDto.setCountryCode(CountryCode.ARE);
        paymentBrokerUpdateRequestDto.setPaymentOptionType(PaymentOptionType.EXTERNAL);
        paymentBrokerUpdateRequestDto.setCurrencyType(CurrencyType.USD);
        return paymentBrokerUpdateRequestDto;
    }
}