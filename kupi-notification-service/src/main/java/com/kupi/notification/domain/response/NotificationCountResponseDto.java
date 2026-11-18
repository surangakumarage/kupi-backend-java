package com.kupi.notification.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Notification Count Response Dto
 */
@Getter
@AllArgsConstructor
public class NotificationCountResponseDto implements ResponseDto {

    private int count;

    /**
     * This method converts object to json string for logging purpose.
     * PII data should be obfuscated.
     *
     * @return json string
     */
    @Override
    public String toLogJson() {
        return toJson();
    }
}
