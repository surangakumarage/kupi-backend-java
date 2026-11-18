package com.kupi.notification.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddTokenRequestDto extends RequestDto {

    private String userId;
    private String deviceId;

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(userId) && isNonEmpty(deviceId);
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
