package com.kupi.cc.auth.domain.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SliderImageCreateRequestDto {

    private String merchantId;
    private List<SliderImageItemDto> images;

    public boolean isRequiredAvailable() {
        return merchantId != null &&
                !merchantId.isEmpty() &&
                images != null &&
                !images.isEmpty();
    }
}
