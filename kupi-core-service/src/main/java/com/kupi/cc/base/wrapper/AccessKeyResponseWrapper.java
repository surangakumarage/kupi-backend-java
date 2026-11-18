package com.kupi.cc.base.wrapper;

import com.kupi.cc.base.domain.BaseDto;
import com.kupi.cc.base.domain.response.KeyResponseDto;
import com.kupi.cc.base.enums.ResponseStatusType;
import lombok.Getter;
import lombok.Setter;

/**
 * AccessKeyResponseWrapper
 */
@Getter
@Setter
public class AccessKeyResponseWrapper implements BaseDto {
    private ResponseStatusType status;
    private String message;
    private KeyResponseDto data;
    private String displayMessage;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
