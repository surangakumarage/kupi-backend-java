package com.kupi.cc.auth.domain.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * This class tests the {@link MobileNoRequestDto} class
 */
class MobileNoRequestDtoTest {

    private static final String LOCAL_MOBILE_NO = "0713321911";
    private static final String MOBILE_NO_WITH_COUNTRY_CODE = "+94-0713321911";

    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "774552663";

    @Test
    void Should_ReturnMobileNo_When_MobileNoIsProvidedWithCountryCode() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto("+94", LOCAL_MOBILE_NO);
        assertEquals(MOBILE_NO_WITH_COUNTRY_CODE, mobileNoRequestDto.getNo());
    }

    /**
     * Start of tests for is required fields exists
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto(COUNTRY_CODE,
                LOCAL_NUMBER);
        assertTrue(mobileNoRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CountryCodeIsEmpty() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto("",
                LOCAL_NUMBER);
        assertFalse(mobileNoRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CountryCodeIsNull() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto(null,
                LOCAL_NUMBER);
        assertFalse(mobileNoRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocalNumberIsEmpty() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto(COUNTRY_CODE,
                "");
        assertFalse(mobileNoRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocalNumberIsNull() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto(COUNTRY_CODE,
                null);
        assertFalse(mobileNoRequestDto.isRequiredAvailable());
    }
}