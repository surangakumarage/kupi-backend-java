package com.kupi.cc.base.wrapper;


import com.kupi.cc.base.domain.BaseDto;
import com.kupi.cc.base.domain.response.AllMerchantListResponseDto;
import com.kupi.cc.base.enums.ResponseStatusType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response wrapper for all merchant list from auth service
 */
@Getter
@Setter
@NoArgsConstructor
public class AllMerchantListResponseWrapper implements BaseDto {

    private ResponseStatusType status;
    private String message;
    private AllMerchantListResponseDto data;
    private String displayMessage;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
