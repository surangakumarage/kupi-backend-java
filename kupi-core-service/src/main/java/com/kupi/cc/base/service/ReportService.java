package com.kupi.cc.base.service;

// import com.google.analytics.data.v1beta.*;
import com.kupi.cc.base.domain.request.ReportRequestDto;
import com.kupi.cc.base.domain.response.ReportDateResponseDto;
import com.kupi.cc.base.domain.response.ViewCountAnalyticResponseDto;
import com.kupi.cc.base.enums.*;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.util.DateRangeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * MIGRATION NOTE: Google Analytics functionality is temporarily disabled for Java 21 + Spring Boot 3.2.6 migration.
 * The google-analytics-data dependency version used (0.42.1) is not available in Maven Central.
 *
 * To re-enable:
 * 1. Update pom.xml with a compatible google-analytics-data version
 * 2. Uncomment the imports above
 * 3. Uncomment the analyticsData field and constructor parameter
 * 4. Uncomment all methods that use analyticsData
 */
@Service
public class ReportService {

    private static final String PROPERTY = "properties/";
    private static final String ANALYTICS_DATE_FORMAT = "yyyy-MM-dd";
    private static final String ALL = "ALL";
    private static final String DEAL_ID_PREFIX = "did-";
    private static final String CATEGORY_ID_PREFIX = "cid-";
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private final String propertyId;
    private final int topViewsCount;
    // private final BetaAnalyticsDataClient analyticsData;

    @Autowired
    public ReportService(@Value("${analytics.propertyId}") String propertyId,
                         @Value("${analytics.topViewsCount}") int topViewsCount) {
                         // BetaAnalyticsDataClient analyticsData) {
        this.propertyId = propertyId;
        this.topViewsCount = topViewsCount;
        // this.analyticsData = analyticsData;
    }

    /**
     * DISABLED: Google Analytics functionality disabled for Java 21 + Spring Boot 3.2.6 migration.
     * This method gives top view counts for deal/category by querying analytics.
     * When querying google analytics we query for no of top count + 1 to omit the "(not set)" values.
     * For category report we query for top count + 2 to omit "(not set)" & "ALL" values.
     *
     * @param graphDateOption    date range enum value
     * @param merchantId         merchant Id
     * @param timeZone           time zone
     * @param reportType         DEAL/CATEGORY
     * @param dealQueryAttribute bank/merchant deal
     * @return GetReportsResponse
     */
    public List<ViewCountAnalyticResponseDto> getTopDealsOrCategoryReport(GraphDateOption graphDateOption,
                                                                          String merchantId, String timeZone,
                                                                          ReportType reportType,
                                                                          ReportQueryAttribute dealQueryAttribute) {
        // DISABLED: Google Analytics integration temporarily disabled
        return new ArrayList<>();

        /* DISABLED CODE - Re-enable when google-analytics-data dependency is available
        String startDate = new DateRangeConverter(timeZone, graphDateOption).getStartDate();
        String endDate = new DateRangeConverter(timeZone, graphDateOption).getEndDate();
        ReportQueryAttribute reportQueryAttribute = reportType.equals(ReportType.DEAL) ?
                dealQueryAttribute : ReportQueryAttribute.CATEGORY_ID_DIMENSION;
        String filterPrefix = reportType.equals(ReportType.DEAL) ? DEAL_ID_PREFIX : CATEGORY_ID_PREFIX;
        var orderBy = OrderBy.newBuilder()
                .setDesc(true)
                .setMetric(OrderBy.MetricOrderBy.newBuilder()
                        .setMetricName(ReportQueryAttribute.EVENT_COUNT_METRIC.getQueryAttribute()));

        var request =
                createAnalyticsRequestBuilder(startDate, endDate, topViewsCount, reportQueryAttribute, filterPrefix);
        if (!merchantId.equals(ALL))
            filterByIdPrefix(request, merchantId, reportQueryAttribute, filterPrefix);

        request.addOrderBys(orderBy);
        RunReportResponse response = analyticsData.runReport(request.build());
        if (reportType.equals(ReportType.DEAL))
            return createDealViewCountAnalyticResponseDtoEntity(response, null,
                    dateToTimestamp(startDate), dateToTimestamp(endDate), timeZone);
        else
            return createCategoryViewCountAnalyticResponseDtoEntity(response, null,
                    dateToTimestamp(startDate), dateToTimestamp(endDate), timeZone);
        */
    }

    /**
     * DISABLED: Google Analytics functionality disabled for Java 21 + Spring Boot 3.2.6 migration.
     * This method generates deals report by querying google analytics.
     *
     * @param reportRequestDto reportRequestDto
     * @param userType         MERCHANT/BANK
     * @param merchantId       merchantId
     * @param dealId           dealId
     * @param page             page
     * @param size             size
     * @param timeZone         timeZone
     * @return GetReportsResponse
     */
    public List<ViewCountAnalyticResponseDto> getDealViewsReport(ReportRequestDto reportRequestDto, UserType userType,
                                                                 String merchantId, String dealId, int page,
                                                                 int size, String timeZone) {
        // DISABLED: Google Analytics integration temporarily disabled
        return new ArrayList<>();

        /* DISABLED CODE - Re-enable when google-analytics-data dependency is available
        ReportDateOption dateOption = reportRequestDto.getOption();
        long startDate = reportRequestDto.getStartDate();
        long endDate = reportRequestDto.getEndDate();
        ReportQueryAttribute reportQueryAttribute = userType.equals(UserType.MERCHANT) ?
                ReportQueryAttribute.MERCHANT_DEAL_ID_DIMENSION : ReportQueryAttribute.BANK_DEAL_ID_DIMENSION;

        var request = createAnalyticsRequestBuilder(dateFormatter(startDate, timeZone),
                dateFormatter(endDate, timeZone), size, reportQueryAttribute, DEAL_ID_PREFIX);
        request.setOffset((long) page * size);

        if (!merchantId.equals(ALL))
            filterByIdPrefix(request, merchantId, reportQueryAttribute, DEAL_ID_PREFIX);
        if (!dealId.equals(ALL)) {
            request = addFilterForReportRequest(request, dealId, reportQueryAttribute);
            if (dateOption != null) {
                request = addDateOptionAndOrderByToReportRequest(dateOption, request);
            }
        }

        RunReportResponse response = analyticsData.runReport(request.build());
        return createDealViewCountAnalyticResponseDtoEntity(response, dateOption, startDate, endDate, timeZone);
        */
    }

    // DISABLED: Google Analytics functionality disabled for Java 21 + Spring Boot 3.2.6 migration
    // private RunReportRequest.Builder addDateOptionAndOrderByToReportRequest(ReportDateOption dateOption,
    //                                                                         RunReportRequest.Builder request) {
    //     var orderBy = OrderBy.newBuilder()
    //             .setDesc(true)
    //             .setDimension(OrderBy.DimensionOrderBy.newBuilder()
    //                     .setDimensionName(dateOption.getAnalyticsOption()));
    //     return request.addDimensions(Dimension.newBuilder().setName(dateOption.getAnalyticsOption()))
    //             .addOrderBys(orderBy);
    // }

    /**
     * DISABLED: Google Analytics functionality disabled for Java 21 + Spring Boot 3.2.6 migration.
     * This method generates categories report by querying google analytics.
     *
     * @param reportRequestDto reportRequestDto
     * @param userId           userId
     * @param categoryId       userId
     * @param page             page
     * @param size             size
     * @param timeZone         timeZone
     * @return categoryIds with view count.
     */
    public List<ViewCountAnalyticResponseDto> getCategoryReport(ReportRequestDto reportRequestDto, String userId,
                                                                String categoryId, int page, int size,
                                                                String timeZone) {
        // DISABLED: Google Analytics integration temporarily disabled
        return new ArrayList<>();

        /* DISABLED CODE - Re-enable when google-analytics-data dependency is available
        ReportDateOption dateOption = reportRequestDto.getOption();
        long startDate = reportRequestDto.getStartDate();
        long endDate = reportRequestDto.getEndDate();
        var request = createAnalyticsRequestBuilder(dateFormatter(startDate, timeZone),
                dateFormatter(endDate, timeZone), size, ReportQueryAttribute.CATEGORY_ID_DIMENSION, CATEGORY_ID_PREFIX);
        request.setOffset((long) page * size);

        if (!userId.equals(ALL))
            filterByIdPrefix(request, userId, ReportQueryAttribute.CATEGORY_ID_DIMENSION, CATEGORY_ID_PREFIX);
        if (!categoryId.equals(ALL)) {
            request = addFilterForReportRequest(request, categoryId, ReportQueryAttribute.CATEGORY_ID_DIMENSION);
            if (dateOption != null) {
                request = addDateOptionAndOrderByToReportRequest(dateOption, request);
            }
        }

        RunReportResponse response = analyticsData.runReport(request.build());
        return createCategoryViewCountAnalyticResponseDtoEntity(response, dateOption, startDate, endDate, timeZone);
        */
    }

    // DISABLED: Google Analytics functionality disabled for Java 21 + Spring Boot 3.2.6 migration
    // private RunReportRequest.Builder createAnalyticsRequestBuilder(String startDate, String endDate, int size,
    //                                                                ReportQueryAttribute queryDimension,
    //                                                                String filterPrefix) {
    //     return RunReportRequest.newBuilder()
    //             .setProperty(PROPERTY + propertyId)
    //             .addDateRanges(DateRange.newBuilder().setStartDate(startDate).setEndDate(endDate))
    //             .addDimensions(Dimension.newBuilder()
    //                     .setName(queryDimension.getQueryAttribute()))
    //             .addMetrics(Metric.newBuilder()
    //                     .setName(ReportQueryAttribute.EVENT_COUNT_METRIC.getQueryAttribute()))
    //             .setDimensionFilter(FilterExpression.newBuilder().setFilter(Filter.newBuilder()
    //                     .setFieldName(queryDimension.getQueryAttribute())
    //                     .setStringFilter(Filter.StringFilter.newBuilder()
    //                             .setMatchType(Filter.StringFilter.MatchType.BEGINS_WITH)
    //                             .setValue(filterPrefix))))
    //             .setLimit(size);
    // }

    // DISABLED: Google Analytics functionality disabled for Java 21 + Spring Boot 3.2.6 migration
    // private RunReportRequest.Builder addFilterForReportRequest(RunReportRequest.Builder request, String filteringValue,
    //                                                            ReportQueryAttribute queryDimension) {
    //     var filter = FilterExpression.newBuilder()
    //             .setFilter(Filter.newBuilder()
    //                     .setFieldName(queryDimension.getQueryAttribute())
    //                     .setStringFilter(Filter.StringFilter.newBuilder().setValue(filteringValue)));
    //     return request.setDimensionFilter(filter);
    // }
    //
    // private void filterByIdPrefix(RunReportRequest.Builder request, String filteringValue,
    //                               ReportQueryAttribute queryDimension, String filteringPrefix) {
    //     ReportQueryAttribute userTypeDimension = queryDimension == ReportQueryAttribute.MERCHANT_DEAL_ID_DIMENSION ?
    //             ReportQueryAttribute.MERCHANT_ID_DIMENSION : ReportQueryAttribute.BANK_ID_DIMENSION;
    //     var filter = FilterExpression.newBuilder()
    //             .setAndGroup(FilterExpressionList.newBuilder()
    //                     .addExpressions(FilterExpression.newBuilder()
    //                             .setFilter(Filter.newBuilder()
    //                                     .setFieldName(userTypeDimension.getQueryAttribute())
    //                                     .setStringFilter(Filter.StringFilter.newBuilder().setValue(filteringValue))))
    //                     .addExpressions(FilterExpression.newBuilder()
    //                             .setFilter(Filter.newBuilder()
    //                                     .setFieldName(queryDimension.getQueryAttribute())
    //                                     .setStringFilter(Filter.StringFilter.newBuilder()
    //                                             .setMatchType(Filter.StringFilter.MatchType.BEGINS_WITH)
    //                                             .setValue(filteringPrefix)))));
    //     request.setDimensionFilter(filter);
    // }

    // DISABLED: Google Analytics functionality disabled for Java 21 + Spring Boot 3.2.6 migration
    // private List<ViewCountAnalyticResponseDto>
    // createDealViewCountAnalyticResponseDtoEntity(RunReportResponse response, ReportDateOption reportDateOption,
    //                                              long startDate, long endDate, String timeZone) {
    //     List<ViewCountAnalyticResponseDto> dealViewsList = new ArrayList<>();
    //     for (Row row : response.getRowsList()) {
    //         ViewCountAnalyticResponseDto viewCountAnalyticResponseDto = new ViewCountAnalyticResponseDto();
    //         viewCountAnalyticResponseDto.setDealId(row.getDimensionValues(ZERO).getValue());
    //         viewCountAnalyticResponseDto.setViewCount(Long.parseLong(row.getMetricValues(ZERO).getValue()));
    //         viewCountAnalyticResponseDto.setTotalRows(response.getRowCount());
    //         if (row.getDimensionValuesCount() == TWO) {
    //             ReportDateResponseDto displayDate = new ReportDateResponseDto(reportDateOption,
    //                     row.getDimensionValues(ONE).getValue(), startDate, endDate, timeZone);
    //             viewCountAnalyticResponseDto.setDisplayDate(displayDate.getDisplayDate());
    //         }
    //         dealViewsList.add(viewCountAnalyticResponseDto);
    //     }
    //     return dealViewsList;
    // }

    // DISABLED: Google Analytics functionality disabled for Java 21 + Spring Boot 3.2.6 migration
    // private List<ViewCountAnalyticResponseDto>
    // createCategoryViewCountAnalyticResponseDtoEntity(RunReportResponse response, ReportDateOption reportDateOption,
    //                                                  long startDate, long endDate, String timeZone) {
    //     List<ViewCountAnalyticResponseDto> dealViewsList = new ArrayList<>();
    //     for (Row row : response.getRowsList()) {
    //         var dealViews = new ViewCountAnalyticResponseDto(row.getDimensionValues(ZERO).getValue(),
    //                 Long.parseLong(row.getMetricValues(ZERO).getValue()));
    //         dealViews.setTotalRows(response.getRowCount());
    //         if (row.getDimensionValuesCount() == TWO) {
    //             ReportDateResponseDto displayDate = new ReportDateResponseDto(reportDateOption,
    //                     row.getDimensionValues(ONE).getValue(), startDate, endDate, timeZone);
    //             dealViews.setDisplayDate(displayDate.getDisplayDate());
    //         }
    //         dealViewsList.add(dealViews);
    //     }
    //     return dealViewsList;
    // }

    /**
     * This method converts sting dates to timestamp.
     *
     * @param dateInString dateInString
     * @return timestamp
     */
    private long dateToTimestamp(String dateInString) {
        try {
            DateFormat formatter = new SimpleDateFormat(ANALYTICS_DATE_FORMAT);
            Date date = formatter.parse(dateInString);
            return date.getTime();
        } catch (ParseException e) {
            throw new KupiCoreException("Date to timestamp conversion failed", e);
        }
    }

    /**
     * This method is used to format date to support Google Analytics.
     *
     * @param timeStamp timeStamp
     * @return date in string
     */
    private String dateFormatter(long timeStamp, String timeZone) {
        var date = new Date(timeStamp);
        var simpleDateFormat = new SimpleDateFormat(ANALYTICS_DATE_FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        return simpleDateFormat.format(date);
    }
}
