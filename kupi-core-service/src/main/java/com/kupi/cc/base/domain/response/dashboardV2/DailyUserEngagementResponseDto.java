package com.kupi.cc.base.domain.response.dashboardV2;

import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Daily user engagement response DTO for chart data (views vs sales per day).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyUserEngagementResponseDto extends ResponseDto {

    private LocalDate date;
    private long views;
    private long sales;
}


