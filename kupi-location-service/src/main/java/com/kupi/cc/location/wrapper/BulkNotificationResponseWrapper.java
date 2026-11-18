package com.kupi.cc.location.wrapper;

import com.kupi.cc.location.domain.BaseDto;
import com.kupi.cc.location.enums.ResponseStatusType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class BulkNotificationResponseWrapper implements BaseDto {

    private ResponseStatusType status;
    private String message;


    @Override
    public String toLogJson() {
        return toJson();
    }
}