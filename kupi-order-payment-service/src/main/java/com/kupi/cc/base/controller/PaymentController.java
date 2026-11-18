package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.pojo.PaymentOrderDto;
import com.kupi.cc.base.domain.request.*;
import com.kupi.cc.base.domain.response.BasicCouponResponseDto;
import com.kupi.cc.base.enums.CouponPurchaseStatus;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.service.CoreService;
import com.kupi.cc.base.service.PaymentService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * payment and refund controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController extends Controller {

    private final PaymentService paymentService;
    private final CoreService coreService;

    @Autowired
    public PaymentController(Translator translator,
                             PaymentService paymentService, CoreService coreService) {
        super(translator);
        this.paymentService = paymentService;
        this.coreService = coreService;
    }

    @PostMapping(path = "/subscription/free", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> makeSubscriptionPayment(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestBody SubscriptionValidationRequestDto subscriptionRequestDto,
            HttpServletRequest request) {

        if (isEmpty(userId)) {
            return getBadRequestError(ErrorResponseStatusType.INVALID_USER_ID);
        }
        if (!subscriptionRequestDto.isRequiredAvailable()) {
            return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
        }
        var authToken = request.getHeader(AUTH_TOKEN_HEADER);

        try {
            coreService.freeSubscription(subscriptionRequestDto, userId, authToken, timeZone);
            return getSuccessResponse(null, SuccessResponseStatusType.SUBSCRIBE_PLAN);
        } catch (KupiOrderPaymentException e) {
            log.error("Error making payment request for user-id: {}", userId, e);
            return getBadRequestError(e.getErrorResponseType());
        }
    }

    /**
     * Validates subscription request and creates payment order in PSP
     *
     * @param subscriptionPaymentRequestDto subscription payment request DTO object
     */
    @PostMapping(path = "/subscription/make", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> makeSubscriptionPayment(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestBody SubscriptionPaymentRequestDto subscriptionPaymentRequestDto,
            HttpServletRequest request) {

        if (isEmpty(userId)) {
            return getBadRequestError(ErrorResponseStatusType.INVALID_USER_ID);
        }
        if (!subscriptionPaymentRequestDto.isRequiredAvailable()) {
            return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
        }
        var authToken = request.getHeader(AUTH_TOKEN_HEADER);

        try {
            var makePaymentResponseDto = paymentService
                    .makeSubscriptionPaymentRequest(subscriptionPaymentRequestDto, userId, authToken, timeZone);
            return getSuccessResponse(makePaymentResponseDto, SuccessResponseStatusType.SUBSCRIPTION_PAYMENT_INITIATED);
        } catch (KupiOrderPaymentException e) {
            log.error("Error making payment request for user-id: {}", userId, e);
            return getBadRequestError(e.getErrorResponseType());
        }
    }

    /**
     * confirm authorized payment request, and save subscription
     *
     * @param paymentCaptureRequestDto payment capture request
     */
    @PostMapping(path = "/subscription/confirm", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> confirmPayment(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestBody PaymentCaptureRequestDto paymentCaptureRequestDto,
            HttpServletRequest request) {
        if (isEmpty(userId)) {
            return getBadRequestError(ErrorResponseStatusType.INVALID_USER_ID);
        }
        if (!paymentCaptureRequestDto.isRequiredAvailable()) {
            return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
        }
        var authToken = request.getHeader(AUTH_TOKEN_HEADER);

        try {
            PaymentOrderDto paymentOrderDto = paymentService.completePayment(paymentCaptureRequestDto);
            coreService.subscribe(userId, paymentOrderDto.getPayableId(), authToken, timeZone);
            return getSuccessResponse(null, SuccessResponseStatusType.SUBSCRIBE_PLAN);
        } catch (KupiOrderPaymentException e) {
            log.error("Error confirming payment request for user-id: {}. ", userId, e);
            return getBadRequestError(e.getErrorResponseType());
        }
    }

    /**
     * This method is used to make payment for coupons.
     *
     * @param timeZone                timeZone
     * @param userId                  userId
     * @param couponPaymentRequestDto couponPaymentRequestDto
     * @param request                 request
     * @return success/error message.
     */
    @PostMapping(path = "/coupon/make", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> makeCouponPayment(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestBody CouponPaymentRequestDto couponPaymentRequestDto,
            HttpServletRequest request) {

        if (isEmpty(userId)) {
            return getBadRequestError(ErrorResponseStatusType.INVALID_USER_ID);
        }
        if (!couponPaymentRequestDto.isRequiredAvailable()) {
            return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
        }
        String authToken = request.getHeader(AUTH_TOKEN_HEADER);
        try {
            BasicCouponResponseDto basicCouponResponseDto =
                    paymentService.makeCouponPaymentRequest(couponPaymentRequestDto, userId, authToken, timeZone);
            return getSuccessResponse(basicCouponResponseDto, SuccessResponseStatusType.COUPON_PAYMENT_INITIATED);
        } catch (KupiOrderPaymentException e) {
            log.error("Failed to make coupon payment. UserId: {}", userId, e);
            return getBadRequestError(e.getErrorResponseType());
        }
    }

    /**
     * This method is used to confirm the coupon payments.
     *
     * @param timeZone                       timeZone
     * @param userId                         userId
     * @param couponPaymentCaptureRequestDto couponPaymentCaptureRequestDto
     * @param request                        request
     * @return success/error message.
     */
    @PostMapping(path = "/coupon/confirm", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> confirmCouponPayment(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestBody CouponPaymentCaptureRequestDto couponPaymentCaptureRequestDto,
            HttpServletRequest request) {
        if (isEmpty(userId)) {
            return getBadRequestError(ErrorResponseStatusType.INVALID_USER_ID);
        }
        if (!couponPaymentCaptureRequestDto.isRequiredAvailable()) {
            return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
        }
        String authToken = request.getHeader(AUTH_TOKEN_HEADER);
        try {
            paymentService.completePayment(couponPaymentCaptureRequestDto);
            coreService.confirmCouponPurchase(couponPaymentCaptureRequestDto, authToken, userId,
                    timeZone, CouponPurchaseStatus.PURCHASED);
            return getSuccessResponse(null, SuccessResponseStatusType.COUPON_PAYMENT_CONFIRMED);
        } catch (KupiOrderPaymentException e) {
            log.error("Failed to confirm coupon payment. UserId: {}", userId, e);
            return getBadRequestError(e.getErrorResponseType());
        }
    }

    /**
     * This method is used to make refund request.
     *
     * @param timeZone         timeZone
     * @param refundRequestDto refundRequestDto
     * @return refund confirmation.
     */
    @PostMapping(path = "/refund-request", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> refundRequest(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                         @RequestBody RefundRequestDto refundRequestDto) {
        log.info("Refund capabilities are yet to be implemented.");
        return null;
    }
}
