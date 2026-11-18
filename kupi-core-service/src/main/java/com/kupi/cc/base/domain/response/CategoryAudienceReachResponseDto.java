package com.kupi.cc.base.domain.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Category audience reach response Dto
 */
@Getter
public class CategoryAudienceReachResponseDto extends PageResponseDto {

    private final List<CategoryViewCountResponseDto> views;

    public CategoryAudienceReachResponseDto(Page<CategoryViewCountResponseDto> page,
                                            List<CategoryViewCountResponseDto> categoryViews) {
        super(page);
        this.views = categoryViews;
    }

    public CategoryAudienceReachResponseDto(int totalItems, int page, int size,
                                            List<CategoryViewCountResponseDto> categoryViews) {
        super(totalItems, page, size);
        this.views = categoryViews;
    }
}
