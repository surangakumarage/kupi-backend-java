package com.kupi.cc.base.domain.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests for {@link OfferTypeUpdateRequestDto} class
 */
class OfferTypeUpdateRequestDtoTest {

    private static final String OFFER_TYPE_ID = "otid=1256-45875-8547-7852";
    private static final String OFFER_TYPE_NAME = "OFFER_TYPE";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreProvided() {
        OfferTypeUpdateRequestDto offerTypeUpdateRequestDto = getSampleOfferTypeUpdateRequestDto();
        assertTrue(offerTypeUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_OfferTypeIdIsNull() {
        OfferTypeUpdateRequestDto offerTypeUpdateRequestDto = getSampleOfferTypeUpdateRequestDto();
        offerTypeUpdateRequestDto.setId(null);
        assertFalse(offerTypeUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_OfferTypeIdIsEmpty() {
        OfferTypeUpdateRequestDto offerTypeUpdateRequestDto = getSampleOfferTypeUpdateRequestDto();
        offerTypeUpdateRequestDto.setId("");
        assertFalse(offerTypeUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_OfferTypeNameIsNull() {
        OfferTypeUpdateRequestDto offerTypeUpdateRequestDto = getSampleOfferTypeUpdateRequestDto();
        offerTypeUpdateRequestDto.setName(null);
        assertFalse(offerTypeUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_OfferTypeNameIsEmpty() {
        OfferTypeUpdateRequestDto offerTypeUpdateRequestDto = getSampleOfferTypeUpdateRequestDto();
        offerTypeUpdateRequestDto.setName("");
        assertFalse(offerTypeUpdateRequestDto.isRequiredAvailable());
    }

    /**
     * This method creates sample OfferTypeUpdateRequestDto
     *
     * @return OfferTypeUpdateRequestDto
     */
    private OfferTypeUpdateRequestDto getSampleOfferTypeUpdateRequestDto() {
        OfferTypeUpdateRequestDto offerTypeUpdateRequestDto = new OfferTypeUpdateRequestDto();
        offerTypeUpdateRequestDto.setId(OFFER_TYPE_ID);
        offerTypeUpdateRequestDto.setName(OFFER_TYPE_NAME);
        return offerTypeUpdateRequestDto;
    }
}