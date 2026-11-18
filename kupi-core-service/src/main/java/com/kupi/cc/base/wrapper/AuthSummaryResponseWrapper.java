package com.kupi.cc.base.wrapper;

import com.kupi.cc.base.domain.BaseDto;
import com.kupi.cc.base.domain.response.dashboard.AuthSummaryResponseDto;
import com.kupi.cc.base.enums.ResponseStatusType;
import lombok.Getter;
import lombok.Setter;

/**
 * Response wrapper for today summary.
 */
@Getter
@Setter
public class AuthSummaryResponseWrapper implements BaseDto {

    private ResponseStatusType status;
    private String message;
    private AuthSummaryResponseDto data;
    private String displayMessage;


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
