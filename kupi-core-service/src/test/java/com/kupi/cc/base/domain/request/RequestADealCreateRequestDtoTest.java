package com.kupi.cc.base.domain.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests for {@link RequestADealCreateRequestDto} class
 */
class RequestADealCreateRequestDtoTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreProvided() {
        RequestADealCreateRequestDto requestADealCreateRequestDto = getSampleRequestADealCreateRequestDto();
        assertTrue(requestADealCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MerchantIdIsNull() {
        RequestADealCreateRequestDto requestADealCreateRequestDto = getSampleRequestADealCreateRequestDto();
        requestADealCreateRequestDto.setMerchantId(null);
        assertFalse(requestADealCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MerchantIdIsEmpty() {
        RequestADealCreateRequestDto requestADealCreateRequestDto = getSampleRequestADealCreateRequestDto();
        requestADealCreateRequestDto.setMerchantId("");
        assertFalse(requestADealCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CategoryIdIsNull() {
        RequestADealCreateRequestDto requestADealCreateRequestDto = getSampleRequestADealCreateRequestDto();
        requestADealCreateRequestDto.setCategoryId(null);
        assertFalse(requestADealCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CategoryIdIsEmpty() {
        RequestADealCreateRequestDto requestADealCreateRequestDto = getSampleRequestADealCreateRequestDto();
        requestADealCreateRequestDto.setCategoryId("");
        assertFalse(requestADealCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NoteIsNull() {
        RequestADealCreateRequestDto requestADealCreateRequestDto = getSampleRequestADealCreateRequestDto();
        requestADealCreateRequestDto.setNote(null);
        assertFalse(requestADealCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NoteIsEmpty() {
        RequestADealCreateRequestDto requestADealCreateRequestDto = getSampleRequestADealCreateRequestDto();
        requestADealCreateRequestDto.setNote("");
        assertFalse(requestADealCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdIsNull() {
        RequestADealCreateRequestDto requestADealCreateRequestDto = getSampleRequestADealCreateRequestDto();
        requestADealCreateRequestDto.setUserId(null);
        assertFalse(requestADealCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdIsEmpty() {
        RequestADealCreateRequestDto requestADealCreateRequestDto = getSampleRequestADealCreateRequestDto();
        requestADealCreateRequestDto.setUserId("");
        assertFalse(requestADealCreateRequestDto.isRequiredAvailable());
    }

    /**
     * This method creates sample RequestADealCreateRequestDto
     *
     * @return RequestADealCreateRequestDto
     */
    private RequestADealCreateRequestDto getSampleRequestADealCreateRequestDto() {
        RequestADealCreateRequestDto requestADealCreateRequestDto = new RequestADealCreateRequestDto();
        requestADealCreateRequestDto.setBrandId("bid-3bb83176-1361-44cb-a33d-9aba63266017");
        requestADealCreateRequestDto.setCategoryId("cid-f061574f-ce13-496f-bc21-01b9c7a40e24");
        requestADealCreateRequestDto.setMerchantId("uid-92b0983c-534e-401f-af15-5f8bcbcddebb");
        requestADealCreateRequestDto.setUserId("uid-ded6ca72-f64f-46c7-a36a-05893ca27c62");
        requestADealCreateRequestDto.setNote("Notes");
        requestADealCreateRequestDto.setOfferTypeId("otid-20503137-25a6-4e72-950f-4c8bad24b530");
        return requestADealCreateRequestDto;
    }
}