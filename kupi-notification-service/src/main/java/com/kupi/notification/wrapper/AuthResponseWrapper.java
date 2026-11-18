package com.kupi.notification.wrapper;

import com.kupi.notification.domain.BaseDto;
import com.kupi.notification.domain.entity.AuthUser;
import com.kupi.notification.enums.ResponseStatusType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ResponseWrapper for auth - service's response
 */
@Getter
@Setter
@NoArgsConstructor
public class AuthResponseWrapper implements BaseDto {

    private ResponseStatusType status;
    private String message;
    private AuthUser data;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
