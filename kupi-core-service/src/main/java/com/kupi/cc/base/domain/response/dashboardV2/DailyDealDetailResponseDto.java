package com.kupi.cc.base.domain.response.dashboardV2;

import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Daily deal detail response DTO for chart data (sold vs redeemed per day).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyDealDetailResponseDto extends ResponseDto {

    private LocalDate date;
    private int soldCount;
    private int redeemedCount;
}


