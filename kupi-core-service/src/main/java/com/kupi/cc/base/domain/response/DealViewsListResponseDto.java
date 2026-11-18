package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Deal views list response Dto
 */
@Getter
@Setter
public class DealViewsListResponseDto extends PageResponseDto {

    private List<DealViewsWithDateResponseDto> views = new ArrayList<>();

    public DealViewsListResponseDto(Page<DealViewCountResponse> page) {
        super(page);
        for (DealViewCountResponse deals : page.getContent()) {
            views.add(new DealViewsWithDateResponseDto(deals.getDeal(), deals.getViewCount(), deals.getDisplayDate()));
        }
    }
}
