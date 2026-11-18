package com.kupi.cc.base.domain.request;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link CategoryBrandMerchantRequestDto} class
 */
class CategoryBrandMerchantRequestDtoTest {

    private static final String MERCHANT_ID = "uid-123456789";
    private static final String CATEGORY_ID = "cid-123456789";

    /**
     * Required fields existence check
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        List<String> categories = new ArrayList<>();
        categories.add(CATEGORY_ID);
        CategoryBrandMerchantRequestDto categoryBrandMerchantRequestDto = new CategoryBrandMerchantRequestDto();
        categoryBrandMerchantRequestDto.setMerchantId(MERCHANT_ID);
        categoryBrandMerchantRequestDto.setCategories(categories);
        assertTrue(categoryBrandMerchantRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MerchantIdIsEmpty() {
        List<String> categories = new ArrayList<>();
        categories.add(CATEGORY_ID);
        CategoryBrandMerchantRequestDto categoryBrandMerchantRequestDto = new CategoryBrandMerchantRequestDto();
        categoryBrandMerchantRequestDto.setMerchantId("");
        categoryBrandMerchantRequestDto.setCategories(categories);
        assertFalse(categoryBrandMerchantRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MerchantIdIsNull() {
        List<String> categories = new ArrayList<>();
        categories.add(CATEGORY_ID);
        CategoryBrandMerchantRequestDto categoryBrandMerchantRequestDto = new CategoryBrandMerchantRequestDto();
        categoryBrandMerchantRequestDto.setMerchantId(null);
        categoryBrandMerchantRequestDto.setCategories(categories);
        assertFalse(categoryBrandMerchantRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CategoriesIsEmpty() {
        List<String> categories = new ArrayList<>();
        CategoryBrandMerchantRequestDto categoryBrandMerchantRequestDto = new CategoryBrandMerchantRequestDto();
        categoryBrandMerchantRequestDto.setMerchantId(MERCHANT_ID);
        categoryBrandMerchantRequestDto.setCategories(categories);
        assertFalse(categoryBrandMerchantRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CategoriesIsNull() {
        CategoryBrandMerchantRequestDto categoryBrandMerchantRequestDto = new CategoryBrandMerchantRequestDto();
        categoryBrandMerchantRequestDto.setMerchantId(MERCHANT_ID);
        categoryBrandMerchantRequestDto.setCategories(null);
        assertFalse(categoryBrandMerchantRequestDto.isRequiredAvailable());
    }

    /**
     * Brand availability check
     */
    @Test
    void Should_ReturnTrue_When_BrandIsAvailable() {
        List<String> brands = new ArrayList<>();
        brands.add("bid-123");
        CategoryBrandMerchantRequestDto categoryBrandMerchantRequestDto = new CategoryBrandMerchantRequestDto();
        categoryBrandMerchantRequestDto.setBrands(brands);
        assertTrue(categoryBrandMerchantRequestDto.isBrandIdsAvailable());
    }

    @Test
    void Should_ReturnFalse_When_BrandIsEmpty() {
        List<String> brands = new ArrayList<>();
        CategoryBrandMerchantRequestDto categoryBrandMerchantRequestDto = new CategoryBrandMerchantRequestDto();
        categoryBrandMerchantRequestDto.setBrands(brands);
        assertFalse(categoryBrandMerchantRequestDto.isBrandIdsAvailable());
    }

    @Test
    void Should_ReturnFalse_When_BrandIsNull() {
        CategoryBrandMerchantRequestDto categoryBrandMerchantRequestDto = new CategoryBrandMerchantRequestDto();
        categoryBrandMerchantRequestDto.setBrands(null);
        assertFalse(categoryBrandMerchantRequestDto.isBrandIdsAvailable());
    }
}