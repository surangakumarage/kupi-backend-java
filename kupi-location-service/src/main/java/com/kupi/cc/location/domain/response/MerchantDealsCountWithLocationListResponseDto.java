package com.kupi.cc.location.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Merchant Deals Count with Location List ResponseDto with Pagination
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantDealsCountWithLocationListResponseDto implements ResponseDto {

    private List<MerchantDealsCountWithLocationResponseDto> deals;
    private Double searchRadiusKm;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;

    @Override
    public String toLogJson() {
        return toJson();
    }
}

