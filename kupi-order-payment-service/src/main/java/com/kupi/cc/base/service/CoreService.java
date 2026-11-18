package com.kupi.cc.base.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kupi.cc.base.domain.request.CouponPaymentCaptureRequestDto;
import com.kupi.cc.base.domain.request.SubscriptionRequestDto;
import com.kupi.cc.base.domain.request.SubscriptionValidationRequestDto;
import com.kupi.cc.base.domain.response.*;
import com.kupi.cc.base.enums.CouponPurchaseStatus;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SubscriptionStatus;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.wrapper.CoreResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

/**
 * Core Service
 */
@Slf4j
@Service
public class CoreService {

    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String USER_ID_HEADER = "User-Id";
    private static final String DEAL_ID = "##DEAL-ID##";
    private static final String COUPON_ID = "##COUPON-ID##";
    private static final String STATUS = "##STATUS##";
    private static final String FAILED_LOG_MESSAGE = "{}. statusCode: {}, body: {}";
    private final String purchaseCouponUrl;
    private final String subscriptionPlanListUrl;
    private final String confirmCouponPurchaseUrl;
    private final String validateSubscriptionUrl;
    private final String freeSubscriptionUrl;
    private final String subscribeUrl;

    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private RestTemplate restTemplate;

    public CoreService(@Value("${core.baseUrl}") String baseUrl,
                       @Value("${core.uri.purchaseCoupon}") String purchaseCouponUrl,
                       @Value("${core.uri.subscriptionPlanListUrl}") String subscriptionPlanListUrl,
                       @Value("${core.uri.confirmCouponPurchase}") String confirmCouponPurchaseUrl,
                       @Value("${core.uri.validateSubscription}") String validateSubscriptionUri,
                       @Value("${core.uri.subscribe}") String subscribeUri,
                       @Value("${core.uri.freeSubscribe}") String freeSubscriptionUri,
                       RestTemplate restTemplate, SubscriptionService subscriptionService) {
        this.subscriptionPlanListUrl = baseUrl + subscriptionPlanListUrl;
        this.subscriptionService = subscriptionService;
        this.validateSubscriptionUrl = baseUrl + validateSubscriptionUri;
        this.subscribeUrl = baseUrl + subscribeUri;
        this.freeSubscriptionUrl = baseUrl + freeSubscriptionUri;
        this.purchaseCouponUrl = baseUrl + purchaseCouponUrl;
        this.confirmCouponPurchaseUrl = baseUrl + confirmCouponPurchaseUrl;
        this.restTemplate = restTemplate;
    }

    /**
     * call core service to subscribe into free subscription plan
     *
     * @param subscriptionRequestDto subscription request dto
     */
    public void freeSubscription(SubscriptionValidationRequestDto subscriptionRequestDto,
                                 String userId, String authToken, String timeZone) {

        var subscriptionPlanId = subscriptionRequestDto.getSubscriptionPlanId();
        var headers = getAuthHeaders(authToken, userId, timeZone);
        HttpEntity<SubscriptionValidationRequestDto> entity = new HttpEntity<>(subscriptionRequestDto, headers);

        try {
            log.info("Calling core service to subscribe into free subscription plan. url: {}.", freeSubscriptionUrl);
            var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
            };
            var result = restTemplate.exchange(
                    freeSubscriptionUrl, HttpMethod.POST, entity, responseTypeReference);
            var response = Objects.requireNonNull(result.getBody());
            log.info("Received free subscription response from core service. Response body: {}.", response.toLogJson());

            if (result.getStatusCode() == HttpStatus.OK) {
                var responseDto = response.getData();
                var subscription = subscriptionService.buildSubscriptionObject(
                        responseDto, subscriptionRequestDto, userId);
                subscription.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
                subscriptionService.saveFreeSubscription(subscription, userId);
                return;
            }

            log.error("Core service returned failed free subscription response for subscription-id: {}", subscriptionPlanId);
            throw new KupiOrderPaymentException("Core service returned failed free subscription");
        } catch (HttpClientErrorException e) {
            log.error(FAILED_LOG_MESSAGE, "Subscribe into free subscription request was failed from core service",
                    e.getRawStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                handleCoreServiceBadResponse(e);
            }
            throw new KupiOrderPaymentException("Subscribe into free subscription request was failed from core service");
        }
    }

    /**
     * call core-service and validate the subscription request
     *
     * @param subscriptionValidationRequestDto subscription validation request dto
     * @return subscription plan details
     */
    public SubscriptionValidationResponseDto validateSubscription(
            SubscriptionValidationRequestDto subscriptionValidationRequestDto,
            String userId, String authToken, String timeZone) {

        var subscriptionPlanId = subscriptionValidationRequestDto.getSubscriptionPlanId();
        var headers = getAuthHeaders(authToken, userId, timeZone);
        HttpEntity<SubscriptionValidationRequestDto> entity = new HttpEntity<>(subscriptionValidationRequestDto, headers);

        try {
            log.info("Calling core service to validate subscription request. url: {}.", validateSubscriptionUrl);
            var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionValidationResponseDto>>() {
            };
            var result = restTemplate.exchange(
                    validateSubscriptionUrl, HttpMethod.POST, entity, responseTypeReference);
            var response = Objects.requireNonNull(result.getBody());
            log.info("Received subscription validation response from core service. Response body: {}.", response.toLogJson());

            if (result.getStatusCode() == HttpStatus.OK) {
                return response.getData();
            }

            log.error("Core service returned failed validation response for subscription-id: {}", subscriptionPlanId);
            throw new KupiOrderPaymentException("Validating subscription request was failed from core service");
        } catch (HttpClientErrorException e) {
            log.error(FAILED_LOG_MESSAGE, "Validating subscription request was failed from core service",
                    e.getRawStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                handleCoreServiceBadResponse(e);
            }
            throw new KupiOrderPaymentException("Validating subscription request was failed from core service");
        }
    }

    /**
     * subscribe already paid temporary subscription
     * send request to core-service to subscribe to the subscription-plan if user has already completed the payment
     *
     * @param userId             user id
     * @param tempSubscriptionId temporary subscription id ( already paid )
     * @param authToken          auth token
     */
    public void subscribe(String userId, String tempSubscriptionId, String authToken, String timeZone) {
        var temporarySubscription = subscriptionService.getSubscriptionFromId(tempSubscriptionId);

        var subscriptionRequestDto = new SubscriptionRequestDto(
                tempSubscriptionId,
                temporarySubscription.getSubscriptionPlanId(),
                temporarySubscription.getUserType());
        var headers = getAuthHeaders(authToken, userId, timeZone);
        HttpEntity<SubscriptionRequestDto> entity = new HttpEntity<>(subscriptionRequestDto, headers);
        var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<ResponseDto>>() {
        };

        try {
            log.info("Calling core service to subscribe. URL: {}.", subscribeUrl);
            var result = restTemplate.exchange(
                    subscribeUrl, HttpMethod.POST, entity, responseTypeReference);
            var response = Objects.requireNonNull(result.getBody());
            log.info("Received subscription response from core service. Result body: {}.", response.toLogJson());

            if (result.getStatusCode() == HttpStatus.OK) {
                subscriptionService.saveCompletedSubscription(temporarySubscription, userId);
                return;
            }

            log.error("Core service returned failed subscription response for temp-subscription-id: {}", tempSubscriptionId);
            throw new KupiOrderPaymentException("Failed to subscribe for temporary-subscription id: " + tempSubscriptionId);
        } catch (HttpClientErrorException e) {
            log.error(FAILED_LOG_MESSAGE, "Failed to subscribe for temporary-subscription id: " + tempSubscriptionId,
                    e.getRawStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                handleCoreServiceBadResponse(e);
            }
            throw new KupiOrderPaymentException("Payment not captured yet for temporary-subscription-id: " + tempSubscriptionId);
        }
    }

    /**
     * map error codes from core-service into error states
     */
    private void handleCoreServiceBadResponse(HttpClientErrorException e) {
        CoreServiceErrorResponseDto errorResponseDto = getErrorResponseDto(e);
        ErrorResponseStatusType statusType;
        switch (errorResponseDto.getErrorCode()) {
            case 4000:
                statusType = ErrorResponseStatusType.MISSING_REQUIRED_FIELDS;
                break;
            case 4021:
                statusType = ErrorResponseStatusType.INVALID_USER_ROLE;
                break;
            case 4046:
                statusType = ErrorResponseStatusType.INACTIVE_SUBSCRIPTION_PLAN;
                break;
            case 4043:
                statusType = ErrorResponseStatusType.ALREADY_SUBSCRIBE_USER;
                break;
            case 4040:
                statusType = ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN;
                break;
            case 4042:
                statusType = ErrorResponseStatusType.UNSUPPORTED_SUBSCRIPTION_USER_TYPE;
                break;
            case 4101:
                statusType = ErrorResponseStatusType.INVALID_MERCHANT_ID;
                break;
            case 4062:
                statusType = ErrorResponseStatusType.COUPON_NOT_FOUND;
                break;
            case 4071:
                statusType = ErrorResponseStatusType.COUPON_ALREADY_PURCHASED;
                break;
            case 4072:
                statusType = ErrorResponseStatusType.NOT_FREE_SUBSCRIPTION;
                break;
            case 4076:
                statusType = ErrorResponseStatusType.DEAL_IS_NOT_AVAILABLE;
                break;
            default:
                log.error("Unknown error from core service. Reason : {}", e.getMessage());
                throw new KupiOrderPaymentException("Unknown error from core service");
        }
        throw new KupiOrderPaymentException(statusType, "Error received from core-service for validation request.");
    }

    private CoreServiceErrorResponseDto getErrorResponseDto(HttpClientErrorException e) {
        String message = e.getMessage();
        assert message != null;
        message = message.substring(message.indexOf("{"), message.indexOf("}") + 1);
        try {
            return new ObjectMapper().readValue(message, CoreServiceErrorResponseDto.class);
        } catch (IOException exception) {
            throw new KupiOrderPaymentException("Converting string data to object is failed", exception);
        }
    }

    /**
     * This method calls core service & gets coupon code for deal.
     *
     * @param dealId    dealId
     * @param authToken authToken
     * @param userId    userId
     * @param timeZone  timeZone
     * @return basicCouponResponseDto
     */
    public BasicCouponResponseDto validateAndGetCouponCode(String dealId, String authToken,
                                                           String userId, String timeZone) {
        HttpHeaders headers = getAuthHeaders(authToken, userId, timeZone);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            String url = purchaseCouponUrl.replace(DEAL_ID, dealId);
            var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<BasicCouponResponseDto>>() {
            };
            log.debug("Calling core service to validate & get coupon code. url: {},", url);
            ResponseEntity<CoreResponseWrapper<BasicCouponResponseDto>> result =
                    restTemplate.exchange(url, HttpMethod.GET, entity, responseTypeReference);
            var response = Objects.requireNonNull(result.getBody());
            log.debug("Get coupon code api response from core: {}", response.toLogJson());
            if (result.getStatusCode() == HttpStatus.OK) {
                return response.getData();
            }
            throw new KupiOrderPaymentException("Validating & getting coupon code was failed from core service.");
        } catch (HttpClientErrorException e) {
            log.error("Validating & getting coupon code was failed from core service. statusCode: {}, body: {}",
                    e.getRawStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                handleCoreServiceBadResponse(e);
            }
            throw new KupiOrderPaymentException("Validating & getting coupon code was failed from core service.", e);
        }
    }

    /**
     * This method calls core service & confirms coupon purchase.
     *
     * @param couponPaymentCaptureRequestDto couponPaymentCaptureRequestDto
     * @param authToken                      authToken
     * @param userId                         userId
     * @param timeZone                       timeZone
     * @param purchaseStatus                 purchaseStatus
     */
    public void confirmCouponPurchase(CouponPaymentCaptureRequestDto couponPaymentCaptureRequestDto, String authToken,
                                      String userId, String timeZone, CouponPurchaseStatus purchaseStatus) {
        HttpHeaders headers = getAuthHeaders(authToken, userId, timeZone);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            String url = confirmCouponPurchaseUrl
                    .replace(COUPON_ID, couponPaymentCaptureRequestDto.getCouponId())
                    .replace(DEAL_ID, couponPaymentCaptureRequestDto.getDealId())
                    .replace(STATUS, purchaseStatus.toString());
            var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<ResponseDto>>() {
            };
            log.debug("Calling core service to confirm coupon purchase. url: {},", confirmCouponPurchaseUrl);
            ResponseEntity<CoreResponseWrapper<ResponseDto>> result =
                    restTemplate.exchange(url, HttpMethod.PUT, entity, responseTypeReference);
            String response = Objects.requireNonNull(result.getBody()).toLogJson();
            log.debug("Confirm coupon purchase api response from core: {}", response);
            if (result.getStatusCode() == HttpStatus.OK) {
                log.debug("Successfully confirmed coupon purchase. couponId: {}",
                        couponPaymentCaptureRequestDto.getCouponId());
                return;
            }
            throw new KupiOrderPaymentException("Confirming coupon purchase was failed from core service.");
        } catch (HttpClientErrorException e) {
            log.error("Confirming coupon purchase was failed from core service. statusCode: {}, body: {}",
                    e.getRawStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                handleCoreServiceBadResponse(e);
            }
            throw new KupiOrderPaymentException("Confirming coupon purchase was failed from core service", e);
        }
    }

    /**
     * This method returns headers for core service urls.
     *
     * @param authToken authToken
     * @param userId    userId
     * @param timeZone  timeZone
     * @return http headers.
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
     * This method will return SubscriptionPlanListResponseDto
     *
     * @param authToken authToken
     * @param userId    userId
     * @param timeZone  timeZone
     * @return SubscriptionPlanList
     */
    public SubscriptionPlanListResponseDto getSubscriptionPlanCount(String authToken, String userId, String timeZone) {
        HttpHeaders headers = getAuthHeaders(authToken, userId, timeZone);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            var responseTypeReference = new ParameterizedTypeReference<CoreResponseWrapper<SubscriptionPlanListResponseDto>>() {
            };
            log.debug("Calling core service to subscription plan list. url: {},", subscriptionPlanListUrl);
            ResponseEntity<CoreResponseWrapper<SubscriptionPlanListResponseDto>> result =
                    restTemplate.exchange(subscriptionPlanListUrl, HttpMethod.GET, entity, responseTypeReference);
            var response = Objects.requireNonNull(result.getBody());
            log.debug("Get subscription plan list api response from core: {}", response.toLogJson());
            if (result.getStatusCode() == HttpStatus.OK) {
                return response.getData();
            }
            throw new KupiOrderPaymentException("Subscription plan list was failed from core service.");
        } catch (HttpClientErrorException e) {
            throw new KupiOrderPaymentException("Getting subscription plan list was failed" + e, e);
        }
    }
}
