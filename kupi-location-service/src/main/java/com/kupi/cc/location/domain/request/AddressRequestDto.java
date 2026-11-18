package com.kupi.cc.location.domain.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Address RequestDto
 */
@Getter
@Setter
@NoArgsConstructor
public class AddressRequestDto extends RequestDto {

    private String value;
    private String proofUrl;
    private Boolean verified;

    /**
     * This method checks whether the required fields are not null
     *
     * @return true/false
     */
    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(value);
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
