package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Brand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BasicBrandResponseDto extends ResponseDto {

    private String id;
    private String name;

    public BasicBrandResponseDto(Brand brand) {
        id = brand.getId();
        name = brand.getName();
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
