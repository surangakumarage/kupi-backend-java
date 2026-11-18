package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for payment statistics/counts
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatisticsResponseDto extends ResponseDto {

    private long totalPayments;      // Total count of all payments
    private long successfulPayments; // Count of successful payments only
    private long failedPayments;     // Count of failed payments only
}
