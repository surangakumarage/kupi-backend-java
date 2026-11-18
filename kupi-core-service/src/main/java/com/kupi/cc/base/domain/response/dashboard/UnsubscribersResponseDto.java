package com.kupi.cc.base.domain.response.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnsubscribersResponseDto extends ResponseDto {

    private Long total;
    private Long merchantUnsubscribers;
    private Long bankUnsubscribers;

    public UnsubscribersResponseDto(long merchantUnsubscribers, long bankUnsubscribers) {
        this.merchantUnsubscribers = merchantUnsubscribers;
        this.bankUnsubscribers = bankUnsubscribers;
        this.total = merchantUnsubscribers + bankUnsubscribers;
    }
}