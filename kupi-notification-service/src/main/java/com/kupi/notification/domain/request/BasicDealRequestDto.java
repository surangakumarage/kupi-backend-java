package com.kupi.notification.domain.request;

import com.kupi.notification.enums.NotificationType;
import lombok.Getter;
import lombok.Setter;

/**
 * Basic Deal RequestDto
 */
@Getter
@Setter
public class BasicDealRequestDto {

    private String id;
    private String title;
    private String subTitle;
    private String imageUrl;
    private NotificationType type;

}
