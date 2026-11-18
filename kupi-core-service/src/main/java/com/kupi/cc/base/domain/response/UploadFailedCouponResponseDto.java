package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.CouponStatisticDetail;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadFailedCouponResponseDto extends ResponseDto {

    private String couponCode;
    private String reason;

    public UploadFailedCouponResponseDto(CouponStatisticDetail couponStatisticDetail) {
        this.couponCode = couponStatisticDetail.getCouponCode();
        this.reason = couponStatisticDetail.getRejectedType().getReason();
    }
}
