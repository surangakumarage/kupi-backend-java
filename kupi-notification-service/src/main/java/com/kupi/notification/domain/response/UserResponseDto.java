package com.kupi.notification.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * User Response Dto
 */
@Getter
@AllArgsConstructor
public class UserResponseDto implements ResponseDto {

    private String id;
    private String imageUrl;

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
