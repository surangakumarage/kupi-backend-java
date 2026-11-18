package com.kupi.cc.location.domain.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Branch RequestDto
 */
@Getter
@Setter
@NoArgsConstructor
public class BranchRequestDto extends RequestDto {

    private String name;
    private AddressRequestDto address;
    private String contactNo;
    private LocationRequestDto location;
    private String userId;

    /**
     * This method checks whether the required fields are not null
     *
     * @return true/false
     */
    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(name) && isNonEmpty(userId) && address.isRequiredAvailable() && location.isRequiredAvailable();
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
