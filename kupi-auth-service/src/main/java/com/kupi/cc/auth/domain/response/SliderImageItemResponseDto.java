package com.kupi.cc.auth.domain.response;

import com.kupi.cc.auth.domain.entity.BusinessSliderImage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SliderImageItemResponseDto {

    private String url;
    private int position;

    public SliderImageItemResponseDto(BusinessSliderImage entity) {
        this.url = entity.getImageUrl();
        this.position = entity.getDisplayOrder();
    }
}
