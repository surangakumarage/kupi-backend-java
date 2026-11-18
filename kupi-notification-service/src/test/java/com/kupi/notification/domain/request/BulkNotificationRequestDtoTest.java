package com.kupi.notification.domain.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link BulkNotificationRequestDto} class.
 */
class BulkNotificationRequestDtoTest {

    private static final String USER_NOTIFICATION_ID = "unid-12568-78544-7954";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreProvided() {
        BulkNotificationRequestDto bulkNotificationRequestDto = getSampleBulkNotificationRequestDto();
        assertTrue(bulkNotificationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NotificationIdsAreNull() {
        BulkNotificationRequestDto bulkNotificationRequestDto = getSampleBulkNotificationRequestDto();
        bulkNotificationRequestDto.setNotificationIds(null);
        assertFalse(bulkNotificationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NotificationIdsAreEmpty() {
        BulkNotificationRequestDto bulkNotificationRequestDto = getSampleBulkNotificationRequestDto();
        List<String> notificationIds = new ArrayList<>();
        bulkNotificationRequestDto.setNotificationIds(notificationIds);
        assertFalse(bulkNotificationRequestDto.isRequiredAvailable());
    }

    /**
     * This method creates sample BulkNotificationRequestDto
     *
     * @return BulkNotificationRequestDto
     */
    private BulkNotificationRequestDto getSampleBulkNotificationRequestDto() {
        BulkNotificationRequestDto bulkNotificationRequestDto = new BulkNotificationRequestDto();
        List<String> notificationIds = new ArrayList<>();
        notificationIds.add(USER_NOTIFICATION_ID);
        bulkNotificationRequestDto.setNotificationIds(notificationIds);
        return bulkNotificationRequestDto;
    }
}