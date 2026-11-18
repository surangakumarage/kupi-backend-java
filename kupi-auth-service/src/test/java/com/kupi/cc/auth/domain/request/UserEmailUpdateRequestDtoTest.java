package com.kupi.cc.auth.domain.request;

import com.kupi.cc.auth.util.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link UserEmailUpdateRequestDto} class
 */
class UserEmailUpdateRequestDtoTest {

    private static final String USER_ID = "uid-sfa584f5s";
    private static final String EMAIL = "abc@hotmail.com";

    @Mock
    private Validator validator;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    /**
     * Required fields availability check
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        UserEmailUpdateRequestDto userEmailUpdateRequestDto = new UserEmailUpdateRequestDto();
        userEmailUpdateRequestDto.setUserId(USER_ID);
        assertTrue(userEmailUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_RequiredFieldsNotExists() {
        UserEmailUpdateRequestDto userEmailUpdateRequestDto = new UserEmailUpdateRequestDto();
        assertFalse(userEmailUpdateRequestDto.isRequiredAvailable());
    }

    /**
     * Email validity check
     */
    @Test
    void Should_ReturnTrue_When_EmailIsValid() {
        UserEmailUpdateRequestDto userEmailUpdateRequestDto = new UserEmailUpdateRequestDto();
        userEmailUpdateRequestDto.setEmail(EMAIL);
        when(validator.isValidEmail(EMAIL)).thenReturn(true);
        assertTrue(userEmailUpdateRequestDto.validateEmail(validator));
    }

    @Test
    void Should_ReturnFalse_When_EmailIsInvalid() {
        UserEmailUpdateRequestDto userEmailUpdateRequestDto = new UserEmailUpdateRequestDto();
        userEmailUpdateRequestDto.setEmail(EMAIL);
        when(validator.isValidEmail(EMAIL)).thenReturn(false);
        assertFalse(userEmailUpdateRequestDto.validateEmail(validator));
    }
}