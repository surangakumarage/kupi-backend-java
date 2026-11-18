package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.ResourceBundleMessageSourceBean;
import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.Subscription;
import com.kupi.cc.base.domain.response.BasicBusinessResponseDto;
import com.kupi.cc.base.domain.response.BasicSubscriptionPlanResponseDto;
import com.kupi.cc.base.domain.response.SubscriptionPlanListResponseDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.service.SubscriptionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests {@link SubscriptionController} class
 */
class SubscriptionControllerTest {

    private static final String USER_ID = "uid-123456789";
    private static final String USER_ID_HEADER = "User-Id";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String AUTH_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String AUTH_TOKEN_HEADER = "Auth-Token";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String SEARCH_TERM = "SEARCH_TERM";
    private static final String FILTER = "FILTER";
    private static final String BUSINESS_NAME = "BUSINESS_NAME";
    private static final String SUBSCRIPTION_ID = "sid-123456789";
    private static final String BUSINESS_ID = "bid-123456789";
    private static final int PAGE = 0;
    private static final int SIZE = 250;
    private static final String ERROR_STATUS = "ERROR";
    private static final String FAILED = "failed";
    private static final String APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8";
    private static final String GET_SUBSCRIPTION_LIST_URI = "/api/v1/subscription/list-subscription-payment/{page}/{size}/search/{searchTerm}/filter/{filter}";
    private static final String GET_SUBSCRIPTION_PLAN_URI = "/api/v1/subscription/subscription-plan";

    private final ResourceBundleMessageSourceBean resourceBundleMessageSourceBean = new ResourceBundleMessageSourceBean();
    Locale locale = LocaleContextHolder.getLocale();
    @Mock
    private Translator translator;
    @Mock
    private SubscriptionService subscriptionService;
    private MockMvc mockMvc;
    private ResourceBundleMessageSource resourceBundleMessageSource;

    @BeforeEach
    void setUp() {
        openMocks(this);
        SubscriptionController subscriptionController = new SubscriptionController(translator, subscriptionService);
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
        mockMvc = MockMvcBuilders.standaloneSetup(subscriptionController)
                .setValidator(noOpValidator)
                .build();
        resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks(this).close();
    }

    /**
     * Start of tests for get subscription list
     * Api context: /api/v1/subscription/list-subscription-payment/{page}/{size}/search/{searchTerm}/filter/{filter}
     */
    @Test
    void Should_ReturnOk_When_GettingSubscriptionListIsSuccessful() throws Exception {
        when(subscriptionService.getSubscriptionListBySubscriptionId(anyString(), anyString(), any(Pageable.class),
                anyString())).thenReturn(getSampleSubscriptionPage());
        when(subscriptionService.getMerchantsList(anyList(), anyString()))
                .thenReturn(getSampleBasicBusinessResponseDtoMap());

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType
                .getCodeString(SuccessResponseStatusType.READ_SUBSCRIBE_PAYMENT_LIST.getCode()), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.getCodeString(SuccessResponseStatusType
                .READ_SUBSCRIBE_PAYMENT_LIST.getCode()))).thenReturn(successMessage);
        String uri = GET_SUBSCRIPTION_LIST_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM)
                .replace("{filter}", FILTER);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_SUBSCRIBE_PAYMENT_LIST.getMessage()))
                .andExpect(jsonPath("$.data.subscriptions[0].id").value(SUBSCRIPTION_ID))
                .andExpect(jsonPath("$.data.subscriptions[0].merchantName").value(BUSINESS_NAME))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingSubscriptionListForInvalidUserId() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.INVALID_USER_ID.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType
                .INVALID_USER_ID.getCode()))).thenReturn(invalidMessage);
        String uri = GET_SUBSCRIPTION_LIST_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM)
                .replace("{filter}", FILTER);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, "")
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingSubscriptionListIsFailed() throws Exception {
        when(subscriptionService.getSubscriptionListBySubscriptionId(anyString(), anyString(), any(Pageable.class),
                anyString())).thenThrow(new KupiOrderPaymentException(FAILED));

        String uri = GET_SUBSCRIPTION_LIST_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM)
                .replace("{filter}", FILTER);
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
                .andExpect(jsonPath("$.displayMessage").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()));
    }

    /**
     * Start of tests for get subscription plan
     * Api context:  /api/v1/subscription/subscription-plan
     */
    @Test
    void Should_ReturnOk_When_GettingSubscriptionPlanIsSuccessful() throws Exception {
        when(subscriptionService.getSubscriptionPlan(anyString(), anyString(), anyString()))
                .thenReturn(getSampleSubscriptionPlanListResponseDto());

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType
                .getCodeString(SuccessResponseStatusType.READ_SUBSCRIBE_PAYMENT_COUNT.getCode()), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.getCodeString(SuccessResponseStatusType
                .READ_SUBSCRIBE_PAYMENT_COUNT.getCode()))).thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.get(GET_SUBSCRIPTION_PLAN_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_SUBSCRIBE_PAYMENT_COUNT.getMessage()))
                .andExpect(jsonPath("$.data.plans[0].id").value(SUBSCRIPTION_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingSubscriptionPlanIsFailed() throws Exception {
        when(subscriptionService.getSubscriptionPlan(anyString(), anyString(), anyString()))
                .thenThrow(new KupiOrderPaymentException(FAILED));

        mockMvc.perform(MockMvcRequestBuilders.get(GET_SUBSCRIPTION_PLAN_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()));
    }

    /**
     * This method returns a sample subscription
     *
     * @return Subscription
     */
    private Subscription getSampleSubscription() {
        Subscription subscription = new Subscription();
        subscription.setId(SUBSCRIPTION_ID);
        subscription.setUserId(USER_ID);
        subscription.setSubscriptionPlanId(SUBSCRIPTION_ID);
        return subscription;
    }

    /**
     * This method returns a sample subscription list
     *
     * @return Subscription List
     */
    private List<Subscription> getSampleSubscriptionList() {
        List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(getSampleSubscription());
        return subscriptionList;
    }

    /**
     * This method returns a sample subscription page
     *
     * @return Subscription Page
     */
    private Page<Subscription> getSampleSubscriptionPage() {
        return new PageImpl<>(getSampleSubscriptionList());
    }

    /**
     * This method returns a sample basic business response dto map
     *
     * @return BasicBusinessResponseDto Map
     */
    private Map<String, BasicBusinessResponseDto> getSampleBasicBusinessResponseDtoMap() {
        Map<String, BasicBusinessResponseDto> basicBusinessResponseDtoMap = new HashMap<>();
        basicBusinessResponseDtoMap.put(USER_ID, getSampleBasicBusinessResponseDto());
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
        basicBusinessResponseDto.setName(BUSINESS_NAME);
        return basicBusinessResponseDto;
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
}