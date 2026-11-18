package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.enums.CouponRejection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Coupon statistics detail entity
 */
@Entity
@Table(name = "coupon_statistics_detail")
@Getter
@Setter
@NoArgsConstructor
public class CouponStatisticDetail {

    @Transient
    private static final String COUPON_STATISTICS_DETAIL_ID_PREFIX = "cousdid-";
    private String couponCode;
    @Enumerated(EnumType.STRING)
    private CouponRejection rejectedType;
    private String fileName;
    @Id
    private String id;
    @ManyToOne
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;


    public CouponStatisticDetail(String couponCode, CouponRejection rejectedType, String fileName, Deal deal) {
        this.id = COUPON_STATISTICS_DETAIL_ID_PREFIX + UUID.randomUUID();
        this.couponCode = couponCode;
        this.rejectedType = rejectedType;
        this.fileName = fileName;
        this.deal = deal;
    }
}
