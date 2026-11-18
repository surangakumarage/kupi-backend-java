package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.PlanType;
import com.kupi.cc.base.enums.UserType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link SubscriptionPlanRequestDto} class
 */
class SubscriptionPlanRequestDtoTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreProvided() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        assertTrue(subscriptionPlanRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_TitleIsNull() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setTitle(null);
        assertFalse(subscriptionPlanRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_TitleIsEmpty() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setTitle("");
        assertFalse(subscriptionPlanRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserRoleIsNull() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setUserRole(null);
        assertFalse(subscriptionPlanRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_PlanTypeIsNull() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setPlanType(null);
        assertFalse(subscriptionPlanRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NoOfTheDealPerMonthIsNull() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setNoOfDealsPerMonth(null);
        assertFalse(subscriptionPlanRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NoOfDealsOfTheDayEventsPerMonthIsNull() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setNoOfDealsOfTheDayEventsPerMonth(null);
        assertFalse(subscriptionPlanRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NoOfDealsOfTheDayPerEventIsNull() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setNoOfDealsOfTheDayPerEvent(null);
        assertFalse(subscriptionPlanRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_IsFreeIsNull() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setIsFree(null);
        assertFalse(subscriptionPlanRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnTrue_When_AmountIsZeroForIsFreeIsTrue() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setIsFree(true);
        subscriptionPlanRequestDto.setAmount(BigDecimal.ZERO);
        assertTrue(subscriptionPlanRequestDto.isValidAmount());
    }

    @Test
    void Should_ReturnTrue_When_AmountIsGreaterThanZeroForIsFreeIsFalse() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setIsFree(false);
        subscriptionPlanRequestDto.setAmount(BigDecimal.valueOf(100));
        assertTrue(subscriptionPlanRequestDto.isValidAmount());
    }

    @Test
    void Should_ReturnFalse_When_AmountIsZeroForIsFreeIsFalse() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setIsFree(false);
        subscriptionPlanRequestDto.setAmount(BigDecimal.ZERO);
        assertFalse(subscriptionPlanRequestDto.isValidAmount());
    }

    @Test
    void Should_ReturnTrue_When_AmountIsGreaterThanZeroForIsFreeIsTrue() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setIsFree(true);
        subscriptionPlanRequestDto.setAmount(BigDecimal.valueOf(100));
        assertFalse(subscriptionPlanRequestDto.isValidAmount());
    }

    @Test
    void Should_ReturnTrue_When_NoOfDealsPerMonthIsGreaterThanZero() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setNoOfDealsPerMonth(1);
        assertTrue(subscriptionPlanRequestDto.isValidNoOfDealsPerMonth());
    }

    @Test
    void Should_ReturnFalse_When_NoOfDealsPerMonthIsEqualToZero() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setNoOfDealsPerMonth(0);
        assertFalse(subscriptionPlanRequestDto.isValidNoOfDealsPerMonth());
    }

    @Test
    void Should_ReturnTrue_When_NoOfDealsOfTheDayEventsPerMonthIsGreaterThanOrEqualToZero() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setNoOfDealsOfTheDayEventsPerMonth(1);
        assertTrue(subscriptionPlanRequestDto.isValidNoOfDealsOfTheDayEventsPerMonth());
    }

    @Test
    void Should_ReturnFalse_When_NoOfDealsOfTheDayEventsPerMonthIsEqualToMinusOne() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setNoOfDealsOfTheDayEventsPerMonth(-1);
        assertFalse(subscriptionPlanRequestDto.isValidNoOfDealsOfTheDayEventsPerMonth());
    }

    @Test
    void Should_ReturnTrue_When_NoOfDealsOfTheDayPerEventIsGreaterThanOrEqualToOne() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setNoOfDealsOfTheDayPerEvent(1);
        assertTrue(subscriptionPlanRequestDto.isValidNoOfDealsOfTheDayPerEvent());
    }

    @Test
    void Should_ReturnFalse_When_NoOfDealsOfTheDayPerEventIsEqualToZero() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setNoOfDealsOfTheDayPerEvent(0);
        assertFalse(subscriptionPlanRequestDto.isValidNoOfDealsOfTheDayPerEvent());
    }

    @Test
    void Should_ReturnTrue_When_TitleANdSubTitleAndDescriptionAndTermsAndConditionCharacterAreWithInLimit() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        assertTrue(subscriptionPlanRequestDto.isValidCharacterLimit());
    }

    @Test
    void Should_ReturnFalse_When_TitleCharacterLengthIsHundredTen() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setTitle("orem Ipsum is simply dummy text of the printing and typesetting industry." +
                " Lorem Ipsum have been the industry's");
        assertFalse(subscriptionPlanRequestDto.isValidCharacterLimit());
    }

    @Test
    void Should_ReturnFalse_When_SubTitleCharacterLengthIsHundredTen() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setSubTitle("orem Ipsum is simply dummy text of the printing and typesetting industry." +
                " Lorem Ipsum have been the industry's");
        assertFalse(subscriptionPlanRequestDto.isValidCharacterLimit());
    }

    /**
     * This method creates sample SubscriptionPlanRequestDto
     *
     * @return SubscriptionPlanRequestDto
     */
    private SubscriptionPlanRequestDto getSampleSubscriptionPlanRequestDto() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = new SubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setTitle("Title");
        subscriptionPlanRequestDto.setSubTitle("Subtitle");
        subscriptionPlanRequestDto.setPlanType(PlanType.MONTHLY);
        subscriptionPlanRequestDto.setNoOfDealsOfTheDayEventsPerMonth(1);
        subscriptionPlanRequestDto.setNoOfDealsPerMonth(1);
        subscriptionPlanRequestDto.setNoOfDealsOfTheDayPerEvent(1);
        subscriptionPlanRequestDto.setUserRole(UserType.MERCHANT);
        subscriptionPlanRequestDto.setIsFree(false);
        subscriptionPlanRequestDto.setAmount(BigDecimal.ONE);
        subscriptionPlanRequestDto.setDescription("Description");
        subscriptionPlanRequestDto.setTermsAndCondition("terms and conditions");
        return subscriptionPlanRequestDto;
    }
}