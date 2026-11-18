package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.Brand;
import com.kupi.cc.base.domain.entity.Category;
import com.kupi.cc.base.domain.entity.OfferType;
import com.kupi.cc.base.domain.entity.RequestADeal;
import com.kupi.cc.base.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RequestADealRepository extends JpaRepository<RequestADeal, String> {

    /**
     * This query returns all request deals by user Id
     *
     * @param pageable pageable
     * @param userId   userId
     * @return page
     */
    Page<RequestADeal> findAllByUserIdAndToUserType(Pageable pageable, String userId, UserType userType);

    /**
     * This query returns count of combined deal request by merchant, category, brand.
     *
     * @param merchantId merchant id
     * @param category   category
     * @param brand      brand
     * @return count
     */
    long countRequestADealsByMerchantIdAndCategoryAndBrandAndToUserType(String merchantId,
                                                                        Category category, Brand brand, UserType userType);

    /**
     * This query returns all request a deal by merchant id, brand id & category id
     *
     * @param pageable   pageable
     * @param merchantId merchant id
     * @param brandId    brand id
     * @param categoryId category id
     * @return RequestADeal List
     */
    @Query(value = "SELECT rd FROM RequestADeal rd where rd.toUserType=:userType and rd.merchantId=:merchantId " +
            "AND rd.brand.id=:brandId AND rd.category.id=:categoryId")
    Page<RequestADeal> getAllByMerchantIdAndBrandIdCategoryIdAndUserType(Pageable pageable, String merchantId,
                                                                         String brandId, String categoryId, UserType userType);

    /**
     * This query returns all request a deal by merchant id, category id
     *
     * @param pageable   pageable
     * @param merchantId merchant id
     * @param categoryId category id
     * @return RequestADeal List
     */
    @Query(value = "SELECT rd FROM RequestADeal rd where rd.toUserType=:userType and rd.merchantId=:merchantId " +
            "AND rd.brand.id is null AND rd.category.id=:categoryId")
    Page<RequestADeal> getAllByMerchantIdCategoryIdAndUserType(Pageable pageable, String merchantId, String categoryId, UserType userType);

    /**
     * This query returns request a deal list for a merchant with optional filters.
     * <p>
     * Filters:
     * - categoryId: pass 'ALL' to fetch all categories, or a specific category id.
     * - searchTerm: kept only for compatibility; currently not used in DB filtering.
     * - startDate / endDate: pass null to ignore the boundary, otherwise filter by createdAt between the range.
     *
     * @param pageable   pageable
     * @param merchantId merchant id (cannot be 'ALL')
     * @param categoryId category id or 'ALL'
     * @param searchTerm search term for note or 'ALL'
     * @param startDate  start date in epoch millis or 0 to ignore
     * @param endDate    end date in epoch millis or 0 to ignore
     * @param userType   toUserType
     * @return RequestADeal list
     */
    @Query("SELECT rd FROM RequestADeal rd " +
            "WHERE rd.toUserType = :userType " +
            "AND rd.merchantId = :merchantId " +
            "AND (:categoryId = 'ALL' OR rd.category.id = :categoryId) " +
            // searchTerm is not used in DB filtering; see controller for in-memory search.
            "AND (:searchTerm IS NOT NULL OR :searchTerm IS NULL) " +
            "AND (:startDate IS NULL OR rd.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR rd.createdAt <= :endDate)")
    Page<RequestADeal> getAllByMerchantIdWithFilters(Pageable pageable,
                                                     String merchantId,
                                                     String categoryId,
                                                     String searchTerm,
                                                     Long startDate,
                                                     Long endDate,
                                                     UserType userType);

    /**
     * This method is used to get total number of new deal request.
     *
     * @param timestamp timestamp
     * @return total number of new deal request
     */
    long countByCreatedAtGreaterThanEqual(long timestamp);

    /**
     * This method is used to get total number of new deal request for userId.
     *
     * @param userId    userId
     * @param timestamp timestamp
     * @return total number of new deal request
     */
    long countByMerchantIdAndCreatedAtGreaterThanEqual(String userId, long timestamp);

    /**
     * This method is used to get total number of deal requests for userId.
     *
     * @param userId userId
     * @return total number of deal requests
     */
    long countByMerchantId(String userId);

    /**
     * This method returns true when offerType associate with requestADeal(s).
     *
     * @param offerType offerType
     * @return true/ false
     */
    boolean existsByOfferType(OfferType offerType);

    /**
     * This method returns distinct mobile userIds.
     *
     * @param pageable pageable
     * @return page of mobile userIds.
     */
    @Query("SELECT DISTINCT r.userId FROM RequestADeal r")
    Page<String> getDistinctUserIds(Pageable pageable);

    /**
     * This method deletes entries, if mobile userIds are not in the list.
     *
     * @param userIds mobile userIds.
     */
    @Transactional
    void deleteByUserIdNotIn(List<String> userIds);
}
