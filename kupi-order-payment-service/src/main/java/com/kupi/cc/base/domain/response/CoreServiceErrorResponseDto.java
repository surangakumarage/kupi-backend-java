package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoreServiceErrorResponseDto extends ResponseDto{
    private String status;
    private String message;
    private String data;
    private String displayMessage;
    private int errorCode;
}
