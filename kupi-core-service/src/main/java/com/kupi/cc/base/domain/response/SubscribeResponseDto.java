package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Subscribe;
import com.kupi.cc.base.enums.UserType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscribeResponseDto extends ResponseDto {

    private String id;
    private String subscriptionPlanId;
    private String userId;
    private UserType userType;

    public SubscribeResponseDto(Subscribe subscribe) {
        this.id = subscribe.getId();
        this.subscriptionPlanId = subscribe.getSubscriptionPlan().getId();
        this.userId = subscribe.getUserId();
        this.userType = subscribe.getUserType();
    }
}
