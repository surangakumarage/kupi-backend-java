package com.kupi.cc.base.domain.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link CreditCardRequestCreateRequestDto} class
 */
class CreditCardRequestCreateRequestDtoTest {

    private static final String USER_ID = "uid-123456789";
    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "123456789";
    private static final String EMAIL = "abc@yahoomail.com";
    private static final String NIC = "123456789v";
    private static final String BANK_ID = "uid-123456789";
    private static final String FULL_NAME = "karim Benzema";
    private static final String CITY = "Paris";

    /**
     * Start of tests for is required fields exists
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = getSampleCreditCardRequestCreateRequestDto(
                BANK_ID, USER_ID, FULL_NAME, NIC, CITY, EMAIL, LOCAL_NUMBER);
        assertTrue(creditCardRequestCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdIsNull() {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = getSampleCreditCardRequestCreateRequestDto(
                BANK_ID, null, FULL_NAME, NIC, CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdIsEmpty() {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = getSampleCreditCardRequestCreateRequestDto(
                BANK_ID, "", FULL_NAME, NIC, CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocalNumberIsNull() {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = getSampleCreditCardRequestCreateRequestDto(
                BANK_ID, USER_ID, FULL_NAME, NIC, CITY, EMAIL, null);
        assertFalse(creditCardRequestCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocalNumberIsEmpty() {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = getSampleCreditCardRequestCreateRequestDto(
                BANK_ID, USER_ID, FULL_NAME, NIC, CITY, EMAIL, "");
        assertFalse(creditCardRequestCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_EmailIsNull() {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = getSampleCreditCardRequestCreateRequestDto(
                BANK_ID, USER_ID, FULL_NAME, NIC, CITY, null, LOCAL_NUMBER);
        assertFalse(creditCardRequestCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_EmailIsEmpty() {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = getSampleCreditCardRequestCreateRequestDto(
                BANK_ID, USER_ID, FULL_NAME, NIC, CITY, "", LOCAL_NUMBER);
        assertFalse(creditCardRequestCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NICIsNull() {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = getSampleCreditCardRequestCreateRequestDto(
                BANK_ID, USER_ID, FULL_NAME, null, CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NICIsEmpty() {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = getSampleCreditCardRequestCreateRequestDto(
                BANK_ID, USER_ID, FULL_NAME, "", CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_BankIdIsNull() {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = getSampleCreditCardRequestCreateRequestDto(
                null, USER_ID, FULL_NAME, NIC, CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_BankIdIsEmpty() {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = getSampleCreditCardRequestCreateRequestDto(
                "", USER_ID, FULL_NAME, NIC, CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_FullNameIsNull() {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = getSampleCreditCardRequestCreateRequestDto(
                BANK_ID, USER_ID, null, NIC, CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_FullNameIsEmpty() {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = getSampleCreditCardRequestCreateRequestDto(
                BANK_ID, USER_ID, "", NIC, CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CityIsNull() {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = getSampleCreditCardRequestCreateRequestDto(
                BANK_ID, USER_ID, FULL_NAME, NIC, null, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CityIsEmpty() {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = getSampleCreditCardRequestCreateRequestDto(
                BANK_ID, USER_ID, FULL_NAME, NIC, "", EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestCreateRequestDto.isRequiredAvailable());
    }

    private CreditCardRequestCreateRequestDto getSampleCreditCardRequestCreateRequestDto(String bankId, String userId,
                                                                                         String fullName, String nic,
                                                                                         String city, String email,
                                                                                         String localNumber) {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = new CreditCardRequestCreateRequestDto();
        creditCardRequestCreateRequestDto.setBankId(bankId);
        creditCardRequestCreateRequestDto.setUserId(userId);
        creditCardRequestCreateRequestDto.setFullName(fullName);
        creditCardRequestCreateRequestDto.setNic(nic);
        creditCardRequestCreateRequestDto.setCity(city);
        creditCardRequestCreateRequestDto.setEmail(email);
        creditCardRequestCreateRequestDto.setMobileNumber(new MobileNoRequestDto(COUNTRY_CODE, localNumber));
        return creditCardRequestCreateRequestDto;
    }
}