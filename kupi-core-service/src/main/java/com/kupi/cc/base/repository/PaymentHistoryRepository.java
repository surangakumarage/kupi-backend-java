package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.PaymentHistory;
import com.kupi.cc.base.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for PaymentHistory entity
 */
@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, String> {

    Page<PaymentHistory> findByUserIdOrderByPaymentDateDesc(String userId, Pageable pageable);

    Page<PaymentHistory> findByUserIdAndStatusOrderByPaymentDateDesc(String userId, PaymentStatus status, Pageable pageable);

    long countByUserId(String userId);

    long countByUserIdAndStatus(String userId, PaymentStatus status);

    // Date range filtering methods
    @Query("SELECT p FROM PaymentHistory p WHERE p.userId = :userId AND p.paymentDate >= :startDate ORDER BY p.paymentDate DESC")
    Page<PaymentHistory> findByUserIdAndDateRangeOrderByPaymentDateDesc(
            @Param("userId") String userId,
            @Param("startDate") long startDate,
            Pageable pageable);

    @Query("SELECT p FROM PaymentHistory p WHERE p.userId = :userId AND p.status = :status AND p.paymentDate >= :startDate ORDER BY p.paymentDate DESC")
    Page<PaymentHistory> findByUserIdAndStatusAndDateRangeOrderByPaymentDateDesc(
            @Param("userId") String userId,
            @Param("status") PaymentStatus status,
            @Param("startDate") long startDate,
            Pageable pageable);

    @Query("SELECT COUNT(p) FROM PaymentHistory p WHERE p.userId = :userId AND p.paymentDate >= :startDate")
    long countByUserIdAndDateRange(@Param("userId") String userId, @Param("startDate") long startDate);

    @Query("SELECT COUNT(p) FROM PaymentHistory p WHERE p.userId = :userId AND p.status = :status AND p.paymentDate >= :startDate")
    long countByUserIdAndStatusAndDateRange(
            @Param("userId") String userId,
            @Param("status") PaymentStatus status,
            @Param("startDate") long startDate);
}
