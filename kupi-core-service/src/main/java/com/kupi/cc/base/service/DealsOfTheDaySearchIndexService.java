package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.DealOfTheDaySearchIndex;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.enums.BasicDealFilterType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.DealsOfTheDaySearchIndexRepository;
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
public class DealsOfTheDaySearchIndexService {

    private static final String ALL = "ALL";
    private static final String MERCHANT = "MERCHANT";
    private static final String CATEGORY = "CATEGORY";
    private static final String SEARCH = "SEARCH";
    private static final String BIND = "_";
    private final DealsOfTheDaySearchIndexRepository dealsOfTheDaySearchIndexRepository;

    @Autowired
    public DealsOfTheDaySearchIndexService(DealsOfTheDaySearchIndexRepository dealsOfTheDaySearchIndexRepository) {
        this.dealsOfTheDaySearchIndexRepository = dealsOfTheDaySearchIndexRepository;
    }

    /**
     * Delete all deals of the day search index
     */
    public void deleteAll() {
        try {
            dealsOfTheDaySearchIndexRepository.deleteAll();
        } catch (DataAccessException e) {
            throw new KupiCoreException("Deleting all deals of the day search index from database was failed", e);
        }
    }

    /**
     * Save all deals of the day search indexes
     *
     * @param dealOfTheDaySearchIndexList dealOfTheDaySearchIndexList
     */
    public void saveAll(List<DealOfTheDaySearchIndex> dealOfTheDaySearchIndexList) {
        try {
            dealsOfTheDaySearchIndexRepository.saveAll(dealOfTheDaySearchIndexList);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Save all deals of the day search index to database was failed", e);
        }
    }


    /**
     * Get all searchDeals for Merchant/ Bank
     *
     * @param pageable   pageable
     * @param categoryId categoryId / ALL
     * @param merchantId merchantId / ALL
     * @param searchTerm searchTerm / ALL
     * @param dealSource dealSource
     * @return DealSearchIndex Page
     */
    public Page<DealOfTheDaySearchIndex> getAllSearchDeals(Pageable pageable, String categoryId, String merchantId,
                                                           String searchTerm, UserType dealSource) {
        try {
            // Use current date to filter deals of the day
            java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
            var dealFilterType = createFilterType(categoryId, merchantId, searchTerm);
            switch (dealFilterType) {
                case CATEGORY_MERCHANT_SEARCH:
                    return dealsOfTheDaySearchIndexRepository
                            .findAllByMerchantIdCategoryIdSearchTermAndDealSource(pageable,
                                    merchantId, categoryId, searchTerm, dealSource, currentDate);
                case ALL_MERCHANT_SEARCH:
                    return dealsOfTheDaySearchIndexRepository
                            .findAllByMerchantIdAndSearchTermAndDealSource(pageable, merchantId, searchTerm, dealSource, currentDate);
                case CATEGORY_MERCHANT_ALL:
                    return dealsOfTheDaySearchIndexRepository
                            .findAllByMerchantIdAndCategoryIdAndDealSource(pageable, merchantId, categoryId, dealSource, currentDate);
                case CATEGORY_ALL_SEARCH:
                    return dealsOfTheDaySearchIndexRepository
                            .findAllByCategoryIdAndSearchTermAndDealSource(pageable, categoryId, searchTerm, dealSource, currentDate);
                case CATEGORY_ALL_ALL:
                    return dealsOfTheDaySearchIndexRepository.findAllByCategoryIdAndDealSource(pageable, categoryId, dealSource, currentDate);
                case ALL_MERCHANT_ALL:
                    return dealsOfTheDaySearchIndexRepository.findAllByMerchantIdAndDealSource(pageable, merchantId, dealSource, currentDate);
                case ALL_ALL_SEARCH:
                    return dealsOfTheDaySearchIndexRepository.findAllApprovedDealsBySearchTermAndDealSource(pageable,
                            searchTerm, dealSource, currentDate);
                default:
                    return dealsOfTheDaySearchIndexRepository.findAllSearchDealsByDealSource(pageable, dealSource, currentDate);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading dealSearchIndex from the database was failed.", e);
        }
    }

    /**
     * Get All search deals with time-based filtering by category id, merchant id, brand id and search term.
     *
     * @param pageable   pageable
     * @param categoryId categoryId / ALL
     * @param merchantId merchantId / ALL
     * @param searchTerm searchTerm / ALL
     * @return DealOfTheDaySearchIndex Page with time-based filtering
     */
    public Page<DealOfTheDaySearchIndex> getAllTimeBasedSearchDeals(Pageable pageable, String categoryId, String merchantId,
                                                                    String searchTerm, UserType dealSource, String timeZone) {
        try {
            // Use current date to filter deals of the day
            java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
            var dealFilterType = createFilterType(categoryId, merchantId, searchTerm);

            Page<DealOfTheDaySearchIndex> dealsPage;
            switch (dealFilterType) {
                case CATEGORY_MERCHANT_SEARCH:
                    dealsPage = dealsOfTheDaySearchIndexRepository
                            .findAllByMerchantIdCategoryIdSearchTermAndDealSource(pageable,
                                    merchantId, categoryId, searchTerm, dealSource, currentDate);
                    break;
                case ALL_MERCHANT_SEARCH:
                    dealsPage = dealsOfTheDaySearchIndexRepository
                            .findAllByMerchantIdAndSearchTermAndDealSource(pageable, merchantId, searchTerm, dealSource, currentDate);
                    break;
                case CATEGORY_MERCHANT_ALL:
                    dealsPage = dealsOfTheDaySearchIndexRepository
                            .findAllByMerchantIdAndCategoryIdAndDealSource(pageable, merchantId, categoryId, dealSource, currentDate);
                    break;
                case CATEGORY_ALL_SEARCH:
                    dealsPage = dealsOfTheDaySearchIndexRepository
                            .findAllByCategoryIdAndSearchTermAndDealSource(pageable, categoryId, searchTerm, dealSource, currentDate);
                    break;
                case CATEGORY_ALL_ALL:
                    dealsPage = dealsOfTheDaySearchIndexRepository.findAllByCategoryIdAndDealSource(pageable, categoryId, dealSource, currentDate);
                    break;
                case ALL_MERCHANT_ALL:
                    dealsPage = dealsOfTheDaySearchIndexRepository.findAllByMerchantIdAndDealSource(pageable, merchantId, dealSource, currentDate);
                    break;
                case ALL_ALL_SEARCH:
                    dealsPage = dealsOfTheDaySearchIndexRepository.findAllApprovedDealsBySearchTermAndDealSource(pageable,
                            searchTerm, dealSource, currentDate);
                    break;
                default:
                    dealsPage = dealsOfTheDaySearchIndexRepository.findAllSearchDealsByDealSource(pageable, dealSource, currentDate);
                    break;
            }

            // Apply time-based filtering in application layer
            return filterDealsOfTheDayByTime(dealsPage, timeZone);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading dealSearchIndex from the database was failed.", e);
        }
    }

    /**
     * This method combine categoryId, merchantId and searchTerm and returns enum
     *
     * @param categoryId categoryId / ALL
     * @param merchantId merchantId / ALL
     * @param searchTerm searchTerm / ALL
     * @return DealFilterType
     */
    private BasicDealFilterType createFilterType(String categoryId, String merchantId, String searchTerm) {
        String part1 = categoryId.equals(ALL) ? ALL : CATEGORY;
        String part2 = merchantId.equals(ALL) ? ALL : MERCHANT;
        String part3 = searchTerm.equals(ALL) ? ALL : SEARCH;

        var value = part1 + BIND + part2 + BIND + part3;

        return BasicDealFilterType.valueOf(value);
    }

    /**
     * Filter deals of the day by time-based criteria in application layer.
     * This method filters deals based on startTime and endTime fields.
     *
     * @param dealsPage original page of deals
     * @param timeZone the timezone to use for time comparison
     * @return filtered page of deals
     */
    private Page<DealOfTheDaySearchIndex> filterDealsOfTheDayByTime(Page<DealOfTheDaySearchIndex> dealsPage, String timeZone) {
        var currentTime = getCurrentTimeInHHmmFormat(timeZone);
        var filteredDeals = dealsPage.getContent().stream()
                .filter(deal -> isDealOfTheDayActiveAtCurrentTime(deal, currentTime))
                .collect(java.util.stream.Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(
                filteredDeals,
                dealsPage.getPageable(),
                dealsPage.getTotalElements()
        );
    }

    /**
     * Check if a deal of the day is active at the current time based on startTime and endTime.
     *
     * @param deal        the deal to check
     * @param currentTime current time in h:mm a format
     * @return true if deal is active at current time
     */
    private boolean isDealOfTheDayActiveAtCurrentTime(DealOfTheDaySearchIndex deal, String currentTime) {
        // If no time restrictions, deal is always active
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

            // Handle cross-midnight scenarios (e.g., 11 PM to 2 AM)
            if (startTime.isAfter(endTime)) {
                // Cross-midnight: current time should be >= startTime OR <= endTime
                return current.isAfter(startTime) || current.isBefore(endTime) ||
                        current.equals(startTime) || current.equals(endTime);
            } else {
                // Normal case: current time should be between startTime and endTime
                return !current.isBefore(startTime) && !current.isAfter(endTime);
            }
        } catch (Exception e) {
            // If parsing fails, consider deal as active
            return true;
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
     * This method will update merchant info.
     *
     * @param merchantMap merchant id, MerchantBusinessResponseDto Map
     */
    public void updateMerchantInfo(Map<String, MerchantBusinessResponseDto> merchantMap) {
        try {
            var dealOfTheDaySearchIndexList =
                    dealsOfTheDaySearchIndexRepository.getAllByMerchantIdIn(new ArrayList<>(merchantMap.keySet()));
            for (var dealOfTheDaySearchIndex : dealOfTheDaySearchIndexList) {
                dealOfTheDaySearchIndex.setMerchantName(merchantMap.get(dealOfTheDaySearchIndex.getMerchantId()).getName());
                dealOfTheDaySearchIndex.setMerchantImageUrl(merchantMap.get(dealOfTheDaySearchIndex.getMerchantId()).getImageUrl());
                dealOfTheDaySearchIndex.setActiveMerchant(merchantMap.get(dealOfTheDaySearchIndex.getMerchantId()).isActive());
            }
            dealsOfTheDaySearchIndexRepository.saveAll(dealOfTheDaySearchIndexList);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Updating merchant info for dealOfTheDaySearchIndex was failed", e);
        }
    }

    /**
     * This method used to get dealOfTheDaySearchIndex list by category id.
     *
     * @param categoryId category id
     * @return DealOfTheDaySearchIndex list
     */
    public List<DealOfTheDaySearchIndex> getDealsOfTheDaySearchIndexByCategoryId(String categoryId) {
        try {
            return dealsOfTheDaySearchIndexRepository.getAllByRelatedCategoriesContaining(categoryId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading dealOfTheDaySearchIndex for category id: " + categoryId + "was failed", e);
        }
    }

    /**
     * This method used to get dealOfTheDaySearchIndex list by brand id.
     *
     * @param brandId brand id
     * @return DealOfTheDaySearchIndex list
     */
    public List<DealOfTheDaySearchIndex> getDealsOfTheDaySearchIndexByBrandId(String brandId) {
        try {
            return dealsOfTheDaySearchIndexRepository.getAllByRelatedBrandsContaining(brandId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading dealOfTheDaySearchIndex for brand id: " + brandId + "was failed", e);
        }
    }
}
