package com.kupi.notification.domain.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link AddTokenRequestDto} class
 */
class AddTokenRequestDtoTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreProvided() {
        AddTokenRequestDto addTokenRequestDto = getSampleAddTokenRequestDto();
        assertTrue(addTokenRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdIsNull() {
        AddTokenRequestDto addTokenRequestDto = getSampleAddTokenRequestDto();
        addTokenRequestDto.setUserId(null);
        assertFalse(addTokenRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdIsEmpty() {
        AddTokenRequestDto addTokenRequestDto = getSampleAddTokenRequestDto();
        addTokenRequestDto.setUserId("");
        assertFalse(addTokenRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_DeviceIdIsNull() {
        AddTokenRequestDto addTokenRequestDto = getSampleAddTokenRequestDto();
        addTokenRequestDto.setDeviceId(null);
        assertFalse(addTokenRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_DeviceIdIsEmpty() {
        AddTokenRequestDto addTokenRequestDto = getSampleAddTokenRequestDto();
        addTokenRequestDto.setDeviceId(null);
        assertFalse(addTokenRequestDto.isRequiredAvailable());
    }

    /**
     * This method creates sample AddTokenRequestDto
     *
     * @return AddTokenRequestDto
     */
    private AddTokenRequestDto getSampleAddTokenRequestDto() {
        AddTokenRequestDto addTokenRequestDto = new AddTokenRequestDto();
        addTokenRequestDto.setUserId("uid-1256-7854-8569-785");
        addTokenRequestDto.setDeviceId("DEVICE-ID");
        return addTokenRequestDto;
    }
}