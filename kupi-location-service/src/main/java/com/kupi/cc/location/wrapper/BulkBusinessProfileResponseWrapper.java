package com.kupi.cc.location.wrapper;

import com.kupi.cc.location.domain.BaseDto;
import com.kupi.cc.location.domain.response.BulkBusinessResponseDto;
import com.kupi.cc.location.enums.ResponseStatusType;
import lombok.Getter;
import lombok.Setter;

/**
 * Bulk Business Profile Response Wrapper
 */
@Getter
@Setter
public class BulkBusinessProfileResponseWrapper implements BaseDto {

    private ResponseStatusType status;
    private String message;
    private BulkBusinessResponseDto data;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
