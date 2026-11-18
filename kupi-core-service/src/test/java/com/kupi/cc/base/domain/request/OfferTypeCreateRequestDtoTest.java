package com.kupi.cc.base.domain.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OfferTypeCreateRequestDtoTest {

    private static final String OFFER_TYPE_NAME = "OFFER_TYPE";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreProvided() {
        OfferTypeCreateRequestDto offerTypeCreateRequestDto = getSampleOfferTypeCreateRequestDto();
        assertTrue(offerTypeCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_OfferTypeNameIsNull() {
        OfferTypeCreateRequestDto offerTypeCreateRequestDto = getSampleOfferTypeCreateRequestDto();
        offerTypeCreateRequestDto.setName(null);
        assertFalse(offerTypeCreateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_OfferTypeNameIsEmpty() {
        OfferTypeCreateRequestDto offerTypeCreateRequestDto = getSampleOfferTypeCreateRequestDto();
        offerTypeCreateRequestDto.setName("");
        assertFalse(offerTypeCreateRequestDto.isRequiredAvailable());
    }

    /**
     * This method creates sample OfferTypeCreateRequestDto
     *
     * @return OfferTypeCreateRequestDto
     */
    private OfferTypeCreateRequestDto getSampleOfferTypeCreateRequestDto() {
        OfferTypeCreateRequestDto offerTypeCreateRequestDto = new OfferTypeCreateRequestDto();
        offerTypeCreateRequestDto.setName(OFFER_TYPE_NAME);
        return offerTypeCreateRequestDto;
    }
}