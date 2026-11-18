package com.kupi.notification.domain.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Basic user request Dto
 */
@Getter
@Setter
public class BasicUserRequestDto extends RequestDto {

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
        return null;
    }
}
