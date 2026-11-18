package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.CouponStatistics;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.CouponStatisticsRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CouponStatisticsService
 */
@Service
public class CouponStatisticsService {

    private final CouponStatisticsRepository couponStatisticsRepository;

    public CouponStatisticsService(CouponStatisticsRepository couponStatisticsRepository) {
        this.couponStatisticsRepository = couponStatisticsRepository;
    }

    /**
     * This method used to get CouponStatistics list by deal ids.
     *
     * @param dealIds dealIds List
     * @return CouponStatistics List
     */
    public List<CouponStatistics> getCouponStatisticsByDealIdList(List<String> dealIds) {
        try {
            return couponStatisticsRepository.findCouponStatisticsByDealIdInAndTotalCouponsGreaterThan(dealIds, 0);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading couponStatistics by deal ids list from database was failed.", e);
        }
    }
}
