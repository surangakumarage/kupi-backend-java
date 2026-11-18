package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Extended Deal List Response DTO
 */
@Getter
@Setter
public class ExtendedDealListResponseDto extends PageResponseDto {

    private final List<ExtendedDealResponseDto> deals;

    public ExtendedDealListResponseDto(Page<?> dealPage, List<ExtendedDealResponseDto> deals) {
        super(dealPage);
        this.deals = deals != null ? deals : new ArrayList<>();
    }
}

