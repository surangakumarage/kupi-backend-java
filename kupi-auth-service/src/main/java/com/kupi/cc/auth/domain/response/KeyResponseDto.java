package com.kupi.cc.auth.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Key response dto
 */
@AllArgsConstructor
@Getter
public class KeyResponseDto implements ResponseDto {

    private String key;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
