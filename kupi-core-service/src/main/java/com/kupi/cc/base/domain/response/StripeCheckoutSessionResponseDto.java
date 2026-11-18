package com.kupi.cc.base.domain.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stripe.model.checkout.Session;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class StripeCheckoutSessionResponseDto extends ResponseDto {
    private String redirectUrl;
    private String sessionId;
    private boolean freePlan;

    public StripeCheckoutSessionResponseDto(Session session) {
        this.redirectUrl = session.getUrl();
        this.sessionId = session.getId();
    }

    public StripeCheckoutSessionResponseDto(boolean freePlan) {
        this.freePlan = freePlan;
    }
}
