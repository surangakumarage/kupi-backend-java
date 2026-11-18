package com.kupi.cc.base.domain.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Bulk user request Dto to get list of users from auth service
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BulkUserRequestDto extends RequestDto {

    private static final int MAX_USER_IDS = 25;
    private List<String> userIds;

    /**
     * This method is to validated userId size.
     *
     * @return true/ false
     */
    public boolean isRequestSizeAcceptable() {
        return userIds.size() <= MAX_USER_IDS;
    }

    /**
     * This method checks all required fields are available.
     *
     * @return true/ false
     */
    @Override
    public boolean isRequiredAvailable() {
        return userIds != null && !userIds.isEmpty();
    }

    /**
     * This method converts this object to json string for logging purpose.
     * All fields are obfuscated.
     *
     * @return json string
     */
    @Override
    public String toLogJson() {
        return toJson();
    }
}
