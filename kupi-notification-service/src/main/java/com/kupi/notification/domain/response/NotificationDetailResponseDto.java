package com.kupi.notification.domain.response;

import com.kupi.notification.domain.NotificationItem;
import com.kupi.notification.domain.entity.NotificationDetail;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Getter
public class NotificationDetailResponseDto extends PageResponseDto {

    private List<NotificationItem> notifications;

    public NotificationDetailResponseDto(Page<NotificationDetail> page, String timeZoneId) {
        super(page);
        convertToList(page.getContent(), timeZoneId);
    }

    private void convertToList(List<NotificationDetail> notificationDetails, String timezoneId) {
        this.notifications = new ArrayList<>();
        notificationDetails.forEach(notificationDetail ->
                notifications.add(new NotificationItem(notificationDetail, timezoneId)));
    }
}
