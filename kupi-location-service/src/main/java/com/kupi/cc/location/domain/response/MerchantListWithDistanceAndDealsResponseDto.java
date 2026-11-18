package com.kupi.cc.location.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Merchant List with Distance and Active Deals Count Response DTO with Pagination
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantListWithDistanceAndDealsResponseDto implements ResponseDto {

    private List<MerchantWithDistanceAndDealsResponseDto> merchants;
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

