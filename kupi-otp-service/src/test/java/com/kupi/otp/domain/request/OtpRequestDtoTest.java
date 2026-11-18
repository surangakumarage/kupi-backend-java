package com.kupi.otp.domain.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link OtpRequestDto} class
 */
class OtpRequestDtoTest {

    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "771109101";
    private static final String EMAIL_ADDRESS = "example@kupi.lk";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_MobileNoRequestDtoAndEmailAddressArePresent() {
        OtpRequestDto otpRequestDto = getSampleOtpRequestDto();
        assertTrue(otpRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnTrue_When_MobileNoRequestDtoOnlyPresent() {
        OtpRequestDto otpRequestDto = getSampleOtpRequestDto();
        otpRequestDto.setEmail(null);
        assertTrue(otpRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnTrue_When_MobileNoRequestDtoIsNullAndEmailAddressIsPresent() {
        OtpRequestDto otpRequestDto = getSampleOtpRequestDto();
        otpRequestDto.setMobileNo(null);
        assertTrue(otpRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_BothMobileNoRequestDtoAndEmailAreNull() {
        OtpRequestDto otpRequestDto = getSampleOtpRequestDto();
        otpRequestDto.setMobileNo(null);
        otpRequestDto.setEmail(null);
        assertFalse(otpRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_BothMobileNoRequestDtoIsNullAndEmailIsEmpty() {
        OtpRequestDto otpRequestDto = getSampleOtpRequestDto();
        otpRequestDto.setMobileNo(null);
        otpRequestDto.setEmail("");
        assertFalse(otpRequestDto.isRequiredAvailable());
    }

    /**
     * This method creates sample MobileNoRequestDto
     *
     * @return MobileNoRequestDto
     */
    private MobileNoRequestDto getSampleMobileNoRequestDto() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto();
        mobileNoRequestDto.setCountryCode(COUNTRY_CODE);
        mobileNoRequestDto.setLocalNumber(LOCAL_NUMBER);
        return mobileNoRequestDto;
    }

    /**
     * This method creates sample OtpRequestDto
     *
     * @return OtpRequestDto
     */
    private OtpRequestDto getSampleOtpRequestDto() {
        MobileNoRequestDto mobileNo = getSampleMobileNoRequestDto();
        OtpRequestDto otpRequestDto = new OtpRequestDto();
        otpRequestDto.setMobileNo(mobileNo);
        otpRequestDto.setEmail(EMAIL_ADDRESS);
        return otpRequestDto;
    }
}