package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.CouponStatistics;
import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.CouponRepository;
import com.kupi.cc.base.repository.CouponStatisticsRepository;
import com.kupi.cc.base.repository.DealSearchIndexRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Scheduled job that periodically syncs remaining coupon counts for active deals.
 * <p>
 * This job runs every 15 minutes and:
 * - Fetches active deals for active merchants (same criteria as mobile active deals endpoint)
 * - Recalculates remaining coupons per deal based on non-expired, available coupons
 * - Updates CouponStatistics.remainingCoupons and DealSearchIndex.remainingCoupons
 */
@Slf4j
@Service
public class CouponExpiryScheduler {

    private static final String ALL = "ALL";

    private final DealService dealService;
    private final CouponRepository couponRepository;
    private final CouponStatisticsRepository couponStatisticsRepository;
    private final DealSearchIndexRepository dealSearchIndexRepository;

    public CouponExpiryScheduler(DealService dealService,
                                 CouponRepository couponRepository,
                                 CouponStatisticsRepository couponStatisticsRepository,
                                 DealSearchIndexRepository dealSearchIndexRepository) {
        this.dealService = dealService;
        this.couponRepository = couponRepository;
        this.couponStatisticsRepository = couponStatisticsRepository;
        this.dealSearchIndexRepository = dealSearchIndexRepository;
    }

    /**
     * Runs every 15 minutes (configurable via coupon.expiryUpdateCron) and
     * synchronizes remaining coupon counts for active deals.
     */
    @Scheduled(cron = "${coupon.expiryUpdateCron:0 */15 * * * *}")
    public void updateRemainingCouponsForActiveDeals() {
        long now = System.currentTimeMillis();
        try {
            log.debug("Starting coupon expiry scheduler at: {}", now);

            // Recalculate for merchant deals
            recalculateForUserType(UserType.MERCHANT, now);

            log.debug("Finished coupon expiry scheduler at: {}", System.currentTimeMillis());
        } catch (Exception e) {
            log.error("Unexpected error while running coupon expiry scheduler", e);
        }
    }

    /**
     * Recalculate remaining coupons for all active deals of a given user type.
     *
     * @param userType user type (MERCHANT/BANK)
     * @param now      current time in milliseconds
     */
    private void recalculateForUserType(UserType userType, long now) {
        try {
            Page<DealSearchIndex> dealPage = dealService.getAllSearchDeals(
                    Pageable.unpaged(),
                    ALL,  // categoryId
                    ALL,  // merchantId
                    ALL,  // brandId
                    ALL,  // searchTerm
                    true, // onlyActiveMerchant
                    true, // onlyActiveDeals
                    userType
            );

            if (dealPage == null || dealPage.isEmpty()) {
                return;
            }

            dealPage.forEach(dealSearchIndex -> syncRemainingCouponsForDeal(dealSearchIndex.getId(), now));
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to fetch active deals for coupon expiry scheduler", e);
        }
    }

    /**
     * Recalculate and sync remaining coupon counts for a single deal.
     *
     * @param dealId deal ID
     * @param now    current time in milliseconds
     */
    private void syncRemainingCouponsForDeal(String dealId, long now) {
        try {
            long actualRemaining = couponRepository.countActiveAvailableCouponsForDeal(dealId, now);
            int remainingInt = (int) actualRemaining;

            // Update statistics
            Optional<CouponStatistics> statsOptional = couponStatisticsRepository.findByDealId(dealId);
            statsOptional.ifPresent(stats -> {
                stats.setRemainingCoupons(remainingInt);
                stats.setUpdatedAt(System.currentTimeMillis());
                couponStatisticsRepository.save(stats);
            });

            // Update search index
            Optional<DealSearchIndex> indexOptional = dealSearchIndexRepository.findById(dealId);
            indexOptional.ifPresent(index -> {
                index.setRemainingCoupons(remainingInt);
                index.setUpdatedAt(System.currentTimeMillis());
                dealSearchIndexRepository.save(index);
            });
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to sync remaining coupons for deal id: " + dealId, e);
        }
    }
}

