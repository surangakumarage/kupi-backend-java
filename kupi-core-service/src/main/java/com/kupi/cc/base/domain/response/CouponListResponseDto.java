package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Coupon;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Coupon list response dto
 */
@Getter
public class CouponListResponseDto extends PageResponseDto {

    private final List<BasicCouponResponseDto> coupons;

    public CouponListResponseDto(Page<Coupon> page, Map<String, BasicUserResponseDto> userMap, String timeZone) {
        super(page);
        coupons = page.stream()
                .map(coupon -> new BasicCouponResponseDto(coupon, userMap.get(coupon.getPurchasedBy()), timeZone))
                .collect(Collectors.toList());
    }
}
