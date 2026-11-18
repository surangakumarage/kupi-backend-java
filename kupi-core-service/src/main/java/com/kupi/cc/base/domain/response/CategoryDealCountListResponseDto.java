package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * List of categories with active deal counts response DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDealCountListResponseDto extends ResponseDto {

    private List<CategoryDealCountResponseDto> categories;

    @Override
    public String toLogJson() {
        return toJson();
    }
}

