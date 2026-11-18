package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * This class should replace from a search engine - real-time update
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class SubscribeSearchIndex {
    @Id
    private String id;
    @Column(nullable = false)
    private String subscriptionPlanId;
    @Column(nullable = false)
    private String userId;
    @Column(nullable = false)
    private String businessName;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType userType;
    @Column(nullable = false)
    private boolean isDeleted = false;
    @Column(nullable = false)
    private long createdAt;
    @Column(nullable = false)
    private long updatedAt;

    public SubscribeSearchIndex(Subscribe subscribe, String businessName) {
        this.id = subscribe.getId();
        this.subscriptionPlanId = subscribe.getSubscriptionPlan().getId();
        this.userId = subscribe.getUserId();
        this.businessName = businessName;
        this.userType = subscribe.getUserType();
        this.isDeleted = subscribe.isDeleted();
        this.createdAt = subscribe.getCreatedAt();
        this.updatedAt = subscribe.getUpdatedAt();
    }
}
