package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.ResourceBundleMessageSourceBean;
import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.response.AllMerchantListResponseDto;
import com.kupi.cc.base.domain.response.MerchantTableListResponseDto;
import com.kupi.cc.base.domain.response.MerchantTableResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.AdminDashboardSummaryResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.MerchantSummaryResponseDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.service.SummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests {@link SummaryV2Controller} class
 */
class SummaryV2ControllerTest {

    private static final String USER_ID_HEADER = "User-Id";
    private static final String USER_ID = "uid-123456789";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String AUTH_TOKEN_HEADER = "Auth-Token";
    private static final String AUTH_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String START_DATE = "2024-01-01";
    private static final String END_DATE = "2024-01-31";
    private static final String INVALID_START_DATE = "2024-01-31";
    private static final String INVALID_END_DATE = "2024-01-01";
    private static final String SEARCH_TERM = "test";
    private static final int PAGE = 0;
    private static final int SIZE = 10;
    private static final long TOTAL_MERCHANTS = 100L;
    private static final long TOTAL_ACTIVE_MOBILE_USERS = 500L;
    private static final long TOTAL_ACTIVE_DEALS = 200L;
    private static final long TOTAL_ACTIVE_COUPONS = 150L;
    private static final int TOTAL_ACTIVE_DEALS_MERCHANT = 10;
    private static final int DEALS_OF_THE_DAY = 5;
    private static final int ACTIVE_COUPON = 8;
    private static final int REDEEMED_COUPON = 3;
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String FAILED = "failed";
    private static final String APPLICATION_JSON = "application/json";

    private static final String GET_SUMMARY_URI = "/api/v2/summary/{userType}/filter";
    private static final String GET_ALL_MERCHANTS_URI = "/api/v2/summary/{userType}/merchants";

    private final ResourceBundleMessageSourceBean resourceBundleMessageSourceBean = new ResourceBundleMessageSourceBean();
    Locale locale = LocaleContextHolder.getLocale();
    private ResourceBundleMessageSource resourceBundleMessageSource;
    private MockMvc mockMvc;
    @Mock
    private Translator translator;
    @Mock
    private SummaryService summaryService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        SummaryV2Controller summaryV2Controller = new SummaryV2Controller(translator, summaryService);
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
        mockMvc = MockMvcBuilders.standaloneSetup(summaryV2Controller)
                .setValidator(noOpValidator)
                .build();
        resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();
    }


    /**
     * Start of tests for get summary
     * Api context: api/v2/summary/{userType}/filter
     */
    @Test
    void Should_ReturnOk_When_GettingAdminSummaryIsSuccessful() throws Exception {
        when(summaryService.getAdminSummary(anyString(), anyString(), any(UserType.class), anyString()))
                .thenReturn(getSampleAdminDashboardSummaryResponseDto());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .SUCCESSFULLY_RETURNED_SUMMARY.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY.getCode())))
                .thenReturn(successMessage);
        String uri = GET_SUMMARY_URI.replace("{userType}", UserType.ADMIN.toString());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY.getCode()))
                .andExpect(jsonPath("$.data.totalMerchants").value(TOTAL_MERCHANTS))
                .andExpect(jsonPath("$.data.totalActiveMobileUsers").value(TOTAL_ACTIVE_MOBILE_USERS))
                .andExpect(jsonPath("$.data.totalLiveDeals").value(TOTAL_ACTIVE_DEALS))
                .andExpect(jsonPath("$.data.totalRedeemCoupons").value(TOTAL_ACTIVE_COUPONS))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnOk_When_GettingMerchantSummaryWithDatesIsSuccessful() throws Exception {
        when(summaryService.getMerchantSummary(anyString(), anyLong(), anyLong(), anyString()))
                .thenReturn(getSampleMerchantSummaryResponseDto());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .SUCCESSFULLY_RETURNED_SUMMARY.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY.getCode())))
                .thenReturn(successMessage);
        String uri = GET_SUMMARY_URI.replace("{userType}", UserType.MERCHANT.toString());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .param("startDate", START_DATE)
                        .param("endDate", END_DATE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY.getCode()))
                .andExpect(jsonPath("$.data.totalActiveDeals").value(TOTAL_ACTIVE_DEALS_MERCHANT))
                .andExpect(jsonPath("$.data.dealsOfTheDay").value(DEALS_OF_THE_DAY))
                .andExpect(jsonPath("$.data.activeCoupon").value(ACTIVE_COUPON))
                .andExpect(jsonPath("$.data.redeemedCoupon").value(REDEEMED_COUPON))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnOk_When_GettingMerchantSummaryWithoutDatesIsSuccessful() throws Exception {
        when(summaryService.getMerchantSummary(anyString(), anyLong(), anyLong(), anyString()))
                .thenReturn(getSampleMerchantSummaryResponseDto());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .SUCCESSFULLY_RETURNED_SUMMARY.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY.getCode())))
                .thenReturn(successMessage);
        String uri = GET_SUMMARY_URI.replace("{userType}", UserType.MERCHANT.toString());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY.getCode()))
                .andExpect(jsonPath("$.data.totalActiveDeals").value(TOTAL_ACTIVE_DEALS_MERCHANT))
                .andExpect(jsonPath("$.data.dealsOfTheDay").value(DEALS_OF_THE_DAY))
                .andExpect(jsonPath("$.data.activeCoupon").value(ACTIVE_COUPON))
                .andExpect(jsonPath("$.data.redeemedCoupon").value(REDEEMED_COUPON))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingSummaryIsFailed() throws Exception {
        when(summaryService.getAdminSummary(anyString(), anyString(), any(UserType.class), anyString()))
                .thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_SUMMARY_URI.replace("{userType}", UserType.ADMIN.toString());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
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

    @Test
    void Should_ReturnInternalServerError_When_StartDateIsAfterEndDate() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_SUMMARY_URI.replace("{userType}", UserType.MERCHANT.toString());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .param("startDate", INVALID_START_DATE)
                        .param("endDate", INVALID_END_DATE)
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
     * Start of tests for get all merchants
     * Api context: api/v2/summary/{userType}/merchants
     */
    @Test
    void Should_ReturnOk_When_GettingAllMerchantsIsSuccessful() throws Exception {
        when(summaryService.getMerchantAndDealCount(anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(getSampleMerchantTableListResponseDto());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .SUCCESSFULLY_RETURNED_SUMMARY.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY.getCode())))
                .thenReturn(successMessage);
        String uri = GET_ALL_MERCHANTS_URI.replace("{userType}", UserType.ADMIN.toString());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .param("searchTerm", SEARCH_TERM)
                        .param("page", String.valueOf(PAGE))
                        .param("size", String.valueOf(SIZE))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(100L))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnOk_When_GettingAllMerchantsWithDefaultPaginationIsSuccessful() throws Exception {
        when(summaryService.getMerchantAndDealCount(anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(getSampleMerchantTableListResponseDto());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .SUCCESSFULLY_RETURNED_SUMMARY.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY.getCode())))
                .thenReturn(successMessage);
        String uri = GET_ALL_MERCHANTS_URI.replace("{userType}", UserType.ADMIN.toString());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingAllMerchantsForNonAdminUserType() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .UNSUPPORTED_USER_TYPE_ACTION.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_ALL_MERCHANTS_URI.replace("{userType}", UserType.MERCHANT.toString());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
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
    void Should_ReturnInternalServerError_When_GettingAllMerchantsIsFailed() throws Exception {
        doThrow(new KupiCoreException(FAILED)).when(summaryService)
                .getMerchantAndDealCount(any(), anyInt(), anyInt(), anyString(), anyString());

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_ALL_MERCHANTS_URI.replace("{userType}", UserType.ADMIN.toString());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
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

    private AdminDashboardSummaryResponseDto getSampleAdminDashboardSummaryResponseDto() {
        return new AdminDashboardSummaryResponseDto(TOTAL_MERCHANTS, TOTAL_ACTIVE_MOBILE_USERS,
                TOTAL_ACTIVE_DEALS, TOTAL_ACTIVE_COUPONS);
    }

    private MerchantSummaryResponseDto getSampleMerchantSummaryResponseDto() {
        return new MerchantSummaryResponseDto(TOTAL_ACTIVE_DEALS_MERCHANT, DEALS_OF_THE_DAY,
                ACTIVE_COUPON, REDEEMED_COUPON);
    }

    private MerchantTableListResponseDto getSampleMerchantTableListResponseDto() {
        AllMerchantListResponseDto allMerchantListResponseDto = new AllMerchantListResponseDto(100L, PAGE, SIZE, new ArrayList<>());
        List<MerchantTableResponseDto> merchants = new ArrayList<>();
        return new MerchantTableListResponseDto(allMerchantListResponseDto, merchants);
    }
}

