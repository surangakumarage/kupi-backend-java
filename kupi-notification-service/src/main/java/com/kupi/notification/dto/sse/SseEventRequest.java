package com.kupi.notification.dto.sse;

import com.kupi.notification.model.event.BusinessEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Unified event request DTO for all business events
 * Transmitted via SSE to clients
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SseEventRequest {

    private String eventId;
    private BusinessEventType eventType;
    private String sourceService;          // e.g., "kupi-core-service"
    private String recipientUserId;        // Target user for this notification
    private Map<String, Object> payload;   // Event-specific data
    private LocalDateTime timestamp;

    /**
     * Validates that required fields are populated
     */
    public boolean isValid() {
        return eventId != null &&
               eventType != null &&
               sourceService != null &&
               recipientUserId != null &&
               payload != null &&
               timestamp != null;
    }
}
