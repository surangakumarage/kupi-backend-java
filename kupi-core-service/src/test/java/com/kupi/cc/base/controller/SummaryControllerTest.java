package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.ResourceBundleMessageSourceBean;
import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.response.dashboard.FullSummaryResponseDto;
import com.kupi.cc.base.domain.response.dashboard.TodaySummaryResponseDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.InvalidUserException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.service.SummaryService;
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

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests {@link SummaryController} class
 */
class SummaryControllerTest {

    private static final String USER_ID_HEADER = "User-Id";
    private static final String USER_ID = "uid-123456789";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String AUTH_TOKEN_HEADER = "Auth-Token";
    private static final String AUTH_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String TITLE = "Today's Summary";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String FAILED = "failed";
    private static final String APPLICATION_JSON = "application/json";

    private static final String GET_TODAY_SUMMARY_URI = "/api/v1/summary/{userType}";

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
        SummaryController summaryController = new SummaryController(translator, summaryService);
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
        mockMvc = MockMvcBuilders.standaloneSetup(summaryController)
                .setValidator(noOpValidator)
                .build();
        resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Start of tests for get today summary
     * Api context: api/v1/summary/{userType}
     */
    @Test
    void Should_ReturnOk_When_GettingTodaySummaryIsSuccessful() throws Exception {
        when(summaryService.getFullSummary(anyString(), any(UserType.class), anyString(), anyString()))
                .thenReturn(getSampleFullSummaryResponseDto());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .SUCCESSFULLY_RETURNED_SUMMARY.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY.getCode())))
                .thenReturn(successMessage);
        String uri = GET_TODAY_SUMMARY_URI.replace("{userType}", UserType.BANK.toString());
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
                .andExpect(jsonPath("$.data.todaySummary.title").value(TITLE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingTodaySummaryForInvalidUserIdOrType() throws Exception {
        when(summaryService.getFullSummary(anyString(), any(UserType.class), anyString(), anyString()))
                .thenThrow(new InvalidUserException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_USER_ID_OR_TYPE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_USER_ID_OR_TYPE.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_TODAY_SUMMARY_URI.replace("{userType}", UserType.BANK.toString());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
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
    void Should_ReturnInternalServerError_When_GettingTodaySummaryIsFailed() throws Exception {
        when(summaryService.getFullSummary(anyString(), any(UserType.class), anyString(), anyString()))
                .thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_TODAY_SUMMARY_URI.replace("{userType}", UserType.BANK.toString());
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

    private FullSummaryResponseDto getSampleFullSummaryResponseDto() {
        FullSummaryResponseDto fullSummaryResponseDto = new FullSummaryResponseDto();
        fullSummaryResponseDto.setTodaySummary(getSampleTodaySummaryResponseDto());
        return fullSummaryResponseDto;
    }

    private TodaySummaryResponseDto getSampleTodaySummaryResponseDto() {
        TodaySummaryResponseDto todaySummaryResponseDto = new TodaySummaryResponseDto();
        todaySummaryResponseDto.setTitle(TITLE);
        return todaySummaryResponseDto;
    }
}