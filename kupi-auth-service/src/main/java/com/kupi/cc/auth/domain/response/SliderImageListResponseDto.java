package com.kupi.cc.auth.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SliderImageListResponseDto implements ResponseDto {

    private String merchantId;
    private List<SliderImageItemResponseDto> images;

    public SliderImageListResponseDto(String merchantId, List<SliderImageItemResponseDto> images) {
        this.merchantId = merchantId;
        this.images = images;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}

