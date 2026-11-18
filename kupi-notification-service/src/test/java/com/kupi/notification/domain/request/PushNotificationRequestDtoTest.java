package com.kupi.notification.domain.request;

import com.kupi.notification.enums.NotificationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link PushNotificationRequestDto} class
 */
class PushNotificationRequestDtoTest {

    private static final String USER_ID = "uid-1";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreProvided() {
        PushNotificationRequestDto pushNotificationRequestDto = getSamplePushNotificationRequestDto();
        assertTrue(pushNotificationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdIsNull() {
        PushNotificationRequestDto pushNotificationRequestDto = getSamplePushNotificationRequestDto();
        pushNotificationRequestDto.setUserId(null);
        assertFalse(pushNotificationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdIsEmpty() {
        PushNotificationRequestDto pushNotificationRequestDto = getSamplePushNotificationRequestDto();
        pushNotificationRequestDto.setUserId("");
        assertFalse(pushNotificationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_DealsGroupIsEmpty() {
        PushNotificationRequestDto pushNotificationRequestDto = getSamplePushNotificationRequestDto();
        pushNotificationRequestDto.setDealGroups(new ArrayList<>());
        assertFalse(pushNotificationRequestDto.isRequiredAvailable());
    }

    /**
     * This method creates sample PushNotificationRequestDto
     *
     * @return PushNotificationRequestDto
     */
    private PushNotificationRequestDto getSamplePushNotificationRequestDto() {
        BasicUserRequestDto merchant = new BasicUserRequestDto();
        merchant.setId("ID");
        BasicDealRequestDto basicDealRequestDto = getSampleBasicDealRequestDto();
        List<BasicDealRequestDto> basicDealRequestDtos = new ArrayList<>();
        basicDealRequestDtos.add(basicDealRequestDto);
        DealGroupRequestDto dealGroupRequestDto = new DealGroupRequestDto();
        dealGroupRequestDto.setMerchant(merchant);
        dealGroupRequestDto.setDeals(basicDealRequestDtos);
        List<DealGroupRequestDto> dealGroupRequestDtos = new ArrayList<>();
        dealGroupRequestDtos.add(dealGroupRequestDto);
        PushNotificationRequestDto pushNotificationRequestDto = new PushNotificationRequestDto();
        pushNotificationRequestDto.setUserId(USER_ID);
        pushNotificationRequestDto.setDealGroups(dealGroupRequestDtos);
        return pushNotificationRequestDto;
    }

    /**
     * This method creates sample BasicDealRequestDto
     *
     * @return BasicDealRequestDto
     */
    private BasicDealRequestDto getSampleBasicDealRequestDto() {
        BasicDealRequestDto basicDealRequestDto = new BasicDealRequestDto();
        basicDealRequestDto.setId("ID");
        basicDealRequestDto.setTitle("Title");
        basicDealRequestDto.setType(NotificationType.MERCHANT);
        basicDealRequestDto.setSubTitle("Sub title");
        return basicDealRequestDto;
    }
}