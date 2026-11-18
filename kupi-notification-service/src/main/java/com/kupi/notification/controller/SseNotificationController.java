package com.kupi.notification.controller;

import com.kupi.notification.configuration.Translator;
import com.kupi.notification.enums.ErrorResponseStatusType;
import com.kupi.notification.enums.SuccessResponseStatusType;
import com.kupi.notification.sse.SseConnectionManager;
import com.kupi.notification.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST Controller for Server-Sent Events (SSE) real-time notifications
 * Handles SSE subscription, heartbeat, and unsubscription
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notification/sse")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SseNotificationController extends Controller {

    private static final String USER_ID_HEADER = "User-Id";
    private static final long SSE_TIMEOUT_MS = 300000L;  // 5 minutes
    private static final long HEARTBEAT_TIMEOUT_MS = 30000L;  // 30 seconds
    private final SseConnectionManager sseConnectionManager;

    @Autowired
    public SseNotificationController(Translator translator, SseConnectionManager sseConnectionManager) {
        super(translator);
        this.sseConnectionManager = sseConnectionManager;
    }

    /**
     * Subscribe to SSE notifications
     * Client calls this endpoint and establishes long-lived connection
     * Server will push events to client as they occur
     *
     * @param userId              User ID from header
     * @param httpServletRequest  HTTP request context
     * @return SseEmitter for streaming events
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
            HttpServletRequest httpServletRequest) {

        log.info("SSE subscription request from user: {}", userId);

        // Validate user ID
        if (userId == null || userId.trim().isEmpty()) {
            log.warn("SSE subscription rejected: missing User-Id header");
            throw new IllegalArgumentException("User-Id header is required");
        }

        try {
            // Create SSE emitter with timeout
            SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

            // Register connection
            sseConnectionManager.addEmitter(userId, emitter);
            log.info("SSE connection established for user: {}, total active: {}",
                    userId, sseConnectionManager.getActiveConnectionCount());

            // Handle connection completion (client disconnect or timeout)
            emitter.onCompletion(() -> {
                log.info("SSE connection completed for user: {}", userId);
                sseConnectionManager.removeEmitter(userId);
            });

            // Handle connection timeout
            emitter.onTimeout(() -> {
                log.warn("SSE connection timeout for user: {}", userId);
                sseConnectionManager.removeEmitter(userId);
            });

            // Handle connection error
            emitter.onError(throwable -> {
                log.error("SSE connection error for user: {}: {}", userId, throwable.getMessage());
                sseConnectionManager.removeEmitter(userId);
            });

            return emitter;

        } catch (Exception e) {
            log.error("Error creating SSE emitter for user: {}", userId, e);
            throw new RuntimeException("Failed to create SSE connection", e);
        }
    }

    /**
     * Send a heartbeat to keep the SSE connection alive
     * Client can call this periodically to prevent connection timeout
     *
     * @param userId User ID from header
     * @return Response indicating heartbeat received
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<ResponseWrapper> heartbeat(
            @RequestHeader(value = USER_ID_HEADER, required = false) String userId) {

        if (userId == null || userId.trim().isEmpty()) {
            return getBadRequestError(ErrorResponseStatusType.INVALID_REQUEST_FORMAT);
        }

        try {
            SseEmitter emitter = sseConnectionManager.getEmitter(userId);
            if (emitter != null) {
                // Send heartbeat as a comment (doesn't trigger event listener on client)
                SseEmitter.SseEventBuilder heartbeat = SseEmitter.event()
                        .id("heartbeat-" + System.currentTimeMillis())
                        .name("heartbeat")
                        .data("alive")
                        .reconnectTime(HEARTBEAT_TIMEOUT_MS);

                sseConnectionManager.sendEventToUser(userId, heartbeat);
                log.debug("Heartbeat sent to user: {}", userId);

                return new ResponseEntity<>(new ResponseWrapper(com.kupi.notification.enums.ResponseStatusType.SUCCESS, SuccessResponseStatusType.HEARTBEAT_RECEIVED.getMessage()), HttpStatus.OK);
            } else {
                log.warn("Heartbeat request for user with no active SSE connection: {}", userId);
                return new ResponseEntity<>(new ResponseWrapper(com.kupi.notification.enums.ResponseStatusType.SUCCESS, SuccessResponseStatusType.SUCCESS.getMessage()), HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("Error sending heartbeat to user: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Unsubscribe from SSE notifications
     * Client calls this to explicitly close the connection
     *
     * @param userId User ID from header
     * @return Response indicating unsubscription processed
     */
    @DeleteMapping("/unsubscribe")
    public ResponseEntity<ResponseWrapper> unsubscribe(
            @RequestHeader(value = USER_ID_HEADER, required = false) String userId) {

        if (userId == null || userId.trim().isEmpty()) {
            return getBadRequestError(ErrorResponseStatusType.INVALID_REQUEST_FORMAT);
        }

        try {
            sseConnectionManager.removeEmitter(userId);
            log.info("SSE unsubscription processed for user: {}", userId);
            return getSuccessResponse(SuccessResponseStatusType.SUCCESS, null);
        } catch (Exception e) {
            log.error("Error unsubscribing user: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Get connection status (for debugging)
     *
     * @param userId User ID from header
     * @return Whether user has active SSE connection
     */
    @GetMapping("/status")
    public ResponseEntity<ResponseWrapper> status(
            @RequestHeader(value = USER_ID_HEADER, required = false) String userId) {

        if (userId == null || userId.trim().isEmpty()) {
            return getBadRequestError(ErrorResponseStatusType.INVALID_REQUEST_FORMAT);
        }

        boolean isConnected = sseConnectionManager.isConnected(userId);
        log.debug("SSE status request for user: {}, connected: {}", userId, isConnected);

        return getSuccessResponse(SuccessResponseStatusType.SUCCESS, null);
    }

    /**
     * Get active connection count (admin endpoint for monitoring)
     *
     * @return Total number of active SSE connections
     */
    @GetMapping("/active-connections")
    public ResponseEntity<ResponseWrapper> getActiveConnections() {
        int count = sseConnectionManager.getActiveConnectionCount();
        log.debug("Active SSE connections count: {}", count);

        return getSuccessResponse(SuccessResponseStatusType.SUCCESS, null);
    }

}
