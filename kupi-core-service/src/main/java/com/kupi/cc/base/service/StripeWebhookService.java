package com.kupi.cc.base.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.kupi.cc.base.domain.request.UserSubscriptionRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class StripeWebhookService {

    private final UserSubscriptionService userSubscriptionService;
    private final PaymentHistoryService paymentHistoryService;
    private final String endpointSecret;

    public StripeWebhookService(UserSubscriptionService userSubscriptionService,
                               PaymentHistoryService paymentHistoryService,
                               @Value("${stripe.webhook.secret}") String endpointSecret) {
        this.userSubscriptionService = userSubscriptionService;
        this.paymentHistoryService = paymentHistoryService;
        this.endpointSecret = endpointSecret;
    }

    public void processStripeEvent(String payload, String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature");
            throw new RuntimeException("Invalid Stripe signature", e);
        }

        switch (event.getType()) {
            case "checkout.session.completed":
                log.info("Payment completed event received.");
                handleCheckoutCompleted(event);
                break;

            case "payment_intent.payment_failed":
                log.warn("Payment failed event received.");
                handlePaymentFailed(event);
                break;

            default:
                log.info("Unhandled Stripe event type: {}", event.getType());
        }
    }

    private void handleCheckoutCompleted(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Session session = null;

        if (deserializer.getObject().isPresent()) {
            session = (Session) deserializer.getObject().get();
        }

        if (session == null) {
            log.error("Failed to deserialize checkout session.");
            return;
        }

        Map<String, String> metadata = session.getMetadata();
        String userId = metadata.get("userId");
        String planId = metadata.get("subscriptionId");
        String sessionId = session.getId();

        log.info("Stripe Checkout completed. Session ID: {}, userId: {}, planId: {}", sessionId, userId, planId);

        if (userId == null || planId == null) {
            log.error("Missing userId or planId in session metadata. Session ID: {}", sessionId);
            return;
        }

        double amount = session.getAmountTotal() != null ? session.getAmountTotal() / 100.0 : 0.0;

        try {
            UserSubscriptionRequestDto userSubscriptionRequestDto = new UserSubscriptionRequestDto();
            userSubscriptionRequestDto.setUserId(userId);
            userSubscriptionRequestDto.setSubscriptionPlanId(planId);
            userSubscriptionService.createUserSubscription(userSubscriptionRequestDto);

            // Save successful payment history
            paymentHistoryService.saveSuccessfulPayment(userId, planId, amount, sessionId);

            log.info("User subscription successfully created for userId: {}", userId);
        } catch (Exception e) {
            // Save failed payment history
            paymentHistoryService.saveFailedPayment(userId, planId, amount, sessionId, e.getMessage());

            log.error("Failed to create user subscription for userId: {} due to: {}", userId, e.getMessage(), e);
        }
    }

    private void handlePaymentFailed(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        PaymentIntent paymentIntent = null;

        if (deserializer.getObject().isPresent()) {
            paymentIntent = (PaymentIntent) deserializer.getObject().get();
        }

        if (paymentIntent == null) {
            log.error("Failed to deserialize payment intent.");
            return;
        }

        Map<String, String> metadata = paymentIntent.getMetadata();
        if (metadata == null || metadata.isEmpty()) {
            log.warn("No metadata found in payment intent: {}", paymentIntent.getId());
            return;
        }

        String userId = metadata.get("userId");
        String planId = metadata.get("subscriptionId");
        String paymentIntentId = paymentIntent.getId();

        log.warn("Payment failed for userId: {}, planId: {}, paymentIntentId: {}", userId, planId, paymentIntentId);

        if (userId != null && planId != null) {
            double amount = paymentIntent.getAmount() != null ? paymentIntent.getAmount() / 100.0 : 0.0;
            String failureReason = paymentIntent.getLastPaymentError() != null ?
                    paymentIntent.getLastPaymentError().getMessage() : "Unknown error";

            paymentHistoryService.saveFailedPayment(userId, planId, amount, paymentIntentId, failureReason);
        }
    }

}
