package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Deal;
import lombok.Getter;
import lombok.Setter;

/**
 * BasicDealCouponResponseDto
 */
@Getter
@Setter
public class BasicDealCouponResponseDto extends ResponseDto {

    private String id;
    private String title;
    private String subTitle;
    private String coverImage;
    private String startTime;
    private String endTime;

    public BasicDealCouponResponseDto(Deal deal) {
        this.id = deal.getId();
        this.title = deal.getTitle();
        this.subTitle = deal.getSubTitle();
        this.coverImage = deal.getCoverImage();
        this.startTime = deal.getStartTime();
        this.endTime = deal.getEndTime();
    }
}
