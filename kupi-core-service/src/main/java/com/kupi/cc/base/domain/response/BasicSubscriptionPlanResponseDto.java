package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.SubscriptionPlan;
import com.kupi.cc.base.enums.CurrencyType;
import com.kupi.cc.base.enums.SubscriptionStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BasicSubscriptionPlanResponseDto extends ResponseDto {

    private String id;
    private String title;
    private String subTitle;
    private boolean isFree;
    private String amount;
    private String imageUrl;
    private SubscriptionStatus status;

    public BasicSubscriptionPlanResponseDto(SubscriptionPlan subscriptionPlan) {
        this.id = subscriptionPlan.getId();
        this.title = subscriptionPlan.getTitle();
        this.subTitle = subscriptionPlan.getSubTitle();
        this.isFree = subscriptionPlan.isFree();
        this.status = subscriptionPlan.getStatus();
        this.imageUrl = subscriptionPlan.getImageUrl();
        this.amount = CurrencyType.USD.getSign() + " " +
                CurrencyType.USD.getSubscriptionAmount(subscriptionPlan.getAmount());
    }
}
