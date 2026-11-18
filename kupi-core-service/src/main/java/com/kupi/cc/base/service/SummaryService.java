package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Category;
import com.kupi.cc.base.domain.entity.CategoryBrandMerchant;
import com.kupi.cc.base.domain.entity.Coupon;
import com.kupi.cc.base.domain.entity.CouponStatistics;
import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.domain.entity.DealView;
import com.kupi.cc.base.domain.response.*;
import com.kupi.cc.base.domain.response.dashboard.*;
import com.kupi.cc.base.domain.response.dashboardV2.ActivityLogEntryResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.ActivityLogResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.AdminDashboardSummaryResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.DailyDealDetailResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.DailyEarningResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.DailyUserEngagementResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.DealDetailsTrendsResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.DealPerformanceListResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.DealPerformanceResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.EarningTrendsResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.MerchantSummaryResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.TopPerformingDealResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.TopPerformingDealsResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.TotalSavingsResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.CombinedTotalSavingsResponseDto;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.domain.response.dashboardV2.UserEngagementTrendsResponseDto;
import com.kupi.cc.base.enums.CategoryType;
import com.kupi.cc.base.enums.DeductionType;
import com.kupi.cc.base.enums.SubscriptionStatus;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.ocpsoft.prettytime.PrettyTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Summary service
 */
@Service
@lombok.extern.slf4j.Slf4j
public class SummaryService {

    private static final int ZERO = 0;
    private static final int MAX_AUTH_SERVICE_PAGE_SIZE = 250;
    private final DealRepository dealRepository;
    private final RequestADealRepository requestADealRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final DealSearchIndexRepository dealSearchIndexRepository;
    private final DealService dealService;
    private final CouponStatisticsService couponStatisticsService;
    private final AuthUserService authUserService;
    private final CategoryBrandMerchantRepository categoryBrandMerchantRepository;
    private final CreditCardRepository creditCardRepository;
    private final BankDealRepository bankDealRepository;
    private final SubscriberRepository subscriberRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final DealsOfTheDayEventRepository dealsOfTheDayEventRepository;
    private final CouponRepository couponRepository;
    private final DealViewService dealViewService;

    @Value("${activity-log.days-to-show:30}")
    private int activityLogDaysToShow;

    @Autowired
    public SummaryService(DealRepository dealRepository, RequestADealRepository requestADealRepository,
                          BrandRepository brandRepository, CategoryRepository categoryRepository,
                          DealSearchIndexRepository dealSearchIndexRepository, DealService dealService,
                          CouponStatisticsService couponStatisticsService, AuthUserService authUserService,
                          CategoryBrandMerchantRepository categoryBrandMerchantRepository,
                          CreditCardRepository creditCardRepository, BankDealRepository bankDealRepository,
                          SubscriberRepository subscriberRepository,
                          SubscriptionPlanRepository subscriptionPlanRepository,
                          DealsOfTheDayEventRepository dealsOfTheDayEventRepository,
                          CouponRepository couponRepository,
                          DealViewService dealViewService) {
        this.dealRepository = dealRepository;
        this.requestADealRepository = requestADealRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.dealSearchIndexRepository = dealSearchIndexRepository;
        this.dealService = dealService;
        this.couponStatisticsService = couponStatisticsService;
        this.authUserService = authUserService;
        this.categoryBrandMerchantRepository = categoryBrandMerchantRepository;
        this.creditCardRepository = creditCardRepository;
        this.bankDealRepository = bankDealRepository;
        this.subscriberRepository = subscriberRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.dealsOfTheDayEventRepository = dealsOfTheDayEventRepository;
        this.couponRepository = couponRepository;
        this.dealViewService = dealViewService;
    }

    /**
     * This method is used to get full summary of Admin/Merchant.
     *
     * @param timeZone  timeZone
     * @param userType  Admin/Merchant
     * @param userId    userId
     * @param authToken authToken
     * @return full summary.
     */
    public FullSummaryResponseDto getFullSummary(String timeZone, UserType userType, String userId, String authToken) {
        var todaySummaryResponseDto = getTodaySummary(timeZone, userType, userId, authToken);
        var totalSummaryResponseDto = getTotalSummary(timeZone, userType, userId, authToken);
        return new FullSummaryResponseDto(todaySummaryResponseDto, totalSummaryResponseDto);
    }

    public AdminDashboardSummaryResponseDto getAdminSummary(String userId, String authToken, UserType userType, String timeZone) {
        try {
            AuthSummaryResponseDto todayAuthSummaryResponseDto = authUserService
                    .getTodaySummaryResponse(userId, authToken, userType);
            ZoneId zone = ZoneId.of(timeZone);
            long nowMillis = ZonedDateTime.now(zone).toInstant().toEpochMilli();
            DealCountsResponseDto totalDealCountsResponseDto = getTotalDeal(nowMillis, userType, userId);
            long totalRedeemCouponCount = getAllRedeemedCouponCount();
            return new AdminDashboardSummaryResponseDto(todayAuthSummaryResponseDto, totalDealCountsResponseDto, totalRedeemCouponCount);

        } catch (DataAccessException e) {
            throw new KupiCoreException("Fetching admin's dashboard values failed.", e);
        }
    }

    public MerchantSummaryResponseDto getMerchantSummary(String merchantId, long startMillis, long endMillis, String timeZone) {
        int activeDeals = getTotalActiveDealsCount(merchantId, startMillis, endMillis);
        int dealsOfTheDayCount = getDealsOfTheDayCount(merchantId, startMillis, endMillis, timeZone);
        int couponCount = getCouponCount(merchantId, startMillis, endMillis);
        int redeemedCouponCount = getRedeemedCouponCount(merchantId, startMillis, endMillis);
        double totalEarnings = getTotalEarnings(merchantId, startMillis, endMillis);
        BigDecimal totalEarningsRounded = BigDecimal
                .valueOf(totalEarnings)
                .setScale(2, RoundingMode.HALF_UP);
        return new MerchantSummaryResponseDto(activeDeals, dealsOfTheDayCount, couponCount, redeemedCouponCount, totalEarningsRounded);
    }

    /**
     * Gets day-by-day earnings for a merchant within a date range for chart display.
     *
     * @param merchantId merchant ID
     * @param startMillis start of date range (epoch millis)
     * @param endMillis end of date range (epoch millis)
     * @param timeZone time zone for date calculations
     * @return EarningTrendsResponseDto with daily earnings data
     */
    public EarningTrendsResponseDto getEarningTrends(String merchantId, long startMillis, long endMillis, String timeZone) {
        try {
            ZoneId zone = ZoneId.of(timeZone);
            List<Coupon> redeemedCoupons = couponRepository
                    .findRedeemedCouponsByMerchantIdAndDateRange(merchantId, startMillis, endMillis);

            Map<LocalDate, List<Coupon>> couponsByDay = redeemedCoupons.stream()
                    .filter(coupon -> coupon.getRedeemedOn() != null)
                    .collect(Collectors.groupingBy(coupon -> {
                        long redeemedOn = coupon.getRedeemedOn();
                        return Instant.ofEpochMilli(redeemedOn).atZone(zone).toLocalDate();
                    }));

            List<DailyEarningResponseDto> dailyEarnings = couponsByDay.entrySet().stream()
                    .map(entry -> {
                        LocalDate date = entry.getKey();
                        List<Coupon> dayCoupons = entry.getValue();
                        
                        double dayTotalEarnings = dayCoupons.stream()
                                .filter(coupon -> coupon.getDeal() != null)
                                .filter(coupon -> isValidPrice(coupon.getDeal().getOriginalPrice()))
                                .mapToDouble(coupon -> calculateEarningsPerCoupon(coupon.getDeal()))
                                .filter(earnings -> earnings > 0)
                                .sum();
                        
                        BigDecimal earningsRounded = BigDecimal.valueOf(dayTotalEarnings)
                                .setScale(2, RoundingMode.HALF_UP);
                        
                        return new DailyEarningResponseDto(date, earningsRounded);
                    })
                    .sorted(Comparator.comparing(DailyEarningResponseDto::getDate))
                    .collect(Collectors.toList());

            LocalDate startDate = Instant.ofEpochMilli(startMillis).atZone(zone).toLocalDate();
            LocalDate endDate = Instant.ofEpochMilli(endMillis).atZone(zone).toLocalDate();
            List<DailyEarningResponseDto> completeDailyEarnings = new ArrayList<>();
            
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                LocalDate finalCurrentDate = currentDate;
                Optional<DailyEarningResponseDto> existingDay = dailyEarnings.stream()
                        .filter(d -> d.getDate().equals(finalCurrentDate))
                        .findFirst();
                
                if (existingDay.isPresent()) {
                    completeDailyEarnings.add(existingDay.get());
                } else {
                    completeDailyEarnings.add(new DailyEarningResponseDto(currentDate, BigDecimal.ZERO.setScale(2)));
                }
                
                currentDate = currentDate.plusDays(1);
            }

            log.info("Earning trends calculated for merchantId={} between {} and {}. Days with data: {}",
                    merchantId, startMillis, endMillis, dailyEarnings.size());

            return new EarningTrendsResponseDto(completeDailyEarnings);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Fetching earning trends failed.", e);
        }
    }

    /**
     * Gets day-by-day sold vs redeemed coupon counts for a merchant within a date range
     * for the Deal Details chart.
     *
     * @param merchantId merchant ID
     * @param startMillis start of date range (epoch millis)
     * @param endMillis end of date range (epoch millis)
     * @param timeZone time zone for date calculations
     * @return DealDetailsTrendsResponseDto with daily sold and redeemed counts
     */
    public DealDetailsTrendsResponseDto getDealDetailsTrends(String merchantId, long startMillis, long endMillis, String timeZone) {
        try {
            ZoneId zone = ZoneId.of(timeZone);

            // Purchased (sold) coupons
            List<Coupon> purchasedCoupons = couponRepository
                    .findPurchasedCouponsByMerchantIdAndDateRange(merchantId, startMillis, endMillis);

            // Redeemed coupons
            List<Coupon> redeemedCoupons = couponRepository
                    .findRedeemedCouponsByMerchantIdAndDateRange(merchantId, startMillis, endMillis);

            Map<LocalDate, Long> purchasedByDay = purchasedCoupons.stream()
                    .filter(coupon -> coupon.getPurchasedOn() != null)
                    .collect(Collectors.groupingBy(coupon ->
                                    Instant.ofEpochMilli(coupon.getPurchasedOn()).atZone(zone).toLocalDate(),
                            Collectors.counting()));

            Map<LocalDate, Long> redeemedByDay = redeemedCoupons.stream()
                    .filter(coupon -> coupon.getRedeemedOn() != null)
                    .collect(Collectors.groupingBy(coupon ->
                                    Instant.ofEpochMilli(coupon.getRedeemedOn()).atZone(zone).toLocalDate(),
                            Collectors.counting()));

            LocalDate startDate = Instant.ofEpochMilli(startMillis).atZone(zone).toLocalDate();
            LocalDate endDate = Instant.ofEpochMilli(endMillis).atZone(zone).toLocalDate();

            List<DailyDealDetailResponseDto> dailyDetails = new ArrayList<>();

            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                int soldCount = purchasedByDay.getOrDefault(currentDate, 0L).intValue();
                int redeemedCount = redeemedByDay.getOrDefault(currentDate, 0L).intValue();

                dailyDetails.add(new DailyDealDetailResponseDto(currentDate, soldCount, redeemedCount));
                currentDate = currentDate.plusDays(1);
            }

            log.info("Deal details trends calculated for merchantId={} between {} and {}. Days={} ",
                    merchantId, startMillis, endMillis, dailyDetails.size());

            return new DealDetailsTrendsResponseDto(dailyDetails);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Fetching deal details trends failed.", e);
        }
    }

    /**
     * Gets top performing deals ranked by total revenue from redemptions for a merchant
     * within a date range.
     *
     * @param merchantId merchant ID
     * @param startMillis start of date range (epoch millis)
     * @param endMillis end of date range (epoch millis)
     * @param limit maximum number of deals to return (default: 10)
     * @return TopPerformingDealsResponseDto with ranked deals
     */
    public TopPerformingDealsResponseDto getTopPerformingDeals(String merchantId, long startMillis, long endMillis, int limit) {
        try {
            List<Coupon> redeemedCoupons = couponRepository
                    .findRedeemedCouponsByMerchantIdAndDateRange(merchantId, startMillis, endMillis);

            long currentTime = System.currentTimeMillis();

            // Group coupons by deal and calculate revenue per deal
            Map<Deal, List<Coupon>> couponsByDeal = redeemedCoupons.stream()
                    .filter(coupon -> coupon.getDeal() != null)
                    .filter(coupon -> isActiveDeal(coupon.getDeal(), currentTime))
                    .filter(coupon -> isValidPrice(coupon.getDeal().getOriginalPrice()))
                    .collect(Collectors.groupingBy(Coupon::getDeal));

            // Calculate revenue and redemption count for each deal
            List<TopPerformingDealResponseDto> topDeals = couponsByDeal.entrySet().stream()
                    .map(entry -> {
                        Deal deal = entry.getKey();
                        List<Coupon> dealCoupons = entry.getValue();

                        // Calculate total revenue for this deal
                        double totalRevenue = dealCoupons.stream()
                                .mapToDouble(coupon -> calculateEarningsPerCoupon(deal))
                                .filter(earnings -> earnings > 0)
                                .sum();

                        // Get redemption count
                        int redemptionCount = dealCoupons.size();

                        // Get first category name
                        String categoryName = deal.getRelatedCategories().stream()
                                .findFirst()
                                .map(Category::getName)
                                .orElse("");

                        BigDecimal revenueRounded = BigDecimal.valueOf(totalRevenue)
                                .setScale(2, RoundingMode.HALF_UP);

                        return new TopPerformingDealResponseDto(
                                0,
                                deal.getId(),
                                deal.getTitle(),
                                categoryName,
                                revenueRounded,
                                redemptionCount
                        );
                    })
                    .filter(deal -> deal.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0)
                    .sorted((d1, d2) -> d2.getTotalRevenue().compareTo(d1.getTotalRevenue()))
                    .limit(limit)
                    .collect(Collectors.toList());

            // Set ranks
            for (int i = 0; i < topDeals.size(); i++) {
                topDeals.get(i).setRank(i + 1);
            }

            log.info("Top performing deals calculated for merchantId={} between {} and {}. Top {} deals returned.",
                    merchantId, startMillis, endMillis, topDeals.size());

            return new TopPerformingDealsResponseDto(topDeals);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Fetching top performing deals failed.", e);
        }
    }
    /**
     * Gets day-by-day user engagement (views vs sales) for a merchant within a date range
     * for the User Engagement chart.
     *
     * @param merchantId  merchant ID
     * @param startMillis start of date range (epoch millis)
     * @param endMillis   end of date range (epoch millis)
     * @param timeZone    time zone for date calculations
     * @return UserEngagementTrendsResponseDto with daily views and sales counts
     */
    public UserEngagementTrendsResponseDto getUserEngagementTrends(String merchantId,
                                                                   String dealId,
                                                                   long startMillis,
                                                                   long endMillis,
                                                                   String timeZone) {
        try {
            ZoneId zone = ZoneId.of(timeZone);

            java.util.List<DealView> dealViews = (dealId == null)
                    ? dealViewService.getMerchantDealViewsByTimeRange(merchantId, startMillis, endMillis)
                    : dealViewService.getDealViewsByTimeRange(dealId, startMillis, endMillis);
            Map<LocalDate, Long> viewsByDay = dealViews.stream()
                    .collect(Collectors.groupingBy(
                            view -> Instant.ofEpochMilli(view.getViewedAt()).atZone(zone).toLocalDate(),
                            Collectors.counting()));

            List<Coupon> purchasedCoupons = (dealId == null)
                    ? couponRepository.findPurchasedCouponsByMerchantIdAndDateRange(merchantId, startMillis, endMillis)
                    : couponRepository.findPurchasedCouponsByDealIdAndDateRange(dealId, startMillis, endMillis);
            Map<LocalDate, Long> salesByDay = purchasedCoupons.stream()
                    .filter(coupon -> coupon.getPurchasedOn() != null)
                    .collect(Collectors.groupingBy(
                            coupon -> Instant.ofEpochMilli(coupon.getPurchasedOn()).atZone(zone).toLocalDate(),
                            Collectors.counting()));

            LocalDate startDate = Instant.ofEpochMilli(startMillis).atZone(zone).toLocalDate();
            LocalDate endDate = Instant.ofEpochMilli(endMillis).atZone(zone).toLocalDate();

            List<DailyUserEngagementResponseDto> dailyEngagement = new ArrayList<>();
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                long viewCount = viewsByDay.getOrDefault(currentDate, 0L);
                long salesCount = salesByDay.getOrDefault(currentDate, 0L);
                dailyEngagement.add(new DailyUserEngagementResponseDto(currentDate, viewCount, salesCount));
                currentDate = currentDate.plusDays(1);
            }

            return new UserEngagementTrendsResponseDto(dailyEngagement);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Fetching user engagement trends failed.", e);
        }
    }

    /**
     * Gets deal performance metrics for all merchant deals within a date range.
     * Returns paginated list with views, sold, redemptions, conversion rate, and earnings per deal.
     *
     * @param merchantId merchant ID
     * @param startMillis start of date range (epoch millis)
     * @param endMillis end of date range (epoch millis)
     * @param timeZone time zone for date calculations
     * @param pageable pagination parameters
     * @return DealPerformanceListResponseDto with paginated deal performance data
     */
    public DealPerformanceListResponseDto getDealPerformance(String merchantId,
                                                           long startMillis,
                                                           long endMillis,
                                                           String timeZone,
                                                           Pageable pageable,
                                                           String searchTerm) {
        try {
            ZoneId zone = ZoneId.of(timeZone);

            // Get deals created in the date range (optionally filtered by search term)
            Page<Deal> dealsPage;
            if (searchTerm == null || searchTerm.isBlank() || "ALL".equalsIgnoreCase(searchTerm.trim())) {
                dealsPage = dealRepository.findDealsByMerchantIdAndCreatedDateRange(
                        pageable, merchantId, startMillis, endMillis);
            } else {
                dealsPage = dealRepository.findDealsByMerchantIdAndCreatedDateRangeAndSearchTerm(
                        pageable, merchantId, startMillis, endMillis, searchTerm.trim());
            }

            List<DealPerformanceResponseDto> dealPerformanceList = dealsPage.getContent().stream()
                    .map(deal -> {
                        String dealId = deal.getId();
                        LocalDate createdDate = Instant.ofEpochMilli(deal.getCreatedAt())
                                .atZone(zone).toLocalDate();

                        long views = dealViewService.getDealViewCountByTimeRange(dealId, startMillis, endMillis);

                        long sold = couponRepository.countPurchasedCouponsByDealIdAndDateRange(
                                dealId, startMillis, endMillis);

                        long redemptions = couponRepository.countRedeemedCouponsByDealIdAndDateRange(
                                dealId, startMillis, endMillis);

                        // Conversion rate: (Sold / Views) * 100
                        double conversionRate = views > 0 ? (sold * 100.0 / views) : 0.0;
                        conversionRate = Math.round(conversionRate * 10.0) / 10.0; // Round to 1 decimal

                        // Earnings: (originalPrice - deduction) * redeemedCount
                        BigDecimal earnings = BigDecimal.ZERO;
                        if (redemptions > 0 && isValidPrice(deal.getOriginalPrice())) {
                            double earningsPerCoupon = calculateEarningsPerCoupon(deal);
                            double totalEarnings = earningsPerCoupon * redemptions;
                            earnings = BigDecimal.valueOf(totalEarnings)
                                    .setScale(2, RoundingMode.HALF_UP);
                        }

                        return new DealPerformanceResponseDto(
                                dealId,
                                createdDate,
                                deal.getTitle(),
                                views,
                                sold,
                                redemptions,
                                conversionRate,
                                earnings
                        );
                    })
                    .collect(Collectors.toList());

            // Create paginated response
            Page<DealPerformanceResponseDto> performancePage = new PageImpl<>(
                    dealPerformanceList,
                    pageable,
                    dealsPage.getTotalElements()
            );

            return new DealPerformanceListResponseDto(performancePage);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Fetching deal performance failed.", e);
        }
    }


    private int getTotalActiveDealsCount(String merchantId, long startMillis, long endMillis) {
        try {
            long now = System.currentTimeMillis();

            long todayStartMillis = LocalDate.now()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant().toEpochMilli();
            if (endMillis < todayStartMillis) {
                return -1;
            }
            if (startMillis > now) {
                List<DealSearchIndex> activeDealsInRange = dealSearchIndexRepository
                        .findActiveDealsByMerchantIdAndDateRange(merchantId, startMillis, endMillis);
                return getCouponFilteredActiveDealCount(activeDealsInRange);
            }
            Page<DealSearchIndex> activeDealsNowPage = dealSearchIndexRepository
                    .getActiveMerchantDeals(Pageable.unpaged(), merchantId, now);
            return getCouponFilteredActiveDealCount(activeDealsNowPage.getContent());

        } catch (DataAccessException e) {
            throw new KupiCoreException("Fetching active deal count failed.", e);
        }
    }

    /**
     * Get current active deal count for a merchant.
     * A deal is considered active if:
     * - It is approved and within its validFrom/expiredOn range, and
     * - It passes coupon availability filtering (same as deal pages).
     *
     * @param merchantId merchant id
     * @return active deal count at current time
     */
    public int getCurrentActiveDealsCountForMerchant(String merchantId) {
        try {
            long now = System.currentTimeMillis();
            Page<DealSearchIndex> activeDealsNowPage =
                    dealSearchIndexRepository.getActiveMerchantDeals(Pageable.unpaged(), merchantId, now);
            return getCouponFilteredActiveDealCount(activeDealsNowPage.getContent());
        } catch (DataAccessException e) {
            throw new KupiCoreException("Fetching current active deal count failed.", e);
        }
    }

    /**
     * Get current active deal count for a merchant within a specific category.
     * A deal is considered active if:
     * - It is approved and within its validFrom/expiredOn range, and
     * - It passes coupon availability filtering (same as deal pages).
     *
     * @param merchantId merchant id
     * @param categoryId category id
     * @return active deal count at current time for the given category
     */
    public int getCurrentActiveDealsCountForMerchantAndCategory(String merchantId, String categoryId) {
        try {
            long now = System.currentTimeMillis();
            Page<DealSearchIndex> activeDealsNowPage =
                    dealSearchIndexRepository.findAllActiveDealsByActiveMerchantIdAndCategoryId(
                            Pageable.unpaged(), merchantId, categoryId, now, UserType.MERCHANT);
            return getCouponFilteredActiveDealCount(activeDealsNowPage.getContent());
        } catch (DataAccessException e) {
            throw new KupiCoreException("Fetching current active deal count by category failed.", e);
        }
    }

    /**
     * Applies the same coupon-based availability logic used in DealController (via
     * DealService.removeCouponNotAvailableDeals) and returns the resulting active deal count.
     * A deal is considered active if:
     * - It does not have coupons (couponAvailable = false), or
     * - It has coupons and there is corresponding CouponStatistics with totalCoupons > 0.
     */
    private int getCouponFilteredActiveDealCount(List<DealSearchIndex> dealSearchIndices) {
        if (dealSearchIndices == null || dealSearchIndices.isEmpty()) {
            return 0;
        }

        List<String> dealIds = dealSearchIndices.stream()
                .map(DealSearchIndex::getId)
                .collect(Collectors.toList());

        List<CouponStatistics> couponStatisticsList = couponStatisticsService
                .getCouponStatisticsByDealIdList(dealIds);

        Page<DealSearchIndex> dealPage = new PageImpl<>(dealSearchIndices);
        Page<DealSearchIndex> filteredDeals =
                dealService.removeCouponNotAvailableDeals(dealPage, couponStatisticsList);

        return filteredDeals.getNumberOfElements();
    }

    private int getDealsOfTheDayCount(String userId, long startMillis, long endMillis, String timeZone) {

        try {
            ZoneId zone = ZoneId.of(timeZone);

            LocalDate startLocal = Instant.ofEpochMilli(startMillis).atZone(zone).toLocalDate();
            LocalDate endLocal = Instant.ofEpochMilli(endMillis).atZone(zone).toLocalDate();

            java.sql.Date startDate = java.sql.Date.valueOf(startLocal);
            java.sql.Date endDate = java.sql.Date.valueOf(endLocal);


            List<Integer> counts = dealsOfTheDayEventRepository
                    .getDealsOfTheDayCountsInRange(userId, startDate, endDate);
            return counts.stream().mapToInt(Integer::intValue).sum();
        } catch (DataAccessException e) {
            throw new KupiCoreException("Fetching active deal count failed.", e);
        }
    }


    private int getCouponCount(String merchantId, long startMillis, long endMillis) {
        try {
            long count = couponRepository.countActiveCouponsOverlappingRange(merchantId, startMillis, endMillis);
            return Math.toIntExact(count);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Fetching coupon count failed.", e);
        }
    }

    private int getRedeemedCouponCount(String merchantId,
                                       long startMillis,
                                       long endMillis) {
        try {
            long count = couponRepository
                    .countRedeemedCouponsByMerchantIdAndDateRange(merchantId, startMillis, endMillis);
            return Math.toIntExact(count);

        } catch (DataAccessException e) {
            throw new KupiCoreException("Fetching redeemed coupon count failed.", e);
        }
    }

    /**
     * Calculates total earnings from redeemed coupons for a merchant within a date range.
     * Earnings = sum of (redeemed coupons count for each deal * (deal original price - deduction amount))
     *
     * @param merchantId merchant ID
     * @param startMillis start of date range (epoch millis)
     * @param endMillis end of date range (epoch millis)
     * @return total earnings as double
     */
    private double getTotalEarnings(String merchantId, long startMillis, long endMillis) {
        try {
            List<Coupon> redeemedCoupons = couponRepository
                    .findRedeemedCouponsByMerchantIdAndDateRange(merchantId, startMillis, endMillis);

            if (redeemedCoupons == null || redeemedCoupons.isEmpty()) {
                log.debug("No redeemed coupons found for merchantId={} between {} and {}", merchantId, startMillis, endMillis);
                return 0.0;
            }

            log.debug("Calculating total earnings for merchantId={} between {} and {}. Redeemed coupons count={}",
                    merchantId, startMillis, endMillis, redeemedCoupons.size());

            double totalEarnings = redeemedCoupons.stream()
                    .filter(coupon -> {
                        boolean hasDeal = coupon.getDeal() != null;
                        if (!hasDeal) {
                            log.debug("Skipping coupon without deal. couponId={}", coupon.getId());
                        }
                        return hasDeal;
                    })
                    .filter(coupon -> {
                        Double originalPrice = coupon.getDeal().getOriginalPrice();
                        boolean validPrice = isValidPrice(originalPrice);
                        if (!validPrice) {
                            log.debug("Skipping coupon with invalid price. couponId={}, dealId={}, originalPrice={}",
                                    coupon.getId(), coupon.getDeal().getId(), originalPrice);
                        }
                        return validPrice;
                    })
                    .mapToDouble(coupon -> {
                        Deal deal = coupon.getDeal();
                        double earnings = calculateEarningsPerCoupon(deal);
                        Double originalPrice = deal.getOriginalPrice();
                        double deductionAmount = originalPrice != null ? originalPrice - earnings : 0.0;

                        log.debug(
                                "Calculated earnings per coupon. couponId={}, dealId={}, originalPrice={}, deductionAmount={}, earnings={}",
                                coupon.getId(), deal.getId(), originalPrice, deductionAmount, earnings);
                        return earnings;
                    })
                    .filter(earnings -> earnings > 0)
                    .sum();

            log.info("Total earnings calculated for merchantId={} between {} and {} is {}",
                    merchantId, startMillis, endMillis, totalEarnings);

            return totalEarnings;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Fetching total earnings failed.", e);
        }
    }

    /**
     * Checks if the original price is valid (not null and greater than 0).
     *
     * @param originalPrice original price to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidPrice(Double originalPrice) {
        return originalPrice != null && originalPrice > 0;
    }

    /**
     * Checks if a deal is currently active (not deleted, not expired, and approved).
     *
     * @param deal deal entity to check
     * @param currentTime current time in milliseconds
     * @return true if deal is active, false otherwise
     */
    private boolean isActiveDeal(Deal deal, long currentTime) {
        if (deal == null) {
            return false;
        }
        return !deal.isDeleted()
                && deal.getApprovalStatus() == ApprovalStatus.APPROVED
                && deal.getValidFrom() <= currentTime
                && deal.getExpiredOn() >= currentTime;
    }

    /**
     * Calculates earnings per coupon based on deal's original price and deduction.
     *
     * @param deal deal entity
     * @return earnings per coupon
     */
    private double calculateEarningsPerCoupon(Deal deal) {
        Double originalPrice = deal.getOriginalPrice();
        double deductionAmount = calculateDeductionAmount(deal, originalPrice);
        return originalPrice - deductionAmount;
    }

    /**
     * Calculates deduction amount based on deal's deduction type.
     *
     * @param deal deal entity
     * @param originalPrice original price of the deal
     * @return deduction amount
     */
    private double calculateDeductionAmount(Deal deal, Double originalPrice) {
        DeductionType deductionType = deal.getDeductionType();

        if (deductionType == DeductionType.AMOUNT) {
            Double deduction = deal.getDeductionAmount();
            return deduction != null ? deduction : 0.0;
        }

        if (deductionType == DeductionType.PERCENTAGE) {
            Double deductionPercentage = deal.getDeductionPercentage();
            if (deductionPercentage != null && deductionPercentage > 0) {
                return originalPrice * (deductionPercentage / 100.0);
            }
        }

        return 0.0;
    }

    /**
     * Calculates deduction amount for a deal search index (same logic as Deal).
     */
    private double calculateDeductionAmount(DealSearchIndex deal, Double originalPrice) {
        DeductionType deductionType = deal.getDeductionType();
        if (deductionType == DeductionType.AMOUNT) {
            Double deduction = deal.getDeductionAmount();
            return deduction != null ? deduction : 0.0;
        }
        if (deductionType == DeductionType.PERCENTAGE) {
            Double deductionPercentage = deal.getDeductionPercentage();
            if (deductionPercentage != null && deductionPercentage > 0) {
                return originalPrice * (deductionPercentage / 100.0);
            }
        }
        return 0.0;
    }

    /**
     * This method is used to get all redeemed coupon count across all merchants.
     *
     * @return total redeemed coupon count
     */
    public int getAllRedeemedCouponCount() {
        try {
            long count = couponRepository.countAllRedeemedCoupons();
            return Math.toIntExact(count);

        } catch (DataAccessException e) {
            throw new KupiCoreException("Fetching all redeemed coupon count failed.", e);
        }
    }

    public MerchantTableListResponseDto getMerchantAndDealCount(String searchTerm,
                                                                int page, int size, String userId, String timeZone) {

        try {
            ZoneId zone = ZoneId.of(timeZone);
            long nowMillis = ZonedDateTime.now(zone).toInstant().toEpochMilli();
            AllMerchantListResponseDto allMerchants = getAllMerchants(searchTerm, page, size, userId);
            if (allMerchants.getMerchants() == null || allMerchants.getMerchants().isEmpty()) {
                return new MerchantTableListResponseDto(allMerchants, List.of());
            }

            List<MerchantTableResponseDto> tableRows = allMerchants.getMerchants().stream()
                    .map(merchant -> {
                        String merchantId = merchant.getProfile().getId();
                        long dealCount = dealRepository.countByMerchantIdAndIsDeletedFalse(merchantId);

                        Page<DealSearchIndex> activeDealsNowPage = dealSearchIndexRepository
                                .getActiveMerchantDeals(Pageable.unpaged(), merchantId, nowMillis);
                        int totalActiveDeals = activeDealsNowPage != null
                                ? getCouponFilteredActiveDealCount(activeDealsNowPage.getContent())
                                : 0;

                        return new MerchantTableResponseDto(merchant, (int) dealCount, totalActiveDeals);
                    }).collect(Collectors.toList());

            return new MerchantTableListResponseDto(allMerchants, tableRows);
        } catch (KupiCoreException e) {
            throw new KupiCoreException("Retrieving merchants and deals count was failed.", e);
        }
    }

    /**
     * This method gets all merchants from auth service with pagination.
     * Since auth service has a maximum page size of 250, this method makes multiple calls
     * if needed to fetch all merchants, then applies pagination to the combined results.
     *
     * @param searchTerm search term for filtering merchants
     * @param page       requested page number (0-based)
     * @param size       requested page size
     * @param userId     userId for authentication
     * @return AllMerchantListResponseDto with paginated merchant list
     */
    private AllMerchantListResponseDto getAllMerchants(String searchTerm, int page, int size, String userId) {
        try {
            List<AuthMerchantListResponseDto> allMerchants = new ArrayList<>();
            int authServicePage = 0;
            boolean hasMorePages = true;

            while (hasMorePages) {
                AllMerchantListResponseDto allMerchantListResponseDto = authUserService.getAllMerchants(
                        userId, authServicePage, MAX_AUTH_SERVICE_PAGE_SIZE,
                        searchTerm != null ? searchTerm : "");

                List<AuthMerchantListResponseDto> merchants = allMerchantListResponseDto.getMerchants();
                if (merchants != null && !merchants.isEmpty()) {
                    allMerchants.addAll(merchants);
                    hasMorePages = merchants.size() >= MAX_AUTH_SERVICE_PAGE_SIZE;
                } else {
                    hasMorePages = false;
                }
                authServicePage++;
            }
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, allMerchants.size());
            List<AuthMerchantListResponseDto> paginatedMerchants;

            if (startIndex >= allMerchants.size()) {
                paginatedMerchants = new ArrayList<>();
            } else {
                paginatedMerchants = allMerchants.subList(startIndex, endIndex);
            }

            return new AllMerchantListResponseDto(allMerchants.size(), page, size, paginatedMerchants);
        } catch (KupiCoreException e) {
            throw new KupiCoreException("Retrieving  merchants details was failed.", e);
        }
    }

    /**
     * This method is used to get today's summary.
     *
     * @param timeZone  timeZone
     * @param userType  roleType
     * @param userId    userId
     * @param authToken authToken
     * @return TodaySummaryResponseDto
     */
    private TodaySummaryResponseDto getTodaySummary(String timeZone, UserType userType, String userId, String authToken) {
        LocalDate today = LocalDate.now(ZoneId.of(timeZone));
        long todayInMillis = today.atStartOfDay(ZoneId.of(timeZone)).toInstant().toEpochMilli();
        long tomorrowInMillis = today.plusDays(1).atStartOfDay(ZoneId.of(timeZone)).toInstant().toEpochMilli();
        AuthSummaryResponseDto authSummaryResponseDto = authUserService.getTodaySummaryResponse(userId, authToken,
                userType);
        SubscriptionCountsResponseDto todaySubscriptionCountsResponseDto = getTodaySubscriptions(todayInMillis);
        DealCountsResponseDto todayDealCountsResponseDto = getTodayDealSummary(todayInMillis, tomorrowInMillis, userType,
                userId);
        UserCountsResponseDto todayUserCountsResponseDto = new UserCountsResponseDto();
        todayUserCountsResponseDto.getTodayUserCountsForAdminOrBank(authSummaryResponseDto, userType);

        var category = getTodayCategory(todayInMillis, userType, userId);
        return new TodaySummaryResponseDto(timeZone, userType,
                todayUserCountsResponseDto, todaySubscriptionCountsResponseDto, todayDealCountsResponseDto, category);
    }

    /**
     * This method is used to get total summary.
     *
     * @param timeZone  timeZone
     * @param userType  userType
     * @param userId    userId
     * @param authToken authToken
     * @return TotalSummaryResponseDto
     */
    private TotalSummaryResponseDto getTotalSummary(String timeZone, UserType userType, String userId, String authToken) {
        LocalDate today = LocalDate.now(ZoneId.of(timeZone));
        long todayInMillis = today.atStartOfDay(ZoneId.of(timeZone)).toInstant().toEpochMilli();
        long currentTimeInMillis = System.currentTimeMillis();
        BusinessMerchantResponseDto businessProfile;
        AuthSummaryResponseDto todayAuthSummaryResponseDto = authUserService.getTodaySummaryResponse(userId, authToken,
                userType);
        SubscriptionCountsResponseDto totalSubscriptionCountsResponseDto = getTotalSubscriptions();
        DealCountsResponseDto totalDealCountsResponseDto = getTotalDeal(todayInMillis, userType, userId);
        UserCountsResponseDto totalUserCountsResponseDto = new UserCountsResponseDto(todayAuthSummaryResponseDto, userType);
        var categoryTotal = getTotalCategory(currentTimeInMillis, userType, userId);
        TotalSummaryResponseDto totalSummaryResponseDto = new TotalSummaryResponseDto(timeZone, userType,
                totalUserCountsResponseDto, totalSubscriptionCountsResponseDto, totalDealCountsResponseDto, categoryTotal);
        if (userType == UserType.BANK || userType == UserType.MERCHANT) {
            businessProfile = userType.equals(UserType.MERCHANT) ?
                    authUserService.getMerchantBusinessByMerchantId(authToken, userId) :
                    authUserService.getBankBusinessByBankId(authToken, userId);
            totalSummaryResponseDto.setTitleAndSubTitleResponse(businessProfile.getCreatedAt().getDisplayDate(), timeZone);
        }
        return totalSummaryResponseDto;
    }

    /**
     * This method is used to get today's category.
     *
     * @param todayInMillis todayInMillis
     * @param userType      userType
     * @param userId        userId
     * @return TodaySummaryResponseDto
     */
    private TodaySummaryResponseDto getTodayCategory(long todayInMillis, UserType userType, String userId) {
        try {
            long noOfNewCategories;
            long noOfNewBrand;
            long newCreditCardRequests = 0;
            switch (userType) {
                case ADMIN:
                    newCreditCardRequests = creditCardRepository.countByCreatedAtGreaterThanEqual(todayInMillis);
                    break;
                case BANK:
                    newCreditCardRequests = creditCardRepository.countByBankIdAndCreatedAtGreaterThanEqual(userId,
                            todayInMillis);
                    break;
                default:
                    break;
            }
            noOfNewCategories = categoryRepository.countByCreatedAtGreaterThanEqual(todayInMillis);
            noOfNewBrand = brandRepository.countByCreatedAtGreaterThanEqual(todayInMillis);
            TodaySummaryResponseDto todaySummaryResponseDto = new TodaySummaryResponseDto();
            todaySummaryResponseDto.setNoOfNewCategoriesAndBrands(noOfNewCategories, noOfNewBrand, userType);
            todaySummaryResponseDto.setNoOfNewCreditCardRequest(newCreditCardRequests, userType);
            return todaySummaryResponseDto;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Returning today category count from database was failed.", e);
        }
    }

    /**
     * This method is used to get total category.
     *
     * @param currentTimeInMillis currentTimeInMillis
     * @param userType            userType
     * @param userId              userId
     * @return TotalSummaryResponseDto
     */
    private TotalSummaryResponseDto getTotalCategory(long currentTimeInMillis, UserType userType, String userId) {
        try {
            Optional<CategoryBrandMerchant> optionalCategoryBrandMerchant =
                    categoryBrandMerchantRepository.findByMerchantId(userId);
            long categories = 0;
            long activeCategories = 0;
            long brands = 0;
            long creditCardRequests = 0;
            switch (userType) {
                case ADMIN:
                    categories = categoryRepository.count();
                    activeCategories = categoryRepository
                            .countByExpiryDateIsNullOrExpiryDateGreaterThan(currentTimeInMillis);
                    brands = brandRepository.count();
                    creditCardRequests = creditCardRepository.count();
                    break;
                case BANK:
                case MERCHANT:
                    categories = getTotalCategoriesForMerchantId(optionalCategoryBrandMerchant);
                    activeCategories = getTotalActiveCategoriesForMerchantId(currentTimeInMillis,
                            optionalCategoryBrandMerchant);
                    brands = getTotalBrandsForMerchantId(optionalCategoryBrandMerchant);
                    creditCardRequests = userType.equals(UserType.BANK) ? creditCardRepository.count() : 0;
                    break;
                default:
                    break;
            }
            TotalSummaryResponseDto totalSummaryResponseDto = new TotalSummaryResponseDto();
            totalSummaryResponseDto.setNoOfTotalNewCategoriesAndBrands(categories, brands, activeCategories);
            totalSummaryResponseDto.setNoOfNewCreditCardRequest(creditCardRequests, userType);
            return totalSummaryResponseDto;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Returning total category from database was failed.", e);
        }
    }

    /**
     * This method is used to get today's subscription.
     *
     * @param todayInMillis todayInMillis
     * @return SubscriptionCountsResponseDto
     */
    private SubscriptionCountsResponseDto getTodaySubscriptions(long todayInMillis) {
        try {
            long newMerchantPlan = subscriptionPlanRepository.
                    countSubscriptionPlansByUserRoleAndCreatedAtGreaterThanEqual(UserType.MERCHANT, todayInMillis);
            long newBankPlan = subscriptionPlanRepository.
                    countSubscriptionPlansByUserRoleAndCreatedAtGreaterThanEqual(UserType.BANK, todayInMillis);
            long newMerchantSubscription = subscriberRepository.
                    countSubscribesByUserTypeAndCreatedAtGreaterThanEqualAndIsDeletedFalse(UserType.MERCHANT, todayInMillis);
            long newBankSubscription = subscriberRepository.
                    countSubscribesByUserTypeAndCreatedAtGreaterThanEqualAndIsDeletedFalse(UserType.BANK, todayInMillis);
            long newMerchantUnsubscription = subscriberRepository.
                    countSubscribesByUserTypeAndUpdatedAtGreaterThanEqualAndIsDeletedTrue(UserType.MERCHANT, todayInMillis);
            long newBankUnsubscription = subscriberRepository.
                    countSubscribesByUserTypeAndUpdatedAtGreaterThanEqualAndIsDeletedTrue(UserType.BANK, todayInMillis);
            PlansResponseDto plansResponseDto = new PlansResponseDto(newMerchantPlan, newBankPlan);
            SubscribersResponseDto subscribersResponseDto = new SubscribersResponseDto(newMerchantSubscription,
                    newBankSubscription);
            UnsubscribersResponseDto unsubscribersResponseDto = new UnsubscribersResponseDto(newMerchantUnsubscription,
                    newBankUnsubscription);
            return new SubscriptionCountsResponseDto(plansResponseDto, subscribersResponseDto,
                    unsubscribersResponseDto);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Returning today subscriptions from database was failed.", e);
        }
    }

    /**
     * This method is used to get total subscription.
     *
     * @return SubscriptionCountsResponseDto
     */
    private SubscriptionCountsResponseDto getTotalSubscriptions() {
        try {
            long merchantPlans = subscriptionPlanRepository.countSubscriptionPlansByUserRole(UserType.MERCHANT);
            long bankPlans = subscriptionPlanRepository.countSubscriptionPlansByUserRole(UserType.BANK);
            long merchantActivePlans = subscriptionPlanRepository.
                    countSubscriptionPlansByUserRoleAndStatus(UserType.MERCHANT, SubscriptionStatus.ACTIVE);
            long bankActivePlans = subscriptionPlanRepository.
                    countSubscriptionPlansByUserRoleAndStatus(UserType.BANK, SubscriptionStatus.ACTIVE);
            long merchantSubscribers = subscriberRepository.countSubscribesByUserTypeAndIsDeletedFalse(UserType.MERCHANT);
            long bankSubscribers = subscriberRepository.countSubscribesByUserTypeAndIsDeletedFalse(UserType.BANK);
            long merchantUnsubscribers = subscriberRepository.countSubscribesByUserTypeAndIsDeletedTrue(UserType.MERCHANT);
            long bankUnsubscribers = subscriberRepository.countSubscribesByUserTypeAndIsDeletedTrue(UserType.BANK);
            PlansResponseDto plansResponseDto = new PlansResponseDto(merchantPlans, merchantActivePlans, bankPlans,
                    bankActivePlans);
            SubscribersResponseDto subscribersResponseDto = new SubscribersResponseDto(merchantSubscribers, bankSubscribers);
            UnsubscribersResponseDto unsubscribersResponseDto = new UnsubscribersResponseDto(merchantUnsubscribers,
                    bankUnsubscribers);
            return new SubscriptionCountsResponseDto(plansResponseDto, subscribersResponseDto,
                    unsubscribersResponseDto);
        } catch (
                DataAccessException e) {
            throw new KupiCoreException("Returning total subscriptions from database was failed.", e);
        }
    }

    /**
     * This method is used to get today's deal.
     *
     * @param todayInMillis    todayInMillis
     * @param tomorrowInMillis tomorrowInMillis
     * @param userType         userType
     * @param userId           userId
     * @return DealCountsResponseDto
     */
    private DealCountsResponseDto getTodayDealSummary(long todayInMillis, long tomorrowInMillis, UserType userType,
                                                      String userId) {
        try {
            long totalDealsToday = 0;
            long expiringDealsToday = 0;
            long dealRequestsToday = 0;
            long merchantDealsToday = 0;
            long bankDealsToday = 0;
            switch (userType) {
                case ADMIN:
                    totalDealsToday = dealRepository.countByCreatedAtGreaterThanEqualAndIsDeletedFalse(todayInMillis) +
                            bankDealRepository.countByCreatedAtGreaterThanEqual(todayInMillis);
                    expiringDealsToday = dealRepository.countByExpiredOnGreaterThanEqualAndExpiredOnLessThan(todayInMillis,
                            tomorrowInMillis) + bankDealRepository.countByExpiredOnGreaterThanEqualAndExpiredOnLessThan
                            (todayInMillis, tomorrowInMillis);
                    dealRequestsToday = requestADealRepository.countByCreatedAtGreaterThanEqual(todayInMillis);
                    merchantDealsToday = dealRepository.countByCreatedAtGreaterThanEqualAndIsDeletedFalse(todayInMillis);
                    bankDealsToday = bankDealRepository.countByCreatedAtGreaterThanEqual(todayInMillis);
                    break;
                case BANK:
                    totalDealsToday = bankDealRepository.countByBankIdAndCreatedAtGreaterThanEqual(userId, todayInMillis);
                    expiringDealsToday = bankDealRepository.countByBankIdAndExpiredOnGreaterThanEqualAndExpiredOnLessThan
                            (userId, todayInMillis, tomorrowInMillis);
                    dealRequestsToday = requestADealRepository.countByMerchantIdAndCreatedAtGreaterThanEqual(userId,
                            todayInMillis);
                    merchantDealsToday = dealRepository.countByCreatedAtGreaterThanEqualAndIsDeletedFalse(todayInMillis);
                    break;
                case MERCHANT:
                    totalDealsToday = dealRepository.countByMerchantIdAndCreatedAtGreaterThanEqualAndIsDeletedFalse(userId, todayInMillis);
                    expiringDealsToday = dealRepository.countByMerchantIdAndExpiredOnGreaterThanEqualAndExpiredOnLessThan
                            (userId, todayInMillis, tomorrowInMillis);
                    dealRequestsToday = requestADealRepository.countByMerchantIdAndCreatedAtGreaterThanEqual(userId,
                            todayInMillis);
                    break;
                default:
                    break;
            }
            return new DealCountsResponseDto(totalDealsToday, expiringDealsToday,
                    merchantDealsToday, bankDealsToday, dealRequestsToday, userType);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Returning today deal from database was failed.", e);
        }
    }

    /**
     * This method is used to get total deal.
     *
     * @param currentTimeInMillis currentTimeInMillis
     * @param userType            userType
     * @param userId              userId
     * @return DealCountsResponseDto
     */
    private DealCountsResponseDto getTotalDeal(long currentTimeInMillis, UserType userType, String userId) {
        try {
            long merchantDeals = 0;
            long bankDeals = 0;
            long activeBankDeals = 0;
            long activeMerchantDeals = 0;
            long activeDeals = 0;
            long dealRequests = 0;
            long totalDeals = 0;
            switch (userType) {
                case ADMIN:
                    merchantDeals = dealRepository.countByIsDeletedFalse();
                    var allActiveMerchantDealsPage = dealSearchIndexRepository
                            .findAllValidDeals(org.springframework.data.domain.Pageable.unpaged(),
                                    currentTimeInMillis, UserType.MERCHANT);
                    activeMerchantDeals = getCouponFilteredActiveDealCount(allActiveMerchantDealsPage.getContent());
                    activeDeals = activeMerchantDeals;
                    dealRequests = requestADealRepository.count();
                    totalDeals = merchantDeals;
                    break;
                case MERCHANT:
                    activeDeals = dealSearchIndexRepository.getTotalNumberOfActiveDealsForMerchantId(userId,
                            currentTimeInMillis);
                    dealRequests = requestADealRepository.countByMerchantId(userId);
                    totalDeals = dealRepository.countByMerchantIdAndIsDeletedFalse(userId);
                    break;
                default:
                    break;
            }
            DealCountsResponseDto dealCountsResponseDto = new DealCountsResponseDto();
            if (userType == UserType.ADMIN) {
                dealCountsResponseDto.setAdminDeals(totalDeals, activeDeals, merchantDeals, activeMerchantDeals, bankDeals,
                        activeBankDeals, dealRequests);
            } else {
                dealCountsResponseDto.setBankOrMerchantDeals(totalDeals, activeDeals, merchantDeals, activeMerchantDeals,
                        dealRequests, userType);
            }
            return dealCountsResponseDto;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Returning total deal from database was failed.", e);
        }
    }

    /**
     * Used to get total categories for merchantId.
     *
     * @param optionalCategoryBrandMerchant optionalCategoryBrandMerchant
     * @return total number of categories for merchant.
     */
    private int getTotalCategoriesForMerchantId(Optional<CategoryBrandMerchant> optionalCategoryBrandMerchant) {
        return optionalCategoryBrandMerchant.map(categoryBrandMerchant ->
                categoryBrandMerchant.getCategories().size()).orElse(ZERO);
    }

    /**
     * Used to get total brands for merchantId.
     *
     * @param optionalCategoryBrandMerchant optionalCategoryBrandMerchant
     * @return total number of brands for merchant.
     */
    private int getTotalBrandsForMerchantId(Optional<CategoryBrandMerchant> optionalCategoryBrandMerchant) {
        return optionalCategoryBrandMerchant.map(categoryBrandMerchant ->
                categoryBrandMerchant.getBrands().size()).orElse(ZERO);
    }

    /**
     * Used to get total active categories for merchantId.
     *
     * @param optionalCategoryBrandMerchant optionalCategoryBrandMerchant
     * @return total number of active categories for merchant.
     */
    private int getTotalActiveCategoriesForMerchantId(long currentTimeInMillis, Optional<CategoryBrandMerchant>
            optionalCategoryBrandMerchant) {
        int activeCategoryCount = ZERO;
        if (optionalCategoryBrandMerchant.isPresent()) {
            for (String categoryId : optionalCategoryBrandMerchant.get().getCategories()) {
                Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
                if (optionalCategory.isPresent()) {
                    Category category = optionalCategory.get();
                    CategoryType categoryType = category.getCategoryType();
                    Long expiryDate = category.getExpiryDate();
                    if (categoryType == CategoryType.NORMAL || (expiryDate != null && expiryDate > currentTimeInMillis)) {
                        activeCategoryCount++;
                    }
                }
            }
        }
        return activeCategoryCount;
    }

    /**
     * Get activity log for merchant including coupon redemptions, deal creations, and deal updates
     *
     * @param merchantId merchant ID
     * @param timeZone   time zone
     * @param pageable   pagination parameters
     * @return ActivityLogResponseDto with paginated activities
     */
    public ActivityLogResponseDto getActivityLog(String merchantId, String timeZone, Pageable pageable) {
        try {
            ZoneId zone = ZoneId.of(timeZone);
            ZonedDateTime now = ZonedDateTime.now(zone);
            ZonedDateTime startDate = now.minusDays(activityLogDaysToShow);
            long startMillis = startDate.toInstant().toEpochMilli();
            long endMillis = now.toInstant().toEpochMilli();

            List<ActivityLogEntryResponseDto> activities = new ArrayList<>();
            PrettyTime prettyTime = new PrettyTime();

            // Get coupon redemptions
            List<Coupon> redeemedCoupons = couponRepository.findRedeemedCouponsByMerchantIdAndDateRange(
                    merchantId, startMillis, endMillis);
            for (Coupon coupon : redeemedCoupons) {
                if (coupon.getDeal() != null) {
                    Deal deal = coupon.getDeal();
                    // Calculate earnings per coupon: originalPrice - deductionAmount
                    double earningsPerCoupon = calculateEarningsPerCoupon(deal);
                    
                    ActivityLogEntryResponseDto entry = new ActivityLogEntryResponseDto();
                    entry.setActivityType("COUPON_REDEEMED");
                    entry.setDescription("Coupon redeemed");
                    entry.setDealTitle(deal.getTitle());
                    entry.setDealId(deal.getId());
                    entry.setAmount(earningsPerCoupon > 0 ? BigDecimal.valueOf(earningsPerCoupon) : BigDecimal.ZERO);
                    entry.setTimestamp(coupon.getRedeemedOn());
                    entry.setTimeAgo(prettyTime.format(new Date(coupon.getRedeemedOn())));
                    activities.add(entry);
                }
            }

            List<Deal> createdDeals = dealRepository.findDealsCreatedByMerchantAndDateRange(
                    merchantId, startMillis, endMillis);
            for (Deal deal : createdDeals) {
                ActivityLogEntryResponseDto entry = new ActivityLogEntryResponseDto();
                entry.setActivityType("DEAL_CREATED");
                entry.setDescription("New Deal Published");
                entry.setDealTitle(deal.getTitle());
                entry.setDealId(deal.getId());
                entry.setAmount(null);
                entry.setTimestamp(deal.getCreatedAt());
                entry.setTimeAgo(prettyTime.format(new Date(deal.getCreatedAt())));
                activities.add(entry);
            }

            List<Deal> updatedDeals = dealRepository.findDealsUpdatedByMerchantAndDateRange(
                    merchantId, startMillis, endMillis);
            for (Deal deal : updatedDeals) {
                ActivityLogEntryResponseDto entry = new ActivityLogEntryResponseDto();
                entry.setActivityType("DEAL_UPDATED");
                entry.setDescription("Deal Updated");
                entry.setDealTitle(deal.getTitle());
                entry.setDealId(deal.getId());
                entry.setAmount(null);
                entry.setTimestamp(deal.getUpdatedAt());
                entry.setTimeAgo(prettyTime.format(new Date(deal.getUpdatedAt())));
                activities.add(entry);
            }
            activities.sort(Comparator.comparing(ActivityLogEntryResponseDto::getTimestamp).reversed());

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), activities.size());
            List<ActivityLogEntryResponseDto> paginatedActivities = activities.subList(start, end);
            Page<ActivityLogEntryResponseDto> activityPage = new PageImpl<>(
                    paginatedActivities, pageable, activities.size());

            return new ActivityLogResponseDto(activityPage);

        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to retrieve activity log for merchant: " + merchantId, e);
        }
    }

    /**
     * Gets total savings for all users: sum of currently active deals' deductionAmount, computed in real time.
     * This way the total is correct even when a deal becomes active later (e.g. validFrom is tomorrow):
     * as soon as the deal is within its valid date range it is included on the next API call.
     *
     * @return TotalSavingsResponseDto with total savings (sum of active deals' deductionAmount)
     */
    public TotalSavingsResponseDto getTotalSavingsForAllUsers() {
        try {
            long currentMills = System.currentTimeMillis();
            List<DealSearchIndex> activeDeals = dealSearchIndexRepository
                    .findAllActiveDealsWithActiveMerchantList(currentMills, UserType.MERCHANT);

            if (activeDeals == null || activeDeals.isEmpty()) {
                return new TotalSavingsResponseDto(BigDecimal.ZERO, null);
            }

            BigDecimal totalSavings = activeDeals.stream()
                    .map(deal -> {
                        Double originalPrice = deal.getOriginalPrice();
                        if (originalPrice == null || originalPrice <= 0) {
                            return BigDecimal.ZERO;
                        }
                        double deductionAmount = calculateDeductionAmount(deal, originalPrice);
                        return BigDecimal.valueOf(deductionAmount).setScale(2, RoundingMode.HALF_UP);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return new TotalSavingsResponseDto(totalSavings, null);
        } catch (DataAccessException e) {
            log.error("Failed to calculate total savings for all users", e);
            throw new KupiCoreException("Failed to retrieve total savings", e);
        }
    }

    /**
     * Calculates total savings for a specific user in real-time.
     * Savings = sum of each redeemed coupon's deal deductionAmount.
     * For percentage-based deals, deductionAmount is derived from originalPrice and deductionPercentage.
     *
     * @param userId user ID
     * @return TotalSavingsResponseDto with total savings (sum of deductionAmount) for the user
     */
    public TotalSavingsResponseDto getTotalSavingsForUser(String userId) {
        try {
            List<Coupon> redeemedCoupons = couponRepository.findRedeemedCouponsByUserIdWithDeal(userId);

            if (redeemedCoupons == null || redeemedCoupons.isEmpty()) {
                log.debug("No redeemed coupons found for userId={}", userId);
                return new TotalSavingsResponseDto(BigDecimal.ZERO, userId);
            }

            log.debug("Calculating total savings for userId={}. Found {} redeemed coupons", userId, redeemedCoupons.size());

            BigDecimal totalSavings = redeemedCoupons.stream()
                    .filter(coupon -> coupon.getDeal() != null)
                    .map(coupon -> {
                        Deal deal = coupon.getDeal();
                        Double originalPrice = deal.getOriginalPrice();
                        if (originalPrice == null || originalPrice <= 0) {
                            log.debug("Skipping coupon with invalid price. couponId={}, dealId={}, originalPrice={}",
                                    coupon.getId(), deal.getId(), originalPrice);
                            return BigDecimal.ZERO;
                        }
                        double deductionAmount = calculateDeductionAmount(deal, originalPrice);
                        return BigDecimal.valueOf(deductionAmount).setScale(2, RoundingMode.HALF_UP);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.info("Total savings calculated for userId={}: {}", userId, totalSavings);

            return new TotalSavingsResponseDto(totalSavings, userId);
        } catch (DataAccessException e) {
            log.error("Failed to calculate total savings for userId: " + userId, e);
            throw new KupiCoreException("Failed to calculate total savings for user", e);
        }
    }

    /**
     * Returns combined total savings: user savings (optional) and total app savings.
     * If userId is null or blank, only total app savings is calculated and returned.
     *
     * @param userId nullable target user id
     * @return CombinedTotalSavingsResponseDto with userSavings (optional) and totalAppSavings
     */
    public CombinedTotalSavingsResponseDto getCombinedTotalSavings(String userId) {
        // Always fetch total app savings from cached value
        TotalSavingsResponseDto appSavingsDto = getTotalSavingsForAllUsers();
        BigDecimal totalAppSavings = appSavingsDto != null ? appSavingsDto.getTotalSavings() : BigDecimal.ZERO;

        // Optionally calculate user savings if userId is provided
        BigDecimal userSavings = null;
        if (userId != null && !userId.isBlank()) {
            TotalSavingsResponseDto userSavingsDto = getTotalSavingsForUser(userId);
            userSavings = userSavingsDto != null ? userSavingsDto.getTotalSavings() : BigDecimal.ZERO;
        }

        return new CombinedTotalSavingsResponseDto(userSavings, totalAppSavings);
    }
}