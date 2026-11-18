package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

/**
 * Total user response dto
 */
@Getter
@Setter
public class TotalUsersResponseDto {

    //TODO: Remove mock data

    private final Long total = 60l;
    private final Long merchants = 10l;
    private final Long activeMerchants = 8l;
    private final Long banks = 20l;
    private final Long activeBanks = 20l;
    private final Long mobileUsers = 30l;
}
