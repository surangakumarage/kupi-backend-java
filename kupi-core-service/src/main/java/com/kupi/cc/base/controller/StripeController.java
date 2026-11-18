package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.UserSubscriptionPlan;
import com.kupi.cc.base.domain.request.CheckoutRequestDto;
import com.kupi.cc.base.domain.request.UserSubscriptionRequestDto;
import com.kupi.cc.base.domain.response.StripeCheckoutSessionResponseDto;
import com.kupi.cc.base.domain.response.StripeSessionResponseDto;
import com.kupi.cc.base.domain.response.StripeSuccessRedirectionResponseDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.exception.InvalidUserSubscriptionPlanException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.exception.StripeServiceException;
import com.kupi.cc.base.exception.StripeSessionNotFoundException;
import com.kupi.cc.base.service.PaymentHistoryService;
import com.kupi.cc.base.service.StripeService;
import com.kupi.cc.base.service.StripeWebhookService;
import com.kupi.cc.base.service.UserSubscriptionPlanService;
import com.kupi.cc.base.service.UserSubscriptionService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequestMapping("api/v1/stripe")
public class StripeController extends Controller {

    private final StripeService stripeService;
    private final StripeWebhookService stripeWebhookService;
    private final UserSubscriptionPlanService userSubscriptionPlanService;
    private final UserSubscriptionService userSubscriptionService;
    private final PaymentHistoryService paymentHistoryService;

    private final String freePlanName;


    public StripeController(Translator translator, StripeService stripeService,
                            StripeWebhookService stripeWebhookService,
                            UserSubscriptionPlanService userSubscriptionPlanService,
                            UserSubscriptionService userSubscriptionService,
                            PaymentHistoryService paymentHistoryService,
                            @Value("${userSubscription.free-plan-name}")
                            String freePlanName) {
        super(translator);
        this.stripeService = stripeService;
        this.stripeWebhookService = stripeWebhookService;
        this.userSubscriptionPlanService = userSubscriptionPlanService;
        this.userSubscriptionService = userSubscriptionService;
        this.paymentHistoryService = paymentHistoryService;
        this.freePlanName = freePlanName;
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<ResponseWrapper> createCheckoutSession(@RequestBody CheckoutRequestDto checkoutRequestDto) {

        try {
            if (!checkoutRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }

            UserSubscriptionPlan userSubscriptionPlan = userSubscriptionPlanService
                    .getUserSubscriptionPlanById(checkoutRequestDto.getSubscriptionId());

            if (freePlanName.equalsIgnoreCase(userSubscriptionPlan.getName())) {
                UserSubscriptionRequestDto subscriptionRequestDto = new UserSubscriptionRequestDto();
                subscriptionRequestDto.setUserId(checkoutRequestDto.getUserId());
                subscriptionRequestDto.setSubscriptionPlanId(userSubscriptionPlan.getId());

                userSubscriptionService.createUserSubscription(subscriptionRequestDto);

                // Save successful payment history for free plan (amount = 0.0)
                paymentHistoryService.saveSuccessfulPayment(checkoutRequestDto.getUserId(),
                                                           userSubscriptionPlan.getId(),
                                                           0.0,
                                                           "free-plan-" + System.currentTimeMillis());

                log.info("User {} successfully subscribed to free plan without payment", checkoutRequestDto.getUserId());

                return getSuccessResponse(
                        new StripeCheckoutSessionResponseDto(true),
                        SuccessResponseStatusType.FREE_PLAN_ACTIVATED
                );
            }

            StripeCheckoutSessionResponseDto checkoutSessionResponseDto = stripeService
                    .createCheckoutSession(checkoutRequestDto, userSubscriptionPlan);
            return getSuccessResponse(checkoutSessionResponseDto,
                    SuccessResponseStatusType.SUCCESSFULLY_RETURNED_STRIPE_SESSION_URL);
        } catch (StripeServiceException e) {
            log.error("Stripe error: Failed to create session for userId: {}",
                    checkoutRequestDto.getUserId(), e);
            return getErrorResponse(ErrorResponseStatusType.STRIPE_SESSION_CREATION_FAILED);

        } catch (InvalidUserSubscriptionPlanException e) {
            log.error("Invalid plan ID: {}", checkoutRequestDto.getSubscriptionId(), e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN_ID);
        } catch (KupiCoreException e) {
            log.error("Failed to create a stripe checkout session for user id: {}",
                    checkoutRequestDto.getUserId(), e);
            return getInternalServerError();
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) {
        String payload;
        String sigHeader = request.getHeader("Stripe-Signature");
        try (BufferedReader reader = request.getReader()) {
            payload = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to read request body");
        }
        try {
            stripeWebhookService.processStripeEvent(payload, sigHeader);
            return ResponseEntity.ok("Webhook received");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Webhook error");
        }
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ResponseWrapper> getSession(@PathVariable String sessionId) {
        try {
            StripeSessionResponseDto sessionById = stripeService.getSessionById(sessionId);
            UserSubscriptionPlan userSubscriptionPlan = userSubscriptionPlanService
                    .getUserSubscriptionPlanById(sessionById.getSubscriptionId());
            StripeSuccessRedirectionResponseDto stripeSuccessRedirectionResponseDto =
                    new StripeSuccessRedirectionResponseDto(sessionById, userSubscriptionPlan.getCouponLimit());
            return getSuccessResponse(stripeSuccessRedirectionResponseDto,
                    SuccessResponseStatusType.SUCCESSFULLY_RETURNED_STRIPE_SESSION_DETAILS);
        } catch (InvalidUserSubscriptionPlanException e) {
            log.error("Invalid subscription plan ID", e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN_ID);
        } catch (StripeSessionNotFoundException e) {
            log.error("Invalid session ID: {}", sessionId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN_ID);
        }
    }
}
