package com.kupi.cc.auth.domain.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link UserPasswordUpdateRequestDto} class
 */
class UserPasswordUpdateRequestDtoTest {

    private static final String USER_ID = "uid-sfdf5415fd4s5";
    private static final String PASSWORD = "124sfd542";
    private static final String NEW_PASSWORD = "sfd20xc1541";

    /**
     * Required fields existence check
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        UserPasswordUpdateRequestDto userPasswordUpdateRequestDto = new UserPasswordUpdateRequestDto();
        userPasswordUpdateRequestDto.setUserId(USER_ID);
        userPasswordUpdateRequestDto.setPassword(PASSWORD);
        userPasswordUpdateRequestDto.setNewPassword(NEW_PASSWORD);
        assertTrue(userPasswordUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_RequiredFieldsNotExists() {
        UserPasswordUpdateRequestDto userPasswordUpdateRequestDto = new UserPasswordUpdateRequestDto();
        assertFalse(userPasswordUpdateRequestDto.isRequiredAvailable());
    }
}