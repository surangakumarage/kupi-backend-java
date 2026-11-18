package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.ResourceBundleMessageSourceBean;
import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.CouponStatistics;
import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.domain.entity.DealsOfTheDayEvent;
import com.kupi.cc.base.domain.entity.DealsOfTheDayEventSearchIndex;
import com.kupi.cc.base.domain.request.DealsOfTheDayEventRequestDto;
import com.kupi.cc.base.domain.response.*;
import com.kupi.cc.base.enums.*;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.service.*;
import com.kupi.cc.base.repository.FavouriteDealRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests {@link DealsOfTheDayController} class
 */
class DealsOfTheDayControllerTest {

    private static final UserType USER_TYPE_BANK = UserType.BANK;
    private static final UserType USER_TYPE_MERCHANT = UserType.MERCHANT;
    private static final UserType USER_TYPE_ADMIN = UserType.ADMIN;
    private static final String EVENT_ID = "dodeid-123456789";
    private static final String DEAL_ID = "did-123456789";
    private static final String USER_ID_HEADER = "User-Id";
    private static final String MERCHANT_ID = "uid-123456789";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String AUTH_TOKEN_HEADER = "Auth-Token";
    private static final String AUTH_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String CATEGORY_ID = "cid-123456789";
    private static final String SEARCH_TERM = "Softlogic";
    private static final String OPTION = "TODAY";
    private static final String NULL_OPTION = "null";
    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final long DATE_IN_MILLISECONDS = 1659465000000L;
    private static final long NO_OF_BANK_EVENTS = 3;
    private static final long NO_OF_MERCHANT_EVENTS = 8;
    private static final long NO_OF_TOTAL_EVENTS = 11;
    private static final long NO_OF_BANKS = 22;
    private static final long NO_OF_MERCHANTS = 15;
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String FAILED = "failed";
    private static final String APPLICATION_JSON = "application/json";

    private static final String GET_DEALS_OF_THE_DAY_URI = "/api/v1/deals/deals-of-the-day/{page}/{size}/category/{categoryId}/{userType}/{merchantId}/search/{searchTerm}";
    private static final String GET_DEALS_OF_THE_DAY_HISTORY_URI = "/api/v1/deals/deals-of-the-day/{userType}/{userId}/{page}/{size}/search/{searchTerm}/filter/{option}";
    private static final String GET_UPCOMING_DEALS_OF_THE_DAY_EVENTS_URI = "/api/v1/deals/deals-of-the-day/upcoming/{userType}/{userId}/{page}/{size}/search/{searchTerm}";
    private static final String GET_UPCOMING_DEALS_OF_THE_DAY_EVENTS_SUMMARY_URI = "/api/v1/deals/deals-of-the-day/upcoming/summary";
    private static final String DEALS_OF_THE_DAY_EVENT_URI = "/api/v1/deals/deals-of-the-day/event/{userType}";
    private static final String DEALS_OF_THE_DAY_EVENT_BY_EVENT_ID_URI = "/api/v1/deals/deals-of-the-day/event/{eventId}";

    private final ResourceBundleMessageSourceBean resourceBundleMessageSourceBean = new ResourceBundleMessageSourceBean();
    Locale locale = LocaleContextHolder.getLocale();
    private MockMvc mockMvc;
    private ResourceBundleMessageSource resourceBundleMessageSource;
    @Mock
    private Translator translator;
    @Mock
    private DealsOfTheDayService dealsOfTheDayService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private AuthUserService authUserService;
    @Mock
    private DealSearchService dealSearchService;
    @Mock
    private CouponStatisticsService couponStatisticsService;
    @Mock
    private DealService dealService;
    @Mock
    private PromoTypeService promoTypeService;
    @Mock
    private FavouriteDealRepository favouriteDealRepository;

    @BeforeEach
    void setUp() {
        initMocks(this);
        DealsOfTheDayController dealsOfTheDayController = new DealsOfTheDayController(translator, dealsOfTheDayService,
                categoryService, authUserService, dealSearchService, couponStatisticsService, dealService,
                promoTypeService, favouriteDealRepository);
        org.springframework.validation.Validator noOpValidator = new org.springframework.validation.Validator() {
            @Override
            public boolean supports(Class<?> clazz) {
                return true;
            }

            @Override
            public void validate(Object target, Errors errors) {
                // No-op validator
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(dealsOfTheDayController)
                .setValidator(noOpValidator)
                .build();
        resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Start of tests for get deals of the day
     * Api context: api/v1/deals/deals-of-the-day/{page}/{size}/category/{categoryId}/{userType}/{merchantId}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_GettingDealsOfTheDayIsSuccessful() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE);
        Page<Deal> dealPage = getSampleDealPage();
        doNothing().when(categoryService).validateCategoryId(CATEGORY_ID);
        CouponStatistics couponStatistics = getSampleCouponStatistics();
        List<CouponStatistics> couponStatisticsList = new ArrayList<>();
        couponStatisticsList.add(couponStatistics);
        var dealList = dealPage.get().map(Deal::getId).collect(Collectors.toList());
        when(dealsOfTheDayService.dealsOfTheDayPageForMerchant(eq(pageable), anyString(), anyString(), anyString(),
                any(UserType.class))).thenReturn(dealPage);
        @SuppressWarnings("unchecked")
        Page<Deal> pageForMerchantMap = (Page<Deal>) any(Page.class);
        when(authUserService.getMerchantMapForDeals(pageForMerchantMap, any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());
        when(couponStatisticsService.getCouponStatisticsByDealIdList(dealList)).thenReturn(couponStatisticsList);
        @SuppressWarnings("unchecked")
        Page<Deal> pageForRemoveCoupons = (Page<Deal>) any(Page.class);
        when(dealsOfTheDayService.removeCouponNotAvailableDeals(pageForRemoveCoupons, anyList())).thenReturn(dealPage);
        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_DEAL_LIST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_DEAL_LIST.getCode())))
                .thenReturn(successMessage);
        String uri = GET_DEALS_OF_THE_DAY_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{categoryId}", CATEGORY_ID).replace("{merchantId}", MERCHANT_ID)
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.deals[0].id").value(DEAL_ID))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingDealsOfTheDayForUnsupportedUserType() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .UNSUPPORTED_USER_TYPE_ACTION.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_DEALS_OF_THE_DAY_URI.replace("{userType}", USER_TYPE_ADMIN.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{categoryId}", CATEGORY_ID).replace("{merchantId}", MERCHANT_ID)
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingDealsOfTheDayForInvalidCategoryId() throws Exception {
        doThrow(new InvalidCategoryException(FAILED)).when(categoryService).validateCategoryId(CATEGORY_ID);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_CATEGORY_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_CATEGORY_ID.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_DEALS_OF_THE_DAY_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{categoryId}", CATEGORY_ID).replace("{merchantId}", MERCHANT_ID)
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_CATEGORY_ID
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_CATEGORY_ID
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingDealsOfTheDayIsFailed() throws Exception {
        doNothing().when(categoryService).validateCategoryId(CATEGORY_ID);
        when(dealsOfTheDayService.dealsOfTheDayPageForMerchant(any(Pageable.class), anyString(), anyString(),
                anyString(), any(UserType.class))).thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_DEALS_OF_THE_DAY_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{categoryId}", CATEGORY_ID).replace("{merchantId}", MERCHANT_ID)
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get deals of the day history
     * Api context: api/v1/deals/deals-of-the-day/{userType}/{userId}/{page}/{size}/search/{searchTerm}/filter/{option}
     */
//    @Test
    void Should_ReturnOk_When_GettingDealsOfTheDayHistoryIsSuccessful() throws Exception {
        when(dealsOfTheDayService.getDealsOfTheDayHistory(anyString(), anyString(), any(DealsOfTheDayHistoryFilter.class),
                anyString(), any(UserType.class))).thenReturn(getSampleDealsOfTheDayBasicEventResponseDtoList());

        // Mock DealSearchIndex for dealStatus calculation
        com.kupi.cc.base.domain.entity.DealSearchIndex dealSearchIndex = getSampleDealSearchIndex();
        when(dealSearchService.findById(DEAL_ID)).thenReturn(dealSearchIndex);
        when(dealService.calculateDealStatus(any(com.kupi.cc.base.domain.entity.DealSearchIndex.class), anyLong(), anyLong()))
                .thenReturn("Active");
        when(dealService.calculateEarnings(anyString(), anyLong(), anyLong()))
                .thenReturn(java.math.BigDecimal.ZERO);

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_DEAL_LIST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_DEAL_LIST.getCode())))
                .thenReturn(successMessage);
        String uri = GET_DEALS_OF_THE_DAY_HISTORY_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{userId}", MERCHANT_ID).replace("{option}", OPTION)
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.dealGroups[0].validOn.milliseconds").value(DATE_IN_MILLISECONDS))
                .andExpect(jsonPath("$.data.dealGroups[0].deals[0].id").value(DEAL_ID))
                .andExpect(jsonPath("$.data.dealGroups[0].deals[0].dealStatus").exists())
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingDealsOfTheDayHistoryForUnsupportedUserType() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .UNSUPPORTED_USER_TYPE_ACTION.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_DEALS_OF_THE_DAY_HISTORY_URI.replace("{userType}", USER_TYPE_ADMIN.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{userId}", MERCHANT_ID).replace("{option}", OPTION)
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingDealsOfTheDayHistoryForInvalidDateOption() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_DATE_OPTION.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_DATE_OPTION.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_DEALS_OF_THE_DAY_HISTORY_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{userId}", MERCHANT_ID).replace("{option}", NULL_OPTION)
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_DATE_OPTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_DATE_OPTION.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingDealsOfTheDayHistoryIsFailed() throws Exception {
        when(dealsOfTheDayService.getDealsOfTheDayHistory(anyString(), anyString(), any(DealsOfTheDayHistoryFilter.class),
                anyString(), any(UserType.class))).thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_DEALS_OF_THE_DAY_HISTORY_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{userId}", MERCHANT_ID).replace("{option}", OPTION)
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get upcoming deals of the day events
     * Api context: api/v1/deals/deals-of-the-day/upcoming/{userType}/{userId}/{page}/{size}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_GettingUpcomingDealsOfTheDayEventsIsSuccessful() throws Exception {
        when(dealsOfTheDayService.getUpcomingDealsOfTheDayEvents(anyInt(), anyInt(), any(UserType.class), anyString(),
                anyString())).thenReturn(getSampleDealsOfTheDayEventSearchIndexPage());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .UPCOMING_DEALS_OF_THE_DAY_EVENTS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.UPCOMING_DEALS_OF_THE_DAY_EVENTS.getCode())))
                .thenReturn(successMessage);
        String uri = GET_UPCOMING_DEALS_OF_THE_DAY_EVENTS_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{userId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.UPCOMING_DEALS_OF_THE_DAY_EVENTS.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.UPCOMING_DEALS_OF_THE_DAY_EVENTS.getCode()))
                .andExpect(jsonPath("$.data.events[0].id").value(DEAL_ID))
                .andExpect(jsonPath("$.data.events[0].validOn.milliseconds").value(DATE_IN_MILLISECONDS))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingUpcomingDealsOfTheDayEventsIsFailed() throws Exception {
        when(dealsOfTheDayService.getUpcomingDealsOfTheDayEvents(anyInt(), anyInt(), any(UserType.class), anyString(),
                anyString())).thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_UPCOMING_DEALS_OF_THE_DAY_EVENTS_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{userId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get upcoming deals of the day events summary
     * Api context: api/v1/deals/deals-of-the-day/upcoming/summary
     */
    @Test
    void Should_ReturnOk_When_GettingUpcomingDealsOfTheDayEventsSummaryIsSuccessful() throws Exception {
        when(dealsOfTheDayService.getDealsOfTheDaySummaryForAdmin())
                .thenReturn(getSampleDealsOfTheDayEventSummaryResponseDto());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .DEALS_OF_THE_DAY_EVENT_SUMMARY.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.DEALS_OF_THE_DAY_EVENT_SUMMARY.getCode())))
                .thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.get(GET_UPCOMING_DEALS_OF_THE_DAY_EVENTS_SUMMARY_URI)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.DEALS_OF_THE_DAY_EVENT_SUMMARY.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.DEALS_OF_THE_DAY_EVENT_SUMMARY.getCode()))
                .andExpect(jsonPath("$.data.noOfBankEvents").value(NO_OF_BANK_EVENTS))
                .andExpect(jsonPath("$.data.noOfMerchantEvents").value(NO_OF_MERCHANT_EVENTS))
                .andExpect(jsonPath("$.data.noOfTotalEvents").value(NO_OF_TOTAL_EVENTS))
                .andExpect(jsonPath("$.data.noOfBanks").value(NO_OF_BANKS))
                .andExpect(jsonPath("$.data.noOfMerchants").value(NO_OF_MERCHANTS))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingUpcomingDealsOfTheDayEventsSummaryIsFailed() throws Exception {
        when(dealsOfTheDayService.getDealsOfTheDaySummaryForAdmin())
                .thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.get(GET_UPCOMING_DEALS_OF_THE_DAY_EVENTS_SUMMARY_URI)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for set deals of the day event
     * Api context: api/v1/deals/deals-of-the-day/event/{userType}
     */
    @Test
    void Should_ReturnOk_When_SettingDealsOfTheDayEventIsSuccessful() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenReturn(getSampleBusinessMerchantResponseDto());
        when(dealsOfTheDayService.addDealsOfTheDayEvent(any(DealsOfTheDayEventRequestDto.class),
                any(UserType.class), any(BusinessMerchantResponseDto.class)))
                .thenReturn(getSampleDealsOfTheDayEvent());

        String successMessage = resourceBundleMessageSource.getMessage(
                String.valueOf(SuccessResponseStatusType.DEALS_OF_THE_DAY_EVENT_CREATED.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.DEALS_OF_THE_DAY_EVENT_CREATED.getCode())))
                .thenReturn(successMessage);

        String uri = DEALS_OF_THE_DAY_EVENT_URI.replace("{userType}", USER_TYPE_BANK.toString());

        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)            // <-- add this
                        .content(getSampleDealsOfTheDayEventRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.DEALS_OF_THE_DAY_EVENT_CREATED.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.DEALS_OF_THE_DAY_EVENT_CREATED.getCode()))
                .andExpect(jsonPath("$.data.id").value(DEAL_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }


    @Test
    void Should_ReturnBadRequest_When_SettingDealsOfTheDayEventForMissingRequiredFields() throws Exception {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = new DealsOfTheDayEventRequestDto();
        dealsOfTheDayEventRequestDto.setValidOn(System.currentTimeMillis() + DATE_IN_MILLISECONDS);
        dealsOfTheDayEventRequestDto.setEventId(EVENT_ID);
        dealsOfTheDayEventRequestDto.setDealIds(new HashSet<>());

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_URI.replace("{userType}", USER_TYPE_BANK.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(dealsOfTheDayEventRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_SettingDealsOfTheDayEventForInvalidFromDate() throws Exception {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();
        dealsOfTheDayEventRequestDto.setValidOn(System.currentTimeMillis());

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_VALID_FROM_DATE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_VALID_FROM_DATE.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_URI.replace("{userType}", USER_TYPE_BANK.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(dealsOfTheDayEventRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_VALID_FROM_DATE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_VALID_FROM_DATE.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_SettingDealsOfTheDayEventForInvalidUserIdOrType() throws Exception {
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        businessMerchantResponseDto.setProfileType(USER_TYPE_ADMIN.toString());

        when(authUserService.getBankBusinessByBankId(anyString(), anyString())).thenReturn(businessMerchantResponseDto);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_USER_ID_OR_TYPE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_USER_ID_OR_TYPE.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_URI.replace("{userType}", USER_TYPE_BANK.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleDealsOfTheDayEventRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_USER_ID_OR_TYPE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_USER_ID_OR_TYPE.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_SettingDealsOfTheDayEventForEventAlreadyExists() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenReturn(getSampleBusinessMerchantResponseDto());
        when(dealsOfTheDayService.addDealsOfTheDayEvent(any(DealsOfTheDayEventRequestDto.class), any(UserType.class),
                any(BusinessMerchantResponseDto.class))).thenThrow(new ExistingEventException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .EVENT_ALREADY_EXIST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.EVENT_ALREADY_EXIST.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_URI.replace("{userType}", USER_TYPE_BANK.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleDealsOfTheDayEventRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.EVENT_ALREADY_EXIST.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.EVENT_ALREADY_EXIST.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_SettingDealsOfTheDayEventForInvalidUserId() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new InvalidUserException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_USER_ID_OR_TYPE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_USER_ID_OR_TYPE.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_URI.replace("{userType}", USER_TYPE_BANK.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleDealsOfTheDayEventRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_USER_ID_OR_TYPE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_USER_ID_OR_TYPE.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_SettingDealsOfTheDayEventForInactiveDealId() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenReturn(getSampleBusinessMerchantResponseDto());
        when(dealsOfTheDayService.addDealsOfTheDayEvent(any(DealsOfTheDayEventRequestDto.class), any(UserType.class),
                any(BusinessMerchantResponseDto.class))).thenThrow(new InvalidDealException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INACTIVE_DEAL_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INACTIVE_DEAL_ID.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_URI.replace("{userType}", USER_TYPE_BANK.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(getSampleDealsOfTheDayEventRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INACTIVE_DEAL_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INACTIVE_DEAL_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_SettingDealsOfTheDayEventForSubscriptionPlanNotFount() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenReturn(getSampleBusinessMerchantResponseDto());
        when(dealsOfTheDayService.addDealsOfTheDayEvent(any(DealsOfTheDayEventRequestDto.class), any(UserType.class),
                any(BusinessMerchantResponseDto.class))).thenThrow(new InvalidSubscribeException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .SUBSCRIPTION_PLAN_NOT_FOUND.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.SUBSCRIPTION_PLAN_NOT_FOUND.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_URI.replace("{userType}", USER_TYPE_BANK.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(getSampleDealsOfTheDayEventRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.SUBSCRIPTION_PLAN_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.SUBSCRIPTION_PLAN_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_SettingDealsOfTheDayEventForDealsOfTheDaySlotUnavailable() throws Exception {
        String errorMessage = "Deals of the day slot is not available on: " +
                new Date(getSampleDealsOfTheDayEventRequestDto().getValidOn());
        LimitExceededType limitExceededType = LimitExceededType.DEALS_PER_DOD;

        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenReturn(getSampleBusinessMerchantResponseDto());
        when(dealsOfTheDayService.addDealsOfTheDayEvent(any(DealsOfTheDayEventRequestDto.class), any(UserType.class),
                any(BusinessMerchantResponseDto.class))).thenThrow(new LimitExceededException(errorMessage,
                limitExceededType));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .DEALS_PER_DOD_EXCEEDED.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.DEALS_PER_DOD_EXCEEDED.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_URI.replace("{userType}", USER_TYPE_BANK.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(getSampleDealsOfTheDayEventRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.DEALS_PER_DOD_EXCEEDED.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.DEALS_PER_DOD_EXCEEDED.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_SettingDealsOfTheDayEventIsFailed() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_URI.replace("{userType}", USER_TYPE_BANK.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleDealsOfTheDayEventRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for update deals of the day event
     * Api context: api/v1/deals/deals-of-the-day/event/{userType}
     */
    @Test
    void Should_ReturnOk_When_UpdatingDealsOfTheDayEventIsSuccessful() throws Exception {
        when(dealsOfTheDayService.updateDealsOfTheDayEvent(any(DealsOfTheDayEventRequestDto.class), anyString(),
                any(UserType.class))).thenReturn(getSampleDealsOfTheDayEvent());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .DEALS_OF_THE_DAY_EVENT_UPDATED.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.DEALS_OF_THE_DAY_EVENT_UPDATED.getCode())))
                .thenReturn(successMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_URI.replace("{userType}", USER_TYPE_BANK.toString());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .content(getSampleDealsOfTheDayEventRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.DEALS_OF_THE_DAY_EVENT_UPDATED.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.DEALS_OF_THE_DAY_EVENT_UPDATED.getCode()))
                .andExpect(jsonPath("$.data.id").value(DEAL_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingDealsOfTheDayEventForMissingRequiredFields() throws Exception {
        DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto = getSampleDealsOfTheDayEventRequestDto();
        dealsOfTheDayEventRequestDto.setEventId(null);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_URI.replace("{userType}", USER_TYPE_BANK.toString());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .content(dealsOfTheDayEventRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingDealsOfTheDayEventForSubscriptionPlanNotFound() throws Exception {
        when(dealsOfTheDayService.updateDealsOfTheDayEvent(any(DealsOfTheDayEventRequestDto.class), anyString(),
                any(UserType.class))).thenThrow(new InvalidSubscribeException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .SUBSCRIPTION_PLAN_NOT_FOUND.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.SUBSCRIPTION_PLAN_NOT_FOUND.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_URI.replace("{userType}", USER_TYPE_BANK.toString());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .content(getSampleDealsOfTheDayEventRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.SUBSCRIPTION_PLAN_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.SUBSCRIPTION_PLAN_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingDealsOfTheDayEventForDealsPerEventLimitExceeded() throws Exception {
        String errorMessage = "Number of deals per event limit exceeded for userId: " + MERCHANT_ID;
        LimitExceededType limitExceededType = LimitExceededType.DEALS_PER_DOD_EVENT;

        when(dealsOfTheDayService.updateDealsOfTheDayEvent(any(DealsOfTheDayEventRequestDto.class), anyString(),
                any(UserType.class))).thenThrow(new LimitExceededException(errorMessage, limitExceededType));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .DEALS_PER_DOD_EVENT_EXCEEDED.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.DEALS_PER_DOD_EVENT_EXCEEDED.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_URI.replace("{userType}", USER_TYPE_BANK.toString());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .content(getSampleDealsOfTheDayEventRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.DEALS_PER_DOD_EVENT_EXCEEDED.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.DEALS_PER_DOD_EVENT_EXCEEDED.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingDealsOfTheDayEventForInactiveDealId() throws Exception {
        when(dealsOfTheDayService.updateDealsOfTheDayEvent(any(DealsOfTheDayEventRequestDto.class), anyString(),
                any(UserType.class))).thenThrow(new InvalidDealException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INACTIVE_DEAL_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INACTIVE_DEAL_ID.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_URI.replace("{userType}", USER_TYPE_BANK.toString());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .content(getSampleDealsOfTheDayEventRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INACTIVE_DEAL_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INACTIVE_DEAL_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingDealsOfTheDayEventForInvalidEventId() throws Exception {
        when(dealsOfTheDayService.updateDealsOfTheDayEvent(any(DealsOfTheDayEventRequestDto.class), anyString(),
                any(UserType.class))).thenThrow(new InvalidDealsOfTheDayEventException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_EVENT_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_EVENT_ID.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_URI.replace("{userType}", USER_TYPE_BANK.toString());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .content(getSampleDealsOfTheDayEventRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_EVENT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_EVENT_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_UpdatingDealsOfTheDayEventIsFailed() throws Exception {
        when(dealsOfTheDayService.updateDealsOfTheDayEvent(any(DealsOfTheDayEventRequestDto.class), anyString(),
                any(UserType.class))).thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_URI.replace("{userType}", USER_TYPE_BANK.toString());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .content(getSampleDealsOfTheDayEventRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for delete deals of the day event
     * Api context: api/v1/deals/deals-of-the-day/event/{eventId}
     */
    @Test
    void Should_ReturnOk_When_DeletingDealsOfTheDayEventIsSuccessful() throws Exception {
        doNothing().when(dealsOfTheDayService).deleteDealsOfTheDayEvent(anyString(), anyString());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .DEALS_OF_THE_DAY_EVENT_DELETED.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.DEALS_OF_THE_DAY_EVENT_DELETED.getCode())))
                .thenReturn(successMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_BY_EVENT_ID_URI.replace("{eventId}", EVENT_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.DEALS_OF_THE_DAY_EVENT_DELETED.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.DEALS_OF_THE_DAY_EVENT_DELETED.getCode()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_DeletingDealsOfTheDayEventForSubscriptionPlanNotFound() throws Exception {
        doThrow(new InvalidSubscribeException(FAILED)).when(dealsOfTheDayService)
                .deleteDealsOfTheDayEvent(anyString(), anyString());

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .SUBSCRIPTION_PLAN_NOT_FOUND.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.SUBSCRIPTION_PLAN_NOT_FOUND.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_BY_EVENT_ID_URI.replace("{eventId}", EVENT_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.SUBSCRIPTION_PLAN_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.SUBSCRIPTION_PLAN_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_DeletingDealsOfTheDayEventForInvalidEventId() throws Exception {
        doThrow(new InvalidDealsOfTheDayEventException(FAILED)).when(dealsOfTheDayService)
                .deleteDealsOfTheDayEvent(anyString(), anyString());

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_EVENT_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_EVENT_ID.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_BY_EVENT_ID_URI.replace("{eventId}", EVENT_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_EVENT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_EVENT_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_DeletingDealsOfTheDayEventIsFailed() throws Exception {
        doThrow(new KupiCoreException(FAILED)).when(dealsOfTheDayService)
                .deleteDealsOfTheDayEvent(anyString(), anyString());

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_BY_EVENT_ID_URI.replace("{eventId}", EVENT_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get deals of the day event by id
     * Api context: api/v1/deals/deals-of-the-day/event/{eventId}
     */
    @Test
    void Should_ReturnOk_When_GettingDealsOfTheDayEventByIdIsSuccessful() throws Exception {
        DealsOfTheDayEvent dealsOfTheDayEvent = getSampleDealsOfTheDayEvent();
        dealsOfTheDayEvent.setUserType(USER_TYPE_MERCHANT);
        dealsOfTheDayEvent.setUserId(MERCHANT_ID);
        dealsOfTheDayEvent.setValidOn(new Date(DATE_IN_MILLISECONDS));
        when(dealsOfTheDayService.getDealsOfTheDayEventById(anyString())).thenReturn(dealsOfTheDayEvent);
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString()))
                .thenReturn(new BusinessMerchantResponseDto());
        when(dealSearchService.getDealListByDealIdList(anyList())).thenReturn(new ArrayList<>());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_DEALS_OF_THE_DAY.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_DEALS_OF_THE_DAY.getCode())))
                .thenReturn(successMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_BY_EVENT_ID_URI.replace("{eventId}", EVENT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_DEALS_OF_THE_DAY.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_DEALS_OF_THE_DAY.getCode()))
                .andExpect(jsonPath("$.data.id").value(DEAL_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingDealsOfTheDayEventByIdForInvalidEventId() throws Exception {
        when(dealsOfTheDayService.getDealsOfTheDayEventById(anyString()))
                .thenThrow(new InvalidDealsOfTheDayEventException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_EVENT_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_EVENT_ID.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_BY_EVENT_ID_URI.replace("{eventId}", EVENT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_EVENT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_EVENT_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingDealsOfTheDayEventByIdForInvalidUserId() throws Exception {
        DealsOfTheDayEvent dealsOfTheDayEvent = getSampleDealsOfTheDayEvent();
        dealsOfTheDayEvent.setUserType(USER_TYPE_MERCHANT);
        dealsOfTheDayEvent.setUserId(MERCHANT_ID);
        dealsOfTheDayEvent.setValidOn(new Date(DATE_IN_MILLISECONDS));

        when(dealsOfTheDayService.getDealsOfTheDayEventById(anyString())).thenReturn(dealsOfTheDayEvent);
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString()))
                .thenThrow(new InvalidUserException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_USER_ID_OR_TYPE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_USER_ID_OR_TYPE.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_BY_EVENT_ID_URI.replace("{eventId}", EVENT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_USER_ID_OR_TYPE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_USER_ID_OR_TYPE.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingDealsOfTheDayEventByIdIsFailed() throws Exception {
        when(dealsOfTheDayService.getDealsOfTheDayEventById(anyString())).thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = DEALS_OF_THE_DAY_EVENT_BY_EVENT_ID_URI.replace("{eventId}", EVENT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    private Page<Deal> getSampleDealPage() {
        List<Deal> dealList = new ArrayList<>();
        dealList.add(getSampleDeal());
        return new PageImpl<>(dealList);
    }

    private Map<String, MerchantBusinessResponseDto> getSampleMerchantBusinessResponseDtoMap() {
        MerchantBusinessResponseDto dto = getSampleMerchantBusinessResponseDto();
        Map<String, MerchantBusinessResponseDto> map = new HashMap<>();
        map.put(dto.getId(), dto);
        return map;
    }

    private MerchantBusinessResponseDto getSampleMerchantBusinessResponseDto() {
        MerchantBusinessResponseDto dto = new MerchantBusinessResponseDto();
        dto.setId(MERCHANT_ID);
        dto.setUserType(USER_TYPE_BANK.toString());
        return dto;
    }

    private List<DealsOfTheDayBasicEventResponseDto> getSampleDealsOfTheDayBasicEventResponseDtoList() {
        DealsOfTheDayBasicEventResponseDto dealsOfTheDayBasicEventResponseDto = new DealsOfTheDayBasicEventResponseDto(
                getSampleBasicDealResponseDtoList(), new Date(DATE_IN_MILLISECONDS), TIME_ZONE);
        List<DealsOfTheDayBasicEventResponseDto> dealsOfTheDayBasicEventResponseDtoList = new ArrayList<>();
        dealsOfTheDayBasicEventResponseDtoList.add(dealsOfTheDayBasicEventResponseDto);
        return dealsOfTheDayBasicEventResponseDtoList;
    }

    private List<BasicDealResponseDto> getSampleBasicDealResponseDtoList() {
        BasicDealResponseDto basicDealResponseDto = new BasicDealResponseDto(getSampleDeal(),
                getSampleBasicMerchantBusinessResponseDto(), TIME_ZONE);
        List<BasicDealResponseDto> basicDealResponseDtoList = new ArrayList<>();
        basicDealResponseDtoList.add(basicDealResponseDto);
        return basicDealResponseDtoList;
    }

    private Deal getSampleDeal() {
        Deal deal = new Deal();
        deal.setId(DEAL_ID);
        deal.setMerchantId(MERCHANT_ID);
        return deal;
    }

    private BasicMerchantBusinessResponseDto getSampleBasicMerchantBusinessResponseDto() {
        BasicMerchantBusinessResponseDto basicMerchantBusinessResponseDto = new BasicMerchantBusinessResponseDto();
        basicMerchantBusinessResponseDto.setUserType(USER_TYPE_BANK.toString());
        return basicMerchantBusinessResponseDto;
    }

    private Page<DealsOfTheDayEventSearchIndex> getSampleDealsOfTheDayEventSearchIndexPage() {
        DealsOfTheDayEventSearchIndex dealsOfTheDayEventSearchIndex = new DealsOfTheDayEventSearchIndex();
        dealsOfTheDayEventSearchIndex.setId(DEAL_ID);
        dealsOfTheDayEventSearchIndex.setValidOn(new Date(DATE_IN_MILLISECONDS));
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
        businessMerchantResponseDto.setProfileType(USER_TYPE_BANK.toString());
        return businessMerchantResponseDto;
    }

    private DealsOfTheDayEvent getSampleDealsOfTheDayEvent() {
        DealsOfTheDayEvent dealsOfTheDayEvent = new DealsOfTheDayEvent();
        dealsOfTheDayEvent.setId(DEAL_ID);
        return dealsOfTheDayEvent;
    }

    /**
     * This method creates sample coupon statistics
     *
     * @return CouponStatistics
     */
    private CouponStatistics getSampleCouponStatistics() {
        Deal deal = getSampleDeal();
        CouponStatistics couponStatistics = new CouponStatistics();
        couponStatistics.setTotalCoupons(10);
        couponStatistics.setRemainingCoupons(5);
        couponStatistics.setId("cousid-12589-9658-7856");
        couponStatistics.setDeal(deal);
        couponStatistics.setMerchantId(MERCHANT_ID);
        couponStatistics.setRedeemedCoupons(4);
        return couponStatistics;
    }

    /**
     * This method creates sample DealSearchIndex
     *
     * @return DealSearchIndex
     */
    private com.kupi.cc.base.domain.entity.DealSearchIndex getSampleDealSearchIndex() {
        com.kupi.cc.base.domain.entity.DealSearchIndex dealSearchIndex = new com.kupi.cc.base.domain.entity.DealSearchIndex();
        dealSearchIndex.setId(DEAL_ID);
        dealSearchIndex.setApprovalStatus(com.kupi.cc.base.enums.ApprovalStatus.APPROVED);
        dealSearchIndex.setDeleted(false);
        dealSearchIndex.setTitle("Test Deal");
        dealSearchIndex.setActiveMerchant(true);
        dealSearchIndex.setDealSource(USER_TYPE_BANK);
        dealSearchIndex.setValidFrom(System.currentTimeMillis() - 10000);
        dealSearchIndex.setExpiredOn(System.currentTimeMillis() + 10000);
        return dealSearchIndex;
    }
}