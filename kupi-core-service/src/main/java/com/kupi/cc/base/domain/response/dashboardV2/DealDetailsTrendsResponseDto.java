package com.kupi.cc.base.domain.response.dashboardV2;

import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Deal details trends response DTO for chart data (sold vs redeemed per day).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DealDetailsTrendsResponseDto extends ResponseDto {

    private List<DailyDealDetailResponseDto> dailyDealDetails;
}


