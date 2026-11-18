package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.CouponStatistics;
import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.domain.entity.FavouriteDeal;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.response.DealListResponseDto;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.InvalidDealException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.DealRepository;
import com.kupi.cc.base.repository.FavouriteDealRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Favourite Deal Service
 */
@Slf4j
@Service
public class FavouriteDealService {

    private static final int BULK_MERCHANT_LIMIT = 25;

    private final FavouriteDealRepository favouriteDealRepository;
    private final DealRepository dealRepository;
    private final DealSearchService dealSearchService;
    private final CouponStatisticsService couponStatisticsService;
    private final DealService dealService;
    private final AuthUserService authUserService;

    @Autowired
    public FavouriteDealService(FavouriteDealRepository favouriteDealRepository,
                                DealRepository dealRepository,
                                DealSearchService dealSearchService,
                                CouponStatisticsService couponStatisticsService,
                                DealService dealService,
                                AuthUserService authUserService) {
        this.favouriteDealRepository = favouriteDealRepository;
        this.dealRepository = dealRepository;
        this.dealSearchService = dealSearchService;
        this.couponStatisticsService = couponStatisticsService;
        this.dealService = dealService;
        this.authUserService = authUserService;
    }

    /**
     * Mark a deal as favourite for a user.
     * If the favourite already exists, this is treated as a no-op.
     *
     * NOTE: Existing logic preserved.
     *
     * @param userId user id
     * @param dealId deal id
     */
    public void addFavouriteDeal(String userId, String dealId) {
        try {
            // Validate deal existence (and that it is not deleted)
            Deal deal = dealRepository.findDealById(dealId)
                    .orElseThrow(() -> new InvalidDealException("Deal not found with id: " + dealId));

            // If already favourite, do nothing (idempotent)
            if (favouriteDealRepository.existsByUserIdAndDealId(userId, deal.getId())) {
                return;
            }

            FavouriteDeal favouriteDeal = new FavouriteDeal(userId, deal.getId());
            favouriteDealRepository.save(favouriteDeal);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving favourite deal to database failed for dealId: " + dealId, e);
        }
    }

    /**
     * Remove a favourite deal for a user.
     * If the favourite does not exist, this is treated as a no-op.
     *
     * NOTE: Existing logic preserved.
     *
     * @param userId user id
     * @param dealId deal id
     */
    @Transactional
    public void removeFavouriteDeal(String userId, String dealId) {
        try {
            if (!favouriteDealRepository.existsByUserIdAndDealId(userId, dealId)) {
                return;
            }
            favouriteDealRepository.deleteByUserIdAndDealId(userId, dealId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Removing favourite deal from database failed for dealId: " + dealId, e);
        }
    }

    /**
     * Get favourite deals for a user filtered by a given list of deal ids.
     * This is a thin wrapper around the repository to keep favourite-related
     * access logic inside the service layer.
     *
     * @param userId   user id
     * @param dealIds  deal ids to filter by
     * @return list of FavouriteDeal
     */
    public List<FavouriteDeal> getFavouriteDealsForUserAndDealIds(String userId, List<String> dealIds) {
        try {
            return favouriteDealRepository.findByUserIdAndDealIdIn(userId, dealIds);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading favourite deals for userId: " + userId + " failed.", e);
        }
    }

    /**
     * Get paginated active favourite deals for a user.
     * <p>
     * Active deals are:
     * - not deleted
     * - APPROVED
     * - merchant is active (approved and not blocked via auth service)
     * - current time is between validFrom and expiredOn
     *
     * @param userId   user id
     * @param timeZone time zone
     * @param page     page number (0-based)
     * @param size     page size
     * @return DealListResponseDto containing active favourite deals with pagination metadata
     */
    public DealListResponseDto getActiveFavouriteDealsForUser(String userId, String timeZone, int page, int size) {
        try {
            List<FavouriteDeal> favouriteDeals = favouriteDealRepository.findByUserId(userId);
            if (favouriteDeals == null || favouriteDeals.isEmpty()) {
                log.debug("No favourite deals found for userId: {}", userId);
                return buildEmptyDealListResponse(timeZone);
            }

            List<String> dealIdList = favouriteDeals.stream()
                    .map(FavouriteDeal::getDealId)
                    .collect(Collectors.toList());

            List<DealSearchIndex> dealSearchIndexList = dealSearchService.getDealListByDealIdList(dealIdList);
            long currentMillis = System.currentTimeMillis();

            List<DealSearchIndex> activeDeals = dealSearchIndexList.stream()
                    .filter(deal -> !deal.isDeleted())
                    .filter(deal -> deal.getApprovalStatus() == ApprovalStatus.APPROVED)
                    .filter(deal -> deal.getValidFrom() <= currentMillis && deal.getExpiredOn() > currentMillis)
                    .collect(Collectors.toList());

            if (activeDeals.isEmpty()) {
                log.debug("No active favourite deals found for userId: {}", userId);
                return buildEmptyDealListResponse(timeZone);
            }

            List<String> merchantIds = activeDeals.stream()
                    .map(DealSearchIndex::getMerchantId)
                    .distinct()
                    .collect(Collectors.toList());
            Set<String> eligibleMerchantIds = getEligibleMerchantIdsFromAuth(merchantIds);
            activeDeals = activeDeals.stream()
                    .filter(deal -> eligibleMerchantIds.contains(deal.getMerchantId()))
                    .collect(Collectors.toList());

            if (activeDeals.isEmpty()) {
                log.debug("No favourite deals from approved/active merchants for userId: {}", userId);
                return buildEmptyDealListResponse(timeZone);
            }

            activeDeals.sort(java.util.Comparator.comparingLong(DealSearchIndex::getCreatedAt).reversed());

            // Apply pagination
            int totalElements = activeDeals.size();
            int fromIndex = page * size;
            if (fromIndex >= totalElements) {
                Page<DealSearchIndex> emptyPage = new PageImpl<>(
                        Collections.emptyList(), PageRequest.of(page, size), totalElements);
                return new DealListResponseDto(emptyPage, timeZone,
                        Collections.emptyList(), Collections.emptySet());
            }
            int toIndex = Math.min(fromIndex + size, totalElements);
            List<DealSearchIndex> pageContent = activeDeals.subList(fromIndex, toIndex);

            Page<DealSearchIndex> dealPage = new PageImpl<>(pageContent, PageRequest.of(page, size), totalElements);

            List<String> pageDealIds = pageContent.stream().map(DealSearchIndex::getId).collect(Collectors.toList());
            List<CouponStatistics> couponStatisticsList =
                    couponStatisticsService.getCouponStatisticsByDealIdList(pageDealIds);

            Page<DealSearchIndex> filteredDealPage =
                    dealService.removeCouponNotAvailableDeals(dealPage, couponStatisticsList);
            Set<String> favouriteDealIds = new HashSet<>(pageDealIds);

            return new DealListResponseDto(filteredDealPage, timeZone, couponStatisticsList, favouriteDealIds);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to get active favourite deals for userId: " + userId, e);
        }
    }

    /**
     * Get merchant IDs that are approved and active (not blocked) by calling auth service.
     * Same logic as location service getEligibleMerchantIds for nearby deals.
     * Batches requests by BULK_MERCHANT_LIMIT (25) to respect auth bulk endpoint limits.
     */
    private Set<String> getEligibleMerchantIdsFromAuth(List<String> merchantIds) {
        if (merchantIds == null || merchantIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> eligible = new HashSet<>();
        for (int i = 0; i < merchantIds.size(); i += BULK_MERCHANT_LIMIT) {
            int end = Math.min(i + BULK_MERCHANT_LIMIT, merchantIds.size());
            List<String> batch = merchantIds.subList(i, end);
            BulkUserRequestDto bulkRequest = new BulkUserRequestDto(batch);
            try {
                var merchantMap = authUserService.getMerchantMap(bulkRequest, UserType.MERCHANT);
                for (MerchantBusinessResponseDto m : merchantMap.values()) {
                    if (m != null && m.getApprovalStatus() == ApprovalStatus.APPROVED && m.isActive()) {
                        eligible.add(m.getId());
                    }
                }
            } catch (KupiCoreException e) {
                log.warn("Auth service call failed for merchant batch, skipping batch: {}", e.getMessage());
            }
        }
        return eligible;
    }

    private DealListResponseDto buildEmptyDealListResponse(String timeZone) {
        org.springframework.data.domain.Page<DealSearchIndex> emptyPage =
                new org.springframework.data.domain.PageImpl<>(Collections.emptyList());
        return new DealListResponseDto(emptyPage, timeZone,
                Collections.emptyList(), Collections.emptySet());
    }
}

