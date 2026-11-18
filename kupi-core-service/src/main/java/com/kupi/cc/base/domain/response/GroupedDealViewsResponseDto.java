package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.DealSearchIndex;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupedDealViewsResponseDto extends ResponseDto {

    private DealReportResponseDto deal;
    private MerchantBusinessResponseDto merchant;
    private long totalViewCount;

    public GroupedDealViewsResponseDto(DealSearchIndex dealSearchIndex, MerchantBusinessResponseDto merchant,
                                       long viewCount) {
        this.deal = new DealReportResponseDto(dealSearchIndex);
        this.merchant = merchant;
        this.totalViewCount = viewCount;
    }
}
