package com.kupi.cc.base.wrapper;

import com.kupi.cc.base.domain.BaseDto;
import com.kupi.cc.base.domain.response.BulkUserResponseDto;
import com.kupi.cc.base.enums.ResponseStatusType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BulkUserResponseWrapper implements BaseDto {

    private ResponseStatusType status;
    private String message;
    private BulkUserResponseDto data;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
