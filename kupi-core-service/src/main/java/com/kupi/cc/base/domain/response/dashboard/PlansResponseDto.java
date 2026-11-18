package com.kupi.cc.base.domain.response.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Plan response dto
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlansResponseDto extends ResponseDto {

    private Long totalPlan;
    private Long totalActivePlan;
    private Long merchantPlans;
    private Long merchantActivePlans;
    private Long bankPlans;
    private Long bankActivePlans;

    public PlansResponseDto(Long merchantPlans, Long merchantActivePlans, Long bankPlans, Long bankActivePlans) {
        this.merchantPlans = merchantPlans;
        this.merchantActivePlans = merchantActivePlans;
        this.bankPlans = bankPlans;
        this.bankActivePlans = bankActivePlans;
        this.totalActivePlan = merchantActivePlans + bankActivePlans;
        this.totalPlan = merchantPlans + bankPlans;
    }

    public PlansResponseDto(Long merchantPlans, Long bankPlans) {
        this.merchantPlans = merchantPlans;
        this.bankPlans = bankPlans;
        this.totalPlan = merchantPlans + bankPlans;
    }
}
