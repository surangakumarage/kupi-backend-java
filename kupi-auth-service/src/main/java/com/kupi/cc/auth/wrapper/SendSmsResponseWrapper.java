package com.kupi.cc.auth.wrapper;

import com.kupi.cc.auth.domain.BaseDto;
import com.kupi.cc.auth.domain.response.ResponseDto;
import com.kupi.cc.auth.enums.ResponseStatusType;
import com.kupi.cc.auth.enums.SuccessResponseStatusType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendSmsResponseWrapper  implements BaseDto {

    private ResponseStatusType status;
    private String message;
    private String displayMessage;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
