package com.kupi.cc.base.domain.response.dashboardV2;

import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Top performing deals response DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopPerformingDealsResponseDto extends ResponseDto {
    private List<TopPerformingDealResponseDto> deals;
}

