package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.request.ReportRequestDto;
import com.kupi.cc.base.domain.response.*;
import com.kupi.cc.base.enums.*;
import com.kupi.cc.base.exception.InvalidDateOptionException;
import com.kupi.cc.base.exception.InvalidDealException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.service.AuthUserService;
import com.kupi.cc.base.service.CategoryService;
import com.kupi.cc.base.service.DealSearchService;
import com.kupi.cc.base.service.ReportService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report controller
 */
@Slf4j
@RestController
@Validated
@RequestMapping("api/v1/reports")
public class ReportController extends Controller {

    private static final int ZERO = 0;
    private final ReportService reportService;
    private final AuthUserService authUserService;
    private final DealSearchService dealSearchService;
    private final CategoryService categoryService;
    Logger logger = LoggerFactory.getLogger(ReportController.class);

    public ReportController(Translator translator, ReportService reportService, AuthUserService authUserService,
                            DealSearchService dealSearchService, CategoryService categoryService) {
        super(translator);
        this.reportService = reportService;
        this.authUserService = authUserService;
        this.dealSearchService = dealSearchService;
        this.categoryService = categoryService;
    }

    /**
     * Get top 10 deal view count list.
     *
     * @param userId     user Id
     * @param timeZone   time Zone
     * @param merchantId merchantId
     * @param option     date option
     * @return ResponseEntity
     */
    @GetMapping("/deal/audience-reach/top-ten/{userType}/{merchantId}/{option}")
    public ResponseEntity<ResponseWrapper> getTopTenDealVsViewCount(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable UserType userType,
            @PathVariable String merchantId,
            @PathVariable String option) {
        try {
            ReportQueryAttribute reportQueryAttribute = userType.equals(UserType.MERCHANT) ?
                    ReportQueryAttribute.MERCHANT_DEAL_ID_DIMENSION : ReportQueryAttribute.BANK_DEAL_ID_DIMENSION;
            var dealViewCountReportResponse =
                    reportService.getTopDealsOrCategoryReport(GraphDateOption.getOption(option), merchantId,
                            timeZone, ReportType.DEAL, reportQueryAttribute);
            List<DealViewCountResponse> dealViewsList = dealSearchService.getDealSetByIds(dealViewCountReportResponse);
            var merchantBusinessResponseDtoMap =
                    getBulkBusinessProfile(dealViewsList);
            final Page<DealViewCountResponse> dealPage = new PageImpl<>(dealViewsList);
            var dealViewsListResponse = new TopTenDealViewsListResponseDto(dealPage, merchantBusinessResponseDtoMap);
            logger.debug("Successfully returned top 10 deal views graph for userId: {}.", userId);
            return getSuccessResponse(dealViewsListResponse, SuccessResponseStatusType.READ_DEAL_VIEW_GRAPH);
        } catch (InvalidDateOptionException e) {
            logger.error("Invalid dateOption. dateOption: {}, userId: {}", option,
                    userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_DATE_OPTION);
        } catch (KupiCoreException e) {
            logger.error("Returning graph of top 10 deals was failed. option: {}, user: {}", option, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Get deal view count list
     *
     * @param userId           userId
     * @param dealId           dealId
     * @param page             page
     * @param size             size
     * @param reportRequestDto reportRequestDto
     * @return audience reach report for merchant business profile
     */
    @PostMapping(value = "/deal/{dealId}/audience-reach/{userType}/{page}/{size}")
    public ResponseEntity<ResponseWrapper> getDealVsViewCount(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable("dealId") String dealId,
            @PathVariable UserType userType,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @RequestBody ReportRequestDto reportRequestDto) {
        try {
            if (!reportRequestDto.isRequiredAvailableForReport()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            if (!reportRequestDto.isValidDateRange()) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_DATE_RANGE);
            }
            if (!reportRequestDto.isSupportedDateRange()) {
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_OPTION_FOR_DATE_RANGE);
            }
            var pageable = PageRequest.of(page, size);
            var dealViewCountReportResponse =
                    reportService.getDealViewsReport(reportRequestDto, userType, ALL, dealId, page, size, timeZone);
            List<DealViewCountResponse> dealViewsList = dealSearchService.getDealSetByIds(dealViewCountReportResponse);
            final Page<DealViewCountResponse> dealPage = new PageImpl<>(dealViewsList, pageable, dealViewsList.size());
            var dealViewsListResponse = new DealViewsListResponseDto(dealPage);

            logger.debug("Successfully returned deal views count list for userId: {}.", userId);
            return getSuccessResponse(dealViewsListResponse, SuccessResponseStatusType.READ_DEAL_VIEW_LIST);
        } catch (KupiCoreException e) {
            logger.error("Returning list of deal views count was failed. reportDateOption: {}, user: {}",
                    reportRequestDto.getOption(), userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get grouped audience reach report.
     *
     * @param userId           userId
     * @param merchantId       merchantId
     * @param page             page
     * @param size             size
     * @param reportRequestDto reportRequestDto
     * @return grouped audience reach report for merchant business profile.
     */
    @PostMapping(value = "/deal/grouped-audience-reach/{userType}/{merchantId}/{page}/{size}")
    public ResponseEntity<ResponseWrapper> groupedAudienceReachReportForDeals(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable UserType userType,
            @PathVariable("merchantId") String merchantId,
            @Min(DEFAULT_PAGE) @PathVariable("page") int page,
            @Min(DEFAULT_PAGE) @Max(PAGE_MAX_SIZE) @Positive @PathVariable("size") int size,
            @RequestBody ReportRequestDto reportRequestDto) {
        try {
            if (reportRequestDto.isRequiredAvailable()) {
                if (!reportRequestDto.isValidDateRange()) {
                    return getErrorResponse(ErrorResponseStatusType.INVALID_DATE_RANGE);
                }
                var dealViewCountReportResponse =
                        reportService.getDealViewsReport(reportRequestDto, userType, merchantId, ALL, page, size, timeZone);
                List<DealViewCountResponse> dealViewsList = dealSearchService.getDealSetByIds(dealViewCountReportResponse);
                var merchantBusinessResponseDtoMap =
                        getBulkBusinessProfile(dealViewsList);
                int totalItems = dealViewsList.isEmpty() ? ZERO : dealViewsList.get(ZERO).getTotalRows();
                var dealViewsListResponse = new GroupedDealViewsListResponseDto(totalItems, page, size,
                        dealViewsList, merchantBusinessResponseDtoMap);
                return getSuccessResponse(dealViewsListResponse, SuccessResponseStatusType.READ_DEAL_VIEW_LIST);
            } else {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (KupiCoreException e) {
            log.error("Getting grouped audience reach report for deals was failed. userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method gets bulk business profile for relevant deals.
     *
     * @param dealViewsList dealViewsList
     * @return bulk business profiles.
     */
    private Map<String, MerchantBusinessResponseDto> getBulkBusinessProfile(List<DealViewCountResponse> dealViewsList) {
        List<String> merchantIds = new ArrayList<>();
        List<String> bankIds = new ArrayList<>();
        Map<String, MerchantBusinessResponseDto> merchantBusinessResponseDtoMap = new HashMap<>();
        for (DealViewCountResponse dealViewCountResponse : dealViewsList) {
            if (dealViewCountResponse.getDealSource().equals(UserType.MERCHANT))
                merchantIds.add(dealViewCountResponse.getMerchantId());
            else
                bankIds.add(dealViewCountResponse.getMerchantId());
        }
        if (!merchantIds.isEmpty())
            merchantBusinessResponseDtoMap =
                    authUserService.getMerchantMap(new BulkUserRequestDto(merchantIds), UserType.MERCHANT);
        if (!bankIds.isEmpty())
            merchantBusinessResponseDtoMap
                    .putAll(authUserService.getMerchantMap(new BulkUserRequestDto(bankIds), UserType.BANK));
        return merchantBusinessResponseDtoMap;
    }

    /**
     * This method is used to get grouped audience reach report for category.
     *
     * @param userId           userId
     * @param timeZone         timeZone
     * @param toUserId         toUserId
     * @param page             page
     * @param size             size
     * @param reportRequestDto reportRequestDto
     * @return category reach list.
     */
    @PostMapping(value = "/grouped-audience-reach/category/user/{toUserId}/{page}/{size}")
    public ResponseEntity<ResponseWrapper> groupedAudienceReachReportForCategory(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable String toUserId,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Min(DEFAULT_PAGE) @Max(PAGE_MAX_SIZE) @Positive @PathVariable int size,
            @RequestBody ReportRequestDto reportRequestDto) {
        try {
            if (reportRequestDto.isRequiredAvailable()) {
                return getCategoryReportResponse(timeZone, toUserId, ALL, page, size, reportRequestDto);
            } else {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (KupiCoreException e) {
            log.error("Getting grouped audience reach report for category was failed. userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get audience reach report for category.
     *
     * @param userId           userId
     * @param timeZone         timeZone
     * @param categoryId       categoryId
     * @param page             page
     * @param size             size
     * @param reportRequestDto reportRequestDto
     * @return category reach list.
     */
    @PostMapping(value = "/audience-reach/category/{categoryId}/{page}/{size}")
    public ResponseEntity<ResponseWrapper> audienceReachReportForCategory(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable String categoryId,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Min(DEFAULT_PAGE) @Max(PAGE_MAX_SIZE) @Positive @PathVariable int size,
            @RequestBody ReportRequestDto reportRequestDto) {
        try {
            if (reportRequestDto.isRequiredAvailableForReport()) {
                if (!reportRequestDto.isSupportedDateRange()) {
                    return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_OPTION_FOR_DATE_RANGE);
                }
                return getCategoryReportResponse(timeZone, ALL, categoryId, page, size, reportRequestDto);
            } else {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (KupiCoreException e) {
            log.error("Getting audience reach report for category was failed. userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This api is used to get top 10 category views list.
     *
     * @param userId   userId
     * @param timeZone timeZone
     * @param option   date option
     * @return top 10 category views.
     */
    @GetMapping("/category/audience-reach/top-ten/{option}")
    public ResponseEntity<ResponseWrapper> getTopTenCategoryReport(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable String option) {
        try {
            var categoryViewCountReportResponse =
                    reportService.getTopDealsOrCategoryReport(GraphDateOption.getOption(option), ALL,
                            timeZone, ReportType.CATEGORY, null);
            List<CategoryViewCountResponseDto> categoryViewsList =
                    categoryService.getCategoriesForReport(categoryViewCountReportResponse, timeZone);
            Page<CategoryViewCountResponseDto> categoryPage = new PageImpl<>(categoryViewsList);
            var categoryReachResponseDto = new CategoryAudienceReachResponseDto(categoryPage, categoryViewsList);
            logger.debug("Successfully returned top 10 category views for userId: {}.", userId);
            return getSuccessResponse(categoryReachResponseDto, SuccessResponseStatusType.CATEGORY_REACH_LIST);
        } catch (InvalidDateOptionException e) {
            logger.error("Invalid dateOption. dateOption: {}, userId: {}", option, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_DATE_OPTION);
        } catch (KupiCoreException e) {
            logger.error("Returning top 10 categories was failed. option: {}, user: {}.", option, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to create category report response.
     *
     * @param timeZone         timeZone
     * @param toUserId         toUserId
     * @param categoryId       categoryId
     * @param page             page
     * @param size             size
     * @param reportRequestDto reportRequestDto
     * @return category report response.
     */
    private ResponseEntity<ResponseWrapper> getCategoryReportResponse(String timeZone, String toUserId,
                                                                      String categoryId, int page, int size,
                                                                      ReportRequestDto reportRequestDto) {
        if (!reportRequestDto.isValidDateRange()) {
            return getErrorResponse(ErrorResponseStatusType.INVALID_DATE_RANGE);
        }
        var categoryViewCountReportResponse =
                reportService.getCategoryReport(reportRequestDto, toUserId, categoryId, page, size, timeZone);
        List<CategoryViewCountResponseDto> categoryViewsList =
                categoryService.getCategoriesForReport(categoryViewCountReportResponse, timeZone);
        int totalItems = categoryViewsList.isEmpty() ? ZERO : categoryViewsList.get(ZERO).getTotalRows();
        var categoryReachResponseDto = new CategoryAudienceReachResponseDto(totalItems, page, size, categoryViewsList);
        return getSuccessResponse(categoryReachResponseDto, SuccessResponseStatusType.CATEGORY_REACH_LIST);
    }
}
