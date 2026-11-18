package com.kupi.cc.location.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Deal list response DTO with same structure as core service's DealListResponseDto
 * (com.kupi.cc.base.domain.response.DealListResponseDto).
 * Used for /deals/filtered endpoint to return a flat list of deals with pagination.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DealListResponseDto implements ResponseDto {

    private List<Map<String, Object>> deals = new ArrayList<>();
    private long totalItems;
    private int totalPages;
    private int page;
    private int size;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
