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
 * Today summary response Dto
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TodaySummaryResponseDto extends ResponseDto {

    private String title = "Today's Summary";
    private String subTitle;

    private UserCountsResponseDto users;
    private SubscriptionCountsResponseDto subscriptions;
    private DealCountsResponseDto deals;

    private Long noOfNewBrands;
    private Long noOfNewCategories;
    private Long noOfNewCreditCardRequests;

    public TodaySummaryResponseDto(String timeZone, UserType userType, UserCountsResponseDto users,
                                   SubscriptionCountsResponseDto subscriptions, DealCountsResponseDto deals,
                                   TodaySummaryResponseDto todaySummaryResponseDto) {
        LocalDate today = LocalDate.now(ZoneId.of(timeZone));
        long todayInMillis = today.atStartOfDay(ZoneId.of(timeZone)).toInstant().toEpochMilli();
        switch (userType) {
            case ADMIN:
                this.users = users;
                this.subscriptions = subscriptions;
                break;
            case BANK:
                this.users = users;
                break;
            default:
        }
        this.deals = deals;
        this.subTitle = new DateResponseDto(todayInMillis, timeZone, Date.valueOf(today)).getDisplayDate();
        this.noOfNewBrands = todaySummaryResponseDto.getNoOfNewBrands();
        this.noOfNewCategories = todaySummaryResponseDto.getNoOfNewCategories();
        this.noOfNewCreditCardRequests = todaySummaryResponseDto.getNoOfNewCreditCardRequests();
    }

    /**
     * This method sets attributes for Categories and Brands
     *
     * @param noOfNewCategories noOfNewCategories
     * @param noOfNewBrands noOfNewBrands
     * @param userType userType
     */
    public void setNoOfNewCategoriesAndBrands(long noOfNewCategories, long noOfNewBrands, UserType userType) {
        if (userType == UserType.ADMIN || userType == UserType.BANK) {
            this.noOfNewCategories = noOfNewCategories;
            this.noOfNewBrands = noOfNewBrands;
        }
    }

    /**
     * This method sets attribute for CreditCardRequest
     *
     * @param noOfNewCreditCardRequests noOfNewCreditCardRequests
     * @param userType userType
     */
    public void setNoOfNewCreditCardRequest(long noOfNewCreditCardRequests, UserType userType){
        if (userType == UserType.ADMIN || userType == UserType.BANK) {
            this.noOfNewCreditCardRequests = noOfNewCreditCardRequests;
        }
    }
}
