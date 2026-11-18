package com.kupi.cc.base.domain.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class test for {@link  BrandUpdateRequestDto} class
 */
class BrandUpdateRequestDtoTest {

    private static final String BRAND_ID = "bid-02a2022f-98c5-4237-a9c7-c1faf989a4a5";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_ValidFieldsAreProvided() {
        BrandUpdateRequestDto brandUpdateRequestDto = getSampleBrandUpdateRequestDto();
        assertTrue(brandUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_BrandIdIsNull() {
        BrandUpdateRequestDto brandUpdateRequestDto = getSampleBrandUpdateRequestDto();
        brandUpdateRequestDto.setId(null);
        assertFalse(brandUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_BrandIdIsEmpty() {
        BrandUpdateRequestDto brandUpdateRequestDto = getSampleBrandUpdateRequestDto();
        brandUpdateRequestDto.setId("");
        assertFalse(brandUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_BrandNameIsNull() {
        BrandUpdateRequestDto brandUpdateRequestDto = getSampleBrandUpdateRequestDto();
        brandUpdateRequestDto.setName(null);
        assertFalse(brandUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_BrandNameIsEmpty() {
        BrandUpdateRequestDto brandUpdateRequestDto = getSampleBrandUpdateRequestDto();
        brandUpdateRequestDto.setName("");
        assertFalse(brandUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_BrandDescriptionIsNull() {
        BrandUpdateRequestDto brandUpdateRequestDto = getSampleBrandUpdateRequestDto();
        brandUpdateRequestDto.setDescription(null);
        assertFalse(brandUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_BrandDescriptionIsEmpty() {
        BrandUpdateRequestDto brandUpdateRequestDto = getSampleBrandUpdateRequestDto();
        brandUpdateRequestDto.setDescription("");
        assertFalse(brandUpdateRequestDto.isRequiredAvailable());
    }

    /**
     * This method creates sample BrandUpdateRequestDto
     *
     * @return BrandUpdateRequestDto
     */
    private BrandUpdateRequestDto getSampleBrandUpdateRequestDto() {
        BrandUpdateRequestDto brandUpdateRequestDto = new BrandUpdateRequestDto();
        brandUpdateRequestDto.setId(BRAND_ID);
        brandUpdateRequestDto.setName("Brand name");
        brandUpdateRequestDto.setDescription("Brand description");
        brandUpdateRequestDto.setImageUrl("https://objects-kupi-dev.s3.ap-southeast-1.amazonaws.com/fid-3338ebed-e3d3-4f86-8b3e-92887f27e51d.jpg");
        return brandUpdateRequestDto;
    }
}