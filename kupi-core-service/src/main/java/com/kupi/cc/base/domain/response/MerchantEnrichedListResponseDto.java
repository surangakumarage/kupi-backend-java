package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Enriched merchant list response dto for UI.
 * Contains merchants with categories, slider images and active deal counts.
 */
@Getter
@Setter
public class MerchantEnrichedListResponseDto extends PageResponseDto {

    private List<MerchantBusinessWithCategoriesResponseDto> merchants = new ArrayList<>();

    public MerchantEnrichedListResponseDto(List<MerchantBusinessWithCategoriesResponseDto> merchants, Page<?> page) {
        super(page);
        if (merchants != null) {
            this.merchants = merchants;
        }
    }

    /**
     * Constructor used when the merchant list has been post-filtered (e.g. removing zero-deal merchants).
     * In this case, totalItems should reflect the filtered list size, not the original page size.
     */
    public MerchantEnrichedListResponseDto(List<MerchantBusinessWithCategoriesResponseDto> merchants, int page, int size) {
        super(merchants != null ? merchants.size() : 0, page, size);
        if (merchants != null) {
            this.merchants = merchants;
        }
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}


