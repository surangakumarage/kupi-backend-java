package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Subscription;
import com.kupi.cc.base.domain.request.CouponPaymentCaptureRequestDto;
import com.kupi.cc.base.domain.request.SubscriptionValidationRequestDto;
import com.kupi.cc.base.domain.response.*;
import com.kupi.cc.base.enums.*;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.wrapper.CoreResponseWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * This class tests {@link CoreService} class
 */
class CoreServiceTest {
    private static final String SUBSCRIPTION_PLAN_ID = "spid-0188800e-1a35-4b6d-92c5-5a66a5b115cc";
    private static final String TEMP_SUBSCRIPTION_ID = "sid-d7a4838c-3d59-48ca-9d15-b9dc39ed3749";
    private static final String DEAL_ID = "did-d47fc18c-4c1b-4a68-9384-968a2613f385";
    private static final String COUPON_ID = "couid-03d448b3-3f89-411a-8570-f49efbecc9da";
    private static final String DEAL_ID_PHRASE = "##DEAL-ID##";
    private static final String COUPON_ID_PHRASE = "##COUPON-ID##";
    private static final String PURCHASE_STATUS_ID_PHRASE = "##STATUS##";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String USER_ID_HEADER = "User-Id";
    private static final String AUTH_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String USER_ID = "uid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String INVALID_ERROR_MESSAGE = "{\"statuses\":\"Error\",\"messages\":\"Unexpectederrors\"}";
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(100);
    private final String baseUrl = "http://localhost:8080/kupi-core-service";
    private final String purchaseCouponUrl = "/api/v1/coupons/deal/##DEAL-ID##";
    private final String confirmCouponPurchaseUrl = "/api/v1/coupons/##COUPON-ID##/deal/##DEAL-ID##/status/##STATUS##";
    private final String validateSubscriptionUrl = "/api/v1/subscription-plan/subscribe/validate";
    private final String subscribeUrl = "/api/v1/subscription-plan/subscribe";
    private final String subscriptionPlanListUrl = "/api/v1/subscription-plan/subscription-plan-count";
    private final String freeSubscriptionUrl = "/api/v1/subscription-plan/subscribe/free";
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private SubscriptionService subscriptionService;
    private CoreService coreService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        coreService = new CoreService(baseUrl, purchaseCouponUrl, subscriptionPlanListUrl, confirmCouponPurchaseUrl,
                validateSubscriptionUrl, subscribeUrl, freeSubscriptionUrl, restTemplate, subscriptionService);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks(this).close();
    }

    @Test
    void Should_ReturnSubscriptionValidationResponseDto_When_SubscriptionAvailableInCoreService() {
        SubscriptionValidationResponseDto subscriptionValidationResponseDto = getSampleSubscriptionValidationResponseDto();
        SubscriptionValidationRequestDto subscriptionValidationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + validateSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        CoreResponseWrapper<SubscriptionValidationResponseDto> responseWrapper = new CoreResponseWrapper<>();
        responseWrapper.setData(subscriptionValidationResponseDto);
        var response = new ResponseEntity<>(responseWrapper, HttpStatus.OK);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(subscriptionValidationRequestDto, header);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference)).thenReturn(response);
        assertEquals(subscriptionValidationResponseDto,
                coreService.validateSubscription(subscriptionValidationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_SubscriptionPlanNotFoundInCoreService() {
        SubscriptionValidationResponseDto subscriptionValidationResponseDto = getSampleSubscriptionValidationResponseDto();
        SubscriptionValidationRequestDto subscriptionValidationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + validateSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        CoreResponseWrapper<SubscriptionValidationResponseDto> responseWrapper = new CoreResponseWrapper<>();
        responseWrapper.setData(subscriptionValidationResponseDto);
        responseWrapper.setStatus(ResponseStatusType.SUCCESS);
        var response = new ResponseEntity<>(responseWrapper, HttpStatus.BAD_REQUEST);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(subscriptionValidationRequestDto, header);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference)).thenReturn(response);
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.validateSubscription(subscriptionValidationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Validating subscription request was failed from core service", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_RequiredFieldsAreNotSendToValidateSubscription() {
        SubscriptionValidationRequestDto subscriptionValidationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + validateSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(subscriptionValidationRequestDto, header);
        var errorResponseDto = getCoreServiceErrorResponseDto(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorResponseDto.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.validateSubscription(subscriptionValidationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(4000, exception.getErrorResponseType().getCode());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_InvalidUserRoleIsSendToValidateSubscription() {
        SubscriptionValidationRequestDto subscriptionValidationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + validateSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(subscriptionValidationRequestDto, header);
        var errorResponseDto = getCoreServiceErrorResponseDto(ErrorResponseStatusType.INVALID_USER_ROLE);
        errorResponseDto.setErrorCode(4021);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorResponseDto.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.validateSubscription(subscriptionValidationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.INVALID_USER_ROLE.getCode(), exception.getErrorResponseType().getCode());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_SubscriptionPlanIsInactive() {
        SubscriptionValidationRequestDto subscriptionValidationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + validateSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(subscriptionValidationRequestDto, header);
        var errorResponseDto = getCoreServiceErrorResponseDto(ErrorResponseStatusType.INACTIVE_SUBSCRIPTION_PLAN);
        errorResponseDto.setErrorCode(4046);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorResponseDto.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.validateSubscription(subscriptionValidationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.INACTIVE_SUBSCRIPTION_PLAN.getCode(),
                exception.getErrorResponseType().getCode());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_UserAlreadySubscribedThePlan() {
        SubscriptionValidationRequestDto subscriptionValidationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + validateSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(subscriptionValidationRequestDto, header);
        var errorResponseDto = getCoreServiceErrorResponseDto(ErrorResponseStatusType.ALREADY_SUBSCRIBE_USER);
        errorResponseDto.setErrorCode(4043);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorResponseDto.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.validateSubscription(subscriptionValidationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.ALREADY_SUBSCRIBE_USER.getCode(),
                exception.getErrorResponseType().getCode());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_InvalidSubscriptionPlanIdIsProvidedToValidateSubscriptionPlan() {
        SubscriptionValidationRequestDto subscriptionValidationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + validateSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(subscriptionValidationRequestDto, header);
        var errorResponseDto = getCoreServiceErrorResponseDto(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN);
        errorResponseDto.setErrorCode(4040);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorResponseDto.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.validateSubscription(subscriptionValidationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getCode(),
                exception.getErrorResponseType().getCode());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_InvalidMerchantIdIsProvidedToValidateSubscriptionPlan() {
        SubscriptionValidationRequestDto subscriptionValidationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + validateSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(subscriptionValidationRequestDto, header);
        var errorResponseDto = getCoreServiceErrorResponseDto(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        errorResponseDto.setErrorCode(4101);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorResponseDto.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.validateSubscription(subscriptionValidationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode(),
                exception.getErrorResponseType().getCode());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_UnexpectedErrorThrowsByCoreService() {
        SubscriptionValidationRequestDto subscriptionValidationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + validateSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(subscriptionValidationRequestDto, header);
        var errorResponseDto = getCoreServiceErrorResponseDto(ErrorResponseStatusType.INVALID_ORDER_ID);
        errorResponseDto.setErrorCode(4444);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorResponseDto.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.validateSubscription(subscriptionValidationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Unknown error from core service", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_CouldNotConvertErrorResponseToJavaObject() {
        SubscriptionValidationRequestDto subscriptionValidationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + validateSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(subscriptionValidationRequestDto, header);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, INVALID_ERROR_MESSAGE));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.validateSubscription(subscriptionValidationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Converting string data to object is failed", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_CoreServiceThrowInternalServerError() {
        SubscriptionValidationRequestDto subscriptionValidationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + validateSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(subscriptionValidationRequestDto, header);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.validateSubscription(subscriptionValidationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Validating subscription request was failed from core service", exception.getMessage());
    }

    @Test
    void Should_CompleteSubscription_When_PaymentIsCaptured() {
        Subscription tempSubscription = getSampleSubscription();
        CoreResponseWrapper<ResponseDto> responseWrapper = new CoreResponseWrapper<>();
        responseWrapper.setStatus(ResponseStatusType.SUCCESS);
        responseWrapper.setData(null);
        var response = new ResponseEntity<>(responseWrapper, HttpStatus.OK);
        when(subscriptionService.getSubscriptionFromId(TEMP_SUBSCRIPTION_ID)).thenReturn(tempSubscription);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(response);
        coreService.subscribe(USER_ID, TEMP_SUBSCRIPTION_ID, AUTH_TOKEN, TIME_ZONE);
        verify(subscriptionService, times(1)).saveCompletedSubscription(tempSubscription, USER_ID);
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_CoreServiceNotSubscribeThePlan() {
        Subscription tempSubscription = getSampleSubscription();
        CoreResponseWrapper<ResponseDto> responseWrapper = new CoreResponseWrapper<>();
        responseWrapper.setStatus(ResponseStatusType.SUCCESS);
        responseWrapper.setData(null);
        var response = new ResponseEntity<>(responseWrapper, HttpStatus.BAD_REQUEST);
        when(subscriptionService.getSubscriptionFromId(TEMP_SUBSCRIPTION_ID)).thenReturn(tempSubscription);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(response);
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.subscribe(USER_ID, TEMP_SUBSCRIPTION_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Failed to subscribe for temporary-subscription id: " + TEMP_SUBSCRIPTION_ID,
                exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_MissingRequiredFieldsAreSendToSubscribeThePlan() {
        Subscription tempSubscription = getSampleSubscription();
        var errorResponseDto = getCoreServiceErrorResponseDto(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
        errorResponseDto.setErrorCode(4000);
        when(subscriptionService.getSubscriptionFromId(TEMP_SUBSCRIPTION_ID)).thenReturn(tempSubscription);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                errorResponseDto.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.subscribe(USER_ID, TEMP_SUBSCRIPTION_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode(),
                exception.getErrorResponseType().getCode());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_InvalidUserRoleSendToSubscribeThePlan() {
        Subscription tempSubscription = getSampleSubscription();
        var errorResponseDto = getCoreServiceErrorResponseDto(ErrorResponseStatusType.INVALID_USER_ROLE);
        errorResponseDto.setErrorCode(4021);
        when(subscriptionService.getSubscriptionFromId(TEMP_SUBSCRIPTION_ID)).thenReturn(tempSubscription);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                errorResponseDto.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.subscribe(USER_ID, TEMP_SUBSCRIPTION_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.INVALID_USER_ROLE.getCode(),
                exception.getErrorResponseType().getCode());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_SubscriptionPlanIsInactiveToSubscribe() {
        Subscription tempSubscription = getSampleSubscription();
        var errorResponseDto = getCoreServiceErrorResponseDto(ErrorResponseStatusType.INACTIVE_SUBSCRIPTION_PLAN);
        errorResponseDto.setErrorCode(4046);
        when(subscriptionService.getSubscriptionFromId(TEMP_SUBSCRIPTION_ID)).thenReturn(tempSubscription);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                errorResponseDto.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.subscribe(USER_ID, TEMP_SUBSCRIPTION_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.INACTIVE_SUBSCRIPTION_PLAN.getCode(),
                exception.getErrorResponseType().getCode());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_AlreadyUserSubscribedThePlan() {
        Subscription tempSubscription = getSampleSubscription();
        var errorResponseDto = getCoreServiceErrorResponseDto(ErrorResponseStatusType.ALREADY_SUBSCRIBE_USER);
        errorResponseDto.setErrorCode(4043);
        when(subscriptionService.getSubscriptionFromId(TEMP_SUBSCRIPTION_ID)).thenReturn(tempSubscription);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                errorResponseDto.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.subscribe(USER_ID, TEMP_SUBSCRIPTION_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.ALREADY_SUBSCRIBE_USER.getCode(),
                exception.getErrorResponseType().getCode());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_InvalidSubscriptionPlanIdIsSendToSubscribeThePlan() {
        Subscription tempSubscription = getSampleSubscription();
        var errorResponseDto = getCoreServiceErrorResponseDto(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN);
        errorResponseDto.setErrorCode(4040);
        when(subscriptionService.getSubscriptionFromId(TEMP_SUBSCRIPTION_ID)).thenReturn(tempSubscription);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                errorResponseDto.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.subscribe(USER_ID, TEMP_SUBSCRIPTION_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN.getCode(),
                exception.getErrorResponseType().getCode());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_UnsupportedUserTypeIsSendToSubscribeThePlan() {
        Subscription tempSubscription = getSampleSubscription();
        var errorResponseDto = getCoreServiceErrorResponseDto(ErrorResponseStatusType.UNSUPPORTED_SUBSCRIPTION_USER_TYPE);
        errorResponseDto.setErrorCode(4042);
        when(subscriptionService.getSubscriptionFromId(TEMP_SUBSCRIPTION_ID)).thenReturn(tempSubscription);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                errorResponseDto.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.subscribe(USER_ID, TEMP_SUBSCRIPTION_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.UNSUPPORTED_SUBSCRIPTION_USER_TYPE.getCode(),
                exception.getErrorResponseType().getCode());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_InvalidMerchantIdSendToSubscribeThePlan() {
        Subscription tempSubscription = getSampleSubscription();
        var errorResponseDto = getCoreServiceErrorResponseDto(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        errorResponseDto.setErrorCode(4101);
        when(subscriptionService.getSubscriptionFromId(TEMP_SUBSCRIPTION_ID)).thenReturn(tempSubscription);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                errorResponseDto.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.subscribe(USER_ID, TEMP_SUBSCRIPTION_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode(),
                exception.getErrorResponseType().getCode());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_UnexpectedErrorsThrowByCoreServiceToSubscribeThePlan() {
        Subscription tempSubscription = getSampleSubscription();
        var errorResponseDto = getCoreServiceErrorResponseDto(ErrorResponseStatusType.INVALID_ORDER_ID);
        errorResponseDto.setErrorCode(4011);
        when(subscriptionService.getSubscriptionFromId(TEMP_SUBSCRIPTION_ID)).thenReturn(tempSubscription);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                errorResponseDto.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.subscribe(USER_ID, TEMP_SUBSCRIPTION_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Unknown error from core service", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_FailedToConvertErrorMessageToObject() {
        Subscription tempSubscription = getSampleSubscription();
        when(subscriptionService.getSubscriptionFromId(TEMP_SUBSCRIPTION_ID)).thenReturn(tempSubscription);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                INVALID_ERROR_MESSAGE));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.subscribe(USER_ID, TEMP_SUBSCRIPTION_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Converting string data to object is failed", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_CoreServiceThrowInternalServiceError() {
        Subscription tempSubscription = getSampleSubscription();
        when(subscriptionService.getSubscriptionFromId(TEMP_SUBSCRIPTION_ID)).thenReturn(tempSubscription);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.subscribe(USER_ID, TEMP_SUBSCRIPTION_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Payment not captured yet for temporary-subscription-id: " + TEMP_SUBSCRIPTION_ID,
                exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_PaymentNotCaptured() {
        Subscription tempSubscription = getSampleSubscription();
        tempSubscription.setSubscriptionStatus(SubscriptionStatus.TEMPORARY);
        when(subscriptionService.getSubscriptionFromId(TEMP_SUBSCRIPTION_ID)).thenReturn(tempSubscription);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.subscribe(USER_ID, TEMP_SUBSCRIPTION_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Payment not captured yet for temporary-subscription-id: " + TEMP_SUBSCRIPTION_ID,
                exception.getMessage());
    }

    @Test
    void Should_ReturnBasicCouponResponseDto_When_SpecificDealHasCoupon() {
        BasicCouponResponseDto responseDto = getSampleBasicCouponResponseDto();
        var responseWrapper = getCouponResponseDto();
        String url = baseUrl + purchaseCouponUrl.replace(DEAL_ID_PHRASE, DEAL_ID);
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<BasicCouponResponseDto>>() {
        };
        var response = new ResponseEntity<CoreResponseWrapper<BasicCouponResponseDto>>(responseWrapper, HttpStatus.OK);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(header);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, responseTypeReference)).thenReturn(response);
        assertEquals(responseDto.getId(),
                coreService.validateAndGetCouponCode(DEAL_ID, AUTH_TOKEN, USER_ID, TIME_ZONE).getId());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_CouponNotFoundInCoreServiceToGetCouponCode() {
        String url = baseUrl + purchaseCouponUrl.replace(DEAL_ID_PHRASE, DEAL_ID);
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<BasicCouponResponseDto>>() {
        };
        var errorResponse = getCoreServiceErrorResponseDto(ErrorResponseStatusType.COUPON_NOT_FOUND);
        errorResponse.setErrorCode(4062);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(header);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorResponse.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.validateAndGetCouponCode(DEAL_ID, AUTH_TOKEN, USER_ID, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_CoreServiceThrowInternalServerErrorForValidateCouponCode() {
        var responseWrapper = getCouponResponseDto();
        responseWrapper.setStatus(ResponseStatusType.ERROR);
        String url = baseUrl + purchaseCouponUrl.replace(DEAL_ID_PHRASE, DEAL_ID);
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<BasicCouponResponseDto>>() {
        };
        var response = new ResponseEntity<CoreResponseWrapper<BasicCouponResponseDto>>(responseWrapper, HttpStatus.INTERNAL_SERVER_ERROR);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(header);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, responseTypeReference)).thenReturn(response);
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.validateAndGetCouponCode(DEAL_ID, AUTH_TOKEN, USER_ID, TIME_ZONE));
        assertEquals("Validating & getting coupon code was failed from core service.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_ValidateCouponCodeIsFailed() {
        String url = baseUrl + purchaseCouponUrl.replace(DEAL_ID_PHRASE, DEAL_ID);
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<BasicCouponResponseDto>>() {
        };
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(header);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.validateAndGetCouponCode(DEAL_ID, AUTH_TOKEN, USER_ID, TIME_ZONE));
        assertEquals("Validating & getting coupon code was failed from core service.", exception.getMessage());
    }

    @Test
    void Should_ConfirmCouponPurchaseByCallingCoreService() {
        CouponPaymentCaptureRequestDto couponPaymentCaptureRequestDto = getSampleCouponPaymentCaptureRequestDto();
        String url = baseUrl + confirmCouponPurchaseUrl
                .replace(COUPON_ID_PHRASE, couponPaymentCaptureRequestDto.getCouponId())
                .replace(DEAL_ID_PHRASE, couponPaymentCaptureRequestDto.getDealId())
                .replace(PURCHASE_STATUS_ID_PHRASE, CouponPurchaseStatus.PURCHASED.toString());
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<ResponseDto>>() {
        };
        CoreResponseWrapper<ResponseDto> responseWrapper = new CoreResponseWrapper<>();
        responseWrapper.setStatus(ResponseStatusType.SUCCESS);
        responseWrapper.setData(null);
        var response = new ResponseEntity<CoreResponseWrapper<ResponseDto>>(responseWrapper, HttpStatus.OK);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(header);
        when(restTemplate.exchange(url, HttpMethod.PUT, entity, responseTypeReference)).thenReturn(response);
        coreService.confirmCouponPurchase(couponPaymentCaptureRequestDto, AUTH_TOKEN, USER_ID, TIME_ZONE,
                CouponPurchaseStatus.PURCHASED);
        assertEquals("200 OK", restTemplate.exchange(url, HttpMethod.PUT, entity, responseTypeReference)
                .getStatusCode().toString());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_CouponNotFoundInCoreServiceToConfirmCouponPurchase() {
        CouponPaymentCaptureRequestDto couponPaymentCaptureRequestDto = getSampleCouponPaymentCaptureRequestDto();
        String url = baseUrl + confirmCouponPurchaseUrl
                .replace(COUPON_ID_PHRASE, couponPaymentCaptureRequestDto.getCouponId())
                .replace(DEAL_ID_PHRASE, couponPaymentCaptureRequestDto.getDealId())
                .replace(PURCHASE_STATUS_ID_PHRASE, CouponPurchaseStatus.PURCHASED.toString());
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<ResponseDto>>() {
        };
        var errorResponse = getCoreServiceErrorResponseDto(ErrorResponseStatusType.COUPON_NOT_FOUND);
        errorResponse.setErrorCode(4062);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(header);
        when(restTemplate.exchange(url, HttpMethod.PUT, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorResponse.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.confirmCouponPurchase(couponPaymentCaptureRequestDto, AUTH_TOKEN, USER_ID, TIME_ZONE,
                        CouponPurchaseStatus.PURCHASED));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_CoreServiceThrowInternalServerErrorForConfirmCouponPurchase() {
        CouponPaymentCaptureRequestDto couponPaymentCaptureRequestDto = getSampleCouponPaymentCaptureRequestDto();
        String url = baseUrl + confirmCouponPurchaseUrl
                .replace(COUPON_ID_PHRASE, couponPaymentCaptureRequestDto.getCouponId())
                .replace(DEAL_ID_PHRASE, couponPaymentCaptureRequestDto.getDealId())
                .replace(PURCHASE_STATUS_ID_PHRASE, CouponPurchaseStatus.PURCHASED.toString());
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<ResponseDto>>() {
        };
        CoreResponseWrapper<ResponseDto> responseWrapper = new CoreResponseWrapper<>();
        responseWrapper.setStatus(ResponseStatusType.ERROR);
        responseWrapper.setData(null);
        var response = new ResponseEntity<CoreResponseWrapper<ResponseDto>>(responseWrapper, HttpStatus.BAD_REQUEST);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(header);
        when(restTemplate.exchange(url, HttpMethod.PUT, entity, responseTypeReference)).thenReturn(response);
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.confirmCouponPurchase(couponPaymentCaptureRequestDto, AUTH_TOKEN, USER_ID, TIME_ZONE,
                        CouponPurchaseStatus.PURCHASED));
        assertEquals("Confirming coupon purchase was failed from core service.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_ConfirmCouponPurchaseIsFailed() {
        CouponPaymentCaptureRequestDto couponPaymentCaptureRequestDto = getSampleCouponPaymentCaptureRequestDto();
        String url = baseUrl + confirmCouponPurchaseUrl
                .replace(COUPON_ID_PHRASE, couponPaymentCaptureRequestDto.getCouponId())
                .replace(DEAL_ID_PHRASE, couponPaymentCaptureRequestDto.getDealId())
                .replace(PURCHASE_STATUS_ID_PHRASE, CouponPurchaseStatus.PURCHASED.toString());
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<ResponseDto>>() {
        };
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(header);
        when(restTemplate.exchange(url, HttpMethod.PUT, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.confirmCouponPurchase(couponPaymentCaptureRequestDto, AUTH_TOKEN, USER_ID, TIME_ZONE,
                        CouponPurchaseStatus.PURCHASED));
        assertEquals("Confirming coupon purchase was failed from core service", exception.getMessage());
    }

    @Test
    void Should_SubscribeThePlan_When_FreeSubscriptionAvailable() {
        Subscription subscription = getSampleSubscription();
        SubscriptionValidationRequestDto validationRequestDto = getSampleSubscriptionValidationRequestDto();
        SubscriptionValidationResponseDto validationResponseDto = getSampleSubscriptionValidationResponseDto();
        String url = baseUrl + freeSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        CoreResponseWrapper<SubscriptionValidationResponseDto> responseWrapper = new CoreResponseWrapper<>();
        responseWrapper.setStatus(ResponseStatusType.SUCCESS);
        responseWrapper.setData(validationResponseDto);
        var response = new ResponseEntity<CoreResponseWrapper<SubscriptionValidationResponseDto>>(responseWrapper, HttpStatus.OK);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<SubscriptionValidationRequestDto>(validationRequestDto, header);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference)).thenReturn(response);
        when(subscriptionService.buildSubscriptionObject(validationResponseDto, validationRequestDto, USER_ID))
                .thenReturn(subscription);
        coreService.freeSubscription(validationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE);
        verify(subscriptionService).saveFreeSubscription(subscription, USER_ID);
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_CoreServiceNotSubscribeTheFreeSubscription() {
        SubscriptionValidationRequestDto validationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + freeSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        CoreResponseWrapper<SubscriptionValidationResponseDto> responseWrapper = new CoreResponseWrapper<>();
        responseWrapper.setStatus(ResponseStatusType.ERROR);
        responseWrapper.setData(null);
        var response = new ResponseEntity<CoreResponseWrapper<SubscriptionValidationResponseDto>>(responseWrapper, HttpStatus.BAD_REQUEST);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<SubscriptionValidationRequestDto>(validationRequestDto, header);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference)).thenReturn(response);
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.freeSubscription(validationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Core service returned failed free subscription", exception.getMessage());
    }

    @Test
    void Should_ReturnKupiOrderPaymentException_When_MissingRequiredFieldsAreSendToSubscribeFreeSubscription() {
        SubscriptionValidationRequestDto validationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + freeSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var errorResponse = getCoreServiceErrorResponseDto(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
        errorResponse.setErrorCode(4000);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<SubscriptionValidationRequestDto>(validationRequestDto, header);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorResponse.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.freeSubscription(validationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS, exception.getErrorResponseType());
    }

    @Test
    void Should_ReturnKupiOrderPaymentException_When_InvalidUserRoleIsSendToSubscribeFreeSubscription() {
        SubscriptionValidationRequestDto validationRequestDto = getSampleSubscriptionValidationRequestDto();
        validationRequestDto.setUserType(UserType.ADMIN);
        String url = baseUrl + freeSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var errorResponse = getCoreServiceErrorResponseDto(ErrorResponseStatusType.INVALID_USER_ROLE);
        errorResponse.setErrorCode(4021);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<SubscriptionValidationRequestDto>(validationRequestDto, header);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorResponse.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.freeSubscription(validationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.INVALID_USER_ROLE, exception.getErrorResponseType());
    }

    @Test
    void Should_ReturnKupiOrderPaymentException_When_SelectedSubscriptionPlanIsInactiveForFreeSubscription() {
        SubscriptionValidationRequestDto validationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + freeSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var errorResponse = getCoreServiceErrorResponseDto(ErrorResponseStatusType.INACTIVE_SUBSCRIPTION_PLAN);
        errorResponse.setErrorCode(4046);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<SubscriptionValidationRequestDto>(validationRequestDto, header);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorResponse.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.freeSubscription(validationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.INACTIVE_SUBSCRIPTION_PLAN, exception.getErrorResponseType());
    }

    @Test
    void Should_ReturnKupiOrderPaymentException_When_SelectedSubscriptionPlanIsNotAFreeSubscriptionPlan() {
        SubscriptionValidationRequestDto validationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + freeSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var errorResponse = getCoreServiceErrorResponseDto(ErrorResponseStatusType.NOT_FREE_SUBSCRIPTION);
        errorResponse.setErrorCode(4072);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<SubscriptionValidationRequestDto>(validationRequestDto, header);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorResponse.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.freeSubscription(validationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.NOT_FREE_SUBSCRIPTION, exception.getErrorResponseType());
    }

    @Test
    void Should_ReturnKupiOrderPaymentException_When_UserAlreadySubscribeTheFreeSubscriptionPlan() {
        SubscriptionValidationRequestDto validationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + freeSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var errorResponse = getCoreServiceErrorResponseDto(ErrorResponseStatusType.ALREADY_SUBSCRIBE_USER);
        errorResponse.setErrorCode(4043);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<SubscriptionValidationRequestDto>(validationRequestDto, header);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorResponse.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.freeSubscription(validationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.ALREADY_SUBSCRIBE_USER, exception.getErrorResponseType());
    }

    @Test
    void Should_ReturnKupiOrderPaymentException_When_InvalidSubscriptionIdIsProvidedForFreeSubscriptionPlan() {
        SubscriptionValidationRequestDto validationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + freeSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var errorResponse = getCoreServiceErrorResponseDto(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN);
        errorResponse.setErrorCode(4040);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<SubscriptionValidationRequestDto>(validationRequestDto, header);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorResponse.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.freeSubscription(validationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN, exception.getErrorResponseType());
    }

    @Test
    void Should_ReturnKupiOrderPaymentException_When_InvalidMerchantIdIsProvidedForFreeSubscriptionPlan() {
        SubscriptionValidationRequestDto validationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + freeSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var errorResponse = getCoreServiceErrorResponseDto(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        errorResponse.setErrorCode(4101);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<SubscriptionValidationRequestDto>(validationRequestDto, header);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorResponse.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.freeSubscription(validationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Error received from core-service for validation request.", exception.getMessage());
        assertEquals(ErrorResponseStatusType.INVALID_MERCHANT_ID, exception.getErrorResponseType());
    }

    @Test
    void Should_ReturnKupiOrderPaymentException_When_UnexpectedErrorsThrownByCoreServiceForFreeSubscriptionPlan() {
        SubscriptionValidationRequestDto validationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + freeSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var errorResponse = getCoreServiceErrorResponseDto(ErrorResponseStatusType.INVALID_ADDRESS_ID);
        errorResponse.setErrorCode(4291);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<SubscriptionValidationRequestDto>(validationRequestDto, header);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorResponse.toJson()));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.freeSubscription(validationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Unknown error from core service", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_CouldNotConvertErrorResponseToJavaObjectForFreeSubscription() {
        SubscriptionValidationRequestDto validationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + freeSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(validationRequestDto, header);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, INVALID_ERROR_MESSAGE));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.freeSubscription(validationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Converting string data to object is failed", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_CoreServiceThrowIntenalServerErrorForFreeSubscription() {
        SubscriptionValidationRequestDto validationRequestDto = getSampleSubscriptionValidationRequestDto();
        String url = baseUrl + freeSubscriptionUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
        };
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<>(validationRequestDto, header);
        when(restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.freeSubscription(validationRequestDto, USER_ID, AUTH_TOKEN, TIME_ZONE));
        assertEquals("Subscribe into free subscription request was failed from core service",
                exception.getMessage());
    }

    @Test
    void Should_ReturnSubscriptionPlanListResponseDto_When_GetSubscriptionCountFromCoreService() {
        SubscriptionPlanListResponseDto subscriptionPlanListResponseDto = getSampleSubscriptionPlanListResponseDto();
        String url = baseUrl + subscriptionPlanListUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionPlanListResponseDto>>() {
        };
        CoreResponseWrapper<SubscriptionPlanListResponseDto> responseWrapper = new CoreResponseWrapper<>();
        responseWrapper.setStatus(ResponseStatusType.SUCCESS);
        responseWrapper.setData(subscriptionPlanListResponseDto);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<SubscriptionValidationRequestDto>(header);
        var response = new ResponseEntity<CoreResponseWrapper<SubscriptionPlanListResponseDto>>(responseWrapper, HttpStatus.OK);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, responseTypeReference)).thenReturn(response);
        assertEquals(subscriptionPlanListResponseDto, coreService.getSubscriptionPlanCount(AUTH_TOKEN, USER_ID, TIME_ZONE));
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_SubscriptionPlanListNotSendByCoreService() {
        String url = baseUrl + subscriptionPlanListUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionPlanListResponseDto>>() {
        };
        CoreResponseWrapper<SubscriptionPlanListResponseDto> responseWrapper = new CoreResponseWrapper<>();
        responseWrapper.setStatus(ResponseStatusType.ERROR);
        responseWrapper.setData(null);
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<SubscriptionValidationRequestDto>(header);
        var response = new ResponseEntity<CoreResponseWrapper<SubscriptionPlanListResponseDto>>(responseWrapper, HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, responseTypeReference)).thenReturn(response);
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.getSubscriptionPlanCount(AUTH_TOKEN, USER_ID, TIME_ZONE));
        assertEquals("Subscription plan list was failed from core service.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_GetSubscriptionPlanListIsFailed() {
        String url = baseUrl + subscriptionPlanListUrl;
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionPlanListResponseDto>>() {
        };
        var header = getAuthHeaders(AUTH_TOKEN, USER_ID, TIME_ZONE);
        var entity = new HttpEntity<SubscriptionValidationRequestDto>(header);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, responseTypeReference))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                coreService.getSubscriptionPlanCount(AUTH_TOKEN, USER_ID, TIME_ZONE));
        assertEquals("Getting subscription plan list was failed" + exception.getCause(), exception.getMessage());
    }

    /**
     * This method creates sample subscription validation response dto
     *
     * @return SubscriptionValidationResponseDt
     */
    private SubscriptionValidationResponseDto getSampleSubscriptionValidationResponseDto() {
        SubscriptionValidationResponseDto subscriptionValidationResponseDto = new SubscriptionValidationResponseDto();
        subscriptionValidationResponseDto.setSubscriptionPlanId(SUBSCRIPTION_PLAN_ID);
        subscriptionValidationResponseDto.setSubTitle("Subtitle");
        subscriptionValidationResponseDto.setFree(true);
        subscriptionValidationResponseDto.setPlanType(SubscriptionType.MONTHLY);
        subscriptionValidationResponseDto.setAmount(new SubscriptionAmountResponseDto(AMOUNT));
        return subscriptionValidationResponseDto;
    }

    /**
     * This method creates sample auth header
     *
     * @param authToken auth token
     * @param userId    user id
     * @param timeZone  time zone
     * @return HttpHeaders
     */
    private HttpHeaders getAuthHeaders(String authToken, String userId, String timeZone) {
        var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken.trim());
        headers.set(TIME_ZONE_HEADER, timeZone);
        headers.set(USER_ID_HEADER, userId);
        return headers;
    }

    /**
     * This method creates sample subscription validation request dto
     *
     * @return SubscriptionValidationRequestDto
     */
    private SubscriptionValidationRequestDto getSampleSubscriptionValidationRequestDto() {
        SubscriptionValidationRequestDto subscriptionValidationRequestDto = new SubscriptionValidationRequestDto();
        subscriptionValidationRequestDto.setSubscriptionPlanId(SUBSCRIPTION_PLAN_ID);
        subscriptionValidationRequestDto.setUserType(UserType.MERCHANT);
        return subscriptionValidationRequestDto;
    }

    /**
     * This method creates sample core service error response dto
     *
     * @param statusType error response status type
     * @return CoreServiceErrorResponseDto
     */
    private CoreServiceErrorResponseDto getCoreServiceErrorResponseDto(ErrorResponseStatusType statusType) {
        CoreServiceErrorResponseDto errorResponseDto = new CoreServiceErrorResponseDto();
        errorResponseDto.setStatus(ResponseStatusType.ERROR.name());
        errorResponseDto.setErrorCode(statusType.getCode());
        errorResponseDto.setMessage(statusType.getMessage());
        errorResponseDto.setDisplayMessage(statusType.getMessage());
        return errorResponseDto;
    }

    /**
     * This method creates sample subscription
     *
     * @return Subscription
     */
    private Subscription getSampleSubscription() {
        Subscription subscription = new Subscription();
        subscription.setId(TEMP_SUBSCRIPTION_ID);
        subscription.setUserType(UserType.MERCHANT);
        subscription.setSubscriptionPlanId(SUBSCRIPTION_PLAN_ID);
        subscription.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        subscription.setSubscriptionType(SubscriptionType.MONTHLY);
        return subscription;
    }

    /**
     * This method creates sample basic coupon response dto
     *
     * @return BasicCouponResponseDto
     */
    private BasicCouponResponseDto getSampleBasicCouponResponseDto() {
        BasicCouponResponseDto basicCouponResponseDto = new BasicCouponResponseDto();
        basicCouponResponseDto.setDealId(DEAL_ID);
        basicCouponResponseDto.setCouponActive(true);
        basicCouponResponseDto.setCouponCode("AAAAAA");
        return basicCouponResponseDto;
    }

    /**
     * This method creates sample basic coupon response dto wrapper
     *
     * @return BasicCouponResponseDto
     */
    private CoreResponseWrapper<BasicCouponResponseDto> getCouponResponseDto() {
        BasicCouponResponseDto basicCouponResponseDto = getSampleBasicCouponResponseDto();
        CoreResponseWrapper<BasicCouponResponseDto> responseWrapper = new CoreResponseWrapper();
        responseWrapper.setData(basicCouponResponseDto);
        responseWrapper.setStatus(ResponseStatusType.SUCCESS);
        return responseWrapper;
    }

    /**
     * This method creates sample coupon payment capture request dto
     *
     * @return CouponPaymentCaptureRequestDto
     */
    private CouponPaymentCaptureRequestDto getSampleCouponPaymentCaptureRequestDto() {
        CouponPaymentCaptureRequestDto couponPaymentCaptureRequestDto = new CouponPaymentCaptureRequestDto();
        couponPaymentCaptureRequestDto.setCouponId(COUPON_ID);
        couponPaymentCaptureRequestDto.setDealId(DEAL_ID);
        return couponPaymentCaptureRequestDto;
    }

    private SubscriptionPlanListResponseDto getSampleSubscriptionPlanListResponseDto() {
        SubscriptionPlanListResponseDto subscriptionPlanListResponseDto = new SubscriptionPlanListResponseDto();
        BasicSubscriptionPlanResponseDto basicSubscriptionPlanResponseDto = new BasicSubscriptionPlanResponseDto();
        basicSubscriptionPlanResponseDto.setId(SUBSCRIPTION_PLAN_ID);
        basicSubscriptionPlanResponseDto.setCount(10);
        basicSubscriptionPlanResponseDto.setTitle("Subscription Title");
        List<BasicSubscriptionPlanResponseDto> subscriptionPlanResponseDtoList = new ArrayList<>();
        subscriptionPlanResponseDtoList.add(basicSubscriptionPlanResponseDto);
        subscriptionPlanListResponseDto.setPlans(subscriptionPlanResponseDtoList);
        return subscriptionPlanListResponseDto;
    }
}