package com.kupi.cc.location.wrapper;

import com.kupi.cc.location.domain.BaseDto;
import com.kupi.cc.location.domain.response.BulkMerchantDealsResponseDto;
import com.kupi.cc.location.enums.ResponseStatusType;
import lombok.Getter;
import lombok.Setter;

/**
 * Response wrapper for bulk deal.
 */
@Getter
@Setter
public class BulkDealResponseWrapper implements BaseDto {

    private ResponseStatusType status;
    private String message;
    private BulkMerchantDealsResponseDto data;
    private String displayMessage;

    @Override
    public String toLogJson() {
        return toJson();
    }
}