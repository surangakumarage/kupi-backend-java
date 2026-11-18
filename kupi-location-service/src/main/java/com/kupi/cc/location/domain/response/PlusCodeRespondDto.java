package com.kupi.cc.location.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PlusCodeRespondDto implements ResponseDto {

    private String compound_code;
    private String global_code;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
