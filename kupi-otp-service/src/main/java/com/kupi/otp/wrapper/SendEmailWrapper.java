package com.kupi.otp.wrapper;

import com.kupi.otp.domain.BaseDto;
import com.kupi.otp.enums.ResponseStatusType;
import lombok.Getter;
import lombok.Setter;

/**
 * Send email wrapper to get email send response from util service
 */
@Getter
@Setter
public class SendEmailWrapper implements BaseDto {

    private ResponseStatusType status;
    private String message;
    private String displayMessage;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
