package com.kupi.cc.location.domain.request;

import com.kupi.cc.location.enums.ApprovalStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Branch Update RequestDto
 */
@Getter
@Setter
public class BranchUpdateRequestDto extends RequestDto {

    private String id;
    private String name;
    private AddressRequestDto address;
    private ContactNoRequestDto contactNo;
    private ApprovalStatus status;
    private LocationRequestDto location;
    private String userId;

    /**
     * This method checks whether the required fields are not null
     *
     * @return true/false
     */
    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(id) && isNonEmpty(name) && isNonEmpty(userId) && location.isRequiredAvailable()
                && address.isRequiredAvailable();
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