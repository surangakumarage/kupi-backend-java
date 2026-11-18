package com.kupi.cc.auth.service;

// Google Analytics imports disabled in Spring Boot 3.2.6 migration
// import com.google.analytics.data.v1beta.*;
import com.kupi.cc.auth.domain.entity.ApprovedBankBusiness;
import com.kupi.cc.auth.domain.entity.ApprovedBusiness;
import com.kupi.cc.auth.domain.entity.BusinessProfileViews;
import com.kupi.cc.auth.domain.request.ReportRequestDto;
import com.kupi.cc.auth.domain.response.BusinessProfileViewsResponseDto;
import com.kupi.cc.auth.domain.response.DisplayDateResponseDto;
import com.kupi.cc.auth.domain.response.GroupedProfileViewsResponseDto;
import com.kupi.cc.auth.enums.AnalyticsDimensionsAndMetrics;
import com.kupi.cc.auth.enums.GraphDateOption;
import com.kupi.cc.auth.enums.ReportDateOption;
import com.kupi.cc.auth.enums.RoleType;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.repository.ApprovedBankBusinessRepository;
import com.kupi.cc.auth.repository.ApprovedBusinessRepository;
import com.kupi.cc.auth.util.DateRangeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Audience reach report service
 *
 * NOTE: Google Analytics functionality has been disabled in Spring Boot 3.2.6 migration.
 * The BetaAnalyticsDataClient dependency is not available. Methods dependent on this
 * service will return empty results until the library is properly updated.
 */
@Service
@Slf4j
public class MerchantAudienceReachReportService {

    private static final String PROPERTY = "properties/";
    private static final String ANALYTICS_DATE_FORMAT = "yyyy-MM-dd";
    private static final String ALL = "ALL";
    private static final String USER_ID_PREFIX = "uid-";
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private final int pageSize;
    private final String propertyId;
    private final MerchantService merchantService;
    private final ApprovedBusinessRepository approvedBusinessRepository;
    private final ApprovedBankBusinessRepository approvedBankBusinessRepository;
    private final Object analyticsData; // BetaAnalyticsDataClient analyticsData - disabled

    @Autowired
    public MerchantAudienceReachReportService(@Value("${googleAnalytics.pageSize}") int pageSize,
                                              @Value("${googleAnalytics.propertyId}") String propertyId,
                                              MerchantService merchantService,
                                              ApprovedBusinessRepository approvedBusinessRepository,
                                              ApprovedBankBusinessRepository approvedBankBusinessRepository) {
        // BetaAnalyticsDataClient analyticsData parameter removed due to Spring Boot 3.2.6 migration
        this.pageSize = pageSize;
        this.propertyId = propertyId;
        this.merchantService = merchantService;
        this.approvedBusinessRepository = approvedBusinessRepository;
        this.analyticsData = null; // No longer available
        this.approvedBankBusinessRepository = approvedBankBusinessRepository;
    }

    /**
     * This method is used to create audience reach report using Google Analytics.
     *
     * @param reportRequestDto reportRequestDto
     * @param userId           userId/ALL
     * @param page             page
     * @param size             size
     * @param timeZone         timeZone
     * @param roleType         merchant/bank
     * @return list of business profile views
     */
    public List<BusinessProfileViewsResponseDto> generateAudienceReachReport(ReportRequestDto reportRequestDto,
                                                                             String userId, int page, int size,
                                                                             String timeZone, RoleType roleType) {
        try {
            if (!userId.equals("ALL")) {
                merchantService.validateBusinessProfileWithMerchantId(userId, roleType);
            }
            List<BusinessProfileViews> businessProfileViewsList =
                    getAudienceReachList(reportRequestDto, userId, page, size, timeZone, roleType);
            return createAudienceReachResponse(roleType, businessProfileViewsList);
        } catch (DataAccessException e) {
            throw new AuthServiceException("Generating audience reach report was failed.", e);
        }
    }

    /**
     * THis method creates audience reach response according to the user role.
     *
     * @param roleType                 merchant/bank
     * @param businessProfileViewsList businessProfileViewsList
     * @return list of business profile views
     */
    private List<BusinessProfileViewsResponseDto> createAudienceReachResponse
    (RoleType roleType, List<BusinessProfileViews> businessProfileViewsList) {

        List<BusinessProfileViewsResponseDto> businessProfileViewsDtoList = new ArrayList<>();
        if (roleType.equals(RoleType.MERCHANT)) {
            for (BusinessProfileViews businessProfileViews : businessProfileViewsList) {
                Optional<ApprovedBusiness> approvedBusiness =
                        approvedBusinessRepository.findByMerchantId(businessProfileViews.getUserId());
                if (approvedBusiness.isPresent()) {
                    var businessProfileViewsResponse = new DisplayDateResponseDto(businessProfileViews.getViewCount(),
                            businessProfileViews.getDisplayDate(), approvedBusiness.get().getBusiness());
                    businessProfileViewsDtoList.add(businessProfileViewsResponse);
                }
            }
        } else {
            for (BusinessProfileViews businessProfileViews : businessProfileViewsList) {
                Optional<ApprovedBankBusiness> approvedBankBusiness =
                        approvedBankBusinessRepository.findByBankId(businessProfileViews.getUserId());
                if (approvedBankBusiness.isPresent()) {
                    var businessProfileViewsResponse = new DisplayDateResponseDto(businessProfileViews.getViewCount(),
                            businessProfileViews.getDisplayDate(), approvedBankBusiness.get().getBankBusiness());
                    businessProfileViewsDtoList.add(businessProfileViewsResponse);
                }
            }
        }
        return businessProfileViewsDtoList;
    }

    /**
     * Query to get merchant views for date option.
     * DISABLED: Google Analytics not available in Spring Boot 3.2.6
     *
     * @param reportRequestDto reportRequestDto
     * @param userId           userId
     * @param page             page
     * @param size             size
     * @param timeZone         timeZone
     * @param roleType         merchant/bank
     * @return profile view list.
     */
    /*
    public List<BusinessProfileViews> getAudienceReachList(ReportRequestDto reportRequestDto, String userId, int page,
                                                           int size, String timeZone, RoleType roleType) {

        ReportDateOption dateOption = reportRequestDto.getOption();
        long startDate = reportRequestDto.getStartDate();
        long endDate = reportRequestDto.getEndDate();
        AnalyticsDimensionsAndMetrics userRoleDimension = roleType.equals(RoleType.MERCHANT) ?
                AnalyticsDimensionsAndMetrics.MERCHANT_ID_DIMENSION : AnalyticsDimensionsAndMetrics.BANK_ID_DIMENSION;
        var orderBy = OrderBy.newBuilder()
                .setDesc(true)
                .setDimension(OrderBy.DimensionOrderBy.newBuilder()
                        .setDimensionName(dateOption.getAnalyticsOption()));
        var request = RunReportRequest.newBuilder()
                .setProperty(PROPERTY + propertyId)
                .addDateRanges(DateRange.newBuilder()
                        .setStartDate(dateFormatter(startDate, timeZone))
                        .setEndDate(dateFormatter(endDate, timeZone)))
                .addDimensions(Dimension.newBuilder().setName(userRoleDimension.getAnalyticsDimension()))
                .addDimensions(Dimension.newBuilder().setName(dateOption.getAnalyticsOption()))
                .addMetrics(Metric.newBuilder().setName(
                        AnalyticsDimensionsAndMetrics.EVENT_COUNT_METRIC.getAnalyticsDimension()))
                .addOrderBys(orderBy)
                .setLimit(size)
                .setOffset(page);

        if (!userId.equals(ALL)) {
            var filter = FilterExpression.newBuilder()
                    .setFilter(Filter.newBuilder()
                            .setFieldName(userRoleDimension.getAnalyticsDimension())
                            .setStringFilter(Filter.StringFilter.newBuilder().setValue(userId)));
            request.setDimensionFilter(filter);
        }
        RunReportResponse response = analyticsData.runReport(request.build());

        List<BusinessProfileViews> businessProfileViewsList = new ArrayList<>();
        for (Row row : response.getRowsList()) {
            businessProfileViewsList.add(
                    new BusinessProfileViews(row.getDimensionValues(ZERO).getValue(),
                            Long.parseLong(row.getMetricValues(ZERO).getValue()),
                            row.getDimensionValues(ONE).getValue(), dateOption, startDate, endDate, timeZone));
        }
        return businessProfileViewsList;
    }
    */

    public List<BusinessProfileViews> getAudienceReachList(ReportRequestDto reportRequestDto, String userId, int page,
                                                           int size, String timeZone, RoleType roleType) {
        // Google Analytics disabled in Spring Boot 3.2.6 - returning empty list
        return new ArrayList<>();
    }

    /**
     * This method is used to format date to support Google Analytics.
     *
     * @param timeStamp timeStamp
     * @return date in string
     */
    private String dateFormatter(long timeStamp, String timeZone) {
        Date date = new Date(timeStamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ANALYTICS_DATE_FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        return simpleDateFormat.format(date);
    }

    /**
     * This method is used to create top 10 audience reach report using Google Analytics.
     *
     * @param graphDateOption graphDateOption
     * @param timeZone        timeZone
     * @return list of business profile views
     */
    public List<BusinessProfileViewsResponseDto> getTopTenViewCounts(GraphDateOption graphDateOption, String timeZone,
                                                                     RoleType roleType) {
        try {
            List<BusinessProfileViews> businessProfileViewsList =
                    getTopTenAudienceReachList(graphDateOption, timeZone, roleType);
            List<BusinessProfileViewsResponseDto> businessProfileViewsDtoList = new ArrayList<>();
            if (roleType.equals(RoleType.MERCHANT)) {
                for (BusinessProfileViews businessProfileViews : businessProfileViewsList) {
                    Optional<ApprovedBusiness> approvedBusiness =
                            approvedBusinessRepository.findByMerchantId(businessProfileViews.getUserId());
                    if (approvedBusiness.isPresent()) {
                        var businessProfileViewsDto =
                                new BusinessProfileViewsResponseDto(businessProfileViews.getViewCount(),
                                        approvedBusiness.get().getBusiness());
                        businessProfileViewsDtoList.add(businessProfileViewsDto);
                    }
                }
            } else {
                for (BusinessProfileViews businessProfileViews : businessProfileViewsList) {
                    Optional<ApprovedBankBusiness> approvedBankBusiness =
                            approvedBankBusinessRepository.findByBankId(businessProfileViews.getUserId());
                    if (approvedBankBusiness.isPresent()) {
                        var businessProfileViewsDto =
                                new BusinessProfileViewsResponseDto(businessProfileViews.getViewCount(),
                                        approvedBankBusiness.get().getBankBusiness());
                        businessProfileViewsDtoList.add(businessProfileViewsDto);
                    }
                }
            }
            return businessProfileViewsDtoList;
        } catch (DataAccessException e) {
            throw new AuthServiceException("Generating top 10 audience reach report was failed.", e);
        }
    }

    /**
     * Query to get top merchant/bank view counts form google analytics.
     * DISABLED: Google Analytics not available in Spring Boot 3.2.6
     *
     * @param graphDateOption graphDateOption
     * @param timeZone        timeZone
     * @return profile view list.
     */
    /*
    public List<BusinessProfileViews> getTopTenAudienceReachList(GraphDateOption graphDateOption, String timeZone,
                                                                 RoleType roleType) {

        String startDate = new DateRangeConverter(timeZone, graphDateOption).getStartDate();
        String endDate = new DateRangeConverter(timeZone, graphDateOption).getEndDate();
        AnalyticsDimensionsAndMetrics profileDimension = roleType.equals(RoleType.MERCHANT) ?
                AnalyticsDimensionsAndMetrics.MERCHANT_ID_DIMENSION : AnalyticsDimensionsAndMetrics.BANK_ID_DIMENSION;
        var orderBy = OrderBy.newBuilder()
                .setDesc(true)
                .setMetric(OrderBy.MetricOrderBy.newBuilder()
                        .setMetricName(AnalyticsDimensionsAndMetrics.EVENT_COUNT_METRIC.getAnalyticsDimension()));

        RunReportRequest request = RunReportRequest.newBuilder()
                .setProperty(PROPERTY + propertyId)
                .addDateRanges(DateRange.newBuilder().setStartDate(startDate).setEndDate(endDate))
                .addDimensions(Dimension.newBuilder().setName(profileDimension.getAnalyticsDimension()))
                .addMetrics(Metric.newBuilder()
                        .setName(AnalyticsDimensionsAndMetrics.EVENT_COUNT_METRIC.getAnalyticsDimension()))
                .setDimensionFilter(addFilterForReportRequest(profileDimension))
                .addOrderBys(orderBy)
                .setLimit(pageSize)
                .build();

        RunReportResponse response = analyticsData.runReport(request);
        return getViewList(response);
    }
    */

    public List<BusinessProfileViews> getTopTenAudienceReachList(GraphDateOption graphDateOption, String timeZone,
                                                                 RoleType roleType) {
        // Google Analytics disabled in Spring Boot 3.2.6 - returning empty list
        return new ArrayList<>();
    }

    /**
     * This method is used to get audience reach report for date range.
     *
     * @param startDate start date
     * @param endDate   end date
     * @param userId    userId
     * @param page      page
     * @param size      size
     * @param roleType  roleType
     * @param timeZone  timeZone
     * @return merchant list with view counts.
     */
    public List<GroupedProfileViewsResponseDto> getReportForDateRange(long startDate, long endDate, String userId,
                                                                      int page, int size, RoleType roleType,
                                                                      String timeZone) {
        try {
            String formattedStartDate = dateFormatter(startDate, timeZone);
            String formattedEndDate = dateFormatter(endDate, timeZone);
            List<BusinessProfileViews> businessProfileViewsList =
                    getGroupedAudienceReachList(formattedStartDate, formattedEndDate, userId, page, size, roleType);
            List<GroupedProfileViewsResponseDto> groupedProfileViewsResponseDtoList = new ArrayList<>();

            if (roleType.equals(RoleType.MERCHANT)) {
                for (BusinessProfileViews businessProfileViews : businessProfileViewsList) {
                    Optional<ApprovedBusiness> approvedBusiness =
                            approvedBusinessRepository.findByMerchantId(businessProfileViews.getUserId());
                    approvedBusiness.ifPresent(value ->
                            groupedProfileViewsResponseDtoList.add(new GroupedProfileViewsResponseDto(
                                    value.getBusiness(),
                                    businessProfileViews.getViewCount(),
                                    businessProfileViews.getTotalRows())));
                }
            } else {
                for (BusinessProfileViews businessProfileViews : businessProfileViewsList) {
                    Optional<ApprovedBankBusiness> approvedBankBusiness =
                            approvedBankBusinessRepository.findByBankId(businessProfileViews.getUserId());
                    approvedBankBusiness.ifPresent(value ->
                            groupedProfileViewsResponseDtoList.add(new GroupedProfileViewsResponseDto(
                                    value.getBankBusiness(),
                                    businessProfileViews.getViewCount(),
                                    businessProfileViews.getTotalRows())));
                }
            }
            return groupedProfileViewsResponseDtoList;
        } catch (DataAccessException e) {
            throw new AuthServiceException("Generating report for date range was failed.", e);
        }
    }

    /**
     * Query to get merchant views in date range.
     * DISABLED: Google Analytics not available in Spring Boot 3.2.6
     *
     * @param startDate startDate
     * @param endDate   endDate
     * @param page      page
     * @param size      size
     * @return profile view list.
     */
    /*
    public List<BusinessProfileViews> getGroupedAudienceReachList(String startDate, String endDate, String userId,
                                                                  int page, int size, RoleType roleType) {

        AnalyticsDimensionsAndMetrics userRoleDimension = roleType.equals(RoleType.MERCHANT) ?
                AnalyticsDimensionsAndMetrics.MERCHANT_ID_DIMENSION : AnalyticsDimensionsAndMetrics.BANK_ID_DIMENSION;

        var request = RunReportRequest.newBuilder()
                .setProperty(PROPERTY + propertyId)
                .addDateRanges(DateRange.newBuilder().setStartDate(startDate).setEndDate(endDate))
                .addDimensions(Dimension.newBuilder()
                        .setName(userRoleDimension.getAnalyticsDimension()))
                .addMetrics(Metric.newBuilder()
                        .setName(AnalyticsDimensionsAndMetrics.EVENT_COUNT_METRIC.getAnalyticsDimension()))
                .setDimensionFilter(addFilterForReportRequest(userRoleDimension))
                .setLimit(size)
                .setOffset((long) page * size);

        if (!userId.equals(ALL)) {
            var filter = FilterExpression.newBuilder()
                    .setFilter(Filter.newBuilder()
                            .setFieldName(userRoleDimension.getAnalyticsDimension())
                            .setStringFilter(Filter.StringFilter.newBuilder().setValue(userId)));
            request.setDimensionFilter(filter);
        }

        RunReportResponse response = analyticsData.runReport(request.build());
        return getViewList(response);
    }
    */

    public List<BusinessProfileViews> getGroupedAudienceReachList(String startDate, String endDate, String userId,
                                                                  int page, int size, RoleType roleType) {
        // Google Analytics disabled in Spring Boot 3.2.6 - returning empty list
        return new ArrayList<>();
    }

    /**
     * This method is used to ignore "not set" values from Google Analytics.
     * DISABLED: FilterExpression not available in Spring Boot 3.2.6
     *
     * @param queryDimension merchantId/bankId dimension.
     * @return analytics filter.
     */
    /*
    private FilterExpression.Builder addFilterForReportRequest(AnalyticsDimensionsAndMetrics queryDimension) {
        return FilterExpression.newBuilder()
                .setFilter(Filter.newBuilder()
                        .setFieldName(queryDimension.getAnalyticsDimension())
                        .setStringFilter(Filter.StringFilter.newBuilder()
                                .setMatchType(Filter.StringFilter.MatchType.BEGINS_WITH)
                                .setValue(USER_ID_PREFIX)));
    }
    */

    /**
     * This method will add analytics response into local list.
     * DISABLED: RunReportResponse not available in Spring Boot 3.2.6
     *
     * @param response analytics response
     * @return profile view list.
     */
    /*
    private List<BusinessProfileViews> getViewList(RunReportResponse response) {
        List<BusinessProfileViews> businessProfileViewsList = new ArrayList<>();
        for (Row row : response.getRowsList()) {
            businessProfileViewsList.add(new BusinessProfileViews(row.getDimensionValues(ZERO).getValue(),
                    Long.parseLong(row.getMetricValues(ZERO).getValue()), response.getRowCount()));
        }
        return businessProfileViewsList;
    }
    */
}
