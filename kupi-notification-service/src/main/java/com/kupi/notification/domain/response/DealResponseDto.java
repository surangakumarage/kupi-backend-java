package com.kupi.notification.domain.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Deal Response Dto
 */
@Getter
@Builder
public class DealResponseDto implements ResponseDto {

    private String id;
    private String title;
    private String subtitle;
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
