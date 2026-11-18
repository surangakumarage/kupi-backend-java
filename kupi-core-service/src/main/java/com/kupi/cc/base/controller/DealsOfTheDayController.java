package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.domain.entity.DealsOfTheDayEvent;
import com.kupi.cc.base.domain.entity.DealsOfTheDayEventSearchIndex;
import com.kupi.cc.base.domain.request.DealsOfTheDayEventRequestDto;
import com.kupi.cc.base.domain.response.*;
import com.kupi.cc.base.enums.DealsOfTheDayHistoryFilter;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.service.*;
import com.kupi.cc.base.domain.entity.FavouriteDeal;
import com.kupi.cc.base.repository.FavouriteDealRepository;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Deals of the day controller
 */
@Validated
@Slf4j
@RestController
@RequestMapping("api/v1/deals/deals-of-the-day")
public class DealsOfTheDayController extends Controller {

    private static final int DOD_PAGE_MAX_SIZE = 10;
    private static final int MAX_PAGE_SIZE_FOR_HISTORY = 6;
    private static final String LOG_MESSAGE_DETAILS = "{} page: {}, size {}, categoryId: {}, merchantId: {}, " +
            "searchTerm: {}, userType: {}, for userId: {}";
    private static final String INVALID_EVENT_ID_USER_ID = "Invalid deals of the day eventId: {} for userId: {}";
    private final DealsOfTheDayService dealsOfTheDayService;
    private final CategoryService categoryService;
    private final AuthUserService authUserService;
    private final DealSearchService dealSearchService;
    private final CouponStatisticsService couponStatisticsService;
    private final DealService dealService;
    private final PromoTypeService promoTypeService;
    private final FavouriteDealRepository favouriteDealRepository;


    @Autowired
    public DealsOfTheDayController(Translator translator, DealsOfTheDayService dealsOfTheDayService,
                                   CategoryService categoryService, AuthUserService authUserService,
                                   DealSearchService dealSearchService, CouponStatisticsService couponStatisticsService,
                                   DealService dealService, PromoTypeService promoTypeService,
                                   FavouriteDealRepository favouriteDealRepository) {
        super(translator);
        this.dealsOfTheDayService = dealsOfTheDayService;
        this.categoryService = categoryService;
        this.authUserService = authUserService;
        this.dealSearchService = dealSearchService;
        this.couponStatisticsService = couponStatisticsService;
        this.dealService = dealService;
        this.promoTypeService = promoTypeService;
        this.favouriteDealRepository = favouriteDealRepository;
    }

    /**
     * This method return deals of the days by category id, merchant id, search term
     *
     * @param userId     userId
     * @param timeZone   timeZone
     * @param page       page
     * @param size       size
     * @param categoryId categoryId
     * @param merchantId merchantId
     * @param searchTerm searchTerm
     * @return ResponseEntity Paginated deals list
     */
    @GetMapping("/{page}/{size}/category/{categoryId}/{userType}/{merchantId}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getDealsOfTheDay(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String categoryId,
            @PathVariable String merchantId,
            @PathVariable UserType userType,
            @PathVariable String searchTerm) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "search deal-of-the-day");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            if (!categoryId.equals(ALL)) {
                categoryService.validateCategoryId(categoryId);
            }
            Pageable pageable = PageRequest.of(page, size);
            var dealsOfTheDayPage = dealsOfTheDayService.
                    dealsOfTheDayPageForMerchant(pageable, categoryId, merchantId, searchTerm, userType);
            dealsOfTheDayPage = filterDealPageByNonBlockedMerchants(dealsOfTheDayPage, userType);
            var dealIdList = dealsOfTheDayPage.get().map(Deal::getId).collect(Collectors.toList());
            var couponStatisticsList = couponStatisticsService.getCouponStatisticsByDealIdList(dealIdList);
            Page<Deal> deals = dealsOfTheDayService.removeCouponNotAvailableDeals(dealsOfTheDayPage, couponStatisticsList);
            var bulkUserResponseMap =
                    authUserService.getMerchantMapForDeals(deals, userType);
            var dealListResponseDto = new DealListResponseDto(deals, bulkUserResponseMap, timeZone);

            // Enrich response with isFavorite flag similar to regular deal listing
            if (userId != null && !deals.getContent().isEmpty()) {
                var finalDealIdList = deals.get().map(Deal::getId).collect(Collectors.toList());
                var favoriteDeals = favouriteDealRepository.findByUserIdAndDealIdIn(userId, finalDealIdList);
                if (favoriteDeals != null && !favoriteDeals.isEmpty()) {
                    var favoriteDealIds = favoriteDeals.stream()
                            .map(FavouriteDeal::getDealId)
                            .collect(Collectors.toSet());
                    dealListResponseDto.getDeals().forEach(basicDeal ->
                            basicDeal.setFavorite(favoriteDealIds.contains(basicDeal.getId())));
                }
            }
            log.debug(LOG_MESSAGE_DETAILS, "Successfully returned deals of the day.", page, size, categoryId,
                    merchantId, searchTerm, userType, userId);
            return getSuccessResponse(dealListResponseDto, SuccessResponseStatusType.READ_DEAL_LIST);
        } catch (InvalidCategoryException e) {
            log.error(LOG_MESSAGE_DETAILS, "Invalid category Id for related categories.", page, size, categoryId,
                    merchantId, searchTerm, userType, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            log.error(LOG_MESSAGE_DETAILS, "Returning deals of the days was failed.", page, size, categoryId,
                    merchantId, searchTerm, userType, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Filters a deal page to only include deals whose merchant/bank is not blocked (approved and active in auth service).
     * Ensures blocked merchants do not appear in mobile app deals-of-the-day listing.
     *
     * @param dealPage page of deals
     * @param userType MERCHANT or BANK
     * @return page containing only deals from non-blocked merchants/banks; same page if empty
     */
    private Page<Deal> filterDealPageByNonBlockedMerchants(Page<Deal> dealPage, UserType userType) {
        if (dealPage == null || !dealPage.hasContent()) {
            return dealPage;
        }
        List<String> merchantIds = dealPage.getContent().stream()
                .map(Deal::getMerchantId)
                .distinct()
                .collect(Collectors.toList());
        Set<String> eligible = authUserService.getEligibleMerchantIds(merchantIds, userType);
        List<Deal> filtered = dealPage.getContent().stream()
                .filter(d -> eligible.contains(d.getMerchantId()))
                .collect(Collectors.toList());
        return new PageImpl<>(filtered, dealPage.getPageable(), dealPage.getTotalElements());
    }

    /**
     * This method is used to get deals of the day history.
     *
     * @param userId     userId
     * @param timeZone   timeZone
     * @param page       page
     * @param size       size
     * @param searchTerm ALL/searchTerm
     * @param option     filter option
     * @return deals of the day history.
     */
    @GetMapping("/{userType}/{userId}/{page}/{size}/search/{searchTerm}/filter/{option}")
    public ResponseEntity<ResponseWrapper> getDealsOfTheDayHistory(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable UserType userType,
            @PathVariable String userId,
            @Min(DEFAULT_PAGE) @Max(MAX_PAGE_SIZE_FOR_HISTORY) @PathVariable int page,
            @Positive @Max(DOD_PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String searchTerm,
            @PathVariable String option

    ) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "get deals-of-the-day history");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            DealsOfTheDayHistoryFilter filter = DealsOfTheDayHistoryFilter.getOption(option);
            List<DealsOfTheDayBasicEventResponseDto> historyResponseList =
                    dealsOfTheDayService.getDealsOfTheDayHistory(timeZone, searchTerm, filter, userId, userType);
            return getSuccessResponse(
                    new DealsOfTheDayHistoryGroupResponseDto(historyResponseList.size(), page, historyResponseList),
                    SuccessResponseStatusType.READ_DEAL_LIST);
        } catch (InvalidDateOptionException e) {
            log.error("Invalid filter option: {} for userId: {}", option, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_DATE_OPTION);
        } catch (KupiCoreException e) {
            log.error("Returning deals of the day history was failed for userId: {}. " +
                    "Page: {}, size: {}, searchTerm: {}, filter: {}", userId, page, size, searchTerm, option, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get deals of the day history for today and upcoming events only.
     * <p>
     * Filter options:
     * - TODAY    : returns today's deals of the day history (same as existing history endpoint with TODAY filter)
     * - UPCOMING : returns upcoming deals of the day events (including today and future dates)
     *
     * @param timeZone    timeZone
     * @param userType    userType
     * @param userId      userId
     * @param page        page
     * @param size        size
     * @param categoryId  categoryId (ALL or specific category ID)
     * @param promoTypeId promoTypeId (ALL or specific promo type ID)
     * @param searchTerm  ALL/searchTerm
     * @param option      filter option (TODAY / UPCOMING)
     * @return deals of the day history for today or upcoming events.
     */
    @GetMapping("/today-upcoming/{userType}/{userId}/{page}/{size}/category/{categoryId}/promotype/{promoTypeId}/search/{searchTerm}/filter/{option}")
    public ResponseEntity<ResponseWrapper> getDealsOfTheDayTodayAndUpcoming(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable UserType userType,
            @PathVariable String userId,
            @Min(DEFAULT_PAGE) @Max(MAX_PAGE_SIZE_FOR_HISTORY) @PathVariable int page,
            @Positive @Max(DOD_PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String categoryId,
            @PathVariable String promoTypeId,
            @PathVariable String searchTerm,
            @PathVariable String option
    ) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType,
                        "get today/upcoming deals-of-the-day history");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }

            // Validate category if not ALL
            if (!categoryId.equals(ALL)) {
                categoryService.validateCategoryId(categoryId);
            }

            // Validate promoType if not ALL
            if (!promoTypeId.equals(ALL)) {
                promoTypeService.getPromoTypeById(promoTypeId);
            }

            String normalizedOption = option == null ? "" : option.trim().toUpperCase();
            long currentTime = System.currentTimeMillis();

            if ("TODAY".equals(normalizedOption)) {
                List<DealsOfTheDayBasicEventResponseDto> historyResponseList =
                        dealsOfTheDayService.getDealsOfTheDayHistory(timeZone, searchTerm,
                                DealsOfTheDayHistoryFilter.TODAY, userId, userType);
                
                // Convert to extended DTOs with dealStatus and apply category/promoType filters
                List<ExtendedDealsOfTheDayBasicEventResponseDto> extendedHistoryList = new ArrayList<>();
                for (DealsOfTheDayBasicEventResponseDto basicEvent : historyResponseList) {
                    List<ExtendedDealResponseDto> extendedDeals = new ArrayList<>();
                    for (BasicDealResponseDto basicDeal : basicEvent.getDeals()) {
                        try {
                            // Get DealSearchIndex to calculate dealStatus and create ExtendedDealResponseDto
                            var dealSearchIndex = dealSearchService.findById(basicDeal.getId());
                            
                            // Apply category filter (using LIKE-style matching similar to repository query)
                            if (!categoryId.equals(ALL)) {
                                String relatedCategoriesStr = dealSearchIndex.getRelatedCategories();
                                if (relatedCategoriesStr == null || relatedCategoriesStr.trim().isEmpty() || 
                                    relatedCategoriesStr.equals("null")) {
                                    log.debug("Deal {} has no valid relatedCategories: [{}], skipping category filter", 
                                            basicDeal.getId(), relatedCategoriesStr);
                                    continue; // Skip this deal if it doesn't have categories
                                }
                                // Use LIKE-style matching: check if categoryId appears in the comma-separated string
                                // This matches the behavior of the repository query: LIKE CONCAT('%', :categoryId, '%')
                                // But we need to ensure we match whole category IDs, not substrings
                                // So we check for exact matches in the comma-separated list
                                String cleanedCategories = relatedCategoriesStr.replaceFirst("^null", "").trim();
                                if (cleanedCategories.isEmpty()) {
                                    log.debug("Deal {} has empty relatedCategories after cleaning, skipping", basicDeal.getId());
                                    continue;
                                }
                                // Split by comma and check for exact match (handling trailing commas)
                                String[] categories = cleanedCategories.split(",");
                                boolean categoryMatch = false;
                                for (String cat : categories) {
                                    String trimmedCat = cat.trim();
                                    if (!trimmedCat.isEmpty() && !trimmedCat.equals("null") && trimmedCat.equals(categoryId)) {
                                        categoryMatch = true;
                                        break;
                                    }
                                }
                                // Also check using contains for edge cases (but this is less precise)
                                if (!categoryMatch && cleanedCategories.contains(categoryId)) {
                                    // Verify it's not a substring match by checking word boundaries (comma-separated)
                                    if (cleanedCategories.equals(categoryId) || 
                                        cleanedCategories.startsWith(categoryId + ",") ||
                                        cleanedCategories.contains("," + categoryId + ",") ||
                                        cleanedCategories.endsWith("," + categoryId)) {
                                        categoryMatch = true;
                                    }
                                }
                                if (!categoryMatch) {
                                    log.debug("Deal {} categories: [{}] does not match filter categoryId: {}", 
                                            basicDeal.getId(), relatedCategoriesStr, categoryId);
                                    continue; // Skip this deal if it doesn't match category filter
                                }
                            }
                            
                            // Apply promoType filter
                            if (!promoTypeId.equals(ALL)) {
                                if (dealSearchIndex.getPromoTypeId() == null || 
                                    !dealSearchIndex.getPromoTypeId().equals(promoTypeId)) {
                                    continue; // Skip this deal if it doesn't match promoType filter
                                }
                            }
                            
                            String dealStatus = dealService.calculateDealStatus(dealSearchIndex, currentTime, currentTime);
                            BigDecimal earnings = dealService.calculateEarnings(basicDeal.getId(), 0L, currentTime);
                            // Create ExtendedDealResponseDto from DealSearchIndex
                            ExtendedDealResponseDto extendedDeal = new ExtendedDealResponseDto(
                                    dealSearchIndex, timeZone, earnings, dealStatus);
                            extendedDeals.add(extendedDeal);
                        } catch (Exception e) {
                            // If DealSearchIndex not found, try to get Deal entity
                            try {
                                Deal deal = dealService.getDeal(basicDeal.getId(), userType);
                                
                                // Apply category filter
                                if (!categoryId.equals(ALL)) {
                                    boolean categoryMatch = deal.getRelatedCategories() != null &&
                                            deal.getRelatedCategories().stream()
                                                    .anyMatch(category -> categoryId.equals(category.getId()));
                                    if (!categoryMatch) {
                                        continue; // Skip this deal if it doesn't match category filter
                                    }
                                }
                                
                                // Apply promoType filter
                                if (!promoTypeId.equals(ALL)) {
                                    if (deal.getPromoType() == null || 
                                        !promoTypeId.equals(deal.getPromoType().getId())) {
                                        continue; // Skip this deal if it doesn't match promoType filter
                                    }
                                }
                                
                                // Get merchant info from basicDeal
                                BasicMerchantBusinessResponseDto merchant = basicDeal.getMerchant() != null 
                                        ? basicDeal.getMerchant() : basicDeal.getBank();
                                String dealStatus = "Draft"; // Default status
                                BigDecimal earnings = dealService.calculateEarnings(basicDeal.getId(), 0L, currentTime);
                                ExtendedDealResponseDto extendedDeal = new ExtendedDealResponseDto(
                                        deal, merchant, timeZone, earnings, dealStatus);
                                extendedDeals.add(extendedDeal);
                            } catch (Exception ex) {
                                log.warn("Failed to get deal or DealSearchIndex for dealId: {}", basicDeal.getId());
                                // Skip this deal if we can't get the required data
                            }
                        }
                    }
                    if (!extendedDeals.isEmpty()) {
                        extendedHistoryList.add(new ExtendedDealsOfTheDayBasicEventResponseDto(
                                basicEvent.getValidOn().getMilliseconds(), timeZone, extendedDeals));
                    }
                }
                
                return getSuccessResponse(
                        new ExtendedDealsOfTheDayHistoryGroupResponseDto(extendedHistoryList.size(), page, extendedHistoryList),
                        SuccessResponseStatusType.READ_DEAL_LIST);
            } else if ("UPCOMING".equals(normalizedOption)) {
                List<UpcomingDealsOfTheDayHistoryGroupResponseDto> historyResponseList =
                        dealsOfTheDayService.getUpcomingDealsOfTheDayHistory(timeZone, searchTerm, userId, userType);
                
                // Convert to extended DTOs with dealStatus and apply category/promoType filters
                List<ExtendedUpcomingDealsOfTheDayHistoryGroupResponseDto> extendedHistoryList = new ArrayList<>();
                for (UpcomingDealsOfTheDayHistoryGroupResponseDto basicEvent : historyResponseList) {
                    List<ExtendedDealResponseDto> extendedDeals = new ArrayList<>();
                    for (BasicDealResponseDto basicDeal : basicEvent.getDealGroup()) {
                        try {
                            // Get DealSearchIndex to calculate dealStatus and create ExtendedDealResponseDto
                            var dealSearchIndex = dealSearchService.findById(basicDeal.getId());
                            
                            // Apply category filter (using LIKE-style matching similar to repository query)
                            if (!categoryId.equals(ALL)) {
                                String relatedCategoriesStr = dealSearchIndex.getRelatedCategories();
                                if (relatedCategoriesStr == null || relatedCategoriesStr.trim().isEmpty() || 
                                    relatedCategoriesStr.equals("null")) {
                                    log.debug("Deal {} has no valid relatedCategories: [{}], skipping category filter", 
                                            basicDeal.getId(), relatedCategoriesStr);
                                    continue; // Skip this deal if it doesn't have categories
                                }
                                // Use LIKE-style matching: check if categoryId appears in the comma-separated string
                                // This matches the behavior of the repository query: LIKE CONCAT('%', :categoryId, '%')
                                // But we need to ensure we match whole category IDs, not substrings
                                // So we check for exact matches in the comma-separated list
                                String cleanedCategories = relatedCategoriesStr.replaceFirst("^null", "").trim();
                                if (cleanedCategories.isEmpty()) {
                                    log.debug("Deal {} has empty relatedCategories after cleaning, skipping", basicDeal.getId());
                                    continue;
                                }
                                // Split by comma and check for exact match (handling trailing commas)
                                String[] categories = cleanedCategories.split(",");
                                boolean categoryMatch = false;
                                for (String cat : categories) {
                                    String trimmedCat = cat.trim();
                                    if (!trimmedCat.isEmpty() && !trimmedCat.equals("null") && trimmedCat.equals(categoryId)) {
                                        categoryMatch = true;
                                        break;
                                    }
                                }
                                // Also check using contains for edge cases (but this is less precise)
                                if (!categoryMatch && cleanedCategories.contains(categoryId)) {
                                    // Verify it's not a substring match by checking word boundaries (comma-separated)
                                    if (cleanedCategories.equals(categoryId) || 
                                        cleanedCategories.startsWith(categoryId + ",") ||
                                        cleanedCategories.contains("," + categoryId + ",") ||
                                        cleanedCategories.endsWith("," + categoryId)) {
                                        categoryMatch = true;
                                    }
                                }
                                if (!categoryMatch) {
                                    log.debug("Deal {} categories: [{}] does not match filter categoryId: {}", 
                                            basicDeal.getId(), relatedCategoriesStr, categoryId);
                                    continue; // Skip this deal if it doesn't match category filter
                                }
                            }
                            
                            // Apply promoType filter
                            if (!promoTypeId.equals(ALL)) {
                                if (dealSearchIndex.getPromoTypeId() == null || 
                                    !dealSearchIndex.getPromoTypeId().equals(promoTypeId)) {
                                    continue; // Skip this deal if it doesn't match promoType filter
                                }
                            }
                            
                            String dealStatus = dealService.calculateDealStatus(dealSearchIndex, currentTime, currentTime);
                            BigDecimal earnings = dealService.calculateEarnings(basicDeal.getId(), 0L, currentTime);
                            // Create ExtendedDealResponseDto from DealSearchIndex
                            ExtendedDealResponseDto extendedDeal = new ExtendedDealResponseDto(
                                    dealSearchIndex, timeZone, earnings, dealStatus);
                            extendedDeals.add(extendedDeal);
                        } catch (Exception e) {
                            // If DealSearchIndex not found, try to get Deal entity
                            try {
                                Deal deal = dealService.getDeal(basicDeal.getId(), userType);
                                
                                // Apply category filter
                                if (!categoryId.equals(ALL)) {
                                    boolean categoryMatch = deal.getRelatedCategories() != null &&
                                            deal.getRelatedCategories().stream()
                                                    .anyMatch(category -> categoryId.equals(category.getId()));
                                    if (!categoryMatch) {
                                        continue; // Skip this deal if it doesn't match category filter
                                    }
                                }
                                
                                // Apply promoType filter
                                if (!promoTypeId.equals(ALL)) {
                                    if (deal.getPromoType() == null || 
                                        !promoTypeId.equals(deal.getPromoType().getId())) {
                                        continue; // Skip this deal if it doesn't match promoType filter
                                    }
                                }
                                
                                // Get merchant info from basicDeal
                                BasicMerchantBusinessResponseDto merchant = basicDeal.getMerchant() != null 
                                        ? basicDeal.getMerchant() : basicDeal.getBank();
                                String dealStatus = "Draft"; // Default status
                                BigDecimal earnings = dealService.calculateEarnings(basicDeal.getId(), 0L, currentTime);
                                ExtendedDealResponseDto extendedDeal = new ExtendedDealResponseDto(
                                        deal, merchant, timeZone, earnings, dealStatus);
                                extendedDeals.add(extendedDeal);
                            } catch (Exception ex) {
                                log.warn("Failed to get deal or DealSearchIndex for dealId: {}", basicDeal.getId());
                                // Skip this deal if we can't get the required data
                            }
                        }
                    }
                    if (!extendedDeals.isEmpty()) {
                        extendedHistoryList.add(new ExtendedUpcomingDealsOfTheDayHistoryGroupResponseDto(
                                basicEvent.getValidOn().getMilliseconds(), timeZone, extendedDeals));
                    }
                }
                
                return getSuccessResponse(
                        new ExtendedUpcomingDealsOfTheDayHistoryResponseDto(extendedHistoryList),
                        SuccessResponseStatusType.READ_DEAL_LIST);
            } else {
                log.error("Invalid filter option for today/upcoming history: {} for userId: {}", option, userId);
                return getErrorResponse(ErrorResponseStatusType.INVALID_DATE_OPTION);
            }
        } catch (InvalidCategoryException e) {
            log.error("Invalid category Id: {} for today/upcoming deals of the day history for userId: {}", 
                    categoryId, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            log.error("Returning today/upcoming deals of the day history was failed for userId: {}. " +
                    "Page: {}, size: {}, categoryId: {}, promoTypeId: {}, searchTerm: {}, filter: {}", 
                    userId, page, size, categoryId, promoTypeId, searchTerm, option, e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns upcoming deals of the day events.
     *
     * @param timeZone   timeZone
     * @param userType   userType
     * @param userId     userId
     * @param page       page
     * @param size       size
     * @param searchTerm searchTerm
     * @return deals of the day events.
     */
    @GetMapping("/upcoming/{userType}/{userId}/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getUpcomingDealsOfTheDayEvents(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable UserType userType,
            @PathVariable String userId,
            @Min(DEFAULT_PAGE) @Max(PAGE_MAX_SIZE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String searchTerm) {
        try {
            Page<DealsOfTheDayEventSearchIndex> dealsOfTheDayEventPage =
                    dealsOfTheDayService.getUpcomingDealsOfTheDayEvents(page, size, userType, userId, searchTerm);
            return getSuccessResponse(new UpcomingDealsOfTheDayEventPageResponseDto(dealsOfTheDayEventPage, timeZone),
                    SuccessResponseStatusType.UPCOMING_DEALS_OF_THE_DAY_EVENTS);
        } catch (KupiCoreException e) {
            log.error("Returning deals of the day events was failed for userId: {}. " +
                    "Page: {}, size: {}, searchTerm: {}.", userId, page, size, searchTerm, e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns deals of the day summary.
     *
     * @param userId userId.
     * @return deals of the day summary.
     */
    @Secured({ADMIN_ROLE})
    @GetMapping("/upcoming/summary")
    public ResponseEntity<ResponseWrapper> getUpcomingDealsOfTheDayEventsSummary(
            @RequestHeader(name = USER_ID_HEADER) String userId) {
        try {
            DealsOfTheDayEventSummaryResponseDto eventSummaryResponseDto =
                    dealsOfTheDayService.getDealsOfTheDaySummaryForAdmin();
            return getSuccessResponse(eventSummaryResponseDto, SuccessResponseStatusType.DEALS_OF_THE_DAY_EVENT_SUMMARY);
        } catch (KupiCoreException e) {
            log.error("Returning deals of the day event summary was failed for userId: {}.", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to add deals of the day event.
     *
     * @param userId                       userId
     * @param userType                     MERCHANT/BANK
     * @param dealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto
     * @return success /error response
     */
    @PostMapping("/event/{userType}")
    public ResponseEntity<ResponseWrapper> setDealsOfTheDayEvent(@RequestHeader(USER_ID_HEADER) String userId,
                                                                 @PathVariable UserType userType,
                                                                 @RequestBody DealsOfTheDayEventRequestDto
                                                                         dealsOfTheDayEventRequestDto,
                                                                 HttpServletRequest request) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            String timeZone = request.getHeader(TIME_ZONE_HEADER);
            if (!dealsOfTheDayEventRequestDto.isRequiredAvailable())
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            if (!dealsOfTheDayEventRequestDto.isValidValidOn(timeZone))
                return getErrorResponse(ErrorResponseStatusType.INVALID_VALID_FROM_DATE);
            BusinessMerchantResponseDto businessProfile = getBusinessProfile(userType, authToken, userId);
            if (!businessProfile.getProfileType().equals(userType.toString()))
                return getErrorResponse(ErrorResponseStatusType.INVALID_USER_ID_OR_TYPE);
            DealsOfTheDayEvent dealsOfTheDayEvent =
                    dealsOfTheDayService.addDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, userType, businessProfile);
            return getSuccessResponse(new BasicDealsOfTheDayEventResponseDto(dealsOfTheDayEvent),
                    SuccessResponseStatusType.DEALS_OF_THE_DAY_EVENT_CREATED);
        } catch (ExistingEventException e) {
            log.error("Deals of the day event is already existing for userId: {}.", userId, e);
            return getErrorResponse(ErrorResponseStatusType.EVENT_ALREADY_EXIST);
        } catch (InvalidUserException e) {
            log.error("Invalid userId: {} or userType: {}", userId, userType, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_USER_ID_OR_TYPE);
        } catch (InvalidDealException e) {
            log.error("Invalid dealIds for deals of the day event. userId: {}", userId, e);
            // Check if the error message contains expired, deactivated, or draft
            if (e.getMessage() != null && 
                (e.getMessage().contains("Expired") || e.getMessage().contains("Deactivated") || 
                 e.getMessage().contains("Draft") || e.getMessage().contains("expired") || 
                 e.getMessage().contains("deactivated") || e.getMessage().contains("draft"))) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_DEAL_STATUS_FOR_DOD);
            }
            return getErrorResponse(ErrorResponseStatusType.INACTIVE_DEAL_ID);
        } catch (InvalidSubscribeException e) {
            log.error("Subscription plan not found for userId: {}", userId, e);
            return getErrorResponse(ErrorResponseStatusType.SUBSCRIPTION_PLAN_NOT_FOUND);
        } catch (LimitExceededException e) {
            log.error("Subscription plan limit exceeded for the event. userId: {}", userId, e);
            return getErrorResponse(e.getLimitExceededType().getErrorResponseStatusType());
        } catch (KupiCoreException e) {
            log.error("Adding deals of the day event failed for userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method will update deals of the day event.
     *
     * @param userId                       userId
     * @param dealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto
     * @return success/error response
     */
    @PutMapping("/event/{userType}")
    public ResponseEntity<ResponseWrapper> updateDealsOfTheDayEvent(@RequestHeader(USER_ID_HEADER) String userId,
                                                                    @PathVariable UserType userType,
                                                                    @RequestBody DealsOfTheDayEventRequestDto
                                                                            dealsOfTheDayEventRequestDto) {
        try {
            if (!dealsOfTheDayEventRequestDto.isRequiredAvailableForUpdate())
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            DealsOfTheDayEvent dealsOfTheDayEvent =
                    dealsOfTheDayService.updateDealsOfTheDayEvent(dealsOfTheDayEventRequestDto, userId, userType);
            return getSuccessResponse(new BasicDealsOfTheDayEventResponseDto(dealsOfTheDayEvent),
                    SuccessResponseStatusType.DEALS_OF_THE_DAY_EVENT_UPDATED);
        } catch (InvalidSubscribeException e) {
            log.error("Subscription plan not found for userId: {}", userId, e);
            return getErrorResponse(ErrorResponseStatusType.SUBSCRIPTION_PLAN_NOT_FOUND);
        } catch (LimitExceededException e) {
            log.error("Subscription plan limit exceeded for the event. userId: {}", userId, e);
            return getErrorResponse(e.getLimitExceededType().getErrorResponseStatusType());
        } catch (InvalidDealException e) {
            log.error("Invalid dealIds for deals of the day event. userId: {}", userId, e);
            // Check if the error message contains expired, deactivated, or draft
            if (e.getMessage() != null && 
                (e.getMessage().contains("Expired") || e.getMessage().contains("Deactivated") || 
                 e.getMessage().contains("Draft") || e.getMessage().contains("expired") || 
                 e.getMessage().contains("deactivated") || e.getMessage().contains("draft"))) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_DEAL_STATUS_FOR_DOD);
            }
            return getErrorResponse(ErrorResponseStatusType.INACTIVE_DEAL_ID);
        } catch (InvalidDealsOfTheDayEventException e) {
            log.error(INVALID_EVENT_ID_USER_ID, dealsOfTheDayEventRequestDto.getEventId(), userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_EVENT_ID);
        } catch (KupiCoreException e) {
            log.error("Updating deals of the day event failed for eventId: {}, userId: {}",
                    dealsOfTheDayEventRequestDto.getEventId(), userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to delete deals of the day event.
     *
     * @param userId  userId
     * @param eventId eventId
     * @return success / error response.
     */
    @DeleteMapping("/event/{eventId}")
    public ResponseEntity<ResponseWrapper> deleteDealsOfTheDayEvent(@RequestHeader(USER_ID_HEADER) String userId,
                                                                    @PathVariable String eventId) {
        try {
            dealsOfTheDayService.deleteDealsOfTheDayEvent(eventId, userId);
            return getSuccessResponse(null, SuccessResponseStatusType.DEALS_OF_THE_DAY_EVENT_DELETED);
        } catch (InvalidSubscribeException e) {
            log.error("Subscription plan not found for userId: {}", userId, e);
            return getErrorResponse(ErrorResponseStatusType.SUBSCRIPTION_PLAN_NOT_FOUND);
        } catch (InvalidDealsOfTheDayEventException e) {
            log.error(INVALID_EVENT_ID_USER_ID, eventId, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_EVENT_ID);
        } catch (KupiCoreException e) {
            log.error("Deleting deals of the day event failed for eventId: {}, userId: {}", eventId, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get dealOfTheDay event by event id.
     *
     * @param timeZone timezone
     * @param userId   user id
     * @param eventId  event id
     * @return success /error response
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<ResponseWrapper> getDealsOfTheDayEventById(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                     @RequestHeader(USER_ID_HEADER) String userId,
                                                                     @PathVariable String eventId,
                                                                     HttpServletRequest request) {
        try {
            var dealsOfTheDayEventById = dealsOfTheDayService.getDealsOfTheDayEventById(eventId);
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            var businessProfile =
                    getBusinessProfile(dealsOfTheDayEventById.getUserType(), authToken, dealsOfTheDayEventById.getUserId());
            Set<String> deals = dealsOfTheDayEventById.getDeals();
            var dealsList = dealSearchService.getDealListByDealIdList(new ArrayList<>(deals));
            var dealsOfTheDayDetailEventResponseDto =
                    new DealsOfTheDayDetailEventResponseDto(dealsOfTheDayEventById, timeZone, dealsList, businessProfile);
            return getSuccessResponse(dealsOfTheDayDetailEventResponseDto,
                    SuccessResponseStatusType.READ_DEALS_OF_THE_DAY);
        } catch (InvalidDealsOfTheDayEventException e) {
            log.error(INVALID_EVENT_ID_USER_ID, eventId, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_EVENT_ID);
        } catch (InvalidUserException e) {
            log.error("Invalid userId: {}", userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_USER_ID_OR_TYPE);
        } catch (KupiCoreException e) {
            log.error("Returning deals of the day event by id:" +
                    " {} failed by userid: {} and time zone: {}", eventId, userId, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * This method used to start deals of the day scheduler manually.
     */
    @GetMapping("/start")
    public void startDealsOfTheDayManually() {
        try {
            dealsOfTheDayService.saveDealsOfTheDay();
        } catch (KupiCoreException e) {
            log.error("Manually Starting Deals of the day was failed ", e);
        }
    }

    /**
     * This method gets business profile from auth service.
     *
     * @param userType  userType
     * @param authToken authToken
     * @param userId    userId
     * @return merchant/bank business profile.
     */
    private BusinessMerchantResponseDto getBusinessProfile(UserType userType, String authToken, String userId) {
        return userType.equals(UserType.MERCHANT) ?
                authUserService.getMerchantBusinessByMerchantId(authToken, userId) :
                authUserService.getBankBusinessByBankId(authToken, userId);
    }
}
