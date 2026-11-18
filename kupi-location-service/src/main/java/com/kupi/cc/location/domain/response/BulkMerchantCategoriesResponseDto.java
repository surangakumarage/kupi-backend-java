package com.kupi.cc.location.domain.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bulk Merchant Categories Response DTO
 * Maps merchant ID to list of categories
 */
@Getter
@Setter
@NoArgsConstructor
public class BulkMerchantCategoriesResponseDto implements ResponseDto {

    private Map<String, List<BasicCategoryResponseDto>> merchantCategories;

    public BulkMerchantCategoriesResponseDto(Map<String, List<BasicCategoryResponseDto>> merchantCategories) {
        this.merchantCategories = merchantCategories != null ? merchantCategories : new HashMap<>();
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}

