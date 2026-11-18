package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.CombinedDealRequest;
import com.kupi.cc.base.domain.entity.RequestADealSearchIndex;
import com.kupi.cc.base.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * This interface should replace from a search engine.
 */
public interface RequestADealSearchIndexRepository extends JpaRepository<RequestADealSearchIndex, String> {

    /**
     * This method returns all CombinedDealRequests by All merchants and by a search term. This API is for admin users
     *
     * @param pageable   pageable
     * @param searchTerm search term
     * @return PageList of CombinedDealRequest
     */
    @Query(value = "select new CombinedDealRequest(sd.categoryId, sd.categoryName, sd.brandId,sd.brandName," +
            "sd.merchantId,sd.merchantName, count(sd)) FROM RequestADealSearchIndex sd where sd.toUserType =:userType" +
            " and sd.brandName like %:searchTerm% OR sd.categoryName like %:searchTerm% OR sd.merchantName like %:searchTerm%" +
            " group by sd.categoryId,sd.brandId, sd.merchantId,sd.categoryName,sd.brandName,sd.merchantName")
    Page<CombinedDealRequest> getAllRequestADealSearchByMerchantNameAndUserType(Pageable pageable, String searchTerm, UserType userType);

    /**
     * This method returns all CombinedDealRequests by All merchants. This API is for admin users
     *
     * @param pageable pageable
     * @return PageList of CombinedDealRequest
     */
    @Query(value = "select new CombinedDealRequest(sd.categoryId, sd.categoryName, sd.brandId,sd.brandName," +
            "sd.merchantId,sd.merchantName, count(sd)) FROM RequestADealSearchIndex sd where sd.toUserType =:userType" +
            " group by sd.categoryId, sd.brandId, sd.merchantId,sd.categoryName,sd.brandName,sd.merchantName")
    Page<CombinedDealRequest> getAllRequestADealByUserType(Pageable pageable, UserType userType);

    /**
     * This method returns all CombinedDealRequests by a merchants and a search term. This API is for merchant users
     *
     * @param pageable   pageable
     * @param merchantId merchant id
     * @param searchTerm search term
     * @return PageList of CombinedDealRequest
     */
    @Query(value = "select new CombinedDealRequest(sd.categoryId, sd.categoryName, sd.brandId,sd.brandName," +
            "sd.merchantId,sd.merchantName, count(sd)) FROM RequestADealSearchIndex sd where " +
            "sd.toUserType =:userType and sd.merchantId=:merchantId AND (sd.brandName like %:searchTerm% OR" +
            " sd.categoryName like %:searchTerm%) group by sd.categoryId,sd.brandId, sd.merchantId,sd.categoryName," +
            "sd.brandName,sd.merchantName")
    Page<CombinedDealRequest> getRequestADealSearchByMerchantNameAndUserType(Pageable pageable,
                                                                             String merchantId, String searchTerm, UserType userType);

    /**
     * This method returns all CombinedDealRequests by a merchants. This API is for merchant users.
     *
     * @param pageable   pageable
     * @param merchantId merchant id
     * @return PageList of CombinedDealRequest
     */
    @Query(value = "select new CombinedDealRequest(sd.categoryId, sd.categoryName, sd.brandId,sd.brandName," +
            "sd.merchantId,sd.merchantName, count(sd)) FROM RequestADealSearchIndex sd " +
            "where sd.toUserType =:userType and sd.merchantId=:merchantId group by sd.categoryId,sd.brandId, sd.merchantId," +
            "sd.categoryName,sd.brandName,sd.merchantName")
    Page<CombinedDealRequest> getRequestADealByMerchantIdAndUserType(Pageable pageable, String merchantId, UserType userType);

    /**
     * This method will return RequestADealSearchIndex List by merchant ids List
     *
     * @param merchantIds merchantIds List
     * @return RequestADealSearchIndex List
     */
    List<RequestADealSearchIndex> getAllByMerchantIdIn(List<String> merchantIds);

    /**
     * This method will return requestADealSearchIndex page by category id.
     *
     * @param pageable   pageable
     * @param categoryId category id
     * @return RequestADealSearchIndex page
     */
    Page<RequestADealSearchIndex> getAllByCategoryId(Pageable pageable, String categoryId);


    /**
     * This method will return requestADealSearchIndex page by brand id.
     *
     * @param pageable   pageable
     * @param brandId brand id
     * @return RequestADealSearchIndex page
     */
        Page<RequestADealSearchIndex> getAllByBrandId(Pageable pageable, String brandId);

    /**
     * This method deletes entries, if mobile userIds are not in the list.
     *
     * @param userIds mobile userIds.
     */
    @Transactional
    void deleteByUserIdNotIn(List<String> userIds);
}
