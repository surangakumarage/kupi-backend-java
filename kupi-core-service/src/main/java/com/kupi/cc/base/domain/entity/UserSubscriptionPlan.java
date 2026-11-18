package com.kupi.cc.base.domain.entity;


import com.kupi.cc.base.domain.request.UserSubscriptionPlanRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * UserSubscriptionPlan entity
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_subscription_plan")
public class UserSubscriptionPlan {

    @Transient
    private static final String USER_SUBSCRIPTION_PLAN_ID_PREFIX = "usp-";

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int couponLimit;

    @Column(nullable = false)
    private int validDays;

    @Column(nullable = false)
    private String stripePriceId;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false)
    private long createdAt;

    @Column(nullable = false)
    private long updatedAt;


    public UserSubscriptionPlan(UserSubscriptionPlanRequestDto dto) {
        this.id = USER_SUBSCRIPTION_PLAN_ID_PREFIX + UUID.randomUUID();
        this.name = dto.getName();
        this.price = dto.getPrice();
        this.couponLimit = dto.getCouponLimit();
        this.validDays = dto.getValidDays();
        this.imageUrl = dto.getImageUrl();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}
