package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSubscriptionStatusResponse extends ResponseDto {
    private boolean isExpired;
    private int usedCoupons;
    private int remainingCoupons;
    private long remainingDays;
    private DateResponseDto subscriptionEndDate;
    private String planName;
    private String planId;
}
