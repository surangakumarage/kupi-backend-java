package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.DeductionType;
import com.kupi.cc.base.enums.UserType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests the {@link DealRequestDto} class
 */
class DealRequestDtoTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreAvailable() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        assertTrue(dealRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_TitleIsEmpty() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setTitle("");
        assertFalse(dealRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_TitleIsNull() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setTitle(null);
        assertFalse(dealRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_SubTitleIsEmpty() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setTitle("");
        assertFalse(dealRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_SubTitleIsNull() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setSubTitle(null);
        assertFalse(dealRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_TermAndConditionIsEmpty() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setTermsAndConditions("");
        assertFalse(dealRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_TermAndConditionIsNull() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setTermsAndConditions(null);
        assertFalse(dealRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MerchantIdIsEmpty() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setMerchantId("");
        assertFalse(dealRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MerchantIdIsNull() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setMerchantId(null);
        assertFalse(dealRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CategoryIdsAreEmpty() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        List<String> categoryIds = new ArrayList<>();
        dealRequestDto.setCategoryIds(categoryIds);
        assertFalse(dealRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CategoryIdsAreNull() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setCategoryIds(null);
        assertFalse(dealRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsNull() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setDeductionType(null);
        assertFalse(dealRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CoverImageIsNull() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setCoverImage(null);
        assertFalse(dealRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CoverImageIsEmpty() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setCoverImage("");
        assertFalse(dealRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ImageUrlsAreEmpty() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        List<String> imageUrls = new ArrayList<>();
        dealRequestDto.setImageUrls(imageUrls);
        assertFalse(dealRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnTrue_When_ValidFromIsValid() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setValidFrom(System.currentTimeMillis() + 3600);
        assertTrue(dealRequestDto.isValidStartDate());
    }

    @Test
    void Should_ReturnFalse_When_ValidFromIsNotValid() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setValidFrom(System.currentTimeMillis() - 3600);
        assertFalse(dealRequestDto.isValidStartDate());
    }

    @Test
    void Should_ReturnTrue_When_expiredOnIsTrue() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setExpiredOn(System.currentTimeMillis() + 360000);
        assertTrue(dealRequestDto.isValidExpireDate());
    }

    @Test
    void Should_ReturnFalse_When_expiredOnIsFalse() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setExpiredOn(System.currentTimeMillis() - 36000000);
        assertTrue(dealRequestDto.isValidExpireDate());
    }

    @Test
    void Should_ReturnTrue_When_originalPriceIsTrue() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        assertTrue(dealRequestDto.isValidPrice());
    }

    @Test
    void Should_ReturnTrue_When_brandAvailable() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        assertTrue(dealRequestDto.isBrandIdsAvailable());
    }

    @Test
    void Should_ReturnFalse_When_brandIsNull() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setBrandIds(null);
        assertFalse(dealRequestDto.isBrandIdsAvailable());
    }

    @Test
    void Should_ReturnFalse_When_brandIsEmpty() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        List<String> brandIds = new ArrayList<>();
        dealRequestDto.setBrandIds(brandIds);
        assertFalse(dealRequestDto.isBrandIdsAvailable());
    }

    @Test
    void Should_ReturnFalse_When_DeductionPercentageIsNull() {
        DealRequestDto dealRequestDto = getDealRequestDto();
        dealRequestDto.setDeductionType(DeductionType.PERCENTAGE);
        dealRequestDto.setDeductionPercentage(0.0);
        assertTrue(dealRequestDto.isRequiredAvailable());
    }


    @Test
    void Should_ReturnFalse_When_DeductionTypeIsAmountAndOriginalPriceIsZeroAndNegativeDeductionAmountWithOriginalPriceGreaterThanDeductionAmount() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(0.0);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setDeductionAmount(-15.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsAmountAndOriginalPriceIsZeroAndZeroDeductionAmountWithOriginalPriceGreaterThanDeductionAmount() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(0.0);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setDeductionAmount(0.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnTrue_When_DeductionTypeIsAmountAndOriginalPricePositiveAndNegativeDeductionAmountWithOriginalPriceGreaterThanDeductionAmount() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(10.0);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setDeductionAmount(-5.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnTrue_When_DeductionTypeIsAmountAndOriginalPricePositiveAndNoDeductionAmountWithOriginalPriceGreaterThanDeductionAmount() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(10.0);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);

        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnTrue_When_DeductionTypeIsAmountAndOriginalPricePositiveAndPositiveDeductionAmountWithOriginalPriceGreaterThanDeductionAmount() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(10.0);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setDeductionAmount(5.0);
        assertTrue(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsAmountAndOriginalPriceNegativeAndNegativeDeductionAmountWithOriginalPriceGreaterThanDeductionAmount() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(-10.0);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setDeductionAmount(-50.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsAmountAndOriginalPriceNegativeAndZeroDeductionAmountWithOriginalPriceGreaterThanDeductionAmount() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(-10.0);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setDeductionAmount(0.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsAmountAndOriginalPriceNegativeAndPositiveDeductionAmountWithOriginalPriceGreaterThanDeductionAmount() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(-10.0);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setDeductionAmount(10.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsAmountAndOriginalPriceIsZeroAndNegativeDeductionAmountWithOriginalPriceLessThanDeductionAmount() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(0.0);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setDeductionAmount(-10.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsAmountAndOriginalPriceIsZeroAndZeroDeductionAmountWithOriginalPriceLessThanDeductionAmount() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(0.0);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setDeductionAmount(0.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsAmountAndOriginalPriceIsZeroAndPositiveDeductionAmountWithOriginalPriceLessThanDeductionAmount() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(0.0);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setDeductionAmount(10.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsAmountAndOriginalPricePositiveAndNegativeDeductionAmountWithOriginalPriceLessThanDeductionAmount() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(10.0);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setDeductionAmount(-10.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsAmountAndOriginalPricePositiveAndDeductionAmountIsZero() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(10.0);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setDeductionAmount(0.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsAmountAndOriginalPricePositiveAndPositiveDeductionAmountWithOriginalPriceLessThanDeductionAmount() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(100.0);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setDeductionAmount(110.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsAmountAndOriginalPriceNegativeAndNegativeDeductionAmountWithOriginalPriceLessThanDeductionAmount() {

        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(-10.0);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setDeductionAmount(-5.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsAmountAndOriginalPriceNegativeAndZeroDeductionAmountWithOriginalPriceLessThanDeductionAmount() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(-10.0);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setDeductionAmount(0.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsAmountAndOriginalPriceNegativeAndPositiveDeductionAmountWithOriginalPriceLessThanDeductionAmount() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(-10.0);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setDeductionAmount(10.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsPercentageAndOriginalPriceIsZeroAndNegativeDeductionPercentage() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(0.0);
        dealRequestDto.setDeductionType(DeductionType.PERCENTAGE);
        dealRequestDto.setDeductionPercentage(-10.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsPercentageAndOriginalPriceIsZeroAndZeroDeductionPercentage() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(0.0);
        dealRequestDto.setDeductionType(DeductionType.PERCENTAGE);
        dealRequestDto.setDeductionPercentage(0.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsPercentageAndOriginalPriceIsZeroAndDeductionPercentageInBetWeenZeroAndHundred() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(0.0);
        dealRequestDto.setDeductionType(DeductionType.PERCENTAGE);
        dealRequestDto.setDeductionPercentage(10.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnTrue_When_DeductionTypeIsPercentageAndOriginalPriceIsZeroAndDeductionPercentageMoreThanHundred() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(0.0);
        dealRequestDto.setDeductionType(DeductionType.PERCENTAGE);
        dealRequestDto.setDeductionPercentage(110.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnTrue_When_DeductionTypeIsPercentageAndOriginalPriceNegativeAndNegativeDeductionPercentage() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(-10.0);
        dealRequestDto.setDeductionType(DeductionType.PERCENTAGE);
        dealRequestDto.setDeductionPercentage(-110.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnTrue_When_DeductionTypeIsPercentageAndOriginalPriceNegativeAndZeroDeductionPercentage() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(-10.0);
        dealRequestDto.setDeductionType(DeductionType.PERCENTAGE);
        dealRequestDto.setDeductionPercentage(0.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnTrue_When_DeductionTypeIsPercentageAndOriginalPriceNegativeAndDeductionPercentageInBetweenZeroAndHundred() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(-10.0);
        dealRequestDto.setDeductionType(DeductionType.PERCENTAGE);
        dealRequestDto.setDeductionPercentage(10.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnTrue_When_DeductionTypeIsPercentageAndOriginalPriceNegativeAndDeductionPercentageMoreThanHundred() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(-10.0);
        dealRequestDto.setDeductionType(DeductionType.PERCENTAGE);
        dealRequestDto.setDeductionPercentage(120.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnTrue_When_DeductionTypeIsPercentageAndOriginalPricePositiveAndDeductionPercentageNegative() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(10.0);
        dealRequestDto.setDeductionType(DeductionType.PERCENTAGE);
        dealRequestDto.setDeductionPercentage(-5.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_DeductionTypeIsPercentageAndOriginalPricePositiveAndDeductionPercentageZero() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(10.0);
        dealRequestDto.setDeductionType(DeductionType.PERCENTAGE);
        dealRequestDto.setDeductionPercentage(0.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnTrue_When_DeductionTypeIsPercentageAndOriginalPricePositiveAndDeductionPercentageInBetweenZeroAndHundred() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(10.0);
        dealRequestDto.setDeductionType(DeductionType.PERCENTAGE);
        dealRequestDto.setDeductionPercentage(50.0);
        assertTrue(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnTrue_When_DeductionTypeIsPercentageAndOriginalPricePositiveAndDeductionPercentageMoreThanHundred() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(10.0);
        dealRequestDto.setDeductionType(DeductionType.PERCENTAGE);
        dealRequestDto.setDeductionPercentage(150.0);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnTrue_When_UserTypeIsBankAndShopIdIsNotEmpty() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setShopId("uid-0c558887-bb0d-4a33-aee8-5dd5444b7a32");
        assertTrue(dealRequestDto.isRequiredAvailable(UserType.BANK));
    }

    @Test
    void Should_ReturnFalse_When_UserTypeIsBankAndShopIdIsEmpty() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setShopId("");
        assertFalse(dealRequestDto.isRequiredAvailable(UserType.BANK));
    }

    @Test
    void Should_ReturnFalse_When_UserTypeIsBankAndShopIdIsNull() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setShopId(null);
        assertFalse(dealRequestDto.isRequiredAvailable(UserType.BANK));
    }

    @Test
    void Should_ReturnTrue_When_UserTypeIsNotBankAndRequiredFieldsAreAvailable() {
        var dealRequestDto = getDealRequestDto();
        assertTrue(dealRequestDto.isRequiredAvailable(UserType.MERCHANT));
    }

    @Test
    void Should_ReturnFalse_When_UserTypeIsNotBankAndRequiredFieldsAreMissing() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setTitle(null);
        assertFalse(dealRequestDto.isRequiredAvailable(UserType.MERCHANT));
    }

    @Test
    void Should_ReturnFalse_When_OriginalPriceIsNull() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(null);
        assertFalse(dealRequestDto.isValidPrice());
    }

    @Test
    void Should_ReturnTrue_When_OriginalPriceIsNullAndDeductionAmountIsGraterThanZero() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(null);
        dealRequestDto.setDeductionAmount(1000.00);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        assertTrue(dealRequestDto.isValidDeductionValue());
    }

    @Test
    void Should_ReturnFalse_When_OriginalPriceIsNullAndDeductionAmountIsZero() {
        var dealRequestDto = getDealRequestDto();
        dealRequestDto.setOriginalPrice(null);
        dealRequestDto.setDeductionAmount(0.00);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        assertFalse(dealRequestDto.isValidDeductionValue());
    }

    /**
     * This method creates sample deal request dto
     *
     * @return dealRequestDto
     */
    private DealRequestDto getDealRequestDto() {

        List<String> imgUrls = new ArrayList<>();
        imgUrls.add("https://app.slack.com/1.png");
        imgUrls.add("https://app.slack.com/2.png");
        imgUrls.add("https://app.slack.com/3.png");

        List<String> brandIds = new ArrayList<>();
        brandIds.add("bid-1245454-154545");
        brandIds.add("bid-1245454-4587487");
        brandIds.add("bid-1245454-1877574");

        List<String> categoryIds = new ArrayList<>();
        categoryIds.add("cid-1585465865");
        categoryIds.add("cid-5674663588");
        categoryIds.add("cid-7656547854");

        DealRequestDto dealRequestDto = new DealRequestDto();
        dealRequestDto.setTitle("Awurudu Deal");
        dealRequestDto.setSubTitle("for sinala and tamil new year");
        dealRequestDto.setDescription("Get 50% off for selected items!");
        dealRequestDto.setTermsAndConditions("Bill amount should be 100 LKR");
        dealRequestDto.setImageUrls(imgUrls);
        dealRequestDto.setCoverImage("https://app.slack.com/coverImg.jpg");
        dealRequestDto.setOriginalPrice(100.00);
        dealRequestDto.setQuantity(20);
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setDeductionAmount(20.00);
        dealRequestDto.setValidFrom(1639106071000L);
        dealRequestDto.setExpiredOn(1545454545);
        dealRequestDto.setMerchantId("uid-1545452154");
        dealRequestDto.setBrandIds(brandIds);
        dealRequestDto.setCategoryIds(categoryIds);
        return dealRequestDto;
    }
}