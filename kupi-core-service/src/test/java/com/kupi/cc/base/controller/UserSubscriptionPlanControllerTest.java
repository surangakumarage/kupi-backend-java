package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.UserSubscriptionPlan;
import com.kupi.cc.base.domain.request.UserSubscriptionPlanRequestDto;
import com.kupi.cc.base.domain.response.UserSubscriptionPlanListResponse;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.exception.ExistingNameException;
import com.kupi.cc.base.service.UserSubscriptionPlanService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserSubscriptionPlanControllerTest {

    private static final String CREATE_USER_SUBSCRIPTION = "/api/v1/user-subscription-plans";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String USER_ID = "user-123";

    private MockMvc mockMvc;

    @Mock
    private UserSubscriptionPlanService planService;

    @Mock
    private Translator translator;

    @BeforeEach
    void setUp() {
        initMocks(this);
        UserSubscriptionPlanController userSubscriptionPlanController = new UserSubscriptionPlanController(translator, planService);
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
        mockMvc = MockMvcBuilders.standaloneSetup(userSubscriptionPlanController)
                .setValidator(noOpValidator)
                .build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldCreateSubscriptionPlanSuccessfully() throws Exception {
        var sampleUserSubscriptionPlanRequestDto = getSampleUserSubscriptionPlanRequestDto();

        mockMvc.perform(post(CREATE_USER_SUBSCRIPTION)
                        .header("time-zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleUserSubscriptionPlanRequestDto.toJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.CREATE_MOBILE_USER_SUBSCRIPTION_PLAN.getMessage()));
        verify(planService).createUserSubscriptionPlan(any(UserSubscriptionPlanRequestDto.class));
    }

    @Test
    void shouldReturnError_whenRequiredFieldsMissing() throws Exception {
        var sampleUserSubscriptionPlanRequestDto = new UserSubscriptionPlanRequestDto();

        mockMvc.perform(post(CREATE_USER_SUBSCRIPTION)
                        .header("time-zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleUserSubscriptionPlanRequestDto.toJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()));

        verify(planService, never()).createUserSubscriptionPlan(any());
    }

    @Test
    void shouldReturnError_whenDuplicateName() throws Exception {
        var sampleUserSubscriptionPlanRequestDto = getSampleUserSubscriptionPlanRequestDto();
        doThrow(new ExistingNameException("Existing user subscription plan name."))
                .when(planService).createUserSubscriptionPlan(any());

        mockMvc.perform(post(CREATE_USER_SUBSCRIPTION)
                        .header("time-zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleUserSubscriptionPlanRequestDto.toJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.EXISTING_SUBSCRIPTION_PLAN_NAME.getMessage()));
    }

    @Test
    void shouldReturnSubscriptionPlansSuccessfully() throws Exception {
        var planListResponse = getSampleUserSubscriptionPlanListResponse();
        when(planService.getAllActivePlans()).thenReturn(planListResponse);
        mockMvc.perform(get(CREATE_USER_SUBSCRIPTION)
                        .header("user-id", USER_ID)
                        .header("time-zone", TIME_ZONE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_USER_SUBSCRIPTION_PLAN_LIST.getMessage()));
    }

    private UserSubscriptionPlanRequestDto getSampleUserSubscriptionPlanRequestDto() {
        var userSubscriptionPlanRequestDto = new UserSubscriptionPlanRequestDto();
        userSubscriptionPlanRequestDto.setName("Pro Plan");
        userSubscriptionPlanRequestDto.setPrice(new BigDecimal("999.99"));
        userSubscriptionPlanRequestDto.setCouponLimit(10);
        userSubscriptionPlanRequestDto.setValidDays(30);
        userSubscriptionPlanRequestDto.setImageUrl("https://www.google.com");
        return userSubscriptionPlanRequestDto;
    }

    private UserSubscriptionPlanListResponse getSampleUserSubscriptionPlanListResponse() {
        var plan = new UserSubscriptionPlan();
        plan.setId("usp-123");
        plan.setName("Pro Plan");
        plan.setPrice(new BigDecimal("999.99"));
        plan.setCouponLimit(10);
        plan.setValidDays(30);
        plan.setStripePriceId("price_abc123");
        plan.setCreatedAt(System.currentTimeMillis());
        plan.setUpdatedAt(System.currentTimeMillis());
        var response = new UserSubscriptionPlanListResponse();
        response.setUserSubscriptionPlanList(List.of(plan));
        return response;
    }
}