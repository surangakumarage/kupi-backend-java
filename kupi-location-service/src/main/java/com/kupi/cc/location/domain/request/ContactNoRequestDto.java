package com.kupi.cc.location.domain.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contact RequestDto
 */
@Getter
@Setter
@NoArgsConstructor
public class ContactNoRequestDto extends RequestDto {
    private String value;
    private Boolean verified;

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
