package com.kupi.cc.base.domain.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link CreditCardRequestUpdateRequestDto} class
 */
class CreditCardRequestUpdateRequestDtoTest {

    private static final String CC_REQUEST_ID = "ccrid-123456789";
    private static final String USER_ID = "uid-123456789";
    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "123456789";
    private static final String EMAIL = "abc@yahoomail.com";
    private static final String NIC = "123456789v";
    private static final String BANK_ID = "uid-123456789";
    private static final String FULL_NAME = "karim Benzema";
    private static final String CITY = "Paris";

    /**
     * Required fields existence check
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto(
                CC_REQUEST_ID, BANK_ID, USER_ID, FULL_NAME, NIC, CITY, EMAIL, LOCAL_NUMBER);
        assertTrue(creditCardRequestUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_IdIsNull() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto(
                null, BANK_ID, USER_ID, FULL_NAME, NIC, CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_IdIsEmpty() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto(
                "", BANK_ID, USER_ID, FULL_NAME, NIC, CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdIsNull() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto(
                CC_REQUEST_ID, BANK_ID, null, FULL_NAME, NIC, CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdIsEmpty() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto(
                CC_REQUEST_ID, BANK_ID, "", FULL_NAME, NIC, CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocalNumberIsNull() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto(
                CC_REQUEST_ID, BANK_ID, USER_ID, FULL_NAME, NIC, CITY, EMAIL, null);
        assertFalse(creditCardRequestUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocalNumberIsEmpty() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto(
                CC_REQUEST_ID, BANK_ID, USER_ID, FULL_NAME, NIC, CITY, EMAIL, "");
        assertFalse(creditCardRequestUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_EmailIsNull() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto(
                CC_REQUEST_ID, BANK_ID, USER_ID, FULL_NAME, NIC, CITY, null, LOCAL_NUMBER);
        assertFalse(creditCardRequestUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_EmailIsEmpty() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto(
                CC_REQUEST_ID, BANK_ID, USER_ID, FULL_NAME, NIC, CITY, "", LOCAL_NUMBER);
        assertFalse(creditCardRequestUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NICIsNull() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto(
                CC_REQUEST_ID, BANK_ID, USER_ID, FULL_NAME, null, CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NICIsEmpty() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto(
                CC_REQUEST_ID, BANK_ID, USER_ID, FULL_NAME, "", CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_BankIdIsNull() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto(
                CC_REQUEST_ID, null, USER_ID, FULL_NAME, NIC, CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_BankIdIsEmpty() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto(
                CC_REQUEST_ID, "", USER_ID, FULL_NAME, NIC, CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_FullNameIsNull() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto(
                CC_REQUEST_ID, BANK_ID, USER_ID, null, NIC, CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_FullNameIsEmpty() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto(
                CC_REQUEST_ID, BANK_ID, USER_ID, "", NIC, CITY, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CityIsNull() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto(
                CC_REQUEST_ID, BANK_ID, USER_ID, FULL_NAME, NIC, null, EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CityIsEmpty() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto(
                CC_REQUEST_ID, BANK_ID, USER_ID, FULL_NAME, NIC, "", EMAIL, LOCAL_NUMBER);
        assertFalse(creditCardRequestUpdateRequestDto.isRequiredAvailable());
    }

    private CreditCardRequestUpdateRequestDto getSampleCreditCardRequestUpdateRequestDto(String id, String bankId,
                                                                                         String userId, String fullName,
                                                                                         String nic, String city,
                                                                                         String email, String localNumber) {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = new CreditCardRequestUpdateRequestDto();
        creditCardRequestUpdateRequestDto.setId(id);
        creditCardRequestUpdateRequestDto.setBankId(bankId);
        creditCardRequestUpdateRequestDto.setUserId(userId);
        creditCardRequestUpdateRequestDto.setFullName(fullName);
        creditCardRequestUpdateRequestDto.setNic(nic);
        creditCardRequestUpdateRequestDto.setCity(city);
        creditCardRequestUpdateRequestDto.setEmail(email);
        creditCardRequestUpdateRequestDto.setMobileNumber(new MobileNoRequestDto(COUNTRY_CODE, localNumber));
        return creditCardRequestUpdateRequestDto;
    }
}