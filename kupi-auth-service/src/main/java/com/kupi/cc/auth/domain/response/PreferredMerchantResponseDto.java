package com.kupi.cc.auth.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PreferredMerchantResponseDto implements ResponseDto {

    private List<String> preferredMerchantIds;
    private String userId;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
