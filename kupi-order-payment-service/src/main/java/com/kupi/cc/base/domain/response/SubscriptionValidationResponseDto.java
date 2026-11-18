package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.enums.SubscriptionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * matches the response coming from core-service
 */
@Getter
@Setter
@NoArgsConstructor
public class SubscriptionValidationResponseDto extends ResponseDto {
    private static final long serialVersionUID = -8496542309876056060L;

    private String id;
    private String subscriptionPlanId;
    private String title;
    private String subTitle;
    private SubscriptionType planType;
    private boolean isFree;
    private boolean isSubscribed;
    private SubscriptionAmountResponseDto amount;
    private String paymentUrl;

}
