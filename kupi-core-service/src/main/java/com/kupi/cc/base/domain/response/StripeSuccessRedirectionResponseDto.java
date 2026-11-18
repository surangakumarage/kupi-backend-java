package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StripeSuccessRedirectionResponseDto extends StripeSessionResponseDto {
    private int couponCount;

    public StripeSuccessRedirectionResponseDto(StripeSessionResponseDto stripeSessionResponseDto,
                                               int couponCount) {
        super(stripeSessionResponseDto);
        this.couponCount = couponCount;
    }
}
