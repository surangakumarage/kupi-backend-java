package com.kupi.cc.base.configuration;

import com.kupi.cc.base.domain.entity.Coupon;
import com.kupi.cc.base.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Custom item writer to handle List<Coupon> from the batch processor
 * Updated for Spring Batch 5.x which uses Chunk instead of List
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CouponListWriter implements ItemWriter<List<Coupon>> {

    private final CouponRepository couponRepository;

    @Override
    public void write(Chunk<? extends List<Coupon>> items) {
        int totalCoupons = 0;

        for (List<Coupon> couponList : items) {
            if (couponList != null && !couponList.isEmpty()) {
                couponRepository.saveAll(couponList);
                totalCoupons += couponList.size();
                log.debug("Saved {} coupons to database", couponList.size());
            }
        }

        log.info("Batch write completed. Total coupons saved: {}", totalCoupons);
    }
}
