package com.kupi.cc.base.service;

import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.checkout.Session;
import com.stripe.param.*;
import com.stripe.param.checkout.SessionCreateParams;
import com.kupi.cc.base.domain.entity.UserSubscriptionPlan;
import com.kupi.cc.base.domain.request.CheckoutRequestDto;
import com.kupi.cc.base.domain.response.StripeCheckoutSessionResponseDto;
import com.kupi.cc.base.domain.response.StripeSessionResponseDto;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.exception.StripeServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StripeService {
    @Value("${stripe.successUrl}")
    private String successUrl;

    @Value("${stripe.errorUrl}")
    private String errorUrl;

    private final static String CURRENCY = "usd";

    private static final SessionCreateParams.Mode MODE = SessionCreateParams.Mode.PAYMENT;

    public StripeCheckoutSessionResponseDto createCheckoutSession(CheckoutRequestDto request,
                                                                  UserSubscriptionPlan userSubscriptionPlan) {

        try {
            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setPrice(userSubscriptionPlan.getStripePriceId())
                    .setQuantity(1L)
                    .build();

            Map<String, String> metadata = new HashMap<>();
            metadata.put("userId", request.getUserId());
            metadata.put("subscriptionId", request.getSubscriptionId());
            CustomerCreateParams customerParams = CustomerCreateParams.builder()
                    .setEmail(request.getEmail())
                    .build();

            Customer customer = Customer.create(customerParams);
            String customerId = customer.getId();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(MODE)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(errorUrl)
                    .addLineItem(lineItem)
                    .setCustomer(customerId)
                    .putAllMetadata(metadata)
                    .build();

            Session session = Session.create(params);

            return new StripeCheckoutSessionResponseDto(session);
        } catch (StripeException e) {
            log.error("StripeException occurred while creating Checkout Session for priceId: {}, userId: {}",
                    request.getSubscriptionId(), request.getUserId(), e);
            throw new StripeServiceException("Creating Stripe Checkout Session failed. " + e);
        } catch (Exception e) {
            log.error("Unexpected error occurred during Stripe Checkout Session creation", e);
            throw new KupiCoreException("Unexpected error during Stripe session creation.", e);
        }
    }

    public String createOneTimeProduct(String name, BigDecimal price, String imageUrl) {
        try {
            ProductCreateParams.Builder productParamsBuilder = ProductCreateParams.builder()
                    .setName(name);
            if (imageUrl != null && !imageUrl.isBlank()) {
                productParamsBuilder.addImage(imageUrl);
            }
            Product product = Product.create(productParamsBuilder.build());
            PriceCreateParams priceParams = PriceCreateParams.builder()
                    .setUnitAmount(price.multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency(CURRENCY)
                    .setProduct(product.getId())
                    .build();
            Price createdPrice = Price.create(priceParams);
            return createdPrice.getId();
        } catch (Exception e) {
            log.error("Failed to create one time Stripe product or price", e);
            throw new RuntimeException("Stripe one time creation failed", e);
        }
    }

    public String createOneTimePrice(String productId, BigDecimal price) {
        try {
            PriceCreateParams priceParams = PriceCreateParams.builder()
                    .setUnitAmount(price.multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency("usd")
                    .setProduct(productId)
                    .build();
            return Price.create(priceParams).getId();
        } catch (Exception e) {
            log.error("Failed to create new Stripe price", e);
            throw new RuntimeException("Stripe price creation failed", e);
        }
    }

    public void updateProduct(String priceId, String newName, String newImageUrl) {
        try {
            Price price = Price.retrieve(priceId);
            String productId = price.getProduct();
            Map<String, Object> params = new HashMap<>();
            params.put("name", newName);
            if (newImageUrl != null && !newImageUrl.isBlank()) {
                params.put("images", List.of(newImageUrl));
            }
            Product product = Product.retrieve(productId);
            product.update(params);
        } catch (StripeException e) {
            log.error("Failed to update product", e);
            throw new StripeServiceException("Stripe product update failed." + e);
        }
    }

    public StripeSessionResponseDto getSessionById(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            String subscriptionId = session.getMetadata().get("subscriptionId");

            return new StripeSessionResponseDto(
                    session.getId(),
                    session.getCustomerDetails() != null ? session.getCustomerDetails().getEmail() : null,
                    session.getAmountTotal(),
                    session.getStatus(),
                    subscriptionId
            );
        } catch (InvalidRequestException e) {
            log.warn("Stripe session not found or invalid: {}", sessionId, e);
            throw new StripeServiceException("Invalid or non-existent Stripe session ID: " + sessionId + e);
        } catch (StripeException e) {
            log.error("Failed to retrieve Stripe session with ID: {}", sessionId, e);
            throw new StripeServiceException("Failed to retrieve Stripe session" + e);
        }
    }

    public void deleteProduct(String priceId) {
        try {
            Price price = Price.retrieve(priceId);
            PriceUpdateParams priceParams = PriceUpdateParams.builder()
                    .setActive(false)
                    .build();
            Price updatedPrice = price.update(priceParams);
            log.info("Deactivated Stripe price: {}", updatedPrice.getId());

            String productId = updatedPrice.getProduct();
            Product product = Product.retrieve(productId);
            try {
                product.delete();
                log.info("Deleted Stripe product: {}", productId);
            } catch (InvalidRequestException deleteErr) {
                log.warn("Product delete failed (likely has prices/usage). Archiving instead. Product: {}", productId);
                ProductUpdateParams archiveParams = ProductUpdateParams.builder()
                        .setActive(false)
                        .build();
                Product archived = product.update(archiveParams);
                log.info("Archived Stripe product: {} (active={})", archived.getId(), archived.getActive());
            }
        } catch (InvalidRequestException e) {
            log.warn("Invalid price ID or related product not found for priceId={}", priceId, e);
            throw new StripeServiceException("Invalid price ID or product not found: " + priceId, e);
        } catch (StripeException e) {
            log.error("Stripe error while deactivating price or deleting/archiving product for priceId={}", priceId, e);
            throw new StripeServiceException("Failed to process Stripe product deletion.", e);
        }
    }

}
