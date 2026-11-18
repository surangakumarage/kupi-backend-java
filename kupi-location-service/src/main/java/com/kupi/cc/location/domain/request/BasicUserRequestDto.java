package com.kupi.cc.location.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Basic user request Dto
 */
@Getter
@Setter
@AllArgsConstructor
public class BasicUserRequestDto extends RequestDto {

    private String id;
    private String imageUrl;


    @Override
    public String toLogJson() {
        return toJson();
    }
}