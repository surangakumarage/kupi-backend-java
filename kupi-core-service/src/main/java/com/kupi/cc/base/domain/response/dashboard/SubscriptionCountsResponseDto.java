package com.kupi.cc.base.domain.response.dashboard;

import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * Subscriptions dto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCountsResponseDto extends ResponseDto {
    private PlansResponseDto plans;
    private SubscribersResponseDto subscribers;
    private UnsubscribersResponseDto unsubscribers;
}