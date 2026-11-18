package com.kupi.cc.base.domain.request;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link DealsOfTheDayEventRequestDto} class
 */
class DealsOfTheDayEventRequestDtoTest {

    private static final String DEAL_ID = "did-123456789";
    private static final String EVENT_ID = "dodeid-123456789";
    private static final long DATE_IN_MILLISECONDS = 1659465000000L;

    /**
     * Start of tests for is required fields exists
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto(
                DATE_IN_MILLISECONDS, EVENT_ID, getSampleDealIdsSet());
        assertTrue(dealsOfTheDayEventRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_RequiredFieldsNotExists() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto(
                DATE_IN_MILLISECONDS, EVENT_ID, new HashSet<>());
        assertFalse(dealsOfTheDayEventRequestDto.isRequiredAvailable());
    }

    /**
     * Start of tests for is valid on
     */
    @Test
    void Should_ReturnTrue_When_DealsOfTheDayEventRequestDtoIsValid() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto(
                System.currentTimeMillis() + DATE_IN_MILLISECONDS, EVENT_ID, getSampleDealIdsSet());
        assertTrue(dealsOfTheDayEventRequestDto.isValidValidOn("Asia/Colombo"));
    }

    @Test
    void Should_ReturnFalse_When_DealsOfTheDayEventRequestDtoIsInvalid() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto(
                DATE_IN_MILLISECONDS, EVENT_ID, getSampleDealIdsSet());
        assertFalse(dealsOfTheDayEventRequestDto.isValidValidOn("Asia/Colombo"));
    }

    /**
     * Start of tests for is required available for update
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExistsForUpdate() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto(
                DATE_IN_MILLISECONDS, EVENT_ID, getSampleDealIdsSet());
        assertTrue(dealsOfTheDayEventRequestDto.isRequiredAvailableForUpdate());
    }

    @Test
    void Should_ReturnFalse_When_RequiredFieldsNotExistsForUpdate() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto(
                DATE_IN_MILLISECONDS, null, getSampleDealIdsSet());
        assertFalse(dealsOfTheDayEventRequestDto.isRequiredAvailableForUpdate());
    }

    private Set<String> getSampleDealIdsSet() {
        Set<String> dealIdsSet = new HashSet<>();
        dealIdsSet.add(DEAL_ID);
        return dealIdsSet;
    }

    private DealsOfTheDayEventRequestDto getSampleDealsOfTheDayEventRequestDto(long validOn, String eventId,
                                                                               Set<String> dealIds) {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = new DealsOfTheDayEventRequestDto();
        dealsOfTheDayEventRequestDto.setValidOn(validOn);
        dealsOfTheDayEventRequestDto.setEventId(eventId);
        dealsOfTheDayEventRequestDto.setDealIds(dealIds);
        return dealsOfTheDayEventRequestDto;
    }
}