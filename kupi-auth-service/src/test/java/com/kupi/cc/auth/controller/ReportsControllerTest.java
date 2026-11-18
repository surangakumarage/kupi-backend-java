package com.kupi.cc.auth.controller;

import com.kupi.cc.auth.configuration.ResourceBundleMessageSourceBean;
import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.domain.entity.Business;
import com.kupi.cc.auth.domain.entity.User;
import com.kupi.cc.auth.domain.request.GroupedReportRequestDto;
import com.kupi.cc.auth.domain.request.ReportRequestDto;
import com.kupi.cc.auth.domain.response.BusinessProfileViewsResponseDto;
import com.kupi.cc.auth.domain.response.GroupedProfileViewsResponseDto;
import com.kupi.cc.auth.enums.*;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.exception.InvalidUserException;
import com.kupi.cc.auth.service.MerchantAudienceReachReportService;
import org.junit.jupiter.api.AfterEach;
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
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests {@link ReportsController} class
 */
class ReportsControllerTest {

    private static final String ROLE_TYPE_MERCHANT = RoleType.MERCHANT.toString();
    private static final String ROLE_TYPE_GUEST = RoleType.GUEST.toString();
    private static final String BUSINESS_ID = "bisid-123456789";
    private static final String USER_ID_HEADER = "User-Id";
    private static final String USER_ID = "uid-123456789";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String FAILED = "failed";
    private static final String TODAY = "TODAY";
    private static final String INVALID_DATE_OPTION = "INVALID_DATE_OPTION";
    private static final int ROWS = 1_000;
    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final long START_DATE = 1_659_292_200_000L;
    private static final long END_DATE = 1_660_761_000_000L;
    private static final long VIEW_COUNT = 1_000_000L;
    private static final String AUDIENCE_REACH_REPORT_FOR_BUSINESS_PROFILE_URI = "/api/v1/reports/{roleType}/{toUserId}/audience-reach/{page}/{size}";
    private static final String AUDIENCE_REACH_REPORT_FOR_TOP_TEN_BUSINESS_PROFILES_URI = "/api/v1/reports/{roleType}/audience-reach/top-ten/{option}";
    private static final String GROUPED_AUDIENCE_REACH_REPORT_FOR_BUSINESS_PROFILE_URI = "/api/v1/reports/{roleType}/grouped-audience-reach/{toUserId}/{page}/{size}";

    private final ResourceBundleMessageSourceBean resourceBundleMessageSourceBean = new ResourceBundleMessageSourceBean();
    Locale locale = LocaleContextHolder.getLocale();
    @Mock
    private Translator translator;
    @Mock
    private MerchantAudienceReachReportService merchantAudienceReachReportService;
    private MockMvc mockMvc;
    private ResourceBundleMessageSource resourceBundleMessageSource;

    @BeforeEach
    void setUp() {
        initMocks(this);
        ReportsController reportsController = new ReportsController(translator, merchantAudienceReachReportService);

        // Create a no-op Spring Validator for MockMvc setup
        org.springframework.validation.Validator noOpValidator = new org.springframework.validation.Validator() {
            @Override
            public boolean supports(Class<?> clazz) {
                return true;
            }

            @Override
            public void validate(Object target, Errors errors) {
                // No-op validator - doesn't validate anything
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(reportsController)
                .setValidator(noOpValidator)
                .build();
        this.resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Start of tests for audience reach report for business profile
     * Api context: /api/v1/reports/{roleType}/{toUserId}/audience-reach/{page}/{size}
     */
    @Test
    void Should_ReturnOk_When_AudienceReachReportForBusinessProfileIsSuccessful() throws Exception {
        when(merchantAudienceReachReportService.generateAudienceReachReport(any(ReportRequestDto.class), anyString(),
                anyInt(), anyInt(), anyString(), any(RoleType.class)))
                .thenReturn(getSampleBusinessProfileViewsResponseDtoList());

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType
                .GET_BUSINESS_PROFILE_VIEWS.getCode(), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.GET_BUSINESS_PROFILE_VIEWS.getCode()))
                .thenReturn(successMessage);
        String uri = AUDIENCE_REACH_REPORT_FOR_BUSINESS_PROFILE_URI.replace("{roleType}", ROLE_TYPE_MERCHANT)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{toUserId}", USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(getSampleReportRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.GET_BUSINESS_PROFILE_VIEWS
                        .getMessage()))
                .andExpect(jsonPath("$.data.views[0].viewCount").value(VIEW_COUNT))
                .andExpect(jsonPath("$.data.views[0].merchant.id").value(USER_ID))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_AudienceReachReportForBusinessProfileForInvalidUserType() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_USER_TYPE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_USER_TYPE.getCode())))
                .thenReturn(invalidMessage);
        String uri = AUDIENCE_REACH_REPORT_FOR_BUSINESS_PROFILE_URI.replace("{roleType}", ROLE_TYPE_GUEST)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{toUserId}", USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(getSampleReportRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_TYPE
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_TYPE
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_AudienceReachReportForBusinessProfileForMissingRequiredFields() throws Exception {
        ReportRequestDto reportRequestDto = getSampleReportRequestDto();
        reportRequestDto.setOption(null);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        String uri = AUDIENCE_REACH_REPORT_FOR_BUSINESS_PROFILE_URI.replace("{roleType}", ROLE_TYPE_MERCHANT)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{toUserId}", USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(reportRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_AudienceReachReportForBusinessProfileForInvalidDateRange() throws Exception {
        ReportRequestDto reportRequestDto = getSampleReportRequestDto();
        reportRequestDto.setEndDate(0L);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_DATE_RANGE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_DATE_RANGE.getCode())))
                .thenReturn(invalidMessage);
        String uri = AUDIENCE_REACH_REPORT_FOR_BUSINESS_PROFILE_URI.replace("{roleType}", ROLE_TYPE_MERCHANT)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{toUserId}", USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(reportRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_DATE_RANGE
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_DATE_RANGE
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_AudienceReachReportForBusinessProfileForUnsupportedOptionForDateRange() throws Exception {
        ReportRequestDto reportRequestDto = getSampleReportRequestDto();
        reportRequestDto.setOption(ReportDateOption.MONTHLY);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .UNSUPPORTED_OPTION_FOR_DATE_RANGE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.UNSUPPORTED_OPTION_FOR_DATE_RANGE.getCode())))
                .thenReturn(invalidMessage);
        String uri = AUDIENCE_REACH_REPORT_FOR_BUSINESS_PROFILE_URI.replace("{roleType}", ROLE_TYPE_MERCHANT)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{toUserId}", USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(reportRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType
                        .UNSUPPORTED_OPTION_FOR_DATE_RANGE.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .UNSUPPORTED_OPTION_FOR_DATE_RANGE.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_AudienceReachReportForBusinessProfileForInvalidMerchantId() throws Exception {
        when(merchantAudienceReachReportService.generateAudienceReachReport(any(ReportRequestDto.class), anyString(),
                anyInt(), anyInt(), anyString(), any(RoleType.class))).thenThrow(new InvalidUserException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_MERCHANT_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode())))
                .thenReturn(invalidMessage);
        String uri = AUDIENCE_REACH_REPORT_FOR_BUSINESS_PROFILE_URI.replace("{roleType}", ROLE_TYPE_MERCHANT)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{toUserId}", USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(getSampleReportRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MERCHANT_ID
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_MERCHANT_ID
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_AudienceReachReportForBusinessProfileIsFailed() throws Exception {
        when(merchantAudienceReachReportService.generateAudienceReachReport(any(ReportRequestDto.class), anyString(),
                anyInt(), anyInt(), anyString(), any(RoleType.class))).thenThrow(new AuthServiceException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = AUDIENCE_REACH_REPORT_FOR_BUSINESS_PROFILE_URI.replace("{roleType}", ROLE_TYPE_MERCHANT)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{toUserId}", USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(getSampleReportRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for audience reach report for top ten business profiles
     * Api context: /api/v1/reports/{roleType}/audience-reach/top-ten/{option}
     */
    @Test
    void Should_ReturnOk_When_AudienceReachReportForTopTenBusinessProfilesIsSuccessful() throws Exception {
        when(merchantAudienceReachReportService.getTopTenViewCounts(any(GraphDateOption.class), anyString(),
                any(RoleType.class))).thenReturn(getSampleBusinessProfileViewsResponseDtoList());

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType
                .GET_BUSINESS_PROFILE_VIEWS.getCode(), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.GET_BUSINESS_PROFILE_VIEWS.getCode()))
                .thenReturn(successMessage);
        String uri = AUDIENCE_REACH_REPORT_FOR_TOP_TEN_BUSINESS_PROFILES_URI.replace("{roleType}", ROLE_TYPE_MERCHANT)
                .replace("{option}", TODAY);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.GET_BUSINESS_PROFILE_VIEWS
                        .getMessage()))
                .andExpect(jsonPath("$.data.views[0].viewCount").value(VIEW_COUNT))
                .andExpect(jsonPath("$.data.views[0].merchant.id").value(USER_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_AudienceReachReportForTopTenBusinessProfilesForInvalidUserType() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_USER_TYPE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_USER_TYPE.getCode())))
                .thenReturn(invalidMessage);
        String uri = AUDIENCE_REACH_REPORT_FOR_TOP_TEN_BUSINESS_PROFILES_URI.replace("{roleType}", ROLE_TYPE_GUEST)
                .replace("{option}", TODAY);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_TYPE
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_TYPE
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_AudienceReachReportForTopTenBusinessProfilesForInvalidDateOption() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_DATE_OPTION.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_DATE_OPTION.getCode())))
                .thenReturn(invalidMessage);
        String uri = AUDIENCE_REACH_REPORT_FOR_TOP_TEN_BUSINESS_PROFILES_URI.replace("{roleType}", ROLE_TYPE_MERCHANT)
                .replace("{option}", INVALID_DATE_OPTION);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_DATE_OPTION
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_DATE_OPTION
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_AudienceReachReportForTopTenBusinessProfilesIsFailed() throws Exception {
        when(merchantAudienceReachReportService.getTopTenViewCounts(any(GraphDateOption.class), anyString(),
                any(RoleType.class))).thenThrow(new AuthServiceException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = AUDIENCE_REACH_REPORT_FOR_TOP_TEN_BUSINESS_PROFILES_URI.replace("{roleType}", ROLE_TYPE_MERCHANT)
                .replace("{option}", TODAY);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for grouped audience reach report for business profile
     * Api context: /api/v1/reports/{roleType}/grouped-audience-reach/{toUserId}/{page}/{size}
     */
    @Test
    void Should_ReturnOk_When_GroupedAudienceReachReportForBusinessProfileIsSuccessful() throws Exception {
        when(merchantAudienceReachReportService.getReportForDateRange(anyLong(), anyLong(), anyString(), anyInt(),
                anyInt(), any(RoleType.class), anyString())).thenReturn(getSampleGroupedProfileViewsResponseDtoList());

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType
                .GET_BUSINESS_PROFILE_VIEWS.getCode(), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.GET_BUSINESS_PROFILE_VIEWS.getCode()))
                .thenReturn(successMessage);
        String uri = GROUPED_AUDIENCE_REACH_REPORT_FOR_BUSINESS_PROFILE_URI.replace("{roleType}", ROLE_TYPE_MERCHANT)
                .replace("{toUserId}", USER_ID).replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(getSampleGroupedReportRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.GET_BUSINESS_PROFILE_VIEWS
                        .getMessage()))
                .andExpect(jsonPath("$.data.views[0].totalViewCount").value(VIEW_COUNT))
                .andExpect(jsonPath("$.data.views[0].merchantId").value(USER_ID))
                .andExpect(jsonPath("$.data.totalItems").value(ROWS))
                .andExpect(jsonPath("$.data.totalPages").value(ROWS))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GroupedAudienceReachReportForBusinessProfileForInvalidUserType() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_USER_TYPE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_USER_TYPE.getCode())))
                .thenReturn(invalidMessage);
        String uri = GROUPED_AUDIENCE_REACH_REPORT_FOR_BUSINESS_PROFILE_URI.replace("{roleType}", ROLE_TYPE_GUEST)
                .replace("{toUserId}", USER_ID).replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(getSampleGroupedReportRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_TYPE
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_TYPE
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GroupedAudienceReachReportForBusinessProfileForMissingRequiredFields() throws Exception {
        GroupedReportRequestDto groupedReportRequestDto = new GroupedReportRequestDto();
        groupedReportRequestDto.setStartDate(0);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        String uri = GROUPED_AUDIENCE_REACH_REPORT_FOR_BUSINESS_PROFILE_URI.replace("{roleType}", ROLE_TYPE_MERCHANT)
                .replace("{toUserId}", USER_ID).replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(groupedReportRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GroupedAudienceReachReportForBusinessProfileForInvalidDateRange() throws Exception {
        GroupedReportRequestDto groupedReportRequestDto = getSampleGroupedReportRequestDto();
        groupedReportRequestDto.setEndDate(100L);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_DATE_RANGE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_DATE_RANGE.getCode())))
                .thenReturn(invalidMessage);
        String uri = GROUPED_AUDIENCE_REACH_REPORT_FOR_BUSINESS_PROFILE_URI.replace("{roleType}", ROLE_TYPE_MERCHANT)
                .replace("{toUserId}", USER_ID).replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(groupedReportRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_DATE_RANGE
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_DATE_RANGE
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GroupedAudienceReachReportForBusinessProfileIsFailed() throws Exception {
        when(merchantAudienceReachReportService.getReportForDateRange(anyLong(), anyLong(), anyString(), anyInt(),
                anyInt(), any(RoleType.class), anyString())).thenThrow(new AuthServiceException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = GROUPED_AUDIENCE_REACH_REPORT_FOR_BUSINESS_PROFILE_URI.replace("{roleType}", ROLE_TYPE_MERCHANT)
                .replace("{toUserId}", USER_ID).replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(getSampleGroupedReportRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    private ReportRequestDto getSampleReportRequestDto() {
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setStartDate(START_DATE);
        reportRequestDto.setEndDate(END_DATE);
        reportRequestDto.setOption(ReportDateOption.DAILY);
        return reportRequestDto;
    }

    private List<BusinessProfileViewsResponseDto> getSampleBusinessProfileViewsResponseDtoList() {
        BusinessProfileViewsResponseDto businessProfileViewsResponseDto = getSampleBusinessProfileViewsResponseDto();
        List<BusinessProfileViewsResponseDto> businessProfileViewsResponseDtoList = new ArrayList<>();
        businessProfileViewsResponseDtoList.add(businessProfileViewsResponseDto);
        return businessProfileViewsResponseDtoList;
    }

    private BusinessProfileViewsResponseDto getSampleBusinessProfileViewsResponseDto() {
        return new BusinessProfileViewsResponseDto(VIEW_COUNT, getSampleBusiness());
    }

    private Business getSampleBusiness() {
        Business business = new Business();
        business.setId(BUSINESS_ID);
        business.setMerchant(getSampleUser());
        return business;
    }

    private User getSampleUser() {
        User user = new User();
        user.setId(USER_ID);
        return user;
    }

    private GroupedReportRequestDto getSampleGroupedReportRequestDto() {
        GroupedReportRequestDto groupedReportRequestDto = new GroupedReportRequestDto();
        groupedReportRequestDto.setStartDate(START_DATE);
        groupedReportRequestDto.setEndDate(END_DATE);
        return groupedReportRequestDto;
    }

    private List<GroupedProfileViewsResponseDto> getSampleGroupedProfileViewsResponseDtoList() {
        List<GroupedProfileViewsResponseDto> groupedProfileViewsResponseDtoList = new ArrayList<>();
        groupedProfileViewsResponseDtoList.add(getSampleGroupedProfileViewsResponseDto());
        return groupedProfileViewsResponseDtoList;
    }

    private GroupedProfileViewsResponseDto getSampleGroupedProfileViewsResponseDto() {
        return new GroupedProfileViewsResponseDto(getSampleBusiness(), VIEW_COUNT, ROWS);
    }
}