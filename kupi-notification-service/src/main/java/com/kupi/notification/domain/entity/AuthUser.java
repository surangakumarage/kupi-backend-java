package com.kupi.notification.domain.entity;

import com.kupi.notification.domain.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Auth User
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser implements BaseDto {

    private String userId;
    private boolean isRegisteredUser;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
