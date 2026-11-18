package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.CouponStatisticDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * CouponStatisticDetailRepository Repository
 */
public interface CouponStatisticDetailRepository extends JpaRepository<CouponStatisticDetail, String> {

    @Query(value = "select cd.id from CouponStatisticDetail cd where cd.deal.id=:dealId and cd.fileName=:fileName")
    List<String> getUploadFailCoupon(String dealId, String fileName);

    /**
     * This method used to get all couponStatisticsDetails by coupon detailIds.
     *
     * @param couponDetailIds couponDetailIds
     * @return CouponStatisticDetail List
     */
    List<CouponStatisticDetail> findAllByIdIn(List<String> couponDetailIds);

    /**
     * Used to get coupon uploaded failed file name list by deal id.
     *
     * @param dealId dealId
     * @return fileName list
     */
    @Query(value = "select distinct cs.fileName from CouponStatisticDetail cs where cs.deal.id= :dealId")
    List<String> getDistinctByFileName(String dealId);

}
