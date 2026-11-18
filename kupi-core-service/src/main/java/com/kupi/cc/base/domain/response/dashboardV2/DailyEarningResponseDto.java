package com.kupi.cc.base.domain.response.dashboardV2;

import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Daily earning response DTO for chart data
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyEarningResponseDto extends ResponseDto {
    private LocalDate date;
    private BigDecimal earnings;
}

