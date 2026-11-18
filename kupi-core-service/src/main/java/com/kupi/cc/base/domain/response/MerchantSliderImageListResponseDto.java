package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Slider image list response dto for a merchant.
 * Mirrors the auth-service SliderImageListResponseDto payload.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantSliderImageListResponseDto extends ResponseDto {

    private String merchantId;
    private List<MerchantSliderImageItemResponseDto> images;

    @Override
    public String toLogJson() {
        return toJson();
    }
}


