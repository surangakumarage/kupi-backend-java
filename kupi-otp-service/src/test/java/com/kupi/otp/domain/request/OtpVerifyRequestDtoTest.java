package com.kupi.otp.domain.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link OtpVerifyRequestDto} class
 */
class OtpVerifyRequestDtoTest {

    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "771109101";
    private static final String EMAIL_ADDRESS = "example@kupi.lk";
    private static final String OTP = "145826";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_MobileNoAndOtpNotNull() {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        assertTrue(otpVerifyRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnTrue_When_MobileNoIsNullButEmailAndOtpAreNotNull() {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setMobileNo(null);
        assertTrue(otpVerifyRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MobileNoIsNotNullAndOtpIsNull() {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setOtp(null);
        assertFalse(otpVerifyRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MobileNoIsNotNullAndOtpIsEmpty() {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setOtp("");
        assertFalse(otpVerifyRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MobileNoAndEmailAndOtpAreNull() {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setOtp(null);
        otpVerifyRequestDto.setMobileNo(null);
        otpVerifyRequestDto.setEmail(null);
        assertFalse(otpVerifyRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_EmailNotNullButMobileNoAndOtpAreNull() {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setOtp(null);
        otpVerifyRequestDto.setMobileNo(null);
        assertFalse(otpVerifyRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_OtpNotNullButMobileNoAndEmailAreNull() {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setEmail(null);
        otpVerifyRequestDto.setMobileNo(null);
        assertFalse(otpVerifyRequestDto.isRequiredAvailable());
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
     * This method creates sample OtpVerifyRequestDto
     *
     * @return OtpVerifyRequestDto
     */
    private OtpVerifyRequestDto getSampleOtpVerifyRequestDto() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        OtpVerifyRequestDto otpVerifyRequestDto = new OtpVerifyRequestDto();
        otpVerifyRequestDto.setOtp(OTP);
        otpVerifyRequestDto.setEmail(EMAIL_ADDRESS);
        otpVerifyRequestDto.setMobileNo(mobileNoRequestDto);
        return otpVerifyRequestDto;
    }
}