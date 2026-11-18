package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.PaymentHistory;
import com.kupi.cc.base.domain.entity.UserSubscriptionPlan;
import com.kupi.cc.base.domain.response.PaymentStatisticsResponseDto;
import com.kupi.cc.base.enums.DateRangeFilter;
import com.kupi.cc.base.enums.PaymentStatus;
import com.kupi.cc.base.repository.PaymentHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for managing payment history
 */
@Service
@Transactional
@Slf4j
public class PaymentHistoryService {

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final UserSubscriptionPlanService userSubscriptionPlanService;

    public PaymentHistoryService(PaymentHistoryRepository paymentHistoryRepository,
                               UserSubscriptionPlanService userSubscriptionPlanService) {
        this.paymentHistoryRepository = paymentHistoryRepository;
        this.userSubscriptionPlanService = userSubscriptionPlanService;
    }

    /**
     * Save successful payment history
     */
    public void saveSuccessfulPayment(String userId, String planId, double amount, String transactionId) {
        savePaymentHistory(userId, planId, amount, PaymentStatus.SUCCESSFUL, transactionId, null);
    }

    /**
     * Save failed payment history
     */
    public void saveFailedPayment(String userId, String planId, double amount, String transactionId, String failureReason) {
        savePaymentHistory(userId, planId, amount, PaymentStatus.FAILED, transactionId, failureReason);
    }

    /**
     * Generic method to save payment history
     */
    private void savePaymentHistory(String userId, String planId, double amount,
                                   PaymentStatus status, String transactionId,
                                   String failureReason) {
        try {
            UserSubscriptionPlan plan = userSubscriptionPlanService.getUserSubscriptionPlanById(planId);

            PaymentHistory payment = new PaymentHistory();
            payment.setId("ph-" + UUID.randomUUID());
            payment.setUserId(userId);
            payment.setSubscriptionPlanName(plan.getName());
            payment.setAmount(amount);
            payment.setPaymentDate(System.currentTimeMillis());
            payment.setStatus(status);
            payment.setTransactionId(transactionId);
            payment.setFailureReason(failureReason);
            long now = System.currentTimeMillis();
            payment.setCreatedAt(now);
            payment.setUpdatedAt(now);

            paymentHistoryRepository.save(payment);
            log.info("Payment history saved for userId: {}, status: {}", userId, status);
        } catch (Exception e) {
            log.error("Failed to save payment history for userId: {}", userId, e);
        }
    }

    /**
     * Get all payment history for user
     */
    @Transactional(readOnly = true)
    public Page<PaymentHistory> getUserPaymentHistory(String userId, Pageable pageable) {
        return paymentHistoryRepository.findByUserIdOrderByPaymentDateDesc(userId, pageable);
    }

    /**
     * Get payment history by status
     */
    @Transactional(readOnly = true)
    public Page<PaymentHistory> getUserPaymentHistoryByStatus(String userId, PaymentStatus status, Pageable pageable) {
        return paymentHistoryRepository.findByUserIdAndStatusOrderByPaymentDateDesc(userId, status, pageable);
    }

    /**
     * Get payment statistics for user
     */
    @Transactional(readOnly = true)
    public PaymentStatisticsResponseDto getUserPaymentStatistics(String userId) {
        long totalPayments = paymentHistoryRepository.countByUserId(userId);
        long successfulPayments = paymentHistoryRepository.countByUserIdAndStatus(userId, PaymentStatus.SUCCESSFUL);
        long failedPayments = paymentHistoryRepository.countByUserIdAndStatus(userId, PaymentStatus.FAILED);

        return new PaymentStatisticsResponseDto(totalPayments, successfulPayments, failedPayments);
    }

    /**
     * Get all payment history for user with date range filter
     */
    @Transactional(readOnly = true)
    public Page<PaymentHistory> getUserPaymentHistoryWithDateRange(String userId, DateRangeFilter dateRange, Pageable pageable) {
        long startDate = dateRange.getStartDateTimestamp();

        if (startDate == 0) {  // ALL_TIME - no filter
            return paymentHistoryRepository.findByUserIdOrderByPaymentDateDesc(userId, pageable);
        }

        return paymentHistoryRepository.findByUserIdAndDateRangeOrderByPaymentDateDesc(userId, startDate, pageable);
    }

    /**
     * Get payment history by status with date range filter
     */
    @Transactional(readOnly = true)
    public Page<PaymentHistory> getUserPaymentHistoryByStatusWithDateRange(String userId, PaymentStatus status, DateRangeFilter dateRange, Pageable pageable) {
        long startDate = dateRange.getStartDateTimestamp();

        if (startDate == 0) {  // ALL_TIME - no filter
            return paymentHistoryRepository.findByUserIdAndStatusOrderByPaymentDateDesc(userId, status, pageable);
        }

        return paymentHistoryRepository.findByUserIdAndStatusAndDateRangeOrderByPaymentDateDesc(userId, status, startDate, pageable);
    }

    /**
     * Get payment statistics for user with date range filter
     */
    @Transactional(readOnly = true)
    public PaymentStatisticsResponseDto getUserPaymentStatisticsWithDateRange(String userId, DateRangeFilter dateRange) {
        long startDate = dateRange.getStartDateTimestamp();

        long totalPayments;
        long successfulPayments;
        long failedPayments;

        if (startDate == 0) {  // ALL_TIME - no filter
            totalPayments = paymentHistoryRepository.countByUserId(userId);
            successfulPayments = paymentHistoryRepository.countByUserIdAndStatus(userId, PaymentStatus.SUCCESSFUL);
            failedPayments = paymentHistoryRepository.countByUserIdAndStatus(userId, PaymentStatus.FAILED);
        } else {
            totalPayments = paymentHistoryRepository.countByUserIdAndDateRange(userId, startDate);
            successfulPayments = paymentHistoryRepository.countByUserIdAndStatusAndDateRange(userId, PaymentStatus.SUCCESSFUL, startDate);
            failedPayments = paymentHistoryRepository.countByUserIdAndStatusAndDateRange(userId, PaymentStatus.FAILED, startDate);
        }

        return new PaymentStatisticsResponseDto(totalPayments, successfulPayments, failedPayments);
    }
}
