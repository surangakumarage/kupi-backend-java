package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Subscribe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSubscriptionPlanResponseDto extends ResponseDto {

    private boolean isSubscribed;
    private String subscriptionId;
    private DateResponseDto subscribedOn;
    private SubscriptionPlanResponseDto subscriptionPlanResponseDto;

    public UserSubscriptionPlanResponseDto(Optional<Subscribe> subscribeOptional, String timeZone) {
        if (subscribeOptional.isPresent()) {
            var subscribe = subscribeOptional.get();
            this.isSubscribed = true;
            this.subscriptionId = subscribe.getId();
            this.subscribedOn = new DateResponseDto(subscribe.getCreatedAt(),
                    timeZone, new Date(subscribe.getCreatedAt()));
            this.subscriptionPlanResponseDto = new SubscriptionPlanResponseDto(subscribe.getSubscriptionPlan());
        } else {
            this.isSubscribed = false;
            this.subscriptionId = null;
            this.subscribedOn = null;
            this.subscriptionPlanResponseDto = null;
        }
    }
}