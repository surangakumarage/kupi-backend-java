package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.UserSubscriptionPlan;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MobileUserSubscriptionPlanResponseDto extends ResponseDto {

    private String id;
    private String name;
    private BigDecimal price;
    private int couponLimit;
    private int validDays;


    public MobileUserSubscriptionPlanResponseDto(UserSubscriptionPlan userSubscriptionPlan) {
        this.id = userSubscriptionPlan.getId();
        this.name = userSubscriptionPlan.getName();
        this.price = userSubscriptionPlan.getPrice();
        this.couponLimit = userSubscriptionPlan.getCouponLimit();
        this.validDays = userSubscriptionPlan.getValidDays();
    }
}
