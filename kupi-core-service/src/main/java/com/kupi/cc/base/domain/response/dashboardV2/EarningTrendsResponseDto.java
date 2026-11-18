package com.kupi.cc.base.domain.response.dashboardV2;

import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Earning trends response DTO for chart data
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EarningTrendsResponseDto extends ResponseDto {
    private List<DailyEarningResponseDto> dailyEarnings;
}

