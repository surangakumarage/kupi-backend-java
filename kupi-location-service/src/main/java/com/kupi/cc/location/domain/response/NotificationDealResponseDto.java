package com.kupi.cc.location.domain.response;

import com.kupi.cc.location.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Notification Deal ResponseDto
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDealResponseDto {

    private String id;
    private String title;
    private String subTitle;
    private String coverImage;
    private UserType dealSource;
    /**
     * Whether this deal is marked as "Deal of the Day"
     */
    private Boolean isDealOfTheDay;
    /**
     * Expiration date in milliseconds
     */
    private Long expiredOn;
}