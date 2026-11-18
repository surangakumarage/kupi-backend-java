package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Deal;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.List;

@Getter
@Setter
public class CouponRedeemSuccessResponseDto extends ResponseDto {
    String dealId;
    String dealCode;
    String dealTitle;
    String dealSubTitle;
    List<String> imageUrls;
    BasicMerchantBusinessResponseDto collectedBy;
    DateResponseDto couponRedeemDate;

    public CouponRedeemSuccessResponseDto(Deal deal, BasicMerchantBusinessResponseDto user, Long couponRedeemTime, String timeZone) {
        this.dealId = deal.getId();
        this.dealCode = deal.getDealCode();
        this.dealTitle = deal.getTitle();
        this.dealSubTitle = deal.getSubTitle();
        this.imageUrls = deal.getImageUrls();
        this.collectedBy = user;
        this.couponRedeemDate = new DateResponseDto(couponRedeemTime, timeZone, new Date(couponRedeemTime));
    }
}

