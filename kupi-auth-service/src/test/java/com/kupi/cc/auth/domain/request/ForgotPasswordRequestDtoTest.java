package com.kupi.cc.auth.domain.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link ForgotPasswordRequestDto} class
 */
class ForgotPasswordRequestDtoTest {

    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "752643597";

    /**
     * Required fields availability check
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        MobileNoRequestDto mobileNo = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        ForgotPasswordRequestDto forgotPasswordRequestDto = new ForgotPasswordRequestDto();
        forgotPasswordRequestDto.setMobileNo(mobileNo);
        assertTrue(forgotPasswordRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MobileNoIsNull() {
        ForgotPasswordRequestDto forgotPasswordRequestDto = new ForgotPasswordRequestDto();
        forgotPasswordRequestDto.setMobileNo(null);
        assertFalse(forgotPasswordRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CountryCodeIsNull() {
        MobileNoRequestDto mobileNo = new MobileNoRequestDto(null, LOCAL_NUMBER);
        ForgotPasswordRequestDto forgotPasswordRequestDto = new ForgotPasswordRequestDto();
        forgotPasswordRequestDto.setMobileNo(mobileNo);
        assertFalse(forgotPasswordRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CountryCodeIsEmpty() {
        MobileNoRequestDto mobileNo = new MobileNoRequestDto("", LOCAL_NUMBER);
        ForgotPasswordRequestDto forgotPasswordRequestDto = new ForgotPasswordRequestDto();
        forgotPasswordRequestDto.setMobileNo(mobileNo);
        assertFalse(forgotPasswordRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocalNumberIsNull() {
        MobileNoRequestDto mobileNo = new MobileNoRequestDto(COUNTRY_CODE, null);
        ForgotPasswordRequestDto forgotPasswordRequestDto = new ForgotPasswordRequestDto();
        forgotPasswordRequestDto.setMobileNo(mobileNo);
        assertFalse(forgotPasswordRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocalNumberIsEmpty() {
        MobileNoRequestDto mobileNo = new MobileNoRequestDto(COUNTRY_CODE, "");
        ForgotPasswordRequestDto forgotPasswordRequestDto = new ForgotPasswordRequestDto();
        forgotPasswordRequestDto.setMobileNo(mobileNo);
        assertFalse(forgotPasswordRequestDto.isRequiredAvailable());
    }
}