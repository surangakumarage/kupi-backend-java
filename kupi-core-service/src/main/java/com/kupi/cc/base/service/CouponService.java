package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Coupon;
import com.kupi.cc.base.domain.entity.CouponStatistics;
import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.request.CouponFileUploadRequestDto;
import com.kupi.cc.base.domain.request.RedeemRequestDto;
import com.kupi.cc.base.domain.request.ValidityPeriodDto;
import com.kupi.cc.base.util.DateParserUtil;
import com.kupi.cc.base.domain.response.*;
import com.kupi.cc.base.enums.*;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.repository.CouponRepository;
import com.kupi.cc.base.repository.CouponStatisticsRepository;
import com.kupi.cc.base.repository.DealRepository;
import com.kupi.cc.base.repository.DealSearchIndexRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Coupon service
 */
@Service
@Slf4j
public class CouponService {

    private static final String DEAL_ID = "dealId";
    private static final String FILE_NAME = "fileName";
    private static final String CURRENT_MILLS = "currentMills";
    private static final String TIME_ZONE = "timeZone";
    private static final String ALL = "ALL";
    private static final String USER = "USER";
    private static final String MERCHANT = "MERCHANT";
    private static final String ADMIN = "Admin";
    private static final String DEAL = "DEAL";
    private static final String SEARCH = "SEARCH";
    private static final String FILTER = "FILTER";
    private static final String BIND = "_";
    private final DealSearchIndexRepository dealSearchIndexRepository;
    private final CouponRepository couponRepository;
    private final CouponStatisticsRepository couponStatisticsRepository;
    private final FileDownloaderService fileDownloaderService;
    private final JobLauncher jobLauncher;
    private final Job job;
    private final DealService dealService;
    private final DealRepository dealRepository;
    private final AuthUserService authUserService;
    private final UserSubscriptionService userSubscriptionService;
    private final CouponCodeGeneratorService couponCodeGeneratorService;
    private final DateParserUtil dateParserUtil;


    public CouponService(DealSearchIndexRepository dealSearchIndexRepository,
                         CouponRepository couponRepository, CouponStatisticsRepository couponStatisticsRepository,
                         FileDownloaderService fileDownloaderService, JobLauncher jobLauncher, @Lazy Job job,
                         DealService dealService, DealRepository dealRepository, AuthUserService authUserService, 
                         UserSubscriptionService userSubscriptionService, CouponCodeGeneratorService couponCodeGeneratorService,
                         DateParserUtil dateParserUtil) {
        this.dealSearchIndexRepository = dealSearchIndexRepository;
        this.couponRepository = couponRepository;
        this.couponStatisticsRepository = couponStatisticsRepository;
        this.fileDownloaderService = fileDownloaderService;
        this.jobLauncher = jobLauncher;
        this.job = job;
        this.dealService = dealService;
        this.dealRepository = dealRepository;
        this.authUserService = authUserService;
        this.userSubscriptionService = userSubscriptionService;
        this.couponCodeGeneratorService = couponCodeGeneratorService;
        this.dateParserUtil = dateParserUtil;
    }

    /**
     * This method is used to list coupon statistics.
     *
     * @param pageable pageable
     * @return coupon stat page.
     */
    public Page<CouponStatistics> listCoupons(Pageable pageable) {
        try {
            return couponStatisticsRepository.findAll(pageable);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Filed to list coupon statistics.", e);
        }
    }

    /**
     * This method is used to get a page of deals with coupons.
     *
     * @param userId     userId/ALL
     * @param searchTerm searchTerm/ALL
     * @param pageable   pageable
     * @param filter     filter
     * @return a page deals with coupons.
     */
    public Page<DealSearchIndex> listDealsWithCoupons(String userId, String searchTerm, Pageable pageable,
                                                      CouponFilter filter) {
        try {
            CouponListQueryType couponListQueryType = createQueryType(userId, searchTerm, filter.toString());
            boolean freeCoupons = filter.equals(CouponFilter.FREE);
            switch (couponListQueryType) {
                case ALL_ALL_ALL:
                    return dealSearchIndexRepository.getCouponDeals(pageable);
                case USER_ALL_ALL:
                    return dealSearchIndexRepository.getCouponDealsByMerchantId(userId, pageable);
                case ALL_SEARCH_ALL:
                    return dealSearchIndexRepository.getCouponDealsWithSearchTerm(searchTerm, pageable);
                case USER_SEARCH_ALL:
                    return dealSearchIndexRepository
                            .getCouponDealsWithMerchantIdAndSearchTerm(userId, searchTerm, pageable);
                case ALL_ALL_FILTER:
                    return dealSearchIndexRepository.getCouponDealsByCouponType(freeCoupons, pageable);
                case USER_ALL_FILTER:
                    return dealSearchIndexRepository
                            .getCouponDealsByMerchantIdAndCouponType(userId, freeCoupons, pageable);
                case ALL_SEARCH_FILTER:
                    return dealSearchIndexRepository
                            .getCouponDealsWithSearchTermAndCouponType(searchTerm, freeCoupons, pageable);
                case USER_SEARCH_FILTER:
                    return dealSearchIndexRepository
                            .getCouponDealsWithMerchantIdAndSearchTermAndCouponType(userId, searchTerm, freeCoupons, pageable);
                default:
                    throw new KupiCoreException("Invalid coupon list query.");
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to get coupon deal list from db.", e);
        }
    }

    /**
     * This method is used to create query type for coupon list.
     *
     * @param userId     userId
     * @param searchTerm searchTerm
     * @param filter     filter
     * @return query type enum.
     */
    private CouponListQueryType createQueryType(String userId, String searchTerm, String filter) {
        userId = userId.equals(ALL) ? ALL : USER;
        searchTerm = searchTerm.equals(ALL) ? ALL : SEARCH;
        filter = filter.equals(ALL) ? ALL : FILTER;
        return CouponListQueryType.valueOf(userId + BIND + searchTerm + BIND + filter);
    }

    /**
     * This method is used to get a page of coupons with dealId.
     *
     * @param pageable   pageable
     * @param dealId     dealId
     * @param searchTerm searchTerm (not used in query, filtering done in controller)
     * @return page of coupons.
     */
    public Page<Coupon> getCouponsByDealId(Pageable pageable, String dealId, String searchTerm) {
        try {
            return couponRepository.findByDealId(dealId, pageable);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to get coupons from database for dealId: " + dealId, e);
        }
    }

    /**
     * This method is used to get a page of coupons with dealId, filtered by user name and date range.
     * It handles fetching user information, filtering by user name, filtering by redeemedOn date range, and pagination.
     *
     * @param pageable   pageable
     * @param dealId     dealId
     * @param searchTerm searchTerm (filters by user name who redeemed the coupon)
     * @param userId     userId for fetching user information
     * @param userMap    output parameter that will be populated with user map
     * @param startMillis start date in milliseconds (optional, must be provided with endMillis)
     * @param endMillis   end date in milliseconds (optional, must be provided with startMillis)
     * @return filtered and paginated coupon page
     */
    public Page<Coupon> getCouponsByDealIdWithUserFilter(Pageable pageable, String dealId, String searchTerm, String userId, Map<String, BasicUserResponseDto> userMap, Long startMillis, Long endMillis) {
        try {
            Page<Coupon> allCouponsPage = couponRepository.findByDealId(dealId, Pageable.unpaged());
            
            // Filter to only include coupons that have purchasedOn or redeemedOn dates (or both)
            List<Coupon> filteredCoupons = allCouponsPage.getContent().stream()
                    .filter(coupon -> coupon.getPurchasedOn() != null || coupon.getRedeemedOn() != null)
                    .collect(Collectors.toList());
            
            List<String> userIdList = filteredCoupons.stream()
                    .map(Coupon::getPurchasedBy)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            
            Map<String, BasicUserResponseDto> fetchedUserMap = userIdList.isEmpty() ? new HashMap<>() :
                    authUserService.getUserMap(userId, new BulkUserRequestDto(userIdList));
            userMap.putAll(fetchedUserMap);
            
            // Filter by user name if searchTerm is not "ALL"
            if (!searchTerm.equals(ALL)) {
                filteredCoupons = filteredCoupons.stream()
                        .filter(coupon -> {
                            BasicUserResponseDto user = userMap.get(coupon.getPurchasedBy());
                            return user != null && user.getName() != null &&
                                    user.getName().toLowerCase().contains(searchTerm.toLowerCase());
                        })
                        .collect(Collectors.toList());
            }
            
            // Filter by redeemedOn date range if both dates are provided
            if (startMillis != null && endMillis != null) {
                filteredCoupons = filteredCoupons.stream()
                        .filter(coupon -> {
                            Long redeemedOn = coupon.getRedeemedOn();
                            return redeemedOn != null && redeemedOn >= startMillis && redeemedOn <= endMillis;
                        })
                        .collect(Collectors.toList());
            }
            
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredCoupons.size());
            List<Coupon> paginatedCoupons = start < filteredCoupons.size() 
                    ? filteredCoupons.subList(start, end) 
                    : Collections.emptyList();
            
            return new PageImpl<>(paginatedCoupons, pageable, filteredCoupons.size());
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to get coupons from database for dealId: " + dealId, e);
        }
    }

    /**
     * This method is used to get new coupon.
     *
     * @param dealId dealId
     * @param userId userId
     * @return coupon details.
     */
    public BasicCouponResponseDto getNewCouponCode(String dealId, String userId,String timeZone,String token) {
        try {
            Optional<CouponStatistics> optionalCouponStatistics = couponStatisticsRepository.findByDealId(dealId);
            if (optionalCouponStatistics.isPresent() && optionalCouponStatistics.get().getRemainingCoupons() != 0) {
                CouponStatistics couponStatistics = optionalCouponStatistics.get();
                if (!couponStatistics.getDeal().isCouponActive())
                    throw new CouponException("Coupons disabled for deal.", CouponErrorType.DEAL_IS_NOT_AVAILABLE);
//                if (couponRepository.findByDealIdAndPurchasedBy(dealId, userId).isPresent())
//                    throw new CouponException("Coupon already claimed", CouponErrorType.COUPON_ALREADY_PURCHASED);
                //todo:remove comments
                Optional<Coupon> optionalCoupon = couponRepository.getNewCoupon(dealId, System.currentTimeMillis());
                DealSearchIndex dealSearchIndex = dealSearchIndexRepository.findById(dealId).orElseThrow(() ->
                        new InvalidDealException("Deal not found in search index table. dealId: " + dealId));
                userSubscriptionService.verifyCouponAvailability(userId);
                if (optionalCoupon.isPresent()) {
                    Coupon coupon = optionalCoupon.get();
                    coupon.assignNewCouponForUser(userId);
                    couponStatistics.updateCouponStatisticsOnPurchase();
                    dealSearchIndex.updateRemainingCouponsOnPurchase();
                    couponRepository.save(coupon);
                    couponStatisticsRepository.save(couponStatistics);
                    dealSearchIndexRepository.save(dealSearchIndex);
                    userSubscriptionService.incrementUsedCouponCount(coupon.getPurchasedBy());
                    BusinessMerchantResponseDto merchant = authUserService.getMerchantBusinessByMerchantId(token, coupon.getDeal().getMerchantId());
                    return new BasicCouponResponseDto(coupon,timeZone,merchant );
                }
            }
            throw new CouponException("Coupons not found for dealId: " + dealId, CouponErrorType.COUPON_NOT_FOUND);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to redeem coupon from database.", e);
        }
    }

    /**
     * This method is used to update coupon purchase status.
     *
     * @param couponId             couponId
     * @param dealId               dealId
     * @param couponPurchaseStatus couponPurchaseStatus
     */
    public void updateCouponPurchaseStatus(String couponId, String dealId, CouponPurchaseStatus couponPurchaseStatus) {
        try {
            Optional<Coupon> optionalCoupon = couponRepository.findById(couponId);
            Optional<CouponStatistics> optionalCouponStatistics = couponStatisticsRepository.findByDealId(dealId);
            DealSearchIndex dealSearchIndex = dealSearchIndexRepository.findById(dealId).orElseThrow(() ->
                    new InvalidDealException("Deal not found in search index table. dealId: " + dealId));
            if (optionalCoupon.isPresent() && optionalCouponStatistics.isPresent()) {
                Coupon coupon = optionalCoupon.get();
                CouponStatistics couponStatistics = optionalCouponStatistics.get();
                if (couponPurchaseStatus == CouponPurchaseStatus.NOT_PURCHASED) {
                    coupon.setPurchasedBy(null);
                    coupon.setPurchasedOn(null);
                    couponStatistics.updateCouponStatisticsOnPurchaseCancellation();
                    dealSearchIndex.updateRemainingCouponsOnPurchaseCancellation();
                }
                coupon.updateCouponPurchaseStatus(couponPurchaseStatus);
                couponRepository.save(coupon);
                couponStatisticsRepository.save(couponStatistics);
                dealSearchIndexRepository.save(dealSearchIndex);
            } else
                throw new CouponException("Coupon not found for couponId: " + couponId, CouponErrorType.COUPON_NOT_FOUND);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to update coupon purchase status for couponId: " + couponId, e);
        }
    }

    /**
     * This method is used to redeem coupons by merchant.
     *
     * @param redeemRequestDto redeemRequestDto
     */
    public CouponRedeemSuccessResponseDto redeemCoupon(RedeemRequestDto redeemRequestDto, String timeZone) {
        try {
            Coupon coupon = validateRedeemRequest(redeemRequestDto);
            coupon.redeemCoupon();
            couponRepository.save(coupon);
            // Update coupon statistics on redeem
            Deal deal = coupon.getDeal();
            Optional<CouponStatistics> optionalCouponStatistics =
                    couponStatisticsRepository.findByDealId(deal.getId());
            optionalCouponStatistics.ifPresent(couponStatistics -> {
                couponStatistics.updateCouponStatisticsOnRedeem();
                couponStatisticsRepository.save(couponStatistics);
            });
            Long couponRedeemedOn = coupon.getRedeemedOn();
            List<String> purchasedUser = Collections.singletonList(coupon.getPurchasedBy());
            BulkUserRequestDto bulkUserRequestDto = new BulkUserRequestDto(purchasedUser);
            List<BasicMerchantBusinessResponseDto> users =
                    authUserService.getBulkUserResponse(ADMIN, bulkUserRequestDto).getUsers();
            Optional<BasicMerchantBusinessResponseDto> optionalUser =
                    users.stream().findFirst();
            if (optionalUser.isEmpty()) {
                throw new KupiCoreException("No user found for coupon redemption");
            }
            return new CouponRedeemSuccessResponseDto(deal, optionalUser.get(), couponRedeemedOn, timeZone);
        } catch (UserSubscriptionExpiredException e) {
            throw new UserSubscriptionExpiredException("User subscription expired");
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to redeem coupon for merchantId: " + redeemRequestDto.getMerchantId(), e);
        }
    }

    /**
     * This method validates the coupon and returns the validation result.
     *
     * @param redeemRequestDto redeemRequestDto
     * @return true/ false
     */
    public CouponValidationResponseDto validateCoupon(RedeemRequestDto redeemRequestDto) {
        try {
            validateRedeemRequest(redeemRequestDto);
            return new CouponValidationResponseDto(true);
        } catch (CouponException | InvalidUserException | ExpiredDealException e) {
            log.warn("Coupon validation failed. Code: {}, Reason: {}",
                    redeemRequestDto.getCouponCode(),
                    e.getMessage());
            return new CouponValidationResponseDto(false);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to validate coupon for merchantId: " + redeemRequestDto.getMerchantId(), e);
        }
    }

    /**
     * This method is used to validate coupon redeem request.
     *
     * @param redeemRequestDto redeemRequestDto
     * @return coupon.
     */
    private Coupon validateRedeemRequest(RedeemRequestDto redeemRequestDto) {
        Optional<Coupon> optionalCoupon = couponRepository
                .findByCouponCodeAndDeal_MerchantId(redeemRequestDto.getCouponCode(), redeemRequestDto.getMerchantId());
        if (optionalCoupon.isEmpty())
            throw new CouponException("Coupon not found", CouponErrorType.COUPON_NOT_FOUND);
        Coupon coupon = optionalCoupon.get();
        Deal deal = coupon.getDeal();

        if (!coupon.isCouponActive() || !deal.isCouponActive())
            throw new CouponException("Coupon is inactive", CouponErrorType.INACTIVE_COUPON);
        if (coupon.isRedeemed())
            throw new CouponException("Coupon already redeemed", CouponErrorType.REDEEMED_COUPON);
        if (!deal.getMerchantId().equals(redeemRequestDto.getMerchantId()))
            throw new InvalidUserException("Deal doesn't belongs to merchant: " + redeemRequestDto.getMerchantId());
        if (deal.getExpiredOn() < System.currentTimeMillis())
            throw new ExpiredDealException("Deal expired on: " + deal.getExpiredOn());
        if (coupon.getPurchasedBy() == null)
            throw new CouponException("Coupon is not claimed by user ", CouponErrorType.COUPON_NOT_CLAIMED_BY_USER);

        return coupon;
    }

    /**
     * This method used to upload coupon csv file in to database.
     *
     * @param couponFileUploadRequestDto couponFileUploadRequestDto
     */
    public void uploadCoupons(CouponFileUploadRequestDto couponFileUploadRequestDto, String timeZone, Deal deal) {
        try {
            var outPutFileName = couponFileUploadRequestDto.getFileName();
            fileDownloaderService
                    .download(couponFileUploadRequestDto.getFileUrl(), outPutFileName);
            log.debug("File:{} downloaded.", outPutFileName);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString(DEAL_ID, couponFileUploadRequestDto.getDealId())
                    .addString(FILE_NAME, outPutFileName)
                    .addLong(CURRENT_MILLS, System.currentTimeMillis())
                    .addString(TIME_ZONE, timeZone)
                    .toJobParameters();
            // Get current coupon count before batch processing
            long couponsBeforeUpload = couponRepository.countByDealId(deal.getId());
            
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);
            if (!jobExecution.getStepExecutions().isEmpty()) {
                // Count actual coupons in database for this deal after batch completion
                long totalCouponsAfterUpload = couponRepository.countByDealId(deal.getId());
                int newCouponsGenerated = (int) (totalCouponsAfterUpload - couponsBeforeUpload);
                updateCouponStatistic(newCouponsGenerated, deal);
            }
        } catch (CouponUploadFailedException | JobExecutionAlreadyRunningException | JobRestartException |
                 JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            log.error("Failed to upload coupons to the database for deal id: {}, time zone: {}", deal.getId(), timeZone, e);
            throw new CouponUploadFailedException("Failed to upload coupons to the database.", e);
        }
    }

    /**
     * This method is used to update the coupon status in a deal
     *
     * @param dealId           deal id
     * @param userType         user type
     * @param activeStatusType active status type
     */
    public void updateDealCouponStatus(String dealId, UserType userType, ActiveStatusType activeStatusType) {
        dealService.updateDealCouponStatus(dealId, userType, activeStatusType);
    }

    /**
     * This method update a coupon status
     *
     * @param couponId         coupon id
     * @param activeStatusType ACTIVE/ INACTIVE
     */
    public void updateCouponStatus(String couponId, ActiveStatusType activeStatusType) {
        try {
            boolean status = activeStatusType == ActiveStatusType.ACTIVE;
            Optional<Coupon> optionalCoupon = couponRepository.findById(couponId);
            if (optionalCoupon.isEmpty() || optionalCoupon.get().isCouponActive() == status) {
                throw new CouponException("Invalid coupon id: " + couponId, CouponErrorType.INVALID_COUPON_ID);
            }
            Coupon coupon = optionalCoupon.get();
            if (coupon.getCouponPurchaseStatus() != CouponPurchaseStatus.NOT_PURCHASED)
                throw new CouponException("Coupon already purchased.", CouponErrorType.PURCHASED_COUPON_CANT_UPDATED);
            String dealId = coupon.getDeal().getId();
            DealSearchIndex dealSearchIndex = dealSearchIndexRepository.findById(dealId)
                    .orElseThrow(() -> new InvalidDealException("Deal search index not found for dealId: " + dealId));
            CouponStatistics couponStatistics = coupon.getDeal().getCouponStatistics();
            if (status)
                updateStatsWhenCouponStateChange(couponStatistics, dealSearchIndex, 1);
            else
                updateStatsWhenCouponStateChange(couponStatistics, dealSearchIndex, -1);
            coupon.setCouponActive(status);
            coupon.setUpdatedAt(System.currentTimeMillis());
            couponRepository.save(coupon);
            dealSearchIndexRepository.save(dealSearchIndex);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Coupon status update is failed for coupon id: " + couponId, e);
        }
    }

    /**
     * This method is used to update coupon statistic changes when a coupon state is changed.
     *
     * @param couponStatistics couponStatistics
     * @param dealSearchIndex  dealSearchIndex
     * @param increaseBy       increment variable
     */
    private void updateStatsWhenCouponStateChange(CouponStatistics couponStatistics, DealSearchIndex dealSearchIndex,
                                                  int increaseBy) {
        couponStatistics.setTotalCoupons(couponStatistics.getTotalCoupons() + increaseBy);
        couponStatistics.setRemainingCoupons(couponStatistics.getRemainingCoupons() + increaseBy);
        dealSearchIndex.setTotalCoupons(dealSearchIndex.getTotalCoupons() + increaseBy);
        dealSearchIndex.setRemainingCoupons(dealSearchIndex.getRemainingCoupons() + increaseBy);
    }

    /**
     * This method used to return coupon history page by user id.
     *
     * @param pageable pageable
     * @param userId   userId
     * @return CouponPage
     */
    public UserCouponHistoryPageResponseDto getHistoryByUserId(Pageable pageable, String timeZone, String userId, String filter) {
        try {
            Page<Coupon> coupons;
            switch (filter) {
                case "USED":
                    coupons = couponRepository.findByPurchasedByAndIsRedeemedTrueOrderByExpiryDateAsc(pageable, userId);
                    break;
                case "EXPIRE_SOON":
                    long currentTime = System.currentTimeMillis();
                    long sevenDaysLater = currentTime + Duration.ofDays(7).toMillis();
                    log.debug("EXPIRE_SOON filter: currentTime={}, sevenDaysLater={}", currentTime, sevenDaysLater);

                    // Use explicit query to:
                    // 1. Exclude already expired coupons (expiryDate > currentTime)
                    // 2. Include only coupons expiring within 7 days (expiryDate <= sevenDaysLater)
                    // 3. Exclude already redeemed/used coupons (isRedeemed = false)
                    coupons = couponRepository.findByPurchasedByAndExpiryDateAfterAndExpiryDateBeforeOrderByExpiryDateAsc(
                            pageable, userId, currentTime, sevenDaysLater);
                    log.debug("Database query returned {} coupons", coupons.getTotalElements());

                    // Additional safety filter at application level to ensure no expired coupons are returned
                    // This handles timezone or database comparison edge cases
                    if (!coupons.isEmpty()) {
                        List<Coupon> filteredCoupons = coupons.getContent().stream()
                                .peek(coupon -> log.debug("Coupon {} expiry: {}, expired: {}, redeemed: {}",
                                    coupon.getCouponCode(), coupon.getExpiryDate(),
                                    coupon.getExpiryDate() <= currentTime, coupon.isRedeemed()))
                                .filter(coupon -> coupon.getExpiryDate() > currentTime && coupon.getExpiryDate() <= sevenDaysLater && !coupon.isRedeemed())
                                .collect(Collectors.toList());
                        log.debug("After safety filter: {} coupons remain", filteredCoupons.size());
                        coupons = new PageImpl<>(filteredCoupons, pageable, filteredCoupons.size());
                    }
                    break;
                case "ACTIVE":
                    coupons = couponRepository.findAllByPurchasedBy(pageable, userId);
                    if (!coupons.isEmpty()) {
                        List<Coupon> activeCoupons = coupons.getContent().stream()
                                .filter(Coupon::isCouponActive)
                                .sorted((c1, c2) -> applySortComparator(c1, c2, pageable))
                                .collect(Collectors.toList());
                        coupons = new PageImpl<>(activeCoupons, pageable, activeCoupons.size());
                    }
                    log.debug("Filtered coupons for ACTIVE status, total: {}", coupons.getTotalElements());
                    break;
                case "INACTIVE":
                    coupons = couponRepository.findAllByPurchasedBy(pageable, userId);
                    if (!coupons.isEmpty()) {
                        List<Coupon> inactiveCoupons = coupons.getContent().stream()
                                .filter(coupon -> !coupon.isCouponActive())
                                .sorted((c1, c2) -> applySortComparator(c1, c2, pageable))
                                .collect(Collectors.toList());
                        coupons = new PageImpl<>(inactiveCoupons, pageable, inactiveCoupons.size());
                    }
                    log.debug("Filtered coupons for INACTIVE status, total: {}", coupons.getTotalElements());
                    break;
                case "ALL":
                default:
                    coupons = couponRepository.findAllByPurchasedBy(pageable, userId);
                    break;
            }
            Map<String, MerchantBusinessResponseDto> merchantMap = Collections.emptyMap();
            if (!coupons.isEmpty()) {
                merchantMap = getMerchantMapFromCoupons(coupons);
            }
            return
                    new UserCouponHistoryPageResponseDto(coupons, timeZone, merchantMap);

        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to return coupon history by user id: " + userId);
        }
    }

    /**
     * Helper method to apply sort comparator based on Pageable sort criteria.
     *
     * @param c1       coupon 1
     * @param c2       coupon 2
     * @param pageable pageable with sort criteria
     * @return comparison result
     */
    private int applySortComparator(Coupon c1, Coupon c2, Pageable pageable) {
        if (!pageable.getSort().isSorted()) {
            return 0;
        }

        for (Sort.Order order : pageable.getSort()) {
            int comparison = 0;
            switch (order.getProperty()) {
                case "expiryDate":
                    comparison = Long.compare(c1.getExpiryDate(), c2.getExpiryDate());
                    break;
                case "purchasedOn":
                    comparison = Long.compare(c1.getPurchasedOn() != null ? c1.getPurchasedOn() : 0,
                            c2.getPurchasedOn() != null ? c2.getPurchasedOn() : 0);
                    break;
                case "redeemedOn":
                    comparison = Long.compare(c1.getRedeemedOn() != null ? c1.getRedeemedOn() : 0,
                            c2.getRedeemedOn() != null ? c2.getRedeemedOn() : 0);
                    break;
                case "createdAt":
                    comparison = Long.compare(c1.getCreatedAt(), c2.getCreatedAt());
                    break;
                case "price":
                    comparison = Double.compare(c1.getPrice() != null ? c1.getPrice() : 0.0,
                            c2.getPrice() != null ? c2.getPrice() : 0.0);
                    break;
            }

            if (comparison != 0) {
                return order.isAscending() ? comparison : -comparison;
            }
        }
        return 0;
    }

    /**
     * This method used to get merchant information.
     *
     * @param coupons coupons page
     * @return MerchantBusinessResponseDto map
     */
    private Map<String, MerchantBusinessResponseDto> getMerchantMapFromCoupons(Page<Coupon> coupons) {
        List<String> merchantIdList = coupons.stream()
                .map(coupon -> coupon.getDeal().getMerchantId())
                .distinct()
                .collect(Collectors.toList());

        return authUserService.getMerchantMap(new BulkUserRequestDto(merchantIdList), UserType.MERCHANT);
    }


    /**
     * Create coupons from validity periods during deal creation.
     * Each validity period represents a date, time range, and number of coupons to create.
     *
     * @param deal            the deal to create coupons for
     * @param validityPeriods list of validity periods
     * @param timeZone        timezone for date parsing
     * @param userType        MERCHANT or BANK
     */
    public void createCouponsFromValidityPeriods(Deal deal, List<ValidityPeriodDto> validityPeriods, 
                                                  String timeZone, UserType userType) {
        if (validityPeriods == null || validityPeriods.isEmpty()) {
            log.debug("No validity periods provided for deal: {}", deal.getId());
            return;
        }

        if (!deal.isCouponAvailable()) {
            log.warn("Deal {} is not configured for coupons, skipping coupon creation", deal.getId());
            return;
        }

        try {
            List<Coupon> allCoupons = new ArrayList<>();

            for (ValidityPeriodDto period : validityPeriods) {
                if (!period.isValid()) {
                    log.warn("Invalid validity period skipped: {}", period);
                    continue;
                }

                // Note: Period start date validation is currently disabled
                // If needed, uncomment the validation below
                // String startTime = period.getStartTime();
                // if (startTime != null && startTime.matches("\\d{2}:\\d{2}")) {
                //     startTime = startTime + ":00";
                // }
                // String periodStartDateTimeString = period.getDate() + " " + startTime;
                // long periodStartDate = dateParserUtil.parseDateToMilliseconds(periodStartDateTimeString, timeZone);
                // if (periodStartDate < deal.getValidFrom()) {
                //     log.warn("Validity period start date {} is before deal's validFrom {} for period: date={}, startTime={}, endTime={}. Skipping this period.",
                //             periodStartDate, deal.getValidFrom(), period.getDate(), period.getStartTime(), period.getEndTime());
                //     continue;
                // }

                // Parse date + endTime to get coupon expiry timestamp
                // Format: "yyyy-MM-dd HH:mm:ss" (e.g., "2026-01-01 18:00:00")
                // DateParserUtil supports "yyyy-MM-dd HH:mm:ss" but not "yyyy-MM-dd HH:mm"
                String endTime = period.getEndTime();
                // Append :00 for seconds if time is in HH:mm format (without seconds)
                if (endTime != null && endTime.matches("\\d{2}:\\d{2}")) {
                    endTime = endTime + ":00";
                }
                String dateTimeString = period.getDate() + " " + endTime;
                long couponExpiryDate = dateParserUtil.parseDateToMilliseconds(dateTimeString, timeZone);

                // Validate coupon expiry is not before deal expiry
                if (couponExpiryDate < deal.getExpiredOn()) {
                    log.warn("Coupon expiry date {} is before deal expiry {} for period: {}", 
                            couponExpiryDate, deal.getExpiredOn(), period);
                    // Use deal expiry as coupon expiry instead
                    couponExpiryDate = deal.getExpiredOn();
                }

                // Save coupons one by one with retry logic to ensure we save exactly the requested count
                int requestedCount = period.getCouponCount();
                int successfullySavedForPeriod = 0;
                int maxRetriesPerCoupon = 10;
                
                log.debug("Creating {} coupons for period: date={}, startTime={}, endTime={}", 
                        requestedCount, period.getDate(), period.getStartTime(), period.getEndTime());
                
                while (successfullySavedForPeriod < requestedCount) {
                    String code = null;
                    Coupon coupon = null;
                    boolean saved = false;
                    int retryAttempt = 0;
                    
                    // Try to save a coupon, retrying with new codes if duplicates are found
                    while (!saved && retryAttempt < maxRetriesPerCoupon) {
                        try {
                            // Generate a new unique code for each attempt
                            code = couponCodeGeneratorService.generateUniqueCode();
                            coupon = new Coupon(deal, userType, code, null, couponExpiryDate);
                            
                            // Attempt to save
                            couponRepository.save(coupon);
                            allCoupons.add(coupon);
                            successfullySavedForPeriod++;
                            saved = true;
                            
                            log.debug("Successfully saved coupon {}/{} with code: {} for period: date={}, startTime={}, endTime={}", 
                                    successfullySavedForPeriod, requestedCount, code, 
                                    period.getDate(), period.getStartTime(), period.getEndTime());
                        } catch (org.springframework.dao.DataIntegrityViolationException e) {
                            // Duplicate code detected - generate a new one and retry
                            retryAttempt++;
                            log.warn("Duplicate coupon code detected: {} for deal: {}. Retrying with new code (attempt {}/{})", 
                                    code, deal.getId(), retryAttempt, maxRetriesPerCoupon);
                        } catch (Exception e) {
                            // Other errors - log and retry
                            retryAttempt++;
                            log.error("Failed to save coupon for deal: {} (attempt {}/{}). Error: {}", 
                                    deal.getId(), retryAttempt, maxRetriesPerCoupon, e.getMessage(), e);
                        }
                    }
                    
                    // If we couldn't save after max retries, log error but continue to try next coupon
                    if (!saved) {
                        log.error("Failed to save coupon after {} retries for deal: {} period: date={}, startTime={}, endTime={}. " +
                                "This may indicate database issues or code generation problems.", 
                                maxRetriesPerCoupon, deal.getId(), period.getDate(), period.getStartTime(), period.getEndTime());
                    }
                }
                
                log.info("Successfully created and saved {}/{} coupons for period: date={}, startTime={}, endTime={}", 
                        successfullySavedForPeriod, requestedCount, period.getDate(), period.getStartTime(), period.getEndTime());
            }

            // Update coupon statistics based on total saved count
            if (!allCoupons.isEmpty()) {
                int totalSaved = allCoupons.size();
                updateCouponStatistic(totalSaved, deal);
                log.info("Successfully saved all {} coupons for deal: {}", totalSaved, deal.getId());
            } else {
                log.warn("No coupons were created for deal: {}", deal.getId());
            }

        } catch (Exception e) {
            log.error("Failed to create coupons from validity periods for deal: {}", deal.getId(), e);
            throw new KupiCoreException("Failed to create coupons from validity periods for deal: " + deal.getId(), e);
        }
    }

    /**
     * Update coupons from validity periods during deal update.
     * Intelligently handles coupon creation, deletion, and count adjustments.
     * Only processes coupons for MERCHANT deals (DRAFT status).
     * Matches creation behavior by allowing periods before validFrom (for consistency).
     *
     * @param deal            the deal to update coupons for
     * @param validityPeriods list of validity periods from update request
     * @param timeZone        timezone for date parsing
     * @param userType        MERCHANT or BANK
     */
    public void updateCouponsFromValidityPeriods(Deal deal, List<ValidityPeriodDto> validityPeriods, 
                                                  String timeZone, UserType userType) {
        if (validityPeriods == null || validityPeriods.isEmpty()) {
            log.debug("No validity periods provided for deal update: {}", deal.getId());
            // If no validity periods provided, delete all existing coupons
            deleteAllCouponsForDeal(deal);
            return;
        }

        if (!deal.isCouponAvailable()) {
            log.warn("Deal {} is not configured for coupons, skipping coupon update", deal.getId());
            return;
        }

        // Only process for MERCHANT deals
        if (userType != UserType.MERCHANT) {
            log.debug("Skipping coupon update for non-MERCHANT deal: {}", deal.getId());
            return;
        }

        try {
            // Get all existing coupons for this deal
            List<Coupon> existingCoupons = couponRepository.findByDealId(deal.getId(), 
                    org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            
            // Group existing coupons by expiryDate (which corresponds to date + endTime)
            Map<Long, List<Coupon>> couponsByExpiryDate = existingCoupons.stream()
                    .collect(Collectors.groupingBy(Coupon::getExpiryDate));
            
            // Build a map of expected expiry dates and counts from new validity periods
            Map<Long, Integer> expectedCouponsByExpiryDate = new HashMap<>();
            List<ValidityPeriodDto> validPeriods = new ArrayList<>();
            
            for (ValidityPeriodDto period : validityPeriods) {
                if (!period.isValid()) {
                    log.warn("Invalid validity period skipped: {}", period);
                    continue;
                }

                // Note: Period start date validation is disabled to match creation behavior
                // This allows updating deals with periods that may have been created before validFrom
                // String startTime = period.getStartTime();
                // if (startTime != null && startTime.matches("\\d{2}:\\d{2}")) {
                //     startTime = startTime + ":00";
                // }
                // String periodStartDateTimeString = period.getDate() + " " + startTime;
                // long periodStartDate = dateParserUtil.parseDateToMilliseconds(periodStartDateTimeString, timeZone);
                // if (periodStartDate < deal.getValidFrom()) {
                //     log.warn("Validity period start date {} is before deal's validFrom {} for period: date={}, startTime={}, endTime={}. Skipping this period.", 
                //             periodStartDate, deal.getValidFrom(), period.getDate(), period.getStartTime(), period.getEndTime());
                //     continue;
                // }

                // Calculate expiry date for this period
                String endTime = period.getEndTime();
                if (endTime != null && endTime.matches("\\d{2}:\\d{2}")) {
                    endTime = endTime + ":00";
                }
                String dateTimeString = period.getDate() + " " + endTime;
                long couponExpiryDate = dateParserUtil.parseDateToMilliseconds(dateTimeString, timeZone);
                
                // Validate coupon expiry is not before deal expiry
                if (couponExpiryDate < deal.getExpiredOn()) {
                    log.warn("Coupon expiry date {} is before deal expiry {} for period: date={}, endTime={}. Using deal expiry instead.", 
                            couponExpiryDate, deal.getExpiredOn(), period.getDate(), period.getEndTime());
                    couponExpiryDate = deal.getExpiredOn();
                }
                
                // If multiple periods map to same expiry date, sum their coupon counts
                int currentCount = expectedCouponsByExpiryDate.getOrDefault(couponExpiryDate, 0);
                expectedCouponsByExpiryDate.put(couponExpiryDate, currentCount + period.getCouponCount());
                
                validPeriods.add(period);
                
                log.debug("Processed validity period: date={}, startTime={}, endTime={}, couponCount={}, expiryDate={}", 
                        period.getDate(), period.getStartTime(), period.getEndTime(), period.getCouponCount(), couponExpiryDate);
            }
            
            int netCouponChange = 0;
            
            // Process each expected expiry date
            for (Map.Entry<Long, Integer> entry : expectedCouponsByExpiryDate.entrySet()) {
                long expiryDate = entry.getKey();
                int expectedCount = entry.getValue();
                List<Coupon> existingCouponsForExpiry = couponsByExpiryDate.getOrDefault(expiryDate, new ArrayList<>());
                int existingCount = existingCouponsForExpiry.size();
                
                if (existingCount == expectedCount) {
                    // Count matches, no changes needed
                    log.debug("Coupon count matches for expiry date {}: {} coupons", expiryDate, existingCount);
                    continue;
                } else if (existingCount > expectedCount) {
                    // Need to delete excess coupons (delete from the end, keeping the oldest ones)
                    int toDelete = existingCount - expectedCount;
                    // Sort by creation time (oldest first) to keep the oldest coupons
                    existingCouponsForExpiry.sort(Comparator.comparing(Coupon::getCreatedAt));
                    List<Coupon> toDeleteList = existingCouponsForExpiry.subList(existingCount - toDelete, existingCount);
                    couponRepository.deleteAll(toDeleteList);
                    netCouponChange -= toDelete;
                    log.info("Deleted {} excess coupons for expiry date {} (had {}, needed {})", 
                            toDelete, expiryDate, existingCount, expectedCount);
                } else {
                    // Need to create more coupons
                    int toCreate = expectedCount - existingCount;
                    
                    // Find periods that match this expiry date
                    List<ValidityPeriodDto> matchingPeriods = validPeriods.stream()
                            .filter(p -> {
                                String et = p.getEndTime();
                                if (et != null && et.matches("\\d{2}:\\d{2}")) {
                                    et = et + ":00";
                                }
                                String dt = p.getDate() + " " + et;
                                long exp = dateParserUtil.parseDateToMilliseconds(dt, timeZone);
                                if (exp < deal.getExpiredOn()) {
                                    exp = deal.getExpiredOn();
                                }
                                return exp == expiryDate;
                            })
                            .collect(Collectors.toList());
                    
                    if (!matchingPeriods.isEmpty()) {
                        // Distribute the coupons to create across matching periods proportionally
                        int totalRequestedForExpiry = matchingPeriods.stream()
                                .mapToInt(ValidityPeriodDto::getCouponCount)
                                .sum();
                        
                        int successfullyCreated = 0;
                        int maxRetriesPerCoupon = 10;
                        
                        // Create coupons proportionally across periods
                        for (ValidityPeriodDto matchingPeriod : matchingPeriods) {
                            // Calculate how many coupons to create for this period
                            double proportion = (double) matchingPeriod.getCouponCount() / totalRequestedForExpiry;
                            int couponsForThisPeriod = (int) Math.round(toCreate * proportion);
                            
                            // Adjust for the last period to ensure we create exactly 'toCreate' total
                            if (matchingPeriod == matchingPeriods.get(matchingPeriods.size() - 1)) {
                                couponsForThisPeriod = toCreate - successfullyCreated;
                            }
                            
                            int createdForPeriod = 0;
                            while (createdForPeriod < couponsForThisPeriod && successfullyCreated < toCreate) {
                                String code = null;
                                Coupon coupon = null;
                                boolean saved = false;
                                int retryAttempt = 0;
                                
                                // Try to save a coupon, retrying with new codes if duplicates are found
                                while (!saved && retryAttempt < maxRetriesPerCoupon) {
                                    try {
                                        // Generate a new unique code for each attempt
                                        code = couponCodeGeneratorService.generateUniqueCode();
                                        coupon = new Coupon(deal, userType, code, null, expiryDate);
                                        
                                        // Attempt to save
                                        couponRepository.save(coupon);
                                        successfullyCreated++;
                                        createdForPeriod++;
                                        netCouponChange++;
                                        saved = true;
                                        
                                        log.debug("Created new coupon {}/{} with code: {} for expiry date: {} (period: {})", 
                                                successfullyCreated, toCreate, code, expiryDate, matchingPeriod.getDate());
                                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                                        // Duplicate code detected - generate a new one and retry
                                        retryAttempt++;
                                        log.warn("Duplicate coupon code detected: {} for deal: {} expiry: {}. Retrying with new code (attempt {}/{})", 
                                                code, deal.getId(), expiryDate, retryAttempt, maxRetriesPerCoupon);
                                    } catch (Exception e) {
                                        // Other errors - log and retry
                                        retryAttempt++;
                                        log.error("Failed to save coupon for deal: {} expiry: {} (attempt {}/{}). Error: {}", 
                                                deal.getId(), expiryDate, retryAttempt, maxRetriesPerCoupon, e.getMessage(), e);
                                    }
                                }
                                
                                // If we couldn't save after max retries, log error but continue to try next coupon
                                if (!saved) {
                                    log.error("Failed to save coupon after {} retries for deal: {} expiry: {}. " +
                                            "This may indicate database issues or code generation problems.", 
                                            maxRetriesPerCoupon, deal.getId(), expiryDate);
                                }
                            }
                        }
                        
                        log.info("Created {}/{} new coupons for expiry date {} (had {}, needed {})", 
                                successfullyCreated, toCreate, expiryDate, existingCount, expectedCount);
                    } else {
                        log.warn("No matching period found for expiry date: {}", expiryDate);
                    }
                }
            }
            
            // Delete coupons for expiry dates not in the new request
            for (Map.Entry<Long, List<Coupon>> entry : couponsByExpiryDate.entrySet()) {
                long expiryDate = entry.getKey();
                if (!expectedCouponsByExpiryDate.containsKey(expiryDate)) {
                    List<Coupon> toDelete = entry.getValue();
                    couponRepository.deleteAll(toDelete);
                    netCouponChange -= toDelete.size();
                    log.info("Deleted {} coupons for expiry date {} (period no longer in request)", 
                            toDelete.size(), expiryDate);
                }
            }
            
            // Update coupon statistics if there was a net change
            if (netCouponChange != 0) {
                updateCouponStatistic(netCouponChange, deal);
                log.info("Updated coupon statistics for deal: {} with net change: {}", deal.getId(), netCouponChange);
            } else {
                log.debug("No net change in coupon count for deal: {}", deal.getId());
            }

        } catch (Exception e) {
            log.error("Failed to update coupons from validity periods for deal: {}", deal.getId(), e);
            throw new KupiCoreException("Failed to update coupons from validity periods for deal: " + deal.getId(), e);
        }
    }
    
    
    /**
     * Delete all coupons for a deal (used when validity periods are removed)
     *
     * @param deal the deal to delete coupons for
     */
    private void deleteAllCouponsForDeal(Deal deal) {
        try {
            List<Coupon> existingCoupons = couponRepository.findByDealId(deal.getId(), 
                    org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            if (!existingCoupons.isEmpty()) {
                int count = existingCoupons.size();
                couponRepository.deleteAll(existingCoupons);
                // Update statistics to reflect deletion
                updateCouponStatistic(-count, deal);
                log.info("Deleted all {} coupons for deal: {}", count, deal.getId());
            }
        } catch (Exception e) {
            log.error("Failed to delete all coupons for deal: {}", deal.getId(), e);
            throw new KupiCoreException("Failed to delete all coupons for deal: " + deal.getId(), e);
        }
    }

    /**
     * Reconstruct validity periods from existing coupons for a deal.
     * Groups coupons by expiry date and reconstructs the validity period information.
     * When multiple periods have the same expiry date (e.g., adjusted to deal.getExpiredOn()),
     * attempts to reconstruct based on coupon creation time grouping.
     *
     * @param deal     the deal to reconstruct validity periods for
     * @param timeZone timezone for date formatting
     * @return list of validity periods, or empty list if no coupons exist
     */
    public List<ValidityPeriodDto> reconstructValidityPeriodsFromCoupons(Deal deal, String timeZone) {
        List<ValidityPeriodDto> validityPeriods = new ArrayList<>();
        
        if (deal == null || !deal.isCouponAvailable()) {
            log.debug("Deal is null or coupons not available, returning empty validity periods");
            return validityPeriods;
        }
        
        try {
            // Get all existing coupons for this deal
            List<Coupon> existingCoupons = couponRepository.findByDealId(deal.getId(), 
                    org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            
            if (existingCoupons.isEmpty()) {
                log.debug("No coupons found for deal: {}, returning empty validity periods", deal.getId());
                return validityPeriods;
            }
            
            // Group coupons by expiryDate
            Map<Long, List<Coupon>> couponsByExpiryDate = existingCoupons.stream()
                    .collect(Collectors.groupingBy(Coupon::getExpiryDate));
            
            // Convert deal's startTime to HH:mm format for use in periods
            String dealStartTime = convertTimeToHHmm(deal.getStartTime());
            
            // Process each expiry date group
            for (Map.Entry<Long, List<Coupon>> entry : couponsByExpiryDate.entrySet()) {
                long expiryDateMillis = entry.getKey();
                List<Coupon> coupons = entry.getValue();
                
                // Convert milliseconds to LocalDateTime in the given timezone
                java.time.ZonedDateTime zonedDateTime = java.time.Instant.ofEpochMilli(expiryDateMillis)
                        .atZone(java.time.ZoneId.of(timeZone));
                
                // Check if expiry date equals deal's expiredOn (meaning it was adjusted)
                boolean isAdjustedToDealExpiry = (expiryDateMillis == deal.getExpiredOn());
                
                if (isAdjustedToDealExpiry) {
                    // When expiry equals deal.getExpiredOn(), coupons may have come from multiple periods
                    // Group by creation time to try to separate them
                    // Coupons created close together likely came from the same period
                    coupons.sort(Comparator.comparing(Coupon::getCreatedAt));
                    
                    // Try to group coupons by creation time clusters (within 5 seconds = same period)
                    List<List<Coupon>> couponGroups = new ArrayList<>();
                    List<Coupon> currentGroup = new ArrayList<>();
                    long lastCreationTime = 0;
                    
                    for (Coupon coupon : coupons) {
                        if (currentGroup.isEmpty() || (coupon.getCreatedAt() - lastCreationTime) < 5000) {
                            // Same group (created within 5 seconds)
                            currentGroup.add(coupon);
                        } else {
                            // New group
                            couponGroups.add(currentGroup);
                            currentGroup = new ArrayList<>();
                            currentGroup.add(coupon);
                        }
                        lastCreationTime = coupon.getCreatedAt();
                    }
                    if (!currentGroup.isEmpty()) {
                        couponGroups.add(currentGroup);
                    }
                    
                    // For each group, try to reverse-calculate the original period
                    // Since we can't know the exact original date, we'll use the deal's expiredOn date
                    // and try to infer from the coupon count and creation pattern
                    for (List<Coupon> group : couponGroups) {
                        int couponCount = group.size();
                        
                        // Use deal's expiredOn date and endTime
                        String date = zonedDateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        String endTime = convertTimeToHHmm(deal.getEndTime());
                        
                        // Use deal's startTime
                        String startTime = dealStartTime;
                        
                        ValidityPeriodDto period = new ValidityPeriodDto(date, startTime, endTime, couponCount);
                        validityPeriods.add(period);
                    }
                } else {
                    // Not adjusted - can reconstruct original date and time from expiryDate
                    String date = zonedDateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String endTime = zonedDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                    String startTime = dealStartTime;
                    int couponCount = coupons.size();
                    
                    ValidityPeriodDto period = new ValidityPeriodDto(date, startTime, endTime, couponCount);
                    validityPeriods.add(period);
                }
            }
            
            // Sort by date and startTime for consistent ordering
            validityPeriods.sort(Comparator
                    .comparing(ValidityPeriodDto::getDate)
                    .thenComparing(ValidityPeriodDto::getStartTime));
            
            log.info("Reconstructed {} validity periods from {} coupons for deal: {}", 
                    validityPeriods.size(), existingCoupons.size(), deal.getId());
            
        } catch (Exception e) {
            log.error("Failed to reconstruct validity periods from coupons for deal: {}", deal.getId(), e);
            // Return empty list on error rather than throwing exception
            return new ArrayList<>();
        }
        
        return validityPeriods;
    }
    
    /**
     * Convert time string from various formats to HH:mm format.
     * Handles formats like "09:00 AM", "18:00 PM", "09:00", etc.
     *
     * @param timeString time string in various formats
     * @return time in HH:mm format, or "00:00" if conversion fails
     */
    private String convertTimeToHHmm(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return "00:00";
        }
        
        String time = timeString.trim();
        
        // If already in HH:mm format, return as-is
        if (time.matches("\\d{2}:\\d{2}")) {
            return time;
        }
        
        // Try to parse formats with AM/PM
        if (time.contains("AM") || time.contains("PM")) {
            try {
                // Remove AM/PM and parse
                String timeWithoutAmPm = time.replace(" AM", "").replace(" PM", "").trim();
                java.time.LocalTime localTime = java.time.LocalTime.parse(
                        timeWithoutAmPm,
                        java.time.format.DateTimeFormatter.ofPattern("hh:mm"));
                
                // Adjust for PM
                if (time.contains("PM") && localTime.getHour() != 12) {
                    localTime = localTime.plusHours(12);
                } else if (time.contains("AM") && localTime.getHour() == 12) {
                    localTime = localTime.minusHours(12);
                }
                
                return localTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception e) {
                // Try regex extraction as fallback
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d{1,2}):(\\d{2})");
                java.util.regex.Matcher matcher = pattern.matcher(time);
                if (matcher.find()) {
                    int hour = Integer.parseInt(matcher.group(1));
                    int minute = Integer.parseInt(matcher.group(2));
                    if (time.contains("PM") && hour != 12) {
                        hour += 12;
                    } else if (time.contains("AM") && hour == 12) {
                        hour = 0;
                    }
                    return String.format("%02d:%02d", hour, minute);
                }
            }
        }
        
        // Default fallback
        return "00:00";
    }
    
    /**
     * This method used to update coupon statistics.
     *
     * @param totalCoupons totalCoupons
     * @param deal         deal
     */
    public void updateCouponStatistic(int totalCoupons, Deal deal) {
        try {
            CouponStatistics couponStatistics;
            var couponStatisticsOptional = couponStatisticsRepository.findByDealId(deal.getId());
            Optional<DealSearchIndex> optionalDealSearchIndex = dealSearchIndexRepository.findById(deal.getId());
            DealSearchIndex dealSearchIndex = optionalDealSearchIndex
                    .orElseThrow(() -> new InvalidDealException("Deal search index not found for id: " + deal.getId()));
            if (couponStatisticsOptional.isPresent()) {
                couponStatistics = couponStatisticsOptional.get();
                couponStatistics.setRemainingCoupons(couponStatistics.getRemainingCoupons() + totalCoupons);
                couponStatistics.setTotalCoupons(couponStatistics.getTotalCoupons() + totalCoupons);
            } else {
                couponStatistics = new CouponStatistics(totalCoupons, deal);
                deal.setCouponStatistics(couponStatistics);
            }
            dealSearchIndex.updateTotalCoupons(totalCoupons);
            dealSearchIndexRepository.save(dealSearchIndex);
            couponStatisticsRepository.save(couponStatistics);
            dealRepository.save(deal);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Coupon Statistic update was failed for deal id: " + deal.getId(), e);
        }
    }

    /**
     * This method is used to get sold coupons by merchantId, dealId & searchTerm.
     *
     * @param merchantId merchantId
     * @param dealId     dealId
     * @param searchTerm searchTerm
     * @param pageable   pageable
     * @return coupon page.
     */
    public Page<Coupon> getSoldCoupons(String merchantId, String dealId, String searchTerm, Pageable pageable) {
        try {
            CouponPurchaseStatus couponPurchaseStatus = CouponPurchaseStatus.PURCHASED;
            MerchantDealSearchType merchantDealSearchType = createMerchantDealQueryType(merchantId, dealId, searchTerm);
            switch (merchantDealSearchType) {
                case ALL_ALL_ALL:
                    return couponRepository.findByCouponPurchaseStatus(couponPurchaseStatus, pageable);
                case MERCHANT_ALL_ALL:
                    return couponRepository.findByDeal_MerchantIdAndCouponPurchaseStatus(merchantId,
                            couponPurchaseStatus, pageable);
                case ALL_DEAL_ALL:
                    return couponRepository.findByCouponPurchaseStatusAndDeal_Id(couponPurchaseStatus, dealId, pageable);
                case MERCHANT_DEAL_ALL:
                    return couponRepository.findByDeal_MerchantIdAndCouponPurchaseStatusAndDeal_Id(merchantId,
                            couponPurchaseStatus, dealId, pageable);
                case ALL_ALL_SEARCH:
                    return couponRepository.findByCouponPurchaseStatusAndCouponCodeContainsIgnoreCase(
                            couponPurchaseStatus, searchTerm, pageable);
                case MERCHANT_ALL_SEARCH:
                    return couponRepository.findByDeal_MerchantIdAndCouponPurchaseStatusAndCouponCodeContainsIgnoreCase(
                            merchantId, couponPurchaseStatus, searchTerm, pageable);
                case ALL_DEAL_SEARCH:
                    return couponRepository.findByCouponPurchaseStatusAndDeal_IdAndCouponCodeContainsIgnoreCase(
                            couponPurchaseStatus, dealId, searchTerm, pageable);
                case MERCHANT_DEAL_SEARCH:
                    return couponRepository
                            .findByDeal_MerchantIdAndCouponPurchaseStatusAndDeal_IdAndCouponCodeContainsIgnoreCase(
                                    merchantId, couponPurchaseStatus, dealId, searchTerm, pageable);
                default:
                    throw new KupiCoreException("Invalid query.");
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Getting sold coupons from db was failed for merchantId: " + merchantId, e);
        }
    }

    /**
     * This method is used to create query type for sold coupon list.
     *
     * @param merchantId merchantId
     * @param dealId     dealId
     * @param searchTerm searchTerm
     * @return query type enum.
     */
    private MerchantDealSearchType createMerchantDealQueryType(String merchantId, String dealId, String searchTerm) {
        merchantId = merchantId.equals(ALL) ? ALL : MERCHANT;
        dealId = dealId.equals(ALL) ? ALL : DEAL;
        searchTerm = searchTerm.equals(ALL) ? ALL : SEARCH;
        return MerchantDealSearchType.valueOf(merchantId + BIND + dealId + BIND + searchTerm);
    }

    /**
     * This method used to get coupon uploaded file name list by deal id.
     *
     * @param dealId dealId
     * @return fileName list
     */
    public List<String> getUploadedCouponFileListByDealId(String dealId) {
        try {
            return couponRepository.getDistinctByFileName(dealId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Getting coupon upload files by deal is: " + dealId + " was failed.", e);
        }
    }

    /**
     * This method used to get successfully uploaded coupons by deal id and fileName.
     *
     * @param dealId   deal id
     * @param fileName file name
     * @return List of uploaded coupons
     */
    public List<String> getUploadSuccessCouponsForFileName(String dealId, String fileName) {
        try {
            return couponRepository.getUploadSuccessCoupon(dealId, fileName);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Getting upload success coupons by deal id " + dealId + " and file name: "
                    + fileName + " was failed.", e);
        }
    }

    /**
     * This method returns true when coupon code exist in database.
     *
     * @param deal       deal
     * @param couponCode coupon code
     * @return true/ false
     */
    public boolean checkCouponCodeAvailableForDeal(Deal deal, String couponCode) {
        try {
            return couponRepository.existsAllByDealAndAndCouponCode(deal, couponCode);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Checking coupon code: " +
                    couponCode + " available for deal id: " + deal.getId() + " was failed", e);
        }
    }
}