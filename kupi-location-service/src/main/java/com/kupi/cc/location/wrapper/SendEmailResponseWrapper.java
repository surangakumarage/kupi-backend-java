package com.kupi.cc.location.wrapper;

import com.kupi.cc.location.domain.BaseDto;
import com.kupi.cc.location.enums.ResponseStatusType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendEmailResponseWrapper implements BaseDto {

    private ResponseStatusType status;
    private String message;
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