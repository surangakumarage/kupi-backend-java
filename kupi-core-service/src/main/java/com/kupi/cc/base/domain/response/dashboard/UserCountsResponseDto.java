package com.kupi.cc.base.domain.response.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kupi.cc.base.domain.response.ResponseDto;
import com.kupi.cc.base.enums.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Total user response dto
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserCountsResponseDto extends ResponseDto {
    private Long total;
    private Long merchants;
    private Long activeMerchants;
    private Long banks;
    private Long activeBanks;
    private Long mobileUsers;

    public UserCountsResponseDto(AuthSummaryResponseDto authSummaryResponseDto, UserType userType) {
        if (userType == UserType.ADMIN) {
            this.banks = authSummaryResponseDto.getTotalBanks();
            this.activeBanks = authSummaryResponseDto.getTotalActiveBanks();
        }
        this.activeMerchants = authSummaryResponseDto.getTotalActiveMerchants();
        this.merchants = authSummaryResponseDto.getTotalMerchants();
        this.mobileUsers = authSummaryResponseDto.getTotalMobileUsers();
        this.total = merchants + (banks == null ? 0 : banks) + mobileUsers;
    }

    /**
     * This method is used to get today's user count
     *
     * @param authSummaryResponseDto authSummaryResponseDto
     * @param userType               userType
     */
    public void getTodayUserCountsForAdminOrBank(AuthSummaryResponseDto authSummaryResponseDto, UserType userType) {
        if (userType == UserType.ADMIN) {
            this.banks = authSummaryResponseDto.getNoOfNewBanks();
        }
        this.merchants = authSummaryResponseDto.getNoOfNewMerchants();
        this.mobileUsers = authSummaryResponseDto.getNoOfNewMobileUsers();
        this.total = merchants + (banks == null ? 0 : banks) + mobileUsers;
    }
}