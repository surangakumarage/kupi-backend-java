package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.Brand;
import com.kupi.cc.base.domain.entity.Category;
import com.kupi.cc.base.domain.entity.CombinedDealRequest;
import com.kupi.cc.base.domain.entity.OfferType;
import com.kupi.cc.base.domain.entity.RequestADeal;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.request.RequestADealCreateRequestDto;
import com.kupi.cc.base.domain.response.*;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.service.*;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import com.kupi.cc.base.util.DateParserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Validated
@RequestMapping("api/v1/request-a-deal")
public class RequestADealController extends Controller {

    private static final String NONE = "NONE";
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final AuthUserService authUserService;
    private final RequestADealService requestADealService;
    private final OfferTypeService offerTypeService;
    private final DateParserUtil dateParserUtil;

    public RequestADealController(CategoryService categoryService, BrandService brandService,
                                  AuthUserService authUserService, RequestADealService requestADealService,
                                  Translator translator, OfferTypeService offerTypeService,
                                  DateParserUtil dateParserUtil) {
        super(translator);
        this.categoryService = categoryService;
        this.brandService = brandService;
        this.authUserService = authUserService;
        this.requestADealService = requestADealService;
        this.offerTypeService = offerTypeService;
        this.dateParserUtil = dateParserUtil;
    }

    /**
     * Create new Request a deal
     *
     * @param timeZone                     time zone
     * @param userId                       userId
     * @param requestADealCreateRequestDto requestADealCreateRequestDto
     * @return ResponseEntity requestADealCreateResponseDto
     */
    @PostMapping("/{toUserType}")
    public ResponseEntity<ResponseWrapper> createRequestADeal(
            @PathVariable UserType toUserType,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestBody RequestADealCreateRequestDto requestADealCreateRequestDto,
            HttpServletRequest request) {
        try {
            Brand brand = null;
            OfferType offerType = null;
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            if (!requestADealCreateRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            var category = categoryService.getCategoryById(requestADealCreateRequestDto.getCategoryId());
            var merchant = UserType.MERCHANT.equals(toUserType) ?
                    authUserService.getMerchantBusinessByMerchantId(authToken, requestADealCreateRequestDto.getMerchantId()) :
                    authUserService.getBankBusinessByBankId(authToken, requestADealCreateRequestDto.getMerchantId());
            if (requestADealCreateRequestDto.getBrandId() != null
                    && !requestADealCreateRequestDto.getBrandId().isEmpty()) {
                brand = brandService.getBrandById(requestADealCreateRequestDto.getBrandId());
            }
            if (requestADealCreateRequestDto.getOfferTypeId() != null
                    && !requestADealCreateRequestDto.getOfferTypeId().isEmpty()) {
                offerType = offerTypeService.getOfferTypeById(requestADealCreateRequestDto.getOfferTypeId());
            }
            var requestDeal =
                    requestADealService.saveRequestADeal(new RequestADeal(requestADealCreateRequestDto, category,
                            brand, offerType), merchant, toUserType);
            var requestADealCreateResponseDto = new RequestADealCreateResponseDto(requestDeal);
            var requestADealResponseJson = requestADealCreateResponseDto.toLogJson();
            log.debug("Successfully saved Request a deal. Request a deal response: {}", requestADealResponseJson);
            return getSuccessResponse(requestADealCreateResponseDto, SuccessResponseStatusType.CREATE_REQUEST_A_DEAL);
        } catch (InvalidUserException e) {
            log.error("Invalid merchant Id: {}, to save the request a deal.",
                    requestADealCreateRequestDto.getMerchantId(), e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (InvalidCategoryException e) {
            log.error("Invalid category Id: {}, to save the request a deal.",
                    requestADealCreateRequestDto.getCategoryId(), e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (InvalidBrandException e) {
            log.error("Invalid Brand Id: {}, to save the request a deal.", requestADealCreateRequestDto.getBrandId());
            return getErrorResponse(ErrorResponseStatusType.INVALID_BRAND_ID);
        } catch (InvalidOfferTypeException e) {
            log.error("Invalid OfferType Id: {} for update Offer Type.", requestADealCreateRequestDto.getOfferTypeId(), e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_OFFER_TYPE_ID);
        } catch (KupiCoreException e) {
            log.error("Creating new request a deal was failed for user id: {}, category id: {}, brand id: {}," +
                            " merchant id: {}, timezone: {}", requestADealCreateRequestDto.getUserId(),
                    requestADealCreateRequestDto.getCategoryId(), requestADealCreateRequestDto.getBrandId(),
                    requestADealCreateRequestDto.getMerchantId(), timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * Mobile: List Deal Request by user Id.
     * Get request deal list by user Id.
     *
     * @param timeZone time zone
     * @param page     page
     * @param size     size
     * @param userId   user Id
     * @return response entity
     */
    @GetMapping("/{toUserType}/{page}/{size}/user/{mobileUserId}")
    public ResponseEntity<ResponseWrapper> getRequestDealsByUserId(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @PathVariable UserType toUserType,
            @PathVariable String mobileUserId) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            var requestADealPage = requestADealService
                    .getRequestDealsByUserIdAndToUserType(pageable, mobileUserId, toUserType);
            var basicRequestDealListResponseDto
                    = createBasicRequestDealListResponseDto(requestADealPage, timeZone, toUserType);
            log.debug("Successfully return the request deal list by user Id for page: {}, size {}, userId:{}",
                    page, size, mobileUserId);
            return getSuccessResponse(basicRequestDealListResponseDto, SuccessResponseStatusType.READ_REQUEST_DEAL_LIST);
        } catch (InvalidUserException e) {
            log.error("Invalid merchant Id(s) to return the request deal list by user Id. userId: {}", mobileUserId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (KupiCoreException e) {
            log.error("Returning request deal list by userId was failed for userId: {}, page: {}, size {}",
                    userId, page, size, e);
            return getInternalServerError();
        }
    }

    /**
     * Get detail request deal
     *
     * @param timeZone       timeZone
     * @param userId         userId
     * @param requestADealId requestDealId
     * @param request        request
     * @return response entity
     */
    @GetMapping("/{requestADealId}")
    public ResponseEntity<ResponseWrapper> getRequestDealsById(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @PathVariable String requestADealId,
            HttpServletRequest request
    ) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            var requestADeal = requestADealService.getRequestADealById(requestADealId);
            var businessMerchant = UserType.MERCHANT.equals(requestADeal.getToUserType()) ?
                    authUserService.getMerchantBusinessByMerchantId(authToken, requestADeal.getMerchantId()) :
                    authUserService.getBankBusinessByBankId(authToken, requestADeal.getMerchantId());
            var detailRequestDealResponseDto = new DetailRequestADealResponseDto(requestADeal,
                    new BasicMerchantBusinessResponseDto(businessMerchant), timeZone);
            log.debug("Successfully return the detail request deal by Id for , userId:{}", userId);
            return getSuccessResponse(detailRequestDealResponseDto, SuccessResponseStatusType.READ_REQUEST_DEAL_DETAIL);
        } catch (InvalidRequestADealException e) {
            log.error("Invalid requestDeal: {} for user Id: {}", requestADealId, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_REQUEST_DEAL_ID);
        } catch (InvalidUserException e) {
            log.error("Invalid merchantId to return the request deal by Id. userId: {}", userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (KupiCoreException e) {
            log.error("Returning request deal by id was failed for userId: {}",
                    userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method creates basic request deal response dto
     *
     * @param page     page
     * @param timeZone timeZone
     * @return BasicRequestADealListResponseDto
     */
    private BasicRequestADealListResponseDto createBasicRequestDealListResponseDto(Page<RequestADeal> page,
                                                                                   String timeZone, UserType toUserType) {
        List<String> merchantIds = new ArrayList<>();
        page.getContent().forEach(requestADeal -> merchantIds.add(requestADeal.getMerchantId()));
        if (!page.getContent().isEmpty()) {
            var bulkUserRequestDto = new BulkUserRequestDto(merchantIds);
            var bulkUserResponseMap = authUserService.
                    getMerchantMap(bulkUserRequestDto, toUserType);
            return new BasicRequestADealListResponseDto(page, bulkUserResponseMap, timeZone);
        }
        return new BasicRequestADealListResponseDto(page, new HashMap<>(), timeZone);
    }

    /**
     * List All Combined Deal Requests
     * This API used to get All deal requests for merchant users.
     *
     * @param page       page
     * @param size       size
     * @param merchantId merchant id
     * @param searchTerm search term
     * @return combined Request a deal list
     */
    @GetMapping("/combinations/{page}/{size}/{toUserType}/{merchantId}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getAllRequestsDeals(
            @Min(DEFAULT_PAGE) @PathVariable("page") int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable("size") int size,
            @PathVariable("merchantId") String merchantId,
            @PathVariable("searchTerm") String searchTerm,
            @PathVariable UserType toUserType,
            HttpServletRequest request) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            if (!merchantId.equals(ALL)) {
                if (UserType.MERCHANT.equals(toUserType)) {
                    authUserService.getMerchantBusinessByMerchantId(authToken, merchantId);
                } else {
                    authUserService.getBankBusinessByBankId(authToken, merchantId);
                }
            }
            Pageable pageable = PageRequest.of(page, size);
            var allCombinedDealRequests = requestADealService
                    .getAllCombinedDealRequests(pageable, merchantId, searchTerm, toUserType);
            if (!allCombinedDealRequests.isEmpty()) {
                var listCombinedDealRequestResponseDto =
                        createListCombinedDealRequestResponseDto(allCombinedDealRequests, toUserType);
                log.info("successfully fetched combinedDealRequestList with size of: {} for merchant id: {}" +
                        " and search term: {}", listCombinedDealRequestResponseDto.getSize(), merchantId, searchTerm);
                return getSuccessResponse(listCombinedDealRequestResponseDto,
                        SuccessResponseStatusType.READ_REQUEST_A_DEAL);
            }
            var listCombinedDealRequestResponseDto = new ListCombinedDealRequestResponseDto(allCombinedDealRequests,
                    null);
            return getSuccessResponse(listCombinedDealRequestResponseDto,
                    SuccessResponseStatusType.READ_REQUEST_A_DEAL);
        } catch (InvalidUserException e) {
            log.error("Invalid merchant Id: {} for get all request a deals.", merchantId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (KupiCoreException e) {
            log.error("Returning all request a deals was failed for page: {}, size {}, merchantId: " +
                    "{} and searchTerm: {}", page, size, merchantId, searchTerm, e);
            return getInternalServerError();
        }
    }

    /**
     * ADMIN/ MERCHANT: Summary for a Single Request a Deal Combination
     *
     * @param userId     user id
     * @param timeZone   time zone
     * @param merchantId merchant id
     * @param brandId    brand id
     * @param categoryId category id
     * @return RequestADealDetailResponse
     */
    @GetMapping("/combination-summary/{toUserType}/{merchantId}/category/{categoryId}/brand/{brandId}")
    public ResponseEntity<ResponseWrapper> getSummaryForSingleRequestADealCombination(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable("merchantId") String merchantId,
            @PathVariable(value = "brandId", required = false) String brandId,
            @PathVariable("categoryId") String categoryId,
            @PathVariable UserType toUserType,
            HttpServletRequest request) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            var merchant = UserType.MERCHANT.equals(toUserType) ?
                    authUserService.getMerchantBusinessByMerchantId(authToken, merchantId) :
                    authUserService.getBankBusinessByBankId(authToken, merchantId);
            var category = categoryService.getCategoryById(categoryId);
            Brand brand = (NONE.equals(brandId)) ? null : brandService.getBrandById(brandId);
            var combinedRequestADealCount =
                    requestADealService.getCountForCombinedRequestADeal(merchantId, category, brand, toUserType);
            var singleRequestADealSummaryResponseDto =
                    new SingleRequestADealSummaryResponseDto(
                            new BasicMerchantBusinessResponseDto(merchant), category, brand, combinedRequestADealCount);
            return getSuccessResponse(singleRequestADealSummaryResponseDto,
                    SuccessResponseStatusType.REQUEST_A_DEAL_COMBINATION_SUMMARY);
        } catch (InvalidUserException e) {
            log.error("Invalid merchant id: {}, to fetch  summary for single request a deal combination.", merchantId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (InvalidCategoryException e) {
            log.error("Invalid category Id: {}, to fetch  summary for single request a deal combination.", categoryId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (InvalidBrandException e) {
            log.error("Invalid Brand Id: {}, to fetch  summary for single request a deal combination.", brandId);
            return getErrorResponse(ErrorResponseStatusType.INVALID_BRAND_ID);
        } catch (KupiCoreException e) {
            log.error("Fetching summary for single request a deal combination" +
                    " was failed for user id: {}, category id: {}, brand id: {}," +
                    "merchant id: {}, timezone: {}", userId, categoryId, brandId, merchantId, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * create listCombinedDealRequest from a page combined deal request.
     *
     * @param allCombinedDealRequests page combined deal request
     * @return List combinedDealRequestResponse
     */
    private ListCombinedDealRequestResponseDto createListCombinedDealRequestResponseDto(
            Page<CombinedDealRequest> allCombinedDealRequests, UserType toUserType) {
        List<String> userIds = new ArrayList<>();
        allCombinedDealRequests.getContent().forEach(allCombinedDealRequest ->
                userIds.add(allCombinedDealRequest.getMerchantId()));
        var bulkUserRequestDto = new BulkUserRequestDto(userIds);
        var bulkUserResponseDto =
                authUserService.getMerchantMap(bulkUserRequestDto, toUserType);
        return new ListCombinedDealRequestResponseDto(allCombinedDealRequests,
                bulkUserResponseDto);
    }

    /**
     * ADMIN/ MERCHANT: List of Request a Deal Items for a Single Combination.
     *
     * @param userId     user id
     * @param timeZone   time zone
     * @param page       page
     * @param size       size
     * @param merchantId merchant id
     * @param brandId    brand id
     * @param categoryId category id
     * @return RequestADealDetailResponse
     */
    @GetMapping("/{page}/{size}/{toUserType}/{merchantId}/category/{categoryId}/brand/{brandId}")
    public ResponseEntity<ResponseWrapper> getAllRequestsDealsDetails(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable("page") int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable("size") int size,
            @PathVariable("merchantId") String merchantId,
            @PathVariable(value = "brandId", required = false) String brandId,
            @PathVariable("categoryId") String categoryId,
            @PathVariable UserType toUserType,
            HttpServletRequest request) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            brandId = ((NONE.equals(brandId)) ? null : brandId);
            var merchant = UserType.MERCHANT.equals(toUserType) ?
                    authUserService.getMerchantBusinessByMerchantId(authToken, merchantId) :
                    authUserService.getBankBusinessByBankId(authToken, merchantId);
            var category = categoryService.getCategoryById(categoryId);
            Brand brand = (brandId != null) ? brandService.getBrandById(brandId) : null;
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            var allRequestADealDetail = requestADealService
                    .getAllRequestADealDetail(pageable, merchantId, categoryId, brandId, toUserType);
            var bulkUserResponse =
                    getBulkUserResponseMap(allRequestADealDetail, userId);
            var requestADealDetailResponseListDto =
                    new RequestADealDetailResponseListDto(allRequestADealDetail,
                            bulkUserResponse, category, brand, merchant, timeZone);
            log.debug("Successfully returned the request a deals by category id for: {}, brand id: {} & merchant id " +
                    ":{}", categoryId, brandId, merchantId);
            return getSuccessResponse(requestADealDetailResponseListDto,
                    SuccessResponseStatusType.READ_REQUEST_DEAL_DETAIL);
        } catch (InvalidUserException e) {
            log.error("Invalid merchant id: {}, to fetch get all request a deal detail.", merchantId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (InvalidCategoryException e) {
            log.error("Invalid category Id: {}, to fetch get all request a deal detail.", categoryId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (InvalidBrandException e) {
            log.error("Invalid Brand Id: {}, to fetch get all request a deal detail.", brandId);
            return getErrorResponse(ErrorResponseStatusType.INVALID_BRAND_ID);
        } catch (KupiCoreException e) {
            log.error("Fetching get all request a deal detail was failed for user id: {}, category id: {}, brand" +
                    " id: {},merchant id: {}, timezone: {}", userId, categoryId, brandId, merchantId, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * ADMIN/ MERCHANT: List of Request a Deal items for a merchant with filters.
     * <p>
     * Filters:
     * - searchTerm: search on notes (case-insensitive). Use 'ALL' to disable search.
     * - categoryId: 'ALL' for all categories or specific category id.
     * - date range: 'ALL' for both startDate and endDate to disable date filtering, or provide
     * concrete dates as query params which will be parsed using {@link DateParserUtil}.
     * <p>
     * Note: merchantId cannot be 'ALL'.
     *
     * @param userId     user id
     * @param timeZone   time zone
     * @param page       page
     * @param size       size
     * @param merchantId merchant id
     * @param categoryId category id or 'ALL'
     * @param searchTerm search term for note or 'ALL'
     * @return RequestADealDetailResponseListDto
     */
    @GetMapping("/{page}/{size}/{toUserType}/{merchantId}/category/{categoryId}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getRequestADealsForMerchantWithFilters(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable("page") int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable("size") int size,
            @PathVariable("merchantId") String merchantId,
            @PathVariable("categoryId") String categoryId,
            @PathVariable("searchTerm") String searchTerm,
            @RequestParam(name = "startDate", required = false, defaultValue = "ALL") String startDate,
            @RequestParam(name = "endDate", required = false, defaultValue = "ALL") String endDate,
            @PathVariable UserType toUserType,
            HttpServletRequest request) {
        try {
            if (ALL.equals(merchantId)) {
                log.error("merchantId cannot be ALL for filtered request-a-deal list.");
                return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
            }

            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            var merchant = UserType.MERCHANT.equals(toUserType) ?
                    authUserService.getMerchantBusinessByMerchantId(authToken, merchantId) :
                    authUserService.getBankBusinessByBankId(authToken, merchantId);

            Long startMillis = null;
            Long endMillis = null;

            boolean isStartAll = startDate == null || ALL.equalsIgnoreCase(startDate);
            boolean isEndAll = endDate == null || ALL.equalsIgnoreCase(endDate);

            if (!(isStartAll && isEndAll)) {
                // Both dates must be provided when filtering by date
                if (isStartAll || isEndAll) {
                    log.error("Invalid date range. Start or end date is missing. startDate: {}, endDate: {}",
                            startDate, endDate);
                    return getErrorResponse(ErrorResponseStatusType.INVALID_DATE_RANGE);
                }

                try {
                    startMillis = dateParserUtil.parseDateToMilliseconds(startDate, timeZone);
                    endMillis = dateParserUtil.parseDateToMilliseconds(endDate, timeZone);

                    // If endDate is provided as a date-only value (e.g. yyyy-MM-dd), make the range inclusive
                    // by moving endMillis to the end of that day (23:59:59.999).
                    if (endDate != null && endDate.trim().length() == 10 && !endDate.contains(" ")) {
                        final long ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L;
                        endMillis = endMillis + ONE_DAY_MILLIS - 1;
                    }
                } catch (IllegalArgumentException e) {
                    log.error("Invalid date format for request-a-deal filter. startDate: {}, endDate: {}", startDate,
                            endDate, e);
                    return getErrorResponse(ErrorResponseStatusType.INVALID_START_END_DATE);
                }

                if (startMillis > endMillis) {
                    log.error("Invalid date range. startDate is after endDate. start: {}, end: {}", startMillis, endMillis);
                    return getErrorResponse(ErrorResponseStatusType.INVALID_DATE_RANGE);
                }
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            var requestADealsPage = requestADealService.getRequestADealsForMerchant(pageable, merchantId,
                    categoryId, searchTerm, startMillis, endMillis, toUserType);

            // In-memory filtering by searchTerm on note and user name (case-insensitive)
            Page<RequestADeal> filteredPage = requestADealsPage;
            if (!ALL.equalsIgnoreCase(searchTerm)) {
                String searchLower = searchTerm.toLowerCase();
                var bulkUserResponseForFilter = getBulkUserResponseMap(requestADealsPage, userId);
                java.util.List<RequestADeal> filteredList = requestADealsPage.getContent().stream()
                        .filter(rd -> {
                            boolean noteMatch = rd.getNote() != null
                                    && rd.getNote().toLowerCase().contains(searchLower);
                            var userDto = bulkUserResponseForFilter.get(rd.getUserId());
                            boolean nameMatch = userDto != null
                                    && userDto.getName() != null
                                    && userDto.getName().toLowerCase().contains(searchLower);
                            return noteMatch || nameMatch;
                        })
                        .collect(java.util.stream.Collectors.toList());
                filteredPage = new PageImpl<>(filteredList, pageable, (long) filteredList.size());
            }

            var bulkUserResponse = getBulkUserResponseMap(filteredPage, userId);

            Category category = null;
            Brand brand = null;
            if (!ALL.equalsIgnoreCase(categoryId)) {
                category = categoryService.getCategoryById(categoryId);
            }

            var responseDto = new RequestADealDetailResponseListDto(filteredPage,
                    bulkUserResponse, category, brand, merchant, timeZone);

            log.debug("Successfully returned request-a-deal list with filters for merchant id: {}", merchantId);
            return getSuccessResponse(responseDto, SuccessResponseStatusType.READ_REQUEST_DEAL_DETAIL);
        } catch (InvalidUserException e) {
            log.error("Invalid merchant id: {}, to fetch filtered request a deal list.", merchantId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (InvalidCategoryException e) {
            log.error("Invalid category Id: {}, to fetch filtered request a deal list.", categoryId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            log.error("Fetching filtered request a deal list failed for user id: {}, merchant id: {}, category id: {}, " +
                    "startDate: {}, endDate: {}, searchTerm: {}", userId, merchantId, categoryId, startDate, endDate,
                    searchTerm, e);
            return getInternalServerError();
        }
    }

    /**
     * Get bulk user Response
     *
     * @param allRequestADeals requestADeals
     * @param userId           user id
     * @return BasicUserResponse Map
     */
    private Map<String, BasicUserResponseDto> getBulkUserResponseMap(Page<RequestADeal> allRequestADeals,
                                                                     String userId) {
        List<String> userIds = new ArrayList<>();
        allRequestADeals.getContent().forEach(requestADeal -> userIds.add(requestADeal.getUserId()));
        var bulkUserRequestDto = new BulkUserRequestDto(userIds);
        if (!userIds.isEmpty())
            return authUserService.getUserMap(userId, bulkUserRequestDto);
        else
            return new HashMap<>();
    }
}