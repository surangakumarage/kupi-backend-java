package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.PaymentHistory;
import com.kupi.cc.base.domain.response.PaymentHistoryPageResponseDto;
import com.kupi.cc.base.domain.response.PaymentHistoryResponseDto;
import com.kupi.cc.base.domain.response.PaymentStatisticsResponseDto;
import com.kupi.cc.base.enums.DateRangeFilter;
import com.kupi.cc.base.enums.PaymentStatus;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.service.PaymentHistoryService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * REST Controller for payment history endpoints
 */
@Slf4j
@RestController
@RequestMapping("api/v1/payment-history")
public class PaymentHistoryController extends Controller {

    private final PaymentHistoryService paymentHistoryService;

    public PaymentHistoryController(Translator translator, PaymentHistoryService paymentHistoryService) {
        super(translator);
        this.paymentHistoryService = paymentHistoryService;
    }

    /**
     * Get all payment history for the logged-in user with statistics
     * Endpoint: GET /api/v1/payment-history
     * Headers: User-Id, Time-Zone (e.g., Asia/Colombo)
     * Query params: page (0-indexed), size, dateRange (ALL_TIME, LAST_7_DAYS, LAST_30_DAYS, LAST_3_MONTHS, LAST_6_MONTHS, LAST_YEAR)
     */
    @GetMapping
    public ResponseEntity<ResponseWrapper> getPaymentHistory(@RequestHeader("User-Id") String userId,
                                                            @RequestHeader(value = TIME_ZONE_HEADER, required = false, defaultValue = "UTC") String timeZone,
                                                            @RequestParam(value = "dateRange", required = false, defaultValue = "ALL_TIME") String dateRange,
                                                            Pageable pageable) {
        try {
            DateRangeFilter filter = DateRangeFilter.fromString(dateRange);
            Page<PaymentHistory> paymentHistoryPage = paymentHistoryService.getUserPaymentHistoryWithDateRange(userId, filter, pageable);
            PaymentStatisticsResponseDto statistics = paymentHistoryService.getUserPaymentStatisticsWithDateRange(userId, filter);

            // Convert to response DTOs with timezone formatting
            Page<PaymentHistoryResponseDto> responsePage = paymentHistoryPage
                    .map(payment -> new PaymentHistoryResponseDto(payment, timeZone));

            PaymentHistoryPageResponseDto response = PaymentHistoryPageResponseDto.from(responsePage, statistics);
            response.setPayments(responsePage.getContent());

            return getSuccessResponse(response, SuccessResponseStatusType.SUCCESSFULLY_RETURNED, "Payment History Loaded Successfully");
        } catch (Exception e) {
            log.error("Failed to fetch payment history for userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Get payment statistics (counts) for the user
     * Endpoint: GET /api/v1/payment-history/stats
     * Query params: dateRange (ALL_TIME, LAST_7_DAYS, LAST_30_DAYS, LAST_3_MONTHS, LAST_6_MONTHS, LAST_YEAR)
     * Returns: {totalPayments: 8, successfulPayments: 6, failedPayments: 2}
     */
    @GetMapping("/stats")
    public ResponseEntity<ResponseWrapper> getPaymentStatistics(@RequestHeader("User-Id") String userId,
                                                               @RequestParam(value = "dateRange", required = false, defaultValue = "ALL_TIME") String dateRange) {
        try {
            DateRangeFilter filter = DateRangeFilter.fromString(dateRange);
            PaymentStatisticsResponseDto statistics = paymentHistoryService.getUserPaymentStatisticsWithDateRange(userId, filter);
            return getSuccessResponse(statistics, SuccessResponseStatusType.SUCCESSFULLY_RETURNED, "Payment Statistics Retrieved Successfully");
        } catch (Exception e) {
            log.error("Failed to fetch payment statistics for userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Get payment history filtered by status
     * Endpoint: GET /api/v1/payment-history/filter?status=SUCCESSFUL
     * Headers: User-Id, Time-Zone (e.g., Asia/Colombo)
     * Query params: status (SUCCESSFUL, FAILED), page, size, dateRange (ALL_TIME, LAST_7_DAYS, LAST_30_DAYS, LAST_3_MONTHS, LAST_6_MONTHS, LAST_YEAR)
     */
    @GetMapping("/filter")
    public ResponseEntity<ResponseWrapper> getPaymentHistoryByStatus(@RequestHeader("User-Id") String userId,
                                                                    @RequestHeader(value = TIME_ZONE_HEADER, required = false, defaultValue = "UTC") String timeZone,
                                                                    @RequestParam PaymentStatus status,
                                                                    @RequestParam(value = "dateRange", required = false, defaultValue = "ALL_TIME") String dateRange,
                                                                    Pageable pageable) {
        try {
            DateRangeFilter filter = DateRangeFilter.fromString(dateRange);
            Page<PaymentHistory> paymentHistoryPage = paymentHistoryService.getUserPaymentHistoryByStatusWithDateRange(userId, status, filter, pageable);
            PaymentStatisticsResponseDto statistics = paymentHistoryService.getUserPaymentStatisticsWithDateRange(userId, filter);

            Page<PaymentHistoryResponseDto> responsePage = paymentHistoryPage
                    .map(payment -> new PaymentHistoryResponseDto(payment, timeZone));

            PaymentHistoryPageResponseDto response = PaymentHistoryPageResponseDto.from(responsePage, statistics);
            response.setPayments(responsePage.getContent());

            String displayMessage = status.equals(PaymentStatus.SUCCESSFUL)
                ? "Successful Payments Retrieved Successfully"
                : "Failed Payments Retrieved Successfully";
            return getSuccessResponse(response, SuccessResponseStatusType.SUCCESSFULLY_RETURNED, displayMessage);
        } catch (Exception e) {
            log.error("Failed to fetch payment history by status for userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Get successful payments only
     * Endpoint: GET /api/v1/payment-history/successful
     * Headers: User-Id, Time-Zone (e.g., Asia/Colombo)
     * Query params: page, size, dateRange (ALL_TIME, LAST_7_DAYS, LAST_30_DAYS, LAST_3_MONTHS, LAST_6_MONTHS, LAST_YEAR)
     */
    @GetMapping("/successful")
    public ResponseEntity<ResponseWrapper> getSuccessfulPayments(@RequestHeader("User-Id") String userId,
                                                                @RequestHeader(value = TIME_ZONE_HEADER, required = false, defaultValue = "UTC") String timeZone,
                                                                @RequestParam(value = "dateRange", required = false, defaultValue = "ALL_TIME") String dateRange,
                                                                Pageable pageable) {
        return getPaymentHistoryByStatus(userId, timeZone, PaymentStatus.SUCCESSFUL, dateRange, pageable);
    }

    /**
     * Get failed payments only
     * Endpoint: GET /api/v1/payment-history/failed
     * Headers: User-Id, Time-Zone (e.g., Asia/Colombo)
     * Query params: page, size, dateRange (ALL_TIME, LAST_7_DAYS, LAST_30_DAYS, LAST_3_MONTHS, LAST_6_MONTHS, LAST_YEAR)
     */
    @GetMapping("/failed")
    public ResponseEntity<ResponseWrapper> getFailedPayments(@RequestHeader("User-Id") String userId,
                                                            @RequestHeader(value = TIME_ZONE_HEADER, required = false, defaultValue = "UTC") String timeZone,
                                                            @RequestParam(value = "dateRange", required = false, defaultValue = "ALL_TIME") String dateRange,
                                                            Pageable pageable) {
        return getPaymentHistoryByStatus(userId, timeZone, PaymentStatus.FAILED, dateRange, pageable);
    }
}
