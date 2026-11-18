package com.kupi.notification.handler;

import com.kupi.notification.sse.SseConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * Handler for SSE-related exceptions and error scenarios
 * Provides graceful error recovery and connection cleanup
 */
@Slf4j
@Component
public class SseErrorHandler {

    private final SseConnectionManager sseConnectionManager;

    @Autowired
    public SseErrorHandler(SseConnectionManager sseConnectionManager) {
        this.sseConnectionManager = sseConnectionManager;
    }

    /**
     * Handle IOException that occurs during SSE event transmission
     * Typically occurs when client disconnects
     *
     * @param userId User ID for the disconnected connection
     * @param ex     The IOException that occurred
     */
    public void handleIoException(String userId, IOException ex) {
        log.warn("IOException in SSE for user {}: {}", userId, ex.getMessage());
        // Remove emitter when IO error occurs
        sseConnectionManager.removeEmitter(userId);
    }

    /**
     * Handle general exception during SSE operations
     *
     * @param userId User ID
     * @param ex     The exception that occurred
     */
    public void handleGeneralException(String userId, Exception ex) {
        log.error("General exception in SSE for user {}: {}", userId, ex.getMessage(), ex);
        // Remove emitter to prevent further attempts on broken connection
        sseConnectionManager.removeEmitter(userId);
    }

    /**
     * Handle IllegalStateException (connection already closed)
     *
     * @param userId User ID
     * @param ex     The IllegalStateException
     */
    public void handleIllegalStateException(String userId, IllegalStateException ex) {
        log.warn("Illegal state in SSE for user {}: {}", userId, ex.getMessage());
        sseConnectionManager.removeEmitter(userId);
    }

    /**
     * Create a safe error event that can be sent via SSE
     * Used for notifying clients of errors
     *
     * @param errorCode    Error code
     * @param errorMessage Error message
     * @return SseEventBuilder for error event
     */
    public SseEmitter.SseEventBuilder createErrorEvent(String errorCode, String errorMessage) {
        return SseEmitter.event()
                .id("error-" + System.currentTimeMillis())
                .name("error")
                .data(String.format("{\"code\": \"%s\", \"message\": \"%s\"}", errorCode, errorMessage))
                .reconnectTime(5000);  // Try reconnect after 5 seconds
    }

    /**
     * Create a reconnection event
     * Tells client to reconnect after specified delay
     *
     * @param delayMs Delay in milliseconds before client should reconnect
     * @return SseEventBuilder for reconnection event
     */
    public SseEmitter.SseEventBuilder createReconnectionEvent(long delayMs) {
        return SseEmitter.event()
                .id("reconnect-" + System.currentTimeMillis())
                .name("reconnect-required")
                .data("Server is restarting. Please reconnect.")
                .reconnectTime(delayMs);
    }

    /**
     * Check if exception indicates client disconnection
     *
     * @param ex Exception to check
     * @return true if this is a client disconnection exception
     */
    public static boolean isClientDisconnection(Exception ex) {
        if (ex == null) {
            return false;
        }
        String message = ex.getMessage();
        return message != null && (
                message.contains("ClientAbortException") ||
                message.contains("client disconnected") ||
                message.contains("Broken pipe") ||
                ex instanceof IOException
        );
    }

    /**
     * Log comprehensive error context for debugging
     *
     * @param userId    User ID
     * @param eventType Type of event being sent
     * @param exception Exception that occurred
     */
    public void logErrorContext(String userId, String eventType, Exception exception) {
        log.error("SSE Error Context - UserId: {}, EventType: {}, ExceptionType: {}, Message: {}",
                userId, eventType, exception.getClass().getSimpleName(), exception.getMessage(), exception);
    }
}
