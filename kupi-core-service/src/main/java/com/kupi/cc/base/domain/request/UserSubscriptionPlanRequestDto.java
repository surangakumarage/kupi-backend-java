package com.kupi.cc.base.domain.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UserSubscriptionPlanRequestDto extends RequestDto {

    private String name;
    private BigDecimal price;
    private int couponLimit;
    private int validDays;
    private String imageUrl;

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(name)
                && price != null
                && couponLimit > 0
                && validDays > 0
                && isNonEmpty(imageUrl);
    }

    public String toLogJson() {
        return toJson();
    }

}
