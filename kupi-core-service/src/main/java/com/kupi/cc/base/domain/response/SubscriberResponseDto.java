package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class SubscriberResponseDto extends BasicMerchantBusinessResponseDto {

    private DateResponseDto subscribedOn;

    public SubscriberResponseDto(BasicMerchantBusinessResponseDto merchantBusinessResponseDto,
                                 long subscribedOn, String timeZone) {
        super(merchantBusinessResponseDto);
        this.subscribedOn = new DateResponseDto(subscribedOn, timeZone, new Date(subscribedOn));
    }
}
