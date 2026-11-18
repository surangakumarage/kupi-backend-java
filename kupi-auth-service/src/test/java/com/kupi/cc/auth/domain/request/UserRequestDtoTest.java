package com.kupi.cc.auth.domain.request;

import com.kupi.cc.auth.enums.RoleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the {@link UserRequestDto} class
 */
class UserRequestDtoTest {

    private static final String FULL_NAME = "Mohamed Nawaz";
    private static final String EMAIL = "mgmnawas@gmail.com";
    private static final String PASSWORD = "password";
    private static final MobileNoRequestDto MOBILE_NO = getSampleMobileNoRequestDto();
    private static final String LANGUAGE = "English";
    private static final RoleType ROLE_TYPE = RoleType.USER;

    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "774552663";

    /**
     * Start of tests for is required fields exists
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        UserRequestDto userRequestDto = getSampleUserRequestDto();
        assertTrue(userRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_RoleTypeIsAdmin() {
        UserRequestDto userRequestDto = getSampleUserRequestDto();
        userRequestDto.setRoleType(RoleType.ADMIN);
        assertFalse(userRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MobileNoIsNull() {
        UserRequestDto userRequestDto = getSampleUserRequestDto();
        userRequestDto.setMobileNo(null);
        assertFalse(userRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_FullNameIsEmpty() {
        UserRequestDto userRequestDto = getSampleUserRequestDto();
        userRequestDto.setFullName("");
        assertFalse(userRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_FullNameIsNull() {
        UserRequestDto userRequestDto = getSampleUserRequestDto();
        userRequestDto.setFullName(null);
        assertFalse(userRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_PasswordIsEmpty() {
        UserRequestDto userRequestDto = getSampleUserRequestDto();
        userRequestDto.setPassword("");
        assertFalse(userRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_PasswordIsNull() {
        UserRequestDto userRequestDto = getSampleUserRequestDto();
        userRequestDto.setPassword(null);
        assertFalse(userRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MobileNoIsRequiredNotAvailable() {
        UserRequestDto userRequestDto = getSampleUserRequestDto();
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        mobileNoRequestDto.setCountryCode("");
        userRequestDto.setMobileNo(mobileNoRequestDto);
        assertFalse(userRequestDto.isRequiredAvailable());
    }



    private static MobileNoRequestDto getSampleMobileNoRequestDto() {
        return new MobileNoRequestDto(COUNTRY_CODE,
                LOCAL_NUMBER);
    }

    private UserRequestDto getSampleUserRequestDto() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setFullName(FULL_NAME);
        userRequestDto.setEmail(EMAIL);
        userRequestDto.setPassword(PASSWORD);
        userRequestDto.setMobileNo(MOBILE_NO);
        userRequestDto.setLanguage(LANGUAGE);
        userRequestDto.setRoleType(ROLE_TYPE);
        return userRequestDto;
    }
}