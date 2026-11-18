package com.kupi.notification.listener;

import com.kupi.notification.dto.sse.SseEventRequest;
import com.kupi.notification.dto.sse.payload.DealEventPayload;
import com.kupi.notification.dto.sse.payload.CouponRedemptionPayload;
import com.kupi.notification.model.event.BusinessEventType;
import com.kupi.notification.service.RealTimeEventPublisher;
import com.kupi.notification.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller that listens for events from kupi-core-service
 * Converts core service events to SSE events and publishes them
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notification/events")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CoreServiceEventListener {

    private final RealTimeEventPublisher realTimeEventPublisher;

    @Autowired
    public CoreServiceEventListener(RealTimeEventPublisher realTimeEventPublisher) {
        this.realTimeEventPublisher = realTimeEventPublisher;
    }

    /**
     * Receive deal created event from core service
     * Publishes SSE event to merchant/admin users
     *
     * @param payload Deal creation event payload
     * @return Response indicating event received
     */
    @PostMapping("/deal/created")
    public ResponseEntity<ResponseWrapper> onDealCreated(@RequestBody DealEventPayload payload) {
        log.info("Received DEAL_CREATED event for deal: {}", payload.getDealId());

        try {
            // Validate payload
            if (payload.getDealId() == null || payload.getMerchantId() == null) {
                log.warn("Invalid deal created payload: missing required fields");
                return ResponseEntity.badRequest().body(
                        new ResponseWrapper("Missing required fields", "INVALID_REQUEST")
                );
            }

            // Convert to SSE event
            SseEventRequest event = SseEventRequest.builder()
                    .eventId("evt-" + UUID.randomUUID())
                    .eventType(BusinessEventType.DEAL_CREATED)
                    .sourceService("kupi-core-service")
                    .recipientUserId(payload.getMerchantId())  // Send to merchant
                    .payload(convertDealPayloadToMap(payload))
                    .timestamp(LocalDateTime.now())
                    .build();

            // Publish event (async)
            realTimeEventPublisher.publishEvent(event);

            log.info("Deal created event published successfully: eventId={}", event.getEventId());
            return ResponseEntity.ok(
                    new ResponseWrapper("Event received and published", "SUCCESS")
            );

        } catch (Exception e) {
            log.error("Error processing deal created event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseWrapper("Failed to process event", "ERROR")
            );
        }
    }

    /**
     * Receive deal approval status changed event
     * Notifies merchant of approval/rejection
     *
     * @param payload Deal approval event
     * @return Response indicating event received
     */
    @PostMapping("/deal/approval-status-changed")
    public ResponseEntity<ResponseWrapper> onDealApprovalChanged(@RequestBody DealEventPayload payload) {
        log.info("Received DEAL_APPROVAL_CHANGED event for deal: {}", payload.getDealId());

        try {
            if (payload.getDealId() == null || payload.getMerchantId() == null || payload.getDealStatus() == null) {
                log.warn("Invalid deal approval payload: missing required fields");
                return ResponseEntity.badRequest().body(
                        new ResponseWrapper("Missing required fields", "INVALID_REQUEST")
                );
            }

            // Determine event type based on status
            BusinessEventType eventType = "APPROVED".equalsIgnoreCase(payload.getDealStatus())
                    ? BusinessEventType.DEAL_APPROVED
                    : BusinessEventType.DEAL_REJECTED;

            SseEventRequest event = SseEventRequest.builder()
                    .eventId("evt-" + UUID.randomUUID())
                    .eventType(eventType)
                    .sourceService("kupi-core-service")
                    .recipientUserId(payload.getMerchantId())  // Send to merchant who created deal
                    .payload(convertDealPayloadToMap(payload))
                    .timestamp(LocalDateTime.now())
                    .build();

            realTimeEventPublisher.publishEvent(event);

            log.info("Deal approval event published successfully: eventId={}, status={}",
                    event.getEventId(), payload.getDealStatus());
            return ResponseEntity.ok(
                    new ResponseWrapper("Event received and published", "SUCCESS")
            );

        } catch (Exception e) {
            log.error("Error processing deal approval event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseWrapper("Failed to process event", "ERROR")
            );
        }
    }

    /**
     * Receive coupon redeemed event from core service
     * Notifies merchant about coupon redemptions
     *
     * @param payload Coupon redemption event payload
     * @return Response indicating event received
     */
    @PostMapping("/coupon/redeemed")
    public ResponseEntity<ResponseWrapper> onCouponRedeemed(@RequestBody CouponRedemptionPayload payload) {
        log.info("Received COUPON_REDEEMED event for coupon: {}", payload.getCouponId());

        try {
            if (payload.getCouponId() == null || payload.getMerchantId() == null) {
                log.warn("Invalid coupon redemption payload: missing required fields");
                return ResponseEntity.badRequest().body(
                        new ResponseWrapper("Missing required fields", "INVALID_REQUEST")
                );
            }

            SseEventRequest event = SseEventRequest.builder()
                    .eventId("evt-" + UUID.randomUUID())
                    .eventType(BusinessEventType.COUPON_REDEEMED)
                    .sourceService("kupi-core-service")
                    .recipientUserId(payload.getMerchantId())  // Send to merchant
                    .payload(convertCouponPayloadToMap(payload))
                    .timestamp(LocalDateTime.now())
                    .build();

            realTimeEventPublisher.publishEvent(event);

            log.info("Coupon redeemed event published successfully: eventId={}", event.getEventId());
            return ResponseEntity.ok(
                    new ResponseWrapper("Event received and published", "SUCCESS")
            );

        } catch (Exception e) {
            log.error("Error processing coupon redeemed event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseWrapper("Failed to process event", "ERROR")
            );
        }
    }

    /**
     * Receive deal status change event
     * Notifies affected parties of status changes
     *
     * @param payload Deal status change event
     * @return Response indicating event received
     */
    @PostMapping("/deal/status-changed")
    public ResponseEntity<ResponseWrapper> onDealStatusChanged(@RequestBody DealEventPayload payload) {
        log.info("Received DEAL_STATUS_CHANGED event for deal: {}", payload.getDealId());

        try {
            if (payload.getDealId() == null || payload.getDealStatus() == null) {
                log.warn("Invalid deal status change payload: missing required fields");
                return ResponseEntity.badRequest().body(
                        new ResponseWrapper("Missing required fields", "INVALID_REQUEST")
                );
            }

            SseEventRequest event = SseEventRequest.builder()
                    .eventId("evt-" + UUID.randomUUID())
                    .eventType(BusinessEventType.DEAL_STATUS_CHANGED)
                    .sourceService("kupi-core-service")
                    .recipientUserId(payload.getMerchantId())  // Send to merchant
                    .payload(convertDealPayloadToMap(payload))
                    .timestamp(LocalDateTime.now())
                    .build();

            realTimeEventPublisher.publishEvent(event);

            log.info("Deal status changed event published successfully: eventId={}", event.getEventId());
            return ResponseEntity.ok(
                    new ResponseWrapper("Event received and published", "SUCCESS")
            );

        } catch (Exception e) {
            log.error("Error processing deal status changed event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseWrapper("Failed to process event", "ERROR")
            );
        }
    }

    /**
     * Health check endpoint for core service to verify listener is running
     *
     * @return OK response
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("SSE Event Listener is healthy");
    }

    /**
     * Convert DealEventPayload to map for inclusion in SSE event
     *
     * @param payload Deal event payload
     * @return Map of payload data
     */
    private Map<String, Object> convertDealPayloadToMap(DealEventPayload payload) {
        Map<String, Object> map = new HashMap<>();
        map.put("dealId", payload.getDealId());
        map.put("dealTitle", payload.getDealTitle());
        map.put("dealDescription", payload.getDealDescription());
        map.put("merchantId", payload.getMerchantId());
        map.put("merchantName", payload.getMerchantName());
        map.put("imageUrl", payload.getImageUrl());
        map.put("dealStatus", payload.getDealStatus());
        map.put("originalPrice", payload.getOriginalPrice());
        map.put("discountedPrice", payload.getDiscountedPrice());
        map.put("discountPercentage", payload.getDiscountPercentage());
        return map;
    }

    /**
     * Convert CouponRedemptionPayload to map for inclusion in SSE event
     *
     * @param payload Coupon event payload
     * @return Map of payload data
     */
    private Map<String, Object> convertCouponPayloadToMap(CouponRedemptionPayload payload) {
        Map<String, Object> map = new HashMap<>();
        map.put("couponId", payload.getCouponId());
        map.put("couponCode", payload.getCouponCode());
        map.put("dealId", payload.getDealId());
        map.put("dealTitle", payload.getDealTitle());
        map.put("merchantId", payload.getMerchantId());
        map.put("merchantName", payload.getMerchantName());
        map.put("userId", payload.getUserId());
        map.put("redeemedAt", payload.getRedeemedAt());
        map.put("redemptionValue", payload.getRedemptionValue());
        return map;
    }
}
