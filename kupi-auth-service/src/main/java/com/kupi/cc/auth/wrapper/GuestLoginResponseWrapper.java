package com.kupi.cc.auth.wrapper;

import com.kupi.cc.auth.domain.Data;
import com.kupi.cc.auth.domain.response.ResponseDto;
import lombok.Getter;
import lombok.Setter;

/**
 * GuestLoginResponseWrapper
 */
@Getter
@Setter
public class GuestLoginResponseWrapper implements ResponseDto {

    private String access_token;
    private String token_type;
    private String refresh_token;
    private long expires_in;
    private String scope;
    private Data data;
    private String message;
    private String status;

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
