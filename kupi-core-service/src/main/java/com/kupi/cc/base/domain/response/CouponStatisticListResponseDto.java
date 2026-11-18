package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.CouponStatistics;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Coupon statistic list response Dto
 */
@Getter
public class CouponStatisticListResponseDto extends PageResponseDto {

    private final List<BasicCouponStatisticResponseDto> couponStatistics;

    public CouponStatisticListResponseDto(Page<CouponStatistics> page) {
        super(page);
        this.couponStatistics = page.stream().map(BasicCouponStatisticResponseDto::new).collect(Collectors.toList());
    }
}
