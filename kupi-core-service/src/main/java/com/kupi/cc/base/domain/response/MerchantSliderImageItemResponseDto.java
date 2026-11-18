package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Slider image item response dto for merchant profiles.
 * Mirrors the auth-service slider image item response shape.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantSliderImageItemResponseDto extends ResponseDto {

    private String url;
    private int position;

    @Override
    public String toLogJson() {
        return toJson();
    }
}


