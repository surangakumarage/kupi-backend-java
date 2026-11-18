package com.kupi.cc.location.domain.request;

import com.kupi.cc.location.enums.VerificationStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Verify Address Contact RequestDto
 */
@Getter
@Setter
public class VerifyAddressContactRequestDto extends RequestDto {

    private String id;
    private VerificationStatus action;

    /**
     * This method checks all required data are available.
     *
     * @return true/ false
     */
    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(id) && action != null;
    }

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