package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.OrderDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class OrderListResponseDto extends ResponseDto {
    private static final long serialVersionUID = 910249416690564422L;

    private List<OrderResponseDto> orderList;

    public OrderListResponseDto(List<OrderDetails> orderDetailsList, String timeZone) {
        this.orderList = orderDetailsList.stream()
                .map(order -> new OrderResponseDto(order, timeZone)).collect(Collectors.toList());
    }

}
