package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.domain.request.DealApprovalStatusUpdateRequestDto;
import com.kupi.cc.base.domain.response.BusinessMerchantResponseDto;
import com.kupi.cc.base.domain.response.DealViewCountResponse;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.domain.response.ViewCountAnalyticResponseDto;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.DealFilterType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.InvalidDealException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.DealSearchIndexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class should replace from a search engine.
 */
@Service
public class DealSearchService {

    private static final String INVALID_DEAL_ID = "Invalid deal Id:";
    private static final String READ_FILTER_DEAL = "Reading deal from search index was failed.";
    private static final String ALL = "ALL";
    private static final String MERCHANT = "MERCHANT";
    private static final String CATEGORY = "CATEGORY";
    private static final String BRAND = "BRAND";
    private static final String SEARCH = "SEARCH";
    private static final String BIND = "_";


    private final DealSearchIndexRepository dealSearchIndexRepository;
    private final int recentlyExpireDaysAhead;

    @Autowired
    public DealSearchService(DealSearchIndexRepository dealSearchIndexRepository,
                            @Value("${deal.recentlyExpireDaysAhead:1}") int recentlyExpireDaysAhead) {
        this.dealSearchIndexRepository = dealSearchIndexRepository;
        this.recentlyExpireDaysAhead = recentlyExpireDaysAhead;
    }

    /**
     * Create deals search index
     *
     * @param dealSearchIndex dealSearchIndex
     */
    public void save(DealSearchIndex dealSearchIndex) {
        try {
            dealSearchIndexRepository.save(dealSearchIndex);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving deals search index to database was failed", e);
        }
    }

    /**
     * Find by Id
     *
     * @param dealId dealId
     * @return DealSearchIndex
     */
    public DealSearchIndex findById(String dealId) {
        try {
            Optional<DealSearchIndex> optionalDealFromDb = dealSearchIndexRepository.findById(dealId);

            if (optionalDealFromDb.isPresent()) {
                return optionalDealFromDb.get();
            } else {
                throw new InvalidDealException(INVALID_DEAL_ID + dealId);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving deals search index to database was failed", e);
        }
    }

    /**
     * Delete deal search index.
     *
     * @param dealId deal Id
     */
    public void deleteDeal(String dealId) {
        try {
            Optional<DealSearchIndex> optionalDealFromDb = dealSearchIndexRepository.getNotDeletedDealById(dealId);
            if (optionalDealFromDb.isPresent()) {
                var filterDealFromDb = optionalDealFromDb.get();
                filterDealFromDb.setDeleted(true);
                dealSearchIndexRepository.save(filterDealFromDb);
            } else
                throw new InvalidDealException(INVALID_DEAL_ID + dealId);
        } catch (DataAccessException e) {
            throw new KupiCoreException(READ_FILTER_DEAL, e);
        }
    }

    /**
     * Approve the filter deal
     *
     * @param dealApprovalStatusUpdateRequestDto dealStatusUpdateRequestDto
     */
    public void updateApprovalStatus(DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto) {
        try {
            Optional<DealSearchIndex> optionalDealFromDb = dealSearchIndexRepository.
                    findPendingDealById(dealApprovalStatusUpdateRequestDto.getId());

            if (optionalDealFromDb.isPresent()) {
                var filterDealFromDb = optionalDealFromDb.get();
                filterDealFromDb.setApprovalStatus(dealApprovalStatusUpdateRequestDto.getApprovalStatus());
                filterDealFromDb.setComment(dealApprovalStatusUpdateRequestDto.getComment());
                filterDealFromDb.setUpdatedAt(System.currentTimeMillis());
                dealSearchIndexRepository.save(filterDealFromDb);
            } else
                throw new InvalidDealException(INVALID_DEAL_ID + dealApprovalStatusUpdateRequestDto.getId());
        } catch (DataAccessException e) {
            throw new KupiCoreException(READ_FILTER_DEAL, e);
        }
    }

    /**
     * Get filter deal list page wise by searching
     *
     * @param pageable   pageable
     * @param merchantId merchantId
     * @param searchTerm searchTerm
     * @param userType   Merchant/Bank
     * @return page
     */
    public Page<DealSearchIndex> searchDeals(Pageable pageable, String merchantId, String searchTerm, UserType userType) {
        try {
            if (merchantId.equals(ALL) && searchTerm.equals(ALL)) {
                return dealSearchIndexRepository.findAllNotDeletedDeals(pageable, userType);
            } else if (merchantId.equals(ALL)) {
                return dealSearchIndexRepository.findAllNotDeletedDealsBySearchTerm(pageable, searchTerm, userType);
            } else if (searchTerm.equals(ALL)) {
                return dealSearchIndexRepository.findAllNotDeletedDealsByMerchantId(pageable, merchantId);
            }
            return dealSearchIndexRepository
                    .findAllNotDeletedDealsByMerchantIdAndSearchTerm(pageable, merchantId, searchTerm);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading all dealSearchIndexes from the database was failed.", e);
        }
    }

    /**
     * Get PENDING deal list page
     *
     * @param pageable pageable
     * @return page
     */
    public Page<DealSearchIndex> getPendingSearchDeals(Pageable pageable, String searchTerm, UserType userType) {
        try {
            if (searchTerm.equals(ALL))
                return dealSearchIndexRepository.findAllByApprovalStatusAndDealSourceAndIsDeletedFalse(pageable,
                        ApprovalStatus.PENDING, userType);
            else
                return dealSearchIndexRepository.findAllPendingDealsBySearchTerm(pageable, searchTerm, userType.toString());
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading pending dealSearchIndexes from the database was failed.", e);
        }
    }

    /**
     * Get all searchDeals
     *
     * @param pageable   pageable
     * @param categoryId <categoryId/ALL>
     * @param merchantId <merchantId/ALL>
     * @param searchTerm <searchTerm/ALL>
     * @return DealSearchIndex Page
     */
    public Page<DealSearchIndex> getAllSearchDeals(Pageable pageable, String categoryId,
                                                   String merchantId, String brandId, String searchTerm, String dealSource) {
        try {
            var dealFilterType = createFilterType(categoryId, merchantId, brandId, searchTerm);
            switch (dealFilterType) {
                case CATEGORY_MERCHANT_ALL_SEARCH:
                    return dealSearchIndexRepository.findAllByMerchantIdCategoryIdSearchTerm(pageable,
                            merchantId, categoryId, searchTerm, dealSource);
                case ALL_MERCHANT_ALL_SEARCH:
                    return dealSearchIndexRepository.findAllByMerchantIdAndSearchTerm(pageable, merchantId, searchTerm, dealSource);
                case CATEGORY_MERCHANT_ALL_ALL:
                    return dealSearchIndexRepository.findAllByMerchantIdAndCategoryId(pageable, merchantId, categoryId, dealSource);
                case CATEGORY_ALL_ALL_SEARCH:
                    return dealSearchIndexRepository.findAllByCategoryIdAndSearchTerm(pageable, categoryId, searchTerm, dealSource);
                case CATEGORY_ALL_ALL_ALL:
                    return dealSearchIndexRepository.findAllByCategoryId(pageable, categoryId, dealSource);
                case ALL_MERCHANT_ALL_ALL:
                    return dealSearchIndexRepository.findAllByMerchantId(pageable, merchantId, dealSource);
                case ALL_ALL_ALL_SEARCH:
                    return dealSearchIndexRepository.findAllApprovedDealsBySearchTerm(pageable, searchTerm, dealSource);
                case ALL_ALL_BRAND_ALL:
                    return dealSearchIndexRepository.findAllByBrandId(pageable, brandId, dealSource);
                case ALL_ALL_BRAND_SEARCH:
                    return dealSearchIndexRepository.findAllByBrandIdAndSearchTerm(pageable, brandId, searchTerm, dealSource);
                default:
                    return dealSearchIndexRepository.findAllSearchDeals(pageable, dealSource);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException(READ_FILTER_DEAL, e);
        }
    }

    /**
     * Get All Search Deals for active merchants
     *
     * @param pageable   pageable
     * @param categoryId category id
     * @param merchantId merchant id
     * @param brandId    brand id
     * @param searchTerm search term
     * @return DealSearchIndex page
     **/

    public Page<DealSearchIndex> getAllActiveDealsForActiveMerchant(Pageable pageable, String categoryId, String merchantId,
                                                                    String brandId, String searchTerm, UserType dealSource) {
        try {
            var currentMilliSecond = System.currentTimeMillis();
            var dealFilterType = createFilterType(categoryId, merchantId, brandId, searchTerm);

            switch (dealFilterType) {
                case CATEGORY_MERCHANT_ALL_SEARCH:
                    return dealSearchIndexRepository
                            .findAllActiveDealsByActiveMerchantIdCategoryIdSearchTerm(
                                    pageable, merchantId, categoryId, searchTerm, currentMilliSecond, dealSource);
                case ALL_MERCHANT_ALL_SEARCH:
                    return dealSearchIndexRepository
                            .findAllActiveDealsByActiveMerchantIdAndSearchTerm(
                                    pageable, merchantId, searchTerm, currentMilliSecond, dealSource);
                case CATEGORY_MERCHANT_ALL_ALL:
                    return dealSearchIndexRepository
                            .findAllActiveDealsByActiveMerchantIdAndCategoryId(
                                    pageable, merchantId, categoryId, currentMilliSecond, dealSource);
                case CATEGORY_ALL_ALL_SEARCH:
                    return dealSearchIndexRepository
                            .findAllActiveDealsCategoryIdAndSearchTermAndActiveMerchant(
                                    pageable, categoryId, searchTerm, currentMilliSecond, dealSource);
                case CATEGORY_ALL_ALL_ALL:
                    return dealSearchIndexRepository
                            .findAllActiveDealsByCategoryIdAndActiveMerchant(
                                    pageable, categoryId, currentMilliSecond, dealSource);
                case ALL_MERCHANT_ALL_ALL:
                    return dealSearchIndexRepository
                            .findAllActiveDealsByActiveMerchantId(pageable, merchantId, currentMilliSecond, dealSource);
                case ALL_ALL_ALL_SEARCH:
                    return dealSearchIndexRepository
                            .findAllApprovedActiveDealsBySearchTermAndActiveMerchant(
                                    pageable, searchTerm, currentMilliSecond, dealSource);
                default:
                    return dealSearchIndexRepository.findAllActiveDealsWithActiveMerchant(pageable, currentMilliSecond, dealSource);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException(READ_FILTER_DEAL, e);
        }
    }

    /**
     * Get all active deals for active merchant with time-based filtering.
     * This method considers startTime and endTime fields to filter deals.
     *
     * @param pageable   pageable
     * @param categoryId category id
     * @param merchantId merchant id
     * @param brandId    brand id
     * @param searchTerm search term
     * @param dealSource deal source
     * @return DealSearchIndex page
     */
    public Page<DealSearchIndex> getAllTimeBasedActiveDealsForActiveMerchant(Pageable pageable, String categoryId, String merchantId,
                                                                             String brandId, String searchTerm, UserType dealSource, String timeZone) {
        try {
            var currentMilliSecond = System.currentTimeMillis();
            var dealFilterType = createFilterType(categoryId, merchantId, brandId, searchTerm);

            Page<DealSearchIndex> dealsPage;
            switch (dealFilterType) {
                case CATEGORY_MERCHANT_ALL_SEARCH:
                    dealsPage = dealSearchIndexRepository
                            .findAllActiveDealsByActiveMerchantIdCategoryIdSearchTerm(
                                    pageable, merchantId, categoryId, searchTerm, currentMilliSecond, dealSource);
                    break;
                case ALL_MERCHANT_ALL_SEARCH:
                    dealsPage = dealSearchIndexRepository
                            .findAllActiveDealsByActiveMerchantIdAndSearchTerm(
                                    pageable, merchantId, searchTerm, currentMilliSecond, dealSource);
                    break;
                case CATEGORY_MERCHANT_ALL_ALL:
                    dealsPage = dealSearchIndexRepository
                            .findAllActiveDealsByActiveMerchantIdAndCategoryId(
                                    pageable, merchantId, categoryId, currentMilliSecond, dealSource);
                    break;
                case CATEGORY_ALL_ALL_SEARCH:
                    dealsPage = dealSearchIndexRepository
                            .findAllActiveDealsCategoryIdAndSearchTermAndActiveMerchant(
                                    pageable, categoryId, searchTerm, currentMilliSecond, dealSource);
                    break;
                case CATEGORY_ALL_ALL_ALL:
                    dealsPage = dealSearchIndexRepository
                            .findAllActiveDealsByCategoryIdAndActiveMerchant(
                                    pageable, categoryId, currentMilliSecond, dealSource);
                    break;
                case ALL_MERCHANT_ALL_ALL:
                    dealsPage = dealSearchIndexRepository
                            .findAllActiveDealsByActiveMerchantId(pageable, merchantId, currentMilliSecond, dealSource);
                    break;
                case ALL_ALL_ALL_SEARCH:
                    dealsPage = dealSearchIndexRepository
                            .findAllApprovedActiveDealsBySearchTermAndActiveMerchant(
                                    pageable, searchTerm, currentMilliSecond, dealSource);
                    break;
                default:
                    dealsPage = dealSearchIndexRepository.findAllActiveDealsWithActiveMerchant(pageable, currentMilliSecond, dealSource);
                    break;
            }

            return filterDealsByTime(dealsPage, timeZone);
        } catch (DataAccessException e) {
            throw new KupiCoreException(READ_FILTER_DEAL, e);
        }
    }

    /**
     * Get current time in h:mm a format (e.g., "8:30 AM") for time-based filtering.
     * Uses the provided timezone instead of server timezone.
     *
     * @param timeZone the timezone to use for current time calculation
     * @return current time in h:mm a format
     */
    private String getCurrentTimeInHHmmFormat(String timeZone) {
        var now = java.time.LocalTime.now(java.time.ZoneId.of(timeZone));
        return now.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
    }

    /**
     * Filter deals by time-based criteria in application layer.
     * This method filters deals based on startTime and endTime fields.
     *
     * @param dealsPage original page of deals
     * @param timeZone the timezone to use for time comparison
     * @return filtered page of deals
     */
    private Page<DealSearchIndex> filterDealsByTime(Page<DealSearchIndex> dealsPage, String timeZone) {
        var currentTime = getCurrentTimeInHHmmFormat(timeZone);
        var filteredDeals = dealsPage.getContent().stream()
                .filter(deal -> isDealActiveAtCurrentTime(deal, currentTime))
                .collect(java.util.stream.Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(
                filteredDeals,
                dealsPage.getPageable(),
                dealsPage.getTotalElements()
        );
    }

    /**
     * Check if a deal is active at the current time based on startTime and endTime.
     *
     * @param deal        the deal to check
     * @param currentTime current time in h:mm a format
     * @return true if deal is active at current time
     */
    private boolean isDealActiveAtCurrentTime(DealSearchIndex deal, String currentTime) {
        if (deal.getStartTime() == null || deal.getEndTime() == null) {
            return true;
        }

        try {
            var startTime = java.time.LocalTime.parse(deal.getStartTime(),
                    java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
            var endTime = java.time.LocalTime.parse(deal.getEndTime(),
                    java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
            var current = java.time.LocalTime.parse(currentTime,
                    java.time.format.DateTimeFormatter.ofPattern("h:mm a"));

            if (startTime.isAfter(endTime)) {
                return current.isAfter(startTime) || current.isBefore(endTime) ||
                        current.equals(startTime) || current.equals(endTime);
            } else {
                return !current.isBefore(startTime) && !current.isAfter(endTime);
            }
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Get All recently expire deals by category id, merchant id, brand id and search term.
     * Returns deals that expire within the configured number of days ahead.
     *
     * @param pageable   pageable
     * @param categoryId category id
     * @param merchantId merchant id
     * @param brandId    brand id
     * @param searchTerm search term
     * @return DealSearchIndex Page
     */
    public Page<DealSearchIndex> getAllSearchRecentlyExpireDeals(Pageable pageable, String categoryId,
                                                                 String merchantId, String brandId,
                                                                 String searchTerm, UserType userType) {
        try {
            var dealFilterType = createFilterType(categoryId, merchantId, brandId, searchTerm);
            var currentMills = System.currentTimeMillis();
            // Calculate expiry threshold: current time + configured days ahead (in milliseconds)
            var expiryThreshold = currentMills + (recentlyExpireDaysAhead * 24L * 60L * 60L * 1000L);

            switch (dealFilterType) {
                case CATEGORY_MERCHANT_ALL_SEARCH:
                    return dealSearchIndexRepository
                            .findAllValidDealsByMerchantIdCategoryIdSearchTerm(
                                    pageable, merchantId, categoryId, searchTerm, currentMills, expiryThreshold, userType);
                case ALL_MERCHANT_ALL_SEARCH:
                    return dealSearchIndexRepository
                            .findAllValidDealsByMerchantIdAndSearchTerm(
                                    pageable, merchantId, searchTerm, currentMills, expiryThreshold, userType);
                case CATEGORY_MERCHANT_ALL_ALL:
                    return dealSearchIndexRepository
                            .findAllValidDealsByMerchantIdAndCategoryId(
                                    pageable, merchantId, categoryId, currentMills, expiryThreshold, userType);
                case CATEGORY_ALL_ALL_SEARCH:
                    return dealSearchIndexRepository
                            .findAllValidDealsByCategoryIdAndSearchTerm(
                                    pageable, categoryId, searchTerm, currentMills, expiryThreshold, userType);
                case CATEGORY_ALL_ALL_ALL:
                    return dealSearchIndexRepository
                            .findAllValidDealsByCategoryId(pageable, categoryId, currentMills, expiryThreshold, userType);
                case ALL_MERCHANT_ALL_ALL:
                    return dealSearchIndexRepository
                            .findAllValidDealsByMerchantId(pageable, merchantId, currentMills, expiryThreshold, userType);
                case ALL_ALL_ALL_SEARCH:
                    return dealSearchIndexRepository
                            .findAllValidDealsBySearchTerm(pageable, searchTerm, currentMills, expiryThreshold, userType);
                default:
                    return dealSearchIndexRepository.findAllValidDealsWithExpiryThreshold(pageable, currentMills, expiryThreshold, userType);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException(READ_FILTER_DEAL, e);
        }
    }

    /**
     * Get All search recently expire deals with time-based filtering by category id, merchant id, brand id and search term.
     * Returns deals that expire within the configured number of days ahead.
     *
     * @param pageable   pageable
     * @param categoryId category id
     * @param merchantId merchant id
     * @param brandId    brand id
     * @param searchTerm search term
     * @return DealSearchIndex Page with time-based filtering
     */
    public Page<DealSearchIndex> getAllTimeBasedSearchRecentlyExpireDeals(Pageable pageable, String categoryId,
                                                                          String merchantId, String brandId,
                                                                          String searchTerm, UserType userType, String timeZone) {
        try {
            var dealFilterType = createFilterType(categoryId, merchantId, brandId, searchTerm);
            var currentMills = System.currentTimeMillis();
            // Calculate expiry threshold: current time + configured days ahead (in milliseconds)
            var expiryThreshold = currentMills + (recentlyExpireDaysAhead * 24L * 60L * 60L * 1000L);

            Page<DealSearchIndex> dealsPage;
            switch (dealFilterType) {
                case CATEGORY_MERCHANT_ALL_SEARCH:
                    dealsPage = dealSearchIndexRepository
                            .findAllValidDealsByMerchantIdCategoryIdSearchTerm(
                                    pageable, merchantId, categoryId, searchTerm, currentMills, expiryThreshold, userType);
                    break;
                case ALL_MERCHANT_ALL_SEARCH:
                    dealsPage = dealSearchIndexRepository
                            .findAllValidDealsByMerchantIdAndSearchTerm(
                                    pageable, merchantId, searchTerm, currentMills, expiryThreshold, userType);
                    break;
                case CATEGORY_MERCHANT_ALL_ALL:
                    dealsPage = dealSearchIndexRepository
                            .findAllValidDealsByMerchantIdAndCategoryId(
                                    pageable, merchantId, categoryId, currentMills, expiryThreshold, userType);
                    break;
                case CATEGORY_ALL_ALL_SEARCH:
                    dealsPage = dealSearchIndexRepository
                            .findAllValidDealsByCategoryIdAndSearchTerm(
                                    pageable, categoryId, searchTerm, currentMills, expiryThreshold, userType);
                    break;
                case CATEGORY_ALL_ALL_ALL:
                    dealsPage = dealSearchIndexRepository
                            .findAllValidDealsByCategoryId(pageable, categoryId, currentMills, expiryThreshold, userType);
                    break;
                case ALL_MERCHANT_ALL_ALL:
                    dealsPage = dealSearchIndexRepository
                            .findAllValidDealsByMerchantId(pageable, merchantId, currentMills, expiryThreshold, userType);
                    break;
                case ALL_ALL_ALL_SEARCH:
                    dealsPage = dealSearchIndexRepository
                            .findAllValidDealsBySearchTerm(pageable, searchTerm, currentMills, expiryThreshold, userType);
                    break;
                default:
                    dealsPage = dealSearchIndexRepository.findAllValidDealsWithExpiryThreshold(pageable, currentMills, expiryThreshold, userType);
                    break;
            }

            return filterDealsByTime(dealsPage, timeZone);
        } catch (DataAccessException e) {
            throw new KupiCoreException(READ_FILTER_DEAL, e);
        }
    }

    /**
     * This method returns a count of deals for specific category id
     *
     * @param categoryId category id
     * @return count deals Count By CategoryId
     */
    public long getDealsCountByCategoryId(String categoryId) {
        try {
            return dealSearchIndexRepository.countByRelatedCategoriesContainsAndIsDeletedFalse(categoryId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading deal count from the database by category id was failed", e);
        }
    }

    /**
     * This method returns active and valid deal count for a specific category id.
     *
     * @param categoryId category id
     * @return active & valid deal count
     */
    public long getActiveDealsCountByCategoryId(String categoryId) {
        try {
            long currentMillis = System.currentTimeMillis();
            return dealSearchIndexRepository.getActiveValidDealsCountByCategoryId(currentMillis, categoryId);
        } catch (DataAccessException e) {
            throw new KupiCoreException(READ_FILTER_DEAL, e);
        }
    }

    /**
     * This method returns count of deal for a specific brand.
     *
     * @param brandId brand Id
     * @return count deals Count By BrandId
     */
    public long getDealsCountByBrandId(String brandId) {
        try {
            return dealSearchIndexRepository.countByRelatedBrandsContainsAndIsDeletedFalse(brandId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading deal count from the database by Brand id was failed", e);
        }
    }

    /**
     * This method returns active and valid deal count for a specific brand id.
     *
     * @param brandId brand id
     * @return active & valid deal count
     */
    public long getActiveDealsCountByBrandId(String brandId) {
        try {
            long currentMillis = System.currentTimeMillis();
            return dealSearchIndexRepository.getActiveValidDealsCountByBrandId(currentMillis, brandId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading dealSearchIndex by brand id from the database was failed.", e);
        }
    }

    /**
     * This method returns active and valid deal count for all categories.
     * Returns a map of categoryId to active deal count.
     *
     * @param categoryIds list of category ids
     * @return map of categoryId to active deal count
     */
    public Map<String, Long> getActiveDealsCountByCategoryIds(List<String> categoryIds) {
        try {
            long currentMillis = System.currentTimeMillis();
            Map<String, Long> categoryDealCountMap = new HashMap<>();
            for (String categoryId : categoryIds) {
                long count = dealSearchIndexRepository.getActiveValidDealsCountByCategoryId(currentMillis, categoryId);
                categoryDealCountMap.put(categoryId, count);
            }
            return categoryDealCountMap;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading deal counts by category ids from the database was failed.", e);
        }
    }

    /**
     * This method combine categoryId, merchantId and searchTerm and returns enum
     *
     * @param categoryId categoryId / ALL
     * @param merchantId merchantId / ALL
     * @param searchTerm searchTerm / ALL
     * @return DealFilterType String eg: ALL_MERCHANT_ALL_ALL
     */
    private DealFilterType createFilterType(String categoryId, String merchantId, String brandId, String searchTerm) {
        String part1 = categoryId.equals(ALL) ? ALL : CATEGORY;
        String part2 = merchantId.equals(ALL) ? ALL : MERCHANT;
        String part3 = brandId.equals(ALL) ? ALL : BRAND;
        String part4 = searchTerm.equals(ALL) ? ALL : SEARCH;
        var value = part1 + BIND + part2 + BIND + part3 + BIND + part4;

        return DealFilterType.valueOf(value);
    }

    /**
     * This method will update DealSearch Index by deal id.
     *
     * @param deal                        deal
     * @param businessMerchantResponseDto businessMerchantResponseDto
     */
    public void updateDealSearchIndex(Deal deal, BusinessMerchantResponseDto businessMerchantResponseDto) {
        try {
            var dealSearchIndex = findById(deal.getId());
            dealSearchIndex.update(deal, businessMerchantResponseDto);
            save(dealSearchIndex);
        } catch (DataAccessException e) {
            throw new KupiCoreException("updating deals search index to database was failed", e);
        }
    }

    /**
     * This method will update merchant info.
     *
     * @param merchantMap merchant id, MerchantBusinessResponseDto Map
     */
    public void updateMerchantInfo(Map<String, MerchantBusinessResponseDto> merchantMap) {
        try {
            List<DealSearchIndex> dealSearchIndexList
                    = dealSearchIndexRepository.getAllByMerchantIdIn(new ArrayList<>(merchantMap.keySet()));
            for (var dealSearchIndex : dealSearchIndexList) {
                dealSearchIndex.setMerchantName(merchantMap.get(dealSearchIndex.getMerchantId()).getName());
                dealSearchIndex.setMerchantImageUrl(merchantMap.get(dealSearchIndex.getMerchantId()).getImageUrl());
                dealSearchIndex.setApprovalStatus(merchantMap.get(dealSearchIndex.getMerchantId()).getApprovalStatus());
                dealSearchIndex.setActiveMerchant(merchantMap.get(dealSearchIndex.getMerchantId()).isActive());
            }
            dealSearchIndexRepository.saveAll(dealSearchIndexList);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Updating merchant info for dealSearchIndex was failed", e);
        }
    }

    /**
     * This method returns active deal ids for active bank.
     *
     * @param bankId bankId
     * @return list of deal ids.
     */
    public List<String> getActiveDealsForActiveBank(String bankId) {
        return dealSearchIndexRepository.getActiveDealsByActiveBankId(bankId, System.currentTimeMillis());
    }

    /**
     * This method saves list of DealSearchIndices.
     *
     * @param dealSearchIndexList DealSearchIndex List
     */
    public void saveListOfDealSearchIndexes(List<DealSearchIndex> dealSearchIndexList) {
        try {
            dealSearchIndexRepository.saveAll(dealSearchIndexList);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving dealSearchIndex to the database was failed.", e);
        }
    }

    /**
     * This method returns DealSearchIndex page by category id.
     *
     * @param pageable   pageable
     * @param categoryId category id
     * @return DealSearchIndex page
     */
    public Page<DealSearchIndex> getDealSearchIndexByCategoryId(Pageable pageable, String categoryId) {
        try {
            return dealSearchIndexRepository.getAllByRelatedCategoriesContaining(pageable, categoryId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading dealSearchIndex by category id: "
                    + categoryId + " from the database was failed.", e);
        }
    }

    /**
     * This method returns DealSearchIndex page by brand id.
     *
     * @param pageable pageable
     * @param brandId  brand id
     * @return DealSearchIndex page
     */
    public Page<DealSearchIndex> getDealSearchIndexByBrandId(Pageable pageable, String brandId) {
        try {
            return dealSearchIndexRepository.getAllByRelatedBrandsContaining(pageable, brandId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading dealSearchIndex by brand id: "
                    + brandId + " from the database was failed.", e);
        }
    }

    /**
     * This method get deals list by deal id.
     *
     * @param dealViewsList dealViewsList
     * @return list of deals with view count & display date.
     */
    public List<DealViewCountResponse> getDealSetByIds(List<ViewCountAnalyticResponseDto> dealViewsList) {
        try {
            List<DealViewCountResponse> dealsAndViews = new ArrayList<>();
            for (ViewCountAnalyticResponseDto dealIdAndViews : dealViewsList) {
                Optional<DealSearchIndex> optionalDealFromDb =
                        dealSearchIndexRepository.findById(dealIdAndViews.getDealId());
                optionalDealFromDb.ifPresent(deal -> dealsAndViews.add(
                        new DealViewCountResponse(deal, deal.getMerchantId(), dealIdAndViews.getViewCount(),
                                dealIdAndViews.getDisplayDate(), deal.getDealSource(), dealIdAndViews.getTotalRows())));
            }
            return dealsAndViews;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading deal by id from database was failed.", e);
        }
    }

    /**
     * This method used to unset isDealOfTheDay flag to false on dealSearchIndex.
     */
    public void disableIsDealOfTheDayFromDealSearch() {
        try {
            dealSearchIndexRepository.disableDealOfTheDayFlags();
        } catch (DataAccessException e) {
            throw new KupiCoreException("Disabling deal of the day flag from deals search index was failed.");
        }
    }

    /**
     * This method used to set isDealOfTheDay flag true on dealSearchIndex.
     *
     * @param dealSearchIndexList dealSearchIndexList
     */
    public void setDealsOfTheDayFlagForDealSearch(List<DealSearchIndex> dealSearchIndexList) {
        try {
            List<DealSearchIndex> dealSearchIndexes = new ArrayList<>();
            for (var dealSearchIndex : dealSearchIndexList) {
                dealSearchIndex.setDealOfTheDay(true);
                dealSearchIndexes.add(dealSearchIndex);
            }
            dealSearchIndexRepository.saveAll(dealSearchIndexes);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Updating deals of the day on bulk dealSearchIndexes was failed.");
        }
    }

    /**
     * This method used to get deal list by list of dealsIds.
     *
     * @param dealIdList dealList
     * @return DealSearchIndex List
     */
    public List<DealSearchIndex> getDealListByDealIdList(List<String> dealIdList) {
        try {
            return dealSearchIndexRepository.findByIdIn(dealIdList);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Returning deal search indexes by deal id(s) was failed.");
        }
    }
}
