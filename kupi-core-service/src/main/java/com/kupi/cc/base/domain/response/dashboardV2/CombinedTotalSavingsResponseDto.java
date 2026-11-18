package com.kupi.cc.base.domain.response.dashboardV2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kupi.cc.base.domain.response.ResponseDto;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Combined Total Savings Response DTO.
 * Returns both user-specific savings and total app savings in a single payload.
 */
@Getter
@Setter
@NoArgsConstructor
public class CombinedTotalSavingsResponseDto extends ResponseDto {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal userSavings;

    private BigDecimal totalAppSavings;

    public CombinedTotalSavingsResponseDto(BigDecimal userSavings, BigDecimal totalAppSavings) {
        this.userSavings = userSavings != null
                ? userSavings.setScale(2, java.math.RoundingMode.HALF_UP)
                : null;
        this.totalAppSavings = totalAppSavings != null
                ? totalAppSavings.setScale(2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);
    }
}


