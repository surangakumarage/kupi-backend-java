package com.kupi.cc.auth.domain.response;

import lombok.Getter;

import java.util.List;

/**
 * All grouped profile views response dto
 */
@Getter
public class AllGroupedProfileViewsResponseDto extends PageResponseDto {

    private final List<GroupedProfileViewsResponseDto> views;

    public AllGroupedProfileViewsResponseDto(int totalItems, int page, int size,
                                             List<GroupedProfileViewsResponseDto> views) {
        super(totalItems, page, size);
        this.views = views;
    }
}
