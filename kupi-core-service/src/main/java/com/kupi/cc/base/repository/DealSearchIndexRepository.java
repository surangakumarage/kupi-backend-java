package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * This class should replace from a search engine.
 */
public interface DealSearchIndexRepository extends JpaRepository<DealSearchIndex, String> {

    String ACTIVE_DEAL = "ds.validFrom <=:currentMills AND ds.expiredOn > :currentMills";
    // VALID_DEAL: only APPROVED, not-deleted deals are considered valid
    String VALID_DEAL = "ds.isDeleted=false AND ds.approvalStatus='APPROVED'";
    String ACTIVE_MERCHANT = "ds.isActiveMerchant=true";
    String VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT = VALID_DEAL + " AND " + ACTIVE_DEAL + " AND " + ACTIVE_MERCHANT;
    String SEARCH =
            "(ds.title LIKE %:searchTerm% OR ds.subTitle LIKE %:searchTerm% OR ds.description" +
                    " LIKE %:searchTerm% OR ds.merchantName LIKE %:searchTerm% OR ds.brandNames LIKE %:searchTerm%" +
                    " OR ds.categoryNames LIKE %:searchTerm% OR ds.dealCode LIKE %:searchTerm%)";
    String MERCHANT_ID = "ds.merchantId=:merchantId";
    String RELATED_CATEGORIES = "ds.relatedCategories LIKE %:categoryId%";
    String VALID_ACTIVE_DEALS = VALID_DEAL + " AND " + ACTIVE_DEAL;
    String DEAL_SOURCE = "ds.dealSource=:dealSource";

    /**
     * This method find the filter deal by given Id which not deleted and approval status pending.
     *
     * @param id deal Id
     * @return optional dealSearchIndex
     */
    @Query(value = "SELECT * FROM search_deal d WHERE d.id=?1 AND d.is_deleted = false AND" +
            " d.approval_status = 'PENDING'", nativeQuery = true)
    Optional<DealSearchIndex> findPendingDealById(String id);

    /**
     * This method finds deal by given id which is not deleted.
     *
     * @param id dealId
     * @return optional dealSearchIndex
     */
    @Query(value = "SELECT d FROM DealSearchIndex d WHERE d.id=?1 AND d.isDeleted = false")
    Optional<DealSearchIndex> getNotDeletedDealById(String id);

    /**
     * This method finds all not deleted deals by searchTerm.
     *
     * @param pageable   pageable
     * @param searchTerm searchTerm
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT d from DealSearchIndex d WHERE d.isDeleted = false AND d.dealSource=:userType AND (" +
            " d.title LIKE %:searchTerm% OR d.subTitle LIKE %:searchTerm% OR d.description LIKE %:searchTerm% OR" +
            " d.merchantName LIKE %:searchTerm% OR d.brandNames LIKE %:searchTerm% OR d.categoryNames" +
            " LIKE %:searchTerm% OR d.dealCode LIKE %:searchTerm%)")
    Page<DealSearchIndex> findAllNotDeletedDealsBySearchTerm(Pageable pageable, @Param("searchTerm") String searchTerm,
                                                             @Param("userType") UserType userType);

    /**
     * This method find all not deleted deals based on searchTerm, approvedStatus and userType.
     *
     * @param pageable       pageable
     * @param searchTerm     searchTerm
     * @param approvalStatus Approved/Pending/Rejected
     * @param userType       Bank/Merchant
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT d from DealSearchIndex d WHERE d.isDeleted = false AND d.dealSource=:userType AND (" +
            " d.title LIKE %:searchTerm% OR d.subTitle LIKE %:searchTerm% OR d.description LIKE %:searchTerm% OR" +
            " d.merchantName LIKE %:searchTerm% OR d.brandNames LIKE %:searchTerm% OR d.categoryNames" +
            " LIKE %:searchTerm% OR d.dealCode LIKE %:searchTerm%) AND d.approvalStatus=:approvalStatus")
    Page<DealSearchIndex> findAllNotDeletedDealsBySearchTerm(Pageable pageable, String searchTerm,
                                                             ApprovalStatus approvalStatus, UserType userType);

    /**
     * This method find all not deleted deals.
     *
     * @param pageable pageable
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT d FROM DealSearchIndex d WHERE d.isDeleted = false AND d.dealSource=?1")
    Page<DealSearchIndex> findAllNotDeletedDeals(Pageable pageable, UserType userType);

    /**
     * This method find all not deleted deals based on approvedStatus and userType.
     *
     * @param pageable       pageable
     * @param approvalStatus Approved/Pending/Rejected
     * @param userType       Bank/Merchant
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT d FROM DealSearchIndex d WHERE d.isDeleted = false AND d.dealSource=:userType AND " +
            "d.approvalStatus=:approvalStatus")
    Page<DealSearchIndex> findAllNotDeletedDeals(Pageable pageable, ApprovalStatus approvalStatus, UserType userType);

    /**
     * This method find all not deleted deals for merchantId.
     *
     * @param pageable   pageable
     * @param merchantId merchantId
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT d FROM DealSearchIndex d WHERE d.merchantId=?1 AND d.isDeleted = false ")
    Page<DealSearchIndex> findAllNotDeletedDealsByMerchantId(Pageable pageable, String merchantId);

    /**
     * This method find all not deleted deals based on merchantId, approvedStatus and userType.
     *
     * @param pageable       pageable
     * @param merchantId     merchantId
     * @param approvalStatus Approved/Pending/Rejected
     * @param userType       Bank/Merchant
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT d FROM DealSearchIndex d WHERE d.approvalStatus=:approvalStatus AND " +
            "d.merchantId=:merchantId AND d.isDeleted=false AND d.dealSource=:userType")
    Page<DealSearchIndex> findAllNotDeletedDealsByMerchantId(Pageable pageable, String merchantId,
                                                             ApprovalStatus approvalStatus, UserType userType);

    /**
     * This method find all not deleted deals for merchantId and searchTerm.
     *
     * @param pageable   pageable
     * @param merchantId merchantId
     * @param searchTerm searchTerm
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT d from DealSearchIndex d WHERE d.merchantId=:merchantId AND d.isDeleted = false " +
            " AND ( d.title LIKE %:searchTerm% OR d.subTitle LIKE %:searchTerm% OR d.description LIKE %:searchTerm%" +
            " OR d.brandNames LIKE %:searchTerm% OR d.categoryNames LIKE %:searchTerm% " +
            "OR d.dealCode LIKE %:searchTerm%)")
    Page<DealSearchIndex> findAllNotDeletedDealsByMerchantIdAndSearchTerm(Pageable pageable,
                                                                          @Param("merchantId") String merchantId,
                                                                          @Param("searchTerm") String searchTerm);

    /**
     * This method find all not deleted deals based on merchantId, approvedStatus, searchTerm and userType.
     *
     * @param pageable       pageable
     * @param merchantId     merchantId
     * @param approvalStatus Approved/Pending/Rejected
     * @param searchTerm     searchTerm
     * @param userType       Bank/Merchant
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT d from DealSearchIndex d WHERE d.merchantId=:merchantId AND d.isDeleted = false " +
            " AND ( d.title LIKE %:searchTerm% OR d.subTitle LIKE %:searchTerm% OR d.description LIKE %:searchTerm%" +
            " OR d.brandNames LIKE %:searchTerm% OR d.categoryNames LIKE %:searchTerm% " +
            "OR d.dealCode LIKE %:searchTerm%) AND d.approvalStatus=:approvalStatus AND d.dealSource=:userType")
    Page<DealSearchIndex> findAllNotDeletedDealsByMerchantIdAndSearchTerm(Pageable pageable, String merchantId,
                                                                          ApprovalStatus approvalStatus,
                                                                          String searchTerm, UserType userType);

    /**
     * This method finds all approved deals by search.
     *
     * @param pageable   pageable
     * @param searchTerm searchTerm
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT * from search_deal d WHERE d.deal_source=:dealSource AND d.is_deleted = false" +
            " AND d.approval_status = 'APPROVED' AND (d.title LIKE %:searchTerm% OR d.sub_title" +
            " LIKE %:searchTerm% OR d.description LIKE %:searchTerm% OR d.merchant_name LIKE" +
            " %:searchTerm% OR d.brand_names LIKE %:searchTerm% OR d.category_names LIKE %:searchTerm% " +
            " OR d.deal_code LIKE %:searchTerm% )",
            nativeQuery = true)
    Page<DealSearchIndex> findAllApprovedDealsBySearchTerm(Pageable pageable,
                                                           @Param("searchTerm") String searchTerm,
                                                           @Param("dealSource") String dealSource);

    /**
     * This method find all deals
     *
     * @param pageable pageable
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT * FROM search_deal d WHERE d.deal_source=:dealSource AND d.is_deleted = false AND" +
            " d.approval_status = 'APPROVED'", nativeQuery = true)
    Page<DealSearchIndex> findAllSearchDeals(Pageable pageable,
                                             @Param("dealSource") String dealSource);

    /**
     * This method find all deals for merchantId
     *
     * @param pageable   pageable
     * @param merchantId merchantId
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT * FROM search_deal d WHERE d.deal_source=:dealSource AND d.merchant_id=:merchantId AND" +
            " d.is_deleted = false AND d.approval_status = 'APPROVED'",
            nativeQuery = true)
    Page<DealSearchIndex> findAllByMerchantId(Pageable pageable, String merchantId,
                                              @Param("dealSource") String dealSource);

    /**
     * This method find all deals for brand id
     *
     * @param pageable pageable
     * @param brandId  brandId
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT * FROM search_deal d WHERE d.deal_source=:dealSource AND d.is_deleted = false" +
            " AND d.related_brands LIKE %:brandId%", nativeQuery = true)
    Page<DealSearchIndex> findAllByBrandId(Pageable pageable, String brandId,
                                           @Param("dealSource") String dealSource);

    /**
     * This method find all deals for brand id and searchTerm
     *
     * @param pageable   pageable
     * @param brandId    brandId
     * @param searchTerm searchTerm
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT * from search_deal d WHERE d.deal_source=:dealSource AND d.is_deleted = false AND" +
            " d.related_brands LIKE %:brandId% AND ( d.title LIKE %:searchTerm% OR" +
            " d.sub_title LIKE %:searchTerm% OR d.brand_names LIKE %:searchTerm% OR d.deal_code LIKE %:searchTerm% " +
            " OR d.merchant_name LIKE %:searchTerm%)", nativeQuery = true)
    Page<DealSearchIndex> findAllByBrandIdAndSearchTerm(Pageable pageable,
                                                        @Param("brandId") String brandId,
                                                        @Param("searchTerm") String searchTerm,
                                                        @Param("dealSource") String dealSource);

    /**
     * This method find all deals for categoryId
     *
     * @param pageable   pageable
     * @param categoryId categoryId
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT * FROM search_deal d WHERE d.deal_source=:dealSource AND d.is_deleted = false" +
            " AND d.related_categories LIKE %:categoryId%", nativeQuery = true)
    Page<DealSearchIndex> findAllByCategoryId(Pageable pageable, @Param("categoryId") String categoryId,
                                              @Param("dealSource") String dealSource);

    /**
     * This method find all deals for categoryId and searchTerm
     *
     * @param pageable   pageable
     * @param categoryId categoryId
     * @param searchTerm searchTerm
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT * from search_deal d WHERE d.deal_source=:dealSource AND d.is_deleted = false AND" +
            " d.related_categories LIKE %:categoryId% AND ( d.title LIKE %:searchTerm% OR" +
            " d.sub_title LIKE %:searchTerm% OR d.brand_names LIKE %:searchTerm% OR d.category_names" +
            " LIKE %:searchTerm% OR d.deal_code LIKE %:searchTerm% OR d.merchant_name LIKE %:searchTerm%)",
            nativeQuery = true)
    Page<DealSearchIndex> findAllByCategoryIdAndSearchTerm(Pageable pageable,
                                                           @Param("categoryId") String categoryId,
                                                           @Param("searchTerm") String searchTerm,
                                                           @Param("dealSource") String dealSource);

    /**
     * This method find all deals for merchantId and categoryId
     *
     * @param pageable   pageable
     * @param merchantId merchantId
     * @param categoryId categoryId
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT * FROM search_deal d WHERE d.merchant_id=:merchantId AND d.deal_source=:dealSource" +
            " AND d.is_deleted = false AND d.approval_status = 'APPROVED' AND d.related_categories LIKE %:categoryId%",
            nativeQuery = true)
    Page<DealSearchIndex> findAllByMerchantIdAndCategoryId(Pageable pageable,
                                                           @Param("merchantId") String merchantId,
                                                           @Param("categoryId") String categoryId,
                                                           @Param("dealSource") String dealSource);

    /**
     * This method find all deals for merchantId and searchTerm
     *
     * @param pageable   pageable
     * @param merchantId merchantId
     * @param searchTerm searchTerm
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT * from search_deal d WHERE d.merchant_id=:merchantId AND d.deal_source=:dealSource" +
            " AND d.is_deleted = false AND d.approval_status = 'APPROVED' AND ( d.title LIKE %:searchTerm%" +
            " OR d.sub_title LIKE %:searchTerm% OR d.description LIKE %:searchTerm%" +
            " OR d.merchant_name LIKE %:searchTerm% OR d.brand_names LIKE %:searchTerm% " +
            " OR d.category_names LIKE %:searchTerm% OR d.deal_code LIKE %:searchTerm% )", nativeQuery = true)
    Page<DealSearchIndex> findAllByMerchantIdAndSearchTerm(Pageable pageable,
                                                           @Param("merchantId") String merchantId,
                                                           @Param("searchTerm") String searchTerm,
                                                           @Param("dealSource") String dealSource);

    /**
     * This method find all deals for categoryId, merchantId and searchTerm
     *
     * @param pageable   pageable
     * @param merchantId merchantId
     * @param categoryId categoryId
     * @param searchTerm searchTerm
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT * from search_deal d WHERE d.merchant_id=:merchantId AND d.deal_source=:dealSource AND d.is_deleted = false AND" +
            " d.approval_status = 'APPROVED' AND d.related_categories LIKE %:categoryId% AND" +
            " ( d.title LIKE %:searchTerm% OR d.sub_title LIKE %:searchTerm% OR d.description" +
            " LIKE %:searchTerm% OR d.merchant_name LIKE %:searchTerm% OR d.brand_names LIKE %:searchTerm%" +
            " OR d.category_names LIKE %:searchTerm% OR d.deal_code LIKE %:searchTerm% )", nativeQuery = true)
    Page<DealSearchIndex> findAllByMerchantIdCategoryIdSearchTerm(Pageable pageable,
                                                                  @Param("merchantId") String merchantId,
                                                                  @Param("categoryId") String categoryId,
                                                                  @Param("searchTerm") String searchTerm,
                                                                  @Param("dealSource") String dealSource);

    /**
     * This method finds all pending deals by search
     *
     * @param pageable   pageable
     * @param searchTerm searchTerm
     * @return dealSearchIndex page
     */
    @Query(value = "SELECT * from search_deal d WHERE d.is_deleted = false AND d.approval_status = 'PENDING' AND " +
            " d.deal_source=:userType AND ( d.title LIKE %:searchTerm% OR d.sub_title LIKE %:searchTerm% OR " +
            " d.description LIKE %:searchTerm% OR d.merchant_name LIKE %:searchTerm% OR " +
            " d.brand_names LIKE %:searchTerm% OR d.category_names LIKE %:searchTerm% OR " +
            " d.deal_code LIKE %:searchTerm% )", nativeQuery = true)
    Page<DealSearchIndex> findAllPendingDealsBySearchTerm(Pageable pageable, String searchTerm, String userType);

    /**
     * This method finds all not deleted pending deals.
     *
     * @param pageable       pageable
     * @param approvalStatus PENDING
     * @return dealSearchIndex page
     */
    Page<DealSearchIndex> findAllByApprovalStatusAndDealSourceAndIsDeletedFalse(Pageable pageable,
                                                                                ApprovalStatus approvalStatus,
                                                                                UserType userType);


    /**
     * This method find all active and approved deals for categoryId, active merchantId and searchTerm.
     *
     * @param pageable     pageable
     * @param merchantId   merchant id
     * @param categoryId   category id
     * @param searchTerm   search term
     * @param currentMills current Milliseconds
     * @return DealSearchIndex page
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE " + MERCHANT_ID + " AND " + DEAL_SOURCE + " AND "
            + VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT + " AND " + RELATED_CATEGORIES + " AND " + SEARCH)
    Page<DealSearchIndex> findAllActiveDealsByActiveMerchantIdCategoryIdSearchTerm(Pageable pageable,
                                                                                   @Param("merchantId") String merchantId,
                                                                                   @Param("categoryId") String categoryId,
                                                                                   @Param("searchTerm") String searchTerm,
                                                                                   @Param("currentMills") long currentMills,
                                                                                   @Param("dealSource") UserType dealSource);

    /**
     * This method find all active and approved deals for active merchantId and searchTerm.
     *
     * @param pageable     pageable
     * @param merchantId   merchant id
     * @param searchTerm   search term
     * @param currentMills current Milliseconds
     * @return DealSearchIndex page
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE " + MERCHANT_ID + " AND " + DEAL_SOURCE + " AND "
            + VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT + " AND " + SEARCH)
    Page<DealSearchIndex> findAllActiveDealsByActiveMerchantIdAndSearchTerm(Pageable pageable,
                                                                            @Param("merchantId") String merchantId,
                                                                            @Param("searchTerm") String searchTerm,
                                                                            @Param("currentMills") long currentMills,
                                                                            @Param("dealSource") UserType dealSource);

    /**
     * This method find all active and approved deals for active merchantId and categoryId.
     *
     * @param pageable     pageable
     * @param merchantId   merchant id
     * @param categoryId   category id
     * @param currentMills current Milliseconds
     * @return DealSearchIndex page
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE " + MERCHANT_ID + " AND " + DEAL_SOURCE + " AND "
            + VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT + " AND " + RELATED_CATEGORIES)
    Page<DealSearchIndex> findAllActiveDealsByActiveMerchantIdAndCategoryId(Pageable pageable,
                                                                            @Param("merchantId") String merchantId,
                                                                            @Param("categoryId") String categoryId,
                                                                            @Param("currentMills") long currentMills,
                                                                            @Param("dealSource") UserType dealSource);

    /**
     * This method find all active and approved deals for categoryId and searchTerm.
     *
     * @param pageable     pageable
     * @param categoryId   category id
     * @param searchTerm   search term
     * @param currentMills current Milliseconds
     * @return DealSearchIndex page
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE " + VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT + " AND " + DEAL_SOURCE + " AND "
            + RELATED_CATEGORIES + " AND " + SEARCH)
    Page<DealSearchIndex> findAllActiveDealsCategoryIdAndSearchTermAndActiveMerchant(Pageable pageable,
                                                                                     @Param("categoryId") String categoryId,
                                                                                     @Param("searchTerm") String searchTerm,
                                                                                     @Param("currentMills") long currentMills,
                                                                                     @Param("dealSource") UserType dealSource);

    /**
     * This method find all active deals for Merchant.
     *
     * @param currentMills currentMills
     * @param dealSource   dealSource
     * @return count active deals
     */
    @Query(value = "SELECT COUNT(ds) FROM DealSearchIndex ds WHERE " + VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT + " AND "
            + DEAL_SOURCE)
    long getActiveDealsCount(@Param("currentMills") long currentMills,
                             @Param("dealSource") UserType dealSource);

    /**
     * This method find active deals for Merchant ID.
     *
     * @param currentMills currentMills
     * @param merchantId   merchantId
     * @return count active deals for Merchant ID.
     */
    @Query(value = "SELECT COUNT(ds) FROM DealSearchIndex ds WHERE " + VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT + " AND "
            + MERCHANT_ID)
    long getActiveDealsCountUserId(@Param("currentMills") long currentMills,
                                   @Param("merchantId") String merchantId);


    /**
     * This method find all active and approved deals for categoryId.
     *
     * @param pageable     pageable
     * @param categoryId   category id
     * @param currentMills current Milliseconds
     * @return DealSearchIndex page
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE " + VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT + " AND " + DEAL_SOURCE + " AND "
            + RELATED_CATEGORIES)
    Page<DealSearchIndex> findAllActiveDealsByCategoryIdAndActiveMerchant(Pageable pageable,
                                                                          @Param("categoryId") String categoryId,
                                                                          @Param("currentMills") long currentMills,
                                                                          @Param("dealSource") UserType dealSource);

    /**
     * This method find all active and approved deals for active merchantId.
     *
     * @param pageable     pageable
     * @param merchantId   merchant id
     * @param currentMills current Milliseconds
     * @return DealSearchIndex page
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE " + MERCHANT_ID + " AND " + DEAL_SOURCE + " AND "
            + VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT)
    Page<DealSearchIndex> findAllActiveDealsByActiveMerchantId(Pageable pageable,
                                                               String merchantId,
                                                               @Param("currentMills") long currentMills,
                                                               @Param("dealSource") UserType dealSource);

    /**
     * This method will return all active deals for active merchants by category id, merchant id, search term and order by expiryOn.
     * Returns deals that expire within the specified expiry threshold.
     *
     * @param pageable       pageable
     * @param merchantId     merchant id
     * @param categoryId     category id
     * @param searchTerm     search term
     * @param currentMills   current milliSeconds
     * @param expiryThreshold expiry threshold in milliseconds (deals must expire before or at this time)
     * @return DealSearchIndex Page
     */
    @Query(value = "SELECT ds from DealSearchIndex ds WHERE ds.merchantId=:merchantId AND ds.dealSource=:userType AND "
            + VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT + " AND ds.expiredOn <= :expiryThreshold AND ds.relatedCategories LIKE %:categoryId% AND " +
            "( ds.title LIKE %:searchTerm% OR ds.subTitle LIKE %:searchTerm% OR ds.description LIKE %:searchTerm% " +
            "OR ds.merchantName LIKE %:searchTerm% OR ds.brandNames LIKE %:searchTerm% OR " +
            " ds.categoryNames LIKE %:searchTerm% OR ds.dealCode LIKE %:searchTerm% )")
    Page<DealSearchIndex> findAllValidDealsByMerchantIdCategoryIdSearchTerm(Pageable pageable,
                                                                            @Param("merchantId") String merchantId,
                                                                            @Param("categoryId") String categoryId,
                                                                            @Param("searchTerm") String searchTerm,
                                                                            @Param("currentMills") long currentMills,
                                                                            @Param("expiryThreshold") long expiryThreshold,
                                                                            @Param("userType") UserType userType);

    /**
     * This method will return all active deals for active merchants by, merchant id, search term and order by expiryOn.
     * Returns deals that expire within the specified expiry threshold.
     *
     * @param pageable       pageable
     * @param merchantId     merchant id
     * @param searchTerm     search term
     * @param currentMills   current milliSeconds
     * @param expiryThreshold expiry threshold in milliseconds (deals must expire before or at this time)
     * @return DealSearchIndex Page
     */
    @Query(value = "SELECT ds from DealSearchIndex ds WHERE ds.merchantId=:merchantId AND ds.dealSource=:userType AND " +
            VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT + " AND ds.expiredOn <= :expiryThreshold AND ( ds.title LIKE %:searchTerm% OR ds.subTitle LIKE %:searchTerm% " +
            "OR ds.description LIKE %:searchTerm% OR ds.merchantName LIKE %:searchTerm% OR ds.brandNames " +
            "LIKE %:searchTerm% OR ds.categoryNames LIKE %:searchTerm% OR ds.dealCode LIKE %:searchTerm% )")
    Page<DealSearchIndex> findAllValidDealsByMerchantIdAndSearchTerm(Pageable pageable,
                                                                     @Param("merchantId") String merchantId,
                                                                     @Param("searchTerm") String searchTerm,
                                                                     @Param("currentMills") long currentMills,
                                                                     @Param("expiryThreshold") long expiryThreshold,
                                                                     @Param("userType") UserType userType);

    /**
     * This method will return all active deals for active merchants by, merchant id, category id and order by expiryOn.
     * Returns deals that expire within the specified expiry threshold.
     *
     * @param pageable       pageable
     * @param merchantId     merchant id
     * @param categoryId     category id
     * @param currentMills   current milliSeconds
     * @param expiryThreshold expiry threshold in milliseconds (deals must expire before or at this time)
     * @return DealSearchIndex Page
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE ds.merchantId=:merchantId AND ds.dealSource=:userType AND " +
            VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT + " AND ds.expiredOn <= :expiryThreshold AND ds.relatedCategories LIKE %:categoryId%")
    Page<DealSearchIndex> findAllValidDealsByMerchantIdAndCategoryId(Pageable pageable,
                                                                     @Param("merchantId") String merchantId,
                                                                     @Param("categoryId") String categoryId,
                                                                     @Param("currentMills") long currentMills,
                                                                     @Param("expiryThreshold") long expiryThreshold,
                                                                     @Param("userType") UserType userType);

    /**
     * This method will return all active deals for active merchants by category id, search term and order by expiryOn.
     * Returns deals that expire within the specified expiry threshold.
     *
     * @param pageable       pageable
     * @param categoryId     category id
     * @param searchTerm     search term
     * @param currentMills   current milliSeconds
     * @param expiryThreshold expiry threshold in milliseconds (deals must expire before or at this time)
     * @return DealSearchIndex Page
     */
    @Query(value = "SELECT ds from DealSearchIndex ds WHERE ds.dealSource=:userType AND " +
            VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT + " AND ds.expiredOn <= :expiryThreshold AND ds.relatedCategories LIKE %:categoryId% AND " +
            " ( ds.title LIKE %:searchTerm% OR ds.subTitle LIKE %:searchTerm% OR ds.brandNames LIKE %:searchTerm% OR " +
            " ds.categoryNames LIKE %:searchTerm% OR ds.dealCode LIKE %:searchTerm% )")
    Page<DealSearchIndex> findAllValidDealsByCategoryIdAndSearchTerm(Pageable pageable,
                                                                     @Param("categoryId") String categoryId,
                                                                     @Param("searchTerm") String searchTerm,
                                                                     @Param("currentMills") long currentMills,
                                                                     @Param("expiryThreshold") long expiryThreshold,
                                                                     @Param("userType") UserType userType);

    /**
     * This method will return all active deals for active merchants by category id and order by expiryOn.
     * Returns deals that expire within the specified expiry threshold.
     *
     * @param pageable       pageable
     * @param categoryId     category id
     * @param currentMills   current milliSeconds
     * @param expiryThreshold expiry threshold in milliseconds (deals must expire before or at this time)
     * @return DealSearchIndex Page
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE ds.dealSource=:userType AND " +
            VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT + " AND ds.expiredOn <= :expiryThreshold AND ds.relatedCategories LIKE %:categoryId%")
    Page<DealSearchIndex> findAllValidDealsByCategoryId(Pageable pageable,
                                                        @Param("categoryId") String categoryId,
                                                        @Param("currentMills") long currentMills,
                                                        @Param("expiryThreshold") long expiryThreshold,
                                                        @Param("userType") UserType userType);

    /**
     * This method will return all active deals for active merchants by merchant id and order by expiryOn.
     * Returns deals that expire within the specified expiry threshold.
     *
     * @param pageable       pageable
     * @param merchantId     merchant id
     * @param currentMills   current milliSeconds
     * @param expiryThreshold expiry threshold in milliseconds (deals must expire before or at this time)
     * @return DealSearchIndex Page
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE ds.dealSource=:userType AND " +
            VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT + " AND ds.expiredOn <= :expiryThreshold AND ds.merchantId=:merchantId")
    Page<DealSearchIndex> findAllValidDealsByMerchantId(Pageable pageable,
                                                        String merchantId,
                                                        @Param("currentMills") long currentMills,
                                                        @Param("expiryThreshold") long expiryThreshold,
                                                        @Param("userType") UserType userType);

    /**
     * This method will return all active deals for active merchants by search term and order by expiryOn.
     * Returns deals that expire within the specified expiry threshold.
     *
     * @param pageable       pageable
     * @param searchTerm     search term
     * @param currentMills   current milliSeconds
     * @param expiryThreshold expiry threshold in milliseconds (deals must expire before or at this time)
     * @return DealSearchIndex Page
     */
    @Query(value = "SELECT ds from DealSearchIndex ds WHERE ds.dealSource=:userType AND " + VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT
            + " AND ds.expiredOn <= :expiryThreshold AND (ds.title LIKE %:searchTerm% OR ds.subTitle LIKE %:searchTerm% OR ds.description" +
            " LIKE %:searchTerm% OR ds.merchantName LIKE %:searchTerm% OR ds.brandNames " +
            "LIKE %:searchTerm% OR ds.categoryNames LIKE %:searchTerm% OR ds.dealCode LIKE %:searchTerm% )")
    Page<DealSearchIndex> findAllValidDealsBySearchTerm(Pageable pageable,
                                                        @Param("searchTerm") String searchTerm,
                                                        @Param("currentMills") long currentMills,
                                                        @Param("expiryThreshold") long expiryThreshold,
                                                        @Param("userType") UserType userType);

    /**
     * This method will return all active deals for active merchants order by expiryOn.
     *
     * @param pageable     pageable
     * @param currentMills current milliSeconds
     * @return DealSearchIndex Page
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE ds.dealSource=:userType AND " +
            VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT)
    Page<DealSearchIndex> findAllValidDeals(Pageable pageable, @Param("currentMills") long currentMills,
                                            @Param("userType") UserType userType);

    /**
     * This method will return all active deals for active merchants order by expiryOn.
     * Returns deals that expire within the specified expiry threshold.
     *
     * @param pageable       pageable
     * @param currentMills   current milliSeconds
     * @param expiryThreshold expiry threshold in milliseconds (deals must expire before or at this time)
     * @return DealSearchIndex Page
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE ds.dealSource=:userType AND " +
            VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT + " AND ds.expiredOn <= :expiryThreshold")
    Page<DealSearchIndex> findAllValidDealsWithExpiryThreshold(Pageable pageable, @Param("currentMills") long currentMills,
                                            @Param("expiryThreshold") long expiryThreshold,
                                            @Param("userType") UserType userType);

    /**
     * This method return randomly selected 10 deals which not expired yet for merchant.
     *
     * @param currentMills currentMills - current time in milliseconds
     * @return deal list
     */
    @Query(value = "SELECT * FROM search_deal d WHERE d.is_deleted = false AND d.deal_source='MERCHANT'" +
            " AND d.approval_status= 'APPROVED' AND d.expired_on > ?1 AND d.valid_from <= ?1" +
            " AND d.is_active_merchant = true ORDER BY RAND() LIMIT ?2",
            nativeQuery = true)
    List<DealSearchIndex> getRandomDealsForMerchant(long currentMills, int dealsOfTheDayLimit);

    /**
     * This method return randomly selected 10 deals which not expired yet for bank.
     *
     * @param currentMills       currentMills - current time in milliseconds
     * @param dealsOfTheDayLimit dealsOfTheDaySize
     * @return deal List
     */
    @Query(value = "SELECT * FROM search_deal d WHERE d.is_deleted = false AND d.deal_source='BANK' " +
            "AND d.approval_status= 'APPROVED' AND d.expired_on > ?1 AND d.valid_from <= ?1 AND " +
            "d.is_active_merchant = true ORDER BY RAND() LIMIT ?2",
            nativeQuery = true)
    List<DealSearchIndex> getRandomDealsForBank(long currentMills, int dealsOfTheDayLimit);

    /**
     * This method returns active and valid deal count for a specific category id.
     *
     * @param currentMills current milliseconds
     * @param categoryId   category id
     * @return active and valid deal count
     */
    @Query(value = "SELECT COUNT(ds) FROM DealSearchIndex ds WHERE " + VALID_ACTIVE_DEALS
            + " AND ds.relatedCategories LIKE %:categoryId%")
    long getActiveValidDealsCountByCategoryId(@Param("currentMills") long currentMills, String categoryId);

    /**
     * This method returns active and valid deal count for a specific brand id.
     *
     * @param currentMills current milliseconds
     * @param brandId      brand id
     * @return active and valid deal count
     */
    @Query(value = "SELECT COUNT(ds) FROM DealSearchIndex ds WHERE " + VALID_ACTIVE_DEALS
            + " AND ds.relatedBrands LIKE %:brandId%")
    long getActiveValidDealsCountByBrandId(@Param("currentMills") long currentMills, String brandId);

    /**
     * This method finds all active and approved deals by search.
     *
     * @param pageable     pagebale
     * @param searchTerm   search term
     * @param currentMills current Milliseconds
     * @return DealSearchIndex Page
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE " + VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT + " AND "
            + DEAL_SOURCE + " AND " + SEARCH)
    Page<DealSearchIndex> findAllApprovedActiveDealsBySearchTermAndActiveMerchant(Pageable pageable,
                                                                                  @Param("searchTerm") String searchTerm,
                                                                                  @Param("currentMills") long currentMills,
                                                                                  @Param("dealSource") UserType dealSource);

    /**
     * This method finds all active and approved deals.
     *
     * @param pageable     pageable
     * @param currentMills current milliseconds
     * @return DealSearchIndex Page
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE " + DEAL_SOURCE + " AND " + VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT)
    Page<DealSearchIndex> findAllActiveDealsWithActiveMerchant(Pageable pageable,
                                                               @Param("currentMills") long currentMills,
                                                               @Param("dealSource") UserType dealSource);

    /**
     * Returns all active and approved deals for active merchants (no pagination).
     * Same criteria as findAllActiveDealsWithActiveMerchant; used for aggregations e.g. total savings.
     *
     * @param currentMills current milliseconds
     * @param dealSource    MERCHANT or BANK
     * @return list of active deal search indices
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE " + DEAL_SOURCE + " AND " + VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT)
    List<DealSearchIndex> findAllActiveDealsWithActiveMerchantList(@Param("currentMills") long currentMills,
                                                                   @Param("dealSource") UserType dealSource);

    /**
     * This method is used to get total number of active deals for merchantId.
     *
     * @param merchantId   merchantId
     * @param currentMills currentMills
     * @return total number of active deals.
     */
    @Query("SELECT COUNT(ds) FROM DealSearchIndex ds WHERE " + MERCHANT_ID + " AND " +
            VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT)
    int getTotalNumberOfActiveDealsForMerchantId(@Param("merchantId") String merchantId,
                                                 @Param("currentMills") long currentMills);

    /**
     * This method is used to get total deals for userId
     *
     * @param merchantId merchantId
     * @return total number of deals for userId.
     */
    long countByMerchantIdAndIsDeletedFalse(String merchantId);

    /**
     * This method will return DealSearchIndex list by merchant ids
     *
     * @param merchantIds merchantIds List
     * @return DealSearchIndex List
     */
    List<DealSearchIndex> getAllByMerchantIdIn(List<String> merchantIds);

    /**
     * This method find all active deal ids for active userId.
     *
     * @param merchantId   merchant id
     * @param currentMills current Milliseconds
     * @return dealId list.
     */
    @Query(value = "SELECT ds.id FROM DealSearchIndex ds WHERE " + MERCHANT_ID + " AND " +
            VALID_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT)
    List<String> getActiveDealsByActiveBankId(String merchantId, long currentMills);

    /**
     * This method returns a count of deals for specific category id
     *
     * @param categoryId category id
     * @return count deals Count By CategoryId
     */
    long countByRelatedCategoriesContainsAndIsDeletedFalse(String categoryId);

    /**
     * This method return a count of deal by brand id
     *
     * @param brandId brand id
     * @return count deals Count By BrandId
     */
    long countByRelatedBrandsContainsAndIsDeletedFalse(String brandId);

    /**
     * This method used to get DealSearchIndex page by category id.
     *
     * @param pageable   pageable
     * @param categoryId category id
     * @return DealSearchIndex page
     */
    Page<DealSearchIndex> getAllByRelatedCategoriesContaining(Pageable pageable, String categoryId);

    /**
     * This method used to get DealSearchIndex page by brand id.
     *
     * @param pageable pageable
     * @param brandId  brand id
     * @return DealSearchIndex page
     */
    Page<DealSearchIndex> getAllByRelatedBrandsContaining(Pageable pageable, String brandId);

    /**
     * This method used for set isDealOfTheDay flag false.
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE DealSearchIndex SET isDealOfTheDay = false WHERE isDealOfTheDay = true ")
    void disableDealOfTheDayFlags();

    /**
     * This method will validate userId with dealId and checks if deal is active.
     *
     * @param userId    userId
     * @param dealId    dealId
     * @param timeStamp timeStamp
     * @return true/false
     */
    @Query("SELECT CASE WHEN (ds.merchantId = :userId AND ds.approvalStatus = 'APPROVED' AND " +
            " ds.validFrom <= :timeStamp AND ds.expiredOn > :timeStamp) " +
            " THEN TRUE ELSE FALSE END FROM DealSearchIndex ds WHERE ds.id = :dealId")
    boolean isActiveDealForMerchant(String userId, String dealId, long timeStamp);

    /**
     * This method used to get deal list by list of dealsIds.
     *
     * @param dealIdList deal id list
     * @return DealSearchIndex list
     */
    List<DealSearchIndex> findByIdIn(List<String> dealIdList);

    /**
     * This method is used to get deals by storeId in a particular time range.
     *
     * @param currentTimeInMillis currentTimeInMillis
     * @param merchantIds         merchantIds
     * @return list of deals.
     */
    @Query("SELECT ds FROM DealSearchIndex ds WHERE ds.validFrom <= :currentTimeInMillis AND " +
            " ds.expiredOn > :currentTimeInMillis AND ds.storeId IN :merchantIds AND " + VALID_DEAL +
            " AND " + ACTIVE_MERCHANT)
    List<DealSearchIndex> getApprovedValidDealsByMerchantIds(long currentTimeInMillis,
                                                             List<String> merchantIds);
    /**
     * This method will return all active deals by merchantId.
     *
     * @param pageable   pageable
     * @param merchantId merchant id
     * @param time       time stamp
     * @return DealSearchIndex Page
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE ds.merchantId = :merchantId AND ds.expiredOn >= :time " +
            "AND ds.validFrom <= :time AND " + VALID_DEAL + "AND " + ACTIVE_MERCHANT)
    Page<DealSearchIndex> getActiveMerchantDeals(Pageable pageable, String merchantId, long time);

    /**
     * This method returns all deals with coupon.
     *
     * @param pageable pageable
     * @return page of deals.
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE ds.isCouponAvailable = true AND " +
            "ds.approvalStatus = 'APPROVED'")
    Page<DealSearchIndex> getCouponDeals(Pageable pageable);

    /**
     * This method returns all deals with coupon which matches the search term.
     *
     * @param searchTerm searchTerm
     * @param pageable   pageable
     * @return page of deals.
     */
    @Query("SELECT ds FROM DealSearchIndex ds WHERE ds.isCouponAvailable = true AND ds.approvalStatus = 'APPROVED' AND " +
            "( ds.title LIKE %:searchTerm% OR ds.dealCode LIKE %:searchTerm% OR ds.merchantName LIKE %:searchTerm% )")
    Page<DealSearchIndex> getCouponDealsWithSearchTerm(String searchTerm, Pageable pageable);

    /**
     * This method returns all deals with coupon which belongs to a merchantId.
     *
     * @param merchantId merchantId
     * @param pageable   pageable
     * @return page of deals.
     */
    @Query("SELECT ds FROM DealSearchIndex ds WHERE ds.merchantId = :merchantId AND ds.isCouponAvailable = true AND " +
            "ds.approvalStatus = 'APPROVED'")
    Page<DealSearchIndex> getCouponDealsByMerchantId(String merchantId, Pageable pageable);

    /**
     * This method returns all deals with coupon which matches the search term and merchantId.
     *
     * @param merchantId merchantId
     * @param searchTerm searchTerm
     * @param pageable   pageable
     * @return page of deals.
     */
    @Query("SELECT ds FROM DealSearchIndex ds WHERE ds.merchantId = :merchantId AND ds.isCouponAvailable = true AND " +
            "ds.approvalStatus = 'APPROVED' AND " +
            "(ds.title LIKE %:searchTerm% OR ds.dealCode LIKE %:searchTerm% )")
    Page<DealSearchIndex> getCouponDealsWithMerchantIdAndSearchTerm(String merchantId, String searchTerm,
                                                                    Pageable pageable);

    /**
     * This method returns all deals with free or paid coupons.
     *
     * @param isFreeCoupons true/false
     * @param pageable      pageable
     * @return page of deals.
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE ds.isCouponAvailable = true AND " +
            "ds.freeCoupons = :isFreeCoupons AND ds.approvalStatus = 'APPROVED'")
    Page<DealSearchIndex> getCouponDealsByCouponType(boolean isFreeCoupons, Pageable pageable);

    /**
     * This method returns all deals with free or paid coupons which belongs to a merchantId.
     *
     * @param merchantId    merchantId
     * @param isFreeCoupons true/false
     * @param pageable      pageable
     * @return page of deals.
     */
    @Query(value = "SELECT ds FROM DealSearchIndex ds WHERE ds.isCouponAvailable = true AND ds.merchantId = :merchantId " +
            "AND ds.freeCoupons = :isFreeCoupons AND ds.approvalStatus = 'APPROVED'")
    Page<DealSearchIndex> getCouponDealsByMerchantIdAndCouponType(String merchantId, boolean isFreeCoupons,
                                                                  Pageable pageable);

    /**
     * This method returns all deals with coupon which matches the search term & coupon type.
     *
     * @param searchTerm    searchTerm
     * @param isFreeCoupons true/false
     * @param pageable      pageable
     * @return page of deals.
     */
    @Query("SELECT ds FROM DealSearchIndex ds WHERE ds.isCouponAvailable = true " +
            "AND ds.freeCoupons = :isFreeCoupons AND ds.approvalStatus = 'APPROVED'" +
            " AND ( ds.title LIKE %:searchTerm% OR ds.dealCode LIKE %:searchTerm% OR ds.merchantName LIKE %:searchTerm% )")
    Page<DealSearchIndex> getCouponDealsWithSearchTermAndCouponType(String searchTerm, boolean isFreeCoupons,
                                                                    Pageable pageable);

    /**
     * This method returns all deals with coupon which matches the search term, merchantId & coupon type.
     *
     * @param merchantId    merchantId
     * @param searchTerm    searchTerm
     * @param isFreeCoupons true/false
     * @param pageable      pageable
     * @return page of deals.
     */
    @Query("SELECT ds FROM DealSearchIndex ds WHERE ds.merchantId = :merchantId AND ds.isCouponAvailable = true " +
            "AND ds.approvalStatus = 'APPROVED' AND ds.freeCoupons = :isFreeCoupons" +
            " AND (ds.title LIKE %:searchTerm% OR ds.dealCode LIKE %:searchTerm% )")
    Page<DealSearchIndex> getCouponDealsWithMerchantIdAndSearchTermAndCouponType(String merchantId, String searchTerm,
                                                                                 boolean isFreeCoupons, Pageable pageable);

    /**
     * Counts the number of active and approved deals for a given merchant within a specific date range.
     *
     * @param merchantId the merchant ID to filter deals by
     * @param startDate  the start of the date range (epoch milliseconds)
     * @param endDate    the end of the date range (epoch milliseconds)
     * @return the count of matching active deals for the given merchant
     */
    @Query("SELECT COUNT(ds) FROM DealSearchIndex ds " +
            "WHERE ds.merchantId = :merchantId " +
            "AND ds.validFrom <= :endDate " +
            "AND ds.expiredOn >= :startDate " +
            "AND ds.isDeleted = false " +
            "AND ds.approvalStatus = 'APPROVED'")
    long countActiveDealsByMerchantIdAndDateRange(@Param("merchantId") String merchantId,
                                                  @Param("startDate") long startDate,
                                                  @Param("endDate") long endDate);

    /**
     * This method retrieves all active and approved deals for a given merchant within a specific date range.
     * This is primarily used when we need to apply additional in-memory filtering (e.g. coupon availability)
     * on top of the basic active-deal criteria.
     *
     * @param merchantId the merchant ID to filter deals by
     * @param startDate  the start of the date range (epoch milliseconds)
     * @param endDate    the end of the date range (epoch milliseconds)
     * @return list of matching active deals for the given merchant
     */
    @Query("SELECT ds FROM DealSearchIndex ds " +
            "WHERE ds.merchantId = :merchantId " +
            "AND ds.validFrom <= :endDate " +
            "AND ds.expiredOn >= :startDate " +
            "AND ds.isDeleted = false " +
            "AND ds.approvalStatus = 'APPROVED'")
    List<DealSearchIndex> findActiveDealsByMerchantIdAndDateRange(@Param("merchantId") String merchantId,
                                                                  @Param("startDate") long startDate,
                                                                  @Param("endDate") long endDate);

    /**
     * Find deals with advanced filtering by status, category, promoType, merchantId, searchTerm, and date range
     *
     * @param pageable   pageable
     * @param status     status filter (ALL, active, inactive, Draft, Expired, Deactivated)
     * @param categoryId category id (ALL or specific)
     * @param promoTypeId promo type id (ALL or specific)
     * @param merchantId merchant id
     * @param searchTerm search term
     * @param userType   user type
     * @param startMillis start date in milliseconds
     * @param endMillis   end date in milliseconds
     * @return Page of DealSearchIndex
     */
    @Query(value = "SELECT * FROM search_deal ds WHERE " +
            "ds.deal_source = :userType " +
            "AND ds.is_deleted = false " +
            "AND (:merchantId = 'ALL' OR ds.merchant_id = :merchantId) " +
            "AND (:categoryId = 'ALL' OR ds.related_categories LIKE CONCAT('%', :categoryId, '%')) " +
            "AND (:promoTypeId = 'ALL' OR ds.promo_type_id = :promoTypeId) " +
            "AND (:searchTerm = 'ALL' OR ds.title LIKE CONCAT('%', :searchTerm, '%') " +
            "     OR ds.sub_title LIKE CONCAT('%', :searchTerm, '%') " +
            "     OR ds.description LIKE CONCAT('%', :searchTerm, '%') " +
            "     OR ds.merchant_name LIKE CONCAT('%', :searchTerm, '%') " +
            "     OR ds.brand_names LIKE CONCAT('%', :searchTerm, '%') " +
            "     OR ds.category_names LIKE CONCAT('%', :searchTerm, '%') " +
            "     OR ds.deal_code LIKE CONCAT('%', :searchTerm, '%')) " +
            "AND (:filterByDateRange = 0 OR (ds.created_at >= :startMillis AND ds.created_at <= :endMillis)) " +
            "AND (" +
            "  (:status = 'ALL') OR " +
            "  (:status = 'active' AND ds.approval_status = 'APPROVED' AND ds.is_coupon_available = true AND ds.valid_from <= :endMillis AND ds.expired_on >= :endMillis) OR " +
            "  (:status = 'inactive' AND ds.approval_status = 'APPROVED' AND ds.is_coupon_available = true AND ds.valid_from > :endMillis AND ds.expired_on >= :endMillis) OR " +
            "  (:status = 'Draft' AND ds.approval_status = 'DRAFT') OR " +
            "  (:status = 'Expired' AND ds.approval_status = 'APPROVED' AND ds.expired_on < :endMillis) OR " +
            "  (:status = 'Deactivated' AND ds.approval_status = 'DEACTIVATE')" +
            ") " +
            "ORDER BY ds.created_at DESC, ds.id DESC",
            nativeQuery = true)
    Page<DealSearchIndex> findDealsWithAdvancedFilter(Pageable pageable,
                                                       @Param("status") String status,
                                                       @Param("categoryId") String categoryId,
                                                       @Param("promoTypeId") String promoTypeId,
                                                       @Param("merchantId") String merchantId,
                                                       @Param("searchTerm") String searchTerm,
                                                       @Param("userType") String userType,
                                                       @Param("startMillis") long startMillis,
                                                       @Param("endMillis") long endMillis,
                                                       @Param("filterByDateRange") int filterByDateRange);


}
