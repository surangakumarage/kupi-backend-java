package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Subscribe;
import com.kupi.cc.base.domain.entity.SubscribeSearchIndex;
import com.kupi.cc.base.domain.entity.SubscriptionPlan;
import com.kupi.cc.base.enums.PlanType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.SubscribeSearchIndexRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link SubscribeSearchIndexService} class.
 */
class SubscribeSearchIndexServiceTest {

    private static final String SUBSCRIPTION_ID = "sid-1458-8652-7854-2596";
    private static final String SUBSCRIBE_ID = "sid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    private static final String MERCHANT_ID = "uid-123456789";
    private static final String MERCHANT_NAME = "NAME";
    private static final String UPDATED_AT = "updatedAt";
    private static final String SEARCH_TERM = "SEARCH TERM";
    private static final String ALL = "ALL";
    private static final int PAGE = 0;
    private static final int SIZE = 1;

    @Mock
    private SubscribeSearchIndexRepository subscribeSearchIndexRepository;
    private SubscribeSearchIndexService subscribeSearchIndexService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        subscribeSearchIndexService = new SubscribeSearchIndexService(subscribeSearchIndexRepository);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_SaveSubscribeSearchIndex() {
        String subscriptionId = "sid-12122";
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        Subscribe subscribe = new Subscribe(subscriptionId, subscriptionPlan, MERCHANT_ID, UserType.MERCHANT);
        subscribeSearchIndexService.save(subscribe, MERCHANT_NAME);
        verify(subscribeSearchIndexRepository).save(any(SubscribeSearchIndex.class));
    }

    @Test
    void Should_ThrowKupiCoreException_When_SaveSubscribeSearchIndexIntoDataBaseIsFailed() {
        String subscriptionId = "sid-123425";
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        Subscribe subscribe = new Subscribe(subscriptionId, subscriptionPlan, MERCHANT_ID, UserType.MERCHANT);
        when(subscribeSearchIndexRepository.save(any(SubscribeSearchIndex.class)))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscribeSearchIndexService.save(subscribe, MERCHANT_NAME));
        assertEquals("Saving subscribeSearchIndex into database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_RemoveDataFromDataBase_When_ValidSubscribeIdIsProvidedForRemoveSubscribeDataFromSearchIndex() {
        SubscribeSearchIndex subscribeSearchIndex = getSampleSubscribeSearchIndex();
        when(subscribeSearchIndexRepository.findById(SUBSCRIBE_ID)).thenReturn(Optional.of(subscribeSearchIndex));
        subscribeSearchIndexService.removeSubscribeDataFromSearchIndex(SUBSCRIBE_ID);
        verify(subscribeSearchIndexRepository).save(subscribeSearchIndex);
    }

    @Test
    void Should_ThrowKupiCoreException_When_RemoveSubscribeSearchIndexFromDataBaseIsFailed() {
        SubscribeSearchIndex subscribeSearchIndex = getSampleSubscribeSearchIndex();
        when(subscribeSearchIndexRepository.findById(SUBSCRIBE_ID)).thenReturn(Optional.of(subscribeSearchIndex));
        when(subscribeSearchIndexRepository.save(subscribeSearchIndex)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscribeSearchIndexService.removeSubscribeDataFromSearchIndex(SUBSCRIBE_ID));
        assertEquals("Unsubscribe from the subscribeSearchIndex by subscribed id: " + SUBSCRIBE_ID +
                " was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnSubscribeSearchIndexPage_When_SearchTermIsAllAndValidSubscriptionPlanIdAreProvided() {
        Page<SubscribeSearchIndex> subscribeSearchIndexPage = getSampleSubscribeSearchIndexPage();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(subscribeSearchIndexRepository.getAllBySubscriptionPlanIdAndIsDeletedFalse(pageable, SUBSCRIPTION_ID))
                .thenReturn(subscribeSearchIndexPage);
        assertEquals(subscribeSearchIndexPage, subscribeSearchIndexService
                .getSubscribersBySubscriptionPlanId(pageable, SUBSCRIPTION_ID, ALL));
    }

    @Test
    void Should_ReturnSubscribeSearchIndexPage_When_SearchTermIsNotAllAndValidSubscriptionPlanIdAreProvided() {
        Page<SubscribeSearchIndex> subscribeSearchIndexPage = getSampleSubscribeSearchIndexPage();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(subscribeSearchIndexRepository.getAllBySubscriptionPlanIdAndBusinessNameContainingAndIsDeletedFalse(
                pageable, SUBSCRIPTION_ID, SEARCH_TERM)).thenReturn(subscribeSearchIndexPage);
        assertEquals(subscribeSearchIndexPage, subscribeSearchIndexService
                .getSubscribersBySubscriptionPlanId(pageable, SUBSCRIPTION_ID, SEARCH_TERM));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetSubscribeSearchIndexPageFromDataBaseIsFailed() {
        getSampleSubscribeSearchIndexPage();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(subscribeSearchIndexRepository.getAllBySubscriptionPlanIdAndIsDeletedFalse(pageable, SUBSCRIPTION_ID))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscribeSearchIndexService.getSubscribersBySubscriptionPlanId(pageable, SUBSCRIPTION_ID, ALL));
        assertEquals("Returning subscribeSearchIndex by subscription id: " + SUBSCRIPTION_ID +
                " into database was failed.", kupiCoreException.getMessage());
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

    /**
     * This method creates sample SubscribeSearchIndex
     *
     * @return SubscribeSearchIndex
     */
    private SubscribeSearchIndex getSampleSubscribeSearchIndex() {
        String subscriptionId = "sid-123425";
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        Subscribe subscribe = new Subscribe(subscriptionId, subscriptionPlan, MERCHANT_ID, UserType.MERCHANT);
        return new SubscribeSearchIndex(subscribe, MERCHANT_NAME);
    }

    /**
     * This method creates sample SubscribeSearchIndex page
     *
     * @return SubscribeSearchIndexPage
     */
    private Page<SubscribeSearchIndex> getSampleSubscribeSearchIndexPage() {
        SubscribeSearchIndex subscribeSearchIndex = getSampleSubscribeSearchIndex();
        List<SubscribeSearchIndex> subscribeSearchIndexList = new ArrayList<>();
        subscribeSearchIndexList.add(subscribeSearchIndex);
        return new PageImpl<>(subscribeSearchIndexList);
    }
}