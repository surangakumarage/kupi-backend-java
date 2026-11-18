package com.kupi.otp.domain.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link MailRequestDto} class
 */
class MailRequestDtoTest {

    private static final String RECIPIENT_ADDRESS = "recipient@kupi.lk";
    private static final String TITLE = "Mail title";
    private static final String CONTENT = "Mail body";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreProvided() {
        MailRequestDto mailRequestDto = new MailRequestDto(RECIPIENT_ADDRESS, TITLE, CONTENT);
        assertTrue(mailRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_RecipientAddressIsNull() {
        MailRequestDto mailRequestDto = new MailRequestDto(null, TITLE, CONTENT);
        assertFalse(mailRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_RecipientAddressIsEmpty() {
        MailRequestDto mailRequestDto = new MailRequestDto("", TITLE, CONTENT);
        assertFalse(mailRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_TitleIsNull() {
        MailRequestDto mailRequestDto = new MailRequestDto(RECIPIENT_ADDRESS, null, CONTENT);
        assertFalse(mailRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_TitleIsEmpty() {
        MailRequestDto mailRequestDto = new MailRequestDto(RECIPIENT_ADDRESS, "", CONTENT);
        assertFalse(mailRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ContentIsNull() {
        MailRequestDto mailRequestDto = new MailRequestDto(RECIPIENT_ADDRESS, TITLE, null);
        assertFalse(mailRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ContentIsEmpty() {
        MailRequestDto mailRequestDto = new MailRequestDto(RECIPIENT_ADDRESS, TITLE, "");
        assertFalse(mailRequestDto.isRequiredAvailable());
    }
}