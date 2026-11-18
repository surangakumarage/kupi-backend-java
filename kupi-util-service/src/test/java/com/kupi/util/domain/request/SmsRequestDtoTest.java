package com.kupi.util.domain.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link SmsRequestDto} class
 */
class SmsRequestDtoTest {

    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "771109101";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreProvided() {
        SmsRequestDto smsRequestDto = getSampleSmsRequestDto();
        assertTrue(smsRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MessageIsNull() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        SmsRequestDto smsRequestDto = new SmsRequestDto(mobileNoRequestDto, null);
        assertFalse(smsRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MessageIsEmpty() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        SmsRequestDto smsRequestDto = new SmsRequestDto(mobileNoRequestDto, "");
        assertFalse(smsRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CountryCodeIsNull() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        mobileNoRequestDto.setCountryCode(null);
        SmsRequestDto smsRequestDto = new SmsRequestDto(mobileNoRequestDto, "Sms Message");
        assertFalse(smsRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CountryCodeIsEmpty() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        mobileNoRequestDto.setCountryCode("");
        SmsRequestDto smsRequestDto = new SmsRequestDto(mobileNoRequestDto, "Sms Message");
        assertFalse(smsRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocalNumberIsNull() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        mobileNoRequestDto.setLocalNumber(null);
        SmsRequestDto smsRequestDto = new SmsRequestDto(mobileNoRequestDto, "Sms Message");
        assertFalse(smsRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocalNumberIsEmpty() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        mobileNoRequestDto.setLocalNumber("");
        SmsRequestDto smsRequestDto = new SmsRequestDto(mobileNoRequestDto, "Sms Message");
        assertFalse(smsRequestDto.isRequiredAvailable());
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
     * This method creates sample SmsRequestDto
     *
     * @return SmsRequestDto
     */
    private SmsRequestDto getSampleSmsRequestDto() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        return new SmsRequestDto(mobileNoRequestDto, "Sms Message");
    }
}