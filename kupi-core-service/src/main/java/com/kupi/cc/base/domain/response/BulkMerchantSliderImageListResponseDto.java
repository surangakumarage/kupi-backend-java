package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bulk merchant slider image list response dto for core-service.
 * Mirrors auth-service BulkSliderImageListResponseDto.
 */
@Getter
@Setter
@NoArgsConstructor
public class BulkMerchantSliderImageListResponseDto extends ResponseDto {

    private Map<String, List<MerchantSliderImageItemResponseDto>> merchantSliderImages = new HashMap<>();

    public BulkMerchantSliderImageListResponseDto(Map<String, List<MerchantSliderImageItemResponseDto>> merchantSliderImages) {
        this.merchantSliderImages = merchantSliderImages != null ? merchantSliderImages : new HashMap<>();
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}


