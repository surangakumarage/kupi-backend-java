package com.kupi.cc.auth.domain.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link UserMobileUpdateRequestDto} class
 */
class UserMobileUpdateRequestDtoTest {

    private static final String USER_ID = "uid-sfds54sf4vc5s48";
    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "753648972";

    /**
     * Required fields availability check
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        MobileNoRequestDto mobileNo = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        UserMobileUpdateRequestDto userMobileUpdateRequestDto = new UserMobileUpdateRequestDto();
        userMobileUpdateRequestDto.setUserId(USER_ID);
        userMobileUpdateRequestDto.setMobileNo(mobileNo);
        assertTrue(userMobileUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_RequiredFieldsNotExists() {
        UserMobileUpdateRequestDto userMobileUpdateRequestDto = new UserMobileUpdateRequestDto();
        userMobileUpdateRequestDto.setUserId(USER_ID);
        assertFalse(userMobileUpdateRequestDto.isRequiredAvailable());
    }
}