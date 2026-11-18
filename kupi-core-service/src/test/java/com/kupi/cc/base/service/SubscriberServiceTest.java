package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Subscribe;
import com.kupi.cc.base.domain.entity.SubscriptionPlan;
import com.kupi.cc.base.domain.entity.SubscriptionRenewal;
import com.kupi.cc.base.domain.response.BusinessMerchantResponseDto;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.PlanType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.repository.SubscriberRepository;
import com.kupi.cc.base.repository.SubscriptionPlanRepository;
import com.kupi.cc.base.repository.SubscriptionRenewalRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests the {@link SubscriberService} class.
 */
class SubscriberServiceTest {

    private static final String SUBSCRIPTION_ID = "sid-1458-8652-7854-2596";
    private static final String USER_ID = "uid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    private static final String MERCHANT_ID = "uid-123456789";
    private static final String SUBSCRIBE_ID = "sid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    private static final long SUBSCRIPTION_COUNT = 1;

    private SubscriberService subscriberService;
    @Mock
    private SubscriberRepository subscriberRepository;
    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock
    private SubscribeSearchIndexService subscribeSearchIndexService;
    @Mock
    private SubscriptionRenewalRepository subscriptionRenewalRepository;
    @Mock
    private AuthUserService authUserService;
    @Mock
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        subscriberService = new SubscriberService(subscriberRepository, subscriptionPlanRepository,
                subscribeSearchIndexService, subscriptionRenewalRepository, authUserService, notificationService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnSubscriptionRenewal_When_SubscriptionPlanAvailableForGivenUserId() {
        SubscriptionRenewal subscriptionRenewal = getSampleSubscriptionRenewal();
        when(subscriptionRenewalRepository.findByUserId(USER_ID)).thenReturn(Optional.of(subscriptionRenewal));
        assertEquals(subscriptionRenewal, subscriberService.checkSubscriptionPlanForDeal(USER_ID));
    }

    @Test
    void Should_ThrowLimitExceededException_When_RemainingDealsPerMonthIsZeroForCheckSubscriptionPlanForDeal() {
        SubscriptionRenewal subscriptionRenewal = getSampleSubscriptionRenewal();
        subscriptionRenewal.setRemainingDealsPerMonth(0);
        when(subscriptionRenewalRepository.findByUserId(USER_ID)).thenReturn(Optional.of(subscriptionRenewal));
        LimitExceededException limitExceededException = assertThrows(LimitExceededException.class, () ->
                subscriberService.checkSubscriptionPlanForDeal(USER_ID));
        assertEquals("Maximum deal quota for month exceeded for userId: " + USER_ID,
                limitExceededException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_CheckSubscriptionPlanForDealIsFailed() {
        SubscriptionRenewal subscriptionRenewal = getSampleSubscriptionRenewal();
        subscriptionRenewal.setRemainingDealsPerMonth(0);
        when(subscriptionRenewalRepository.findByUserId(USER_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriberService.checkSubscriptionPlanForDeal(USER_ID));
        assertEquals("Checking subscription plan was failed for userId: " + USER_ID,
                kupiCoreException.getMessage());
    }

    @Test
    void Should_SaveSubscriptionRenewal() {
        SubscriptionRenewal subscriptionRenewal = getSampleSubscriptionRenewal();
        subscriberService.saveSubscriptionRenewal(subscriptionRenewal);
        verify(subscriptionRenewalRepository, times(1)).save(subscriptionRenewal);
    }

    @Test
    void Should_ThrowKupiCoreException_When_SaveSubscriptionRenewalIsFailed() {
        SubscriptionRenewal subscriptionRenewal = getSampleSubscriptionRenewal();
        when(subscriptionRenewalRepository.save(subscriptionRenewal)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriberService.saveSubscriptionRenewal(subscriptionRenewal));
        assertEquals("Saving subscriptionRenewal to database was failed for id: " + SUBSCRIPTION_ID,
                kupiCoreException.getMessage());
    }

    /**
     * Get subscription renewal by userId
     */
    @Test
    void Should_ReturnSubscriptionRenewal_When_GettingSubscriptionRenewalByUserIdIsSuccessful() {
        when(subscriptionRenewalRepository.findByUserId(anyString()))
                .thenReturn(Optional.of(getSampleSubscriptionRenewal()));
        assertEquals(SUBSCRIPTION_ID, subscriberService.getSubscriptionRenewalByUserId(USER_ID).getId());
    }

    @Test
    void Should_ThrowInvalidSubscribeException_When_GettingSubscriptionRenewalByUserIdForSubscriptionNotFound() {
        when(subscriptionRenewalRepository.findByUserId(anyString()))
                .thenReturn(Optional.empty());
        InvalidSubscribeException exception = assertThrows(InvalidSubscribeException.class, () ->
                subscriberService.getSubscriptionRenewalByUserId(USER_ID));
        assertEquals("Subscription not found for userId: " + USER_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingSubscriptionRenewalByUserIdIsFailed() {
        when(subscriptionRenewalRepository.findByUserId(anyString())).thenThrow(new DataAccessException("failed") {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () ->
                subscriberService.getSubscriptionRenewalByUserId(USER_ID));
        assertEquals("Checking subscription plan was failed for userId: " + USER_ID, exception.getMessage());
    }

    @Test
    void Should_CheckSubscriptionPlanHasUser_When_ValidSubscriptionPlanIdIsProvidedForCheckSubscriptionPlanHasUsers() {
        when(subscriberRepository.existsBySubscriptionPlanIdAndIsDeletedFalse(SUBSCRIPTION_ID)).thenReturn(false);
        subscriberService.checkSubscriptionPlanHasUsers(SUBSCRIPTION_ID);
        verify(subscriberRepository).existsBySubscriptionPlanIdAndIsDeletedFalse(SUBSCRIPTION_ID);
    }

    @Test
    void Should_ThrowUnsupportedDeleteSubscriptionPlanException_When_UserSubscribedThePlanForCheckSubscriptionPlanHasUsers() {
        when(subscriberRepository.existsBySubscriptionPlanIdAndIsDeletedFalse(SUBSCRIPTION_ID)).thenReturn(true);
        UnsupportedDeleteSubscriptionPlanException unsupportedDeleteSubscriptionPlanException =
                assertThrows(UnsupportedDeleteSubscriptionPlanException.class, () ->
                        subscriberService.checkSubscriptionPlanHasUsers(SUBSCRIPTION_ID));
        assertEquals("Subscription Plan has users.", unsupportedDeleteSubscriptionPlanException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_CheckSubscriptionPlanHasUserIsFailedForCheckSubscriptionPlanHasUsers() {
        when(subscriberRepository.existsBySubscriptionPlanIdAndIsDeletedFalse(SUBSCRIPTION_ID))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriberService.checkSubscriptionPlanHasUsers(SUBSCRIPTION_ID));
        assertEquals("Checking subscription plan id: " + SUBSCRIPTION_ID + " from subscription table was failed",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_CheckUserSubscribedAPlan_When_UserNotSubscribedAPlanForCheckUserIsNotInAPlan() {
        when(subscriptionRenewalRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        subscriberService.checkUserIsNotInAPlan(USER_ID);
        verify(subscriptionRenewalRepository).findByUserId(USER_ID);
    }

    @Test
    void Should_ThrowExistingSubscribedUserException_When_UserSubscribedAPlanForCheckUserIsNotInAPlan() {
        SubscriptionRenewal subscriptionRenewal = getSampleSubscriptionRenewal();
        when(subscriptionRenewalRepository.findByUserId(USER_ID)).thenReturn(Optional.of(subscriptionRenewal));
        ExistingSubscribedUserException existingSubscribedUserException = assertThrows(ExistingSubscribedUserException.class, () ->
                subscriberService.checkUserIsNotInAPlan(USER_ID));
        assertEquals("User already subscribe a plan. userId: " + USER_ID,
                existingSubscribedUserException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_CheckUserIsNotInAPlanIsFailed() {
        getSampleSubscriptionRenewal();
        when(subscriptionRenewalRepository.findByUserId(USER_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriberService.checkUserIsNotInAPlan(USER_ID));
        assertEquals("Reading Subscription was failed", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnSubscribe_When_UserSubscribedAPlanForSetSubscribe() {
        String subscriptionId = "sid=12344";
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        Subscribe subscribe = new Subscribe(subscriptionId, subscriptionPlan, businessMerchantResponseDto.getMerchantId(),
                UserType.MERCHANT);
        assertEquals(subscribe.getUserId(),
                subscriberService.setSubscribe(subscriptionId, subscriptionPlan, businessMerchantResponseDto,
                        UserType.MERCHANT).getUserId());
    }

    @Test
    void Should_ThrowUnsupportedUserTypeException_When_UnsupportedUserTypeIsProvidedForSetSubscribe() {
        String subscriptionId = "sid=12344";
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        UnsupportedUserTypeException unsupportedUserTypeException = assertThrows(UnsupportedUserTypeException.class, () ->
                subscriberService.setSubscribe(subscriptionId, subscriptionPlan, businessMerchantResponseDto, UserType.BANK));
        assertEquals("Unsupported user type to subscribe the plan id: " + subscriptionPlan.getId(),
                unsupportedUserTypeException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_UnsupportedUserTypeIsProvidedForSetSubscribe() {
        String subscriptionId = "sid=12344";
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        BusinessMerchantResponseDto merchantResponseDto = getSampleBusinessMerchantResponseDto();
        doThrow(new DataAccessException("ERROR") {
        }).when(subscribeSearchIndexService).save(any(Subscribe.class), anyString());
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriberService.setSubscribe(subscriptionId, subscriptionPlan, merchantResponseDto, UserType.MERCHANT));
        assertEquals("Setting Subscription was failed for userId: " + merchantResponseDto.getMerchantId() +
                " and planId: " + subscriptionPlan.getId(), kupiCoreException.getMessage());
    }

    @Test
    void Should_Unsubscribe_When_ValidSubscribeIdIsProvidedForUnsubscribeAPlan() {
        String subscriptionId = "sid=12344";
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        Subscribe subscribe = new Subscribe(subscriptionId, subscriptionPlan, MERCHANT_ID, UserType.MERCHANT);
        SubscriptionRenewal subscriptionRenewal = getSampleSubscriptionRenewal();
        when(subscriberRepository.findById(SUBSCRIBE_ID)).thenReturn(Optional.of(subscribe));
        when(subscriptionRenewalRepository.findById(SUBSCRIBE_ID)).thenReturn(Optional.of(subscriptionRenewal));
        subscriberService.unsubscribeAPlan(SUBSCRIBE_ID, UserType.MERCHANT);
        verify(subscriberRepository).save(subscribe);
        verify(subscribeSearchIndexService).removeSubscribeDataFromSearchIndex(SUBSCRIBE_ID);

    }

    @Test
    void Should_ThrowUnsupportedUserTypeException_When_UserTypeNotSupportToUnsubscribe() {
        String subscriptionId = "sid=12344";
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        Subscribe subscribe = new Subscribe(subscriptionId, subscriptionPlan, MERCHANT_ID, UserType.MERCHANT);
        when(subscriberRepository.findById(SUBSCRIBE_ID)).thenReturn(Optional.of(subscribe));
        UnsupportedUserTypeException unsupportedUserTypeException = assertThrows(UnsupportedUserTypeException.class, () ->
                subscriberService.unsubscribeAPlan(SUBSCRIBE_ID, UserType.BANK));
        assertEquals("Unsupported user type to unsubscribe the plan.",
                unsupportedUserTypeException.getMessage());
    }

    @Test
    void Should_ThrowInvalidSubscribeException_When_InvalidSubscribeIdProvidedForGetSubscribeById() {
        when(subscriberRepository.findById(SUBSCRIBE_ID)).thenReturn(Optional.empty());
        InvalidSubscribeException invalidSubscribeException = assertThrows(InvalidSubscribeException.class, () ->
                subscriberService.unsubscribeAPlan(SUBSCRIBE_ID, UserType.MERCHANT));
        assertEquals("Invalid Subscribe id: " + SUBSCRIBE_ID, invalidSubscribeException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetSubscribeFromDataBaseIsFailedForUnsubscribeAPlan() {
        when(subscriberRepository.findById(SUBSCRIBE_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriberService.unsubscribeAPlan(SUBSCRIBE_ID, UserType.MERCHANT));
        assertEquals("Returning subscribe by id: " + SUBSCRIBE_ID + " from database was failed",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ThrowInvalidSubscribeException_When_InvalidSubscribeIdProvidedForGetSubscriptionRenewalById() {
        String subscriptionId = "sid=12344";
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        Subscribe subscribe = new Subscribe(subscriptionId, subscriptionPlan, MERCHANT_ID, UserType.MERCHANT);
        when(subscriberRepository.findById(SUBSCRIBE_ID)).thenReturn(Optional.of(subscribe));
        when(subscriptionRenewalRepository.findById(SUBSCRIBE_ID)).thenReturn(Optional.empty());
        InvalidSubscribeException invalidSubscribeException = assertThrows(InvalidSubscribeException.class, () ->
                subscriberService.unsubscribeAPlan(SUBSCRIBE_ID, UserType.MERCHANT));
        assertEquals("Subscription not found for subscriptionId: " + SUBSCRIBE_ID,
                invalidSubscribeException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetSubscriptionRenewalFromDataBaseIsFailedForUnsubscribeAPlan() {
        String subscriptionId = "sid=12344";
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        Subscribe subscribe = new Subscribe(subscriptionId, subscriptionPlan, MERCHANT_ID, UserType.MERCHANT);
        when(subscriberRepository.findById(SUBSCRIBE_ID)).thenReturn(Optional.of(subscribe));
        when(subscriptionRenewalRepository.findById(SUBSCRIBE_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriberService.unsubscribeAPlan(SUBSCRIBE_ID, UserType.MERCHANT));
        assertEquals("Checking subscription plan was failed for subscriptionId: " + SUBSCRIBE_ID,
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_UnsubscribeAPlanIsFailed() {
        String subscriptionId = "sid=12344";
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        Subscribe subscribe = new Subscribe(subscriptionId, subscriptionPlan, MERCHANT_ID, UserType.MERCHANT);
        SubscriptionRenewal subscriptionRenewal = getSampleSubscriptionRenewal();
        when(subscriberRepository.findById(SUBSCRIBE_ID)).thenReturn(Optional.of(subscribe));
        when(subscriptionRenewalRepository.findById(SUBSCRIBE_ID)).thenReturn(Optional.of(subscriptionRenewal));
        when(subscriberRepository.save(subscribe)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriberService.unsubscribeAPlan(SUBSCRIBE_ID, UserType.MERCHANT));
        assertEquals("Unsubscribe the subscription plan for subscribed id: " + SUBSCRIBE_ID + " was failed.",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnOptionalSubscribe_When_ValidUserIdIsProvidedForGetSubscribeByUserId() {
        String subscriptionId = "sid=12344";
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        Subscribe subscribe = new Subscribe(subscriptionId, subscriptionPlan, MERCHANT_ID, UserType.MERCHANT);
        var optionalSubscribe = Optional.of(subscribe);
        when(subscriberRepository.getByUserIdAndIsDeletedFalse(USER_ID)).thenReturn(optionalSubscribe);
        assertEquals(optionalSubscribe, subscriberService.getSubscribeByUserId(USER_ID));
    }

//    @Test
    void Should_ThrowKupiCoreException_When_GetSubscribeFromDataBaseIsFailed() {
        when(subscriberRepository.getByUserIdAndIsDeletedFalse(USER_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriberService.getSubscribeByUserId(USER_ID));
        assertEquals("Returning subscribe by id: " + USER_ID + " from database was failed",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnSubscriberCount_When_ValidSubscriptionPlanIdIsProvidedForGetSubscribersCountBySubscriptionPlanId() {
        when(subscriberRepository.countSubscribeBySubscriptionPlanIdAndIsDeletedFalse(SUBSCRIPTION_ID))
                .thenReturn(SUBSCRIPTION_COUNT);
        assertEquals(SUBSCRIPTION_COUNT, subscriberService.getSubscribersCountBySubscriptionPlanId(SUBSCRIPTION_ID));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetSubscriberCountFromDataBaseIsFailed() {
        when(subscriberRepository.countSubscribeBySubscriptionPlanIdAndIsDeletedFalse(SUBSCRIPTION_ID))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriberService.getSubscribersCountBySubscriptionPlanId(SUBSCRIPTION_ID));
        assertEquals("Returning subscribers count by subscription plan id: " + SUBSCRIPTION_ID +
                        " from database was failed", kupiCoreException.getMessage());
    }

    /**
     * This method creates sample subscription renewal
     *
     * @return SubscriptionRenewal
     */
    private SubscriptionRenewal getSampleSubscriptionRenewal() {
        SubscriptionRenewal subscriptionRenewal = new SubscriptionRenewal();
        subscriptionRenewal.setId(SUBSCRIPTION_ID);
        subscriptionRenewal.setSubscriptionPlanId("spid-c9d5b89a-5125-4838-b3cb-58bc8619589f");
        subscriptionRenewal.setPlanType(PlanType.MONTHLY);
        subscriptionRenewal.setUnsubscribed(false);
        subscriptionRenewal.setRemainingDealsPerMonth(10);
        return subscriptionRenewal;
    }

    /**
     * This method creates BusinessMerchantResponseDto
     *
     * @return BusinessMerchantResponseDto
     */
    private BusinessMerchantResponseDto getSampleBusinessMerchantResponseDto() {
        BusinessMerchantResponseDto businessMerchantResponseDto = new BusinessMerchantResponseDto();
        businessMerchantResponseDto.setBusinessId("uid-001");
        businessMerchantResponseDto.setBusinessName("Roy");
        businessMerchantResponseDto.setImageUrl("goo.le/image");
        businessMerchantResponseDto.setOwnerName("Roy Owner");
        businessMerchantResponseDto.setAddress("Colombo");
        businessMerchantResponseDto.setMerchantId(MERCHANT_ID);
        businessMerchantResponseDto.setWebSite("goo.le/image2");
        businessMerchantResponseDto.setApprovalStatus(ApprovalStatus.APPROVED);
        businessMerchantResponseDto.setActive(true);
        return businessMerchantResponseDto;
    }

    /**
     * This method creates sample SubscriptionPlan
     *
     * @return SubscriptionPlan
     */
    private SubscriptionPlan getSampleSubscriptionPlan() {
        SubscriptionPlan subscriptionPlan = new SubscriptionPlan();
        subscriptionPlan.setId(SUBSCRIPTION_ID);
        subscriptionPlan.setTitle("Title");
        subscriptionPlan.setSubTitle("Subtitle");
        subscriptionPlan.setPlanType(PlanType.MONTHLY);
        subscriptionPlan.setNoOfDealsOfTheDayEventsPerMonth(1);
        subscriptionPlan.setNoOfDealsPerMonth(1);
        subscriptionPlan.setNoOfDealsOfTheDayPerEvent(1);
        subscriptionPlan.setUserRole(UserType.MERCHANT);
        subscriptionPlan.setFree(false);
        subscriptionPlan.setAmount(BigDecimal.ONE);
        subscriptionPlan.setDescription("Description");
        subscriptionPlan.setTermsAndCondition("terms and conditions");
        return subscriptionPlan;
    }
}