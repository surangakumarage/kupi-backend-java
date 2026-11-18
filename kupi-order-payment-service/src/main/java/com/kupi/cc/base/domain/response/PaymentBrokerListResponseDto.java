package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.PaymentBroker;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class PaymentBrokerListResponseDto extends ResponseDto {
    private List<PaymentBrokerResponseDto> paymentBrokerList;

    public PaymentBrokerListResponseDto(List<PaymentBroker> paymentBrokers) {
        this.paymentBrokerList = paymentBrokers.stream()
                .map(PaymentBrokerResponseDto::new).collect(Collectors.toList());
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
