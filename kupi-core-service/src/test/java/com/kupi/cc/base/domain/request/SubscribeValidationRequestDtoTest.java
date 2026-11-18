package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.UserType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link SubscribeValidationRequestDto} class.
 */
class SubscribeValidationRequestDtoTest {

    private static final String SUBSCRIPTION_PLAN_ID = "spid-fc2aaeeb-9fe4-4584-a253-1831c8610600";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreProvided() {
        SubscribeValidationRequestDto subscribeValidationRequestDto = getSampleSubscribeRequestDto();
        assertTrue(subscribeValidationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_SubscriptionPlanIdIsNull() {
        SubscribeValidationRequestDto subscribeValidationRequestDto = getSampleSubscribeRequestDto();
        subscribeValidationRequestDto.setSubscriptionPlanId(null);
        assertFalse(subscribeValidationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_SubscriptionPlanIdIsEmpty() {
        SubscribeValidationRequestDto subscribeValidationRequestDto = getSampleSubscribeRequestDto();
        subscribeValidationRequestDto.setSubscriptionPlanId("");
        assertFalse(subscribeValidationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserTypeIsNull() {
        SubscribeValidationRequestDto subscribeValidationRequestDto = getSampleSubscribeRequestDto();
        subscribeValidationRequestDto.setUserType(null);
        assertFalse(subscribeValidationRequestDto.isRequiredAvailable());
    }

    /**
     * This method creates sample SubscribeValidationRequestDto
     *
     * @return SubscribeValidationRequestDto
     */
    private SubscribeValidationRequestDto getSampleSubscribeRequestDto() {
        SubscribeValidationRequestDto subscribeValidationRequestDto = new SubscribeValidationRequestDto();
        subscribeValidationRequestDto.setSubscriptionPlanId(SUBSCRIPTION_PLAN_ID);
        subscribeValidationRequestDto.setUserType(UserType.MERCHANT);
        return subscribeValidationRequestDto;
    }
}