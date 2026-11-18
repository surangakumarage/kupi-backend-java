package com.kupi.cc.auth.domain.request;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link ResetPasswordRequestDto} class
 */
class ResetPasswordRequestDtoTest {

    private static final String TOKEN = "E1S543A84X545A";
    private static final String PASSWORD = "2x54s44a545";

    /**
     * Required fields existence check
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        ResetPasswordRequestDto resetPasswordRequestDto = new ResetPasswordRequestDto();
        ReflectionTestUtils.setField(resetPasswordRequestDto, "token", TOKEN);
        ReflectionTestUtils.setField(resetPasswordRequestDto, "password", PASSWORD);
        assertTrue(resetPasswordRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_TokenIsEmpty() {
        ResetPasswordRequestDto resetPasswordRequestDto = new ResetPasswordRequestDto();
        ReflectionTestUtils.setField(resetPasswordRequestDto, "token", "");
        ReflectionTestUtils.setField(resetPasswordRequestDto, "password", PASSWORD);
        assertFalse(resetPasswordRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_TokenIsNull() {
        ResetPasswordRequestDto resetPasswordRequestDto = new ResetPasswordRequestDto();
        ReflectionTestUtils.setField(resetPasswordRequestDto, "token", null);
        ReflectionTestUtils.setField(resetPasswordRequestDto, "password", PASSWORD);
        assertFalse(resetPasswordRequestDto.isRequiredAvailable());
    }
}