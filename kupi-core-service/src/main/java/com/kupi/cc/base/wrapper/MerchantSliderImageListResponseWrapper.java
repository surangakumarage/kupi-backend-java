package com.kupi.cc.base.wrapper;

import com.kupi.cc.base.domain.BaseDto;
import com.kupi.cc.base.domain.response.MerchantSliderImageListResponseDto;
import com.kupi.cc.base.enums.ResponseStatusType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Wrapper for slider image list responses coming from auth-service.
 */
@Getter
@Setter
@NoArgsConstructor
public class MerchantSliderImageListResponseWrapper implements BaseDto {

    private ResponseStatusType status;
    private String message;
    private MerchantSliderImageListResponseDto data;
    private String displayMessage;

    @Override
    public String toLogJson() {
        return toJson();
    }
}


