package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Subscribe entity
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "subscribe")
public class Subscribe {

    @Transient
    private static final String SUBSCRIBE_ID_PREFIX = "sid-";

    @Id
    private String id;
    @ManyToOne
    @JoinColumn(name = "subscription_plan_id")
    private SubscriptionPlan subscriptionPlan;
    @Column(nullable = false)
    private String userId;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column(nullable = false)
    private boolean isDeleted = false;
    @Column(nullable = false)
    private long createdAt;
    @Column(nullable = false)
    private long updatedAt;

    public Subscribe(String subscriptionId, SubscriptionPlan subscriptionPlan, String merchantOrBankId, UserType userType) {
        if (subscriptionId == null) {
            subscriptionId = SUBSCRIBE_ID_PREFIX + UUID.randomUUID();
        }
        this.id = subscriptionId;
        this.subscriptionPlan = subscriptionPlan;
        this.userId = merchantOrBankId;
        this.userType = userType;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}
