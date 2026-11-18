package com.kupi.notification.domain.request;

import com.kupi.notification.enums.NotificationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * NotificationRequestDto
 */
@Getter
@Setter
@NoArgsConstructor
public class NotificationRequestDto extends RequestDto {

    private String title;
    private String body;
    private String imageUrl;
    private NotificationType type;
    private String referralId;

    public NotificationRequestDto(String title, String body, NotificationType notificationType,
                                  String referralId, String imageUrl) {
        this.title = title;
        this.body = body;
        this.type = notificationType;
        this.referralId = referralId;
        this.imageUrl = imageUrl;
    }

    /**
     * This method checks all required fields are available.
     *
     * @return true/ false
     */
    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(title) &&
                isNonEmpty(body);
    }

    /**
     * This method converts this object to json string for logging purpose.
     *
     * @return json string
     */
    @Override
    public String toLogJson() {
        return toJson();
    }
}
