package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.Subscribe;
import com.kupi.cc.base.domain.entity.SubscribeSearchIndex;
import com.kupi.cc.base.domain.entity.SubscriptionPlan;
import com.kupi.cc.base.domain.entity.SubscriptionRenewal;
import com.kupi.cc.base.domain.request.*;
import com.kupi.cc.base.domain.response.BusinessMerchantResponseDto;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.enums.*;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.service.AuthUserService;
import com.kupi.cc.base.service.SubscribeSearchIndexService;
import com.kupi.cc.base.service.SubscriberService;
import com.kupi.cc.base.service.SubscriptionPlanService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests {@link SubscriptionPlanController} class
 */
class SubscriptionPlanControllerTest {

    private static final String CREATE_SUBSCRIPTION_URI = "/api/v1/subscription-plan";
    private static final String UPDATE_SUBSCRIPTION_URI = "/api/v1/subscription-plan";
    private static final String GET_SUBSCRIPTION_PLAN_BY_ID_URI = "/api/v1/subscription-plan/{subscriptionId}";
    private static final String SEARCH_SUBSCRIPTION_PLAN_LIST_URI = "/api/v1/subscription-plan/{userType}/{page}/{size}/search/{searchTerm}";
    private static final String SEARCH_ACTIVE_SUBSCRIPTION_PLAN_LIST_URI = "/api/v1/subscription-plan/ACTIVE/{userType}/{page}/{size}/search/{searchTerm}";
    private static final String CHANGE_STATUS_URI = "/api/v1/subscription-plan/status";
    private static final String DELETE_SUBSCRIPTION_PLAN_URI = "/api/v1/subscription-plan/{subscriptionId}";
    private static final String SUBSCRIBE_URI = "/api/v1/subscription-plan/subscribe";
    private static final String UNSUBSCRIBE_URI = "/api/v1/subscription-plan/{userType}/unsubscribe/{subscribedId}";
    private static final String SEARCH_SUBSCRIBERS_URI = "/api/v1/subscription-plan/{subscriptionPlanId}/subscribers/{page}/{size}/search/{searchTerm}";
    private static final String GET_SUBSCRIPTION_PLAN_BY_USER_ID = "/api/v1/subscription-plan/{userType}/{subscribeUserId}";
    private static final String GET_SUBSCRIPTION_PLAN_SUMMARY = "/api/v1/subscription-plan/{subscriptionPlanId}/summary";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String USER_ID = "uid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    private static final String MERCHANT_ID = "uid-123456789";
    private static final String MERCHANT_NAME = "NAME";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String SUBSCRIPTION_PLAN_ID = "spid-fc2aaeeb-9fe4-4584-a253-1831c8610600";
    private static final String SUBSCRIBE_ID = "sid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    private static final String ACCESS_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String SEARCH_TERM = "SEARCH TERM";
    private static final String UPDATED_AT = "updatedAt";
    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final int TOTAL_PAGES = 1;
    private static final int TOTAL_ITEMS = 1;
    private static final long SUBSCRIPTION_COUNT = 1;

    @Mock
    private SubscriptionPlanService subscriptionPlanService;
    @Mock
    private Translator translator;
    @Mock
    private SubscriberService subscriberService;
    @Mock
    private AuthUserService authUserService;
    @Mock
    private SubscribeSearchIndexService subscribeSearchIndexService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        initMocks(this);
        SubscriptionPlanController subscriptionPlanController = new SubscriptionPlanController(subscriptionPlanService,
                translator, subscriberService, authUserService, subscribeSearchIndexService);
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
        mockMvc = MockMvcBuilders.standaloneSetup(subscriptionPlanController)
                .setValidator(noOpValidator)
                .build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnOk_When_UserTypeEitherMerchantOrBankForCreateSubscriptionPlan() throws Exception {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionPlanRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.CREATE_SUBSCRIPTION_PLAN.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.CREATE_SUBSCRIPTION_PLAN.getCode()))
                .andExpect(jsonPath("$.data.id", startsWith("spid-")));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForCreateSubscriptionPlan() throws Exception {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setTitle("");
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionPlanRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_ExceedCharactersAreProvidedForCreateSubscriptionPlan() throws Exception {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setTitle("orem Ipsum is simply dummy text of the printing and typesetting industry." +
                " Lorem Ipsum have been the industry's");
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionPlanRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.CHARACTER_LIMIT_EXCEEDED_FOR_PLAN.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.CHARACTER_LIMIT_EXCEEDED_FOR_PLAN.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidAmountIsProvidedForCreateSubscriptionPlan() throws Exception {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setIsFree(true);
        subscriptionPlanRequestDto.setAmount(BigDecimal.ONE);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionPlanRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_AMOUNT.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_AMOUNT.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidNoOfDealsOfTheDayEventsPerMonthIsProvidedForCreateSubscriptionPlan() throws Exception {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setNoOfDealsOfTheDayEventsPerMonth(-1);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionPlanRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_NO_OF_DOD_EVENTS_PER_MONTH.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_NO_OF_DOD_EVENTS_PER_MONTH.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidNoOfDealsPerMonthIsProvidedForCreateSubscriptionPlan() throws Exception {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setNoOfDealsPerMonth(0);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionPlanRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_NO_OF_DEALS_PER_MONTH.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_NO_OF_DEALS_PER_MONTH.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidNoOfDealsOfTheDayPerEventIsProvidedForCreateSubscriptionPlan() throws Exception {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setNoOfDealsOfTheDayPerEvent(0);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionPlanRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_NO_OF_DOD_PER_EVENT.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_NO_OF_DOD_PER_EVENT.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_UserTypeNeitherMerchantNorBankIsProvidedForCreateSubscriptionPlan() throws Exception {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setUserRole(UserType.ADMIN);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionPlanRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ROLE.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ROLE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_ExistingSubscriptionPlanTitleIsProvidedForCreateSubscriptionPlan() throws Exception {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        doThrow(new ExistingNameException("ERROR")).when(subscriptionPlanService)
                .createSubscriptionPlan(any(SubscriptionPlan.class), any(UserType.class));
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionPlanRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.EXISTING_SUBSCRIPTION_PLAN_NAME.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.EXISTING_SUBSCRIPTION_PLAN_NAME.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_CreateSubscriptionPlanIsFailed() throws Exception {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = getSampleSubscriptionPlanRequestDto();
        doThrow(new KupiCoreException("ERROR")).when(subscriptionPlanService)
                .createSubscriptionPlan(any(SubscriptionPlan.class), any(UserType.class));
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionPlanRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidSubscriptionPlanIdIsProvidedForGetSubscriptionPlanById() throws Exception {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        when(subscriptionPlanService.getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID)).thenReturn(subscriptionPlan);
        String uri = GET_SUBSCRIPTION_PLAN_BY_ID_URI.replace("{subscriptionId}", SUBSCRIPTION_PLAN_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_SUBSCRIPTION_PLAN.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_SUBSCRIPTION_PLAN.getCode()))
                .andExpect(jsonPath("$.data.id", startsWith("spid-")));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidSubscriptionPlanIdIsProvidedForGetSubscriptionPlanById() throws Exception {
        doThrow(new InvalidSubscriptionPlanException("ERROR")).when(subscriptionPlanService)
                .getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID);
        String uri = GET_SUBSCRIPTION_PLAN_BY_ID_URI.replace("{subscriptionId}", SUBSCRIPTION_PLAN_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetSubscriptionPlanByIdIsFailed() throws Exception {
        doThrow(new KupiCoreException("ERROR")).when(subscriptionPlanService)
                .getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID);
        String uri = GET_SUBSCRIPTION_PLAN_BY_ID_URI.replace("{subscriptionId}", SUBSCRIPTION_PLAN_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_UserTypeEitherBankOrMerchantForSearchSubscriptionPlanList() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        Page<SubscriptionPlan> subscriptionPlanPage = getSampleSubscriptionPlanPage();
        when(subscriptionPlanService.getSubscriptionPlanPage(pageable, SEARCH_TERM, UserType.MERCHANT))
                .thenReturn(subscriptionPlanPage);
        String uri = SEARCH_SUBSCRIPTION_PLAN_LIST_URI.replace("{userType}", UserType.MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_SUBSCRIPTION_PLAN_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_SUBSCRIPTION_PLAN_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGES))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE));
    }

    @Test
    void Should_ReturnBadRequest_When_UserTypeNeitherMerchantNorBankIsProvidedForSearchSubscriptionPlanList() throws Exception {
        String uri = SEARCH_SUBSCRIPTION_PLAN_LIST_URI.replace("{userType}", UserType.ADMIN.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ROLE.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ROLE.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_SearchSubscriptionPlanListIsFailed() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        doThrow(new KupiCoreException("ERROR")).when(subscriptionPlanService)
                .getSubscriptionPlanPage(pageable, SEARCH_TERM, UserType.MERCHANT);
        String uri = SEARCH_SUBSCRIPTION_PLAN_LIST_URI.replace("{userType}", UserType.MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_UserTypeEitherBankOrMerchantForSearchActiveSubscriptionPlanList() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        Page<SubscriptionPlan> subscriptionPlanPage = getSampleSubscriptionPlanPage();
        when(subscriptionPlanService.getActiveSubscriptionPlan(pageable, SEARCH_TERM, UserType.MERCHANT))
                .thenReturn(subscriptionPlanPage);
        String uri = SEARCH_ACTIVE_SUBSCRIPTION_PLAN_LIST_URI.replace("{userType}", UserType.MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_SUBSCRIPTION_PLAN_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_SUBSCRIPTION_PLAN_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGES))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE));
    }

    @Test
    void Should_ReturnBadRequest_When_UserTypeNeitherMerchantNorBankIsProvidedForSearchActiveSubscriptionPlanList() throws Exception {
        String uri = SEARCH_ACTIVE_SUBSCRIPTION_PLAN_LIST_URI.replace("{userType}", UserType.ADMIN.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ROLE.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ROLE.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_SearchActiveSubscriptionPlanListIsFailed() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        doThrow(new KupiCoreException("ERROR")).when(subscriptionPlanService)
                .getActiveSubscriptionPlan(pageable, SEARCH_TERM, UserType.MERCHANT);
        String uri = SEARCH_ACTIVE_SUBSCRIPTION_PLAN_LIST_URI.replace("{userType}", UserType.MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_UserTypeEitherMerchantOrBankForUpdateSubscriptionPlan() throws Exception {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        when(subscriptionPlanService.updateSubscriptionPlan(any(SubscriptionUpdateRequestDto.class)))
                .thenReturn(subscriptionPlan);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.UPDATE_SUBSCRIPTION_PLAN.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.UPDATE_SUBSCRIPTION_PLAN.getCode()))
                .andExpect(jsonPath("$.data.id", startsWith("spid-")));
    }

    @Test
    void Should_ReturnBadRequest_When_UserTypeNeitherMerchantNorBankIsProvidedForUpdateSubscriptionPlan() throws Exception {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setUserRole(UserType.ADMIN);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ROLE.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ROLE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForUpdateSubscriptionPlan() throws Exception {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setId("");
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_ExceedCharactersAreProvidedForUpdateSubscriptionPlan() throws Exception {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setTitle("orem Ipsum is simply dummy text of the printing and typesetting industry." +
                " Lorem Ipsum have been the industry's");
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.CHARACTER_LIMIT_EXCEEDED_FOR_PLAN.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.CHARACTER_LIMIT_EXCEEDED_FOR_PLAN.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidAmountIsProvidedForUpdateSubscriptionPlan() throws Exception {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setIsFree(true);
        subscriptionUpdateRequestDto.setAmount(BigDecimal.ONE);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_AMOUNT.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_AMOUNT.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_ExistingSubscriptionPlanTitleIsProvidedForUpdateSubscriptionPlan() throws Exception {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        doThrow(new ExistingNameException("ERROR")).when(subscriptionPlanService)
                .updateSubscriptionPlan(any(SubscriptionUpdateRequestDto.class));
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.EXISTING_SUBSCRIPTION_PLAN_NAME.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.EXISTING_SUBSCRIPTION_PLAN_NAME.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_InvalidSubscriptionPlanIdIsProvidedForUpdateSubscriptionPlan() throws Exception {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        doThrow(new InvalidSubscriptionPlanException("ERROR")).when(subscriptionPlanService)
                .updateSubscriptionPlan(any(SubscriptionUpdateRequestDto.class));
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_UpdateSubscriptionPlanIsFailed() throws Exception {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        doThrow(new KupiCoreException("ERROR")).when(subscriptionPlanService)
                .updateSubscriptionPlan(any(SubscriptionUpdateRequestDto.class));
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_SUBSCRIPTION_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_RequiredFieldsAreProvidedForChangeStatus() throws Exception {
        SubscriptionPlanStatusRequestDto subscriptionPlanStatusRequestDto = getSampleSubscriptionPlanStatusRequestDto();
        mockMvc.perform(MockMvcRequestBuilders.put(CHANGE_STATUS_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionPlanStatusRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.UPDATE_SUBSCRIPTION_PLAN_STATUS.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.UPDATE_SUBSCRIPTION_PLAN_STATUS.getCode()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForChangeStatus() throws Exception {
        SubscriptionPlanStatusRequestDto subscriptionPlanStatusRequestDto = getSampleSubscriptionPlanStatusRequestDto();
        subscriptionPlanStatusRequestDto.setId("");
        mockMvc.perform(MockMvcRequestBuilders.put(CHANGE_STATUS_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionPlanStatusRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidSubscriptionPlanIdIsProvidedForChangeStatus() throws Exception {
        SubscriptionPlanStatusRequestDto subscriptionPlanStatusRequestDto = getSampleSubscriptionPlanStatusRequestDto();
        doThrow(new InvalidSubscriptionPlanException("ERROR")).when(subscriptionPlanService)
                .updateSubscriptionPlanStatus(SUBSCRIPTION_PLAN_ID, SubscriptionStatus.ACTIVE);
        mockMvc.perform(MockMvcRequestBuilders.put(CHANGE_STATUS_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionPlanStatusRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_ChangeStatusIsFailed() throws Exception {
        SubscriptionPlanStatusRequestDto subscriptionPlanStatusRequestDto = getSampleSubscriptionPlanStatusRequestDto();
        doThrow(new KupiCoreException("ERROR")).when(subscriptionPlanService)
                .updateSubscriptionPlanStatus(SUBSCRIPTION_PLAN_ID, SubscriptionStatus.ACTIVE);
        mockMvc.perform(MockMvcRequestBuilders.put(CHANGE_STATUS_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .content(subscriptionPlanStatusRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidSubscriptionPlanIdIsProvidedForDeleteSubscriptionPlan() throws Exception {
        String uri = DELETE_SUBSCRIPTION_PLAN_URI.replace("{subscriptionId}", SUBSCRIPTION_PLAN_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.DELETE_SUBSCRIPTION_PLAN.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.DELETE_SUBSCRIPTION_PLAN.getCode()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidSubscriptionPlanIdIsProvidedForDeleteSubscriptionPlan() throws Exception {
        doThrow(new InvalidSubscriptionPlanException("ERROR")).when(subscriptionPlanService)
                .deleteSubscriptionPlanById(SUBSCRIPTION_PLAN_ID);
        String uri = DELETE_SUBSCRIPTION_PLAN_URI.replace("{subscriptionId}", SUBSCRIPTION_PLAN_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_SubscriptionPlanHasUserForDeleteSubscriptionPlan() throws Exception {
        doThrow(new UnsupportedDeleteSubscriptionPlanException("ERROR")).when(subscriptionPlanService)
                .deleteSubscriptionPlanById(SUBSCRIPTION_PLAN_ID);
        String uri = DELETE_SUBSCRIPTION_PLAN_URI.replace("{subscriptionId}", SUBSCRIPTION_PLAN_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_SUBSCRIPTION_PLAN_DELETE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_SUBSCRIPTION_PLAN_DELETE.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_DeleteSubscriptionPlanIsFailed() throws Exception {
        doThrow(new KupiCoreException("ERROR")).when(subscriptionPlanService)
                .deleteSubscriptionPlanById(SUBSCRIPTION_PLAN_ID);
        String uri = DELETE_SUBSCRIPTION_PLAN_URI.replace("{subscriptionId}", SUBSCRIPTION_PLAN_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_UserTypeIsMerchantForSubscribe() throws Exception {
        String subscriptionId = "sid-12345";
        SubscribeRequestDto subscribeRequestDto = getSampleSubscribeRequestDto();
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        BusinessMerchantResponseDto merchantResponseDto = getSampleBusinessMerchantResponseDto();
        Subscribe subscribe = new Subscribe(subscriptionId, subscriptionPlan, MERCHANT_ID, UserType.MERCHANT);
        subscriptionPlan.setStatus(SubscriptionStatus.ACTIVE);
        when(subscriptionPlanService.getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID)).thenReturn(subscriptionPlan);
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, USER_ID)).thenReturn(merchantResponseDto);
        when(subscriberService.setSubscribe(subscriptionId, subscriptionPlan, merchantResponseDto, UserType.MERCHANT))
                .thenReturn(subscribe);
        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIBE_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(subscribeRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.SUBSCRIBE_PLAN.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.SUBSCRIBE_PLAN.getCode()));
    }

    @Test
    void Should_ReturnOk_When_UserTypeIsBankForSubscribe() throws Exception {
        String subscriptionId = "sid-123345";
        SubscribeRequestDto subscribeRequestDto = getSampleSubscribeRequestDto();
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        BusinessMerchantResponseDto merchantResponseDto = getSampleBusinessMerchantResponseDto();
        Subscribe subscribe = new Subscribe(subscriptionId, subscriptionPlan, MERCHANT_ID, UserType.MERCHANT);
        subscriptionPlan.setStatus(SubscriptionStatus.ACTIVE);
        when(subscriptionPlanService.getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID)).thenReturn(subscriptionPlan);
        when(authUserService.getBankBusinessByBankId(ACCESS_TOKEN, USER_ID)).thenReturn(merchantResponseDto);
        when(subscriberService.setSubscribe(subscriptionId, subscriptionPlan, merchantResponseDto, UserType.BANK))
                .thenReturn(subscribe);
        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIBE_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(subscribeRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.SUBSCRIBE_PLAN.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.SUBSCRIBE_PLAN.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_UserTypeNeitherMerchantNorBankIsProvidedForSubscribe() throws Exception {
        SubscribeRequestDto subscribeRequestDto = getSampleSubscribeRequestDto();
        subscribeRequestDto.setUserType(UserType.ADMIN);
        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIBE_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(subscribeRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ROLE.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ROLE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForSubscribe() throws Exception {
        SubscribeRequestDto subscribeRequestDto = getSampleSubscribeRequestDto();
        subscribeRequestDto.setSubscriptionPlanId("");
        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIBE_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(subscribeRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_SubscriptionPlanIsInactiveForSubscribe() throws Exception {
        SubscribeRequestDto subscribeRequestDto = getSampleSubscribeRequestDto();
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        subscriptionPlan.setStatus(SubscriptionStatus.INACTIVE);
        when(subscriptionPlanService.getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID)).thenReturn(subscriptionPlan);
        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIBE_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(subscribeRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INACTIVE_SUBSCRIPTION_PLAN.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INACTIVE_SUBSCRIPTION_PLAN.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_UnsupportedUserTypeIsProvidedForSubscribe() throws Exception {
        SubscribeRequestDto subscribeRequestDto = getSampleSubscribeRequestDto();
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        BusinessMerchantResponseDto merchantResponseDto = getSampleBusinessMerchantResponseDto();
        subscriptionPlan.setStatus(SubscriptionStatus.ACTIVE);
        when(subscriptionPlanService.getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID)).thenReturn(subscriptionPlan);
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, USER_ID)).thenReturn(merchantResponseDto);
        doThrow(new UnsupportedUserTypeException("ERROR")).when(subscriberService)
                .setSubscribe(anyString(), any(SubscriptionPlan.class), any(BusinessMerchantResponseDto.class),
                        any(UserType.class));
        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIBE_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(subscribeRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_SUBSCRIPTION_USER_TYPE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_SUBSCRIPTION_USER_TYPE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_UserAlreadySubscribedAPlanForSubscribe() throws Exception {
        SubscribeRequestDto subscribeRequestDto = getSampleSubscribeRequestDto();
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        subscriptionPlan.setStatus(SubscriptionStatus.ACTIVE);
        when(subscriptionPlanService.getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID)).thenReturn(subscriptionPlan);
        doThrow(new ExistingSubscribedUserException("ERROR")).when(subscriberService)
                .checkUserIsNotInAPlan(USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIBE_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(subscribeRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.ALREADY_SUBSCRIBE_USER.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.ALREADY_SUBSCRIBE_USER.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidMerchantIdIsProvidedForSubscribe() throws Exception {
        SubscribeRequestDto subscribeRequestDto = getSampleSubscribeRequestDto();
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        subscriptionPlan.setStatus(SubscriptionStatus.ACTIVE);
        when(subscriptionPlanService.getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID)).thenReturn(subscriptionPlan);
        doThrow(new InvalidUserException("ERROR")).when(authUserService)
                .getMerchantBusinessByMerchantId(ACCESS_TOKEN, USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIBE_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(subscribeRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidSubscriptionPlanIdIsProvidedForSubscribe() throws Exception {
        SubscribeRequestDto subscribeRequestDto = getSampleSubscribeRequestDto();
        doThrow(new InvalidSubscriptionPlanException("ERROR")).when(subscriptionPlanService)
                .getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID);
        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIBE_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(subscribeRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_SubscribeIsFailed() throws Exception {
        SubscribeRequestDto subscribeRequestDto = getSampleSubscribeRequestDto();
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        BusinessMerchantResponseDto merchantResponseDto = getSampleBusinessMerchantResponseDto();
        subscriptionPlan.setStatus(SubscriptionStatus.ACTIVE);
        when(subscriptionPlanService.getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID)).thenReturn(subscriptionPlan);
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, USER_ID)).thenReturn(merchantResponseDto);
        doThrow(new KupiCoreException("ERROR")).when(subscriberService).setSubscribe(anyString(),
                any(SubscriptionPlan.class), any(BusinessMerchantResponseDto.class), any(UserType.class));
        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIBE_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(subscribeRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidSubscribeIdAndUserTypeIsProvidedForUnsubscribe() throws Exception {
        String uri = UNSUBSCRIBE_URI.replace("{userType}", UserType.MERCHANT.name())
                .replace("{subscribedId}", SUBSCRIBE_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.UNSUBSCRIBE_PLAN.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.UNSUBSCRIBE_PLAN.getCode()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void Should_ReturnBadRequest_When_UserTypeNeitherMerchantNorBankIsProvidedForUnsubscribe() throws Exception {
        String uri = UNSUBSCRIBE_URI.replace("{userType}", UserType.ADMIN.name())
                .replace("{subscribedId}", SUBSCRIBE_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ROLE.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ROLE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidSubscribeIdIsProvidedForUnsubscribe() throws Exception {
        doThrow(new InvalidSubscribeException("ERROR")).when(subscriberService)
                .unsubscribeAPlan(SUBSCRIBE_ID, UserType.MERCHANT);
        String uri = UNSUBSCRIBE_URI.replace("{userType}", UserType.MERCHANT.name())
                .replace("{subscribedId}", SUBSCRIBE_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidUserTypeIsProvidedForUnsubscribe() throws Exception {
        doThrow(new UnsupportedUserTypeException("ERROR")).when(subscriberService)
                .unsubscribeAPlan(SUBSCRIBE_ID, UserType.MERCHANT);
        String uri = UNSUBSCRIBE_URI.replace("{userType}", UserType.MERCHANT.name())
                .replace("{subscribedId}", SUBSCRIBE_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_UNSUBSCRIBE_USER_TYPE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_UNSUBSCRIBE_USER_TYPE.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_UnsubscribeIsFailed() throws Exception {
        doThrow(new KupiCoreException("ERROR")).when(subscriberService)
                .unsubscribeAPlan(SUBSCRIBE_ID, UserType.MERCHANT);
        String uri = UNSUBSCRIBE_URI.replace("{userType}", UserType.MERCHANT.name())
                .replace("{subscribedId}", SUBSCRIBE_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_SubscribeSearchIndexPageIsEmptyForSearchSubscribers() throws Exception {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        List<SubscribeSearchIndex> subscribeSearchIndexList = new ArrayList<>();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(subscriptionPlanService.getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID)).thenReturn(subscriptionPlan);
        when(subscribeSearchIndexService.getSubscribersBySubscriptionPlanId(pageable, SUBSCRIPTION_PLAN_ID, SEARCH_TERM))
                .thenReturn(new PageImpl<>(subscribeSearchIndexList, pageable, 1));
        String uri = SEARCH_SUBSCRIBERS_URI.replace("{searchTerm}", SEARCH_TERM)
                .replace("{subscriptionPlanId}", SUBSCRIPTION_PLAN_ID)
                .replace("{size}", String.valueOf(SIZE)).replace("{page}", String.valueOf(PAGE));
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.SUBSCRIBE_PLAN_USER.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.SUBSCRIBE_PLAN_USER.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGES))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE));
    }

    @Test
    void Should_ReturnOk_When_ValidSubscriptionPlanIdIsProvidedForSearchSubscribers() throws Exception {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        Page<SubscribeSearchIndex> subscribeSearchIndexPage = getSampleSubscribeSearchIndexPage();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        Map<String, MerchantBusinessResponseDto> merchantBusinessResponseDtoMap = getSampleMerchantBusinessResponseDtoMap();
        when(subscriptionPlanService.getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID)).thenReturn(subscriptionPlan);
        when(subscribeSearchIndexService.getSubscribersBySubscriptionPlanId(pageable, SUBSCRIPTION_PLAN_ID, SEARCH_TERM))
                .thenReturn(subscribeSearchIndexPage);
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(merchantBusinessResponseDtoMap);
        String uri = SEARCH_SUBSCRIBERS_URI.replace("{searchTerm}", SEARCH_TERM)
                .replace("{subscriptionPlanId}", SUBSCRIPTION_PLAN_ID)
                .replace("{size}", String.valueOf(SIZE)).replace("{page}", String.valueOf(PAGE));
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.SUBSCRIBE_PLAN_USER.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.SUBSCRIBE_PLAN_USER.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGES))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidSubscriptionPlanIdIsProvidedForSearchSubscribers() throws Exception {
        doThrow(new InvalidSubscriptionPlanException("ERROR")).when(subscriptionPlanService)
                .getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID);
        String uri = SEARCH_SUBSCRIBERS_URI.replace("{searchTerm}", SEARCH_TERM)
                .replace("{subscriptionPlanId}", SUBSCRIPTION_PLAN_ID)
                .replace("{size}", String.valueOf(SIZE)).replace("{page}", String.valueOf(PAGE));
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_SearchSubscribersIsFailed() throws Exception {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(subscriptionPlanService.getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID)).thenReturn(subscriptionPlan);
        doThrow(new KupiCoreException("ERROR")).when(subscribeSearchIndexService)
                .getSubscribersBySubscriptionPlanId(pageable, SUBSCRIPTION_PLAN_ID, SEARCH_TERM);
        String uri = SEARCH_SUBSCRIBERS_URI.replace("{searchTerm}", SEARCH_TERM)
                .replace("{subscriptionPlanId}", SUBSCRIPTION_PLAN_ID)
                .replace("{size}", String.valueOf(SIZE)).replace("{page}", String.valueOf(PAGE));
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_UserTypeIsMerchantForGetSubscriptionPlanByUserId() throws Exception {
        String subscriptionId = "sid-123345";
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        Subscribe subscribe = new Subscribe(subscriptionId, subscriptionPlan, MERCHANT_ID, UserType.MERCHANT);
        var optionalSubscribe = Optional.of(subscribe);
        SubscriptionRenewal subscriptionRenewal = getSampleSubscriptionRenewal();
        when(subscriberService.getSubscribeByUserId(SUBSCRIBE_ID)).thenReturn(optionalSubscribe);
        when(subscriberService.checkSubscriptionPlanForDeal(SUBSCRIBE_ID)).thenReturn(subscriptionRenewal);
        String uri = GET_SUBSCRIPTION_PLAN_BY_USER_ID.replace("{userType}", UserType.MERCHANT.name())
                .replace("{subscribeUserId}", SUBSCRIBE_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.USER_SUBSCRIPTION_PLAN.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.USER_SUBSCRIPTION_PLAN.getCode()));
    }

    @Test
    void Should_ReturnOk_When_UserTypeIsBankForGetSubscriptionPlanByUserId() throws Exception {
        String subscriptionId = "sid-123345";
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        Subscribe subscribe = new Subscribe(subscriptionId, subscriptionPlan, MERCHANT_ID, UserType.BANK);
        var optionalSubscribe = Optional.of(subscribe);
        SubscriptionRenewal subscriptionRenewal = getSampleSubscriptionRenewal();
        when(subscriberService.getSubscribeByUserId(SUBSCRIBE_ID)).thenReturn(optionalSubscribe);
        when(subscriberService.checkSubscriptionPlanForDeal(SUBSCRIBE_ID)).thenReturn(subscriptionRenewal);
        String uri = GET_SUBSCRIPTION_PLAN_BY_USER_ID.replace("{userType}", UserType.BANK.name())
                .replace("{subscribeUserId}", SUBSCRIBE_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.USER_SUBSCRIPTION_PLAN.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.USER_SUBSCRIPTION_PLAN.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidMerchantIdIsProvidedForGetSubscriptionPlanByUserId() throws Exception {
        doThrow(new InvalidUserException("ERROR")).when(authUserService)
                .getMerchantBusinessByMerchantId(ACCESS_TOKEN, SUBSCRIBE_ID);
        String uri = GET_SUBSCRIPTION_PLAN_BY_USER_ID.replace("{userType}", UserType.MERCHANT.name())
                .replace("{subscribeUserId}", SUBSCRIBE_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetSubscriptionPlanByUserIdIsFailed() throws Exception {
        doThrow(new KupiCoreException("ERROR")).when(authUserService)
                .getMerchantBusinessByMerchantId(ACCESS_TOKEN, SUBSCRIBE_ID);
        String uri = GET_SUBSCRIPTION_PLAN_BY_USER_ID.replace("{userType}", UserType.MERCHANT.name())
                .replace("{subscribeUserId}", SUBSCRIBE_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidSubscriptionPlanIdIsProvidedForGetSubscriptionPlanSummary() throws Exception {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        when(subscriptionPlanService.getSubscriptionPlanById(anyString())).thenReturn(subscriptionPlan);
        when(subscriberService.getSubscribersCountBySubscriptionPlanId(SUBSCRIPTION_PLAN_ID))
                .thenReturn(SUBSCRIPTION_COUNT);
        String uri = GET_SUBSCRIPTION_PLAN_SUMMARY.replace("{subscriptionPlanId}", SUBSCRIBE_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.SUBSCRIPTION_SUMMARY.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.SUBSCRIPTION_SUMMARY.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidSubscriptionPlanIdIsProvidedForGetSubscriptionPlanSummary() throws Exception {
        doThrow(new InvalidSubscriptionPlanException("ERROR")).when(subscriptionPlanService)
                .getSubscriptionPlanById(anyString());
        String uri = GET_SUBSCRIPTION_PLAN_SUMMARY.replace("{subscriptionPlanId}", SUBSCRIBE_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetSubscriptionPlanSummaryIsFailed() throws Exception {
        doThrow(new KupiCoreException("ERROR")).when(subscriptionPlanService)
                .getSubscriptionPlanById(anyString());
        String uri = GET_SUBSCRIPTION_PLAN_SUMMARY.replace("{subscriptionPlanId}", SUBSCRIBE_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    /**
     * This method creates sample SubscriptionPlanRequestDto
     *
     * @return SubscriptionPlanRequestDto
     */
    private SubscriptionPlanRequestDto getSampleSubscriptionPlanRequestDto() {
        SubscriptionPlanRequestDto subscriptionPlanRequestDto = new SubscriptionPlanRequestDto();
        subscriptionPlanRequestDto.setTitle("Title");
        subscriptionPlanRequestDto.setSubTitle("Subtitle");
        subscriptionPlanRequestDto.setPlanType(PlanType.MONTHLY);
        subscriptionPlanRequestDto.setNoOfDealsOfTheDayEventsPerMonth(1);
        subscriptionPlanRequestDto.setNoOfDealsPerMonth(1);
        subscriptionPlanRequestDto.setNoOfDealsOfTheDayPerEvent(1);
        subscriptionPlanRequestDto.setUserRole(UserType.MERCHANT);
        subscriptionPlanRequestDto.setIsFree(false);
        subscriptionPlanRequestDto.setAmount(BigDecimal.ONE);
        subscriptionPlanRequestDto.setDescription("Description");
        subscriptionPlanRequestDto.setTermsAndCondition("terms and conditions");
        return subscriptionPlanRequestDto;
    }

    /**
     * This method creates sample SubscriptionPlan
     *
     * @return SubscriptionPlan
     */
    private SubscriptionPlan getSampleSubscriptionPlan() {
        SubscriptionPlan subscriptionPlan = new SubscriptionPlan();
        subscriptionPlan.setId(SUBSCRIPTION_PLAN_ID);
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
     * This method creates sample SubscriptionPlan Page
     *
     * @return SubscriptionPlanPage
     */
    private Page<SubscriptionPlan> getSampleSubscriptionPlanPage() {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        List<SubscriptionPlan> subscriptionPlanList = new ArrayList<>();
        subscriptionPlanList.add(subscriptionPlan);
        return new PageImpl<>(subscriptionPlanList);
    }

    /**
     * This method creates sample SubscriptionUpdateRequestDto
     *
     * @return SubscriptionUpdateRequestDto
     */
    private SubscriptionUpdateRequestDto getSampleSubscriptionUpdateRequestDto() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = new SubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setId(SUBSCRIPTION_PLAN_ID);
        subscriptionUpdateRequestDto.setTitle("Title");
        subscriptionUpdateRequestDto.setSubTitle("Subtitle");
        subscriptionUpdateRequestDto.setPlanType(PlanType.MONTHLY);
        subscriptionUpdateRequestDto.setNoOfDealsOfTheDayEventsPerMonth(1);
        subscriptionUpdateRequestDto.setNoOfDealsPerMonth(1);
        subscriptionUpdateRequestDto.setNoOfDealsOfTheDayPerEvent(1);
        subscriptionUpdateRequestDto.setUserRole(UserType.MERCHANT);
        subscriptionUpdateRequestDto.setIsFree(false);
        subscriptionUpdateRequestDto.setAmount(BigDecimal.ONE);
        subscriptionUpdateRequestDto.setDescription("Description");
        subscriptionUpdateRequestDto.setTermsAndCondition("terms and conditions");
        return subscriptionUpdateRequestDto;
    }

    /**
     * This method creates sample SubscriptionPlanStatusRequestDto
     *
     * @return SubscriptionPlanStatusRequestDto
     */
    private SubscriptionPlanStatusRequestDto getSampleSubscriptionPlanStatusRequestDto() {
        SubscriptionPlanStatusRequestDto subscriptionPlanStatusRequestDto = new SubscriptionPlanStatusRequestDto();
        subscriptionPlanStatusRequestDto.setId(SUBSCRIPTION_PLAN_ID);
        subscriptionPlanStatusRequestDto.setStatus(SubscriptionStatus.ACTIVE);
        return subscriptionPlanStatusRequestDto;
    }

    /**
     * This method creates sample SubscribeRequestDto
     *
     * @return SubscribeRequestDto
     */
    private SubscribeRequestDto getSampleSubscribeRequestDto() {
        SubscribeRequestDto subscribeRequestDto = new SubscribeRequestDto();
        subscribeRequestDto.setTempSubscriptionId(SUBSCRIPTION_PLAN_ID);
        subscribeRequestDto.setSubscriptionPlanId(SUBSCRIPTION_PLAN_ID);
        subscribeRequestDto.setUserType(UserType.MERCHANT);
        return subscribeRequestDto;
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
     * This method creates sample SubscribeSearchIndex
     *
     * @return SubscribeSearchIndex
     */
    private SubscribeSearchIndex getSampleSubscribeSearchIndex() {
        String subscriptionId = "sid-123345";
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

    /**
     * This method creates sample MerchantBusinessResponseDto
     *
     * @return MerchantBusinessResponseDto
     */
    private MerchantBusinessResponseDto getSampleMerchantBusinessResponseDto() {
        MerchantBusinessResponseDto merchantBusinessResponseDto = new MerchantBusinessResponseDto();
        merchantBusinessResponseDto.setId(MERCHANT_ID);
        merchantBusinessResponseDto.setName("madusanka business");
        merchantBusinessResponseDto.setApprovalStatus(ApprovalStatus.APPROVED);
        merchantBusinessResponseDto.setUserType("MERCHANT");
        merchantBusinessResponseDto.setActive(true);
        merchantBusinessResponseDto.setImageUrl("https://objects-kupi-dev." +
                "s3.ap-southeast-1.amazonaws.com/fid-76b07696-" +
                "feac-49c3-a8f8-723db8f26c32.jpg");
        return merchantBusinessResponseDto;
    }

    /**
     * This method creates sample merchant business response map
     *
     * @return MerchantBusinessResponseDto map
     */
    private Map<String, MerchantBusinessResponseDto> getSampleMerchantBusinessResponseDtoMap() {
        MerchantBusinessResponseDto merchantBusinessResponseDto = getSampleMerchantBusinessResponseDto();
        Map<String, MerchantBusinessResponseDto> merchantBusinessResponseDtoMap = new HashMap<>();
        merchantBusinessResponseDtoMap.put(merchantBusinessResponseDto.getId(), merchantBusinessResponseDto);
        return merchantBusinessResponseDtoMap;
    }

    /**
     * This method creates sample subscription renewal
     *
     * @return SubscriptionRenewal
     */
    private SubscriptionRenewal getSampleSubscriptionRenewal() {
        SubscriptionRenewal subscriptionRenewal = new SubscriptionRenewal();
        subscriptionRenewal.setId(SUBSCRIPTION_PLAN_ID);
        subscriptionRenewal.setSubscriptionPlanId("spid-c9d5b89a-5125-4838-b3cb-58bc8619589f");
        subscriptionRenewal.setPlanType(PlanType.MONTHLY);
        subscriptionRenewal.setUnsubscribed(false);
        subscriptionRenewal.setRemainingDealsPerMonth(10);
        return subscriptionRenewal;
    }
}