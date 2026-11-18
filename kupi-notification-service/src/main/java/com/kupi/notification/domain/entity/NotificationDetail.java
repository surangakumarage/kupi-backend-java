package com.kupi.notification.domain.entity;

import com.kupi.notification.domain.request.NotificationRequestDto;
import com.kupi.notification.enums.NotificationType;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "notification_detail")
@Data
@NoArgsConstructor
public class NotificationDetail {

    @Transient
    private static final String NOTIFICATION_PREFIX = "nid-";

    @Id
    private String id;
    private String title;
    private String body;
    private String imageUrl;
    private String userId;
    private boolean isRead = false;
    private Date createdAt;
    private Date updatedAt;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    private String referralId;
    private boolean isDeleted;
    private String eventType;
    private String sourceService;
    private boolean isRealtimeSent = false;

    public NotificationDetail(NotificationRequestDto notificationRequestDto, String userId) {
        this.id = NOTIFICATION_PREFIX + UUID.randomUUID();
        this.title = notificationRequestDto.getTitle();
        this.body = notificationRequestDto.getBody();
        this.imageUrl = notificationRequestDto.getImageUrl();
        this.userId = userId;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.type = notificationRequestDto.getType();
        this.referralId = notificationRequestDto.getReferralId();
    }

    // Explicit setters (Lombok @Data should generate these, but being explicit for compatibility)
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setBody(String body) { this.body = body; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setIsRead(boolean isRead) { this.isRead = isRead; }
    public void setRead(boolean isRead) { this.isRead = isRead; }  // Alias for backward compatibility
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public void setType(NotificationType type) { this.type = type; }
    public void setReferralId(String referralId) { this.referralId = referralId; }
    public void setIsDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setSourceService(String sourceService) { this.sourceService = sourceService; }
    public void setIsRealtimeSent(boolean isRealtimeSent) { this.isRealtimeSent = isRealtimeSent; }
}
