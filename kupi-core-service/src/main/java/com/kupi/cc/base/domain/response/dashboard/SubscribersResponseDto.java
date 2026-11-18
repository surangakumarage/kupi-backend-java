package com.kupi.cc.base.domain.response.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Subscribers Response Dto
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscribersResponseDto extends ResponseDto {

    private Long totalSubscribers;
    private Long merchantSubscribers;
    private Long bankSubscribers;

    public SubscribersResponseDto(long merchantSubscribers, long bankSubscribers) {
        this.merchantSubscribers = merchantSubscribers;
        this.bankSubscribers = bankSubscribers;
        this.totalSubscribers = merchantSubscribers + bankSubscribers;
    }
}
