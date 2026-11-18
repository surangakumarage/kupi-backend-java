package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Deal view tracking entity
 * Tracks when users view deal detail pages
 */
@Entity
@Table(name = "deal_view")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DealView {
    @Transient
    private static final String DEAL_VIEW_ID_PREFIX = "dvid-";

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String dealId;

    @Column(nullable = false)
    private String merchantId;

    @Column(nullable = false)
    private long viewedAt;

    private String storeId;

    @Column(nullable = false)
    private long createdAt;

    @Column(nullable = false)
    private long updatedAt;

    /**
     * Constructor for creating a new deal view
     * This is the primary constructor - extracts only needed fields for loose coupling
     *
     * @param userId    user id viewing the deal
     * @param dealId    deal id being viewed
     * @param merchantId merchant id (from deal)
     */
    public DealView(String userId, String dealId, String merchantId) {
        long now = System.currentTimeMillis();
        this.id = DEAL_VIEW_ID_PREFIX + UUID.randomUUID();
        this.userId = userId;
        this.dealId = dealId;
        this.merchantId = merchantId;
        this.viewedAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Convenience constructor that takes Deal object
     * Extracts only the needed fields to maintain loose coupling
     *
     * @param userId user id viewing the deal
     * @param deal  deal entity being viewed

     */
    public DealView(String userId, Deal deal) {
        this(userId, deal.getId(), deal.getMerchantId());
    }
}

