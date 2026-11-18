package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Category with active deal count response DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDealCountResponseDto extends ResponseDto {

    private String categoryId;
    private String categoryName;
    private String imageUrl;
    private long activeDealCount;

    @Override
    public String toLogJson() {
        return toJson();
    }
}

