package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UploadCouponFileDetailResponseDto extends ResponseDto {

    private List<String> successCoupons;
    private List<UploadFailedCouponResponseDto> failedCoupons;
    private int uploadSuccessCount;
    private int uploadFailedCount;
    private int totalCouponCount;

    public UploadCouponFileDetailResponseDto(List<String> successCoupons, List<UploadFailedCouponResponseDto> failedCoupons) {
        this.successCoupons = successCoupons;
        this.failedCoupons = failedCoupons;
        this.uploadSuccessCount = successCoupons.size();
        this.uploadFailedCount = failedCoupons.size();
        this.totalCouponCount = uploadSuccessCount + uploadFailedCount;
    }
}
