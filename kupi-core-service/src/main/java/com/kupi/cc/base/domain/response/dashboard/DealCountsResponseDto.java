package com.kupi.cc.base.domain.response.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kupi.cc.base.domain.response.ResponseDto;
import com.kupi.cc.base.enums.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Total deals response dto
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DealCountsResponseDto extends ResponseDto {

    private Long totalDeal;
    private Long expiringDeals;
    private Long activeDeals;
    private Long merchantDeals;
    private Long activeMerchantDeals;
    private Long bankDeals;
    private Long activeBankDeals;
    private Long dealRequests;

    public DealCountsResponseDto(Long total, Long expiringDeals, Long merchantDeals, Long bankDeals, Long dealRequests,
                                 UserType userType) {
        switch (userType) {
            case ADMIN:
                this.bankDeals = bankDeals;
                this.merchantDeals = merchantDeals;
                break;
            case BANK:
                this.merchantDeals = merchantDeals;
                break;
            default:
        }
        this.totalDeal = total;
        this.expiringDeals = expiringDeals;
        this.dealRequests = dealRequests;
    }

    /**
     * This method is to set deals for Admin
     *
     * @param totalDeal           totalDeal
     * @param activeDeals         activeDeals
     * @param merchantDeals       merchantDeals
     * @param activeMerchantDeals activeMerchantDeals
     * @param bankDeals           bankDeals
     * @param activeBankDeals     activeBankDeals
     * @param dealRequests        dealRequests
     */
    public void setAdminDeals(Long totalDeal, Long activeDeals, Long merchantDeals, Long activeMerchantDeals,
                              Long bankDeals, Long activeBankDeals, Long dealRequests) {
        this.totalDeal = totalDeal;
        this.activeDeals = activeDeals;
        this.dealRequests = dealRequests;
        this.bankDeals = bankDeals;
        this.merchantDeals = merchantDeals;
        this.activeMerchantDeals = activeMerchantDeals;
        this.activeBankDeals = activeBankDeals;
    }

    /**
     * This method is to set deals for Bank/Merchant
     *
     * @param totalDeal           totalDeal
     * @param activeDeals         activeDeals
     * @param merchantDeals       merchantDeals
     * @param activeMerchantDeals activeMerchantDeals
     * @param dealRequests        dealRequests
     * @param userType            userType
     */
    public void setBankOrMerchantDeals(Long totalDeal, Long activeDeals, Long merchantDeals, Long activeMerchantDeals,
                                       Long dealRequests, UserType userType) {
        if (userType == UserType.BANK) {
            this.merchantDeals = merchantDeals;
            this.activeMerchantDeals = activeMerchantDeals;
        }
        this.totalDeal = totalDeal;
        this.activeDeals = activeDeals;
        this.dealRequests = dealRequests;
    }
}