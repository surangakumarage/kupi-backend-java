package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.enums.CouponPurchaseStatus;
import com.kupi.cc.base.enums.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Coupon entity
 */
@Entity
@Table(name = "coupon", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"couponCode"})
})
@Getter
@Setter
@NoArgsConstructor

public class Coupon {
    @Transient
    private static final String COUPON_ID_PREFIX = "couid-";

    @Id
    private String id;
    private String fileName;
    private String couponCode;
    private boolean isFree;
    private Double price;
    private long expiryDate;
    private String purchasedBy;
    private Long purchasedOn;
    @Enumerated(EnumType.STRING)
    private CouponPurchaseStatus couponPurchaseStatus;
    private boolean isRedeemed;
    private Long redeemedOn;
    private long createdAt;
    private long updatedAt;
    @Enumerated(EnumType.STRING)
    private UserType userType;
    @ManyToOne
    @JoinColumn
    private Deal deal;
    @ManyToOne
    @JoinColumn
    private BankDeal bankDeal;
    private boolean couponActive;

    public Coupon(Deal deal, UserType userType, String couponCode, String fileName, long expiryDate) {
        this.id = COUPON_ID_PREFIX + UUID.randomUUID();
        this.fileName = fileName;
        this.couponCode = couponCode;
        this.expiryDate = expiryDate;
        this.isRedeemed = false;
        this.isFree = deal.isFreeCoupons();
        this.price = deal.getCouponPrice();
        this.couponPurchaseStatus = CouponPurchaseStatus.NOT_PURCHASED;
        this.purchasedBy = null;
        this.purchasedOn = null;
        this.redeemedOn = null;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.userType = userType;
        this.deal = deal;
        this.bankDeal = null;
        this.couponActive = true;
    }

    /**
     * This method assigns the user for a coupon in redeem process.
     *
     * @param userId mobile userId
     */
    public void assignNewCouponForUser(String userId) {
        this.purchasedBy = userId;
        this.couponPurchaseStatus = CouponPurchaseStatus.NOT_CONFIRMED;
        this.purchasedOn = System.currentTimeMillis();
        this.updatedAt = purchasedOn;
    }

    /**
     * This method updates coupon purchase status.
     *
     * @param couponPurchaseStatus couponPurchaseStatus
     */
    public void updateCouponPurchaseStatus(CouponPurchaseStatus couponPurchaseStatus) {
        this.couponPurchaseStatus = couponPurchaseStatus;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * This method is used to redeem coupon by merchant.
     */
    public void redeemCoupon() {
        this.isRedeemed = true;
        this.redeemedOn = System.currentTimeMillis();
        this.updatedAt = this.redeemedOn;
    }
}
