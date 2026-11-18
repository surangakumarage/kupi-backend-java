package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.domain.request.CouponCreateRequestDto;
import com.kupi.cc.base.domain.request.CouponUpdateRequestDto;
import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "coupon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends Auditable {
    private static final long serialVersionUID = -8011189670951820569L;

    @Transient
    private static final String ITEM_ID_PREFIX = "cid-";

    @Id
    private String id;

    @Column(nullable = false)
    private String couponId;

    @Column(nullable = false)
    private String dealId;

    @Column(nullable = false)
    private String merchantId;

    @Column(nullable = false)
    private BigDecimal couponPrice;

    @Column(nullable = false)
    private long dealExpiryDate;

    @Column(nullable = false)
    private long couponExpiryDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn
    private OrderDetails orderDetails;

    @Column(nullable = false)
    private boolean isDeleted;

    public Coupon(CouponCreateRequestDto couponRequestDto, OrderDetails orderDetails) {
        this.id = ITEM_ID_PREFIX + UUID.randomUUID();
        this.couponId = couponRequestDto.getCouponId();
        this.dealId = couponRequestDto.getDealId();
        this.merchantId = couponRequestDto.getMerchantId();
        this.couponPrice = couponRequestDto.getCouponPrice();
        this.couponExpiryDate = couponRequestDto.getCouponExpiryDate();
        this.dealExpiryDate = couponRequestDto.getDealExpiryDate();
        this.orderDetails = orderDetails;
        this.isDeleted = false;
    }

    /**
     * create new coupon from coupon update request DTO
     * context      order update request might contain new coupons
     *              these brand-new coupon entities will be persisted to the database with other updated coupon entities
     * @param couponUpdate      coupon update
     * @param order             parent order entity
     */
    public Coupon(CouponUpdateRequestDto couponUpdate, OrderDetails order) {
        this.id = ITEM_ID_PREFIX + UUID.randomUUID();
        this.couponId = couponUpdate.getCouponId();
        this.dealId = couponUpdate.getDealId();
        this.merchantId = couponUpdate.getMerchantId();
        this.couponPrice = couponUpdate.getCouponPrice();
        this.dealExpiryDate = couponUpdate.getDealExpiryDate();
        this.couponExpiryDate = couponUpdate.getCouponExpiryDate();
        this.orderDetails = order;
        this.isDeleted = couponUpdate.isDeleted();
    }

    public void update(CouponUpdateRequestDto couponUpdateRequestDto) {
        this.isDeleted = couponUpdateRequestDto.isDeleted();
    }

    public void setNewId() {
        this.id = ITEM_ID_PREFIX + UUID.randomUUID();
    }
}
