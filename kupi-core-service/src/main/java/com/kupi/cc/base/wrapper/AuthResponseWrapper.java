package com.kupi.cc.base.wrapper;

import com.kupi.cc.base.domain.BaseDto;
import com.kupi.cc.base.domain.entity.AuthUser;
import com.kupi.cc.base.enums.ResponseStatusType;
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
    private String displayMessage;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
