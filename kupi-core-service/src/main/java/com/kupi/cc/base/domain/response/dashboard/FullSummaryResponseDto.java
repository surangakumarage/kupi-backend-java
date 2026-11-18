package com.kupi.cc.base.domain.response.dashboard;

import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Full summary response Dto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FullSummaryResponseDto extends ResponseDto {
    private TodaySummaryResponseDto todaySummary;
    private TotalSummaryResponseDto totalSummary;
}
