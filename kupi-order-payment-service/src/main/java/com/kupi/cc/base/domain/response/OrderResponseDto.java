package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.OrderDetails;
import com.kupi.cc.base.enums.CurrencyType;
import com.kupi.cc.base.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
public class OrderResponseDto extends ResponseDto {
    private static final long serialVersionUID = -8838917903836012233L;

    private String id;
    private String userId;
    private DateResponseDto createdDate;
    private CurrencyType currencyType;
    private OrderStatus status;
    private List<CouponResponseDto> coupons;
    private BigDecimal grossTotal;
    private BigDecimal netTotal;
    private String stateSequence;

    public OrderResponseDto(OrderDetails orderDetails, String timeZone) {
        this.id = orderDetails.getId();
        this.userId = orderDetails.getUserId();
        this.createdDate = new DateResponseDto(orderDetails.getCreatedDate(), timeZone);
        this.currencyType = orderDetails.getCurrencyType();
        this.status = orderDetails.getStatus();
        this.coupons = orderDetails.getCoupons().stream()
                .filter(Objects::nonNull)
                .filter(coupon -> !coupon.isDeleted())
                .map(item -> new CouponResponseDto(item, timeZone))
                .collect(Collectors.toList());
        this.grossTotal = orderDetails.getGrossTotal();
        this.netTotal = orderDetails.getNetTotal();
        this.stateSequence = orderDetails.getStateSequence();
    }
}
