package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.Category;
import com.kupi.cc.base.domain.entity.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface DealRepository extends JpaRepository<Deal, String> {

    /**
     * This method find the deal by given Id which not deleted and approval status pending.
     *
     * @param id deal Id
     * @return optional Deal
     */
    @Query(value = "SELECT * FROM deal d WHERE d.id=?1 AND d.is_deleted = false AND d.approval_status= 'PENDING'",
            nativeQuery = true)
    Optional<Deal> findPendingDealById(String id);

    /**
     * This method finds all non deleted deals.
     *
     * @param pageable pageable
     * @return deal page
     */
    @Query(value = "SELECT * FROM deal d WHERE d.is_deleted = false", nativeQuery = true)
    Page<Deal> findAllDeals(Pageable pageable);

    /**
     * This method find the deal by given Id which not deleted.
     *
     * @param id deal Id
     * @return optional Deal
     */
    @Query(value = "SELECT * FROM deal d WHERE d.id=?1 AND d.is_deleted = false",
            nativeQuery = true)
    Optional<Deal> findDealById(String id);


    /**
     * This method finds all deals for given merchant id.
     *
     * @param pageable   pageable
     * @param merchantId merchantId
     * @return deal page
     */
    @Query(value = "SELECT * FROM deal d WHERE d.merchant_id=?1 AND d.is_deleted = false",
            nativeQuery = true)
    Page<Deal> findAllDealsByMerchantId(Pageable pageable, String merchantId);

    /**
     * This method return a count of deal by brand id
     *
     * @param brandId brand id
     * @return count deals Count By BrandId
     */
    long countByRelatedBrandsId(String brandId);

    /**
     * This method returns a count of deals for specific category id
     *
     * @param categoryId category id
     * @return count deals Count By CategoryId
     */
    long countByRelatedCategoriesId(String categoryId);

    /**
     * This method is used to get total number of new deals.
     *
     * @param dateInMillis dateInMillis
     * @return total number of new deals
     */
    int countByCreatedAtGreaterThanEqualAndIsDeletedFalse(long dateInMillis);

    /**
     * This method is used to get total number of expiring deals.
     *
     * @param startTimestamp startTimestamp
     * @param endTimestamp   endTimestamp
     * @return total number of expiring deals
     */
    int countByExpiredOnGreaterThanEqualAndExpiredOnLessThan(long startTimestamp, long endTimestamp);

    /**
     * This method is used to get total number of new deals for merchantId.
     *
     * @param merchantId   merchantId
     * @param dateInMillis dateInMillis
     * @return total number of new deals
     */
    int countByMerchantIdAndCreatedAtGreaterThanEqualAndIsDeletedFalse(String merchantId, long dateInMillis);

    /**
     * This method is used to get total number of deals for merchantId.
     *
     * @param merchantId merchantId
     * @return total number of deals for merchantId
     */
    long countByMerchantIdAndIsDeletedFalse(String merchantId);

    /**
     * Find deals created by merchant within date range
     *
     * @param merchantId merchant ID
     * @param startDate  start date in milliseconds
     * @param endDate    end date in milliseconds
     * @return list of deals
     */
    @Query("SELECT d FROM Deal d WHERE d.merchantId = :merchantId " +
            "AND d.isDeleted = false " +
            "AND d.createdAt >= :startDate AND d.createdAt <= :endDate " +
            "ORDER BY d.createdAt DESC")
    List<Deal> findDealsCreatedByMerchantAndDateRange(@Param("merchantId") String merchantId,
                                                       @Param("startDate") long startDate,
                                                       @Param("endDate") long endDate);

    /**
     * Find deals updated by merchant within date range (excluding creations)
     *
     * @param merchantId merchant ID
     * @param startDate  start date in milliseconds
     * @param endDate    end date in milliseconds
     * @return list of deals
     */
    @Query("SELECT d FROM Deal d WHERE d.merchantId = :merchantId " +
            "AND d.isDeleted = false " +
            "AND d.updatedAt >= :startDate AND d.updatedAt <= :endDate " +
            "AND d.updatedAt > d.createdAt " +
            "ORDER BY d.updatedAt DESC")
    List<Deal> findDealsUpdatedByMerchantAndDateRange(@Param("merchantId") String merchantId,
                                                       @Param("startDate") long startDate,
                                                       @Param("endDate") long endDate);

    /**
     * This method is used to get total number of expiring deals for merchantId.
     *
     * @param merchantId     merchantId
     * @param startTimestamp startTimestamp
     * @param endTimestamp   endTimestamp
     * @return total number of expiring deals
     */
    int countByMerchantIdAndExpiredOnGreaterThanEqualAndExpiredOnLessThan(String merchantId, long startTimestamp,
                                                                          long endTimestamp);

    /**
     * This method returns a list of deal by category.
     *
     * @param pageable pageable
     * @param category category
     * @return Deal List
     */
    Page<Deal> getAllByRelatedCategories(Pageable pageable, Category category);

    /**
     * This method used for set isDealOfTheDay flag false.
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE Deal SET isDealOfTheDay = false WHERE isDealOfTheDay = true ")
    void disableDealOfTheDayFlags();

    /**
     * This method is used to find deals by list of ids.
     *
     * @param pageable pageable
     * @param dealIds  dealIds
     * @return page of deals.
     */
    Page<Deal> findByIdIn(Pageable pageable, List<String> dealIds);

    /**
     * This method is used to get total count of deals.
     *
     * @return count of deals.
     */
    long countByIsDeletedFalse();

    /**
     * This method is used to get total number of deals for merchantId based on coupon availability.
     *
     * @param merchantId merchantId
     * @return total number of deals for merchantId based on coupon availability
     */
    @Query(value = "SELECT COUNT(d) FROM Deal d WHERE d.merchantId =:merchantId AND (d.approvalStatus = 'APPROVED' OR d.approvalStatus = 'PUBLISHED') " +
            "AND d.isCouponAvailable =:isCouponAvailable")
    long countDealsByMerchantId(String merchantId, boolean isCouponAvailable);

    /**
     * Finds all deals for a merchant created within a date range.
     * Used for Deal Performance report.
     *
     * @param pageable   pageable
     * @param merchantId merchant ID
     * @param startDate  start date (epoch millis)
     * @param endDate    end date (epoch millis)
     * @return page of deals
     */
    @Query(value = "SELECT * FROM deal d WHERE d.merchant_id = :merchantId " +
            "AND d.is_deleted = false " +
            "AND d.created_at >= :startDate " +
            "AND d.created_at <= :endDate " +
            "ORDER BY d.created_at DESC",
            nativeQuery = true)
    Page<Deal> findDealsByMerchantIdAndCreatedDateRange(Pageable pageable,
                                                         @Param("merchantId") String merchantId,
                                                         @Param("startDate") long startDate,
                                                         @Param("endDate") long endDate);

    /**
     * Finds all deals for a merchant created within a date range and matching a search term.
     * Search is applied on deal title and deal code.
     *
     * @param pageable   pageable
     * @param merchantId merchant ID
     * @param startDate  start date (epoch millis)
     * @param endDate    end date (epoch millis)
     * @param searchTerm search term for deal title or deal code
     * @return page of deals
     */
    @Query(value = "SELECT * FROM deal d WHERE d.merchant_id = :merchantId " +
            "AND d.is_deleted = false " +
            "AND d.created_at >= :startDate " +
            "AND d.created_at <= :endDate " +
            "AND ( :searchTerm IS NULL OR :searchTerm = '' OR :searchTerm = 'ALL' " +
            "      OR d.title LIKE CONCAT('%', :searchTerm, '%') " +
            "      OR d.deal_code LIKE CONCAT('%', :searchTerm, '%') ) " +
            "ORDER BY d.created_at DESC",
            nativeQuery = true)
    Page<Deal> findDealsByMerchantIdAndCreatedDateRangeAndSearchTerm(Pageable pageable,
                                                                      @Param("merchantId") String merchantId,
                                                                      @Param("startDate") long startDate,
                                                                      @Param("endDate") long endDate,
                                                                      @Param("searchTerm") String searchTerm);
}