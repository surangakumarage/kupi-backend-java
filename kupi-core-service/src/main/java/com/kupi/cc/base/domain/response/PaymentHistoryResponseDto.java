package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.PaymentHistory;
import com.kupi.cc.base.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Response DTO for payment history
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryResponseDto extends ResponseDto {

    private String id;
    private String planName;
    private double amount;
    private long paymentDate;
    private String formattedDate;  // Formatted date based on timezone
    private PaymentStatus status;
    private String failureReason;

    public PaymentHistoryResponseDto(PaymentHistory paymentHistory) {
        this.id = paymentHistory.getId();
        this.planName = paymentHistory.getSubscriptionPlanName();
        this.amount = paymentHistory.getAmount();
        this.paymentDate = paymentHistory.getPaymentDate();
        this.status = paymentHistory.getStatus();
        this.failureReason = paymentHistory.getFailureReason();
    }

    /**
     * Constructor with timezone formatting
     * Formats paymentDate as "MMM dd, yyyy • hh:mm a" based on timezone
     */
    public PaymentHistoryResponseDto(PaymentHistory paymentHistory, String timeZoneId) {
        this(paymentHistory);
        this.formattedDate = formatPaymentDate(paymentHistory.getPaymentDate(), timeZoneId);
    }

    /**
     * Format payment date based on timezone
     * Example: "Jan 20, 2026 • 2:30 PM" (Asia/Colombo)
     */
    private static String formatPaymentDate(long paymentDateMillis, String timeZoneId) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a");
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
            sdf.setTimeZone(timeZone);
            return sdf.format(new Date(paymentDateMillis));
        } catch (Exception e) {
            return null;
        }
    }
}
