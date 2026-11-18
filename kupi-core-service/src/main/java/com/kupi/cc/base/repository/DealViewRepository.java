package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.DealView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Deal View Repository
 */
public interface DealViewRepository extends JpaRepository<DealView, String> {

    /**
     * This method finds all deal views for a given deal id.
     *
     * @param dealId deal id
     * @return list of deal views
     */
    List<DealView> findByDealId(String dealId);

    /**
     * This method finds all deal views for a given merchant id.
     *
     * @param merchantId merchant id
     * @return list of deal views
     */
    List<DealView> findByMerchantId(String merchantId);

    /**
     * This method counts views for a given deal id.
     *
     * @param dealId deal id
     * @return count of views
     */
    long countByDealId(String dealId);

    /**
     * This method counts views for a given merchant id.
     *
     * @param merchantId merchant id
     * @return count of views
     */
    long countByMerchantId(String merchantId);

    /**
     * This method counts views for a given deal id within a time range.
     *
     * @param dealId      deal id
     * @param startTime   start timestamp
     * @param endTime     end timestamp
     * @return count of views
     */
    @Query("SELECT COUNT(dv) FROM DealView dv WHERE dv.dealId = :dealId " +
            "AND dv.viewedAt >= :startTime AND dv.viewedAt <= :endTime")
    long countByDealIdAndViewedAtBetween(@Param("dealId") String dealId,
                                         @Param("startTime") long startTime,
                                         @Param("endTime") long endTime);

    /**
     * This method counts views for a given merchant id within a time range.
     *
     * @param merchantId  merchant id
     * @param startTime   start timestamp
     * @param endTime     end timestamp
     * @return count of views
     */
    @Query("SELECT COUNT(dv) FROM DealView dv WHERE dv.merchantId = :merchantId " +
            "AND dv.viewedAt >= :startTime AND dv.viewedAt <= :endTime")
    long countByMerchantIdAndViewedAtBetween(@Param("merchantId") String merchantId,
                                             @Param("startTime") long startTime,
                                             @Param("endTime") long endTime);

    /**
     * This method retrieves all deal views for a merchant within a time range.
     *
     * @param merchantId merchant id
     * @param startTime  start timestamp
     * @param endTime    end timestamp
     * @return list of deal views
     */
    @Query("SELECT dv FROM DealView dv WHERE dv.merchantId = :merchantId " +
            "AND dv.viewedAt >= :startTime AND dv.viewedAt <= :endTime")
    List<DealView> findByMerchantIdAndViewedAtBetween(@Param("merchantId") String merchantId,
                                                      @Param("startTime") long startTime,
                                                      @Param("endTime") long endTime);

    /**
     * This method retrieves all deal views for a deal within a time range.
     *
     * @param dealId    deal id
     * @param startTime start timestamp
     * @param endTime   end timestamp
     * @return list of deal views
     */
    @Query("SELECT dv FROM DealView dv WHERE dv.dealId = :dealId " +
            "AND dv.viewedAt >= :startTime AND dv.viewedAt <= :endTime")
    List<DealView> findByDealIdAndViewedAtBetween(@Param("dealId") String dealId,
                                                  @Param("startTime") long startTime,
                                                  @Param("endTime") long endTime);
}

