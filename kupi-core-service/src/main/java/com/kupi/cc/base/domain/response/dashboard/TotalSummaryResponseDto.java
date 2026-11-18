package com.kupi.cc.base.domain.response.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kupi.cc.base.domain.response.DateResponseDto;
import com.kupi.cc.base.domain.response.ResponseDto;
import com.kupi.cc.base.enums.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Summary response Dto
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TotalSummaryResponseDto extends ResponseDto {

    private static final String APP_SUMMARY = "App Summary";
    private static final String MY_SUMMARY = "My Summary";
    private static final String JOIN = " - ";

    private String title = APP_SUMMARY;
    private String subTitle;

    private UserCountsResponseDto users;
    private SubscriptionCountsResponseDto subscriptions;
    private DealCountsResponseDto deals;

    private Long totalCategories;
    private Long totalActiveCategories;
    private Long totalBrands;
    private Long totalNoOfCreditCardRequest;

    public TotalSummaryResponseDto(String timeZone, UserType userType, UserCountsResponseDto users,
                                   SubscriptionCountsResponseDto subscriptions, DealCountsResponseDto deals,
                                   TotalSummaryResponseDto totalSummaryResponseDto) {
        LocalDate today = LocalDate.now(ZoneId.of(timeZone));
        long todayInMillis = today.atStartOfDay(ZoneId.of(timeZone)).toInstant().toEpochMilli();
        switch (userType) {
            case ADMIN:
                this.totalNoOfCreditCardRequest = totalSummaryResponseDto.getTotalNoOfCreditCardRequest();
                this.subscriptions = subscriptions;
                this.users = users;
                break;
            case BANK:
                this.users = users;
                this.totalNoOfCreditCardRequest = totalSummaryResponseDto.getTotalNoOfCreditCardRequest();
                break;
            default:
        }
        this.deals = deals;
        this.subTitle = new DateResponseDto(todayInMillis, timeZone, Date.valueOf(today)).getDisplayDate();
        this.totalBrands = totalSummaryResponseDto.getTotalBrands();
        this.totalActiveCategories = totalSummaryResponseDto.getTotalActiveCategories();
        this.totalCategories = totalSummaryResponseDto.getTotalCategories();
    }

    /**
     * This method sets attributes for Categories, ActiveCategories and Brands
     *
     * @param totalCategories       totalCategories
     * @param totalBrands           totalBrands
     * @param totalActiveCategories totalActiveCategories
     */
    public void setNoOfTotalNewCategoriesAndBrands(long totalCategories, long totalBrands, long totalActiveCategories) {
        this.totalCategories = totalCategories;
        this.totalActiveCategories = totalActiveCategories;
        this.totalBrands = totalBrands;
    }

    /**
     * This method sets attribute for CreditCardRequest
     *
     * @param totalNoOfCreditCardRequest totalNoOfCreditCardRequest
     * @param userType                   userType
     */
    public void setNoOfNewCreditCardRequest(long totalNoOfCreditCardRequest, UserType userType) {
        if (userType == UserType.ADMIN || userType == UserType.BANK) {
            this.totalNoOfCreditCardRequest = totalNoOfCreditCardRequest;
        }
    }

    /**
     * This method sets attributes for Merchant response.
     *
     * @param joinedDate joinedDate
     * @param timeZone   timeZone
     */
    public void setTitleAndSubTitleResponse(String joinedDate, String timeZone) {
        this.title = MY_SUMMARY;
        LocalDate today = LocalDate.now(ZoneId.of(timeZone));
        long todayInMillis = today.atStartOfDay(ZoneId.of(timeZone)).toInstant().toEpochMilli();
        String todayDate = new DateResponseDto(todayInMillis, timeZone, Date.valueOf(today)).getDisplayDate();
        this.subTitle = joinedDate + JOIN + todayDate;
    }
}