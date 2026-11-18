package com.kupi.cc.base.domain.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link BrandRequestDto
 */
class BrandRequestDtoTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_ValidFieldsAreProvided() {
        BrandRequestDto brandRequestDto = getSampleBrandRequestDto();
        assertTrue(brandRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NameIsNull() {
        BrandRequestDto brandRequestDto = getSampleBrandRequestDto();
        brandRequestDto.setName(null);
        assertFalse(brandRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NameIsEmpty() {
        BrandRequestDto brandRequestDto = getSampleBrandRequestDto();
        brandRequestDto.setName("");
        assertFalse(brandRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_DescriptionIsNull() {
        BrandRequestDto brandRequestDto = getSampleBrandRequestDto();
        brandRequestDto.setDescription(null);
        assertFalse(brandRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_DescriptionIsEmpty() {
        BrandRequestDto brandRequestDto = getSampleBrandRequestDto();
        brandRequestDto.setDescription("");
        assertFalse(brandRequestDto.isRequiredAvailable());
    }

    /**
     * This method creates sample BrandRequestDto
     *
     * @return BrandRequestDto
     */
    private BrandRequestDto getSampleBrandRequestDto() {
        BrandRequestDto brandRequestDto = new BrandRequestDto();
        brandRequestDto.setName("Brand Name");
        brandRequestDto.setDescription("Brand Description");
        brandRequestDto.setImageUrl("https://picsum.photos/200/300");
        return brandRequestDto;
    }
}