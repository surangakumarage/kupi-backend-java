package com.kupi.cc.location.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Basic Category Response DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BasicCategoryResponseDto implements ResponseDto {
    private String id;
    private String name;
    private String imageUrl;

    @Override
    public String toLogJson() {
        return toJson();
    }
}

