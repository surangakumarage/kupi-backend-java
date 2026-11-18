package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.domain.request.UserSubscriptionRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * UserSubscription entity
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_subscriptions")
public class UserSubscription {

    @Transient
    private static final String USER_SUBSCRIPTION_ID_PREFIX = "usid-";

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private UserSubscriptionPlan userSubscriptionPlan;

    @Column(nullable = false)
    private long subscriptionStart;

    @Column(nullable = false)
    private long subscriptionEnd;

    @Column(nullable = false)
    private int usedCouponCount = 0;

    @Column(nullable = false)
    private long createdAt;
    @Column(nullable = false)
    private long updatedAt;

    public UserSubscription(UserSubscriptionRequestDto userSubscriptionRequestDto,
                            UserSubscriptionPlan userSubscriptionPlan) {
        long now = System.currentTimeMillis();
        this.id = USER_SUBSCRIPTION_ID_PREFIX + UUID.randomUUID();
        this.userId = userSubscriptionRequestDto.getUserId();
        this.userSubscriptionPlan = userSubscriptionPlan;
        this.subscriptionStart = now;
        this.subscriptionEnd = now + (userSubscriptionPlan.getValidDays() * 24 * 60 * 60 * 1000L);
        this.usedCouponCount = 0;
        this.createdAt = now;
        this.updatedAt = now;
    }

}
