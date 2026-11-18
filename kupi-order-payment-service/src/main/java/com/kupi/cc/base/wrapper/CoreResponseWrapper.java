package com.kupi.cc.base.wrapper;

import com.kupi.cc.base.domain.BaseDto;
import com.kupi.cc.base.domain.response.ResponseDto;
import com.kupi.cc.base.enums.ResponseStatusType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CoreResponseWrapper<T extends ResponseDto> implements BaseDto {
    private static final long serialVersionUID = -268017429964259806L;

    private ResponseStatusType status;
    private String message;
    private String displayMessage;
    private int errorCode;
    private T data;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
