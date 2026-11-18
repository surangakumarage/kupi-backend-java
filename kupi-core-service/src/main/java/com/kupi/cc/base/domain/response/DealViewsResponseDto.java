package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.DealSearchIndex;
import lombok.Getter;
import lombok.Setter;

/**
 * Deal views response Dto
 */
@Setter
@Getter
public class DealViewsResponseDto extends ResponseDto {

    private DealReportResponseDto deal;
    private MerchantBusinessResponseDto merchant;
    private long viewCount;

    public DealViewsResponseDto(DealSearchIndex dealSearchIndex, MerchantBusinessResponseDto merchant, long viewCount) {
        this.deal = new DealReportResponseDto(dealSearchIndex);
        this.merchant = merchant;
        this.viewCount = viewCount;
    }

    public DealViewsResponseDto(DealSearchIndex dealSearchIndex, long viewCount) {
        this.deal = new DealReportResponseDto(dealSearchIndex);
        this.viewCount = viewCount;
    }
}
