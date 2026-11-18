package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.Coupon;
import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.enums.CouponPurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Coupon Repository
 */
public interface CouponRepository extends JpaRepository<Coupon, String> {

    /**
     * Used to get merchant-deal coupon which is not redeemed.
     *
     * @param dealId merchant dealId
     * @return merchant-deal coupon.
     */
    @Query(value = "SELECT * FROM coupon c WHERE c.deal_id = ?1 AND c.coupon_purchase_status = 'NOT_PURCHASED' " +
            " AND c.is_redeemed = false AND c.coupon_active = true AND c.expiry_date > ?2 ORDER BY RAND() LIMIT 1",
            nativeQuery = true)
    Optional<Coupon> getNewCoupon(String dealId, long timeStamp);

    /**
     * Used to find coupons by dealId.
     *
     * @param dealId   dealId
     * @param pageable pageable
     * @return coupon page.
     */
    Page<Coupon> findByDealId(String dealId, Pageable pageable);

    /**
     * Used to find coupons by dealId and search term.
     *
     * @param dealId     dealId
     * @param searchTerm searchTerm
     * @param pageable   pageable
     * @return coupon page.
     */
    Page<Coupon> findByDealIdAndCouponCodeContains(String dealId, String searchTerm, Pageable pageable);

    /**
     * Used to find coupon by couponCode and coupon code.
     *
     * @param couponCode couponCode
     * @param merchantId merchantId
     * @return coupon.
     */
    Optional<Coupon> findByCouponCodeAndDeal_MerchantId(String couponCode, String merchantId);

    /**
     * Used to find coupon by dealId and userId.
     *
     * @param dealId dealId
     * @param userId userId
     * @return coupon.
     */
    Optional<Coupon> findByDealIdAndPurchasedBy(String dealId, String userId);

    /**
     * Used to get coupon page by user id, ordered by expiry date in ascending order (soonest expiry first).
     * Returns ALL coupons for the user regardless of purchase status.
     *
     * @param pageable pageable
     * @param userId   userId
     * @return Coupon Page (all coupons regardless of couponPurchaseStatus)
     */
    @Query("SELECT c FROM Coupon c WHERE c.purchasedBy = :userId ORDER BY c.expiryDate ASC")
    Page<Coupon> findAllByPurchasedByOrderByExpiryDateAsc(Pageable pageable, @Param("userId") String userId);

    /**
     * Query to get all coupons for user with flexible sorting via Pageable.
     * This method respects the sort parameter from Pageable (no hardcoded ORDER BY).
     *
     * @param pageable pageable with custom sort
     * @param userId   userId
     * @return Coupon Page (respects Pageable sort)
     */
    @Query("SELECT c FROM Coupon c WHERE c.purchasedBy = :userId")
    Page<Coupon> findAllByPurchasedBy(Pageable pageable, @Param("userId") String userId);

    /**
     * Query to get coupons by purchase status.
     *
     * @param couponPurchaseStatus couponPurchaseStatus
     * @param pageable             pageable
     * @return Coupon Page
     */
    Page<Coupon> findByCouponPurchaseStatus(CouponPurchaseStatus couponPurchaseStatus, Pageable pageable);

    /**
     * Query to get coupons by merchantId and purchase status.
     *
     * @param merchantId           merchantId
     * @param couponPurchaseStatus couponPurchaseStatus
     * @param pageable             pageable
     * @return Coupon Page
     */
    Page<Coupon> findByDeal_MerchantIdAndCouponPurchaseStatus(
            String merchantId, CouponPurchaseStatus couponPurchaseStatus, Pageable pageable);

    /**
     * Query to get coupons by purchase status & search term.
     *
     * @param couponPurchaseStatus couponPurchaseStatus
     * @param searchTerm           searchTerm
     * @param pageable             pageable
     * @return Coupon Page
     */
    Page<Coupon> findByCouponPurchaseStatusAndCouponCodeContainsIgnoreCase(CouponPurchaseStatus couponPurchaseStatus,
                                                                           String searchTerm, Pageable pageable);

    /**
     * Query to get coupons by merchantId, purchase status & search term.
     *
     * @param merchantId           merchantId
     * @param couponPurchaseStatus couponPurchaseStatus
     * @param searchTerm           searchTerm
     * @param pageable             pageable
     * @return Coupon Page
     */
    Page<Coupon> findByDeal_MerchantIdAndCouponPurchaseStatusAndCouponCodeContainsIgnoreCase(
            String merchantId, CouponPurchaseStatus couponPurchaseStatus, String searchTerm, Pageable pageable);

    /**
     * Query to get coupons by purchase status & dealId.
     *
     * @param couponPurchaseStatus couponPurchaseStatus
     * @param dealId               dealId
     * @param pageable             pageable
     * @return Coupon Page
     */
    Page<Coupon> findByCouponPurchaseStatusAndDeal_Id(CouponPurchaseStatus couponPurchaseStatus, String dealId,
                                                      Pageable pageable);

    /**
     * Query to get coupons by merchantId, purchase status & dealId.
     *
     * @param merchantId           merchantId
     * @param couponPurchaseStatus couponPurchaseStatus
     * @param dealId               dealId
     * @param pageable             pageable
     * @return Coupon Page
     */
    Page<Coupon> findByDeal_MerchantIdAndCouponPurchaseStatusAndDeal_Id(
            String merchantId, CouponPurchaseStatus couponPurchaseStatus, String dealId, Pageable pageable);

    /**
     * Query to get coupons by purchase status, dealId & search term.
     *
     * @param couponPurchaseStatus couponPurchaseStatus
     * @param dealId               dealId
     * @param searchTerm           searchTerm
     * @param pageable             pageable
     * @return Coupon Page
     */
    Page<Coupon> findByCouponPurchaseStatusAndDeal_IdAndCouponCodeContainsIgnoreCase(
            CouponPurchaseStatus couponPurchaseStatus, String dealId, String searchTerm, Pageable pageable);

    /**
     * Query to get coupons by merchantId, purchase status, dealId & search term.
     *
     * @param merchantId           merchantId
     * @param couponPurchaseStatus couponPurchaseStatus
     * @param dealId               dealId
     * @param searchTerm           searchTerm
     * @param pageable             pageable
     * @return Coupon Page
     */
    Page<Coupon> findByDeal_MerchantIdAndCouponPurchaseStatusAndDeal_IdAndCouponCodeContainsIgnoreCase(
            String merchantId, CouponPurchaseStatus couponPurchaseStatus, String dealId, String searchTerm,
            Pageable pageable);

    /**
     * Used to get coupon uploaded file name list by deal id.
     *
     * @param dealId dealId
     * @return fileName list
     */
    @Query(value = "select distinct c.fileName from Coupon c where c.deal.id= :dealId")
    List<String> getDistinctByFileName(String dealId);

    /**
     * This method used to get successfully uploaded coupon list by deal id and file name.
     *
     * @param dealId   dealId
     * @param fileName fileName
     * @return coupon list
     */
    @Query(value = "select c.couponCode from Coupon c where c.deal.id=:dealId and c.fileName=:fileName")
    List<String> getUploadSuccessCoupon(String dealId, String fileName);

    /**
     * This method returns true when coupon code exist in database.
     *
     * @param deal       deal
     * @param couponCode coupon code
     * @return boolean
     */
    boolean existsAllByDealAndAndCouponCode(Deal deal, String couponCode);

    /**
     * This method retrieves a paginated list of redeemed coupons for a specific user,
     * ordered by the coupon expiry date in ascending order (soonest expiry first).
     *
     * @param pageable pagination information including page number and size
     * @param userId   the ID of the user who purchased the coupons
     * @return a page of redeemed coupons for the given user
     */
    Page<Coupon> findByPurchasedByAndIsRedeemedTrueOrderByExpiryDateAsc(Pageable pageable, String userId);

    /**
     * This method retrieves a paginated list of coupons for a specific user that are
     * expiring within a given date range, ordered by the coupon expiry date in ascending order (soonest expiry first).
     *
     * @param pageable  pagination information including page number and size
     * @param userId    the ID of the user who purchased the coupons
     * @param startTime start of the expiry date range (in milliseconds)
     * @param endTime   end of the expiry date range (in milliseconds)
     * @return a page of coupons that will expire between the given dates for the specified user
     */
    Page<Coupon> findByPurchasedByAndExpiryDateBetweenOrderByExpiryDateAsc(
            Pageable pageable, String userId, long startTime, long endTime);

    /**
     * This method retrieves a paginated list of coupons for a specific user that will expire
     * within the next 7 days (expiry date is NOT in the past and falls within 7 days from now).
     * Only returns unredeemed coupons. Uses explicit > and <= comparison to ensure accuracy.
     *
     * @param pageable       pagination information including page number and size
     * @param userId         the ID of the user who purchased the coupons
     * @param currentTime    current time in milliseconds (coupons must have expiryDate > currentTime)
     * @param sevenDaysLater end of the 7-day window in milliseconds
     * @return a page of unredeemed coupons that will expire within the next 7 days for the specified user
     */
    @Query("SELECT c FROM Coupon c " +
            "WHERE c.purchasedBy = :userId " +
            "AND c.expiryDate > :currentTime " +
            "AND c.expiryDate <= :sevenDaysLater " +
            "AND c.isRedeemed = false " +
            "ORDER BY c.expiryDate ASC")
    Page<Coupon> findByPurchasedByAndExpiryDateAfterAndExpiryDateBeforeOrderByExpiryDateAsc(
            Pageable pageable,
            @Param("userId") String userId,
            @Param("currentTime") long currentTime,
            @Param("sevenDaysLater") long sevenDaysLater);

    /**
     * Check if a coupon code exists globally in the database
     *
     * @param couponCode the coupon code to check
     * @return true if the code exists, false otherwise
     */
    boolean existsByCouponCode(String couponCode);

    /**
     * Find existing coupon codes from a list of codes
     *
     * @param couponCodes list of coupon codes to check
     * @return list of codes that already exist in the database
     */
    @Query("SELECT c.couponCode FROM Coupon c WHERE c.couponCode IN :couponCodes")
    List<String> findExistingCodes(@Param("couponCodes") List<String> couponCodes);

    /**
     * Count coupons by deal ID
     *
     * @param dealId deal ID
     * @return count of coupons for the deal
     */
    long countByDealId(String dealId);

    /**
     * Counts active, non-expired, not-purchased coupons for a specific deal.
     * This is used by the scheduled job to keep remaining coupon counts in sync.
     *
     * @param dealId      deal ID
     * @param currentTime current time in milliseconds
     * @return count of available coupons for the deal
     */
    @Query("SELECT COUNT(c) FROM Coupon c " +
            "WHERE c.deal.id = :dealId " +
            "AND c.isRedeemed = false " +
            "AND c.couponActive = true " +
            "AND c.couponPurchaseStatus = com.kupi.cc.base.enums.CouponPurchaseStatus.NOT_PURCHASED " +
            "AND c.expiryDate > :currentTime")
    long countActiveAvailableCouponsForDeal(@Param("dealId") String dealId,
                                            @Param("currentTime") long currentTime);

    /**
     * Counts active coupons for a merchant within an expiry date range.
     *
     * @param merchantId merchant ID
     * @param startDate  start of range (epoch millis)
     * @param endDate    end of range (epoch millis)
     * @return total coupons
     */
    @Query(
            "SELECT COUNT(c) " +
                    "FROM Coupon c " +
                    "WHERE c.deal.merchantId = :merchantId " +
                    "AND c.isRedeemed = false " +
                    "AND c.couponActive = true " +
                    "AND c.purchasedBy IS NOT NULL " +
                    "AND c.purchasedOn >= :startDate " +
                    "AND c.purchasedOn <= :endDate"
    )
    long countActiveCouponsOverlappingRange(
            @Param("merchantId") String merchantId,
            @Param("startDate") long startDate,
            @Param("endDate") long endDate
    );



    @Query("SELECT COUNT(c) FROM Coupon c " +
            "WHERE c.deal.id IS NOT NULL " +
            "AND c.deal.merchantId = :merchantId " +
            "AND c.isRedeemed = true " +
            "AND c.redeemedOn IS NOT NULL " +
            "AND c.redeemedOn >= :startDate " +
            "AND c.redeemedOn <= :endDate")
    long countRedeemedCouponsByMerchantIdAndDateRange(@Param("merchantId") String merchantId,
                                                      @Param("startDate") long startDate, @Param("endDate") long endDate);

    @Query("SELECT COUNT(c) FROM Coupon c " +
            "WHERE c.deal.id IS NOT NULL " +
            "AND c.isRedeemed = true " +
            "AND c.redeemedOn IS NOT NULL")
    long countAllRedeemedCoupons();

    @Query("SELECT COUNT(c) FROM Coupon c " +
            "WHERE c.isRedeemed = false " +
            "AND c.purchasedBy IS NOT NULL " +
            "AND c.redeemedOn IS NULL " +
            "AND c.couponActive = true " +
            "AND c.expiryDate > :currentTime")
    long countActiveCoupons(@Param("currentTime") long currentTime);

    /**
     * Retrieves all redeemed coupons for a merchant within a date range with deal information.
     * Used for calculating total earnings.
     *
     * @param merchantId merchant ID
     * @param startDate  start of range (epoch millis)
     * @param endDate    end of range (epoch millis)
     * @return list of redeemed coupons with deal information
     */
    @Query("SELECT c FROM Coupon c JOIN FETCH c.deal d " +
            "WHERE d.id IS NOT NULL " +
            "AND d.merchantId = :merchantId " +
            "AND c.isRedeemed = true " +
            "AND c.redeemedOn IS NOT NULL " +
            "AND c.redeemedOn >= :startDate " +
            "AND c.redeemedOn <= :endDate")
    List<Coupon> findRedeemedCouponsByMerchantIdAndDateRange(@Param("merchantId") String merchantId,
                                                             @Param("startDate") long startDate,
                                                             @Param("endDate") long endDate);

    /**
     * Retrieves all purchased (sold) coupons for a merchant within a date range.
     * A purchased coupon is defined as:
     *  - isRedeemed = false
     *  - couponActive = true
     *  - purchasedBy is not null
     *  - purchasedOn between startDate and endDate
     *
     * This matches the same criteria used in countActiveCouponsOverlappingRange, but returns entities for
     * day-by-day aggregation.
     *
     * @param merchantId merchant ID
     * @param startDate  start of range (epoch millis)
     * @param endDate    end of range (epoch millis)
     * @return list of purchased coupons
     */
    @Query("SELECT c FROM Coupon c " +
            "WHERE c.deal.id IS NOT NULL " +
            "AND c.deal.merchantId = :merchantId " +
            "AND c.purchasedBy IS NOT NULL " +
            "AND c.purchasedOn IS NOT NULL " +
            "AND c.purchasedOn >= :startDate " +
            "AND c.purchasedOn <= :endDate")
    List<Coupon> findPurchasedCouponsByMerchantIdAndDateRange(@Param("merchantId") String merchantId,
                                                              @Param("startDate") long startDate,
                                                              @Param("endDate") long endDate);
    
    /**
     * Retrieves all purchased (sold) coupons for a specific deal within a date range.
     *
     * @param dealId    deal ID
     * @param startDate start of range (epoch millis)
     * @param endDate   end of range (epoch millis)
     * @return list of purchased coupons for the deal
     */
    @Query("SELECT c FROM Coupon c " +
            "WHERE c.deal.id = :dealId " +
            "AND c.purchasedBy IS NOT NULL " +
            "AND c.purchasedOn IS NOT NULL " +
            "AND c.purchasedOn >= :startDate " +
            "AND c.purchasedOn <= :endDate")
    List<Coupon> findPurchasedCouponsByDealIdAndDateRange(@Param("dealId") String dealId,
                                                          @Param("startDate") long startDate,
                                                          @Param("endDate") long endDate);

    /**
     * Counts purchased (sold) coupons for a specific deal within a date range.
     *
     * @param dealId    deal ID
     * @param startDate start of range (epoch millis)
     * @param endDate   end of range (epoch millis)
     * @return count of purchased coupons
     */
    @Query("SELECT COUNT(c) FROM Coupon c " +
            "WHERE c.deal.id = :dealId " +
            "AND c.purchasedBy IS NOT NULL " +
            "AND c.purchasedOn IS NOT NULL " +
            "AND c.purchasedOn >= :startDate " +
            "AND c.purchasedOn <= :endDate")
    long countPurchasedCouponsByDealIdAndDateRange(@Param("dealId") String dealId,
                                                   @Param("startDate") long startDate,
                                                   @Param("endDate") long endDate);

    /**
     * Counts redeemed coupons for a specific deal within a date range.
     *
     * @param dealId    deal ID
     * @param startDate start of range (epoch millis)
     * @param endDate   end of range (epoch millis)
     * @return count of redeemed coupons
     */
    @Query("SELECT COUNT(c) FROM Coupon c " +
            "WHERE c.deal.id = :dealId " +
            "AND c.isRedeemed = true " +
            "AND c.redeemedOn IS NOT NULL " +
            "AND c.redeemedOn >= :startDate " +
            "AND c.redeemedOn <= :endDate")
    long countRedeemedCouponsByDealIdAndDateRange(@Param("dealId") String dealId,
                                                  @Param("startDate") long startDate,
                                                  @Param("endDate") long endDate);

    /**
     * Retrieves all redeemed coupons with deal information.
     * Used for calculating total savings across all users.
     *
     * @return list of all redeemed coupons with deal information
     */
    @Query("SELECT c FROM Coupon c JOIN FETCH c.deal d " +
            "WHERE d.id IS NOT NULL " +
            "AND c.isRedeemed = true " +
            "AND c.redeemedOn IS NOT NULL")
    List<Coupon> findAllRedeemedCouponsWithDeal();

    /**
     * Retrieves all redeemed coupons for a specific user with deal information.
     * Used for calculating total savings for a single user.
     *
     * @param userId user ID
     * @return list of redeemed coupons with deal information for the user
     */
    @Query("SELECT c FROM Coupon c JOIN FETCH c.deal d " +
            "WHERE d.id IS NOT NULL " +
            "AND c.purchasedBy = :userId " +
            "AND c.isRedeemed = true " +
            "AND c.redeemedOn IS NOT NULL")
    List<Coupon> findRedeemedCouponsByUserIdWithDeal(@Param("userId") String userId);

}
