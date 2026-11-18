package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Payment;
import com.kupi.cc.base.domain.entity.Subscription;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.response.BasicBusinessResponseDto;
import com.kupi.cc.base.domain.response.BasicSubscriptionPlanResponseDto;
import com.kupi.cc.base.domain.response.SubscriptionPlanListResponseDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.PaymentStatus;
import com.kupi.cc.base.enums.SubscriptionStatus;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.repository.SubscriptionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * This class tests {@link SubscriptionService} class
 */
class SubscriptionServiceTest {

    private static final String USER_ID = "uid-123456789";
    private static final String SUBSCRIPTION_ID = "sid-123456789";
    private static final String BUSINESS_ID = "bid-123456789";
    private static final String TOKEN = "TOKEN";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String SEARCH_TERM = "SEARCH_TERM";
    private static final String FILTER = "FILTER";
    private static final String STATE_SEQUENCE = "STATE_SEQUENCE";
    private static final String ALL = "ALL";
    private static final long SUBSCRIPTION_COUNT = 50;
    private static final int PAGE = 0;
    private static final int SIZE = 250;
    private static final String ERROR = "ERROR";

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private CoreService coreService;
    @Mock
    private AuthService authService;
    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        subscriptionService = new SubscriptionService();
        ReflectionTestUtils.setField(subscriptionService, "subscriptionRepository", subscriptionRepository);
        ReflectionTestUtils.setField(subscriptionService, "coreService", coreService);
        ReflectionTestUtils.setField(subscriptionService, "authService", authService);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks(this).close();
    }

    /**
     * Start of tests for Save temporary subscription
     */
    @Test
    void Should_SaveTemporarySubscription_When_SavingTemporarySubscriptionIsSuccessful() {
        subscriptionService.saveTemporarySubscription(getSampleSubscription(), USER_ID);
        verify(subscriptionRepository, times(1)).saveAndFlush(any(Subscription.class));
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_SavingTemporarySubscriptionIsFailed() {
        Subscription subscription = getSampleSubscription();
        when(subscriptionRepository.saveAndFlush(any(Subscription.class)))
                .thenThrow(new AssertionError(ERROR));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> subscriptionService
                .saveTemporarySubscription(subscription, USER_ID));
        assertEquals(ErrorResponseStatusType.INTERNAL_SERVER_ERROR, exception.getErrorResponseType());
    }

    /**
     * Save free subscription
     */
    @Test
    void Should_SaveFreeSubscription_When_SavingFreeSubscriptionIsSuccessful() {
        when(subscriptionRepository.findByUserIdAndSubscriptionStatusActive(anyString()))
                .thenReturn(Optional.of(getSampleSubscription()));
        subscriptionService.saveFreeSubscription(getSampleSubscription(), USER_ID);
        verify(subscriptionRepository, times(2)).saveAndFlush(any(Subscription.class));
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_SavingFreeSubscriptionForErrorUpdatingOldSubscription() {
        Subscription subscription = getSampleSubscription();
        when(subscriptionRepository.findByUserIdAndSubscriptionStatusActive(anyString()))
                .thenThrow(new DataAccessException(ERROR) {
                });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> subscriptionService
                .saveFreeSubscription(subscription, USER_ID));
        assertEquals(ErrorResponseStatusType.INTERNAL_SERVER_ERROR, exception.getErrorResponseType());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_SavingFreeSubscriptionIntoDBWasFailed() {
        Subscription subscription = getSampleSubscription();

        when(subscriptionRepository.findByUserIdAndSubscriptionStatusActive(anyString()))
                .thenReturn(Optional.of(subscription));
        when(subscriptionRepository.saveAndFlush(any(Subscription.class))).thenReturn(subscription)
                .thenThrow(new AssertionError(ERROR));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> subscriptionService
                .saveFreeSubscription(subscription, USER_ID));
        verify(subscriptionRepository, times(2)).saveAndFlush(any(Subscription.class));
        assertEquals(ErrorResponseStatusType.INTERNAL_SERVER_ERROR, exception.getErrorResponseType());
    }

    /**
     * Start of tests for Get subscription from id
     */
    @Test
    void Should_ReturnSubscription_When_GettingSubscriptionFromIdIsSuccessful() {
        when(subscriptionRepository.findById(SUBSCRIPTION_ID)).thenReturn(Optional.of(getSampleSubscription()));
        assertEquals(SUBSCRIPTION_ID, subscriptionService.getSubscriptionFromId(SUBSCRIPTION_ID).getId());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_GettingSubscriptionFromIdForInvalidSubscriptionRequest() {
        when(subscriptionRepository.findById(SUBSCRIPTION_ID)).thenReturn(Optional.empty());
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                subscriptionService.getSubscriptionFromId(SUBSCRIPTION_ID));
        assertEquals("Temporary subscription plan does not exist: " + SUBSCRIPTION_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_GettingSubscriptionFromIdIsFailed() {
        when(subscriptionRepository.findById(SUBSCRIPTION_ID)).thenThrow(new DataAccessException(ERROR) {
        });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                subscriptionService.getSubscriptionFromId(SUBSCRIPTION_ID));
        assertEquals(ErrorResponseStatusType.INTERNAL_SERVER_ERROR, exception.getErrorResponseType());
    }

    /**
     * Start of tests for Save completed subscription
     */
    @Test
    void Should_SaveCompletedSubscription_When_SavingCompletedSubscriptionIsSuccessful() {
        when(subscriptionRepository.findByUserIdAndSubscriptionStatusActive(anyString()))
                .thenReturn(Optional.of(getSampleSubscription()));
        subscriptionService.saveCompletedSubscription(getSampleSubscription(), USER_ID);
        verify(subscriptionRepository, times(2)).saveAndFlush(any(Subscription.class));
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_SavingCompletedSubscriptionForErrorUpdatingOldSubscription() {
        Subscription subscription = getSampleSubscription();
        when(subscriptionRepository.findByUserIdAndSubscriptionStatusActive(anyString()))
                .thenThrow(new DataAccessException(ERROR) {
                });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> subscriptionService
                .saveCompletedSubscription(subscription, USER_ID));
        assertEquals(ErrorResponseStatusType.INTERNAL_SERVER_ERROR, exception.getErrorResponseType());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_SavingCompletedSubscriptionIntoDBWasFailed() {
        Subscription subscription = getSampleSubscription();

        when(subscriptionRepository.findByUserIdAndSubscriptionStatusActive(anyString()))
                .thenReturn(Optional.of(subscription));
        when(subscriptionRepository.saveAndFlush(any(Subscription.class))).thenReturn(subscription)
                .thenThrow(new AssertionError(ERROR));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> subscriptionService
                .saveCompletedSubscription(subscription, USER_ID));
        verify(subscriptionRepository, times(2)).saveAndFlush(any(Subscription.class));
        assertEquals(ErrorResponseStatusType.INTERNAL_SERVER_ERROR, exception.getErrorResponseType());
    }

    /**
     * Start of tests for Get subscription list by subscription id
     */
    @Test
    void Should_ReturnSubscriptionPage_When_GettingSubscriptionListBySubscriptionIdForAllSearchTermAndAllFilter() {
        Page<Subscription> subscriptionPage = getSampleSubscriptionPage();

        when(subscriptionRepository.findBySubscriptionStatus(SubscriptionStatus.ACTIVE, getSamplePageable())).thenReturn(subscriptionPage);
        assertEquals(subscriptionPage, subscriptionService.getSubscriptionListBySubscriptionId(USER_ID, ALL,
                getSamplePageable(), ALL));
    }

    @Test
    void Should_ReturnSubscriptionPage_When_GettingSubscriptionListBySubscriptionIdForAllSearchTerm() {
        Page<Subscription> subscriptionPage = getSampleSubscriptionPage();

        when(subscriptionRepository.findAllBySubscriptionStatusAndSubscriptionPlanId(any(), anyString(), any(Pageable.class)))
                .thenReturn(subscriptionPage);
        assertEquals(subscriptionPage, subscriptionService.getSubscriptionListBySubscriptionId(USER_ID, ALL,
                getSamplePageable(), FILTER));
    }

    @Test
    void Should_ReturnSubscriptionPage_When_GettingSubscriptionListBySubscriptionIdForAllFilter() {
        Page<Subscription> subscriptionPage = getSampleSubscriptionPage();

        when(subscriptionRepository.findAllBySubscriptionStatusAndTitleContains(any(), anyString(), any(Pageable.class)))
                .thenReturn(subscriptionPage);
        assertEquals(subscriptionPage, subscriptionService.getSubscriptionListBySubscriptionId(USER_ID, SEARCH_TERM,
                getSamplePageable(), ALL));
    }

    @Test
    void Should_ReturnSubscriptionPage_When_GettingSubscriptionListBySubscriptionId() {
        Page<Subscription> subscriptionPage = getSampleSubscriptionPage();

        when(subscriptionRepository.findAllBySubscriptionStatusAndSubscriptionPlanIdAndTitleContains(
                any(), anyString(), anyString(), any(Pageable.class))).thenReturn(subscriptionPage);
        assertEquals(subscriptionPage, subscriptionService.getSubscriptionListBySubscriptionId(USER_ID, SEARCH_TERM,
                getSamplePageable(), SEARCH_TERM));
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_GettingSubscriptionListBySubscriptionIdIsFailed() {
        Pageable pageable = getSamplePageable();

        when(subscriptionRepository.findBySubscriptionStatus(SubscriptionStatus.ACTIVE, getSamplePageable()))
                .thenThrow(new DataAccessException(ERROR) {
                });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> subscriptionService
                .getSubscriptionListBySubscriptionId(USER_ID, ALL, pageable, ALL));
        assertEquals("Getting subscription payment list was failed for userId: " + USER_ID, exception.getMessage());
    }

    /**
     * Start of tests for Get merchants list
     */
    @Test
    void Should_ReturnMerchantMap_When_GettingMerchantsListIsSuccessful() {
        Map<String, BasicBusinessResponseDto> basicBusinessResponseDtoMap = getSampleBasicBusinessResponseDtoMap();

        when(authService.getMerchantMap(any(BulkUserRequestDto.class), anyString()))
                .thenReturn(basicBusinessResponseDtoMap);
        assertEquals(basicBusinessResponseDtoMap, subscriptionService
                .getMerchantsList(getSampleUserIdList(), TIME_ZONE));
    }

    @Test
    void Should_ReturnEmptyMap_When_GettingMerchantsListForEmptyUserIdList() {
        assertTrue(subscriptionService.getMerchantsList(new ArrayList<>(), TIME_ZONE).isEmpty());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_GettingMerchantsListIsFailed() {
        List<String> userIdList = getSampleUserIdList();
        DataAccessException e = new DataAccessException(ERROR) {
        };

        when(authService.getMerchantMap(any(BulkUserRequestDto.class), anyString()))
                .thenThrow(e);
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> subscriptionService
                .getMerchantsList(userIdList, TIME_ZONE));
        assertEquals("Getting merchant plan was failed: " + e, exception.getMessage());

    }

    /**
     * Start of tests for Get subscription plan
     */
    @Test
    void Should_ReturnSubscriptionPlanListResponseDto_When_GettingSubscriptionPlanIsSuccessful() {
        when(coreService.getSubscriptionPlanCount(anyString(), anyString(), anyString()))
                .thenReturn(getSampleSubscriptionPlanListResponseDto());
        when(subscriptionRepository.countBySubscriptionPlanId(anyString())).thenReturn(SUBSCRIPTION_COUNT);
        assertEquals(SUBSCRIPTION_ID, subscriptionService.getSubscriptionPlan(USER_ID, TOKEN, TIME_ZONE).getPlans()
                .get(0).getId());
        assertEquals(SUBSCRIPTION_COUNT, subscriptionService.getSubscriptionPlan(USER_ID, TOKEN, TIME_ZONE).getPlans()
                .get(0).getCount());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_GettingSubscriptionPlanIsFailed() {
        DataAccessException dataAccessException = new DataAccessException(ERROR) {
        };
        when(coreService.getSubscriptionPlanCount(anyString(), anyString(), anyString()))
                .thenReturn(getSampleSubscriptionPlanListResponseDto());
        when(subscriptionRepository.countBySubscriptionPlanId(anyString())).thenThrow(dataAccessException);
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> subscriptionService
                .getSubscriptionPlan(USER_ID, TOKEN, TIME_ZONE));
        assertEquals("Getting subscription plan was failed: " + dataAccessException, exception.getMessage());
    }

    /**
     * This method returns a sample subscription
     *
     * @return Subscription
     */
    private Subscription getSampleSubscription() {
        Subscription subscription = new Subscription();
        subscription.setId(SUBSCRIPTION_ID);
        subscription.setPayments(getSamplePaymentSet());
        return subscription;
    }

    /**
     * This method returns a sample payment set
     *
     * @return Payment Set
     */
    private Set<Payment> getSamplePaymentSet() {
        Payment payment = new Payment();
        payment.setPaymentStatus(PaymentStatus.CAPTURED);
        payment.setStateSequence(STATE_SEQUENCE);

        Set<Payment> paymentSet = new HashSet<>();
        paymentSet.add(payment);
        return paymentSet;
    }

    /**
     * This method returns a sample subscription page
     *
     * @return Subscription Page
     */
    private Page<Subscription> getSampleSubscriptionPage() {
        List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(getSampleSubscription());
        return new PageImpl<>(subscriptionList);
    }

    /**
     * This method returns a sample subscription plan list response dto
     *
     * @return SubscriptionPlanListResponseDto
     */
    private SubscriptionPlanListResponseDto getSampleSubscriptionPlanListResponseDto() {
        SubscriptionPlanListResponseDto subscriptionPlanListResponseDto = new SubscriptionPlanListResponseDto();
        subscriptionPlanListResponseDto.setPlans(getSampleBasicSubscriptionPlanResponseDtoList());
        return subscriptionPlanListResponseDto;
    }

    /**
     * This method returns a sample basic subscription plan response dto list
     *
     * @return BasicSubscriptionPlanResponseDto List
     */
    private List<BasicSubscriptionPlanResponseDto> getSampleBasicSubscriptionPlanResponseDtoList() {
        List<BasicSubscriptionPlanResponseDto> basicSubscriptionPlanResponseDtoList = new ArrayList<>();
        basicSubscriptionPlanResponseDtoList.add(getSampleBasicSubscriptionPlanResponseDto());
        return basicSubscriptionPlanResponseDtoList;
    }

    /**
     * This method returns a sample basic subscription plan response dto
     *
     * @return BasicSubscriptionPlanResponseDto
     */
    private BasicSubscriptionPlanResponseDto getSampleBasicSubscriptionPlanResponseDto() {
        BasicSubscriptionPlanResponseDto basicSubscriptionPlanResponseDto = new BasicSubscriptionPlanResponseDto();
        basicSubscriptionPlanResponseDto.setId(SUBSCRIPTION_ID);
        return basicSubscriptionPlanResponseDto;
    }

    /**
     * This method returns a sample Pageable object
     *
     * @return Pageable
     */
    private Pageable getSamplePageable() {
        return PageRequest.of(PAGE, SIZE);
    }

    /**
     * This method returns a sample basic business response dto map
     *
     * @return BasicBusinessResponseDto Map
     */
    private Map<String, BasicBusinessResponseDto> getSampleBasicBusinessResponseDtoMap() {
        Map<String, BasicBusinessResponseDto> basicBusinessResponseDtoMap = new HashMap<>();
        basicBusinessResponseDtoMap.put(getSampleBasicBusinessResponseDto().getId(), getSampleBasicBusinessResponseDto());
        return basicBusinessResponseDtoMap;
    }

    /**
     * This method returns a sample basic business response dto
     *
     * @return BasicBusinessResponseDto
     */
    private BasicBusinessResponseDto getSampleBasicBusinessResponseDto() {
        BasicBusinessResponseDto basicBusinessResponseDto = new BasicBusinessResponseDto();
        basicBusinessResponseDto.setId(BUSINESS_ID);
        return basicBusinessResponseDto;
    }

    /**
     * This method returns a sample user id list
     *
     * @return UserId List
     */
    private List<String> getSampleUserIdList() {
        List<String> userIdList = new ArrayList<>();
        userIdList.add(USER_ID);
        return userIdList;
    }
}