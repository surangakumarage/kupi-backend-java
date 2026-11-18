package com.kupi.notification.service;

import com.kupi.notification.dto.sse.SseEventRequest;
import com.kupi.notification.model.event.BusinessEventType;
import com.kupi.notification.repository.NotificationRepository;
import com.kupi.notification.sse.SseConnectionManager;
import com.kupi.notification.domain.entity.NotificationDetail;
import com.kupi.notification.enums.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

/**
 * Service for publishing real-time events via SSE
 * Handles event transformation, persistence, and delivery via SSE with Firebase fallback
 */
@Slf4j
@Service
public class RealTimeEventPublisher {

    private final SseConnectionManager sseConnectionManager;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Autowired
    public RealTimeEventPublisher(
            SseConnectionManager sseConnectionManager,
            NotificationRepository notificationRepository,
            NotificationService notificationService) {
        this.sseConnectionManager = sseConnectionManager;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    /**
     * Publish an event via SSE with Firebase backup
     * This method should be called asynchronously to not block the business logic
     *
     * @param eventRequest The event to publish
     */
    @Async
    public void publishEvent(SseEventRequest eventRequest) {
        if (eventRequest == null || !eventRequest.isValid()) {
            log.warn("Invalid event request received: {}", eventRequest);
            return;
        }

        log.info("Publishing event: type={}, recipient={}, eventId={}",
                eventRequest.getEventType(), eventRequest.getRecipientUserId(), eventRequest.getEventId());

        try {
            // Step 1: Save event to database for audit trail
            saveEventToDatabase(eventRequest);

            // Step 2: Try to send via SSE
            boolean sseDelivered = publishViaSse(eventRequest);

            // Step 3: If SSE failed, fallback to Firebase
            if (!sseDelivered) {
                log.info("SSE delivery failed for user: {}, falling back to Firebase", eventRequest.getRecipientUserId());
                publishViaFirebase(eventRequest);
            } else {
                log.debug("SSE delivery successful for user: {}", eventRequest.getRecipientUserId());
            }

        } catch (Exception e) {
            log.error("Error publishing event: {}", eventRequest.getEventId(), e);
        }
    }

    /**
     * Save event to database for audit trail and potential replay
     *
     * @param eventRequest The event to save
     */
    private void saveEventToDatabase(SseEventRequest eventRequest) {
        try {
            NotificationDetail detail = new NotificationDetail();
            detail.setId("sse-" + UUID.randomUUID());
            detail.setUserId(eventRequest.getRecipientUserId());
            detail.setTitle(mapEventTypeToTitle(eventRequest.getEventType()));
            detail.setBody(buildEventBody(eventRequest));
            detail.setEventType(eventRequest.getEventType().toString());
            detail.setSourceService(eventRequest.getSourceService());
            detail.setIsRealtimeSent(false);  // Mark as pending for SSE
            detail.setCreatedAt(new Date());
            detail.setUpdatedAt(new Date());
            detail.setIsRead(false);
            detail.setIsDeleted(false);

            // Map event type to notification type
            detail.setType(mapEventTypeToNotificationType(eventRequest.getEventType()));

            notificationRepository.save(detail);
            log.debug("Event saved to database: id={}", detail.getId());

        } catch (Exception e) {
            log.error("Error saving event to database", e);
            // Don't rethrow - continue with SSE/Firebase delivery
        }
    }

    /**
     * Publish event via SSE to connected client
     *
     * @param eventRequest The event to publish
     * @return true if SSE delivery was successful, false otherwise
     */
    private boolean publishViaSse(SseEventRequest eventRequest) {
        try {
            if (!sseConnectionManager.isConnected(eventRequest.getRecipientUserId())) {
                log.debug("User not connected via SSE: {}", eventRequest.getRecipientUserId());
                return false;
            }

            SseEmitter.SseEventBuilder sseEvent = SseEmitter.event()
                    .id(eventRequest.getEventId())
                    .name(eventRequest.getEventType().toString())
                    .data(eventRequest)
                    .reconnectTime(30000);  // 30 seconds reconnect time

            boolean success = sseConnectionManager.sendEventToUser(
                    eventRequest.getRecipientUserId(),
                    sseEvent
            );

            if (success) {
                // Update database to mark SSE as delivered
                updateSseDeliveryStatus(eventRequest.getRecipientUserId(), true);
            }

            return success;

        } catch (Exception e) {
            log.error("Error publishing via SSE: {}", eventRequest.getEventId(), e);
            return false;
        }
    }

    /**
     * Publish event via Firebase as backup if SSE is not available
     *
     * @param eventRequest The event to publish
     */
    private void publishViaFirebase(SseEventRequest eventRequest) {
        try {
            // Build Firebase notification
            String title = mapEventTypeToTitle(eventRequest.getEventType());
            String body = buildEventBody(eventRequest);

            log.debug("Publishing via Firebase: user={}, title={}", eventRequest.getRecipientUserId(), title);

            // Note: Actual Firebase publish logic would go here
            // This is a placeholder - integrate with existing NotificationService Firebase logic
            // notificationService.sendFirebaseNotification(eventRequest.getRecipientUserId(), title, body);

        } catch (Exception e) {
            log.error("Error publishing via Firebase: {}", eventRequest.getEventId(), e);
        }
    }

    /**
     * Update database to mark SSE delivery
     *
     * @param userId    User ID
     * @param delivered Whether delivery was successful
     */
    private void updateSseDeliveryStatus(String userId, boolean delivered) {
        try {
            // This would require a custom repository query to update the latest notification for this user
            // Implementation depends on your existing notification repository setup
            log.debug("Updated SSE delivery status for user: {}, delivered: {}", userId, delivered);
        } catch (Exception e) {
            log.error("Error updating SSE delivery status", e);
        }
    }

    /**
     * Map BusinessEventType to a user-friendly title
     *
     * @param eventType The event type
     * @return User-friendly title
     */
    private String mapEventTypeToTitle(BusinessEventType eventType) {
        return switch (eventType) {
            case DEAL_CREATED -> "New Deal Available";
            case DEAL_APPROVED -> "Your Deal Has Been Approved";
            case DEAL_REJECTED -> "Your Deal Was Rejected";
            case COUPON_REDEEMED -> "Coupon Redeemed";
            case DEAL_STATUS_CHANGED -> "Deal Status Updated";
        };
    }

    /**
     * Build event body from event data
     *
     * @param eventRequest The event
     * @return Event body text
     */
    private String buildEventBody(SseEventRequest eventRequest) {
        return switch (eventRequest.getEventType()) {
            case DEAL_CREATED -> {
                Object dealTitle = eventRequest.getPayload().get("dealTitle");
                Object merchantName = eventRequest.getPayload().get("merchantName");
                yield String.format("%s just created a new deal", merchantName != null ? merchantName : "A merchant");
            }
            case DEAL_APPROVED -> "Your deal has been approved and is now live";
            case DEAL_REJECTED -> "Your deal was rejected. Please review and resubmit.";
            case COUPON_REDEEMED -> {
                Object dealTitle = eventRequest.getPayload().get("dealTitle");
                yield String.format("Your coupon for %s has been redeemed", dealTitle != null ? dealTitle : "a deal");
            }
            case DEAL_STATUS_CHANGED -> {
                Object status = eventRequest.getPayload().get("dealStatus");
                yield String.format("Deal status changed to %s", status != null ? status : "updated");
            }
        };
    }

    /**
     * Map BusinessEventType to NotificationType enum used in database
     *
     * @param eventType The business event type
     * @return NotificationType
     */
    private NotificationType mapEventTypeToNotificationType(BusinessEventType eventType) {
        return switch (eventType) {
            case DEAL_CREATED, DEAL_APPROVED, DEAL_REJECTED, DEAL_STATUS_CHANGED -> NotificationType.MERCHANT_DEAL;
            case COUPON_REDEEMED -> NotificationType.MERCHANT_DEAL;
        };
    }
}
