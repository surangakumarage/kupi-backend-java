package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

/**
 * SubscriptionPlanList ResponseDto
 */

@Getter
@Setter
@NoArgsConstructor
public class SubscriptionPlanListResponseDto extends ResponseDto{

    private List<BasicSubscriptionPlanResponseDto> plans;
}