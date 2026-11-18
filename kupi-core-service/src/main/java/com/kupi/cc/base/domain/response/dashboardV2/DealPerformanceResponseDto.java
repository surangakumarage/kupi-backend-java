package com.kupi.cc.base.domain.response.dashboardV2;

import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Deal Performance Response DTO
 * Represents performance metrics for a single deal
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DealPerformanceResponseDto extends ResponseDto {

    private String dealId;
    private LocalDate createdDate;
    private String dealName;
    private long views;
    private long sold;
    private long redemptions;
    private double conversionRate;
    private BigDecimal earnings;
}

