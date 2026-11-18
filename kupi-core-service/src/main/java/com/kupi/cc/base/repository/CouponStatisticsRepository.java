package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.CouponStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Coupon Statistics Repository
 */
public interface CouponStatisticsRepository extends JpaRepository<CouponStatistics, String> {

    /**
     * Used to find coupon stat by dealId.
     *
     * @param dealId dealId
     * @return coupon statistics.
     */
    Optional<CouponStatistics> findByDealId(String dealId);

    /**
     * This method used to get CouponStatistics list by deal ids.
     *
     * @param dealIds dealIds
     * @return CouponStatistics List
     */
    List<CouponStatistics> findCouponStatisticsByDealIdInAndTotalCouponsGreaterThan(List<String> dealIds, int couponCount);
}
