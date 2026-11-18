package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.SubscriptionPlan;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class SubscriptionPlanListResponseDto extends PageResponseDto {

    private List<BasicSubscriptionPlanResponseDto> plans;

    public SubscriptionPlanListResponseDto(Page<SubscriptionPlan> subscriptionPlansPage) {
        super(subscriptionPlansPage);
        plans = subscriptionPlansPage
                .getContent()
                .stream()
                .map(BasicSubscriptionPlanResponseDto::new)
                .collect(Collectors
                        .toList());
    }
}
