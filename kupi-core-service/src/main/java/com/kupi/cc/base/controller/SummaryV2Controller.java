package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.response.dashboardV2.ActivityLogResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.AdminDashboardSummaryResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.DealDetailsTrendsResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.DealPerformanceListResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.EarningTrendsResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.MerchantSummaryResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.TopPerformingDealsResponseDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.service.SummaryService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@Slf4j
@RequestMapping("/api/v2/summary")
public class SummaryV2Controller extends Controller {

    private final SummaryService summaryService;

    @Autowired
    public SummaryV2Controller(Translator translator, SummaryService summaryService) {
        super(translator);
        this.summaryService = summaryService;
    }

    @GetMapping("/{userType}/filter")
    public ResponseEntity<ResponseWrapper> getSummary(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                      @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                      @PathVariable UserType userType,
                                                      @RequestParam(required = false) String startDate,
                                                      @RequestParam(required = false) String endDate,
                                                      HttpServletRequest request) {
        try {
            if (userType == UserType.ADMIN) {
                String authToken = request.getHeader(AUTH_TOKEN_HEADER);
                AdminDashboardSummaryResponseDto adminSummary = summaryService
                        .getAdminSummary(userId, authToken, userType, timeZone);
                return getSuccessResponse(adminSummary, SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY);
            }
            ZoneId zone = ZoneId.of(timeZone);
            LocalDate today = LocalDate.now(zone);
            long startMillis;
            long endMillis;
            if (startDate == null || endDate == null) {
                startMillis = today.atStartOfDay(zone).toInstant().toEpochMilli();
                endMillis = ZonedDateTime.now(zone).toInstant().toEpochMilli();
            } else {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                startMillis = start.atStartOfDay(zone).toInstant().toEpochMilli();
                endMillis = end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1;
            }
            if (startMillis > endMillis) {
                throw new IllegalArgumentException("Start date must be before end date");
            }
            MerchantSummaryResponseDto summary =
                    summaryService.getMerchantSummary(userId, startMillis, endMillis, timeZone);
            return getSuccessResponse(summary, SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY);

        } catch (Exception e) {
            log.error("Summary fetch failed for userId={}, userType={}", userId, userType, e);
            return getInternalServerError();
        }
    }

    @GetMapping("/{userType}/earning-trends")
    public ResponseEntity<ResponseWrapper> getEarningTrends(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                           @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                           @PathVariable UserType userType,
                                                           @RequestParam(required = false) String startDate,
                                                           @RequestParam(required = false) String endDate) {
        try {
            if (userType == UserType.ADMIN) {
                log.debug("Earning trends endpoint is not supported for ADMIN userType");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            ZoneId zone = ZoneId.of(timeZone);
            LocalDate today = LocalDate.now(zone);
            long startMillis;
            long endMillis;
            if (startDate == null || endDate == null) {
                startMillis = today.atStartOfDay(zone).toInstant().toEpochMilli();
                endMillis = ZonedDateTime.now(zone).toInstant().toEpochMilli();
            } else {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                startMillis = start.atStartOfDay(zone).toInstant().toEpochMilli();
                endMillis = end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1;
            }
            if (startMillis > endMillis) {
                throw new IllegalArgumentException("Start date must be before end date");
            }
            EarningTrendsResponseDto earningTrends = summaryService.getEarningTrends(userId, startMillis, endMillis, timeZone);
            return getSuccessResponse(earningTrends, SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY);

        } catch (Exception e) {
            log.error("Earning trends fetch failed for userId={}, userType={}", userId, userType, e);
            return getInternalServerError();
        }
    }

    @GetMapping("/{userType}/deal-details-trends")
    public ResponseEntity<ResponseWrapper> getDealDetailsTrends(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                                @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                @PathVariable UserType userType,
                                                                @RequestParam(required = false) String startDate,
                                                                @RequestParam(required = false) String endDate) {
        try {
            if (userType == UserType.ADMIN) {
                log.debug("Deal details trends endpoint is not supported for ADMIN userType");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            ZoneId zone = ZoneId.of(timeZone);
            LocalDate today = LocalDate.now(zone);
            long startMillis;
            long endMillis;
            if (startDate == null || endDate == null) {
                startMillis = today.atStartOfDay(zone).toInstant().toEpochMilli();
                endMillis = ZonedDateTime.now(zone).toInstant().toEpochMilli();
            } else {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                startMillis = start.atStartOfDay(zone).toInstant().toEpochMilli();
                endMillis = end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1;
            }
            if (startMillis > endMillis) {
                throw new IllegalArgumentException("Start date must be before end date");
            }
            DealDetailsTrendsResponseDto dealDetailsTrends =
                    summaryService.getDealDetailsTrends(userId, startMillis, endMillis, timeZone);
            return getSuccessResponse(dealDetailsTrends, SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY);

        } catch (Exception e) {
            log.error("Deal details trends fetch failed for userId={}, userType={}", userId, userType, e);
            return getInternalServerError();
        }
    }

    @GetMapping("/{userType}/top-performing-deals")
    public ResponseEntity<ResponseWrapper> getTopPerformingDeals(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                                  @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                  @PathVariable UserType userType,
                                                                  @RequestParam(required = false) String startDate,
                                                                  @RequestParam(required = false) String endDate,
                                                                  @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        try {
            if (userType == UserType.ADMIN) {
                log.debug("Top performing deals endpoint is not supported for ADMIN userType");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            ZoneId zone = ZoneId.of(timeZone);
            LocalDate today = LocalDate.now(zone);
            long startMillis;
            long endMillis;
            if (startDate == null || endDate == null) {
                startMillis = today.atStartOfDay(zone).toInstant().toEpochMilli();
                endMillis = ZonedDateTime.now(zone).toInstant().toEpochMilli();
            } else {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                startMillis = start.atStartOfDay(zone).toInstant().toEpochMilli();
                endMillis = end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1;
            }
            if (startMillis > endMillis) {
                throw new IllegalArgumentException("Start date must be before end date");
            }
            TopPerformingDealsResponseDto topDeals = summaryService.getTopPerformingDeals(userId, startMillis, endMillis, limit);
            return getSuccessResponse(topDeals, SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY);

        } catch (Exception e) {
            log.error("Top performing deals fetch failed for userId={}, userType={}", userId, userType, e);
            return getInternalServerError();
        }
    }

    /**
     * User engagement trends endpoint.
     * Tracks the number of deal views and sales over time in the selected period.
     *
     * @param userId   merchant user id
     * @param timeZone time zone
     * @param userType user type (only MERCHANT/BANK supported)
     * @param startDate optional start date (yyyy-MM-dd), defaults to start of today
     * @param endDate   optional end date (yyyy-MM-dd), defaults to now
     * @return user engagement trends response
     */
    @GetMapping("/{userType}/user-engagement-trends/deal/{dealId}")
    public ResponseEntity<ResponseWrapper> getUserEngagementTrends(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable UserType userType,
            @PathVariable String dealId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            if (userType == UserType.ADMIN) {
                log.debug("User engagement trends endpoint is not supported for ADMIN userType");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            ZoneId zone = ZoneId.of(timeZone);
            LocalDate today = LocalDate.now(zone);
            long startMillis;
            long endMillis;
            if (startDate == null || endDate == null) {
                startMillis = today.atStartOfDay(zone).toInstant().toEpochMilli();
                endMillis = ZonedDateTime.now(zone).toInstant().toEpochMilli();
            } else {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                startMillis = start.atStartOfDay(zone).toInstant().toEpochMilli();
                endMillis = end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1;
            }
            if (startMillis > endMillis) {
                throw new IllegalArgumentException("Start date must be before end date");
            }
            String selectedDealId = ALL.equals(dealId) ? null : dealId;
            var userEngagementTrends =
                    summaryService.getUserEngagementTrends(userId, selectedDealId, startMillis, endMillis, timeZone);
            return getSuccessResponse(userEngagementTrends, SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY);

        } catch (Exception e) {
            log.error("User engagement trends fetch failed for userId={}, userType={}", userId, userType, e);
            return getInternalServerError();
        }
    }

    /**
     * Deal Performance endpoint.
     * Returns detailed performance metrics for each deal including views, sales, redemptions, conversion rate, and earnings.
     *
     * @param userId   merchant user id
     * @param timeZone time zone
     * @param userType user type (only MERCHANT/BANK supported)
     * @param startDate optional start date (yyyy-MM-dd), defaults to start of today
     * @param endDate   optional end date (yyyy-MM-dd), defaults to now
     * @param page     page number (default: 0)
     * @param size     page size (default: 10, max: 250)
     * @return deal performance list response
     */
    @GetMapping("/{userType}/deal-performance")
    public ResponseEntity<ResponseWrapper> getDealPerformance(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable UserType userType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Positive @Max(PAGE_MAX_SIZE) int size) {
        try {
            // Deal performance is only for merchant deals
            if (userType != UserType.MERCHANT) {
                log.debug("Deal performance endpoint is not supported for userType {}", userType);
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            ZoneId zone = ZoneId.of(timeZone);
            LocalDate today = LocalDate.now(zone);
            long startMillis;
            long endMillis;
            if (startDate == null || endDate == null) {
                startMillis = today.atStartOfDay(zone).toInstant().toEpochMilli();
                endMillis = ZonedDateTime.now(zone).toInstant().toEpochMilli();
            } else {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                startMillis = start.atStartOfDay(zone).toInstant().toEpochMilli();
                endMillis = end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1;
            }
            if (startMillis > endMillis) {
                throw new IllegalArgumentException("Start date must be before end date");
            }
            // Use native query ordering (created_at column) and keep pageable unsorted to avoid column name mismatch
            Pageable pageable = PageRequest.of(page, size);
            DealPerformanceListResponseDto dealPerformance =
                    summaryService.getDealPerformance(userId, startMillis, endMillis, timeZone, pageable, searchTerm);
            return getSuccessResponse(dealPerformance, SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY);

        } catch (Exception e) {
            log.error("Deal performance fetch failed for userId={}, userType={}", userId, userType, e);
            return getInternalServerError();
        }
    }

    @Secured({ADMIN_ROLE})
    @GetMapping("/{userType}/merchants")
    public ResponseEntity<ResponseWrapper> getAllMerchants(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable UserType userType,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) @Min(DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + PAGE_MAX_SIZE) @Positive @Max(PAGE_MAX_SIZE) int size) {

        try {
            if (userType != UserType.ADMIN) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "Admin Summary Dashboard.");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            var response = summaryService.getMerchantAndDealCount(searchTerm, page, size, userId, timeZone);
            return getSuccessResponse(response, SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY);

        } catch (KupiCoreException e) {
            log.error("Returning merchant details for admin dashboard failed", e);
            return getInternalServerError();
        }
    }

    /**
     * Activity Log endpoint.
     * Returns real-time log of all deal activities including redemptions, publications, and updates.
     *
     * @param userId   merchant user id
     * @param timeZone time zone
     * @param userType user type (only MERCHANT/BANK supported)
     * @param page     page number
     * @param size     page size
     * @return activity log response
     */
    @GetMapping("/{userType}/activity-log/{page}/{size}")
    public ResponseEntity<ResponseWrapper> getActivityLog(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable UserType userType,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size) {
        try {
            if (userType == UserType.ADMIN) {
                log.debug("Activity log endpoint is not supported for ADMIN userType");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            Pageable pageable = PageRequest.of(page, size);
            ActivityLogResponseDto activityLog = summaryService.getActivityLog(userId, timeZone, pageable);
            return getSuccessResponse(activityLog, SuccessResponseStatusType.SUCCESSFULLY_RETURNED_SUMMARY);

        } catch (Exception e) {
            log.error("Activity log fetch failed for userId={}, userType={}", userId, userType, e);
            return getInternalServerError();
        }
    }

    /**
     * Total Savings endpoint.
     * Returns combined total savings for the logged-in user and the entire app.
     * Always returns:
     * - userSavings     → savings for the authenticated user
     * - totalAppSavings → total savings across all users
     *
     * @param authUserId user id from header (authenticated user)
     * @return total savings response
     */
    @GetMapping("/total-savings")
    public ResponseEntity<ResponseWrapper> getTotalSavings(
            @RequestHeader(name = USER_ID_HEADER) String authUserId) {
        try {
            var combinedSavings = summaryService.getCombinedTotalSavings(authUserId);
            return getSuccessResponse(combinedSavings, SuccessResponseStatusType.TOTAL_SAVINGS_ALL);
        } catch (Exception e) {
            log.error("Total savings fetch failed for authUserId={}", authUserId, e);
            return getInternalServerError();
        }
    }
}