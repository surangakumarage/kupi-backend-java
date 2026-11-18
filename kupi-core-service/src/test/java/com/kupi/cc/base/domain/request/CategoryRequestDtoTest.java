package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.CategoryType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class test for {@link CategoryRequestDto} class
 */
class CategoryRequestDtoTest {
    private static final String TIME_ZONE = "Asia/Colombo";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreAvailable() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        assertTrue(categoryRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ProvidedNameIsEmpty() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        categoryRequestDto.setName("");
        assertFalse(categoryRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ProvidedNameIsNull() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        categoryRequestDto.setName(null);
        assertFalse(categoryRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ProvidedDescriptionIsEmpty() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        categoryRequestDto.setDescription("");
        assertFalse(categoryRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ProvidedDescriptionIsNull() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        categoryRequestDto.setDescription(null);
        assertFalse(categoryRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ProvidedImageUrlIsEmpty() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        categoryRequestDto.setImageUrl("");
        assertFalse(categoryRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ProvidedImageUrlIsNull() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        categoryRequestDto.setImageUrl(null);
        assertFalse(categoryRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnTrue_When_ValidDateIsProvided() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        assertTrue(categoryRequestDto.isValidDateRange(TIME_ZONE));
    }

    @Test
    void Should_ReturnFalse_When_ProvidedStartDateIsNull() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        categoryRequestDto.setStartDate(null);
        assertFalse(categoryRequestDto.isValidDateRange(TIME_ZONE));
    }

    @Test
    void Should_ReturnFalse_When_ProvidedExpiryDateIsNull() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        categoryRequestDto.setExpiryDate(null);
        assertFalse(categoryRequestDto.isValidDateRange(TIME_ZONE));
    }

    @Test
    void Should_ReturnFalse_When_ProvidedStartDateIsGreaterThanExpiryDate() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        categoryRequestDto.setStartDate(1670697000000L);
        categoryRequestDto.setExpiryDate(1670697000000L);
        assertFalse(categoryRequestDto.isValidDateRange(TIME_ZONE));
    }

    @Test
    void Should_ReturnFalse_When_ProvidedStartDateIsLessThanTodayDate() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        categoryRequestDto.setStartDate(1650484031450L);
        assertFalse(categoryRequestDto.isValidDateRange(TIME_ZONE));
    }

    @Test
    void Should_ReturnFalse_When_ProvidedExpiryDateIsAlreadyExpired() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        categoryRequestDto.setExpiryDate(1650484031450L);
        assertFalse(categoryRequestDto.isValidDateRange(TIME_ZONE));
    }

    @Test
    void Should_ReturnTrue_When_ValidDateIsProvidedForUpdateCategory() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        assertTrue(categoryRequestDto.isValidDateRangeForUpdateCategory());
    }

    @Test
    void Should_ReturnFalse_When_ProvidedStartDateIsNullForUpdateCategory() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        categoryRequestDto.setStartDate(null);
        assertFalse(categoryRequestDto.isValidDateRangeForUpdateCategory());
    }

    @Test
    void Should_ReturnFalse_When_ProvidedExpiryDateIsNullForUpdateCategory() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        categoryRequestDto.setExpiryDate(null);
        assertFalse(categoryRequestDto.isValidDateRangeForUpdateCategory());
    }

    @Test
    void Should_ReturnFalse_When_ProvidedStartDateIsGreaterThanExpiryDateForUpdateCategory() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        categoryRequestDto.setStartDate(1670697000000L);
        categoryRequestDto.setExpiryDate(1670697000000L);
        assertFalse(categoryRequestDto.isValidDateRange(TIME_ZONE));
    }

    @Test
    void Should_ReturnFalse_When_ProvidedExpiryDateIsAlreadyExpiredForUpdateCategory() {
        CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
        categoryRequestDto.setExpiryDate(1650484031450L);
        assertFalse(categoryRequestDto.isValidDateRange(TIME_ZONE));
    }

    /**
     * This method creates sample category request dto
     *
     * @return CategoryRequestDto
     */
    private CategoryRequestDto getSampleCategoryRequestDto() {
        List<String> relatedCategories = new ArrayList<>();
        long currentTimeStamp = System.currentTimeMillis();

        CategoryRequestDto categoryRequestDto = new CategoryRequestDto();
        categoryRequestDto.setCategoryType(CategoryType.NORMAL);
        categoryRequestDto.setDescription("food and snacks");
        categoryRequestDto.setName("New Year");
        categoryRequestDto.setPopular(true);
        categoryRequestDto.setStartDate(currentTimeStamp + 86400000);
        categoryRequestDto.setExpiryDate(currentTimeStamp + 172800000);
        categoryRequestDto.setRelatedCategories(relatedCategories);
        categoryRequestDto.setImageUrl("https://picsum.photos/200/300");
        return categoryRequestDto;
    }
}