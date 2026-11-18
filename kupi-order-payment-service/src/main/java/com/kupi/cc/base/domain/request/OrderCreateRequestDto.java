package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.CurrencyType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateRequestDto extends RequestDto {
    private static final long serialVersionUID = -6153758510175127329L;
    private CurrencyType currencyType;
    private List<CouponCreateRequestDto> coupons;

    @Override
    public String toLogJson() {
        return toJson();
    }

    @Override
    public boolean isRequiredAvailable() {
        boolean isAllItemsAvailable = coupons != null && !coupons.isEmpty()
                && coupons.stream().map(CouponCreateRequestDto::isRequiredAvailable).reduce(true, Boolean::logicalAnd);
        return currencyType != null && isAllItemsAvailable;
    }
}
