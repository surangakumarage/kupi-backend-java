package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.SubscriptionStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link SubscriptionPlanStatusRequestDto} class
 */
class SubscriptionPlanStatusRequestDtoTest {

    private static final String SUBSCRIPTION_PLAN_ID = "spid-fc2aaeeb-9fe4-4584-a253-1831c8610600";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreProvided() {
        SubscriptionPlanStatusRequestDto subscriptionPlanStatusRequestDto = getSampleSubscriptionPlanStatusRequestDto();
        assertTrue(subscriptionPlanStatusRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_SubscriptionPlanIdIsNull() {
        SubscriptionPlanStatusRequestDto subscriptionPlanStatusRequestDto = getSampleSubscriptionPlanStatusRequestDto();
        subscriptionPlanStatusRequestDto.setId(null);
        assertFalse(subscriptionPlanStatusRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_SubscriptionPlanIdIsEmpty() {
        SubscriptionPlanStatusRequestDto subscriptionPlanStatusRequestDto = getSampleSubscriptionPlanStatusRequestDto();
        subscriptionPlanStatusRequestDto.setId("");
        assertFalse(subscriptionPlanStatusRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_SubscriptionPlanStatusIsNull() {
        SubscriptionPlanStatusRequestDto subscriptionPlanStatusRequestDto = getSampleSubscriptionPlanStatusRequestDto();
        subscriptionPlanStatusRequestDto.setStatus(null);
        assertFalse(subscriptionPlanStatusRequestDto.isRequiredAvailable());
    }

    /**
     * This method creates sample SubscriptionPlanStatusRequestDto
     *
     * @return SubscriptionPlanStatusRequestDto
     */
    private SubscriptionPlanStatusRequestDto getSampleSubscriptionPlanStatusRequestDto() {
        SubscriptionPlanStatusRequestDto subscriptionPlanStatusRequestDto = new SubscriptionPlanStatusRequestDto();
        subscriptionPlanStatusRequestDto.setId(SUBSCRIPTION_PLAN_ID);
        subscriptionPlanStatusRequestDto.setStatus(SubscriptionStatus.ACTIVE);
        return subscriptionPlanStatusRequestDto;
    }
}