package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.domain.entity.DealView;
import com.kupi.cc.base.exception.InvalidDealException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.DealRepository;
import com.kupi.cc.base.repository.DealViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Deal View Service
 */
@Service
public class DealViewService {

    private final DealViewRepository dealViewRepository;
    private final DealRepository dealRepository;

    @Autowired
    public DealViewService(DealViewRepository dealViewRepository, DealRepository dealRepository) {
        this.dealViewRepository = dealViewRepository;
        this.dealRepository = dealRepository;
    }

    /**
     * Track a deal view
     *
     * @param userId user id viewing the deal
     * @param dealId deal id being viewed
     */
    public void trackDealView(String userId, String dealId) {
        try {
            Deal deal = dealRepository.findDealById(dealId)
                    .orElseThrow(() -> new InvalidDealException("Deal not found with id: " + dealId));
            String merchantId = deal.getMerchantId();
            DealView dealView = new DealView(userId, dealId, merchantId);
            dealViewRepository.save(dealView);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving deal view to database was failed for dealId: " + dealId, e);
        }
    }

    /**
     * Get view count for a deal
     *
     * @param dealId deal id
     * @return view count
     */
    public long getDealViewCount(String dealId) {
        try {
            return dealViewRepository.countByDealId(dealId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading deal view count from database was failed for dealId: " + dealId, e);
        }
    }

    /**
     * Get view count for a merchant
     *
     * @param merchantId merchant id
     * @return view count
     */
    public long getMerchantViewCount(String merchantId) {
        try {
            return dealViewRepository.countByMerchantId(merchantId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading merchant view count from database was failed for merchantId: " + merchantId, e);
        }
    }

    /**
     * Get view count for a deal within a time range
     *
     * @param dealId    deal id
     * @param startTime start timestamp
     * @param endTime   end timestamp
     * @return view count
     */
    public long getDealViewCountByTimeRange(String dealId, long startTime, long endTime) {
        try {
            return dealViewRepository.countByDealIdAndViewedAtBetween(dealId, startTime, endTime);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading deal view count by time range from database was failed for dealId: " + dealId, e);
        }
    }

    /**
     * Get view count for a merchant within a time range
     *
     * @param merchantId merchant id
     * @param startTime  start timestamp
     * @param endTime    end timestamp
     * @return view count
     */
    public long getMerchantViewCountByTimeRange(String merchantId, long startTime, long endTime) {
        try {
            return dealViewRepository.countByMerchantIdAndViewedAtBetween(merchantId, startTime, endTime);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading merchant view count by time range from database was failed for merchantId: " + merchantId, e);
        }
    }

    /**
     * Get all deal views for a merchant within a time range.
     * This is used for day-by-day aggregation in dashboard charts.
     *
     * @param merchantId merchant id
     * @param startTime  start timestamp
     * @param endTime    end timestamp
     * @return list of deal views
     */
    public java.util.List<DealView> getMerchantDealViewsByTimeRange(String merchantId, long startTime, long endTime) {
        try {
            return dealViewRepository.findByMerchantIdAndViewedAtBetween(merchantId, startTime, endTime);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading merchant deal views by time range from database was failed for merchantId: " + merchantId, e);
        }
    }

    /**
     * Get all deal views for a specific deal within a time range.
     *
     * @param dealId    deal id
     * @param startTime start timestamp
     * @param endTime   end timestamp
     * @return list of deal views
     */
    public java.util.List<DealView> getDealViewsByTimeRange(String dealId, long startTime, long endTime) {
        try {
            return dealViewRepository.findByDealIdAndViewedAtBetween(dealId, startTime, endTime);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading deal views by time range from database was failed for dealId: " + dealId, e);
        }
    }
}

