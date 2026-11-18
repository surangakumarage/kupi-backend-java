
package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Deal Validity Periods Response DTO
 * Contains list of validity periods and offer expiration date
 */
@Getter
@Setter
@NoArgsConstructor
public class DealValidityPeriodsResponseDto extends ResponseDto {

    private List<ValidityPeriodResponseDto> validityPeriods;
    private DateResponseDto offerExpiredOn; // Deal's expiredOn date

    /**
     * Constructor that creates response from validity periods and deal expiration
     *
     * @param validityPeriods list of validity period DTOs
     * @param expiredOn       deal expiration timestamp in milliseconds
     * @param timeZone        timezone for date formatting
     */
    public DealValidityPeriodsResponseDto(List<com.kupi.cc.base.domain.request.ValidityPeriodDto> validityPeriods,
                                          long expiredOn, String timeZone) {
        this.validityPeriods = new ArrayList<>();

        if (validityPeriods != null) {
            for (com.kupi.cc.base.domain.request.ValidityPeriodDto period : validityPeriods) {
                this.validityPeriods.add(new ValidityPeriodResponseDto(period, timeZone));
            }
        }

        // Create DateResponseDto for offer expiration
        if (expiredOn > 0) {
            this.offerExpiredOn = new DateResponseDto(expiredOn, timeZone, new Date(expiredOn));
        }
    }
}