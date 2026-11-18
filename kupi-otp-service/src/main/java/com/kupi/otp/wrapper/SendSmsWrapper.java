package com.kupi.otp.wrapper;

import com.kupi.otp.domain.BaseDto;
import com.kupi.otp.enums.ResponseStatusType;
import lombok.Getter;
import lombok.Setter;

/**
 * Send sms wrapper to get sms send response from util service
 */
@Setter
@Getter
public class SendSmsWrapper implements BaseDto {

    private ResponseStatusType status;
    private String message;
    private String displayMessage;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
