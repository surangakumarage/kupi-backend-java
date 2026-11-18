package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.SubscriptionPlan;
import com.kupi.cc.base.enums.PlanType;
import com.kupi.cc.base.enums.UserType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionPlanResponseDto extends ResponseDto {

    private String id;
    private String title;
    private String subTitle;
    private String description;
    private String imageUrl;
    private String termsAndCondition;
    private UserType userRole;
    private PlanType planType;
    private int noOfDealsPerMonth;
    private int noOfDealsOfTheDayEventsPerMonth;
    private int noOfDealsOfTheDayPerEvent;
    private boolean isFree;
    private SubscriptionAmountResponseDto amount;

    public SubscriptionPlanResponseDto(SubscriptionPlan subscriptionPlan) {
        this.id = subscriptionPlan.getId();
        this.title = subscriptionPlan.getTitle();
        this.subTitle = subscriptionPlan.getSubTitle();
        this.description = subscriptionPlan.getDescription();
        this.imageUrl = subscriptionPlan.getImageUrl();
        this.termsAndCondition = subscriptionPlan.getTermsAndCondition();
        this.userRole = subscriptionPlan.getUserRole();
        this.planType = subscriptionPlan.getPlanType();
        this.noOfDealsPerMonth = subscriptionPlan.getNoOfDealsPerMonth();
        this.noOfDealsOfTheDayEventsPerMonth = subscriptionPlan.getNoOfDealsOfTheDayEventsPerMonth();
        this.noOfDealsOfTheDayPerEvent = subscriptionPlan.getNoOfDealsOfTheDayPerEvent();
        this.isFree = subscriptionPlan.isFree();
        this.amount = new SubscriptionAmountResponseDto(subscriptionPlan.getAmount());
    }
}
