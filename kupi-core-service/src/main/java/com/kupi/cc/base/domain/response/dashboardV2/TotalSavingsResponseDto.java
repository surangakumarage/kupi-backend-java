package com.kupi.cc.base.domain.response.dashboardV2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Total Savings Response DTO
 * Represents total savings for all users or a specific user
 */
@Getter
@Setter
@NoArgsConstructor
public class TotalSavingsResponseDto extends ResponseDto {

    private BigDecimal totalSavings;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userId; // null for ALL users, userId for specific user

    public TotalSavingsResponseDto(BigDecimal totalSavings, String userId) {
        this.totalSavings = totalSavings != null ? totalSavings.setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.userId = userId;
    }
}

