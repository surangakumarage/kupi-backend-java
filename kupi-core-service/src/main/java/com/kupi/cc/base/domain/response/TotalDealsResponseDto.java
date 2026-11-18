package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

/**
 * Total deals response dto
 */
@Getter
@Setter
public class TotalDealsResponseDto {

    //TODO: Remove mock data

    private final Long total = 50l;
    private final Long activeDeals = 50l;
    private final Long bankDeals = 20l;
    private final Long activeBankDeals = 20l;
    private final Long merchantDeals = 30l;
    private final Long activeMerchantDeals = 25l;
    private final Long expiringDeals = 10l;
    private final Long dealRequests = 10l;
}
