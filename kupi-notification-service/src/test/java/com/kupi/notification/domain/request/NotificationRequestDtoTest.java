package com.kupi.notification.domain.request;

import com.kupi.notification.enums.NotificationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link NotificationRequestDto} class
 */
class NotificationRequestDtoTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreProvided() {
        NotificationRequestDto notificationRequestDto = getSampleNotificationRequestDto();
        assertTrue(notificationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MessageBodyIsNull() {
        NotificationRequestDto notificationRequestDto = getSampleNotificationRequestDto();
        notificationRequestDto.setBody(null);
        assertFalse(notificationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MessageBodyIsEmpty() {
        NotificationRequestDto notificationRequestDto = getSampleNotificationRequestDto();
        notificationRequestDto.setBody("");
        assertFalse(notificationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MessageTitleIsNull() {
        NotificationRequestDto notificationRequestDto = getSampleNotificationRequestDto();
        notificationRequestDto.setTitle(null);
        assertFalse(notificationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MessageTitleIsEmpty() {
        NotificationRequestDto notificationRequestDto = getSampleNotificationRequestDto();
        notificationRequestDto.setTitle("");
        assertFalse(notificationRequestDto.isRequiredAvailable());
    }

    /**
     * This method creates sample NotificationRequestDto
     *
     * @return NotificationRequestDto
     */
    private NotificationRequestDto getSampleNotificationRequestDto() {
        NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
        notificationRequestDto.setBody("Message body");
        notificationRequestDto.setReferralId("Referral Id");
        notificationRequestDto.setTitle("Title");
        notificationRequestDto.setType(NotificationType.MERCHANT);
        return notificationRequestDto;
    }
}