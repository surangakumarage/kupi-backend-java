package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.CombinedDealRequest;
import com.kupi.cc.base.domain.entity.RequestADeal;
import com.kupi.cc.base.domain.entity.RequestADealSearchIndex;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.RequestADealSearchIndexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class should replace from a search engine.
 */
@Service
public class RequestADealSearchIndexService {

    private static final String ALL = "ALL";
    private final RequestADealSearchIndexRepository requestADealSearchIndexRepository;

    @Autowired
    public RequestADealSearchIndexService(RequestADealSearchIndexRepository requestADealSearchIndexRepository) {
        this.requestADealSearchIndexRepository = requestADealSearchIndexRepository;
    }

    /**
     * This method saves RequestADealSearchIndices.
     *
     * @param requestADeal RequestADeal
     * @param merchantName merchantName
     */
    public void save(RequestADeal requestADeal, String merchantName) {
        try {
            RequestADealSearchIndex requestADealSearchIndex = new RequestADealSearchIndex(requestADeal, merchantName);
            requestADealSearchIndexRepository.save(requestADealSearchIndex);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving requestADealSearchIndex, requestADealId: "
                    + requestADeal.getId() + " into database was failed.", e);
        }
    }

    /**
     * This method returns All combinedRequestADeals
     *
     * @param pageable   pageable
     * @param merchantId merchant id
     * @param searchTerm search term
     * @return CombinedDealRequest Page
     */
    public Page<CombinedDealRequest> getAllCombinedDealRequests(Pageable pageable,
                                                                String merchantId,
                                                                String searchTerm, UserType userType) {
        try {
            if (merchantId.equals(ALL) && !searchTerm.equals(ALL)) {
                return requestADealSearchIndexRepository.getAllRequestADealSearchByMerchantNameAndUserType(pageable, searchTerm, userType);
            } else if (merchantId.equals(ALL)) {
                return requestADealSearchIndexRepository.getAllRequestADealByUserType(pageable, userType);
            } else if (searchTerm.equals(ALL)) {
                return requestADealSearchIndexRepository.getRequestADealByMerchantIdAndUserType(pageable, merchantId, userType);
            } else {
                return requestADealSearchIndexRepository.getRequestADealSearchByMerchantNameAndUserType(pageable, merchantId,
                        searchTerm, userType);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("fetching combinedDealRequest from the database was failed.", e);
        }
    }

    /**
     * This method updates merchant info.
     *
     * @param merchantMap merchant id, MerchantBusinessResponseDto Map
     */
    public void updateMerchantInfo(Map<String, MerchantBusinessResponseDto> merchantMap) {
        try {
            var requestADealSearchIndexList =
                    requestADealSearchIndexRepository.getAllByMerchantIdIn(new ArrayList<>(merchantMap.keySet()));
            for (var requestADealSearchIndex : requestADealSearchIndexList) {
                requestADealSearchIndex.setMerchantName(merchantMap.get(requestADealSearchIndex.getMerchantId()).getName());
            }
            requestADealSearchIndexRepository.saveAll(requestADealSearchIndexList);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Updating merchant info for requestADealSearchIndex was failed", e);
        }
    }


    /**
     * This method returns request a deal index page by category id.
     *
     * @param categoryId category id
     * @return RequestADealSearchIndex page
     */
    public Page<RequestADealSearchIndex> getRequestADDealSearchIndexByCategoryId(Pageable pageable, String categoryId) {
        try {
            return requestADealSearchIndexRepository.getAllByCategoryId(pageable, categoryId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading requestADealSearchIndexes for category id: " + categoryId + " was failed", e);
        }
    }

    /**
     * This method saves list of dealSearchIndices.
     *
     * @param dealSearchIndexList dealSearchIndexList
     */
    public void saveListOfRequestADealIndex(List<RequestADealSearchIndex> dealSearchIndexList) {
        try {
            requestADealSearchIndexRepository.saveAll(dealSearchIndexList);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving requestADealSearchIndexes list to database was failed", e);
        }
    }

    /**
     * This method returns request a deal index page by brand id.
     *
     * @param pageable pageable
     * @param brandId  brand id
     * @return RequestADealSearchIndex page
     */
    public Page<RequestADealSearchIndex> getRequestADDealSearchIndexesByBrandId(Pageable pageable, String brandId) {
        try {
            return requestADealSearchIndexRepository.getAllByBrandId(pageable, brandId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading requestADealSearchIndexes for brand id: " + brandId + " was failed", e);
        }
    }
}
