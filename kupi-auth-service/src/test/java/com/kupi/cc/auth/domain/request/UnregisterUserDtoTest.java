package com.kupi.cc.auth.domain.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link UnregisterUserDto} class
 */
class UnregisterUserDtoTest {

    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "775263881";

    private final MobileNoRequestDto mobileNo = getSampleMobileNoRequestDto();

    /**
     * Required fields availability check
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        UnregisterUserDto unregisterUserDto = new UnregisterUserDto();
        unregisterUserDto.setMobileNo(mobileNo);
        assertTrue(unregisterUserDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MobileNoRequiredNotAvailable() {
        mobileNo.setCountryCode("");
        UnregisterUserDto unregisterUserDto = new UnregisterUserDto();
        unregisterUserDto.setMobileNo(mobileNo);
        assertFalse(unregisterUserDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_RequiredFieldsNotExists() {
        UnregisterUserDto unregisterUserDto = new UnregisterUserDto();
        assertFalse(unregisterUserDto.isRequiredAvailable());
    }


    private MobileNoRequestDto getSampleMobileNoRequestDto() {
        return new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
    }
}