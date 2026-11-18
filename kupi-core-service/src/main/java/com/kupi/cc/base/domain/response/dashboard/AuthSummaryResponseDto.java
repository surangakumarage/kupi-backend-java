package com.kupi.cc.base.domain.response.dashboard;

import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Today auth summary response Dto
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthSummaryResponseDto extends ResponseDto {

    private long noOfNewMerchants;
    private long totalMerchants;
    private long totalActiveMerchants;
    private long noOfNewMobileUsers;
    private long totalMobileUsers;
    private long noOfNewBanks;
    private long totalBanks;
    private long totalActiveBanks;
}
