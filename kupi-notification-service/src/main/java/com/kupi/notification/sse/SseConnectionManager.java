package com.kupi.notification.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe manager for SSE connections
 * Manages SseEmitter instances for each connected user
 * Handles sending events and cleaning up disconnected clients
 */
@Slf4j
@Component
public class SseConnectionManager {

    private final ConcurrentMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Register a new SSE connection for a user
     *
     * @param userId   The user ID
     * @param emitter  The SseEmitter for this connection
     */
    public void addEmitter(String userId, SseEmitter emitter) {
        if (userId == null || emitter == null) {
            log.warn("Attempted to add null userId or emitter");
            return;
        }
        emitters.put(userId, emitter);
        log.debug("SSE connection registered for user: {}", userId);
    }

    /**
     * Remove an SSE connection for a user
     *
     * @param userId The user ID
     */
    public void removeEmitter(String userId) {
        if (userId == null) {
            return;
        }
        SseEmitter removed = emitters.remove(userId);
        if (removed != null) {
            log.debug("SSE connection removed for user: {}", userId);
            try {
                removed.complete();
            } catch (Exception e) {
                log.debug("Exception completing emitter for user {}: {}", userId, e.getMessage());
            }
        }
    }

    /**
     * Get the SseEmitter for a specific user
     *
     * @param userId The user ID
     * @return The SseEmitter if exists, null otherwise
     */
    public SseEmitter getEmitter(String userId) {
        return emitters.get(userId);
    }

    /**
     * Check if a user has an active SSE connection
     *
     * @param userId The user ID
     * @return true if user has an active connection
     */
    public boolean isConnected(String userId) {
        return emitters.containsKey(userId);
    }

    /**
     * Send an SSE event to a specific user
     * Removes the connection if delivery fails
     *
     * @param userId The target user ID
     * @param event  The SSE event to send
     * @return true if event was sent successfully, false otherwise
     */
    public boolean sendEventToUser(String userId, SseEmitter.SseEventBuilder event) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            log.debug("No active SSE connection for user: {}", userId);
            return false;
        }

        try {
            emitter.send(event);
            log.debug("SSE event sent successfully to user: {}", userId);
            return true;
        } catch (IOException e) {
            log.warn("Failed to send SSE event to user: {}, removing connection. Error: {}", userId, e.getMessage());
            removeEmitter(userId);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error sending SSE event to user: {}", userId, e);
            removeEmitter(userId);
            return false;
        }
    }

    /**
     * Get the number of active SSE connections
     *
     * @return Number of connected users
     */
    public int getActiveConnectionCount() {
        return emitters.size();
    }

    /**
     * Broadcast an event to multiple users (not used in basic implementation)
     * Sends same event to list of users
     *
     * @param userIds List of user IDs to receive the event
     * @param event   The SSE event to broadcast
     * @return Number of successful deliveries
     */
    public int broadcastEvent(java.util.List<String> userIds, SseEmitter.SseEventBuilder event) {
        int successCount = 0;
        for (String userId : userIds) {
            if (sendEventToUser(userId, event)) {
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * Clean up all SSE connections (useful on service shutdown)
     */
    public void shutdownAllConnections() {
        log.info("Shutting down all SSE connections. Active connections: {}", emitters.size());
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.debug("Exception completing emitter during shutdown for user {}: {}", userId, e.getMessage());
            }
        });
        emitters.clear();
        log.info("All SSE connections closed");
    }
}
