package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.SubscriptionPlan;
import com.kupi.cc.base.enums.PlanType;
import com.kupi.cc.base.enums.SubscriptionStatus;
import com.kupi.cc.base.enums.UserType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionPlanSummaryResponseDto extends ResponseDto {

    private String id;
    private String title;
    private String imageUrl;
    private SubscriptionStatus status;
    private UserType userType;
    private PlanType durationType;
    private long noOfSubscribers;

    public SubscriptionPlanSummaryResponseDto(SubscriptionPlan subscriptionPlan, long noOfSubscribers) {
        this.id = subscriptionPlan.getId();
        this.title = subscriptionPlan.getTitle();
        this.imageUrl = subscriptionPlan.getImageUrl();
        this.status = subscriptionPlan.getStatus();
        this.userType = subscriptionPlan.getUserRole();
        this.durationType = subscriptionPlan.getPlanType();
        this.noOfSubscribers = noOfSubscribers;
    }
}
