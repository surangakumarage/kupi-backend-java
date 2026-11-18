package com.kupi.util.domain.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link MobileNoRequestDto} class
 */
class MobileNoRequestDtoTest {

    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "771109101";
    private static final String MOBILE_NUMBER = "+94771109101";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_ValidCountryCodeAndLocalNumbersAreProvided() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        assertTrue(mobileNoRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CountryCodeIsNull() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        mobileNoRequestDto.setCountryCode(null);
        assertFalse(mobileNoRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CountryCodeIsEmpty() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        mobileNoRequestDto.setCountryCode("");
        assertFalse(mobileNoRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocalNumberIsNull() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        mobileNoRequestDto.setLocalNumber(null);
        assertFalse(mobileNoRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocalNumberIsEmpty() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        mobileNoRequestDto.setLocalNumber("");
        assertFalse(mobileNoRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnMobileNumber_When_CountryCodeAndLocalNumberAreNotEmptyAndNotNull() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        assertEquals(MOBILE_NUMBER, mobileNoRequestDto.getNo());
    }

    @Test
    void Should_ReturnNull_When_CountryCodeIsNull() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        mobileNoRequestDto.setCountryCode(null);
        assertNull(mobileNoRequestDto.getNo());
    }

    @Test
    void Should_ReturnNull_When_CountryCodeIsEmpty() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        mobileNoRequestDto.setCountryCode("");
        assertNull(mobileNoRequestDto.getNo());
    }

    @Test
    void Should_ReturnNull_When_LocalNumberIsNull() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        mobileNoRequestDto.setLocalNumber(null);
        assertNull(mobileNoRequestDto.getNo());
    }

    @Test
    void Should_ReturnNull_When_LocalNumberIsEmpty() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        mobileNoRequestDto.setLocalNumber("");
        assertNull(mobileNoRequestDto.getNo());
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
}