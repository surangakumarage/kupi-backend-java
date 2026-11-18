package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.DealSearchIndex;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DealViewsWithDateResponseDto extends DealViewsResponseDto {

    private String displayDate;

    public DealViewsWithDateResponseDto(DealSearchIndex dealSearchIndex, long viewCount, String displayDate) {
        super(dealSearchIndex, viewCount);
        this.displayDate = displayDate;
    }
}
