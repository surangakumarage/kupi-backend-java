package com.kupi.cc.location.domain.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contact ResponseDto
 */
@Getter
@Setter
@NoArgsConstructor
public class ContactNoResponseDto implements ResponseDto {

    private String value;
    private boolean verified;

    public ContactNoResponseDto(String value, boolean verified) {
        this.value = value;
        this.verified = verified;
    }

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