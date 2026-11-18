package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Subscription;
import com.kupi.cc.base.enums.CurrencyType;
import com.kupi.cc.base.enums.SubscriptionType;
import lombok.*;

@Getter
@Setter
public class SubscriptionResponseDto extends ResponseDto {
    private static final long serialVersionUID = 7103616022334792365L;

    private String id;
    private String userId;
    private CurrencyType currencyType;
    private String subscriptionPlanId;
    private SubscriptionAmountResponseDto subscriptionFee;
    private SubscriptionType subscriptionType;
    private String title;
    private String subTitle;

    public SubscriptionResponseDto(Subscription subscription) {
        this.id = subscription.getId();
        this.userId = subscription.getUserId();
        this.currencyType = subscription.getCurrencyType();
        this.subscriptionPlanId = subscription.getSubscriptionPlanId();
        this.subscriptionFee = new SubscriptionAmountResponseDto(subscription.getSubscriptionFee());
        this.subscriptionType = subscription.getSubscriptionType();
        this.title = subscription.getTitle();
        this.subTitle = subscription.getSubTitle();
    }
}
