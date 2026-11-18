package com.kupi.notification.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Notification Status Response Dto
 */
@AllArgsConstructor
@Getter
public class NotificationStatusResponseDto implements ResponseDto {

    private boolean isEnabled;

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
