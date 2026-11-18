package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.*;
import com.kupi.cc.base.domain.request.DealApprovalStatusUpdateRequestDto;
import com.kupi.cc.base.domain.request.DealRequestDto;
import com.kupi.cc.base.domain.request.DealUpdateRequestDto;
import com.kupi.cc.base.domain.response.BasicMerchantBusinessResponseDto;
import com.kupi.cc.base.domain.response.BusinessMerchantResponseDto;
import com.kupi.cc.base.enums.ActiveStatusType;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.ExpiredDealApprovalException;
import com.kupi.cc.base.exception.InvalidDealException;
import com.kupi.cc.base.exception.InvalidUserException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.exception.CannotRepublishDealException;
import com.kupi.cc.base.repository.BankDealRepository;
import com.kupi.cc.base.repository.CouponRepository;
import com.kupi.cc.base.repository.DealRepository;
import com.kupi.cc.base.repository.DealSearchIndexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class DealService {

    private static final String INVALID_DEAL = "Invalid deal Id: ";
    private static final String READ_DEAL = "Reading deal from the database was failed.";
    private static final String ALL = "ALL";
    private static final int ONE = 1;

    private final DealRepository dealRepository;
    private final BankDealRepository bankDealRepository;
    private final DealSearchService dealSearchService;
    private final DealApprovalService dealApprovalService;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final MerchantBankSearchIndexService merchantBankSearchIndexService;
    private final DealSearchIndexRepository dealSearchIndexRepository;
    private final SubscriberService subscriberService;
    private final PromoTypeService promoTypeService;
    private final CouponRepository couponRepository;
    private final TotalSavingsScheduler totalSavingsScheduler;

    @Autowired
    public DealService(DealRepository dealRepository, BankDealRepository bankDealRepository,
                       DealSearchService dealSearchService, DealApprovalService dealApprovalService,
                       BrandService brandService, CategoryService categoryService,
                       MerchantBankSearchIndexService merchantBankSearchIndexService, DealCodeService dealCodeService,
                       DealSearchIndexRepository dealSearchIndexRepository, SubscriberService subscriberService,
                       PromoTypeService promoTypeService, CouponRepository couponRepository,
                       TotalSavingsScheduler totalSavingsScheduler) {
        this.dealRepository = dealRepository;
        this.bankDealRepository = bankDealRepository;
        this.dealSearchService = dealSearchService;
        this.dealApprovalService = dealApprovalService;
        this.brandService = brandService;
        this.categoryService = categoryService;
        this.merchantBankSearchIndexService = merchantBankSearchIndexService;
        this.dealSearchIndexRepository = dealSearchIndexRepository;
        this.subscriberService = subscriberService;
        this.promoTypeService = promoTypeService;
        this.couponRepository = couponRepository;
        this.totalSavingsScheduler = totalSavingsScheduler;
    }

    /**
     * Create deal
     *
     * @param deal deal
     */
    public void createDeal(Deal deal, BasicMerchantBusinessResponseDto basicMerchantBusinessResponseDto,
                           UserType userType, DealRequestDto dealRequestDto, BusinessMerchantResponseDto merchant) {
        try {
            if (userType == UserType.MERCHANT) {
                dealRepository.save(deal);
            } else {
                BankDeal bankDeal = new BankDeal(deal);
                bankDeal.setMerchantId(dealRequestDto.getShopId());
                bankDealRepository.save(bankDeal);
                merchantBankSearchIndexService.save(bankDeal, basicMerchantBusinessResponseDto, merchant);
            }
            var filterDeal = new DealSearchIndex(deal, basicMerchantBusinessResponseDto, userType);
            dealSearchService.save(filterDeal);
            totalSavingsScheduler.triggerRecalculationAsync();
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving deal to database was failed", e);
        }
    }

    /**
     * Update deal delete flag
     *
     * @param userType Merchant/Bank
     * @param dealId   deal Id
     */
    public void deleteDeal(String dealId, UserType userType) {
        try {
            Deal dealFromDb = getDeal(dealId, userType);
            // Allow deleting DRAFT, APPROVED, UNPUBLISHED, PENDING, REJECTED, etc.
            // All deal statuses can be deleted
            dealFromDb.setDeleted(true);
            dealFromDb.setUpdatedAt(System.currentTimeMillis());
            if (userType.equals(UserType.MERCHANT))
                dealRepository.save(dealFromDb);
            else
                bankDealRepository.save(new BankDeal(dealFromDb));
            dealSearchService.deleteDeal(dealId);
            totalSavingsScheduler.triggerRecalculationAsync();
        } catch (DataAccessException e) {
            throw new KupiCoreException("Read/Write deal from database was failed.", e);
        }
    }

    /**
     * Approve the deal
     *
     * @param dealApprovalStatusUpdateRequestDto dealStatusUpdateRequestDto
     */
    public void updateApprovalOfDeal(DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto,
                                     String authToken, UserType userType) {
        try {
            Optional<Deal> optionalDealFromDb = userType.equals(UserType.MERCHANT) ?
                    dealRepository.findPendingDealById(dealApprovalStatusUpdateRequestDto.getId()) :
                    bankDealRepository.findPendingDealById(dealApprovalStatusUpdateRequestDto.getId());
            if (optionalDealFromDb.isPresent()) {
                var dealFromDb = optionalDealFromDb.get();
                if (dealFromDb.getExpiredOn() < System.currentTimeMillis() &&
                        dealApprovalStatusUpdateRequestDto.getApprovalStatus().equals(ApprovalStatus.APPROVED)) {
                    throw new ExpiredDealApprovalException("Unable to approve expired deal of deal id: " +
                            dealApprovalStatusUpdateRequestDto.getId());
                }
                if (dealApprovalStatusUpdateRequestDto.getApprovalStatus() == ApprovalStatus.APPROVED) {
                    SubscriptionRenewal subscriptionRenewal =
                            subscriberService.checkSubscriptionPlanForDeal(dealFromDb.getMerchantId());
                    subscriptionRenewal.updateRemainingDealsPerMonth(-ONE);
                    subscriberService.saveSubscriptionRenewal(subscriptionRenewal);
                }
                dealFromDb.setApprovalStatus(dealApprovalStatusUpdateRequestDto.getApprovalStatus());
                dealFromDb.setComment(dealApprovalStatusUpdateRequestDto.getComment());
                dealFromDb.setUpdatedAt(System.currentTimeMillis());
                if (userType.equals(UserType.MERCHANT))
                    dealRepository.save(dealFromDb);
                else
                    bankDealRepository.save(new BankDeal(dealFromDb));
                dealSearchService.updateApprovalStatus(dealApprovalStatusUpdateRequestDto);
                totalSavingsScheduler.triggerRecalculationAsync();
                dealApprovalService.sendMailAndSmsToMerchant(dealFromDb, dealApprovalStatusUpdateRequestDto, authToken);
            } else
                throw new InvalidDealException(INVALID_DEAL + dealApprovalStatusUpdateRequestDto.getId());
        } catch (DataAccessException e) {
            throw new KupiCoreException("Read/Write deal from database was failed.", e);
        }
    }

    /**
     * Get deal list page wise by searching
     *
     * @param pageable pageable
     * @return page
     */
    public Page<Deal> listAllDeals(Pageable pageable) {
        try {
            return dealRepository.findAllDeals(pageable);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Read deals from database was failed.", e);
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
            return dealSearchService.searchDeals(pageable, merchantId, searchTerm, userType);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Read deals from database was failed.", e);
        }
    }

    /**
     * Get filter deal list page by searching and approval status
     *
     * @param pageable       pageable
     * @param merchantId     merchantId
     * @param approvalStatus Approved/Pending/Reject
     * @param searchTerm     searchTerm
     * @param userType       Bank/Merchant
     * @return Deal Page
     */
    public Page<DealSearchIndex> searchDealsByApprovalStatus(Pageable pageable, String merchantId,
                                                             ApprovalStatus approvalStatus, String searchTerm,
                                                             UserType userType) {
        try {
            if (merchantId.equals(ALL) && searchTerm.equals(ALL)) {
                return dealSearchIndexRepository.findAllNotDeletedDeals(pageable, approvalStatus, userType);
            } else if (merchantId.equals(ALL)) {
                return dealSearchIndexRepository.findAllNotDeletedDealsBySearchTerm(pageable, searchTerm,
                        approvalStatus, userType);
            } else if (searchTerm.equals(ALL)) {
                return dealSearchIndexRepository.findAllNotDeletedDealsByMerchantId(pageable, merchantId,
                        approvalStatus, userType);
            }
            return dealSearchIndexRepository.findAllNotDeletedDealsByMerchantIdAndSearchTerm(pageable, merchantId,
                    approvalStatus, searchTerm, userType);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading all dealSearchIndexes based approval status from database was " +
                    "failed.", e);
        }
    }

    /**
     * Get PENDING deal list page - ALL / searchTerm
     *
     * @param pageable pageable
     * @return page
     */
    public Page<DealSearchIndex> getPendingDeals(Pageable pageable, String searchTerm, UserType userType) {
        try {
            return dealSearchService.getPendingSearchDeals(pageable, searchTerm, userType);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Read pending deals from database was failed.", e);
        }
    }

    /**
     * Get all searchDeal list by category id, merchant id, brand id and search term.
     *
     * @param pageable pageable
     * @return page dealSearchIndex
     */
    public Page<DealSearchIndex> getAllSearchDeals(Pageable pageable, String categoryId,
                                                   String merchantId, String brandId, String searchTerm,
                                                   boolean onlyActiveMerchant, boolean onlyActiveDeals, UserType userType) {

        return (onlyActiveMerchant && onlyActiveDeals) ? dealSearchService
                .getAllActiveDealsForActiveMerchant(pageable, categoryId, merchantId, brandId, searchTerm, userType) :
                dealSearchService.getAllSearchDeals(pageable, categoryId, merchantId, brandId, searchTerm, userType.name());

    }

    /**
     * Get all search deals with time-based filtering.
     * This method considers startTime and endTime fields to filter deals.
     *
     * @param pageable           pageable
     * @param categoryId         category id
     * @param merchantId         merchant id
     * @param brandId            brand id
     * @param searchTerm         search term
     * @param onlyActiveMerchant only active merchant
     * @param onlyActiveDeals    only active deals
     * @param userType           user type
     * @return DealSearchIndex page
     */
    public Page<DealSearchIndex> getAllTimeBasedSearchDeals(Pageable pageable, String categoryId,
                                                            String merchantId, String brandId, String searchTerm,
                                                            boolean onlyActiveMerchant, boolean onlyActiveDeals, UserType userType, String timeZone) {

        return (onlyActiveMerchant && onlyActiveDeals) ? dealSearchService
                .getAllTimeBasedActiveDealsForActiveMerchant(pageable, categoryId, merchantId, brandId, searchTerm, userType, timeZone) :
                dealSearchService.getAllSearchDeals(pageable, categoryId, merchantId, brandId, searchTerm, userType.name());

    }

    /**
     * Get all recently expiring deals list page.
     *
     * @param pageable   pageable
     * @param categoryId category id
     * @param merchantId merchant id
     * @param brandId    brand id
     * @param searchTerm search term
     * @return DealSearchIndex page
     */
    public Page<DealSearchIndex> getRecentlyExpireDeals(Pageable pageable, String categoryId,
                                                        String merchantId, String brandId, String searchTerm, UserType userType) {
        try {
            return dealSearchService
                    .getAllSearchRecentlyExpireDeals(pageable, categoryId, merchantId, brandId, searchTerm, userType);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading dealSearchIndex from the database was failed.", e);
        }
    }

    /**
     * Get recently expire deals with time-based filtering.
     *
     * @param pageable   pageable
     * @param categoryId category id
     * @param merchantId merchant id
     * @param brandId    brand id
     * @param searchTerm search term
     * @param userType   user type
     * @return DealSearchIndex page with time-based filtering
     */
    public Page<DealSearchIndex> getTimeBasedRecentlyExpireDeals(Pageable pageable, String categoryId,
                                                                 String merchantId, String brandId, String searchTerm, UserType userType, String timeZone) {
        try {
            return dealSearchService
                    .getAllTimeBasedSearchRecentlyExpireDeals(pageable, categoryId, merchantId, brandId, searchTerm, userType, timeZone);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading dealSearchIndex from the database was failed.", e);
        }
    }

    /**
     * This method read deal page by merchant Id
     *
     * @param pageable   pageable
     * @param merchantId merchantId
     * @return page
     */
    public Page<Deal> listAllDealsByMerchant(Pageable pageable, String merchantId) {
        try {
            return dealRepository.findAllDealsByMerchantId(pageable, merchantId);
        } catch (DataAccessException e) {
            throw new KupiCoreException(READ_DEAL, e);
        }
    }

    /**
     * Get deal by Id
     *
     * @param dealId dealId
     * @return deal
     */
    public Deal getDeal(String dealId, UserType userType) {
        try {
            Optional<Deal> optionalDealFromDb;
            if (userType.equals(UserType.MERCHANT))
                optionalDealFromDb = dealRepository.findDealById(dealId);
            else {
                Optional<BankDeal> optionalBankDeal = bankDealRepository.findDealById(dealId);
                optionalDealFromDb = optionalBankDeal.map(Deal::new);
            }
            if (optionalDealFromDb.isPresent()) {
                return optionalDealFromDb.get();
            } else {
                throw new InvalidDealException(INVALID_DEAL + dealId);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException(READ_DEAL, e);
        }
    }

    /**
     * This method update an existing deal and deal search index.
     *
     * @param dealUpdateRequestDto        dealUpdateRequestDto
     * @param userType                    Merchant/Bank
     * @param deal                        deal From DB
     * @param businessMerchantResponseDto businessMerchantResponseDto
     * @return deal
     */
    public Deal updateDeal(DealUpdateRequestDto dealUpdateRequestDto, UserType userType, Deal deal,
                           BusinessMerchantResponseDto businessMerchantResponseDto) {
        try {
            if (!userType.toString().equalsIgnoreCase(businessMerchantResponseDto.getProfileType()))
                throw new InvalidUserException("User type doesn't match with user id.");
            var categoriesList = categoryService.getCategorySetByIds(dealUpdateRequestDto.getCategoryIds());
            Set<Brand> brandsList = new HashSet<>();
            deal.setRelatedCategories(categoriesList);
            if (dealUpdateRequestDto.isBrandIdsAvailable()) {
                brandsList.addAll(brandService.getBrandSetByIdList(dealUpdateRequestDto.getBrandIds()));
                deal.setRelatedBrands(brandsList);
            }
            PromoType promoType = null;
            if (dealUpdateRequestDto.getPromoTypeId() != null && !dealUpdateRequestDto.getPromoTypeId().trim().isEmpty()) {
                promoType = promoTypeService.getPromoTypeById(dealUpdateRequestDto.getPromoTypeId());
            }
            deal.update(dealUpdateRequestDto, categoriesList, brandsList, promoType);
            dealSearchService.updateDealSearchIndex(deal, businessMerchantResponseDto);
            totalSavingsScheduler.triggerRecalculationAsync();
            if (userType.equals(UserType.MERCHANT))
                dealRepository.save(deal);
            else
                bankDealRepository.save(new BankDeal(deal));
            return deal;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Update deal was failed.", e);
        }
    }

    /**
     * Publish a draft deal (change status from DRAFT to APPROVED).
     * Updates both Deal and DealSearchIndex.
     * Only for MERCHANT deals.
     *
     * @param dealId deal ID
     */
    public void publishDraftDeal(String dealId) {
        try {
            Deal deal = getDeal(dealId, UserType.MERCHANT);

            if (deal.getApprovalStatus() == ApprovalStatus.DEACTIVATE) {
                throw new CannotRepublishDealException("Deactivate deal cannot be re publish.");
            }
            deal.setApprovalStatus(ApprovalStatus.APPROVED);
            deal.setUpdatedAt(System.currentTimeMillis());
            dealRepository.save(deal);
            Optional<DealSearchIndex> optionalDealSearchIndex = dealSearchIndexRepository.findById(dealId);
            if (optionalDealSearchIndex.isPresent()) {
                DealSearchIndex dealSearchIndex = optionalDealSearchIndex.get();
                dealSearchIndex.setApprovalStatus(ApprovalStatus.APPROVED);
                dealSearchIndex.setUpdatedAt(System.currentTimeMillis());
                dealSearchIndexRepository.save(dealSearchIndex);
            }
            totalSavingsScheduler.triggerRecalculationAsync();

        } catch (DataAccessException e) {
            throw new KupiCoreException("Publishing draft deal was failed for dealId: " + dealId, e);
        }
    }

    /**
     * Unpublish an approved deal (change status from APPROVED to UNPUBLISHED).
     * Updates both Deal and DealSearchIndex.
     * Only for MERCHANT deals.
     * Once unpublished, the deal cannot be published again.
     *
     * @param dealId deal ID
     */
    public void unpublishDeal(String dealId) {
        try {
            Deal deal = getDeal(dealId, UserType.MERCHANT);

            if (deal.getApprovalStatus() != ApprovalStatus.APPROVED) {
                throw new InvalidDealException("Only APPROVED deals can be unpublished. Current status: " + deal.getApprovalStatus());
            }

            deal.setApprovalStatus(ApprovalStatus.DEACTIVATE);
            deal.setUpdatedAt(System.currentTimeMillis());
            dealRepository.save(deal);

            Optional<DealSearchIndex> optionalDealSearchIndex = dealSearchIndexRepository.findById(dealId);
            if (optionalDealSearchIndex.isPresent()) {
                DealSearchIndex dealSearchIndex = optionalDealSearchIndex.get();
                dealSearchIndex.setApprovalStatus(ApprovalStatus.DEACTIVATE);
                dealSearchIndex.setUpdatedAt(System.currentTimeMillis());
                dealSearchIndexRepository.save(dealSearchIndex);
            }
            totalSavingsScheduler.triggerRecalculationAsync();

        } catch (DataAccessException e) {
            throw new KupiCoreException("Unpublishing deal was failed for dealId: " + dealId, e);
        }
    }

    /**
     * This method returns active bank deals for active bankId & merchantId.
     *
     * @param pageable   pageable
     * @param bankId     bankId
     * @param merchantId merchantId
     * @return bank deals page.
     */
    public Page<BankDeal> getActiveDealsForActiveBankAndMerchant(Pageable pageable, String bankId, String merchantId) {
        try {
            List<String> dealIdList = dealSearchService.getActiveDealsForActiveBank(bankId);
            return bankDealRepository.findByMerchantIdAndIdIn(pageable, merchantId, dealIdList);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Getting active deals for active bank & merchant was failed.", e);
        }
    }

    /**
     * This method returns true when bank deal's expiredOn equal or less than current mills.
     *
     * @param dealId deal id
     * @return true/ false
     */
    public boolean checkDealIsExpired(String dealId) {
        try {
            return bankDealRepository.isBankDealExpired(dealId, System.currentTimeMillis());
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading BankDeal by id: " + dealId + " was failed.", e);
        }
    }

    /**
     * This method return true when bank deal exists by deal id.
     *
     * @param dealId deal id
     * @return true/ false
     */
    public boolean checkBankDealExistByDealId(String dealId) {
        try {
            return bankDealRepository.existsById(dealId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading BankDeal by id: " + dealId + " was failed.", e);
        }
    }

    /**
     * This method set isDealsOfTheDay status true on merchant deals.
     *
     * @param dealList deal search index list.
     */
    public void setDealsOfTheDayFlagForMerchantDeal(List<Deal> dealList) {
        try {
            dealList.forEach(deal -> deal.setDealOfTheDay(true));
            dealRepository.saveAll(dealList);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving deal list was failed.", e);
        }
    }

    /**
     * This method used to set isDealOfTheDay flag on bank deals.
     *
     * @param bankDealList bankDealList
     */
    public void setDealsOfTheDayFlagForBankDeal(List<BankDeal> bankDealList) {
        try {
            bankDealList.forEach(bankDeal -> bankDeal.setDealOfTheDay(true));
            bankDealRepository.saveAll(bankDealList);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving bank deal list was failed.", e);
        }
    }

    /**
     * This method used to unset isDealOfTheDay flag for merchant deals.
     */
    public void disableIsDealOfTheDayFromMerchantDeals() {
        try {
            dealRepository.disableDealOfTheDayFlags();
        } catch (DataAccessException e) {
            throw new KupiCoreException("Disabling deal of the day flag from merchant deals was failed.");
        }
    }

    /**
     * This method used to unset isDealOfTheDay flag for bank deals.
     */
    public void disableIsDealOfTheDayFromBankDeals() {
        try {
            bankDealRepository.disableDealOfTheDayFlags();
        } catch (DataAccessException e) {
            throw new KupiCoreException("Disabling deal of the day flag from bank deals was failed.");
        }
    }

    /**
     * This method is used to get deals for bulk merchantIds.
     *
     * @param merchantIds merchantIds
     * @param timeZone    timeZone (not used, kept for backward compatibility)
     * @return list of deals.
     */
    public List<DealSearchIndex> getDealsForBulkMerchantIds(List<String> merchantIds, String timeZone) {
        try {
            long currentTimeInMillis = System.currentTimeMillis();
            return dealSearchIndexRepository.getApprovedValidDealsByMerchantIds(
                    currentTimeInMillis, merchantIds);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Getting deals for bulk merchantIds was failed. ", e);
        }
    }

    /**
     * This method returns active merchant deals by merchant id
     *
     * @param pageable   pageable
     * @param time       time (date) in milliseconds
     * @param merchantId merchant id
     * @return dealSearchIndex page
     */
    public Page<DealSearchIndex> getActiveMerchantDeals(Pageable pageable, long time, String merchantId) {
        try {
            return dealSearchIndexRepository.getActiveMerchantDeals(pageable, merchantId, time);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Getting active merchant deals was failed for merchantId: " + merchantId, e);
        }
    }

    /**
     * This method is used to update coupon status of a deal
     *
     * @param dealId           deal id
     * @param userType         user type
     * @param activeStatusType active status type
     */
    public void updateDealCouponStatus(String dealId, UserType userType, ActiveStatusType activeStatusType) {
        try {
            Deal deal = getDeal(dealId, userType);
            DealSearchIndex dealSearchIndex = dealSearchService.findById(dealId);
            if (activeStatusType == ActiveStatusType.ACTIVE) {
                deal.setCouponActive(true);
                dealSearchIndex.setCouponActive(true);
            } else {
                deal.setCouponActive(false);
                dealSearchIndex.setCouponActive(false);
            }
            dealRepository.save(deal);
            dealSearchIndexRepository.save(dealSearchIndex);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to update deal coupon status for dealId: " + dealId, e);
        }
    }

    /**
     * This method creates a dealSearchIndex page based on coupon availability.
     *
     * @param dealPage             dealPage
     * @param couponStatisticsList couponStatisticsList
     * @return dealSearchIndex page
     */
    public Page<DealSearchIndex> removeCouponNotAvailableDeals(Page<DealSearchIndex> dealPage,
                                                               List<CouponStatistics> couponStatisticsList) {
        List<DealSearchIndex> dealSearchIndexList = new ArrayList<>();
        dealPage.get().forEach(dealSearchIndex -> {
            if (!dealSearchIndex.isCouponAvailable()) {
                dealSearchIndexList.add(dealSearchIndex);
            } else {
                for (var couponStatistic : couponStatisticsList) {
                    if (couponStatistic.getDeal().getId().equals(dealSearchIndex.getId())) {
                        dealSearchIndexList.add(dealSearchIndex);
                    }
                }
            }
        });
        return new PageImpl<>(dealSearchIndexList);
    }

    /**
     * This method is used to get deals summary for a merchant
     *
     * @param merchantId merchant id
     * @return deal summary
     */
    public Map<String, Long> getDealSummaryForMerchant(String merchantId) {
        try {
            long couponDeals = dealRepository.countDealsByMerchantId(merchantId, true);
            long specialOfferDeals = dealRepository.countDealsByMerchantId(merchantId, false);
            Map<String, Long> dealSummaryMap = new HashMap<>();
            dealSummaryMap.put("totalDeals", couponDeals + specialOfferDeals);
            dealSummaryMap.put("couponDeals", couponDeals);
            dealSummaryMap.put("specialOfferDeals", specialOfferDeals);
            return dealSummaryMap;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to retrieve deal summary for merchant: " + merchantId, e);
        }
    }

    /**
     * Search deals with advanced filtering by status, category, promoType, date range, and search term
     *
     * @param pageable   pageable
     * @param status     status filter (ALL, active, inactive, Draft, Expired, Deactivated)
     * @param categoryId category id (ALL or specific)
     * @param promoTypeId promo type id (ALL or specific)
     * @param merchantId merchant id
     * @param searchTerm search term
     * @param userType   user type
     * @param startDate  start date in milliseconds (null means current time)
     * @param endDate    end date in milliseconds (null means current time)
     * @return Page of DealSearchIndex
     */
    public Page<DealSearchIndex> searchDealsWithAdvancedFilter(Pageable pageable, String status, String categoryId,
                                                               String promoTypeId, String merchantId, String searchTerm,
                                                               UserType userType, Long startDate, Long endDate) {
        try {
            long currentTime = System.currentTimeMillis();
            long startMillis = startDate != null ? startDate : currentTime;
            long endMillis = endDate != null ? endDate : currentTime;

            // Convert UserType enum to string for native query
            String userTypeString = userType.name();
            
            // If dates are provided, filter by date range (1 = filter, 0 = don't filter)
            int filterByDateRange = (startDate != null && endDate != null) ? 1 : 0;

            return dealSearchIndexRepository.findDealsWithAdvancedFilter(
                    pageable, status, categoryId, promoTypeId, merchantId, searchTerm, userTypeString, 
                    startMillis, endMillis, filterByDateRange);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading deals with advanced filter from database was failed.", e);
        }
    }

    /**
     * Calculate deal status based on the provided date range and approval status
     *
     * @param dealSearchIndex deal search index
     * @param startMillis      start date in milliseconds (from request, or current time if not provided)
     * @param endMillis        end date in milliseconds (from request, or current time if not provided)
     * @return deal status string (active, inactive, Draft, Expired, Deactivated)
     */
    public String calculateDealStatus(DealSearchIndex dealSearchIndex, long startMillis, long endMillis) {
        ApprovalStatus approvalStatus = dealSearchIndex.getApprovalStatus();
        
        // Use the end of the date range as the reference time for status calculation
        // This represents the "current time" within the context of the query
        long referenceTime = endMillis;
        
        // Deactivated status - highest priority
        if (approvalStatus == ApprovalStatus.DEACTIVATE) {
            return "Deactivated";
        }
        
        // Draft status
        if (approvalStatus == ApprovalStatus.DRAFT) {
            return "Draft";
        }
        
        // Check if deal is expired (expiredOn is before the reference time)
        // This takes priority over active/inactive status
        if (approvalStatus == ApprovalStatus.APPROVED && dealSearchIndex.getExpiredOn() < referenceTime) {
            return "Expired";
        }
        
        // For approved deals, check active/inactive status
        // Both require coupon availability
        // Note: At this point, we know expiredOn >= referenceTime (otherwise we would have returned "Expired" above)
        if (approvalStatus == ApprovalStatus.APPROVED && dealSearchIndex.isCouponAvailable()) {
            // Active: current time is between validFrom and expiredOn
            // Since expiredOn >= referenceTime is already guaranteed, we only need to check validFrom
            if (dealSearchIndex.getValidFrom() <= referenceTime) {
                return "active";
            }
            
            // Inactive: approved but current time is before validFrom (not yet started)
            // If we reach here, validFrom > referenceTime is guaranteed (since active check above failed)
            // expiredOn >= referenceTime is already guaranteed from the expired check above
            return "inactive";
        }
        
        // For other approval statuses (PENDING, REJECTED, etc.) that are not expired, return Draft
        return "Draft";
    }

    /**
     * Calculate earnings for a deal based on redeemed coupons
     *
     * @param dealId     deal id
     * @param startMillis start date in milliseconds
     * @param endMillis   end date in milliseconds
     * @return earnings as BigDecimal
     */
    public java.math.BigDecimal calculateEarnings(String dealId, long startMillis, long endMillis) {
        try {
            Optional<Deal> dealOpt = dealRepository.findDealById(dealId);
            if (dealOpt.isEmpty()) {
                return java.math.BigDecimal.ZERO;
            }
            
            Deal deal = dealOpt.get();
            Double originalPrice = deal.getOriginalPrice();
            if (originalPrice == null || originalPrice <= 0) {
                return java.math.BigDecimal.ZERO;
            }
            
            // Calculate deduction amount
            double deductionAmount = 0.0;
            if (deal.getDeductionType() == com.kupi.cc.base.enums.DeductionType.AMOUNT) {
                Double deduction = deal.getDeductionAmount();
                deductionAmount = deduction != null ? deduction : 0.0;
            } else if (deal.getDeductionType() == com.kupi.cc.base.enums.DeductionType.PERCENTAGE) {
                Double deductionPercentage = deal.getDeductionPercentage();
                if (deductionPercentage != null && deductionPercentage > 0) {
                    deductionAmount = originalPrice * (deductionPercentage / 100.0);
                }
            }
            
            // Calculate earnings per coupon
            double earningsPerCoupon = originalPrice - deductionAmount;
            
            // Count redeemed coupons in date range
            long redeemedCount = couponRepository.countRedeemedCouponsByDealIdAndDateRange(dealId, startMillis, endMillis);
            
            // Total earnings = earnings per coupon * redeemed count
            double totalEarnings = earningsPerCoupon * redeemedCount;
            
            return java.math.BigDecimal.valueOf(totalEarnings);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to calculate earnings for deal: " + dealId, e);
        }
    }
}
