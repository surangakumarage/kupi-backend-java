package com.kupi.cc.auth.domain.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MenuImageRequestDto extends RequestDto {

    private String merchantId;
    private String imageUrl;

    @Override
    public boolean isRequiredAvailable() {
        return merchantId != null && !merchantId.isEmpty();
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}

