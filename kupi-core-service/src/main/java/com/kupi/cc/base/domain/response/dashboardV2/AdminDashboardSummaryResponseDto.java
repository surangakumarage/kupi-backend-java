package com.kupi.cc.base.domain.response.dashboardV2;

import com.kupi.cc.base.domain.response.ResponseDto;
import com.kupi.cc.base.domain.response.dashboard.AuthSummaryResponseDto;
import com.kupi.cc.base.domain.response.dashboard.DealCountsResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AdminDashboardSummaryResponseDto extends ResponseDto {
    private long totalMerchants;
    private long totalActiveMobileUsers;
    private long totalLiveDeals;
    private long totalRedeemCoupons;

    public AdminDashboardSummaryResponseDto(AuthSummaryResponseDto adminSummary,
                                            DealCountsResponseDto dealCountsResponseDto, long totalActiveCoupons) {
        this.totalMerchants = adminSummary.getTotalMerchants();
        this.totalActiveMobileUsers = adminSummary.getTotalMobileUsers();
        this.totalLiveDeals = dealCountsResponseDto.getActiveDeals();
        this.totalRedeemCoupons = totalActiveCoupons;
    }
}
