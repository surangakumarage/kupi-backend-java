package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.ResourceBundleMessageSourceBean;
import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.pojo.PaymentOrderDto;
import com.kupi.cc.base.domain.request.*;
import com.kupi.cc.base.domain.response.BasicCouponResponseDto;
import com.kupi.cc.base.domain.response.SubscriptionValidationResponseDto;
import com.kupi.cc.base.enums.CouponPurchaseStatus;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.service.CoreService;
import com.kupi.cc.base.service.PaymentService;
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

import java.math.BigDecimal;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests {@link PaymentController} class
 */
class PaymentControllerTest {

    private static final String USER_ID_HEADER = "User-Id";
    private static final String USER_ID = "uid-123456789";
    private static final String DEAL_ID = "did-123456789";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String AUTH_TOKEN_HEADER = "Auth-Token";
    private static final String AUTH_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String SUBSCRIPTION_PLAN_ID = "SUBSCRIPTION_PLAN_ID";
    private static final String PAYMENT_BROKER_ID = "PAYMENT_BROKER_ID";
    private static final String SUBSCRIPTION_ID = "SUBSCRIPTION_ID";
    private static final String PAYMENT_ID = "PAYMENT_ID";
    private static final String PAYER_ID = "PAYER_ID";
    private static final String ORDER_ID = "ORDER_ID";
    private static final String COUPON_ID = "COUPON_ID";
    private static final String APPROVAL_LINK = "APPROVAL_LINK";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String FAILED = "failed";
    private static final String APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8";
    private static final String FREE_SUBSCRIPTION_PAYMENT_URI = "/api/v1/payment/subscription/free";
    private static final String SUBSCRIPTION_PAYMENT_URI = "/api/v1/payment/subscription/make";
    private static final String COUPON_PAYMENT_URI = "/api/v1/payment/coupon/make";
    private static final String CONFIRM_SUBSCRIPTION_PAYMENT_URI = "/api/v1/payment/subscription/confirm";
    private static final String CONFIRM_COUPON_PAYMENT_URI = "/api/v1/payment/coupon/confirm";

    private final ResourceBundleMessageSourceBean resourceBundleMessageSourceBean = new ResourceBundleMessageSourceBean();
    Locale locale = LocaleContextHolder.getLocale();
    @Mock
    private Translator translator;
    @Mock
    private PaymentService paymentService;
    @Mock
    private CoreService coreService;
    private MockMvc mockMvc;
    private ResourceBundleMessageSource resourceBundleMessageSource;

    @BeforeEach
    void setUp() {
        openMocks(this);
        PaymentController paymentController = new PaymentController(translator, paymentService, coreService);
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
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                .setValidator(noOpValidator)
                .build();
        resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks(this).close();
    }

    /**
     * Start of tests for make subscription payment (free)
     * Api context: /api/v1/payment/subscription/free
     */
    @Test
    void Should_ReturnOk_When_MakingFreeSubscriptionPaymentIsSuccessful() throws Exception {
        doNothing().when(coreService).freeSubscription(any(SubscriptionValidationRequestDto.class), anyString(),
                anyString(), anyString());

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType
                .getCodeString(SuccessResponseStatusType.SUBSCRIBE_PLAN.getCode()), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.getCodeString(SuccessResponseStatusType
                .SUBSCRIBE_PLAN.getCode()))).thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(FREE_SUBSCRIPTION_PAYMENT_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleSubscriptionValidationRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.SUBSCRIBE_PLAN.getMessage()))
                .andExpect(jsonPath("$.data.id").doesNotExist())
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_MakingFreeSubscriptionPaymentForInvalidUserId() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.INVALID_USER_ID.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType
                .INVALID_USER_ID.getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(FREE_SUBSCRIPTION_PAYMENT_URI)
                        .header(USER_ID_HEADER, "")
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleSubscriptionValidationRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_MakingFreeSubscriptionPaymentForMissingRequiredFields() throws Exception {
        SubscriptionValidationRequestDto subscriptionValidationRequestDto = getSampleSubscriptionValidationRequestDto();
        subscriptionValidationRequestDto.setUserType(null);

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(FREE_SUBSCRIPTION_PAYMENT_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(subscriptionValidationRequestDto.toJson())
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
    void Should_ReturnBadRequest_When_MakingFreeSubscriptionPaymentIsFailed() throws Exception {
        doThrow(new KupiOrderPaymentException(FAILED)).when(coreService)
                .freeSubscription(any(SubscriptionValidationRequestDto.class), anyString(), anyString(), anyString());

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(FREE_SUBSCRIPTION_PAYMENT_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleSubscriptionValidationRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for make subscription payment
     * Api context: /api/v1/payment/subscription/make
     */
    @Test
    void Should_ReturnOk_When_MakingSubscriptionPaymentIsSuccessful() throws Exception {
        when(paymentService.makeSubscriptionPaymentRequest(any(SubscriptionPaymentRequestDto.class), anyString(),
                anyString(), anyString())).thenReturn(getSampleSubscriptionValidationResponseDto());

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType
                .getCodeString(SuccessResponseStatusType.SUBSCRIPTION_PAYMENT_INITIATED.getCode()), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.getCodeString(SuccessResponseStatusType
                .SUBSCRIPTION_PAYMENT_INITIATED.getCode()))).thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIPTION_PAYMENT_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleSubscriptionPaymentRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.SUBSCRIPTION_PAYMENT_INITIATED.getMessage()))
                .andExpect(jsonPath("$.data.id").value(SUBSCRIPTION_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_MakingSubscriptionPaymentForInvalidUserId() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.INVALID_USER_ID.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType
                .INVALID_USER_ID.getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIPTION_PAYMENT_URI)
                        .header(USER_ID_HEADER, "")
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleSubscriptionPaymentRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_MakingSubscriptionPaymentForMissingRequiredFields() throws Exception {
        SubscriptionPaymentRequestDto subscriptionPaymentRequestDto = getSampleSubscriptionPaymentRequestDto();
        subscriptionPaymentRequestDto.setPaymentBrokerId(null);

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIPTION_PAYMENT_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(subscriptionPaymentRequestDto.toJson())
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
    void Should_ReturnBadRequest_When_MakingSubscriptionPaymentIsFailed() throws Exception {
        when(paymentService.makeSubscriptionPaymentRequest(any(SubscriptionPaymentRequestDto.class), anyString(),
                anyString(), anyString())).thenThrow(new KupiOrderPaymentException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIPTION_PAYMENT_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleSubscriptionPaymentRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for confirm payment
     * Api context: /api/v1/payment/subscription/confirm
     */
    @Test
    void Should_ReturnOk_When_ConfirmingPaymentIsSuccessful() throws Exception {
        when(paymentService.completePayment(any(PaymentCaptureRequestDto.class))).thenReturn(getSamplePaymentOrderDto());
        doNothing().when(coreService).subscribe(anyString(), anyString(), anyString(), anyString());

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType
                .getCodeString(SuccessResponseStatusType.SUBSCRIBE_PLAN.getCode()), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.getCodeString(SuccessResponseStatusType
                .SUBSCRIBE_PLAN.getCode()))).thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CONFIRM_SUBSCRIPTION_PAYMENT_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSamplePaymentCaptureRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.SUBSCRIBE_PLAN.getMessage()))
                .andExpect(jsonPath("$.data.id").doesNotExist())
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_ConfirmingPaymentForInvalidUserId() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.INVALID_USER_ID.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType
                .INVALID_USER_ID.getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CONFIRM_SUBSCRIPTION_PAYMENT_URI)
                        .header(USER_ID_HEADER, "")
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSamplePaymentCaptureRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_ConfirmingPaymentForMissingRequiredFields() throws Exception {
        PaymentCaptureRequestDto paymentCaptureRequestDto = getSamplePaymentCaptureRequestDto();
        paymentCaptureRequestDto.setPayerId(null);

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CONFIRM_SUBSCRIPTION_PAYMENT_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(paymentCaptureRequestDto.toJson())
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
    void Should_ReturnBadRequest_When_ConfirmingPaymentIsFailed() throws Exception {
        when(paymentService.completePayment(any(PaymentCaptureRequestDto.class)))
                .thenThrow(new KupiOrderPaymentException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CONFIRM_SUBSCRIPTION_PAYMENT_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSamplePaymentCaptureRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for make coupon payment
     * Api context: /api/v1/payment/coupon/make
     */
    @Test
    void Should_ReturnOk_When_MakingCouponPaymentIsSuccessful() throws Exception {
        when(paymentService.makeCouponPaymentRequest(any(CouponPaymentRequestDto.class), anyString(), anyString(),
                anyString())).thenReturn(getSampleBasicCouponResponseDto());

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType
                .getCodeString(SuccessResponseStatusType.COUPON_PAYMENT_INITIATED.getCode()), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.getCodeString(SuccessResponseStatusType
                .COUPON_PAYMENT_INITIATED.getCode()))).thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(COUPON_PAYMENT_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCouponPaymentRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.COUPON_PAYMENT_INITIATED.getMessage()))
                .andExpect(jsonPath("$.data.id").value(COUPON_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_MakingCouponPaymentForInvalidUserId() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.INVALID_USER_ID.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType
                .INVALID_USER_ID.getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(COUPON_PAYMENT_URI)
                        .header(USER_ID_HEADER, "")
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCouponPaymentRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_MakingCouponPaymentForMissingRequiredFields() throws Exception {
        CouponPaymentRequestDto couponPaymentRequestDto = getSampleCouponPaymentRequestDto();
        couponPaymentRequestDto.setAmount(null);

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(COUPON_PAYMENT_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(couponPaymentRequestDto.toJson())
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
    void Should_ReturnBadRequest_When_MakingCouponPaymentIsFailed() throws Exception {
        when(paymentService.makeCouponPaymentRequest(any(CouponPaymentRequestDto.class), anyString(), anyString(),
                anyString())).thenThrow(new KupiOrderPaymentException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(COUPON_PAYMENT_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCouponPaymentRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for confirm coupon payment
     * Api context: /api/v1/payment/coupon/confirm
     */
    @Test
    void Should_ReturnOk_When_ConfirmingCouponPaymentIsSuccessful() throws Exception {
        when(paymentService.completePayment(any(PaymentCaptureRequestDto.class))).thenReturn(getSamplePaymentOrderDto());
        doNothing().when(coreService).confirmCouponPurchase(any(CouponPaymentCaptureRequestDto.class), anyString(),
                anyString(), anyString(), any(CouponPurchaseStatus.class));

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType
                .getCodeString(SuccessResponseStatusType.COUPON_PAYMENT_CONFIRMED.getCode()), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.getCodeString(SuccessResponseStatusType
                .COUPON_PAYMENT_CONFIRMED.getCode()))).thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CONFIRM_COUPON_PAYMENT_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCouponPaymentCaptureRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.COUPON_PAYMENT_CONFIRMED.getMessage()))
                .andExpect(jsonPath("$.data.id").doesNotExist())
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_ConfirmingCouponPaymentForInvalidUserId() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.INVALID_USER_ID.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType
                .INVALID_USER_ID.getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CONFIRM_COUPON_PAYMENT_URI)
                        .header(USER_ID_HEADER, "")
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCouponPaymentCaptureRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_ConfirmingCouponPaymentForMissingRequiredFields() throws Exception {
        CouponPaymentCaptureRequestDto couponPaymentCaptureRequestDto = getSampleCouponPaymentCaptureRequestDto();
        couponPaymentCaptureRequestDto.setPayerId(null);

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CONFIRM_COUPON_PAYMENT_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(couponPaymentCaptureRequestDto.toJson())
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
    void Should_ReturnBadRequest_When_ConfirmingCouponPaymentIsFailed() throws Exception {
        when(paymentService.completePayment(any(PaymentCaptureRequestDto.class)))
                .thenThrow(new KupiOrderPaymentException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CONFIRM_COUPON_PAYMENT_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCouponPaymentCaptureRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * This method returns a sample subscription payment request dto
     *
     * @return SubscriptionPaymentRequestDto
     */
    private SubscriptionPaymentRequestDto getSampleSubscriptionPaymentRequestDto() {
        SubscriptionValidationRequestDto subscriptionValidationRequestDto = new SubscriptionValidationRequestDto();
        subscriptionValidationRequestDto.setSubscriptionPlanId(SUBSCRIPTION_PLAN_ID);
        subscriptionValidationRequestDto.setUserType(UserType.MERCHANT);

        SubscriptionPaymentRequestDto subscriptionPaymentRequestDto = new SubscriptionPaymentRequestDto();
        subscriptionPaymentRequestDto.setPaymentBrokerId(PAYMENT_BROKER_ID);
        subscriptionPaymentRequestDto.setSubscriptionValidationRequestDto(subscriptionValidationRequestDto);

        return subscriptionPaymentRequestDto;
    }

    /**
     * This method returns a sample subscription validation response dto
     *
     * @return SubscriptionValidationResponseDto
     */
    private SubscriptionValidationResponseDto getSampleSubscriptionValidationResponseDto() {
        SubscriptionValidationResponseDto subscriptionValidationResponseDto = new SubscriptionValidationResponseDto();
        subscriptionValidationResponseDto.setId(SUBSCRIPTION_ID);
        return subscriptionValidationResponseDto;
    }

    /**
     * This method returns a sample payment capture request dto
     *
     * @return PaymentCaptureRequestDto
     */
    private PaymentCaptureRequestDto getSamplePaymentCaptureRequestDto() {
        PaymentCaptureRequestDto paymentCaptureRequestDto = new PaymentCaptureRequestDto();
        paymentCaptureRequestDto.setPaymentId(PAYMENT_ID);
        paymentCaptureRequestDto.setPayerId(PAYER_ID);
        return paymentCaptureRequestDto;
    }

    /**
     * This method returns a sample payment order dto
     *
     * @return PaymentOrderDto
     */
    private PaymentOrderDto getSamplePaymentOrderDto() {
        PaymentOrderDto paymentOrderDto = new PaymentOrderDto(ORDER_ID, APPROVAL_LINK);
        paymentOrderDto.setPayableId(PAYER_ID);
        return paymentOrderDto;
    }

    /**
     * This method returns a sample coupon payment request dto
     *
     * @return CouponPaymentRequestDto
     */
    private CouponPaymentRequestDto getSampleCouponPaymentRequestDto() {
        CouponPaymentRequestDto couponPaymentRequestDto = new CouponPaymentRequestDto();
        couponPaymentRequestDto.setDealId(DEAL_ID);
        couponPaymentRequestDto.setPaymentBrokerId(PAYMENT_BROKER_ID);
        couponPaymentRequestDto.setAmount(new BigDecimal(100));
        return couponPaymentRequestDto;
    }

    /**
     * This method returns a sample basic coupon response dto
     *
     * @return BasicCouponResponseDto
     */
    private BasicCouponResponseDto getSampleBasicCouponResponseDto() {
        BasicCouponResponseDto basicCouponResponseDto = new BasicCouponResponseDto();
        basicCouponResponseDto.setId(COUPON_ID);
        return basicCouponResponseDto;
    }

    /**
     * This method returns a sample coupon payment capture request dto
     *
     * @return CouponPaymentCaptureRequestDto
     */
    private CouponPaymentCaptureRequestDto getSampleCouponPaymentCaptureRequestDto() {
        CouponPaymentCaptureRequestDto couponPaymentCaptureRequestDto = new CouponPaymentCaptureRequestDto();
        couponPaymentCaptureRequestDto.setCouponId(COUPON_ID);
        couponPaymentCaptureRequestDto.setDealId(DEAL_ID);
        couponPaymentCaptureRequestDto.setPaymentId(PAYMENT_ID);
        couponPaymentCaptureRequestDto.setPayerId(PAYER_ID);
        return couponPaymentCaptureRequestDto;
    }

    /**
     * This method returns a sample subscription validation request dto
     *
     * @return SubscriptionValidationRequestDto
     */
    private SubscriptionValidationRequestDto getSampleSubscriptionValidationRequestDto() {
        SubscriptionValidationRequestDto subscriptionValidationRequestDto = new SubscriptionValidationRequestDto();
        subscriptionValidationRequestDto.setSubscriptionPlanId(SUBSCRIPTION_PLAN_ID);
        subscriptionValidationRequestDto.setUserType(UserType.MERCHANT);
        return subscriptionValidationRequestDto;
    }
}