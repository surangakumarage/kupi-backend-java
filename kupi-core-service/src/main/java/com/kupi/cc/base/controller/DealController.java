package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.*;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.request.DealApprovalStatusUpdateRequestDto;
import com.kupi.cc.base.domain.request.DealRequestDto;
import com.kupi.cc.base.domain.request.DealUpdateRequestDto;
import com.kupi.cc.base.domain.request.ValidityPeriodDto;
import com.kupi.cc.base.domain.response.*;
import com.kupi.cc.base.domain.response.CategoryDealCountListResponseDto;
import com.kupi.cc.base.domain.response.CategoryDealCountResponseDto;
import com.kupi.cc.base.enums.*;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.service.*;
import com.kupi.cc.base.repository.CouponRepository;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Deal Controller
 */
@Slf4j
@RestController
@Validated
@RequestMapping("api/v1/deals")
public class DealController extends Controller {

    private static final String LOG_MESSAGE = "page: {}, size {}, categoryId: {}, merchantId: {}, searchTerm: {} " +
            "for userId: {}";
    private static final String CATEGORY_ID = " CategoryId: ";
    private final DealService dealService;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final AuthUserService authUserService;
    private final DealCodeService dealCodeService;
    private final SubscriberService subscriberService;
    private final CouponStatisticsService couponStatisticsService;
    private final PromoTypeService promoTypeService;
    private final CouponService couponService;
    private final DealViewService dealViewService;
    private final FavouriteDealService favouriteDealService;
    private final DealSearchService dealSearchService;

    @Autowired
    public DealController(DealService dealService, BrandService brandService, CategoryService categoryService,
                          AuthUserService authUserService, Translator translator, DealCodeService dealCodeService,
                          SubscriberService subscriberService, CouponStatisticsService couponStatisticsService,
                          PromoTypeService promoTypeService, CouponService couponService,
                          DealViewService dealViewService,
                          DealSearchService dealSearchService, FavouriteDealService favouriteDealService) {
        super(translator);
        this.dealService = dealService;
        this.brandService = brandService;
        this.categoryService = categoryService;
        this.authUserService = authUserService;
        this.dealCodeService = dealCodeService;
        this.subscriberService = subscriberService;
        this.couponStatisticsService = couponStatisticsService;
        this.promoTypeService = promoTypeService;
        this.couponService = couponService;
        this.dealViewService = dealViewService;
        this.dealSearchService = dealSearchService;
        this.favouriteDealService = favouriteDealService;
    }

    /**
     * Create Deal
     *
     * @param userId         userId
     * @param userType       MERCHANT/ BANK
     * @param dealRequestDto dealRequestDto
     * @param timeZone       time zone
     * @return SuccessResponse / ErrorResponse
     */
    @Secured({MERCHANT_ROLE})
    @PostMapping("/{userType}")
    public ResponseEntity<ResponseWrapper> createDeal(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable UserType userType,
            @RequestBody DealRequestDto dealRequestDto,
            HttpServletRequest request) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "create deal");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            if (!dealRequestDto.isRequiredAvailable(userType)) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            if (!dealRequestDto.isValidStartDate()) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_VALID_FROM_DATE);
            }
            if (!dealRequestDto.isValidPrice()) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_PRICE);
            }
            if (!dealRequestDto.isValidExpireDate()) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_EXPIRED_ON_DATE);
            }
            if (!dealRequestDto.isValidDeductionValue()) {
                var error = (dealRequestDto.getDeductionType() == DeductionType.AMOUNT) ?
                        ErrorResponseStatusType.INVALID_DEDUCTION_AMOUNT :
                        ErrorResponseStatusType.INVALID_DEDUCTION_PERCENTAGE;
                return getErrorResponse(error);
            }
            var businessProfile =
                    getBusinessProfile(userType, authToken, dealRequestDto.getMerchantId());
            if (!businessProfile.isActive()) {
                log.debug("Error Creating deal for Inactive merchant for merchant id: {}.",
                        businessProfile.getMerchantId());
                return getErrorResponse(ErrorResponseStatusType.INACTIVE_MERCHANT_FOR_CREATE_DEAL);
            }
            BusinessMerchantResponseDto merchant = null;
            if (UserType.BANK.equals(userType)) {
                merchant = authUserService.getMerchantBusinessByMerchantId(authToken, dealRequestDto.getShopId());
            }
            String dealCode = dealCodeService.generateAndSave(LocalDate.now(), userType);
            var dealCreateResponseDto = readAndCreateResponse(dealRequestDto, timeZone,
                    businessProfile, userType, merchant, dealCode);
            String objectToJson = dealCreateResponseDto.toLogJson();
            log.debug("Successfully created deal for userId: {}, to userType: {}, deal response {}",
                    userId, userType, objectToJson);
            return getSuccessResponse(dealCreateResponseDto,
                    SuccessResponseStatusType.CREATE_DEAL);
        } catch (LimitExceededException e) {
            log.error("Subscription plan limit exceeded for creating deal. userId: {}",
                    dealRequestDto.getMerchantId(), e);
            return getErrorResponse(e.getLimitExceededType().getErrorResponseStatusType());
        } catch (InvalidSubscribeException e) {
            log.error("Subscription plan not found for user id :{}", dealRequestDto.getMerchantId(), e);
            return getErrorResponse(ErrorResponseStatusType.SUBSCRIPTION_PLAN_NOT_FOUND);
        } catch (InvalidUserException e) {
            log.error("Invalid/ Inactive merchant id :{} for userType :{}. user id :{}",
                    dealRequestDto.getMerchantId(), userType, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INACTIVE_MERCHANT_FOR_CREATE_DEAL);
        } catch (InvalidCategoryException e) {
            log.error("Reading related category id for user: {} was failed.", userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (InvalidBrandException e) {
            log.error("Error when reading related brand id for user: {}.", userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_BRAND_ID);
        } catch (InvalidDateOptionException e) {
            log.error("Date ranges are not matching with seasonal categories date range. User: {}.", userId, e);
            return getErrorResponse(ErrorResponseStatusType.DEAL_DATES_DOESNT_MATCH_SEASONAL_CATEGORIES);
        } catch (KupiCoreException e) {
            log.error("Creating new deal for userId: {} was failed. userType: {}", userId, userType, e);
            return getInternalServerError();
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

    @PutMapping("/{userType}")
    public ResponseEntity<ResponseWrapper> updateDeal(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable UserType userType,
            @RequestBody DealUpdateRequestDto dealUpdateRequestDto,
            HttpServletRequest request) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "update deal");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            if (!dealUpdateRequestDto.isRequiredAvailable(userType)) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            if (!dealUpdateRequestDto.isValidStartDate()) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_VALID_FROM_DATE);
            }
            if (!dealUpdateRequestDto.isValidPrice()) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_PRICE);
            }
            if (!dealUpdateRequestDto.isValidExpireDate()) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_EXPIRED_ON_DATE);
            }
            if (!dealUpdateRequestDto.isValidDeductionValue()) {
                var error = (dealUpdateRequestDto.getDeductionType() == DeductionType.AMOUNT) ?
                        ErrorResponseStatusType.INVALID_DEDUCTION_AMOUNT :
                        ErrorResponseStatusType.INVALID_DEDUCTION_PERCENTAGE;
                return getErrorResponse(error);
            }
            
            // Get existing deal
            Deal deal = dealService.getDeal(dealUpdateRequestDto.getId(), userType);
            
            // Only DRAFT deals can be updated
            if (deal.getApprovalStatus() != ApprovalStatus.DRAFT) {
                log.debug("Attempted to update deal with status: {} for dealId: {}", 
                        deal.getApprovalStatus(), dealUpdateRequestDto.getId());
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_DEAL_UPDATE);
            }
            
            var businessProfile =
                    getBusinessProfile(userType, authToken, dealUpdateRequestDto.getMerchantId());
            if (!businessProfile.isActive()) {
                log.debug("Error updating deal for Inactive merchant for merchant id: {}.",
                        businessProfile.getMerchantId());
                return getErrorResponse(ErrorResponseStatusType.INACTIVE_MERCHANT_FOR_CREATE_DEAL);
            }
            
            BusinessMerchantResponseDto merchant = null;
            if (UserType.BANK.equals(userType)) {
                merchant = authUserService.getMerchantBusinessByMerchantId(authToken, dealUpdateRequestDto.getShopId());
            }
            
            var dealUpdateResponseDto = readAndUpdateResponse(dealUpdateRequestDto, timeZone,
                    businessProfile, userType, merchant, deal);
            
            String objectToJson = dealUpdateResponseDto.toLogJson();
            log.debug("Successfully updated deal for userId: {}, to userType: {}, deal response {}",
                    userId, userType, objectToJson);
            return getSuccessResponse(dealUpdateResponseDto, SuccessResponseStatusType.UPDATE_DEAL);
        } catch (LimitExceededException e) {
            log.error("Subscription plan limit exceeded for updating deal. userId: {}",
                    dealUpdateRequestDto.getMerchantId(), e);
            return getErrorResponse(e.getLimitExceededType().getErrorResponseStatusType());
        } catch (InvalidSubscribeException e) {
            log.error("Subscription plan not found for user id :{}", dealUpdateRequestDto.getMerchantId(), e);
            return getErrorResponse(ErrorResponseStatusType.SUBSCRIPTION_PLAN_NOT_FOUND);
        } catch (InvalidDealException e) {
            log.error("Error when reading deal, dealId {}", dealUpdateRequestDto.getId(), e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_DEAL_ID);
        } catch (InvalidUserException e) {
            log.error("Invalid/ Inactive merchant id :{} for userType :{}. user id :{}",
                    dealUpdateRequestDto.getMerchantId(), userType, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INACTIVE_MERCHANT_FOR_CREATE_DEAL);
        } catch (InvalidCategoryException e) {
            log.error("Reading related category id for user: {} was failed.", userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (InvalidBrandException e) {
            log.error("Error when reading related brand id for user: {}.", userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_BRAND_ID);
        } catch (InvalidDateOptionException e) {
            log.error("Date ranges are not matching with seasonal categories date range. User: {}.", userId, e);
            return getErrorResponse(ErrorResponseStatusType.DEAL_DATES_DOESNT_MATCH_SEASONAL_CATEGORIES);
        } catch (KupiCoreException e) {
            log.error("Updating existing deal was failed. dealId: {}, userId: {}, to userType: {} ",
                    dealUpdateRequestDto.getId(), userId, userType, e);
            return getInternalServerError();
        }
    }

    /**
     * Publish a draft deal (change status from DRAFT to APPROVED).
     * Updates both Deal and DealSearchIndex.
     * Only for MERCHANT deals.
     *
     * @param userId   merchant user id
     * @param timeZone time zone
     * @param dealId   deal ID to publish
     * @return SuccessResponse / ErrorResponse
     */
    @Secured({MERCHANT_ROLE})
    @PutMapping("/MERCHANT/{dealId}/publish")
    public ResponseEntity<ResponseWrapper> publishDraftDeal(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable String dealId) {
        try {
            // Verify deal belongs to the merchant
            Deal deal = dealService.getDeal(dealId, UserType.MERCHANT);
            if (!deal.getMerchantId().equals(userId)) {
                log.error("Deal {} does not belong to merchant {}", dealId, userId);
                return getErrorResponse(ErrorResponseStatusType.INVALID_DEAL_ID);
            }

            // Publish the deal (updates both Deal and DealSearchIndex)
            dealService.publishDraftDeal(dealId);
            
            log.debug("Successfully published draft deal for userId: {}, dealId: {}", userId, dealId);
            return getSuccessResponse(null, SuccessResponseStatusType.PUBLISH_DEAL);
        } catch (CannotRepublishDealException e) {
            log.error("Attempted to re-publish an unpublished deal. dealId: {}, userId: {}", dealId, userId, e);
            return getErrorResponse(ErrorResponseStatusType.CANNOT_REPUBLISH_UNPUBLISHED_DEAL);
        } catch (InvalidDealException e) {
            log.error("Invalid deal or deal is not in DRAFT status. dealId: {}, userId: {}",
                    dealId, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_DEAL_ID);
        } catch (KupiCoreException e) {
            log.error("Publishing draft deal was failed. dealId: {}, userId: {}",
                    dealId, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Unpublish an approved deal (change status from APPROVED to UNPUBLISHED).
     * Updates both Deal and DealSearchIndex.
     * Only for MERCHANT deals.
     * Once unpublished, the deal cannot be published again.
     *
     * @param userId   merchant user id
     * @param timeZone time zone
     * @param dealId   deal ID to unpublish
     * @return SuccessResponse / ErrorResponse
     */
    @Secured({MERCHANT_ROLE})
    @PutMapping("/MERCHANT/{dealId}/unpublish")
    public ResponseEntity<ResponseWrapper> unpublishDeal(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable String dealId) {
        try {
            // Verify deal belongs to the merchant
            Deal deal = dealService.getDeal(dealId, UserType.MERCHANT);
            if (!deal.getMerchantId().equals(userId)) {
                log.error("Deal {} does not belong to merchant {}", dealId, userId);
                return getErrorResponse(ErrorResponseStatusType.INVALID_DEAL_ID);
            }

            // Unpublish the deal (updates both Deal and DealSearchIndex)
            dealService.unpublishDeal(dealId);

            log.debug("Successfully unpublished deal for userId: {}, dealId: {}",
                    userId, dealId);
            return getSuccessResponse(null, SuccessResponseStatusType.UNPUBLISH_DEAL);
        } catch (InvalidDealException e) {
            log.error("Invalid deal or deal is not in APPROVED status. dealId: {}, userId: {}",
                    dealId, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_DEAL_ID);
        } catch (KupiCoreException e) {
            log.error("Unpublishing deal was failed. dealId: {}, userId: {}",
                    dealId, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Get paginated basic deal list
     *
     * @return Deal List Response
     */
    @Secured({ADMIN_ROLE})
    @GetMapping("/{page}/{size}/search/ALL")
    public ResponseEntity<ResponseWrapper> getDealList(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size
    ) {
        try {
            Page<Deal> dealPage = dealService
                    .listAllDeals(generateAPageable(page, size, DEFAULT_NATIVE_SORT, false));
            var dealListResponseDto = createDealListResponseDto(dealPage, timeZone);
            log.debug("Successfully returned all deal list for page: {}, size {} for userId: {}",
                    page, size, userId);
            return getSuccessResponse(dealListResponseDto, SuccessResponseStatusType.READ_DEAL_LIST);
        } catch (KupiCoreException e) {
            log.error("Returning all deal list was failed for page: {}, size {} for userId: {}",
                    page, size, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Get paginated basic deal list
     *
     * @return Deal List Response
     */
    @Secured({ADMIN_ROLE})
    @GetMapping("/{page}/{size}/{userType}/{merchantId}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getDealList(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable UserType userType,
            @PathVariable String merchantId,
            @PathVariable String searchTerm
    ) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "search deal by merchant/bank id");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            // Stable ordering: first by createdAt DESC, then by id DESC
            Pageable pageable = PageRequest.of(page, size,
                    Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
            var dealPage = dealService.searchDeals(pageable, merchantId, searchTerm, userType);
            var dealListResponseDto = new DealListResponseDto(dealPage, timeZone);
            log.debug("Successfully returned search deal list for page: {}, size: {} for userId: {}, userType: {}",
                    page, size, userId, userType);
            return getSuccessResponse(dealListResponseDto, SuccessResponseStatusType.READ_DEAL_LIST);
        } catch (KupiCoreException e) {
            log.error("Returning search deal list was failed for page: {}, size {} for userId: {}, userType: {}",
                    page, size, userId, userType, e);
            return getInternalServerError();
        }
    }

    /**
     * Get paginated deal list with advanced filtering by status, category, promoType, date range, and search
     *
     * @param userId      userId
     * @param timeZone    timeZone
     * @param status      status filter (ALL, active, inactive, Draft, Expired, Deactivated)
     * @param categoryId  category id (ALL or specific)
     * @param promoTypeId promo type id (ALL or specific)
     * @param page        page
     * @param size        size
     * @param userType    userType
     * @param merchantId  merchantId
     * @param searchTerm  searchTerm
     * @param startDate   start date (optional, format: yyyy-MM-dd)
     * @param endDate     end date (optional, format: yyyy-MM-dd)
     * @return Extended Deal List Response with dealStatus, soldCouponCount, and earnings
     */
    @Secured({ADMIN_ROLE})
    @GetMapping("/status/{status}/category/{categoryId}/promoType/{promoTypeId}/{page}/{size}/{userType}/{merchantId}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getDealListWithAdvancedFilter(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable String status,
            @PathVariable String categoryId,
            @PathVariable String promoTypeId,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable UserType userType,
            @PathVariable String merchantId,
            @PathVariable String searchTerm,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "search deal with advanced filter");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }

            // Validate date range - both must be provided together or both null
            if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }

            Long startMillis = null;
            Long endMillis = null;

            if (startDate != null && endDate != null) {
                ZoneId zone = ZoneId.of(timeZone);
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                startMillis = start.atStartOfDay(zone).toInstant().toEpochMilli();
                endMillis = end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1;

                if (startMillis > endMillis) {
                    return getErrorResponse(ErrorResponseStatusType.INVALID_DATE_RANGE);
                }
            }

            // Validate category if not ALL
            if (!categoryId.equals(ALL)) {
                categoryService.validateCategoryId(categoryId);
            }

            // Validate promoType if not ALL
            if (!promoTypeId.equals(ALL)) {
                promoTypeService.getPromoTypeById(promoTypeId);
            }

            // Use unsorted Pageable since native query has explicit ORDER BY clause
            Pageable pageable = PageRequest.of(page, size);

            var dealPage = dealService.searchDealsWithAdvancedFilter(
                    pageable, status, categoryId, promoTypeId, merchantId, searchTerm, userType, startMillis, endMillis);

            // Convert to ExtendedDealResponseDto with dealStatus and earnings
            List<ExtendedDealResponseDto> extendedDeals = new ArrayList<>();
            long currentTime = System.currentTimeMillis();
            long queryStartMillis = startMillis != null ? startMillis : currentTime;
            long queryEndMillis = endMillis != null ? endMillis : currentTime;
            
            // For earnings calculation, if no date range provided, use all time (from 0 to current time)
            long earningsStartMillis = startMillis != null ? startMillis : 0L;
            long earningsEndMillis = endMillis != null ? endMillis : currentTime;

            for (DealSearchIndex dealSearchIndex : dealPage.getContent()) {
                String dealStatus = dealService.calculateDealStatus(dealSearchIndex, queryStartMillis, queryEndMillis);
                BigDecimal earnings = dealService.calculateEarnings(
                        dealSearchIndex.getId(), earningsStartMillis, earningsEndMillis);

                ExtendedDealResponseDto extendedDeal = new ExtendedDealResponseDto(
                        dealSearchIndex, timeZone, earnings, dealStatus);
                extendedDeals.add(extendedDeal);
            }

            ExtendedDealListResponseDto extendedDealListResponseDto =
                    new ExtendedDealListResponseDto(dealPage, extendedDeals);

            log.debug("Successfully returned advanced filtered deal list for page: {}, size: {} for userId: {}, userType: {}",
                    page, size, userId, userType);
            return getSuccessResponse(extendedDealListResponseDto, SuccessResponseStatusType.READ_DEAL_LIST);
        } catch (InvalidCategoryException e) {
            log.error("Invalid category Id: {} for advanced filter search", categoryId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            log.error("Returning advanced filtered deal list was failed for page: {}, size {} for userId: {}, userType: {}",
                    page, size, userId, userType, e);
            return getInternalServerError();
        } catch (Exception e) {
            log.error("Unexpected error in advanced filtered deal list for page: {}, size {} for userId: {}, userType: {}",
                    page, size, userId, userType, e);
            return getInternalServerError();
        }
    }

    /**
     * Get paginated deal list based on approvalStatus
     *
     * @param userId         userId
     * @param timeZone       timeZone
     * @param page           page
     * @param size           size
     * @param approvalStatus Approved/Pending/Rejected
     * @param userType       Merchant/Bank
     * @param merchantId     merchantId
     * @param searchTerm     searchTerm
     * @return Deal List Response
     */
    @GetMapping("/{approvalStatus}/{page}/{size}/{userType}/{merchantId}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getApprovedDealList(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable ApprovalStatus approvalStatus,
            @PathVariable UserType userType,
            @PathVariable String merchantId,
            @PathVariable String searchTerm) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "search deal by merchant/bank id");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            var dealPage =
                    dealService.searchDealsByApprovalStatus(generateAPageable(page, size, DEFAULT_SORT,
                            false), merchantId, approvalStatus, searchTerm, userType);
            var dealListResponseDto = new DealListResponseDto(dealPage, timeZone);
            log.debug("Successfully returned search deal list for approvedStatus: {}, page: {}, size: {} for " +
                    "userId: {}, userType: {}", approvalStatus, page, size, userId, userType);
            return getSuccessResponse(dealListResponseDto, SuccessResponseStatusType.READ_DEAL_LIST);
        } catch (KupiCoreException e) {
            log.error("Returning search deal list was failed for approvedStatus: {}, page: {}, size {} for " +
                    "userId: {}, userType: {}", approvalStatus, page, size, userId, userType, e);
            return getInternalServerError();
        }
    }

    /**
     * Get paginated PENDING deal list
     *
     * @param userId     user Id
     * @param timeZone   timeZone
     * @param userType   BANK/MERCHANT
     * @param page       page number
     * @param size       page size
     * @param searchTerm search term
     * @return pending deal list
     */
    @Secured({ADMIN_ROLE})
    @GetMapping("/{userType}/PENDING/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getPendingDealList(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable UserType userType,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String searchTerm
    ) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "search pending deal");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            Pageable pageable = searchTerm.equals(ALL) ?
                    generateAPageable(page, size, DEFAULT_SORT, false)
                    : generateAPageable(page, size, DEFAULT_NATIVE_SORT, false);
            var dealPage = dealService.getPendingDeals(pageable, searchTerm, userType);
            var dealListResponseDto = new DealListResponseDto(dealPage, timeZone);
            log.debug("Successfully returned pending deal list for page: {}, size: {}, userType: {}, userId: {}",
                    page, size, userType, userId);
            return getSuccessResponse(dealListResponseDto, SuccessResponseStatusType.READ_DEAL_LIST);
        } catch (KupiCoreException e) {
            log.error("Returning pending deal list was failed for page: {}, size: {}, userType: {}, userId: {}",
                    page, size, userType, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Get paginated deal list for merchantId
     *
     * @return Deal List Response
     */
    @GetMapping("/{page}/{size}/merchant/{merchantId}")
    public ResponseEntity<ResponseWrapper> getDealListForMerchant(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String merchantId,
            HttpServletRequest request) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            var businessMerchant =
                    authUserService.getMerchantBusinessByMerchantId(authToken, merchantId);
            var dealPage = dealService
                    .listAllDealsByMerchant(generateAPageable(page, size, DEFAULT_NATIVE_SORT, false), merchantId);
            var dealListResponseDto =
                    new DealListResponseDto(dealPage, new BasicMerchantBusinessResponseDto(businessMerchant), timeZone);
            log.debug("Successfully returned the deal list for merchant Id for page: {}, size {} for userId: {}",
                    page, size, userId);
            return getSuccessResponse(dealListResponseDto, SuccessResponseStatusType.READ_DEAL_LIST);

        } catch (InvalidUserException e) {
            log.error("Invalid merchant Id: {}.", userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (KupiCoreException e) {
            log.error("Returning deal list for merchant Id was failed for page: {}, size {} for userId: {}",
                    page, size, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Soft delete of a deal
     *
     * @param userId   userId
     * @param timeZone timeZone
     * @param userType Merchant/Bank
     * @param dealId   dealId
     * @return SuccessResponse / ErrorResponse
     */
    @Secured({MERCHANT_ROLE})
    @DeleteMapping("/{userType}/{dealId}")
    public ResponseEntity<ResponseWrapper> deleteDeal(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                      @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                      @PathVariable UserType userType,
                                                      @PathVariable String dealId) {

        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "get deal by id");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            dealService.deleteDeal(dealId, userType);
            log.debug("Successfully deal deleted by userId {}, dealId {}, userType {}", userId, dealId, userType);
            return getSuccessResponse(null, SuccessResponseStatusType.DELETE_DEAL);

        } catch (InvalidDealException e) {
            log.error("Error when delete deal, dealId {}", dealId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_DEAL_ID);
        } catch (KupiCoreException e) {
            log.error("Deleting deal. dealId {}, userType {} was failed.", dealId, userType, e);
            return getInternalServerError();
        }
    }

    /**
     * Approve a deal
     *
     * @param userId                             userId
     * @param userType                           BANK/MERCHANT
     * @param dealApprovalStatusUpdateRequestDto dealStatusUpdateRequestDto
     * @return SuccessResponse / ErrorResponse
     */
    @Secured({ADMIN_ROLE})
    @PutMapping("/{userType}/approve")
    public ResponseEntity<ResponseWrapper> approveDeal(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                       @PathVariable UserType userType,
                                                       @RequestBody DealApprovalStatusUpdateRequestDto
                                                               dealApprovalStatusUpdateRequestDto,
                                                       HttpServletRequest request) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "approve deal");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            if (!dealApprovalStatusUpdateRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            dealService.updateApprovalOfDeal(dealApprovalStatusUpdateRequestDto, authToken, userType);
            log.debug("Successfully approved deal by userId {}, userType{}, dealId {}",
                    userId, userType, dealApprovalStatusUpdateRequestDto.getId());
            SuccessResponseStatusType successResponseStatusType =
                    (dealApprovalStatusUpdateRequestDto.getApprovalStatus().equals(ApprovalStatus.APPROVED))
                            ? SuccessResponseStatusType.APPROVE_DEAL : SuccessResponseStatusType.REJECT_DEAL;
            return getSuccessResponse(null, successResponseStatusType);
        } catch (LimitExceededException e) {
            log.error("Subscription plan limit exceeded for approving deal. userId: {}", userId, e);
            return getErrorResponse(e.getLimitExceededType().getErrorResponseStatusType());
        } catch (ExpiredDealApprovalException e) {
            log.error("Unable to approve expired deal of deal id: {} for user type: {}.",
                    dealApprovalStatusUpdateRequestDto.getId(), userType, e);
            return getErrorResponse(ErrorResponseStatusType.EXPIRED_DEAL_APPROVAL);
        } catch (InvalidUserException e) {
            log.error("Reading merchant id for user: {}, userType: {} was failed.", userId, userType, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (InvalidDealException e) {
            log.error("Reading the deal by dealId: {} was failed for: {}",
                    dealApprovalStatusUpdateRequestDto.getId(), dealApprovalStatusUpdateRequestDto.getApprovalStatus(), e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_DEAL_ID);
        } catch (KupiCoreException e) {
            log.error("Approve/Reject deal was failed. dealId {}, userType {}, userId {}.",
                    dealApprovalStatusUpdateRequestDto.getId(), userType, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Read deal by Id
     *
     * @param userId   user Id
     * @param timeZone timeZone
     * @param dealId   request deal Id
     * @return deal response Dto
     */
    @GetMapping("/{userType}/{dealId}")
    public ResponseEntity<ResponseWrapper> getDetailDeal(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable UserType userType,
            @PathVariable String dealId,
            HttpServletRequest request) {

        try {
            log.debug("Calling get deal detail by id api for deal id: {}", dealId);
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "get deal detail");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            var deal = dealService.getDeal(dealId, userType);
            var businessProfile =
                    getBusinessProfile(userType, authToken, deal.getMerchantId());
            var dealOwnerResponseDto = new BasicMerchantBusinessResponseDto(businessProfile);
            DealResponseDto detailDealResponseDto;
            if (userType.equals(UserType.MERCHANT)) {
                detailDealResponseDto = new DealResponseDto(deal, timeZone, dealOwnerResponseDto);
            } else {
                var storeDetailsForBankDeal =
                        getBusinessProfile(UserType.MERCHANT, authToken, deal.getStoreId());
                var storeResponseDto = new BasicMerchantBusinessResponseDto(storeDetailsForBankDeal);
                detailDealResponseDto = new DealResponseDto(deal, timeZone, storeResponseDto, dealOwnerResponseDto);
            }
            // Populate additional analytics fields
            long viewCount = dealViewService.getDealViewCount(dealId);
            long now = System.currentTimeMillis();
            BigDecimal totalEarning = dealService.calculateEarnings(dealId, 0L, now);
            if (totalEarning != null) {
                totalEarning = totalEarning.setScale(2, RoundingMode.HALF_UP);
            }
            detailDealResponseDto.setViewCount(viewCount);
            detailDealResponseDto.setTotalEarning(totalEarning);
            detailDealResponseDto.setDealSource(userType.toString());
            
            // Populate validity periods - use stored periods if available, otherwise reconstruct from coupons
            if (deal.isCouponAvailable()) {
                List<ValidityPeriodDto> validityPeriods = deal.getValidityPeriods();
                // If no stored periods, try to reconstruct from coupons
                if (validityPeriods == null || validityPeriods.isEmpty()) {
                    validityPeriods = couponService.reconstructValidityPeriodsFromCoupons(deal, timeZone);
                }
                detailDealResponseDto.setValidityPeriods(validityPeriods.isEmpty() ? null : validityPeriods);
            }

            Set<String> favoriteDealIds = getFavoriteDealIdsForUser(userId, Collections.singletonList(dealId));
            detailDealResponseDto.setFavorite(favoriteDealIds.contains(dealId));
            
            log.debug("Successfully returned the deal detail for deal id: {}", dealId);
            return getSuccessResponse(detailDealResponseDto, SuccessResponseStatusType.READ_DEAL);
        } catch (InvalidDealException e) {
            log.error("Invalid dealId: {} for user Id: {}", dealId, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_DEAL_ID);
        } catch (KupiCoreException e) {
            log.error("Getting deal detail. dealId: {}, userId: {}, userType: {} was failed.",
                    dealId, userId, userType, e);
            return getInternalServerError();
        }
    }

    /**
     * This method return deal list by category Id, merchant Id, search term
     *
     * @param userId     userId
     * @param timeZone   timeZone
     * @param page       page
     * @param size       size
     * @param categoryId categoryId / ALL
     * @param merchantId merchantId / ALL
     * @param searchTerm searchTerm / ALL
     * @return ResponseEntity Paginated deals list
     */
    @GetMapping("/{page}/{size}/category/{categoryId}/{userType}/{merchantId}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> searchDeals(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String categoryId,
            @PathVariable String merchantId,
            @PathVariable String searchTerm,
            @PathVariable UserType userType
    ) {
        return searchDeals(userId, timeZone,
                generateAPageable(page, size, DEFAULT_NATIVE_SORT, false),
                categoryId, merchantId, searchTerm, false, false, userType);
    }

    /**
     * This method return active deal list for active merchant by category Id, merchant Id, search term.
     *
     * @param userId     userId
     * @param timeZone   timeZone
     * @param page       page
     * @param size       size
     * @param categoryId categoryId
     * @param merchantId merchantId
     * @param searchTerm searchTerm
     * @return Paginated deals list
     */
    @GetMapping("active/{page}/{size}/category/{categoryId}/{userType}/active/{merchantId}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> searchActiveDealsForActiveMerchant(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String categoryId,
            @PathVariable String merchantId,
            @PathVariable String searchTerm,
            @PathVariable UserType userType) {
        return searchDeals(userId, timeZone,
                generateAPageable(page, size, "id", false),
                categoryId, merchantId, searchTerm, true, true, userType);
    }

    /**
     * Get deal validity periods by deal ID
     * Returns all validity periods for a deal, formatted with dates and times
     *
     * @param userId   user Id
     * @param timeZone timeZone
     * @param userType MERCHANT/BANK
     * @param dealId   deal ID
     * @return DealValidityPeriodsResponseDto with formatted validity periods and offer expiration
     */
    @GetMapping("/{userType}/{dealId}/validity-periods")
    public ResponseEntity<ResponseWrapper> getDealValidityPeriods(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable UserType userType,
            @PathVariable String dealId) {
        try {
            log.debug("Calling get deal validity periods api for deal id: {}", dealId);

            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "get deal validity periods");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }

            var deal = dealService.getDeal(dealId, userType);

            List<ValidityPeriodDto> validityPeriods = null;
            if (deal.isCouponAvailable()) {
                validityPeriods = deal.getValidityPeriods();
                if (validityPeriods == null || validityPeriods.isEmpty()) {
                    validityPeriods = couponService.reconstructValidityPeriodsFromCoupons(deal, timeZone);
                }
            }

            if (validityPeriods == null) {
                validityPeriods = new ArrayList<>();
            }

            var responseDto = new DealValidityPeriodsResponseDto(validityPeriods, deal.getExpiredOn(), timeZone);

            log.debug("Successfully returned validity periods for deal id: {}", dealId);
            return getSuccessResponse(responseDto, SuccessResponseStatusType.SUCCESSFULLY_RETURNED_DEAL_VALIDITY_PERIODS);
        } catch (InvalidDealException e) {
            log.error("Invalid dealId: {} for getting validity periods. userId: {}", dealId, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_DEAL_ID);
        } catch (KupiCoreException e) {
            log.error("Getting deal validity periods failed. dealId: {}, userId: {}, userType: {}",
                    dealId, userId, userType, e);
            return getInternalServerError();
        }
    }

    /**
     * Returns Deals according to DealStatus and ActiveMerchant Status.
     *
     * @param userId             user id
     * @param timeZone           time zone
     * @param pageable           pageable
     * @param categoryId         category id
     * @param merchantId         merchant id
     * @param searchTerm         search term
     * @param onlyActiveDeals    true/ false
     * @param onlyActiveMerchant true/ false
     * @return Deal List
     */
    private ResponseEntity<ResponseWrapper> searchDeals(
            String userId,
            String timeZone,
            Pageable pageable,
            String categoryId,
            String merchantId,
            String searchTerm,
            boolean onlyActiveDeals,
            boolean onlyActiveMerchant,
            UserType userType
    ) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "search deals");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            if (!categoryId.equals(ALL)) {
                categoryService.validateCategoryId(categoryId);
            }
            var dealPage = dealService.getAllSearchDeals(pageable, categoryId, merchantId, ALL,
                    searchTerm, onlyActiveDeals, onlyActiveMerchant, userType);
            dealPage = filterDealPageByNonBlockedMerchants(dealPage, userType);
            var dealIdList = dealPage.get().map(DealSearchIndex::getId).collect(Collectors.toList());
            var couponStatisticsList = couponStatisticsService.getCouponStatisticsByDealIdList(dealIdList);
            var dealSearchIndices = dealService.removeCouponNotAvailableDeals(dealPage, couponStatisticsList);

            Set<String> favoriteDealIds = getFavoriteDealIdsForUser(userId, dealSearchIndices.getContent().stream()
                    .map(DealSearchIndex::getId).collect(Collectors.toList()));
            var dealListResponseDto = new DealListResponseDto(dealSearchIndices, timeZone, couponStatisticsList, favoriteDealIds);
            log.debug("Successfully returned deal list for mobile users for " + LOG_MESSAGE, pageable.getPageNumber(),
                    pageable.getPageSize(), categoryId, merchantId, searchTerm, userId);
            return getSuccessResponse(dealListResponseDto, SuccessResponseStatusType.READ_DEAL_LIST);
        } catch (InvalidCategoryException e) {
            log.error("Invalid category Id for related categories. " + LOG_MESSAGE, pageable.getPageNumber(),
                    pageable.getPageSize(), categoryId, merchantId, searchTerm, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            log.error("Returning deal list for mobile users was failed for " + LOG_MESSAGE, pageable.getPageNumber(),
                    pageable.getPageSize(), categoryId, merchantId, searchTerm, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Returns Deals according to DealStatus and ActiveMerchant Status with time-based filtering.
     *
     * @param userId             user id
     * @param timeZone           time zone
     * @param pageable           pageable
     * @param categoryId         category id
     * @param merchantId         merchant id
     * @param searchTerm         search term
     * @param onlyActiveDeals    true/ false
     * @param onlyActiveMerchant true/ false
     * @return Deal List with time-based filtering
     */
    private ResponseEntity<ResponseWrapper> searchTimeBasedDeals(
            String userId,
            String timeZone,
            Pageable pageable,
            String categoryId,
            String merchantId,
            String searchTerm,
            boolean onlyActiveDeals,
            boolean onlyActiveMerchant,
            UserType userType
    ) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "search time-based deals");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            if (!categoryId.equals(ALL)) {
                categoryService.validateCategoryId(categoryId);
            }
            var dealPage = dealService.getAllTimeBasedSearchDeals(pageable, categoryId, merchantId, ALL,
                    searchTerm, onlyActiveDeals, onlyActiveMerchant, userType, timeZone);
            dealPage = filterDealPageByNonBlockedMerchants(dealPage, userType);
            var dealIdList = dealPage.get().map(DealSearchIndex::getId).collect(Collectors.toList());
            var couponStatisticsList = couponStatisticsService.getCouponStatisticsByDealIdList(dealIdList);
            var dealSearchIndices = dealService.removeCouponNotAvailableDeals(dealPage, couponStatisticsList);
            var dealListResponseDto = new DealListResponseDto(dealSearchIndices, timeZone, couponStatisticsList);
            log.debug("Successfully returned time-based deal list for mobile users for " + LOG_MESSAGE, pageable.getPageNumber(),
                    pageable.getPageSize(), categoryId, merchantId, searchTerm, userId);
            return getSuccessResponse(dealListResponseDto, SuccessResponseStatusType.READ_DEAL_LIST);
        } catch (InvalidCategoryException e) {
            log.error("Invalid category Id for related categories. " + LOG_MESSAGE, pageable.getPageNumber(),
                    pageable.getPageSize(), categoryId, merchantId, searchTerm, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            log.error("Returning time-based deal list for mobile users was failed for " + LOG_MESSAGE, pageable.getPageNumber(),
                    pageable.getPageSize(), categoryId, merchantId, searchTerm, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method return recently expire deals by category Id, merchant Id, search term
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
    @GetMapping("/recently-expire/{page}/{size}/category/{categoryId}/{userType}/{merchantId}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getRecentlyExpireDeals(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String categoryId,
            @PathVariable String merchantId,
            @PathVariable UserType userType,
            @PathVariable String searchTerm) {
        return getRecentlyExpireDeals(
                userId, timeZone, generateAPageable(page, size, EXPIRED_ON_SORT, true),
                categoryId, merchantId, searchTerm, false, false, userType);
    }

    /**
     * This method return recently expire deals by category Id, merchant Id, search term for mobile
     *
     * @param userId     userId
     * @param timeZone   timeZone
     * @param page       page
     * @param size       size
     * @param categoryId categoryId
     * @param merchantId merchantId
     * @param searchTerm searchTerm
     * @return deal List
     */
    @GetMapping("/recently-expire/{page}/{size}/category/{categoryId}/{userType}/active/{merchantId}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getRecentlyExpireDealsForMobile(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String categoryId,
            @PathVariable String merchantId,
            @PathVariable UserType userType,
            @PathVariable String searchTerm) {
        return getRecentlyExpireDeals(
                userId, timeZone, generateAPageable(page, size, EXPIRED_ON_SORT, true),
                categoryId, merchantId, searchTerm, true, true, userType);
    }

    /**
     * This method return recently expire deals by category Id, merchant Id, search term  and merchant active status.
     *
     * @param userId             user id
     * @param timeZone           time zone
     * @param pageable           pageable
     * @param categoryId         category id
     * @param merchantId         merchant id
     * @param searchTerm         search term
     * @param onlyActiveMerchant merchant active status true/ false
     * @return Deal List
     */
    private ResponseEntity<ResponseWrapper> getRecentlyExpireDeals(
            String userId,
            String timeZone,
            Pageable pageable,
            String categoryId,
            String merchantId,
            String searchTerm,
            boolean onlyActiveDeal,
            boolean onlyActiveMerchant,
            UserType userType
    ) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "get recently expired deal");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            if (!categoryId.equals(ALL)) {
                categoryService.validateCategoryId(categoryId);
            }
            var dealPage = dealService.getRecentlyExpireDeals(pageable, categoryId, merchantId, ALL,
                    searchTerm, userType);
            dealPage = filterDealPageByNonBlockedMerchants(dealPage, userType);
            var dealIdList = dealPage.get().map(DealSearchIndex::getId).collect(Collectors.toList());
            var couponStatisticsList = couponStatisticsService.getCouponStatisticsByDealIdList(dealIdList);
            var dealSearchIndices = dealService.removeCouponNotAvailableDeals(dealPage, couponStatisticsList);

            Set<String> favoriteDealIds = getFavoriteDealIdsForUser(userId, dealSearchIndices.getContent().stream()
                    .map(DealSearchIndex::getId).collect(Collectors.toList()));
            var dealListResponseDto = new DealListResponseDto(dealSearchIndices, timeZone, couponStatisticsList, favoriteDealIds);
            log.debug("Successfully returned  recently expire deals  for " +
                            LOG_MESSAGE, pageable.getPageNumber(),
                    pageable.getPageSize(), categoryId, merchantId, searchTerm, userId);
            return getSuccessResponse(dealListResponseDto, SuccessResponseStatusType.READ_DEAL_LIST);
        } catch (InvalidCategoryException e) {
            log.error("Invalid category Id for related categories. " + LOG_MESSAGE, pageable.getPageNumber(),
                    pageable.getPageSize(), categoryId, merchantId, searchTerm, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            log.error("Returning recently expire deals was failed. " + LOG_MESSAGE, pageable.getPageNumber(),
                    pageable.getPageSize(), categoryId, merchantId, searchTerm, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Filters a deal page to only include deals whose merchant/bank is not blocked (approved and active in auth service).
     * Ensures blocked merchants do not appear in mobile app deal listings.
     *
     * @param dealPage page of deals from search
     * @param userType MERCHANT or BANK
     * @return page containing only deals from non-blocked merchants/banks; same page if empty
     */
    private Page<DealSearchIndex> filterDealPageByNonBlockedMerchants(Page<DealSearchIndex> dealPage, UserType userType) {
        if (dealPage == null || !dealPage.hasContent()) {
            return dealPage;
        }
        List<String> merchantIds = dealPage.getContent().stream()
                .map(DealSearchIndex::getMerchantId)
                .distinct()
                .collect(Collectors.toList());
        Set<String> eligible = authUserService.getEligibleMerchantIds(merchantIds, userType);
        List<DealSearchIndex> filtered = dealPage.getContent().stream()
                .filter(d -> eligible.contains(d.getMerchantId()))
                .collect(Collectors.toList());
        return new PageImpl<>(filtered, dealPage.getPageable(), dealPage.getTotalElements());
    }

    /**
     * Returns the set of deal IDs that are marked as favorite by the user for the given deal list.
     *
     * @param userId  user id (may be null)
     * @param dealIds list of deal ids to check
     * @return set of deal ids that are favorite for the user; empty set if userId is null or dealIds empty
     */
    private Set<String> getFavoriteDealIdsForUser(String userId, List<String> dealIds) {
        if (userId == null || userId.isBlank() || dealIds == null || dealIds.isEmpty()) {
            return Collections.emptySet();
        }
        var favoriteDeals = favouriteDealService.getFavouriteDealsForUserAndDealIds(userId, dealIds);
        if (favoriteDeals == null || favoriteDeals.isEmpty()) {
            return Collections.emptySet();
        }
        return favoriteDeals.stream().map(FavouriteDeal::getDealId).collect(Collectors.toSet());
    }

    /**
     * This method return deal list  by category id & search term
     *
     * @param userId     userId
     * @param page       page
     * @param size       size
     * @param categoryId categoryId
     * @param searchTerm searchTerm / ALL
     * @return ResponseEntity Paginated deals list
     */
    @GetMapping("/{userType}/category/{categoryId}/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getDealsByCategoryId(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String categoryId,
            @PathVariable UserType userType,
            @PathVariable String searchTerm) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "search deals by category id");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            categoryService.validateCategoryId(categoryId);
            var dealPage =
                    dealService.getAllSearchDeals(
                            generateAPageable(page, size, DEFAULT_NATIVE_SORT, false),
                            categoryId, ALL, ALL, searchTerm, false, false, userType);
            var dealListResponseDto = new DealListResponseDto(dealPage, timeZone);
            log.debug("Successfully return the searchDeal list for page: {}, size: {}, userType: {}, userId:{}, " +
                    "categoryId: {} and searchTerm: {}", page, size, userType, userId, categoryId, searchTerm);
            return getSuccessResponse(dealListResponseDto, SuccessResponseStatusType.READ_DEAL_LIST);

        } catch (InvalidCategoryException e) {
            log.error("Invalid category Id: {} for get deals by category id for page: {}, size: {}, userType: {}," +
                    " userId: {}, searchTerm: {}.", categoryId, page, size, userType, userId, searchTerm, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            log.error("Returning deal list by Category Id was failed for page: {}, size: {}, userType: {}, " +
                            "userId:{}, categoryId: {} and searchTerm: {}", page, size, userType, userId, categoryId,
                    searchTerm, e);
            return getInternalServerError();
        }
    }

    /**
     * This method return deal list  by brand id & search term
     *
     * @param userId     userId
     * @param timeZone   time zone
     * @param page       page
     * @param size       size
     * @param brandId    brand id
     * @param searchTerm search term
     * @return ResponseEntity Paginated deals list
     */
    @GetMapping("/{userType}/brand/{brandId}/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getDealsByBrandId(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String brandId,
            @PathVariable UserType userType,
            @PathVariable String searchTerm) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "search deals by brand id");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            brandService.validateBrandId(brandId);
            var dealPage = dealService.getAllSearchDeals(
                    generateAPageable(page, size, DEFAULT_NATIVE_SORT, false),
                    ALL, ALL, brandId, searchTerm, false, false, userType);
            var dealListResponseDto = new DealListResponseDto(dealPage, timeZone);
            log.debug("Successfully return the Deal search list for page: {}, size: {}, userType: {}, userId:{}, " +
                    "brandId: {} and searchTerm: {}", page, size, userType, userId, brandId, searchTerm);
            return getSuccessResponse(dealListResponseDto, SuccessResponseStatusType.READ_DEAL_LIST);

        } catch (InvalidBrandException e) {
            log.error("Invalid brand Id: {} for get deals by brand id for page: {}, size: {}, userType: {}, " +
                    "userId: {}, searchTerm: {}.", brandId, page, size, userType, userId, searchTerm, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_BRAND_ID);
        } catch (KupiCoreException e) {
            log.error("Returning deal list by brand Id was failed for page: {}, size {}, brandId: {}, " +
                    "userType: {}, userId: {} and searchTerm: {}", page, size, brandId, userType, userId, searchTerm, e);
            return getInternalServerError();
        }
    }

    /**
     * Get active deal counts for all categories
     *
     * @param userId   user id
     * @param timeZone time zone
     * @return ResponseEntity with list of categories and their active deal counts
     */
    @GetMapping("/category/dealcounts")
    public ResponseEntity<ResponseWrapper> getCategoryDealCounts(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone) {
        try {
            // Get all active categories
            var activeCategoriesPage = categoryService.getActiveCategories(
                    PageRequest.of(0, Integer.MAX_VALUE), ALL);
            List<Category> categories = activeCategoriesPage.getContent();

            // Get category IDs
            List<String> categoryIds = categories.stream()
                    .map(Category::getId)
                    .collect(Collectors.toList());

            // Get active deal counts for all categories
            Map<String, Long> categoryDealCountMap = dealSearchService.getActiveDealsCountByCategoryIds(categoryIds);

            // Build response DTOs
            List<CategoryDealCountResponseDto> categoryDealCountList = categories.stream()
                    .map(category -> {
                        long dealCount = categoryDealCountMap.getOrDefault(category.getId(), 0L);
                        return new CategoryDealCountResponseDto(
                                category.getId(),
                                category.getName(),
                                category.getImageUrl(),
                                dealCount
                        );
                    })
                    .collect(Collectors.toList());

            var responseDto = new CategoryDealCountListResponseDto(categoryDealCountList);
            log.debug("Successfully returned category deal counts for userId: {}", userId);
            return getSuccessResponse(responseDto, SuccessResponseStatusType.READ_CATEGORY_DEAL_COUNTS);
        } catch (KupiCoreException e) {
            log.error("Returning category deal counts was failed for userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get active deals list for bankId & merchantId.
     *
     * @param userId     userId
     * @param timeZone   timeZone
     * @param page       page
     * @param size       size
     * @param bankId     bankId
     * @param merchantId merchantId
     * @return active deals list.
     */
    @GetMapping("/active/{page}/{size}/bank/{bankId}/merchant/{merchantId}")
    public ResponseEntity<ResponseWrapper> getActiveDealsByBankIdAndMerchantId(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String bankId,
            @PathVariable String merchantId,
            HttpServletRequest request) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            var merchantProfile = getBusinessProfile(UserType.MERCHANT, authToken, merchantId);
            var bankProfile = getBusinessProfile(UserType.BANK, authToken, bankId);
            if (!merchantProfile.isActive()) {
                return getErrorResponse(ErrorResponseStatusType.INACTIVE_MERCHANT);
            }
            Pageable pageable = generateAPageable(page, size, DEFAULT_SORT, false);
            Page<BankDeal> dealPage =
                    dealService.getActiveDealsForActiveBankAndMerchant(pageable, bankId, merchantId);
            var merchantBusinessResponseDto = new BasicMerchantBusinessResponseDto(merchantProfile);
            var bankBusinessResponseDto = new BasicMerchantBusinessResponseDto(bankProfile);
            var dealListResponseDto = new DealListResponseDto(merchantBusinessResponseDto, bankBusinessResponseDto,
                    dealPage, timeZone);
            log.debug("Successfully returned active deals list for bankId: {} and for merchantId: {}. " +
                    "page: {}, size: {}", bankId, merchantId, page, size);
            return getSuccessResponse(dealListResponseDto, SuccessResponseStatusType.READ_DEAL_LIST);
        } catch (InvalidUserException e) {
            log.debug("Invalid merchantId: {}", merchantId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (KupiCoreException e) {
            log.error("Returning active deals for active bank & merchant was failed. " +
                            "userId: {}, bankId: {}, merchantId: {}, page: {}, size: {}.",
                    userId, bankId, merchantId, page, size, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get deals by bulk merchant ids.
     *
     * @param bulkUserRequestDto bulkUserRequestDto
     * @return merchant & deal list.
     */
    @PostMapping("/bulk-info")
    public ResponseEntity<ResponseWrapper> getDealsForBulkUser(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                               @RequestBody BulkUserRequestDto bulkUserRequestDto) {
        try {
            if (!bulkUserRequestDto.isRequiredAvailable())
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            if (!bulkUserRequestDto.isRequestSizeAcceptable())
                return getErrorResponse(ErrorResponseStatusType.MAX_USER_ID_LIMIT_EXCEEDED);
            List<String> merchantIds = bulkUserRequestDto.getUserIds();
            List<DealSearchIndex> dealSearchIndexList = dealService.getDealsForBulkMerchantIds(merchantIds, timeZone);
            var dealIdList = dealSearchIndexList.stream()
                    .map(DealSearchIndex::getId)
                    .collect(Collectors.toList());
            var couponStatisticsList = couponStatisticsService.getCouponStatisticsByDealIdList(dealIdList);
            Page<DealSearchIndex> dealPage = new PageImpl<>(dealSearchIndexList);
            var filteredDealPage = dealService.removeCouponNotAvailableDeals(dealPage, couponStatisticsList);
            List<DealSearchIndex> filteredDealSearchIndexList = filteredDealPage.getContent();
            
            Map<String, MerchantBusinessResponseDto> merchantBusinessResponseDtoMap =
                    authUserService.getMerchantMap(bulkUserRequestDto, UserType.MERCHANT);
            BulkMerchantDealsResponseDto bulkMerchantDealsResponseDto =
                    new BulkMerchantDealsResponseDto(filteredDealSearchIndexList, merchantBusinessResponseDtoMap);
            return getSuccessResponse(bulkMerchantDealsResponseDto,
                    SuccessResponseStatusType.RETURNED_DEALS_BY_MERCHANT_IDS);
        } catch (KupiCoreException e) {
            log.error("Getting deals for bulk merchant was failed. ", e);
            return getInternalServerError();
        }
    }

    /**
     * Returns DealListResponseDto for the given deal IDs (used by location service for nearby filtered deals).
     * Preserves the order of requested IDs. Pagination metadata uses the provided totalElements.
     *
     * @param ids           ordered list of deal IDs
     * @param page          page number (0-indexed)
     * @param size          page size
     * @param totalElements total number of items across all pages (for pagination metadata)
     * @param timeZone      time zone header
     * @return DealListResponseDto with deals for the requested IDs and pagination
     */
    @GetMapping("/by-ids")
    public ResponseEntity<ResponseWrapper> getDealsByIds(
            @RequestHeader(name = USER_ID_HEADER, required = false) String userId,
            @RequestParam("ids") List<String> ids,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) int size,
            @RequestParam("totalElements") @Min(0) long totalElements,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone) {
        try {
            if (ids == null || ids.isEmpty()) {
                Page<DealSearchIndex> emptyPage = new PageImpl<>(Collections.emptyList(),
                        PageRequest.of(page, size), 0L);
                return getSuccessResponse(new DealListResponseDto(emptyPage, timeZone),
                        SuccessResponseStatusType.READ_DEAL_LIST);
            }
            List<DealSearchIndex> list = dealSearchService.getDealListByDealIdList(ids);
            Map<String, DealSearchIndex> byId = list.stream().collect(Collectors.toMap(DealSearchIndex::getId, d -> d));
            List<DealSearchIndex> ordered = ids.stream()
                    .map(byId::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            List<String> orderedIds = ordered.stream().map(DealSearchIndex::getId).collect(Collectors.toList());
            List<CouponStatistics> couponStatisticsList = couponStatisticsService.getCouponStatisticsByDealIdList(orderedIds);
            Page<DealSearchIndex> dealPage = new PageImpl<>(ordered, PageRequest.of(page, size), totalElements);
            Page<DealSearchIndex> filteredDealPage = dealService.removeCouponNotAvailableDeals(dealPage, couponStatisticsList);
            // Preserve original totalElements: removeCouponNotAvailableDeals returns PageImpl(content) so total is lost
            Page<DealSearchIndex> pageWithCorrectTotal = new PageImpl<>(
                    filteredDealPage.getContent(),
                    PageRequest.of(page, size),
                    totalElements);
            Set<String> favoriteDealIds = getFavoriteDealIdsForUser(userId, orderedIds);
            DealListResponseDto dealListResponseDto = new DealListResponseDto(pageWithCorrectTotal, timeZone, couponStatisticsList, favoriteDealIds);
            return getSuccessResponse(dealListResponseDto, SuccessResponseStatusType.READ_DEAL_LIST);
        } catch (KupiCoreException e) {
            log.error("Getting deals by IDs failed.", e);
            return getInternalServerError();
        }
    }

    /**
     * This method creates deal response dto
     *
     * @param dealPage dealPage
     * @return DealListResponseDto
     */
    private DealListResponseDto createDealListResponseDto(Page<Deal> dealPage,
                                                          String timeZone) {
        List<String> userIds = new ArrayList<>();
        dealPage.getContent().forEach(deal -> userIds.add(deal.getMerchantId()));
        if (!dealPage.getContent().isEmpty()) {
            var bulkUserRequestDto = new BulkUserRequestDto(userIds);
            var bulkUserResponseMap = authUserService.
                    getMerchantMap(bulkUserRequestDto, UserType.MERCHANT);
            return new DealListResponseDto(dealPage, bulkUserResponseMap, timeZone);
        }
        return new DealListResponseDto(dealPage, new HashMap<>(), timeZone);
    }

    /**
     * This method create deal response dto
     *
     * @param dealRequestDto dealRequestDto
     * @param timeZone       time zone
     * @return Deal response Dto
     */
    private DealResponseDto readAndCreateResponse(DealRequestDto dealRequestDto, String timeZone,
                                                  BusinessMerchantResponseDto businessMerchantResponseDto,
                                                  UserType userType, BusinessMerchantResponseDto merchant, String dealCode) {
        if (!userType.toString().equalsIgnoreCase(businessMerchantResponseDto.getProfileType()))
            throw new InvalidUserException("User type doesn't match with user id.");
        subscriberService.checkSubscriptionPlanForDeal(dealRequestDto.getMerchantId());
        Set<Brand> brands = new HashSet<>();
        if (dealRequestDto.isBrandIdsAvailable()) {
            brands.addAll(brandService.getBrandSetByIdList(dealRequestDto.getBrandIds()));
        }
        var categories = categoryService.getCategorySetByIds(dealRequestDto.getCategoryIds());
        checkDealDateWithSeasonalCategories(dealRequestDto, categories);
        var merchantResponseDto = new BasicMerchantBusinessResponseDto(businessMerchantResponseDto);
        PromoType promoType = null;
        if (dealRequestDto.getPromoTypeId() != null && !dealRequestDto.getPromoTypeId().trim().isEmpty()) {
            promoType = promoTypeService.getPromoTypeById(dealRequestDto.getPromoTypeId());
        }
        var deal = new Deal(dealRequestDto, brands, categories, promoType, dealCode);
        dealService.createDeal(deal, merchantResponseDto, userType, dealRequestDto, merchant);
        
        // Create coupons from validity periods if provided
        if (dealRequestDto.getValidityPeriods() != null && !dealRequestDto.getValidityPeriods().isEmpty() 
                && deal.isCouponAvailable()) {
            couponService.createCouponsFromValidityPeriods(deal, dealRequestDto.getValidityPeriods(), 
                    timeZone, userType);
        }
        
        // Try to reload deal to get updated CouponStatistics relationship
        // If reload fails (e.g., in tests), use the original deal object
        Deal dealForResponse = deal;
        try {
            Deal reloadedDeal = dealService.getDeal(deal.getId(), userType);
            if (reloadedDeal != null) {
                dealForResponse = reloadedDeal;
            }
        } catch (Exception e) {
            log.debug("Failed to reload deal after creation, using original deal object: {}", e.getMessage());
            // Continue with original deal object
        }
        
        var dealResponseDto = new DealResponseDto(dealForResponse, timeZone, merchantResponseDto);
        
        // Populate validity periods - use stored periods if available, otherwise reconstruct from coupons
        if (dealForResponse.isCouponAvailable()) {
            List<ValidityPeriodDto> validityPeriods = dealForResponse.getValidityPeriods();
            // If no stored periods, try to reconstruct from coupons
            if (validityPeriods == null || validityPeriods.isEmpty()) {
                validityPeriods = couponService.reconstructValidityPeriodsFromCoupons(dealForResponse, timeZone);
            }
            dealResponseDto.setValidityPeriods(validityPeriods.isEmpty() ? null : validityPeriods);
        }
        
        return dealResponseDto;
    }

    /**
     * This method update deal response dto
     *
     * @param dealUpdateRequestDto dealUpdateRequestDto
     * @param timeZone             time zone
     * @return Deal response Dto
     */
    private DealResponseDto readAndUpdateResponse(DealUpdateRequestDto dealUpdateRequestDto, String timeZone,
                                                  BusinessMerchantResponseDto businessMerchantResponseDto,
                                                  UserType userType, BusinessMerchantResponseDto merchant, Deal existingDeal) {
        if (!userType.toString().equalsIgnoreCase(businessMerchantResponseDto.getProfileType()))
            throw new InvalidUserException("User type doesn't match with user id.");
        subscriberService.checkSubscriptionPlanForDeal(dealUpdateRequestDto.getMerchantId());
        Set<Brand> brands = new HashSet<>();
        if (dealUpdateRequestDto.isBrandIdsAvailable()) {
            brands.addAll(brandService.getBrandSetByIdList(dealUpdateRequestDto.getBrandIds()));
        }
        var categories = categoryService.getCategorySetByIds(dealUpdateRequestDto.getCategoryIds());
        checkDealDateWithSeasonalCategories(dealUpdateRequestDto, categories);
        var merchantResponseDto = new BasicMerchantBusinessResponseDto(businessMerchantResponseDto);
        var updateDeal = dealService.updateDeal(dealUpdateRequestDto, userType, existingDeal, businessMerchantResponseDto);
        
        // Handle coupon updates intelligently: only for MERCHANT deals
        // Intelligently updates coupons based on validity periods:
        // - Keeps existing coupons that match new periods
        // - Removes coupons for periods no longer in request
        // - Adds coupons for new periods
        // - Adjusts counts when coupon counts change
        if (userType == UserType.MERCHANT && updateDeal.isCouponAvailable()) {
            couponService.updateCouponsFromValidityPeriods(updateDeal, dealUpdateRequestDto.getValidityPeriods(), 
                    timeZone, userType);
            // Reload deal to get updated CouponStatistics relationship
            updateDeal = dealService.getDeal(updateDeal.getId(), userType);
        }
        
        var dealResponseDto = new DealResponseDto(updateDeal, timeZone, merchantResponseDto);
        
        // Populate validity periods - use stored periods if available, otherwise reconstruct from coupons
        if (updateDeal.isCouponAvailable()) {
            List<ValidityPeriodDto> validityPeriods = updateDeal.getValidityPeriods();
            // If no stored periods, try to reconstruct from coupons
            if (validityPeriods == null || validityPeriods.isEmpty()) {
                validityPeriods = couponService.reconstructValidityPeriodsFromCoupons(updateDeal, timeZone);
            }
            dealResponseDto.setValidityPeriods(validityPeriods.isEmpty() ? null : validityPeriods);
        }
        
        return dealResponseDto;
    }

    /**
     * This method will check if the deal validity period is in the range of seasonal category validity period.
     *
     * @param dealRequestDto dealRequestDto
     * @param categorySet    categorySet
     */
    private void checkDealDateWithSeasonalCategories(DealRequestDto dealRequestDto, Set<Category> categorySet) {
        Set<Category> seasonalCategories = categorySet
                .stream()
                .filter(category -> category.getCategoryType().equals(CategoryType.SEASONAL))
                .collect(Collectors.toSet());
        for (Category category : seasonalCategories) {
            if (dealRequestDto.getValidFrom() < category.getStartDate() ||
                    dealRequestDto.getExpiredOn() > category.getExpiryDate()) {
                throw new InvalidDateOptionException(
                        ErrorResponseStatusType.DEAL_DATES_DOESNT_MATCH_SEASONAL_CATEGORIES.getMessage() +
                                CATEGORY_ID + category.getId());
            }
        }
    }

    /**
     * @param page            page
     * @param size            size
     * @param sortBy          sortBy
     * @param isAscendingTrue true/ false
     * @return pageable
     */
    private Pageable generateAPageable(int page, int size, String sortBy, boolean isAscendingTrue) {
        return (isAscendingTrue) ? PageRequest.of(page, size, Sort.by(sortBy).ascending())
                : PageRequest.of(page, size, Sort.by(sortBy).descending());
    }

    /**
     * This method returns active deals for active merchants from time stamp
     *
     * @param userId     userId
     * @param timeZone   time zone
     * @param page       page
     * @param size       size
     * @param time       time stamp
     * @param userType   MERCHANT/BANK
     * @param merchantId merchantId
     * @return ResponseEntity active merchant list
     */
    @GetMapping("/active/{time}/{page}/{size}/{userType}/{merchantId}")
    public ResponseEntity<ResponseWrapper> getActiveMerchantDealsFromTimestamp(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable long time,
            @PathVariable UserType userType,
            @PathVariable String merchantId
    ) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "get active merchant deals from timestamp");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }


            Pageable pageable = PageRequest.of(page, size);
            var activeMerchantDealsPage = dealService
                    .getActiveMerchantDeals(pageable, time, merchantId);
            var dealListResponseDto = new DealListResponseDto(activeMerchantDealsPage, timeZone);
            log.debug("Successfully returned active merchant deals from time stamp for userId: {}, page: {}, size: {}, " +
                    "time: {}, userType: {}, merchantId: {}", userId, page, size, time, userType, merchantId);
            return getSuccessResponse(dealListResponseDto, SuccessResponseStatusType.READ_DEAL_LIST);
        } catch (KupiCoreException e) {
            log.error("Getting active merchant deals from time stamp was failed for userId: {}, page: {}, size: {}, " +
                    "time: {}, userType: {}, merchantId: {}", userId, page, size, time, userType, merchantId, e);
            return getInternalServerError();
        }
    }

    /**
     * Get paginated active favourite deals for the authenticated user.
     * <p>
     * This returns only deals that are:
     * - not deleted
     * - APPROVED
     * - merchant is active (approved and not blocked)
     * - currently within the validFrom / expiredOn window
     *
     * @param userId   user id from header
     * @param timeZone time zone from header
     * @param page     page number (0-based)
     * @param size     page size
     * @return DealListResponseDto containing active favourite deals with pagination
     */
    @GetMapping("/favourites/{page}/{size}")
    public ResponseEntity<ResponseWrapper> getActiveFavouriteDealsForUser(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size) {
        try {
            DealListResponseDto responseDto =
                    favouriteDealService.getActiveFavouriteDealsForUser(userId, timeZone, page, size);
            log.debug("Successfully returned active favourite deals for userId: {}, page: {}, size: {}", userId, page, size);
            return getSuccessResponse(responseDto, SuccessResponseStatusType.READ_DEAL_LIST);
        } catch (KupiCoreException e) {
            log.error("Failed to return active favourite deals for userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns deal summary for a merchant
     *
     * @param userId     userId
     * @param userType   MERCHANT
     * @param merchantId merchantId
     * @return Deal summary ResponseEntity
     */
    @GetMapping("/summary/{userType}/{merchantId}")
    public ResponseEntity<ResponseWrapper> getDealSummaryForMerchant(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                                     @PathVariable UserType userType,
                                                                     @PathVariable String merchantId) {
        if (userType != UserType.MERCHANT)
            return getErrorResponse(ErrorResponseStatusType.INVALID_USER_ID_OR_TYPE);
        try {
            var dealSummary = dealService.getDealSummaryForMerchant(merchantId);
            DealSummaryResponseDto dealSummaryResponseDto = new DealSummaryResponseDto(dealSummary, merchantId);
            log.debug("Successfully returned the deals summary for userId: {}, userType: {}, merchantId: {}",
                    userId, userType, merchantId);
            return getSuccessResponse(dealSummaryResponseDto, SuccessResponseStatusType.READ_DEAL_SUMMARY_FOR_MERCHANT);
        } catch (KupiCoreException e) {
            log.error("Getting deals summary for merchant was failed for userId: {}, userType: {}, merchantId: {}",
                    userId, userType, merchantId, e);
            return getInternalServerError();
        }
    }
}
