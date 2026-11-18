package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.PaymentBroker;
import com.kupi.cc.base.domain.pojo.PaymentOrderDto;
import com.kupi.cc.base.domain.request.CouponPaymentCaptureRequestDto;
import com.kupi.cc.base.domain.request.CouponPaymentRequestDto;
import com.kupi.cc.base.domain.request.PaymentCaptureRequestDto;
import com.kupi.cc.base.domain.request.SubscriptionPaymentRequestDto;
import com.kupi.cc.base.domain.response.BasicCouponResponseDto;
import com.kupi.cc.base.domain.response.SubscriptionValidationResponseDto;
import com.kupi.cc.base.enums.CouponPurchaseStatus;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.PaymentBrokerName;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.repository.CachedRepository;
import com.kupi.cc.base.repository.PaymentRepository;
import com.kupi.cc.base.service.paymentgateway.PaymentServiceProvider;
import com.kupi.cc.base.service.paymentgateway.PaypalServiceProvider;
import com.kupi.cc.base.service.paymentgateway.StripeServiceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.kupi.cc.base.enums.ErrorResponseStatusType.*;

@Slf4j
@Service
public class PaymentService {
    private static final String SUBSCRIPTION_PLAN_ID_SUFFIX = "##subscription_plan_id##";

    @Autowired
    private CachedRepository cachedRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaypalServiceProvider paypalServiceProvider;

    @Autowired
    private StripeServiceProvider stripeServiceProvider;

    @Autowired
    private PaymentBrokerService paymentBrokerService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private CoreService coreService;

    @Value("${redirect-uri.subscription.capture}")
    private String captureRedirectUrl;

    @Value("${redirect-uri.subscription.cancel}")
    private String cancelRedirectUrl;

    @Value("${redirect-uri.coupon.capture}")
    private String captureCouponPaymentUrl;

    @Value("${redirect-uri.coupon.cancel}")
    private String cancelCouponPaymentUrl;

    /**
     * (implementation of strategy design pattern)
     * select corresponding PSP service
     */
    private PaymentServiceProvider derivePaymentServiceProvider(PaymentBrokerName paymentBroker) {
        PaymentServiceProvider serviceProvider;
        switch (paymentBroker) {
            case PAYPAL:
                serviceProvider = paypalServiceProvider;
                break;
            case STRIPE:
                serviceProvider = stripeServiceProvider;
                break;
            default:
                log.error("Provided payment broker name: {} is not supported by the system yet.", paymentBroker);
                throw new KupiOrderPaymentException(UNSUPPORTED_PAYMENT_BROKER);
        }
        if (serviceProvider == null) {
            log.error("Error with instantiating the payment service provider service bean.");
            throw new KupiOrderPaymentException();
        }
        return serviceProvider;
    }

    /**
     * Check request dto has provided a valid payment broker id
     * validate subscription request with core service
     * create payment order in PSP
     *
     * @param paymentRequestDto subscription payment request DTO
     */
    public SubscriptionValidationResponseDto makeSubscriptionPaymentRequest(
            SubscriptionPaymentRequestDto paymentRequestDto, String userId, String authToken, String timeZone) {
        var subscriptionValidationRequestDto = paymentRequestDto.getSubscriptionValidationRequestDto();

        var paymentBroker = paymentBrokerService.getPaymentBrokerById(paymentRequestDto.getPaymentBrokerId());
        var validationResponseDto = coreService.validateSubscription(
                paymentRequestDto.getSubscriptionValidationRequestDto(), userId, authToken, timeZone);

        if (!validationResponseDto.isFree()) {
            assert isValidRedirectUri(captureRedirectUrl) && isValidRedirectUri(cancelRedirectUrl);
            var updatedCancelRedirectUrl = cancelRedirectUrl.replace(
                    SUBSCRIPTION_PLAN_ID_SUFFIX, validationResponseDto.getSubscriptionPlanId());

            var subscription = subscriptionService.buildSubscriptionObject(
                    validationResponseDto, subscriptionValidationRequestDto, userId);

            var paymentServiceProvider = derivePaymentServiceProvider(paymentBroker.getName());

            var paymentOrderDto = paymentServiceProvider.createOrder(
                    validationResponseDto.getAmount().getAmount(),
                    paymentBroker.getCurrencyType(),
                    captureRedirectUrl, updatedCancelRedirectUrl);

            paymentOrderDto.setPaymentBrokerId(paymentBroker.getId());
            paymentOrderDto.setPayableId(subscription.getId());
            cachePaymentOrder(paymentOrderDto);

            subscription.updateWithPaymentRequest();
            subscriptionService.saveTemporarySubscription(subscription, userId);

            validationResponseDto.setId(subscription.getId());
            validationResponseDto.setPaymentUrl(paymentOrderDto.getAuthorizationRedirectUrl());
        }
        return validationResponseDto;
    }

    /**
     * This method calls core service & get a coupon code and do the payment using payment broker.
     *
     * @param couponPaymentRequestDto couponPaymentRequestDto
     * @param userId                  userId
     * @param authToken               authToken
     * @param timeZone                timeZone
     * @return basicCouponResponseDto
     */
    public BasicCouponResponseDto makeCouponPaymentRequest(CouponPaymentRequestDto couponPaymentRequestDto,
                                                           String userId, String authToken, String timeZone) {
        PaymentBroker paymentBroker =
                paymentBrokerService.getPaymentBrokerById(couponPaymentRequestDto.getPaymentBrokerId());
        BasicCouponResponseDto basicCouponResponseDto =
                coreService.validateAndGetCouponCode(couponPaymentRequestDto.getDealId(), authToken, userId, timeZone);
        try {
            validateCouponResponse(couponPaymentRequestDto, basicCouponResponseDto);
            PaymentServiceProvider paymentServiceProvider = derivePaymentServiceProvider(paymentBroker.getName());

            var paymentOrderDto = paymentServiceProvider.createOrder(
                    basicCouponResponseDto.getAmount(),
                    paymentBroker.getCurrencyType(),
                    captureCouponPaymentUrl, cancelCouponPaymentUrl);

            paymentOrderDto.setPaymentBrokerId(paymentBroker.getId());
            paymentOrderDto.setPayableId(basicCouponResponseDto.getId());
            cachePaymentOrder(paymentOrderDto);
            basicCouponResponseDto.setPaymentUrl(paymentOrderDto.getAuthorizationRedirectUrl());
            return basicCouponResponseDto;
        } catch (Exception e) {
            log.error("Coupon payment failed after getting coupon code. Calling core service to rollback." +
                    " couponId: {}", basicCouponResponseDto.getId());
            CouponPaymentCaptureRequestDto couponPaymentCaptureRequestDto =
                    new CouponPaymentCaptureRequestDto(basicCouponResponseDto.getId(), basicCouponResponseDto.getDealId());
            coreService.confirmCouponPurchase(couponPaymentCaptureRequestDto, authToken, userId,
                    timeZone, CouponPurchaseStatus.NOT_PURCHASED);
            throw new KupiOrderPaymentException("Coupon payment failed after getting coupon code. couponId: " +
                    basicCouponResponseDto.getId(), e);
        }
    }

    /**
     * This method is used to validate coupon response which is fetched from core service.
     *
     * @param couponPaymentRequestDto couponPaymentRequestDto
     * @param basicCouponResponseDto  basicCouponResponseDto
     */
    private void validateCouponResponse(CouponPaymentRequestDto couponPaymentRequestDto,
                                        BasicCouponResponseDto basicCouponResponseDto) {
        //todo:remove the comments from validation.
//        if (!couponPaymentRequestDto.getAmount().equals(basicCouponResponseDto.getAmount()))
//            throw new KupiOrderPaymentException("Request amount & coupon amount doesn't match.");
        if (basicCouponResponseDto.getId() == null || basicCouponResponseDto.getDealId() == null ||
                basicCouponResponseDto.getCouponCode() == null)
            throw new KupiOrderPaymentException("Invalid coupon.");
    }

    private boolean isValidRedirectUri(String redirectUri) {
        return redirectUri != null && !redirectUri.isBlank();
    }

    private void cachePaymentOrder(PaymentOrderDto paymentOrderDto) {
        try {
            cachedRepository.save(paymentOrderDto);
        } catch (RedisConnectionFailureException e) {
            log.error("Cannot save payment order details to redis cached repository.", e);
            throw new KupiOrderPaymentException();
        }
    }

    /**
     * complete payment capture, for pre authorized payment request
     *
     * @param paymentCaptureRequestDto payment capture request
     */
    public PaymentOrderDto completePayment(PaymentCaptureRequestDto paymentCaptureRequestDto) {
        String paymentOrderId = paymentCaptureRequestDto.getPaymentId();
        var paymentOrderDto = getCreatedOrderFromCache(paymentOrderId);

        if (!PaymentOrderDto.PaymentOrderState.ORDER_CREATED.equals(paymentOrderDto.getState())) {
            log.error("Payment is not in ORDER_CREATED state. Might be an anomaly or order is already captured.");
            throw new KupiOrderPaymentException(ALREADY_CAPTURED_ORDER);
        }

        var paymentBroker = paymentBrokerService.getPaymentBrokerById(paymentOrderDto.getPaymentBrokerId());
        var paymentServiceProvider = derivePaymentServiceProvider(paymentBroker.getName());

        paymentServiceProvider.captureOrder(paymentOrderId, paymentCaptureRequestDto.getPayerId());
        removeCreatedOrderFromCache(paymentOrderId);
        return paymentOrderDto;
    }

    private void removeCreatedOrderFromCache(String orderId) {
        try {
            var cacheDeleted = cachedRepository.deleteById(orderId);
            if (!cacheDeleted) {
                log.error("Delete key: {} from redis cached repository failed.", orderId);
                throw new KupiOrderPaymentException();
            }
        } catch (RedisConnectionFailureException e) {
            log.error("Cannot correct to redis cached repository for order-id: " + orderId, e);
            throw new KupiOrderPaymentException();
        }
    }

    private PaymentOrderDto getCreatedOrderFromCache(String orderId) {
        try {
            return Optional.ofNullable(cachedRepository.findById(orderId)).orElseThrow(() -> {
                log.error("Cannot retrieve payment order details from cache repository for order-id: {}", orderId);
                throw new KupiOrderPaymentException(INCORRECT_OR_EXPIRED_PAYMENT_TOKEN);
            });
        } catch (RedisConnectionFailureException e) {
            log.error("Cannot connect to redis cached repository for order-id: " + orderId, e);
            throw new KupiOrderPaymentException();
        }
    }
}
