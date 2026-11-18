package com.kupi.cc.location.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Nearby Active Deals Count Response DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NearbyActiveDealsCountResponseDto implements ResponseDto {

    /**
     * Total count of active deals within the search radius
     */
    private Long activeDealsCount;

    /**
     * Search radius in kilometers used for the query
     */
    private Double searchRadiusKm;

    @Override
    public String toLogJson() {
        return toJson();
    }
}

