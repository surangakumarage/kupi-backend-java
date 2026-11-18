package com.kupi.cc.auth.domain.request;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link BulkUserRequestDto} class
 */
class BulkUserRequestDtoTest {

    private static final String USER_ID = "uid-sgfds6g5s2345";

    /**
     * Required fields availability check
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        List<String> userIds = new ArrayList<>();
        userIds.add(USER_ID);
        BulkUserRequestDto bulkUserRequestDto = new BulkUserRequestDto();
        bulkUserRequestDto.setUserIds(userIds);
        assertTrue(bulkUserRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdsListIsEmpty() {
        List<String> userIds = new ArrayList<>();
        BulkUserRequestDto bulkUserRequestDto = new BulkUserRequestDto();
        bulkUserRequestDto.setUserIds(userIds);
        assertFalse(bulkUserRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdsListIsNull() {
        BulkUserRequestDto bulkUserRequestDto = new BulkUserRequestDto();
        bulkUserRequestDto.setUserIds(null);
        assertFalse(bulkUserRequestDto.isRequiredAvailable());
    }
}