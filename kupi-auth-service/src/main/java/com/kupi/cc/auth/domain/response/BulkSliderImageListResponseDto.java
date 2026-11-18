package com.kupi.cc.auth.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bulk slider image list response dto.
 * Maps merchantId to list of slider images.
 */
@Getter
@Setter
public class BulkSliderImageListResponseDto implements ResponseDto {

    private Map<String, List<SliderImageItemResponseDto>> merchantSliderImages = new HashMap<>();

    public BulkSliderImageListResponseDto() {
    }

    public BulkSliderImageListResponseDto(Map<String, List<SliderImageItemResponseDto>> merchantSliderImages) {
        this.merchantSliderImages = merchantSliderImages != null ? merchantSliderImages : new HashMap<>();
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}


