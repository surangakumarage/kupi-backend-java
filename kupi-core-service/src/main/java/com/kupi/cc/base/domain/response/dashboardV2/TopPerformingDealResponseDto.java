package com.kupi.cc.base.domain.response.dashboardV2;

import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Top performing deal response DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopPerformingDealResponseDto extends ResponseDto {
    private int rank;
    private String dealId;
    private String dealName;
    private String category;
    private BigDecimal totalRevenue;
    private int redemptionCount;
}

