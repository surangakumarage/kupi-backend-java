package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Basic coupon response Dto
 */
@Getter
@Setter
@NoArgsConstructor
public class BasicCouponResponseDto extends ResponseDto {

    private static final long serialVersionUID = -3116072573560528463L;
    private String id;
    private String dealId;
    private boolean couponActive;
    private String couponCode;
    private boolean redeemed;
    private DateResponseDto redeemedOn;
    private DateResponseDto expiredOn;
    private boolean expired;
    private BigDecimal amount = BigDecimal.valueOf(0.01);//todo:remove
    private String paymentUrl;

}
