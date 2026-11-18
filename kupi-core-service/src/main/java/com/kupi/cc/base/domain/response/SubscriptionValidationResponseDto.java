package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.SubscriptionPlan;
import com.kupi.cc.base.enums.PlanType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionValidationResponseDto extends ResponseDto {
    private String subscriptionPlanId;
    private String title;
    private String subTitle;
    private PlanType planType;
    private boolean isFree;
    private boolean isSubscribed;
    private SubscriptionAmountResponseDto amount;

    public SubscriptionValidationResponseDto(SubscriptionPlan subscriptionPlan) {
        this.subscriptionPlanId = subscriptionPlan.getId();
        this.title = subscriptionPlan.getTitle();
        this.subTitle = subscriptionPlan.getSubTitle();
        this.planType = subscriptionPlan.getPlanType();
        this.isFree = subscriptionPlan.isFree();
        this.isSubscribed = false;
        this.amount = new SubscriptionAmountResponseDto(subscriptionPlan.getAmount());
    }
}
