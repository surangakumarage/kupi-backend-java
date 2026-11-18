package com.kupi.cc.location.wrapper;

import com.kupi.cc.location.domain.BaseDto;
import com.kupi.cc.location.domain.response.BulkMerchantCategoriesResponseDto;
import com.kupi.cc.location.enums.ResponseStatusType;
import lombok.Getter;
import lombok.Setter;

/**
 * Response wrapper for bulk merchant categories.
 */
@Getter
@Setter
public class BulkMerchantCategoriesResponseWrapper implements BaseDto {

    private ResponseStatusType status;
    private String message;
    private BulkMerchantCategoriesResponseDto data;
    private String displayMessage;

    @Override
    public String toLogJson() {
        return toJson();
    }
}

