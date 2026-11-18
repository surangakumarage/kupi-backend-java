package com.kupi.cc.location.wrapper;

import com.kupi.cc.location.domain.BaseDto;
import com.kupi.cc.location.domain.entity.AuthUser;
import com.kupi.cc.location.enums.ResponseStatusType;
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