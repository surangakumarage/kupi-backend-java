package com.kupi.cc.location.domain.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class BulkMerchantDealsResponseDto implements ResponseDto {

    private List<MerchantDealsResponseDto> dealGroups = new ArrayList<>();

    @Override
    public String toLogJson() {
        return toJson();
    }
}