package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BasicSubscriptionPlan ResponseDto
 */

@Getter
@Setter
@NoArgsConstructor
public class BasicSubscriptionPlanResponseDto extends ResponseDto{

    private String id;
    private String title;
    private String imageUrl;
    private long count;
}