package com.kupi.cc.base.domain.response;


import com.kupi.cc.base.domain.entity.Subscribe;
import com.kupi.cc.base.domain.entity.SubscriptionRenewal;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class SubscriptionPlanDetailResponseDto extends UserSubscriptionPlanResponseDto {
    private int remainingNoOfDealsPerMonth;
    private int remainingNoOfDealsOfTheDayEventsPerMonth;
    private int remainingNoOfDealsOfTheDayPerEvent;


    public SubscriptionPlanDetailResponseDto(SubscriptionRenewal subscriptionRenewal,
                                             Optional<Subscribe> subscribeOptional, String timeZone) {
        super(subscribeOptional, timeZone);
        this.remainingNoOfDealsPerMonth = subscriptionRenewal.getRemainingDealsPerMonth();
        this.remainingNoOfDealsOfTheDayEventsPerMonth = subscriptionRenewal.getRemainingDealsOfTheDayEventsPerMonth();
        this.remainingNoOfDealsOfTheDayPerEvent = subscriptionRenewal.getNoOfDealsPerEvent();
    }


}
