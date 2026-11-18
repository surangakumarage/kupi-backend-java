package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.Coupon;
import com.kupi.cc.base.domain.entity.CouponStatistics;
import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.request.CouponFileUploadRequestDto;
import com.kupi.cc.base.domain.request.RedeemRequestDto;
import com.kupi.cc.base.domain.request.UploadedCouponFileDetailRequestDto;
import com.kupi.cc.base.domain.response.*;
import com.kupi.cc.base.enums.*;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.service.AuthUserService;
import com.kupi.cc.base.service.CouponService;
import com.kupi.cc.base.service.CouponStatisticDetailService;
import com.kupi.cc.base.service.DealService;
import com.kupi.cc.base.service.DealSearchService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Coupon Controller
 */
@Slf4j
@RestController
@Validated
@RequestMapping("api/v1/coupons")
public class CouponController extends Controller {

    private final CouponService couponService;
    private final DealService dealService;
    private final AuthUserService authUserService;
    private final int dateGaps;
    private final CouponStatisticDetailService couponStatisticDetailService;
    private final DealSearchService dealSearchService;

    public CouponController(Translator translator, CouponService couponService, DealService dealService,
                            AuthUserService authUserService,
                            @Value("${couponsUpload.dealExpiryDateValidationDateGap}") int dateGaps,
                            CouponStatisticDetailService couponStatisticDetailService,
                            DealSearchService dealSearchService) {
        super(translator);
        this.couponService = couponService;
        this.dealService = dealService;
        this.authUserService = authUserService;
        this.dateGaps = dateGaps;
        this.couponStatisticDetailService = couponStatisticDetailService;
        this.dealSearchService = dealSearchService;
    }

    /**
     * This method list coupon statistics.
     *
     * @param page page
     * @param size size
     * @return coupon statistics list.
     */
    @GetMapping("/coupon-stat/{page}/{size}")
    public ResponseEntity<ResponseWrapper> listCouponStatistics(@Min(DEFAULT_PAGE) @PathVariable int page,
                                                                @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CouponStatistics> couponStatisticsPage = couponService.listCoupons(pageable);
            CouponStatisticListResponseDto couponStatisticListResponseDto =
                    new CouponStatisticListResponseDto(couponStatisticsPage);
            log.debug("Successfully returned coupon statistics.");
            return getSuccessResponse(couponStatisticListResponseDto,
                    SuccessResponseStatusType.SUCCESSFULLY_RETURNED_COUPON_STAT);
        } catch (KupiCoreException e) {
            log.error("Failed to return coupon statistics.", e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to list deals with coupons.
     *
     * @param timezone   timezone
     * @param userId     userId
     * @param page       page
     * @param size       size
     * @param searchTerm searchTerm
     * @return list of deals with coupons.
     */
    @GetMapping("/{userType}/{userId}/{page}/{size}/search/{searchTerm}/filter/{filter}")
    public ResponseEntity<ResponseWrapper> listDealsWithCoupons(@RequestHeader(name = TIME_ZONE_HEADER) String timezone,
                                                                @PathVariable String userId,
                                                                @PathVariable("userType") UserType userType,
                                                                @Min(DEFAULT_PAGE) @PathVariable int page,
                                                                @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
                                                                @PathVariable String searchTerm,
                                                                @PathVariable CouponFilter filter) {
        try {
            log.debug("Received request with userType={}", userType);
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            Page<DealSearchIndex> dealSearchIndexPage =
                    couponService.listDealsWithCoupons(userId, searchTerm, pageable, filter);
            return getSuccessResponse(new DealListResponseDto(dealSearchIndexPage, timezone),
                    SuccessResponseStatusType.READ_DEAL_LIST);
        } catch (KupiCoreException e) {
            log.error("Failed to return coupon deals for userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to list coupons by dealId.
     *
     * @param timeZone   timeZone
     * @param userId     userId
     * @param dealId     dealId
     * @param page       page
     * @param size       size
     * @param searchTerm searchTerm
     * @return list of coupons.
     */
    @GetMapping("/deal/{dealId}/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> listCouponsByDealId(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                               @RequestHeader(name = USER_ID_HEADER) String userId,
                                                               @PathVariable String dealId,
                                                               @Min(DEFAULT_PAGE) @PathVariable int page,
                                                               @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
                                                               @PathVariable String searchTerm,
                                                               @RequestParam(required = false) String startDate,
                                                               @RequestParam(required = false) String endDate) {
        try {
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
            
            Pageable pageable = PageRequest.of(page, size);
            Map<String, BasicUserResponseDto> userMap = new HashMap<>();
            Page<Coupon> couponPage = couponService.getCouponsByDealIdWithUserFilter(pageable, dealId, searchTerm, userId, userMap, startMillis, endMillis);
            
            return getSuccessResponse(new CouponListResponseDto(couponPage, userMap, timeZone),
                    SuccessResponseStatusType.SUCCESSFULLY_RETURNED_COUPONS);
        } catch (KupiCoreException e) {
            log.error("Failed to return coupon list for dealId: {}", dealId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to redeem coupon.
     *
     * @param timeZone timeZone
     * @param userId   userId
     * @param dealId   dealId
     * @return coupon details.
     */
    @GetMapping("/deal/{dealId}")
    public ResponseEntity<ResponseWrapper> purchaseCouponForMobileUser(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @PathVariable String dealId,
            HttpServletRequest request) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);

            BasicCouponResponseDto basicCouponResponseDto = couponService.getNewCouponCode(dealId, userId,timeZone, authToken);
//            if (coupon.isFree()) {
//                log.error("Coupon can't be free when purchasing. Coupon price: {}, isFree: {}",
//                        coupon.getPrice(), coupon.isFree());
//                return getInternalServerError();
//            }
//            BasicCouponResponseDto basicCouponResponseDto = new BasicCouponResponseDto(coupon, timeZone);
            log.debug("Successfully redeemed coupon for dealId: {} userId: {}", dealId, userId);
            return getSuccessResponse(basicCouponResponseDto, SuccessResponseStatusType.SUCCESSFULLY_REDEEMED_COUPON);
        } catch (CouponException e) {
            log.error("Coupon not available for dealId: {}", dealId, e);
            return getErrorResponse(e.getCouponErrorType().getErrorResponseStatusType());
        } catch (KupiCoreException e) {
            log.error("Failed to redeem coupon for dealId: {} userId: {}", dealId, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to update coupon purchase status.
     *
     * @param userId               userId
     * @param couponId             couponId
     * @param couponPurchaseStatus couponPurchaseStatus
     * @return success/error message.
     */
    @PutMapping("/{couponId}/deal/{dealId}/status/{couponPurchaseStatus}")
    public ResponseEntity<ResponseWrapper> updateCouponPurchaseStatus(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @PathVariable String couponId,
            @PathVariable String dealId,
            @PathVariable CouponPurchaseStatus couponPurchaseStatus) {
        try {
            couponService.updateCouponPurchaseStatus(couponId, dealId, couponPurchaseStatus);
            log.debug("Successfully updated coupon purchase status for couponId: {} userId: {}", couponId, userId);
            return getSuccessResponse(null, SuccessResponseStatusType.COUPON_PURCHASE_STATUS_UPDATE);
        } catch (CouponException e) {
            log.error("Coupon not available for couponId: {}", couponId, e);
            return getErrorResponse(ErrorResponseStatusType.COUPON_NOT_FOUND);
        } catch (KupiCoreException e) {
            log.error("Failed to update coupon purchase status for couponId: {} userId: {}", couponId, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method used to validate a coupon code.
     *
     * @param redeemRequestDto redeemRequestDto
     * @return true/false response.
     */
    @PostMapping("/validate")
    public ResponseEntity<ResponseWrapper> validateCoupon(@RequestBody RedeemRequestDto redeemRequestDto) {
        if (!redeemRequestDto.isRequiredAvailableForCouponValidation()) {
            return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
        }
        try {
            CouponValidationResponseDto couponValidationResponseDto = couponService.validateCoupon(redeemRequestDto);
            return getSuccessResponse(couponValidationResponseDto,
                    SuccessResponseStatusType.SUCCESSFULLY_RETURNED_COUPON_VALIDITY);
        } catch (KupiCoreException e) {
            log.error("Failed to validate coupon for couponCode: {}, merchantId: {}.", redeemRequestDto.getCouponCode(),
                    redeemRequestDto.getMerchantId(), e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to redeem coupon by merchant.
     *
     * @param redeemRequestDto redeemRequestDto
     * @return success/error response.
     */
    @PostMapping("/redeem")
    public ResponseEntity<ResponseWrapper> redeemCouponForMerchant(@RequestBody RedeemRequestDto redeemRequestDto,
                                                                   @RequestHeader(name = TIME_ZONE_HEADER) String timeZone) {
        if (!redeemRequestDto.isRequiredAvailable())
            return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
        String couponCode = redeemRequestDto.getCouponCode();
        String merchantId = redeemRequestDto.getMerchantId();
        try {
            CouponRedeemSuccessResponseDto couponRedeemSuccessResponseDto = couponService.redeemCoupon(redeemRequestDto, timeZone);
            log.debug("Successfully redeemed coupon for couponCode: {}", couponCode);
            return getSuccessResponse(couponRedeemSuccessResponseDto, SuccessResponseStatusType.SUCCESSFULLY_REDEEMED_COUPON);
        } catch (CouponException e) {
            log.error("Coupon not available for couponCode: {}", couponCode, e);
            return getErrorResponse(e.getCouponErrorType().getErrorResponseStatusType());
        } catch (InvalidUserException e) {
            log.error("MerchantId: {} doesn't match", merchantId, e);
            return getErrorResponse(ErrorResponseStatusType.MERCHANT_DOESNT_MATCH);
        } catch (ExpiredDealException e) {
            log.error("Deal is expired.", e);
            return getErrorResponse(ErrorResponseStatusType.EXPIRED_DEAL);
        } catch (UserSubscriptionExpiredException e) {
            return getErrorResponse(ErrorResponseStatusType.USER_SUBSCRIPTION_EXPIRED);
        } catch (KupiCoreException e) {
            log.error("Failed to redeem coupon for couponCode: {}, merchantId: {}.", couponCode, merchantId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to upload coupons by csv file.
     *
     * @param couponRequestDto couponFileUploadRequestDto
     * @return Success Response/ Error Response
     */
    @PostMapping("/upload")
    public ResponseEntity<ResponseWrapper> uploadCoupons(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                         @RequestBody CouponFileUploadRequestDto couponRequestDto) {
        try {
            if (!couponRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            Deal deal = dealService.getDeal(couponRequestDto.getDealId(), UserType.MERCHANT);
            if (isDealExpiredOrAboutToExpire(deal.getExpiredOn())) {
                return getErrorResponse(ErrorResponseStatusType.ADD_COUPON_FOR_RECENTLY_EXPIRING_DEAL);
            }
            couponService.uploadCoupons(couponRequestDto, timeZone, deal);
            log.debug("Coupon uploading process started for deal id: {}.", couponRequestDto.getDealId());
            return getSuccessResponse(null, SuccessResponseStatusType.UPLOAD_COUPONS);
        } catch (InvalidDealException e) {
            log.error("Invalid deal id: {} to upload coupons", couponRequestDto.getDealId());
            return getErrorResponse(ErrorResponseStatusType.INVALID_DEAL_ID);
        } catch (CouponUploadFailedException e) {
            log.error("Failed to upload coupon for deal id: {}", couponRequestDto.getDealId());
            return getErrorResponse(ErrorResponseStatusType.COUPON_UPLOAD_FAILED);
        } catch (KupiCoreException e) {
            log.error("Failed to upload coupon for dealId: {}.", couponRequestDto.getDealId(), e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns true when the deal is expired or about to expire for given date gaps.
     *
     * @param expiryDate deal expiry date
     * @return true/ false
     */
    private boolean isDealExpiredOrAboutToExpire(long expiryDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DATE, dateGaps);
        long gapTimesInMillis = calendar.getTimeInMillis();
        return gapTimesInMillis > expiryDate;
    }

    /**
     * This method is used to update coupon status of a deal
     *
     * @param userId           user id
     * @param timeZone         time zone
     * @param userType         user type
     * @param activeStatusType active status type
     * @param dealId           deal id
     * @return Success/ Error response
     */
    @PutMapping("/{userType}/{activeStatusType}/{dealId}")
    public ResponseEntity<ResponseWrapper> updateDealCouponStatus(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                                  @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                  @PathVariable UserType userType,
                                                                  @PathVariable ActiveStatusType activeStatusType,
                                                                  @PathVariable String dealId) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "update deal coupon status");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            if (activeStatusType != ActiveStatusType.ACTIVE && activeStatusType != ActiveStatusType.INACTIVE) {
                log.error("Coupon status: {} invalid for update deal coupon status", activeStatusType);
                return getErrorResponse(ErrorResponseStatusType.INVALID_COUPON_STATUS);
            }
            couponService.updateDealCouponStatus(dealId, userType, activeStatusType);
            log.debug("Successfully updated deal coupon status for userId: {}, timeZone: {}, userType: {}, " +
                    "activeStatusType: {}, dealId: {}", userId, timeZone, userType, activeStatusType, dealId);
            SuccessResponseStatusType successMsg = activeStatusType == ActiveStatusType.ACTIVE ?
                    SuccessResponseStatusType.DEAL_COUPON_STATUS_ACTIVE :
                    SuccessResponseStatusType.DEAL_COUPON_STATUS_INACTIVE;
            return getSuccessResponse(null, successMsg);
        } catch (InvalidDealException e) {
            log.error("Invalid deal to update deal coupon status", e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_DEAL_ID);
        } catch (KupiCoreException e) {
            log.error("Failed to update deal coupon status for dealId: {}.", dealId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method used to update coupon status
     *
     * @param userId           user id
     * @param couponId         coupon id
     * @param activeStatusType ACTIVE/ INACTIVE
     * @return Success/ Error response
     */
    @PutMapping("{couponId}/{activeStatusType}")
    public ResponseEntity<ResponseWrapper> updateCouponStatus(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                              @PathVariable String couponId,
                                                              @PathVariable ActiveStatusType activeStatusType) {
        try {
            if (activeStatusType != ActiveStatusType.ACTIVE && activeStatusType != ActiveStatusType.INACTIVE) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_COUPON_STATUS);
            }
            couponService.updateCouponStatus(couponId, activeStatusType);
            SuccessResponseStatusType responseType = activeStatusType == ActiveStatusType.ACTIVE ?
                    SuccessResponseStatusType.COUPON_IS_ACTIVE : SuccessResponseStatusType.COUPON_IS_INACTIVE;
            log.debug("Coupon status is successfully updated for userId: {} couponId: {}", userId, couponId);
            return getSuccessResponse(null, responseType);
        } catch (CouponException e) {
            log.error("Invalid coupon id or can't update status. couponId: {}.", couponId, e);
            return getErrorResponse(e.getCouponErrorType().getErrorResponseStatusType());
        } catch (KupiCoreException e) {
            log.error("Failed to update coupon status for couponId: {}.", couponId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method used to get coupon history by user id.
     *
     * @param page      page
     * @param size      size
     * @param userId    userId
     * @param timeZone  timeZone
     * @param filter    filter (ALL, USED, EXPIRE_SOON, ACTIVE, INACTIVE) - default: ALL
     * @param sortBy    sort by field (purchasedOn, expiryDate, redeemedOn, createdAt, price) - default: purchasedOn
     * @param sortOrder sort order (ASC, DESC) - default: DESC
     * @return success/error response.
     */
    @GetMapping("user/history/{page}/{size}")
    public ResponseEntity<ResponseWrapper> getCouponHistoryByUser(@Min(DEFAULT_PAGE) @PathVariable int page,
                                                                  @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
                                                                  @RequestHeader(name = USER_ID_HEADER) String userId,
                                                                  @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                  @RequestParam(name = "filter", required = false, defaultValue = "ALL") String filter,
                                                                  @RequestParam(name = "sortBy", required = false, defaultValue = "purchasedOn") String sortBy,
                                                                  @RequestParam(name = "sortOrder", required = false, defaultValue = "DESC") String sortOrder) {
        try {
            Sort.Direction direction = Sort.Direction.fromString(sortOrder.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            var userCouponHistoryPageResponseDto = couponService.getHistoryByUserId(pageable, timeZone, userId, filter.toUpperCase());
            log.debug("Successfully returned the coupon history list for user id: {} with sortBy: {}, sortOrder: {}. ", userId, sortBy, sortOrder);
            return getSuccessResponse(userCouponHistoryPageResponseDto,
                    SuccessResponseStatusType.READ_USER_COUPON_HISTORY);
        } catch (IllegalArgumentException e) {
            log.error("Invalid sort parameter - sortBy: {} or sortOrder: {} for userId: {}. ", sortBy, sortOrder, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_SORT_PARAMETER);
        } catch (KupiCoreException e) {
            log.error("Failed to return coupon history by user id: {}. ", userId, e);
            return getInternalServerError();
        }
    }


    /**
     * This method is used to purchase free coupon.
     *
     * @param timeZone timeZone
     * @param userId   userId
     * @param dealId   dealId
     * @return coupon details.
     */
    @GetMapping("/free/deal/{dealId}")
    public ResponseEntity<ResponseWrapper> purchaseFreeCoupon(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @PathVariable String dealId,
            HttpServletRequest request) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            BasicCouponResponseDto basicCouponResponseDto = couponService.getNewCouponCode(dealId, userId, timeZone,authToken);

//            if (!coupon.isFree()) {
//                log.error("This coupon is not free, please purchase. price: {}", coupon.getPrice());
//                return getInternalServerError();
//            }
//            BasicCouponResponseDto basicCouponResponseDto = new BasicCouponResponseDto(coupon, timeZone,authToken);
            log.debug("Successfully redeemed coupon for dealId: {} userId: {}", dealId, userId);
            return getSuccessResponse(basicCouponResponseDto, SuccessResponseStatusType.SUCCESSFULLY_REDEEMED_COUPON);
        } catch (CouponException e) {
            log.error("Coupon not available for dealId: {}", dealId, e);
            return getErrorResponse(e.getCouponErrorType().getErrorResponseStatusType());
        } catch (UserSubscriptionExpiredException e) {
            return getErrorResponse(ErrorResponseStatusType.USER_SUBSCRIPTION_EXPIRED);
        } catch (ExceededCouponLimitException e) {
            return getErrorResponse(ErrorResponseStatusType.COUPON_LIMIT_EXCEEDED);
        } catch (KupiCoreException e) {
            log.error("Failed to redeem coupon for dealId: {} userId: {}", dealId, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get sold coupons by merchantId and dealId.
     *
     * @param timeZone   timeZone
     * @param merchantId merchantId
     * @param dealId     dealId
     * @param searchTerm searchTerm
     * @param page       page
     * @param size       size
     * @return sold coupon list.
     */
    @GetMapping("/sold/MERCHANT/{merchantId}/deal/{dealId}/search/{searchTerm}/{page}/{size}")
    public ResponseEntity<ResponseWrapper> getSoldCouponsByMerchant(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                    @RequestHeader(name = USER_ID_HEADER) String userId,
                                                                    @PathVariable String merchantId,
                                                                    @PathVariable String dealId,
                                                                    @PathVariable String searchTerm,
                                                                    @PathVariable @Min(DEFAULT_PAGE) int page,
                                                                    @PathVariable @Positive @Max(PAGE_MAX_SIZE) int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("purchasedOn").descending());
            Page<Coupon> couponPage = couponService.getSoldCoupons(merchantId, dealId, searchTerm, pageable);
            List<String> userIdList = couponPage.stream().map(Coupon::getPurchasedBy).collect(Collectors.toList());
            Map<String, BasicUserResponseDto> userMap = userIdList.isEmpty() ? new HashMap<>() :
                    authUserService.getUserMap(userId, new BulkUserRequestDto(userIdList));
            return getSuccessResponse(new CouponListResponseDto(couponPage, userMap, timeZone),
                    SuccessResponseStatusType.SUCCESSFULLY_RETURNED_COUPONS);
        } catch (KupiCoreException e) {
            log.error("Failed to return coupon list for merchantId: {}, dealId: {}", merchantId, dealId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method used to get coupon uploaded file name list by deal id.
     *
     * @param dealId dealId
     * @return fileName List
     */
    @GetMapping("/files/deal/{dealId}")
    public ResponseEntity<ResponseWrapper> getUploadedCouponFilesForDealId(@PathVariable String dealId) {
        try {
            var successFileListByDealId = couponService.getUploadedCouponFileListByDealId(dealId);
            var uploadedFailedCouponFileListByDealId =
                    couponStatisticDetailService.getUploadedFailedCouponFileListByDealId(dealId);
            var couponUploadFileList =
                    new CouponUploadFileListDto(successFileListByDealId, uploadedFailedCouponFileListByDealId);
            return getSuccessResponse(couponUploadFileList, SuccessResponseStatusType.READ_COUPON_UPLOAD_FILE_NAMES);
        } catch (KupiCoreException e) {
            log.error("Failed to return file name list for dealId: {}", dealId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method used to get details of single coupon uploaded file.
     *
     * @param fileRequestDto fileRequestDto
     * @return uploadCouponFileDetailResponseDto
     */
    @PostMapping("/file-details")
    public ResponseEntity<ResponseWrapper> getCouponFileDetails(@RequestBody
                                                                UploadedCouponFileDetailRequestDto fileRequestDto) {
        try {
            var successCoupons = couponService
                    .getUploadSuccessCouponsForFileName(fileRequestDto.getDealId(), fileRequestDto.getFileName());
            var uploadFailedCouponDetails
                    = couponStatisticDetailService.getUploadFailedCouponDetails(fileRequestDto.getDealId(), fileRequestDto.getFileName());
            var uploadCouponFileDetailResponseDto =
                    new UploadCouponFileDetailResponseDto(successCoupons, uploadFailedCouponDetails);
            return getSuccessResponse(uploadCouponFileDetailResponseDto, SuccessResponseStatusType.READ_COUPON_UPLOAD_FILE);
        } catch (KupiCoreException e) {
            log.error("Failed to return details for file name: {} and deal id: {}.",
                    fileRequestDto.getDealId(), fileRequestDto.getFileName(), e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get coupon details for a deal.
     *
     * @param dealId dealId
     * @return coupon details (title, subTitle, total, available, purchased).
     */
    @GetMapping("/deal/{dealId}/details")
    public ResponseEntity<ResponseWrapper> getDealCouponDetails(@PathVariable String dealId) {
        try {
            DealSearchIndex dealSearchIndex = dealSearchService.findById(dealId);
            DealCouponDetailsResponseDto dealCouponDetailsResponseDto =
                    new DealCouponDetailsResponseDto(dealSearchIndex.getTitle(),
                            dealSearchIndex.getSubTitle(),
                            dealSearchIndex.getTotalCoupons(),
                            dealSearchIndex.getRemainingCoupons());
            log.debug("Successfully returned coupon details for dealId: {}", dealId);
            return getSuccessResponse(dealCouponDetailsResponseDto,
                    SuccessResponseStatusType.SUCCESSFULLY_RETURNED_COUPON_STAT);
        } catch (InvalidDealException e) {
            log.error("Invalid deal id: {} to get coupon details", dealId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_DEAL_ID);
        } catch (KupiCoreException e) {
            log.error("Failed to return coupon details for dealId: {}", dealId, e);
            return getInternalServerError();
        }
    }
}
