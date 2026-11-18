package com.kupi.cc.auth.domain.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link UserUpdateRequestDto} class
 */
class UserUpdateRequestDtoTest {

    private static final String USER_ID = "uid-1234567890";

    /**
     * Required fields availability check
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        UserUpdateRequestDto userUpdateRequestDto = new UserUpdateRequestDto();
        userUpdateRequestDto.setUserId(USER_ID);
        assertTrue(userUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdIsEmpty() {
        UserUpdateRequestDto userUpdateRequestDto = new UserUpdateRequestDto();
        userUpdateRequestDto.setUserId("");
        assertFalse(userUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdIsNull() {
        UserUpdateRequestDto userUpdateRequestDto = new UserUpdateRequestDto();
        userUpdateRequestDto.setUserId(null);
        assertFalse(userUpdateRequestDto.isRequiredAvailable());
    }
}