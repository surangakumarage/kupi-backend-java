package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.PaymentBroker;
import com.kupi.cc.base.domain.entity.Subscription;
import com.kupi.cc.base.domain.pojo.PaymentOrderDto;
import com.kupi.cc.base.domain.request.*;
import com.kupi.cc.base.domain.response.BasicCouponResponseDto;
import com.kupi.cc.base.domain.response.SubscriptionAmountResponseDto;
import com.kupi.cc.base.domain.response.SubscriptionValidationResponseDto;
import com.kupi.cc.base.enums.CouponPurchaseStatus;
import com.kupi.cc.base.enums.CurrencyType;
import com.kupi.cc.base.enums.PaymentBrokerName;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.repository.CachedRepository;
import com.kupi.cc.base.service.paymentgateway.PaypalServiceProvider;
import com.kupi.cc.base.service.paymentgateway.StripeServiceProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * This class tests {@link PaymentService} class
 */
class PaymentServiceTest {

    private static final String PAYMENT_BROKER_ID = "PAYMENT_BROKER_ID";
    private static final String COUPON_CODE = "COUPON_CODE";
    private static final String USER_ID = "uid-123456789";
    private static final String DEAL_ID = "did-123456789";
    private static final String VALIDATION_ID = "VALIDATION_ID";
    private static final String SUBSCRIPTION_ID = "SUBSCRIPTION_ID";
    private static final String ORDER_ID = "ORDER_ID";
    private static final String COUPON_ID = "COUPON_ID";
    private static final String PAYMENT_ID = "PAYMENT_ID";
    private static final String PAYER_ID = "PAYER_ID";
    private static final String APPROVAL_LINK = "APPROVAL_LINK";
    private static final String TOKEN = "TOKEN";
    private static final String ERROR = "ERROR";
    private static final String TIME_ZONE = "Asia/Colombo";
    @Mock
    private CachedRepository cachedRepository;
    @Mock
    private PaypalServiceProvider paypalServiceProvider;
    @Mock
    private StripeServiceProvider stripeServiceProvider;
    @Mock
    private PaymentBrokerService paymentBrokerService;
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private CoreService coreService;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        paymentService = new PaymentService();
        ReflectionTestUtils.setField(paymentService, "cachedRepository", cachedRepository);
        ReflectionTestUtils.setField(paymentService, "paypalServiceProvider", paypalServiceProvider);
        ReflectionTestUtils.setField(paymentService, "stripeServiceProvider", stripeServiceProvider);
        ReflectionTestUtils.setField(paymentService, "paymentBrokerService", paymentBrokerService);
        ReflectionTestUtils.setField(paymentService, "subscriptionService", subscriptionService);
        ReflectionTestUtils.setField(paymentService, "coreService", coreService);
        String captureRedirectUrl = "http://localhost:3000/paymentcomplete";
        ReflectionTestUtils.setField(paymentService, "captureRedirectUrl", captureRedirectUrl);
        String cancelRedirectUrl = "http://localhost:3000/subscriptions/details/##subscription_plan_id##";
        ReflectionTestUtils.setField(paymentService, "cancelRedirectUrl", cancelRedirectUrl);
        String captureCouponPaymentUrl = "https://coupon.complete";
        ReflectionTestUtils.setField(paymentService, "captureCouponPaymentUrl", captureCouponPaymentUrl);
        String cancelCouponPaymentUrl = "https://coupon.cancel";
        ReflectionTestUtils.setField(paymentService, "cancelCouponPaymentUrl", cancelCouponPaymentUrl);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks(this).close();
    }

    /**
     * Start of tests for Make subscription payment request
     */
    @Test
    void Should_ReturnSubscriptionValidationResponseDto_When_MakingSubscriptionPaymentRequestForFreeSubscriptionIsSuccessful() {
        when(paymentBrokerService.getPaymentBrokerById(anyString())).thenReturn(getSamplePaymentBroker());
        when(coreService.validateSubscription(any(SubscriptionValidationRequestDto.class), anyString(), anyString(),
                anyString())).thenReturn(getSampleSubscriptionValidationResponseDto());
        assertEquals(VALIDATION_ID, paymentService
                .makeSubscriptionPaymentRequest(getSampleSubscriptionPaymentRequestDto(), USER_ID, TOKEN, TIME_ZONE)
                .getId());
    }

    @Test
    void Should_ReturnSubscriptionValidationResponseDto_When_MakingSubscriptionPaymentRequestForPaidSubscriptionIsSuccessful() {
        SubscriptionAmountResponseDto subscriptionAmountResponseDto = new SubscriptionAmountResponseDto();
        subscriptionAmountResponseDto.setAmount(new BigDecimal(100));
        SubscriptionValidationResponseDto subscriptionValidationResponseDto = getSampleSubscriptionValidationResponseDto();
        subscriptionValidationResponseDto.setFree(false);
        subscriptionValidationResponseDto.setAmount(subscriptionAmountResponseDto);

        when(paymentBrokerService.getPaymentBrokerById(anyString())).thenReturn(getSamplePaymentBroker());
        when(coreService.validateSubscription(any(SubscriptionValidationRequestDto.class), anyString(), anyString(),
                anyString())).thenReturn(subscriptionValidationResponseDto);
        when(subscriptionService.buildSubscriptionObject(any(SubscriptionValidationResponseDto.class),
                any(SubscriptionValidationRequestDto.class), anyString())).thenReturn(getSampleSubscription());
        doNothing().when(subscriptionService).saveTemporarySubscription(any(Subscription.class), anyString());
        when(paypalServiceProvider.createOrder(any(BigDecimal.class), any(CurrencyType.class), anyString(),
                anyString())).thenReturn(new PaymentOrderDto(ORDER_ID, APPROVAL_LINK));

        paymentService.makeSubscriptionPaymentRequest(getSampleSubscriptionPaymentRequestDto(), USER_ID, TOKEN,
                TIME_ZONE);

        verify(subscriptionService, times(1)).saveTemporarySubscription(any(Subscription.class),
                anyString());
        verify(cachedRepository, times(1)).save(any(PaymentOrderDto.class));
        assertEquals(SUBSCRIPTION_ID, paymentService
                .makeSubscriptionPaymentRequest(getSampleSubscriptionPaymentRequestDto(), USER_ID, TOKEN, TIME_ZONE)
                .getId());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_MakingSubscriptionPaymentRequestForUnsupportedPaymentBroker() {
        SubscriptionPaymentRequestDto subscriptionPaymentRequestDto = getSampleSubscriptionPaymentRequestDto();
        PaymentBroker paymentBroker = getSamplePaymentBroker();
        paymentBroker.setName(PaymentBrokerName.APPLEPAY);
        SubscriptionValidationResponseDto subscriptionValidationResponseDto = getSampleSubscriptionValidationResponseDto();
        subscriptionValidationResponseDto.setFree(false);

        when(paymentBrokerService.getPaymentBrokerById(anyString())).thenReturn(paymentBroker);
        when(coreService.validateSubscription(any(SubscriptionValidationRequestDto.class), anyString(), anyString(),
                anyString())).thenReturn(subscriptionValidationResponseDto);
        when(subscriptionService.buildSubscriptionObject(any(SubscriptionValidationResponseDto.class),
                any(SubscriptionValidationRequestDto.class), anyString())).thenReturn(getSampleSubscription());
        doNothing().when(subscriptionService).saveTemporarySubscription(any(Subscription.class), anyString());

        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> paymentService
                .makeSubscriptionPaymentRequest(subscriptionPaymentRequestDto, USER_ID, TOKEN, TIME_ZONE));
        assertEquals("Unsupported payment broker name found.", exception.getErrorResponseType().getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_MakingSubscriptionPaymentRequestForFailedToAccessRedisCachedRepository() {
        SubscriptionPaymentRequestDto subscriptionPaymentRequestDto = getSampleSubscriptionPaymentRequestDto();
        SubscriptionAmountResponseDto subscriptionAmountResponseDto = new SubscriptionAmountResponseDto();
        subscriptionAmountResponseDto.setAmount(new BigDecimal(100));
        SubscriptionValidationResponseDto subscriptionValidationResponseDto = getSampleSubscriptionValidationResponseDto();
        subscriptionValidationResponseDto.setFree(false);
        subscriptionValidationResponseDto.setAmount(subscriptionAmountResponseDto);

        when(paymentBrokerService.getPaymentBrokerById(anyString())).thenReturn(getSamplePaymentBroker());
        when(coreService.validateSubscription(any(SubscriptionValidationRequestDto.class), anyString(), anyString(),
                anyString())).thenReturn(subscriptionValidationResponseDto);
        when(subscriptionService.buildSubscriptionObject(any(SubscriptionValidationResponseDto.class),
                any(SubscriptionValidationRequestDto.class), anyString())).thenReturn(getSampleSubscription());
        doNothing().when(subscriptionService).saveTemporarySubscription(any(Subscription.class), anyString());
        when(paypalServiceProvider.createOrder(any(BigDecimal.class), any(CurrencyType.class), anyString(),
                anyString())).thenReturn(new PaymentOrderDto(ORDER_ID, APPROVAL_LINK));
        doThrow(new RedisConnectionFailureException(ERROR)).when(cachedRepository).save(any(PaymentOrderDto.class));

        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> paymentService
                .makeSubscriptionPaymentRequest(subscriptionPaymentRequestDto, USER_ID, TOKEN, TIME_ZONE));
        assertEquals("Internal server error.", exception.getErrorResponseType().getMessage());
    }

    /**
     * Start of tests for Make coupon payment request
     */
    @Test
    void Should_ReturnBasicCouponResponseDto_When_MakingCouponPaymentRequestIsSuccessful() {
        PaymentBroker paymentBroker = getSamplePaymentBroker();
        paymentBroker.setName(PaymentBrokerName.STRIPE);
        when(paymentBrokerService.getPaymentBrokerById(anyString())).thenReturn(paymentBroker);
        when(coreService.validateAndGetCouponCode(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(getSampleBasicCouponResponseDto());
        when(stripeServiceProvider.createOrder(any(BigDecimal.class), any(CurrencyType.class), anyString(),
                anyString())).thenReturn(new PaymentOrderDto(ORDER_ID, APPROVAL_LINK));

        paymentService.makeCouponPaymentRequest(getSampleCouponPaymentRequestDto(), USER_ID, TOKEN, TIME_ZONE);

        verify(cachedRepository).save(any(PaymentOrderDto.class));
        assertEquals(COUPON_ID, paymentService.makeCouponPaymentRequest(getSampleCouponPaymentRequestDto(),
                USER_ID, TOKEN, TIME_ZONE).getId());
    }

    // TODO: 12/1/2022 I have commented this one, because the implementation code block relevant to this test case is
    //  currently commented
//    @Test
//    void Should_ThrowKupiOrderPaymentException_When_MakingCouponPaymentRequestForRequestAmountAndCouponAmountDontMatch() {
//        CouponPaymentRequestDto couponPaymentRequestDto = getSampleCouponPaymentRequestDto();
//        BasicCouponResponseDto basicCouponResponseDto = getSampleBasicCouponResponseDto();
//        basicCouponResponseDto.setAmount(new BigDecimal(10));
//
//        when(paymentBrokerService.getPaymentBrokerById(anyString())).thenReturn(getSamplePaymentBroker());
//        when(coreService.validateAndGetCouponCode(anyString(), anyString(), anyString(), anyString()))
//                .thenReturn(basicCouponResponseDto);
//
//        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> paymentService
//                .makeCouponPaymentRequest(couponPaymentRequestDto, USER_ID, TOKEN, TIME_ZONE));
//        assertEquals("Request amount & coupon amount doesn't match.", exception.getCause().getMessage());
//    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_MakingCouponPaymentRequestForInvalidCoupon() {
        CouponPaymentRequestDto couponPaymentRequestDto = getSampleCouponPaymentRequestDto();
        BasicCouponResponseDto basicCouponResponseDto = getSampleBasicCouponResponseDto();
        basicCouponResponseDto.setCouponCode(null);

        when(paymentBrokerService.getPaymentBrokerById(anyString())).thenReturn(getSamplePaymentBroker());
        when(coreService.validateAndGetCouponCode(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(basicCouponResponseDto);

        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                paymentService.makeCouponPaymentRequest(couponPaymentRequestDto, USER_ID, TOKEN, TIME_ZONE));
        assertEquals("Invalid coupon.", exception.getCause().getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_MakingCouponPaymentRequestForUnsupportedPaymentBroker() {
        CouponPaymentRequestDto couponPaymentRequestDto = getSampleCouponPaymentRequestDto();
        PaymentBroker paymentBroker = getSamplePaymentBroker();
        paymentBroker.setName(PaymentBrokerName.APPLEPAY);

        when(paymentBrokerService.getPaymentBrokerById(anyString())).thenReturn(paymentBroker);
        when(coreService.validateAndGetCouponCode(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(getSampleBasicCouponResponseDto());

        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> paymentService
                .makeCouponPaymentRequest(couponPaymentRequestDto, USER_ID, TOKEN, TIME_ZONE));
        verify(coreService, times(1)).confirmCouponPurchase(any(CouponPaymentCaptureRequestDto.class),
                anyString(), anyString(), anyString(), any(CouponPurchaseStatus.class));
        assertEquals("Coupon payment failed after getting coupon code. couponId: " +
                COUPON_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_MakingCouponPaymentRequestForFailedToAccessRedisCachedRepository() {
        CouponPaymentRequestDto couponPaymentRequestDto = getSampleCouponPaymentRequestDto();
        PaymentBroker paymentBroker = getSamplePaymentBroker();
        paymentBroker.setName(PaymentBrokerName.STRIPE);
        when(paymentBrokerService.getPaymentBrokerById(anyString())).thenReturn(paymentBroker);
        when(coreService.validateAndGetCouponCode(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(getSampleBasicCouponResponseDto());
        when(stripeServiceProvider.createOrder(any(BigDecimal.class), any(CurrencyType.class), anyString(),
                anyString())).thenReturn(new PaymentOrderDto(ORDER_ID, APPROVAL_LINK));
        doThrow(new RedisConnectionFailureException(ERROR)).when(cachedRepository).save(any(PaymentOrderDto.class));

        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> paymentService
                .makeCouponPaymentRequest(couponPaymentRequestDto, USER_ID, TOKEN, TIME_ZONE));
        verify(coreService, times(1)).confirmCouponPurchase(any(CouponPaymentCaptureRequestDto.class),
                anyString(), anyString(), anyString(), any(CouponPurchaseStatus.class));
        assertEquals("Coupon payment failed after getting coupon code. couponId: " +
                COUPON_ID, exception.getMessage());
    }

    /**
     * Start of tests for complete payment
     */
    @Test
    void Should_ReturnPaymentOrderDto_When_CompletingPaymentIsSuccessful() {
        when(cachedRepository.findById(anyString())).thenReturn(getSamplePaymentOrderDto());
        when(paymentBrokerService.getPaymentBrokerById(anyString())).thenReturn(getSamplePaymentBroker());
        doNothing().when(paypalServiceProvider).captureOrder(anyString(), anyString());
        when(cachedRepository.deleteById(anyString())).thenReturn(true);

        assertEquals(ORDER_ID, paymentService.completePayment(getSamplePaymentCaptureRequestDto()).getOrderId());
        verify(paypalServiceProvider, times(1)).captureOrder(anyString(), anyString());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_CompletingPaymentForCannotFindCachedOrderPaymentRequest() {
        PaymentCaptureRequestDto paymentCaptureRequestDto = getSamplePaymentCaptureRequestDto();

        when(cachedRepository.findById(anyString())).thenReturn(null);
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> paymentService
                .completePayment(paymentCaptureRequestDto));
        assertEquals("Payment token used for this request might be invalid or already used.", exception
                .getErrorResponseType().getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_CompletingPaymentForAccessingRedisCachedRepositoryFailed() {
        PaymentCaptureRequestDto paymentCaptureRequestDto = getSamplePaymentCaptureRequestDto();

        when(cachedRepository.findById(anyString())).thenThrow(new RedisConnectionFailureException(ERROR));
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> paymentService
                .completePayment(paymentCaptureRequestDto));
        assertEquals("Internal server error.", exception.getErrorResponseType().getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_CompletingPaymentForAlreadyCapturedOrder() {
        PaymentCaptureRequestDto paymentCaptureRequestDto = getSamplePaymentCaptureRequestDto();
        PaymentOrderDto paymentOrderDto = getSamplePaymentOrderDto();
        paymentOrderDto.setState(PaymentOrderDto.PaymentOrderState.CAPTURED);
        when(cachedRepository.findById(anyString())).thenReturn(paymentOrderDto);
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> paymentService
                .completePayment(paymentCaptureRequestDto));
        assertEquals("Payment for this order is already captured.", exception.getErrorResponseType().getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_CompletingPaymentForDeletingOrderIdFromRedisCachedRepositoryIsFailed() {
        PaymentCaptureRequestDto paymentCaptureRequestDto = getSamplePaymentCaptureRequestDto();

        when(cachedRepository.findById(anyString())).thenReturn(getSamplePaymentOrderDto());
        when(paymentBrokerService.getPaymentBrokerById(anyString())).thenReturn(getSamplePaymentBroker());
        doNothing().when(paypalServiceProvider).captureOrder(anyString(), anyString());
        when(cachedRepository.deleteById(anyString())).thenReturn(false);

        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> paymentService
                .completePayment(paymentCaptureRequestDto));
        assertEquals("Internal server error.", exception.getErrorResponseType().getMessage());
        verify(paypalServiceProvider, times(1)).captureOrder(anyString(), anyString());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_CompletingPaymentForConnectingRedisCachedRepositoryFailed() {
        PaymentCaptureRequestDto paymentCaptureRequestDto = getSamplePaymentCaptureRequestDto();

        when(cachedRepository.findById(anyString())).thenReturn(getSamplePaymentOrderDto());
        when(paymentBrokerService.getPaymentBrokerById(anyString())).thenReturn(getSamplePaymentBroker());
        doNothing().when(paypalServiceProvider).captureOrder(anyString(), anyString());
        when(cachedRepository.deleteById(anyString())).thenThrow(new RedisConnectionFailureException(ERROR));

        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () -> paymentService
                .completePayment(paymentCaptureRequestDto));
        assertEquals("Internal server error.", exception.getErrorResponseType().getMessage());
        verify(paypalServiceProvider, times(1)).captureOrder(anyString(), anyString());
    }

    /**
     * This method returns a sample subscription payment request dto
     *
     * @return SubscriptionPaymentRequestDto
     */
    private SubscriptionPaymentRequestDto getSampleSubscriptionPaymentRequestDto() {
        SubscriptionPaymentRequestDto subscriptionPaymentRequestDto = new SubscriptionPaymentRequestDto();
        subscriptionPaymentRequestDto.setPaymentBrokerId(PAYMENT_BROKER_ID);
        subscriptionPaymentRequestDto.setSubscriptionValidationRequestDto(getSampleSubscriptionValidationRequestDto());
        return subscriptionPaymentRequestDto;
    }

    /**
     * This method returns a sample subscription validation request dto
     *
     * @return SubscriptionValidationRequestDto
     */
    private SubscriptionValidationRequestDto getSampleSubscriptionValidationRequestDto() {
        return new SubscriptionValidationRequestDto();
    }

    /**
     * This method returns a sample payment broker
     *
     * @return PaymentBroker
     */
    private PaymentBroker getSamplePaymentBroker() {
        PaymentBroker paymentBroker = new PaymentBroker();
        paymentBroker.setId(PAYMENT_BROKER_ID);
        paymentBroker.setName(PaymentBrokerName.PAYPAL);
        paymentBroker.setCurrencyType(CurrencyType.USD);
        return paymentBroker;
    }

    /**
     * This method returns a sample subscription validation response dto
     *
     * @return SubscriptionValidationResponseDto
     */
    private SubscriptionValidationResponseDto getSampleSubscriptionValidationResponseDto() {
        SubscriptionValidationResponseDto subscriptionValidationResponseDto = new SubscriptionValidationResponseDto();
        subscriptionValidationResponseDto.setSubscriptionPlanId(SUBSCRIPTION_ID);
        subscriptionValidationResponseDto.setId(VALIDATION_ID);
        subscriptionValidationResponseDto.setFree(true);
        return subscriptionValidationResponseDto;
    }

    /**
     * This method returns a sample coupon payment request dto
     *
     * @return CouponPaymentRequestDto
     */
    private CouponPaymentRequestDto getSampleCouponPaymentRequestDto() {
        CouponPaymentRequestDto couponPaymentRequestDto = new CouponPaymentRequestDto();
        couponPaymentRequestDto.setPaymentBrokerId(PAYMENT_BROKER_ID);
        couponPaymentRequestDto.setDealId(DEAL_ID);
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
        basicCouponResponseDto.setDealId(DEAL_ID);
        basicCouponResponseDto.setAmount(new BigDecimal(100));
        basicCouponResponseDto.setCouponCode(COUPON_CODE);
        return basicCouponResponseDto;
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
        paymentOrderDto.setPaymentBrokerId(PAYMENT_BROKER_ID);
        return paymentOrderDto;
    }

    /**
     * This method returns a sample subscription
     *
     * @return Subscription
     */
    private Subscription getSampleSubscription() {
        Subscription subscription = new Subscription();
        subscription.setId(SUBSCRIPTION_ID);
        return subscription;
    }
}