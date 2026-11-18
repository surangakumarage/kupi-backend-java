package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.UserSubscriptionPlan;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserSubscriptionPlanListResponse extends ResponseDto {

    private List<UserSubscriptionPlan> userSubscriptionPlanList;


}
