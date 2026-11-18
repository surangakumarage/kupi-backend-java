package com.kupi.cc.base.wrapper;

import com.kupi.cc.base.domain.BaseDto;
import com.kupi.cc.base.domain.response.BulkMerchantBusinessResponseDto;
import com.kupi.cc.base.enums.ResponseStatusType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BulkMerchantResponseWrapper implements BaseDto {

    private ResponseStatusType status;
    private String message;
    private BulkMerchantBusinessResponseDto data;

    @Override
    public String toLogJson() {
        return toJson();
    }
}