package com.kupi.cc.auth.domain.response;

import com.kupi.cc.auth.domain.entity.BusinessMenuImage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MenuImageResponseDto implements ResponseDto {
    private  String menuImageUrl;

    public MenuImageResponseDto(BusinessMenuImage businessMenuImage) {
        this.menuImageUrl = businessMenuImage.getMenuImageUrl();
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
