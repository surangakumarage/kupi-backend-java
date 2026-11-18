package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.*;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.request.DealsOfTheDayEventRequestDto;
import com.kupi.cc.base.domain.response.BusinessMerchantResponseDto;
import com.kupi.cc.base.domain.response.DealsOfTheDayEventSummaryResponseDto;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.enums.DealsOfTheDayHistoryFilter;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.aop.AopInvocationException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link DealsOfTheDayService} class
 */
class DealsOfTheDayServiceTest {

    private static final String DEAL_ID = "did-123456789";
    private static final String CATEGORY_ID = "cid-123456789";
    private static final String MERCHANT_ID = "uid-123456789";
    private static final String EVENT_ID = "dodeid-123456789";
    private static final String SEARCH_TERM = "Softlogic";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String ALL = "ALL";
    private static final String FAILED = "failed";
    private static final String TITLE = "TITLE";
    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final int REMAINING_DEALS_OF_THE_DAY_EVENTS_PER_MONTH = 999999999;
    private static final long NO_OF_BANK_EVENTS = 3;
    private static final long NO_OF_MERCHANT_EVENTS = 8;
    private static final long NO_OF_TOTAL_EVENTS = 11;
    private static final long NO_OF_BANKS = 22;
    private static final long NO_OF_MERCHANTS = 15;
    private static final long DATE_IN_MILLISECONDS = 1659465000000L;

    @Mock
    private DealsOfTheDayRepository dealsOfTheDayRepository;
    @Mock
    private BankDealsOfTheDayRepository bankDealsOfTheDayRepository;
    @Mock
    private DealRepository dealRepository;
    @Mock
    private BankDealRepository bankDealRepository;
    @Mock
    private DealSearchService dealSearchService;
    @Mock
    private DealsOfTheDaySearchIndexService dealsOfTheDaySearchIndexService;
    @Mock
    private DealSearchIndexRepository dealSearchIndexRepository;
    @Mock
    private DealService dealService;
    @Mock
    private DealsOfTheDayEventRepository dealsOfTheDayEventRepository;
    @Mock
    private AuthUserService authUserService;
    @Mock
    private DealsOfTheDayEventSearchIndexRepository dealsOfTheDayEventSearchIndexRepository;
    @Mock
    private SubscriberService subscriberService;
    private DealsOfTheDayService dealsOfTheDayService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        dealsOfTheDayService = new DealsOfTheDayService(3,
                dealsOfTheDayRepository, bankDealsOfTheDayRepository, dealRepository, bankDealRepository,
                dealSearchService, dealsOfTheDaySearchIndexService, dealSearchIndexRepository, dealService,
                dealsOfTheDayEventRepository, authUserService, dealsOfTheDayEventSearchIndexRepository, subscriberService);
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Deals of the day page for merchant
     */
    @Test
    void Should_ReturnDealPage_When_GettingDealsOfTheDayPageForMerchantForMerchantIsSuccessful() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);

        when(dealsOfTheDaySearchIndexService.getAllSearchDeals(any(Pageable.class), anyString(), anyString(),
                anyString(), any(UserType.class))).thenReturn(getSampleDealOfTheDaySearchIndexPage());
        when(dealRepository.findById(DEAL_ID)).thenReturn(Optional.of(getSampleDeal()));

        assertEquals(DEAL_ID, dealsOfTheDayService.dealsOfTheDayPageForMerchant(pageable, CATEGORY_ID, MERCHANT_ID,
                SEARCH_TERM, UserType.MERCHANT).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnDealPage_When_GettingDealsOfTheDayPageForMerchantForBankIsSuccessful() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);

        when(dealsOfTheDaySearchIndexService.getAllSearchDeals(any(Pageable.class), anyString(), anyString(),
                anyString(), any(UserType.class))).thenReturn(getSampleDealOfTheDaySearchIndexPage());
        when(bankDealRepository.findById(DEAL_ID)).thenReturn(Optional.of(getSampleBankDeal()));

        assertEquals(DEAL_ID, dealsOfTheDayService.dealsOfTheDayPageForMerchant(pageable, CATEGORY_ID, MERCHANT_ID,
                SEARCH_TERM, UserType.BANK).getContent().get(0).getId());
    }

    @Test
    void Should_ThrowInvalidDealException_When_GettingDealsOfTheDayPageForMerchantForMerchantIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);

        when(dealsOfTheDaySearchIndexService.getAllSearchDeals(any(Pageable.class), anyString(), anyString(),
                anyString(), any(UserType.class))).thenReturn(getSampleDealOfTheDaySearchIndexPage());
        when(dealRepository.findById(DEAL_ID)).thenReturn(Optional.empty());

        InvalidDealException exception = assertThrows(InvalidDealException.class, () ->
                dealsOfTheDayService.dealsOfTheDayPageForMerchant(pageable, CATEGORY_ID, MERCHANT_ID, SEARCH_TERM,
                        UserType.MERCHANT));
        assertEquals("Invalid deal Id: " + DEAL_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidDealException_When_GettingDealsOfTheDayPageForMerchantForBankIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);

        when(dealsOfTheDaySearchIndexService.getAllSearchDeals(any(Pageable.class), anyString(), anyString(),
                anyString(), any(UserType.class))).thenReturn(getSampleDealOfTheDaySearchIndexPage());
        when(bankDealRepository.findById(DEAL_ID)).thenReturn(Optional.empty());

        InvalidDealException exception = assertThrows(InvalidDealException.class, () ->
                dealsOfTheDayService.dealsOfTheDayPageForMerchant(pageable, CATEGORY_ID, MERCHANT_ID, SEARCH_TERM,
                        UserType.BANK));
        assertEquals("Invalid deal Id: " + DEAL_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingDealsOfTheDayPageForMerchantIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);

        when(dealsOfTheDaySearchIndexService.getAllSearchDeals(any(Pageable.class), anyString(), anyString(),
                anyString(), any(UserType.class))).thenReturn(getSampleDealOfTheDaySearchIndexPage());
        when(dealRepository.findById(DEAL_ID)).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () ->
                dealsOfTheDayService.dealsOfTheDayPageForMerchant(pageable, CATEGORY_ID, MERCHANT_ID, SEARCH_TERM,
                        UserType.MERCHANT));
        assertEquals("Read deals of the day from database was failed.", exception.getMessage());
    }

    /**
     * Get deals of the day history
     */
    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForMerchantForTodayAndAllUserIdAndSearchTerm() {
        when(dealsOfTheDayRepository.getDealsOfTheDayByDateAndSearchTerm(any(Pageable.class), anyString(), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, SEARCH_TERM,
                DealsOfTheDayHistoryFilter.TODAY, ALL, UserType.MERCHANT).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForBankForTodayAndAllUserIdAndSearchTerm() {
        when(bankDealsOfTheDayRepository.getDealsOfTheDayByDateAndSearchTerm(any(Pageable.class), anyString(), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, SEARCH_TERM,
                DealsOfTheDayHistoryFilter.TODAY, ALL, UserType.BANK).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForMerchantForTodayAndUserIdAndAllSearchTerm() {
        when(dealsOfTheDayRepository.getDealsByMerchantIdAndAppliedOn(any(Pageable.class), anyString(), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, ALL,
                DealsOfTheDayHistoryFilter.TODAY, MERCHANT_ID, UserType.MERCHANT).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForBankForTodayAndUserIdAndAllSearchTerm() {
        when(bankDealsOfTheDayRepository.getDealsByBankIdAndAppliedOn(any(Pageable.class), anyString(), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, ALL,
                DealsOfTheDayHistoryFilter.TODAY, MERCHANT_ID, UserType.BANK).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForMerchantForTodayAndAllUserIdAndAllSearchTerm() {
        when(dealsOfTheDayRepository.getDealByAppliedOn(any(Pageable.class), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, ALL,
                DealsOfTheDayHistoryFilter.TODAY, ALL, UserType.MERCHANT).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForBankForTodayAndAllUserIdAndAllSearchTerm() {
        when(bankDealsOfTheDayRepository.getDealByAppliedOn(any(Pageable.class), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, ALL,
                DealsOfTheDayHistoryFilter.TODAY, ALL, UserType.BANK).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForMerchantForTodayAndUserIdAndSearchTerm() {
        when(dealsOfTheDayRepository.getDealsOfTheDayByMerchantDateAndSearchTerm(any(Pageable.class), anyString(),
                anyString(), any(Date.class))).thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, SEARCH_TERM,
                DealsOfTheDayHistoryFilter.TODAY, MERCHANT_ID, UserType.MERCHANT).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForBankForTodayAndUserIdAndSearchTerm() {
        when(bankDealsOfTheDayRepository.getDealsOfTheDayByBankIdDateAndSearchTerm(any(Pageable.class), anyString(),
                anyString(), any(Date.class))).thenReturn(new PageImpl<>(new ArrayList<>()));

        assertEquals(new ArrayList<>(), dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, SEARCH_TERM,
                DealsOfTheDayHistoryFilter.TODAY, MERCHANT_ID, UserType.BANK).get(0).getDeals());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForMerchantForYesterdayAndAllUserIdAndSearchTerm() {
        when(dealsOfTheDayRepository.getDealsOfTheDayByDateAndSearchTerm(any(Pageable.class), anyString(), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, SEARCH_TERM,
                DealsOfTheDayHistoryFilter.YESTERDAY, ALL, UserType.MERCHANT).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForBankForYesterdayAndAllUserIdAndSearchTerm() {
        when(bankDealsOfTheDayRepository.getDealsOfTheDayByDateAndSearchTerm(any(Pageable.class), anyString(), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, SEARCH_TERM,
                DealsOfTheDayHistoryFilter.YESTERDAY, ALL, UserType.BANK).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForMerchantForYesterdayAndUserIdAndAllSearchTerm() {
        when(dealsOfTheDayRepository.getDealsByMerchantIdAndAppliedOn(any(Pageable.class), anyString(), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, ALL,
                DealsOfTheDayHistoryFilter.YESTERDAY, MERCHANT_ID, UserType.MERCHANT).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForBankForYesterdayAndUserIdAndAllSearchTerm() {
        when(bankDealsOfTheDayRepository.getDealsByBankIdAndAppliedOn(any(Pageable.class), anyString(), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, ALL,
                DealsOfTheDayHistoryFilter.YESTERDAY, MERCHANT_ID, UserType.BANK).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForMerchantForYesterdayAndAllUserIdAndAllSearchTerm() {
        when(dealsOfTheDayRepository.getDealByAppliedOn(any(Pageable.class), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, ALL,
                DealsOfTheDayHistoryFilter.YESTERDAY, ALL, UserType.MERCHANT).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForBankForYesterdayAndAllUserIdAndAllSearchTerm() {
        when(bankDealsOfTheDayRepository.getDealByAppliedOn(any(Pageable.class), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, ALL,
                DealsOfTheDayHistoryFilter.YESTERDAY, ALL, UserType.BANK).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForMerchantForYesterdayAndUserIdAndSearchTerm() {
        when(dealsOfTheDayRepository.getDealsOfTheDayByMerchantDateAndSearchTerm(any(Pageable.class), anyString(),
                anyString(), any(Date.class))).thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, SEARCH_TERM,
                DealsOfTheDayHistoryFilter.YESTERDAY, MERCHANT_ID, UserType.MERCHANT).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForBankForYesterdayAndUserIdAndSearchTerm() {
        when(bankDealsOfTheDayRepository.getDealsOfTheDayByBankIdDateAndSearchTerm(any(Pageable.class), anyString(),
                anyString(), any(Date.class))).thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, SEARCH_TERM,
                DealsOfTheDayHistoryFilter.YESTERDAY, MERCHANT_ID, UserType.BANK).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForMerchantForLastWeekAndAllUserIdAndSearchTerm() {
        when(dealsOfTheDayRepository.getDealsOfTheDayByDateAndSearchTerm(any(Pageable.class), anyString(), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, SEARCH_TERM,
                DealsOfTheDayHistoryFilter.LAST_WEEK, ALL, UserType.MERCHANT).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForBankForLastWeekAndAllUserIdAndSearchTerm() {
        when(bankDealsOfTheDayRepository.getDealsOfTheDayByDateAndSearchTerm(any(Pageable.class), anyString(), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, SEARCH_TERM,
                DealsOfTheDayHistoryFilter.LAST_WEEK, ALL, UserType.BANK).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForMerchantForLastWeekAndUserIdAndAllSearchTerm() {
        when(dealsOfTheDayRepository.getDealsByMerchantIdAndAppliedOn(any(Pageable.class), anyString(), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, ALL,
                DealsOfTheDayHistoryFilter.LAST_WEEK, MERCHANT_ID, UserType.MERCHANT).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForBankForLastWeekAndUserIdAndAllSearchTerm() {
        when(bankDealsOfTheDayRepository.getDealsByBankIdAndAppliedOn(any(Pageable.class), anyString(), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, ALL,
                DealsOfTheDayHistoryFilter.LAST_WEEK, MERCHANT_ID, UserType.BANK).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForMerchantForLastWeekAndAllUserIdAndAllSearchTerm() {
        when(dealsOfTheDayRepository.getDealByAppliedOn(any(Pageable.class), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, ALL,
                DealsOfTheDayHistoryFilter.LAST_WEEK, ALL, UserType.MERCHANT).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForBankForLastWeekAndAllUserIdAndAllSearchTerm() {
        when(bankDealsOfTheDayRepository.getDealByAppliedOn(any(Pageable.class), any(Date.class)))
                .thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, ALL,
                DealsOfTheDayHistoryFilter.LAST_WEEK, ALL, UserType.BANK).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForMerchantForLastWeekAndUserIdAndSearchTerm() {
        when(dealsOfTheDayRepository.getDealsOfTheDayByMerchantDateAndSearchTerm(any(Pageable.class), anyString(),
                anyString(), any(Date.class))).thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, SEARCH_TERM,
                DealsOfTheDayHistoryFilter.LAST_WEEK, MERCHANT_ID, UserType.MERCHANT).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayBasicEventResponseDtoList_When_GettingDealsOfTheDayHistoryForBankForLastWeekAndUserIdAndSearchTerm() {
        when(bankDealsOfTheDayRepository.getDealsOfTheDayByBankIdDateAndSearchTerm(any(Pageable.class), anyString(),
                anyString(), any(Date.class))).thenReturn(getSampleDealPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        assertEquals(DEAL_ID, dealsOfTheDayService.getDealsOfTheDayHistory(TIME_ZONE, SEARCH_TERM,
                DealsOfTheDayHistoryFilter.LAST_WEEK, MERCHANT_ID, UserType.BANK).get(0).getDeals().get(0).getId());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingDealsOfTheDayHistoryIsFailed() {
        when(dealsOfTheDayRepository.getDealsOfTheDayByDateAndSearchTerm(any(Pageable.class), anyString(), any(Date.class)))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> dealsOfTheDayService
                .getDealsOfTheDayHistory(TIME_ZONE, SEARCH_TERM, DealsOfTheDayHistoryFilter.TODAY, ALL,
                        UserType.MERCHANT));

        assertEquals("Failed to get deals of the day history from db", exception.getMessage());
    }

    /**
     * Get upcoming deals of the day events
     */
    @Test
    void Should_ReturnDealsOfTheDayEventSearchIndexPage_When_GettingUpcomingDealsOfTheDayEventsForAllUserIdAndAllSearchTerm() {
        when(dealsOfTheDayEventSearchIndexRepository.findByValidOnGreaterThanAndUserType(any(Pageable.class),
                any(Date.class), any(UserType.class))).thenReturn(getSampleDealsOfTheDayEventSearchIndexPage());
        assertEquals(DEAL_ID, dealsOfTheDayService.getUpcomingDealsOfTheDayEvents(PAGE, SIZE, UserType.MERCHANT, ALL,
                ALL).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayEventSearchIndexPage_When_GettingUpcomingDealsOfTheDayEventsForUserIdAndAllSearchTerm() {
        when(dealsOfTheDayEventSearchIndexRepository.findByValidOnGreaterThanAndUserIdAndUserType(any(Pageable.class),
                any(Date.class), anyString(), any(UserType.class))).thenReturn(getSampleDealsOfTheDayEventSearchIndexPage());
        assertEquals(DEAL_ID, dealsOfTheDayService.getUpcomingDealsOfTheDayEvents(PAGE, SIZE, UserType.MERCHANT,
                MERCHANT_ID, ALL).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayEventSearchIndexPage_When_GettingUpcomingDealsOfTheDayEventsForAllUserIdAndSearchTerm() {
        when(dealsOfTheDayEventSearchIndexRepository
                .findByValidOnGreaterThanAndUserTypeAndDealTitlesContainsOrUserNameContains(any(Pageable.class),
                        any(Date.class), any(UserType.class), anyString(), anyString()))
                .thenReturn(getSampleDealsOfTheDayEventSearchIndexPage());
        assertEquals(DEAL_ID, dealsOfTheDayService.getUpcomingDealsOfTheDayEvents(PAGE, SIZE, UserType.MERCHANT,
                ALL, SEARCH_TERM).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnDealsOfTheDayEventSearchIndexPage_When_GettingUpcomingDealsOfTheDayEventsForUserIdAndSearchTerm() {
        when(dealsOfTheDayEventSearchIndexRepository
                .findByValidOnGreaterThanAndUserIdAndUserTypeAndDealTitlesContains(any(Pageable.class),
                        any(Date.class), anyString(), any(UserType.class), anyString()))
                .thenReturn(getSampleDealsOfTheDayEventSearchIndexPage());
        assertEquals(DEAL_ID, dealsOfTheDayService.getUpcomingDealsOfTheDayEvents(PAGE, SIZE, UserType.MERCHANT,
                MERCHANT_ID, SEARCH_TERM).getContent().get(0).getId());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingUpcomingDealsOfTheDayEventsIsFailed() {
        when(dealsOfTheDayEventSearchIndexRepository.findByValidOnGreaterThanAndUserType(any(Pageable.class),
                any(Date.class), any(UserType.class))).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> dealsOfTheDayService
                .getUpcomingDealsOfTheDayEvents(PAGE, SIZE, UserType.MERCHANT, ALL, ALL));
        assertEquals("Getting upcoming deals of the day events from db was failed.", exception.getMessage());
    }

    /**
     * Get deals of the day summary for admin
     */
    @Test
    void Should_ReturnDealsOfTheDayEventSummaryResponseDto_When_GettingDealsOfTheDaySummaryForAdminIsSuccessful() {
        when(dealsOfTheDayEventSearchIndexRepository.countByValidOnGreaterThanAndUserTypeEquals(any(Date.class),
                eq(UserType.BANK))).thenReturn(NO_OF_BANK_EVENTS);
        when(dealsOfTheDayEventSearchIndexRepository.countByValidOnGreaterThanAndUserTypeEquals(any(Date.class),
                eq(UserType.MERCHANT))).thenReturn(NO_OF_MERCHANT_EVENTS);
        when(dealsOfTheDayEventSearchIndexRepository.getUserCountByUserType(eq(UserType.BANK), any(Date.class)))
                .thenReturn(NO_OF_BANKS);
        when(dealsOfTheDayEventSearchIndexRepository.getUserCountByUserType(eq(UserType.MERCHANT), any(Date.class)))
                .thenReturn(NO_OF_MERCHANTS);

        assertEquals(getSampleDealsOfTheDayEventSummaryResponseDto().getNoOfBankEvents(), dealsOfTheDayService
                .getDealsOfTheDaySummaryForAdmin().getNoOfBankEvents());
        assertEquals(getSampleDealsOfTheDayEventSummaryResponseDto().getNoOfMerchantEvents(), dealsOfTheDayService
                .getDealsOfTheDaySummaryForAdmin().getNoOfMerchantEvents());
        assertEquals(getSampleDealsOfTheDayEventSummaryResponseDto().getNoOfTotalEvents(), dealsOfTheDayService
                .getDealsOfTheDaySummaryForAdmin().getNoOfTotalEvents());
        assertEquals(getSampleDealsOfTheDayEventSummaryResponseDto().getNoOfBanks(), dealsOfTheDayService
                .getDealsOfTheDaySummaryForAdmin().getNoOfBanks());
        assertEquals(getSampleDealsOfTheDayEventSummaryResponseDto().getNoOfMerchants(), dealsOfTheDayService
                .getDealsOfTheDaySummaryForAdmin().getNoOfMerchants());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingDealsOfTheDaySummaryForAdminIsFailed() {
        when(dealsOfTheDayEventSearchIndexRepository.countByValidOnGreaterThanAndUserTypeEquals(any(Date.class),
                any(UserType.class))).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () ->
                dealsOfTheDayService.getDealsOfTheDaySummaryForAdmin());
        assertEquals("Returning deals of the day summary for admin was failed", exception.getMessage());
    }

    /**
     * Add deals of the day event
     */
    @Test
    void Should_ReturnDealsOfTheDayEvent_When_AddingDealsOfTheDayEventIsSuccessful() {
        DealsOfTheDayEvent dealsOfTheDayEvent = new DealsOfTheDayEvent(getSampleDealsOfTheDayEventRequestDto(),
                MERCHANT_ID, UserType.BANK);

        when(dealsOfTheDayEventRepository.findByUserIdAndValidOn(anyString(), any(Date.class)))
                .thenReturn(Optional.empty());
        when(dealsOfTheDayEventRepository.findByValidOnAndUserType(any(Date.class), any(UserType.class)))
                .thenReturn(getSampleDealsOfTheDayEventList());
        when(dealSearchIndexRepository.findById(DEAL_ID)).thenReturn(Optional.of(getSampleDealSearchIndex()));
        when(subscriberService.getSubscriptionRenewalByUserId(anyString())).thenReturn(getSampleSubscriptionRenewal());
        when(dealSearchIndexRepository.findAllById(anySet())).thenReturn(getSampleDealSearchIndexList());

        dealsOfTheDayService.addDealsOfTheDayEvent(getSampleDealsOfTheDayEventRequestDto(), UserType.BANK,
                getSampleBusinessMerchantResponseDto());
        verify(dealsOfTheDayEventRepository).save(any(DealsOfTheDayEvent.class));
        verify(dealsOfTheDayEventSearchIndexRepository).save(any(DealsOfTheDayEventSearchIndex.class));
        doNothing().when(subscriberService).saveSubscriptionRenewal(getSampleSubscriptionRenewal());

        assertEquals(dealsOfTheDayEvent.getDeals(), dealsOfTheDayService
                .addDealsOfTheDayEvent(getSampleDealsOfTheDayEventRequestDto(), UserType.BANK,
                        getSampleBusinessMerchantResponseDto()).getDeals());
        assertEquals(dealsOfTheDayEvent.getValidOn().toString(), dealsOfTheDayService
                .addDealsOfTheDayEvent(getSampleDealsOfTheDayEventRequestDto(), UserType.BANK,
                        getSampleBusinessMerchantResponseDto()).getValidOn().toString());
        assertEquals(MERCHANT_ID, dealsOfTheDayService.addDealsOfTheDayEvent(getSampleDealsOfTheDayEventRequestDto(),
                UserType.BANK, getSampleBusinessMerchantResponseDto()).getUserId());
        assertEquals(UserType.BANK, dealsOfTheDayService.addDealsOfTheDayEvent(getSampleDealsOfTheDayEventRequestDto(),
                UserType.BANK, getSampleBusinessMerchantResponseDto()).getUserType());
    }

    @Test
    void Should_ThrowExistingEventException_When_AddingDealsOfTheDayEventForExistingEvent() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();

        when(dealsOfTheDayEventRepository.findByUserIdAndValidOn(anyString(), any(Date.class)))
                .thenReturn(Optional.of(getSampleDealsOfTheDayEvent()));
        when(dealsOfTheDayEventRepository.findByValidOnAndUserType(any(Date.class), any(UserType.class)))
                .thenReturn(new ArrayList<>());
        when(dealSearchIndexRepository.findById(DEAL_ID)).thenReturn(Optional.of(getSampleDealSearchIndex()));
        when(subscriberService.getSubscriptionRenewalByUserId(anyString())).thenReturn(getSampleSubscriptionRenewal());
        when(dealsOfTheDayEventSearchIndexRepository.findById(anyString()))
                .thenReturn(Optional.of(new DealsOfTheDayEventSearchIndex()));
        when(dealSearchIndexRepository.findAllById(anySet())).thenReturn(getSampleDealSearchIndexList());
        when(dealsOfTheDayEventRepository.save(any(DealsOfTheDayEvent.class)))
                .thenReturn(getSampleDealsOfTheDayEvent());
        when(dealsOfTheDayEventSearchIndexRepository.save(any(DealsOfTheDayEventSearchIndex.class)))
                .thenReturn(new DealsOfTheDayEventSearchIndex());

        DealsOfTheDayEvent result = dealsOfTheDayService
                .addDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, UserType.BANK, businessMerchantResponseDto);
        
        assertEquals(MERCHANT_ID, result.getUserId());
        verify(dealsOfTheDayEventRepository).save(any(DealsOfTheDayEvent.class));
    }

    @Test
    void Should_ThrowKupiCoreException_When_AddingDealsOfTheDayEventForCheckingExistingEventFailed() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();

        when(dealsOfTheDayEventRepository.findByUserIdAndValidOn(anyString(), any(Date.class)))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> dealsOfTheDayService
                .addDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, UserType.BANK, businessMerchantResponseDto));
        assertEquals(exception.getLocalizedMessage(), exception.getMessage());
    }

    @Test
    void Should_ThrowLimitExceededException_When_AddingDealsOfTheDayEventForDealsOfTheDaySlotNotAvailable() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();

        when(dealsOfTheDayEventRepository.findByUserIdAndValidOn(anyString(), any(Date.class)))
                .thenReturn(Optional.empty());
        Set<String> dealIdsSet = new HashSet<>();
        dealIdsSet.add(DEAL_ID);
        dealIdsSet.add(DEAL_ID + "1");
        dealIdsSet.add(DEAL_ID + "2");
        DealsOfTheDayEvent dealsOfTheDayEvent = getSampleDealsOfTheDayEvent();
        dealsOfTheDayEvent.setDeals(dealIdsSet);
        List<DealsOfTheDayEvent> dealsOfTheDayEventList = new ArrayList<>();
        dealsOfTheDayEventList.add(dealsOfTheDayEvent);
        when(dealsOfTheDayEventRepository.findByValidOnAndUserType(any(Date.class), any(UserType.class)))
                .thenReturn(dealsOfTheDayEventList);
        LimitExceededException exception = assertThrows(LimitExceededException.class, () -> dealsOfTheDayService
                .addDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, UserType.BANK, businessMerchantResponseDto));
        assertEquals("Deals of the day slot is not available on: " +
                new Date(getSampleDealsOfTheDayEventRequestDto().getValidOn()), exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_AddingDealsOfTheDayEventForCheckDealsOfTheDayEventSlotAvailableIsFailed() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();

        when(dealsOfTheDayEventRepository.findByUserIdAndValidOn(anyString(), any(Date.class)))
                .thenReturn(Optional.empty());
        when(dealsOfTheDayEventRepository.findByValidOnAndUserType(any(Date.class), any(UserType.class)))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> dealsOfTheDayService
                .addDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, UserType.BANK, businessMerchantResponseDto));
        assertEquals("Checking deals of the day event slot's availability was failed for userType: "
                        + UserType.BANK + " on date: " + new Date(getSampleDealsOfTheDayEventRequestDto().getValidOn()),
                exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidDealException_When_AddingDealsOfTheDayEventForDealIsNotApprovedOrInactive() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();

        when(dealsOfTheDayEventRepository.findByUserIdAndValidOn(anyString(), any(Date.class)))
                .thenReturn(Optional.empty());
        when(dealsOfTheDayEventRepository.findByValidOnAndUserType(any(Date.class), any(UserType.class)))
                .thenReturn(getSampleDealsOfTheDayEventList());
        DealSearchIndex dealSearchIndex = getSampleDealSearchIndex();
        dealSearchIndex.setApprovalStatus(com.kupi.cc.base.enums.ApprovalStatus.PENDING);
        when(dealSearchIndexRepository.findById(DEAL_ID)).thenReturn(Optional.of(dealSearchIndex));
        InvalidDealException exception = assertThrows(InvalidDealException.class, () -> dealsOfTheDayService
                .addDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, UserType.BANK, businessMerchantResponseDto));
        assertEquals("Expired, deactivated, or draft deals cannot be used for deals of the day. " +
                "Invalid dealIds: " + DEAL_ID + ". Statuses: PENDING", exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidDealException_When_AddingDealsOfTheDayEventForDealIsNotFoundInDB() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();

        when(dealsOfTheDayEventRepository.findByUserIdAndValidOn(anyString(), any(Date.class)))
                .thenReturn(Optional.empty());
        when(dealsOfTheDayEventRepository.findByValidOnAndUserType(any(Date.class), any(UserType.class)))
                .thenReturn(getSampleDealsOfTheDayEventList());
        when(dealSearchIndexRepository.findById(DEAL_ID))
                .thenThrow(new AopInvocationException(FAILED));
        InvalidDealException exception = assertThrows(InvalidDealException.class, () -> dealsOfTheDayService
                .addDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, UserType.BANK, businessMerchantResponseDto));
        assertEquals("DealId not found in database. DealIds: " +
                getSampleDealsOfTheDayEventRequestDto().getDealIds(), exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_AddingDealsOfTheDayEventForCheckIfValidDealIdsIsFailed() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();

        when(dealsOfTheDayEventRepository.findByUserIdAndValidOn(anyString(), any(Date.class)))
                .thenReturn(Optional.empty());
        when(dealsOfTheDayEventRepository.findByValidOnAndUserType(any(Date.class), any(UserType.class)))
                .thenReturn(getSampleDealsOfTheDayEventList());
        when(dealSearchIndexRepository.isActiveDealForMerchant(anyString(), anyString(), anyLong()))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> dealsOfTheDayService
                .addDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, UserType.BANK, businessMerchantResponseDto));
        assertEquals(exception.getLocalizedMessage(), exception.getMessage());
    }

    @Test
    void Should_ThrowLimitExceededException_When_AddingDealsOfTheDayEventForNoOfDealsPerEventLimitExceeded() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();

        when(dealsOfTheDayEventRepository.findByUserIdAndValidOn(anyString(), any(Date.class)))
                .thenReturn(Optional.empty());
        when(dealsOfTheDayEventRepository.findByValidOnAndUserType(any(Date.class), any(UserType.class)))
                .thenReturn(getSampleDealsOfTheDayEventList());
        when(dealSearchIndexRepository.findById(DEAL_ID)).thenReturn(Optional.of(getSampleDealSearchIndex()));
        SubscriptionRenewal subscriptionRenewal = getSampleSubscriptionRenewal();
        subscriptionRenewal.setNoOfDealsPerEvent(0);
        when(subscriberService.getSubscriptionRenewalByUserId(anyString())).thenReturn(subscriptionRenewal);
        LimitExceededException exception = assertThrows(LimitExceededException.class, () -> dealsOfTheDayService
                .addDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, UserType.BANK, businessMerchantResponseDto));
        assertEquals("Number of deals per event limit exceeded for userId: " + MERCHANT_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowLimitExceededException_When_AddingDealsOfTheDayEventForNoOfDealsOfTheDayPerMonthOrYearExceeded() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();

        when(dealsOfTheDayEventRepository.findByUserIdAndValidOn(anyString(), any(Date.class)))
                .thenReturn(Optional.empty());
        when(dealsOfTheDayEventRepository.findByValidOnAndUserType(any(Date.class), any(UserType.class)))
                .thenReturn(getSampleDealsOfTheDayEventList());
        when(dealSearchIndexRepository.findById(DEAL_ID)).thenReturn(Optional.of(getSampleDealSearchIndex()));
        SubscriptionRenewal subscriptionRenewal = getSampleSubscriptionRenewal();
        subscriptionRenewal.setRemainingDealsOfTheDayEventsPerMonth(0);
        when(subscriberService.getSubscriptionRenewalByUserId(anyString())).thenReturn(subscriptionRenewal);
        LimitExceededException exception = assertThrows(LimitExceededException.class, () -> dealsOfTheDayService
                .addDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, UserType.BANK, businessMerchantResponseDto));
        assertEquals("Number of deals of the day events per month/year limit exceeded for userId: " +
                MERCHANT_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_AddingDealsOfTheDayEventForCheckSubscriptionPlanFailed() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();

        when(dealsOfTheDayEventRepository.findByUserIdAndValidOn(anyString(), any(Date.class)))
                .thenReturn(Optional.empty());
        when(dealsOfTheDayEventRepository.findByValidOnAndUserType(any(Date.class), any(UserType.class)))
                .thenReturn(getSampleDealsOfTheDayEventList());
        when(dealSearchIndexRepository.isActiveDealForMerchant(anyString(), anyString(), anyLong()))
                .thenReturn(true);
        when(subscriberService.getSubscriptionRenewalByUserId(anyString())).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> dealsOfTheDayService
                .addDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, UserType.BANK, businessMerchantResponseDto));
        assertEquals(exception.getLocalizedMessage(), exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_AddingDealsOfTheDayEventForCreateDealTitlesFailed() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();

        when(dealsOfTheDayEventRepository.findByUserIdAndValidOn(anyString(), any(Date.class)))
                .thenReturn(Optional.empty());
        when(dealsOfTheDayEventRepository.findByValidOnAndUserType(any(Date.class), any(UserType.class)))
                .thenReturn(getSampleDealsOfTheDayEventList());
        when(dealSearchIndexRepository.findById(DEAL_ID)).thenReturn(Optional.of(getSampleDealSearchIndex()));
        when(subscriberService.getSubscriptionRenewalByUserId(anyString())).thenReturn(getSampleSubscriptionRenewal());
        when(dealSearchIndexRepository.findAllById(anySet())).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> dealsOfTheDayService
                .addDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, UserType.BANK, businessMerchantResponseDto));
        assertEquals("Getting deal search indexes failed when creating deal titles.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_AddingDealsOfTheDayEventIsFailed() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();

        when(dealsOfTheDayEventRepository.findByUserIdAndValidOn(anyString(), any(Date.class)))
                .thenReturn(Optional.empty());
        when(dealsOfTheDayEventRepository.findByValidOnAndUserType(any(Date.class), any(UserType.class)))
                .thenReturn(getSampleDealsOfTheDayEventList());
        when(dealSearchIndexRepository.findById(DEAL_ID)).thenReturn(Optional.of(getSampleDealSearchIndex()));
        when(subscriberService.getSubscriptionRenewalByUserId(anyString())).thenReturn(getSampleSubscriptionRenewal());
        when(dealSearchIndexRepository.findAllById(anySet())).thenReturn(getSampleDealSearchIndexList());


        DataAccessException e = new DataAccessException(FAILED) {
        };

        when(dealsOfTheDayEventRepository.save(any(DealsOfTheDayEvent.class)))
                .thenThrow(e);
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> dealsOfTheDayService
                .addDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, UserType.BANK, businessMerchantResponseDto));
        assertEquals("Saving deals of the day event in db was failed for userId: " + MERCHANT_ID + e,
                exception.getMessage());
    }

    /**
     * Update deals of the day event
     */
    @Test
    void Should_ReturnDealsOfTheDayEvent_When_UpdatingDealsOfTheDayEventIsSuccessful() {
        when(dealsOfTheDayEventRepository.findById(anyString())).thenReturn(Optional.of(getSampleDealsOfTheDayEvent()));
        when(dealsOfTheDayEventRepository.findByValidOnAndUserType(any(Date.class), any(UserType.class)))
                .thenReturn(getSampleDealsOfTheDayEventList());
        when(dealSearchIndexRepository.findById(DEAL_ID)).thenReturn(Optional.of(getSampleDealSearchIndex()));
        when(subscriberService.getSubscriptionRenewalByUserId(anyString())).thenReturn(getSampleSubscriptionRenewal());
        when(dealsOfTheDayEventSearchIndexRepository.findById(anyString()))
                .thenReturn(Optional.of(new DealsOfTheDayEventSearchIndex()));
        when(dealSearchIndexRepository.findAllById(anySet())).thenReturn(getSampleDealSearchIndexList());

        dealsOfTheDayService.updateDealsOfTheDayEvent(getSampleDealsOfTheDayEventRequestDto(),
                MERCHANT_ID, UserType.BANK);

        verify(dealsOfTheDayEventRepository).save(any(DealsOfTheDayEvent.class));
        verify(dealsOfTheDayEventSearchIndexRepository).save(any(DealsOfTheDayEventSearchIndex.class));
        assertEquals(MERCHANT_ID, dealsOfTheDayService.updateDealsOfTheDayEvent(getSampleDealsOfTheDayEventRequestDto(),
                MERCHANT_ID, UserType.BANK).getUserId());
    }

    @Test
    void Should_ThrowLimitExceededException_When_UpdatingDealsOfTheDayEventForNoOfDealsPerEventLimitExceeded() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();
        SubscriptionRenewal subscriptionRenewal = getSampleSubscriptionRenewal();
        subscriptionRenewal.setNoOfDealsPerEvent(0);

        when(dealsOfTheDayEventRepository.findById(anyString())).thenReturn(Optional.of(getSampleDealsOfTheDayEvent()));
        when(dealsOfTheDayEventRepository.findByValidOnAndUserType(any(Date.class), any(UserType.class)))
                .thenReturn(getSampleDealsOfTheDayEventList());
        when(dealSearchIndexRepository.findById(DEAL_ID)).thenReturn(Optional.of(getSampleDealSearchIndex()));
        when(subscriberService.getSubscriptionRenewalByUserId(anyString())).thenReturn(subscriptionRenewal);

        LimitExceededException exception = assertThrows(LimitExceededException.class, () -> dealsOfTheDayService
                .updateDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, MERCHANT_ID, UserType.BANK));
        assertEquals("Number of deals per event limit exceeded for userId: " + MERCHANT_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidDealsOfTheDayEventException_When_UpdatingDealsOfTheDayEventForDealsOfTheDayEventSearchIndexNoFound() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();

        when(dealsOfTheDayEventRepository.findById(anyString())).thenReturn(Optional.of(getSampleDealsOfTheDayEvent()));
        when(dealsOfTheDayEventRepository.findByValidOnAndUserType(any(Date.class), any(UserType.class)))
                .thenReturn(getSampleDealsOfTheDayEventList());
        when(dealSearchIndexRepository.findById(DEAL_ID)).thenReturn(Optional.of(getSampleDealSearchIndex()));
        when(subscriberService.getSubscriptionRenewalByUserId(anyString())).thenReturn(getSampleSubscriptionRenewal());
        when(dealsOfTheDayEventSearchIndexRepository.findById(anyString())).thenReturn(Optional.empty());
        InvalidDealsOfTheDayEventException exception = assertThrows(InvalidDealsOfTheDayEventException.class, () ->
                dealsOfTheDayService.updateDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, MERCHANT_ID,
                        UserType.BANK));
        assertEquals("Deals of the day event search index not found for eventId: " + EVENT_ID,
                exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_UpdatingDealsOfTheDayEventGetDealsOfTheDayEventSearchIndexFromDBIsFailed() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();

        when(dealsOfTheDayEventRepository.findById(anyString())).thenReturn(Optional.of(getSampleDealsOfTheDayEvent()));
        when(dealsOfTheDayEventRepository.findByValidOnAndUserType(any(Date.class), any(UserType.class)))
                .thenReturn(getSampleDealsOfTheDayEventList());
        when(dealSearchIndexRepository.findById(DEAL_ID)).thenReturn(Optional.of(getSampleDealSearchIndex()));
        when(subscriberService.getSubscriptionRenewalByUserId(anyString())).thenReturn(getSampleSubscriptionRenewal());
        when(dealsOfTheDayEventSearchIndexRepository.findById(anyString())).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> dealsOfTheDayService
                .updateDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, MERCHANT_ID, UserType.BANK));
        assertEquals("Getting deals of the day event search index from db was failed for eventId: " + EVENT_ID,
                exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidDealsOfTheDayEventException_When_UpdatingDealsOfTheDayEventForDealsOfTheDayEventNotFound() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();

        when(dealsOfTheDayEventRepository.findById(anyString())).thenReturn(Optional.empty());
        InvalidDealsOfTheDayEventException exception = assertThrows(InvalidDealsOfTheDayEventException.class, () ->
                dealsOfTheDayService.updateDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, MERCHANT_ID,
                        UserType.BANK));
        assertEquals("Deals of the day event not found for eventId: " + EVENT_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_UpdatingDealsOfTheDayEventIsFailed() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();

        when(dealsOfTheDayEventRepository.findById(anyString())).thenReturn(Optional.of(getSampleDealsOfTheDayEvent()));
        when(dealsOfTheDayEventRepository.findByValidOnAndUserType(any(Date.class), any(UserType.class)))
                .thenReturn(getSampleDealsOfTheDayEventList());
        when(dealSearchIndexRepository.findById(DEAL_ID)).thenReturn(Optional.of(getSampleDealSearchIndex()));
        when(subscriberService.getSubscriptionRenewalByUserId(anyString())).thenReturn(getSampleSubscriptionRenewal());
        when(dealsOfTheDayEventSearchIndexRepository.findById(anyString()))
                .thenReturn(Optional.of(new DealsOfTheDayEventSearchIndex()));
        when(dealSearchIndexRepository.findAllById(anySet())).thenReturn(getSampleDealSearchIndexList());
        doThrow(new DataAccessException(FAILED) {
        }).when(dealsOfTheDayEventRepository).save(any(DealsOfTheDayEvent.class));
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> dealsOfTheDayService
                .updateDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, MERCHANT_ID, UserType.BANK));
        assertEquals(exception.getLocalizedMessage(), exception.getMessage());
    }

    /**
     * Delete deals of the day event
     */
    @Test
    void Should_DeleteDealsOfTheDayEvent() {
        when(dealsOfTheDayEventRepository.findById(anyString())).thenReturn(Optional.of(getSampleDealsOfTheDayEvent()));
        doNothing().when(dealsOfTheDayEventRepository).deleteById(anyString());
        doNothing().when(dealsOfTheDayEventSearchIndexRepository).deleteById(anyString());
        when(subscriberService.getSubscriptionRenewalByUserId(anyString())).thenReturn(getSampleSubscriptionRenewal());

        dealsOfTheDayService.deleteDealsOfTheDayEvent(EVENT_ID, MERCHANT_ID);

        verify(subscriberService, times(1)).saveSubscriptionRenewal(any(SubscriptionRenewal.class));
    }

    @Test
    void Should_ThrowInvalidDealsOfTheDayEventException_When_DeletingDealsOfTheDayEventForDoDEventNotFound() {
        when(dealsOfTheDayEventRepository.findById(anyString())).thenReturn(Optional.empty());
        InvalidDealsOfTheDayEventException exception = assertThrows(InvalidDealsOfTheDayEventException.class, () ->
                dealsOfTheDayService.deleteDealsOfTheDayEvent(EVENT_ID, MERCHANT_ID));
        assertEquals("Deals of the day event not found.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_DeletingDealsOfTheDayEventIsFailed() {
        when(dealsOfTheDayEventRepository.findById(anyString())).thenReturn(Optional.of(getSampleDealsOfTheDayEvent()));
        doThrow(new DataAccessException(FAILED) {
        }).when(dealsOfTheDayEventRepository).deleteById(anyString());
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> dealsOfTheDayService
                .deleteDealsOfTheDayEvent(EVENT_ID, MERCHANT_ID));
        assertEquals(exception.getLocalizedMessage(), exception.getMessage());
    }

    /**
     * Get deals of the day event by id
     */
    @Test
    void Should_ReturnDealsOfTheDayEvent_When_GettingDealsOfTheDayEventByIdIsSuccessful() {
        when(dealsOfTheDayEventRepository.findById(anyString())).thenReturn(Optional.of(getSampleDealsOfTheDayEvent()));

        assertEquals(MERCHANT_ID, dealsOfTheDayService.getDealsOfTheDayEventById(EVENT_ID).getUserId());
    }

    @Test
    void Should_ThrowInvalidDealsOfTheDayEventException_When_GettingDealsOfTheDayEventByIdForInvalidDoDEventId() {
        when(dealsOfTheDayEventRepository.findById(anyString())).thenReturn(Optional.empty());

        InvalidDealsOfTheDayEventException exception = assertThrows(InvalidDealsOfTheDayEventException.class, () ->
                dealsOfTheDayService.getDealsOfTheDayEventById(EVENT_ID));
        assertEquals("Invalid Deal of the day event id: {}" + EVENT_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingDealsOfTheDayEventByIdIsFailed() {
        when(dealsOfTheDayEventRepository.findById(anyString())).thenThrow(new DataAccessException(FAILED) {
        });

        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> dealsOfTheDayService
                .getDealsOfTheDayEventById(EVENT_ID));
        assertEquals("Returning deal of the day event by id: {} was failed. " + EVENT_ID, exception.getMessage());
    }

    private Page<DealOfTheDaySearchIndex> getSampleDealOfTheDaySearchIndexPage() {
        DealOfTheDaySearchIndex dealOfTheDaySearchIndex = new DealOfTheDaySearchIndex();
        dealOfTheDaySearchIndex.setId(DEAL_ID);
        List<DealOfTheDaySearchIndex> dealOfTheDaySearchIndexList = new ArrayList<>();
        dealOfTheDaySearchIndexList.add(dealOfTheDaySearchIndex);
        return new PageImpl<>(dealOfTheDaySearchIndexList);
    }

    private Deal getSampleDeal() {
        Deal deal = new Deal();
        deal.setId(DEAL_ID);
        deal.setMerchantId(MERCHANT_ID);
        return deal;
    }

    private BankDeal getSampleBankDeal() {
        BankDeal bankDeal = new BankDeal();
        bankDeal.setId(DEAL_ID);
        return bankDeal;
    }

    private Page<Deal> getSampleDealPage() {
        List<Deal> dealList = new ArrayList<>();
        dealList.add(getSampleDeal());
        return new PageImpl<>(dealList);
    }

    private Map<String, MerchantBusinessResponseDto> getSampleMerchantBusinessResponseDtoMap() {
        Map<String, MerchantBusinessResponseDto> merchantBusinessResponseDtoMap = new HashMap<>();
        merchantBusinessResponseDtoMap.put(MERCHANT_ID, getSampleMerchantBusinessResponseDto());
        return merchantBusinessResponseDtoMap;
    }

    private MerchantBusinessResponseDto getSampleMerchantBusinessResponseDto() {
        MerchantBusinessResponseDto merchantBusinessResponseDto = new MerchantBusinessResponseDto();
        merchantBusinessResponseDto.setId(MERCHANT_ID);
        merchantBusinessResponseDto.setUserType(UserType.MERCHANT.toString());
        return merchantBusinessResponseDto;
    }

    private Page<DealsOfTheDayEventSearchIndex> getSampleDealsOfTheDayEventSearchIndexPage() {
        DealsOfTheDayEventSearchIndex dealsOfTheDayEventSearchIndex = new DealsOfTheDayEventSearchIndex();
        dealsOfTheDayEventSearchIndex.setId(DEAL_ID);
        List<DealsOfTheDayEventSearchIndex> dealsOfTheDayEventSearchIndexList = new ArrayList<>();
        dealsOfTheDayEventSearchIndexList.add(dealsOfTheDayEventSearchIndex);
        return new PageImpl<>(dealsOfTheDayEventSearchIndexList);
    }

    private DealsOfTheDayEventSummaryResponseDto getSampleDealsOfTheDayEventSummaryResponseDto() {
        return new DealsOfTheDayEventSummaryResponseDto(NO_OF_BANK_EVENTS, NO_OF_MERCHANT_EVENTS, NO_OF_TOTAL_EVENTS,
                NO_OF_BANKS, NO_OF_MERCHANTS);
    }

    private DealsOfTheDayEventRequestDto getSampleDealsOfTheDayEventRequestDto() {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = new DealsOfTheDayEventRequestDto();
        dealsOfTheDayEventRequestDto.setValidOn(System.currentTimeMillis() + DATE_IN_MILLISECONDS);
        dealsOfTheDayEventRequestDto.setEventId(EVENT_ID);
        dealsOfTheDayEventRequestDto.setDealIds(getSampleDealIdsSet());
        return dealsOfTheDayEventRequestDto;
    }

    private Set<String> getSampleDealIdsSet() {
        Set<String> dealIdsSet = new HashSet<>();
        dealIdsSet.add(DEAL_ID);
        return dealIdsSet;
    }

    private BusinessMerchantResponseDto getSampleBusinessMerchantResponseDto() {
        BusinessMerchantResponseDto businessMerchantResponseDto = new BusinessMerchantResponseDto();
        businessMerchantResponseDto.setMerchantId(MERCHANT_ID);
        return businessMerchantResponseDto;
    }

    private List<DealsOfTheDayEvent> getSampleDealsOfTheDayEventList() {
        DealsOfTheDayEvent dealsOfTheDayEvent = getSampleDealsOfTheDayEvent();
        dealsOfTheDayEvent.setDeals(getSampleDealIdsSet());
        List<DealsOfTheDayEvent> dealsOfTheDayEventList = new ArrayList<>();
        dealsOfTheDayEventList.add(dealsOfTheDayEvent);
        return dealsOfTheDayEventList;
    }

    private DealsOfTheDayEvent getSampleDealsOfTheDayEvent() {
        DealsOfTheDayEvent dealsOfTheDayEvent = new DealsOfTheDayEvent();
        dealsOfTheDayEvent.setId(EVENT_ID);
        dealsOfTheDayEvent.setUserId(MERCHANT_ID);
        dealsOfTheDayEvent.setValidOn(new java.sql.Date(System.currentTimeMillis() + DATE_IN_MILLISECONDS));
        dealsOfTheDayEvent.setDeals(new HashSet<>()); // Initialize empty deals set
        return dealsOfTheDayEvent;
    }

    private SubscriptionRenewal getSampleSubscriptionRenewal() {
        SubscriptionRenewal subscriptionRenewal = new SubscriptionRenewal();
        subscriptionRenewal.setNoOfDealsPerEvent(8);
        subscriptionRenewal.setRemainingDealsOfTheDayEventsPerMonth(REMAINING_DEALS_OF_THE_DAY_EVENTS_PER_MONTH);
        return subscriptionRenewal;
    }

    private List<DealSearchIndex> getSampleDealSearchIndexList() {
        List<DealSearchIndex> dealSearchIndexList = new ArrayList<>();
        dealSearchIndexList.add(getSampleDealSearchIndex());
        return dealSearchIndexList;
    }

    private DealSearchIndex getSampleDealSearchIndex() {
        DealSearchIndex dealSearchIndex = new DealSearchIndex();
        dealSearchIndex.setId(DEAL_ID);
        dealSearchIndex.setTitle(TITLE);
        dealSearchIndex.setMerchantId(MERCHANT_ID);
        dealSearchIndex.setApprovalStatus(com.kupi.cc.base.enums.ApprovalStatus.APPROVED);
        // expiredOn must be greater than the validOn timestamp from the request
        long validOnTimestamp = System.currentTimeMillis() + DATE_IN_MILLISECONDS;
        dealSearchIndex.setExpiredOn(validOnTimestamp + DATE_IN_MILLISECONDS);
        dealSearchIndex.setValidFrom(System.currentTimeMillis() - DATE_IN_MILLISECONDS);
        return dealSearchIndex;
    }
}