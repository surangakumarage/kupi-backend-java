package com.kupi.notification.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kupi.notification.domain.entity.NotificationDetail;
import com.kupi.notification.domain.response.DateResponseDto;
import com.kupi.notification.domain.response.DealResponseDto;
import com.kupi.notification.domain.response.UserResponseDto;
import com.kupi.notification.enums.NotificationType;
import lombok.Getter;

@Getter
public class NotificationItem {

    private final String id;
    private final String title;
    private final String body;
    private final String imageUrl;
    private final boolean read;
    private final DateResponseDto receivedOn;
    private final NotificationType type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserResponseDto merchant;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DealResponseDto deal;

    public NotificationItem(NotificationDetail notification, String timeZone) {
        this.id = notification.getId();
        this.title = notification.getTitle();
        this.body = notification.getBody();
        this.imageUrl = notification.getImageUrl();
        this.read = notification.isRead();
        this.receivedOn =
                new DateResponseDto(notification.getCreatedAt().getTime(), timeZone, notification.getCreatedAt());
        this.type = notification.getType();
        if (type == NotificationType.MERCHANT)
            this.merchant = new UserResponseDto(notification.getReferralId(), imageUrl);
        if (type == NotificationType.MERCHANT_DEAL || type == NotificationType.BANK_DEAL)
            this.deal = DealResponseDto.builder()
                    .id(notification.getReferralId())
                    .title(title)
                    .subtitle(notification.getBody())
                    .imageUrl(imageUrl)
                    .build();
    }

}
