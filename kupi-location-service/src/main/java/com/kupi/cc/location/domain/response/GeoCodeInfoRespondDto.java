package com.kupi.cc.location.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GeoCodeInfoRespondDto implements ResponseDto {

    private PlusCodeRespondDto plus_code;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
