package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.CurrencyType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderUpdateRequestDto extends RequestDto {

    private static final long serialVersionUID = -1907588186768238834L;
    private String id;
    private CurrencyType currencyType;
    private List<CouponUpdateRequestDto> coupons;

    @Override
    public boolean isRequiredAvailable() {
        boolean isAllItemsAvailable = coupons != null && coupons.stream()
                .map(CouponUpdateRequestDto::isRequiredAvailable)
                .reduce(true, Boolean::logicalAnd);
        return isNonEmpty(id) && currencyType != null && isAllItemsAvailable;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}