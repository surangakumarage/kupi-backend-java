package com.kupi.cc.base.domain.request;

import lombok.Getter;
import lombok.Setter;

/**
 * CouponFileUploadRequestDto
 */
@Getter
@Setter
public class CouponFileUploadRequestDto extends RequestDto {

    private String fileUrl;
    private String dealId;
    private String dealCode;
    private String fileName;

    @Override
    public String toLogJson() {
        return toJson();
    }

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(fileUrl) && isNonEmpty(dealId) && isNonEmpty(dealCode);
    }
}
