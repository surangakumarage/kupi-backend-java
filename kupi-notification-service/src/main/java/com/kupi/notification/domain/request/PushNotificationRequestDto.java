package com.kupi.notification.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PushNotificationRequestDto extends RequestDto {

    private String userId;
    private List<DealGroupRequestDto> dealGroups;

    /**
     * This method checks all required fields are available.
     *
     * @return true/ false
     */
    @Override
    public boolean isRequiredAvailable() {
        return !dealGroups.isEmpty() && isNonEmpty(userId);
    }


    /**
     * This method converts this object to json string for logging purpose.
     *
     * @return json string
     */
    @Override
    public String toLogJson() {
        return toJson();
    }

}
