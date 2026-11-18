package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.PlanType;
import com.kupi.cc.base.enums.UserType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link SubscriptionUpdateRequestDto} class
 */
class SubscriptionUpdateRequestDtoTest {

    private static final String SUBSCRIPTION_PLAN_ID = "spid-fc2aaeeb-9fe4-4584-a253-1831c8610600";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreProvided() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        assertTrue(subscriptionUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_SubscriptionIdIsNull() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setId(null);
        assertFalse(subscriptionUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_SubscriptionIdIsEmpty() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setId("");
        assertFalse(subscriptionUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_TitleIsNull() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setTitle(null);
        assertFalse(subscriptionUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_TitleIsEmpty() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setTitle("");
        assertFalse(subscriptionUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserRoleIsNull() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setUserRole(null);
        assertFalse(subscriptionUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_PlanTypeIsNull() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setPlanType(null);
        assertFalse(subscriptionUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NoOfTheDealPerMonthIsNull() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setNoOfDealsPerMonth(null);
        assertFalse(subscriptionUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NoOfDealsOfTheDayEventsPerMonthIsNull() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setNoOfDealsOfTheDayEventsPerMonth(null);
        assertFalse(subscriptionUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NoOfDealsOfTheDayPerEventIsNull() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setNoOfDealsOfTheDayPerEvent(null);
        assertFalse(subscriptionUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_IsFreeIsNull() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setIsFree(null);
        assertFalse(subscriptionUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnTrue_When_AmountIsZeroForIsFreeIsTrue() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setIsFree(true);
        subscriptionUpdateRequestDto.setAmount(BigDecimal.ZERO);
        assertTrue(subscriptionUpdateRequestDto.isValidAmount());
    }

    @Test
    void Should_ReturnTrue_When_AmountIsGreaterThanZeroForIsFreeIsFalse() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setIsFree(false);
        subscriptionUpdateRequestDto.setAmount(BigDecimal.valueOf(100));
        assertTrue(subscriptionUpdateRequestDto.isValidAmount());
    }

    @Test
    void Should_ReturnFalse_When_AmountIsZeroForIsFreeIsFalse() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setIsFree(false);
        subscriptionUpdateRequestDto.setAmount(BigDecimal.ZERO);
        assertFalse(subscriptionUpdateRequestDto.isValidAmount());
    }

    @Test
    void Should_ReturnTrue_When_AmountIsGreaterThanZeroForIsFreeIsTrue() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setIsFree(true);
        subscriptionUpdateRequestDto.setAmount(BigDecimal.valueOf(100));
        assertFalse(subscriptionUpdateRequestDto.isValidAmount());
    }

    @Test
    void Should_ReturnTrue_When_NoOfDealsPerMonthIsGreaterThanZero() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setNoOfDealsPerMonth(1);
        assertTrue(subscriptionUpdateRequestDto.isValidNoOfDealsPerMonth());
    }

    @Test
    void Should_ReturnFalse_When_NoOfDealsPerMonthIsEqualToZero() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setNoOfDealsPerMonth(0);
        assertFalse(subscriptionUpdateRequestDto.isValidNoOfDealsPerMonth());
    }

    @Test
    void Should_ReturnTrue_When_NoOfDealsOfTheDayEventsPerMonthIsGreaterThanOrEqualToZero() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setNoOfDealsOfTheDayEventsPerMonth(1);
        assertTrue(subscriptionUpdateRequestDto.isValidNoOfDealsOfTheDayEventsPerMonth());
    }

    @Test
    void Should_ReturnFalse_When_NoOfDealsOfTheDayEventsPerMonthIsEqualToMinusOne() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setNoOfDealsOfTheDayEventsPerMonth(-1);
        assertFalse(subscriptionUpdateRequestDto.isValidNoOfDealsOfTheDayEventsPerMonth());
    }

    @Test
    void Should_ReturnTrue_When_NoOfDealsOfTheDayPerEventIsGreaterThanOrEqualToOne() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setNoOfDealsOfTheDayPerEvent(1);
        assertTrue(subscriptionUpdateRequestDto.isValidNoOfDealsOfTheDayPerEvent());
    }

    @Test
    void Should_ReturnFalse_When_NoOfDealsOfTheDayPerEventIsEqualToZero() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setNoOfDealsOfTheDayPerEvent(0);
        assertFalse(subscriptionUpdateRequestDto.isValidNoOfDealsOfTheDayPerEvent());
    }

    @Test
    void Should_ReturnTrue_When_TitleANdSubTitleAndDescriptionAndTermsAndConditionCharacterAreWithInLimit() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        assertTrue(subscriptionUpdateRequestDto.isValidCharacterLimit());
    }

    @Test
    void Should_ReturnFalse_When_TitleCharacterLengthIsHundredTen() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setTitle("orem Ipsum is simply dummy text of the printing and typesetting industry." +
                " Lorem Ipsum have been the industry's");
        assertFalse(subscriptionUpdateRequestDto.isValidCharacterLimit());
    }

    @Test
    void Should_ReturnFalse_When_SubTitleCharacterLengthIsHundredTen() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setSubTitle("orem Ipsum is simply dummy text of the printing and typesetting industry." +
                " Lorem Ipsum have been the industry's");
        assertFalse(subscriptionUpdateRequestDto.isValidCharacterLimit());
    }

    @Test
    void Should_ReturnFalse_When_DescriptionCharacterLengthIsFiveHundredTen() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setDescription("It is a long established fact that a reader will be distracted by " +
                "the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it " +
                "has a more-or-less normal distribution of letters, as  opposed to using  Content here, content here', " +
                "making it look like readable English. Many desktop  publishing packages and web page editors now use " +
                "Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites " +
                "still in their infancy. Various versions");
        assertFalse(subscriptionUpdateRequestDto.isValidCharacterLimit());
    }

    /**
     * This method creates sample SubscriptionUpdateRequestDto
     *
     * @return SubscriptionUpdateRequestDto
     */
    private SubscriptionUpdateRequestDto getSampleSubscriptionUpdateRequestDto() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = new SubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setId(SUBSCRIPTION_PLAN_ID);
        subscriptionUpdateRequestDto.setTitle("Title");
        subscriptionUpdateRequestDto.setSubTitle("Subtitle");
        subscriptionUpdateRequestDto.setPlanType(PlanType.MONTHLY);
        subscriptionUpdateRequestDto.setNoOfDealsOfTheDayEventsPerMonth(1);
        subscriptionUpdateRequestDto.setNoOfDealsPerMonth(1);
        subscriptionUpdateRequestDto.setNoOfDealsOfTheDayPerEvent(1);
        subscriptionUpdateRequestDto.setUserRole(UserType.MERCHANT);
        subscriptionUpdateRequestDto.setIsFree(false);
        subscriptionUpdateRequestDto.setAmount(BigDecimal.ONE);
        subscriptionUpdateRequestDto.setDescription("Description");
        subscriptionUpdateRequestDto.setTermsAndCondition("terms and conditions");
        return subscriptionUpdateRequestDto;
    }
}