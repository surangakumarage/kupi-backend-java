package com.kupi.cc.location.domain.response;

import lombok.Getter;
import lombok.Setter;

/**
 * Business Response Dto
 */
@Setter
@Getter
public class BusinessResponseDto implements ResponseDto {
    private String id;
    private String name;
    private String imageUrl;
    private String approvalStatus;
    private boolean isActive;
    private String userType;

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


