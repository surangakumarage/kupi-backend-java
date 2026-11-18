package com.kupi.cc.base.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

/**
 * Favourite Deal entity
 * Stores deals marked as favourite by end users.
 */
@Getter
@Setter
@Entity
@Table(name = "favourite_deal",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "deal_id"}))
@NoArgsConstructor
@AllArgsConstructor
public class FavouriteDeal {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "deal_id", nullable = false)
    private String dealId;

    @Column(nullable = false)
    private long createdAt;

    @Column(nullable = false)
    private long updatedAt;

    /**
     * Convenience constructor for creating a new favourite deal.
     *
     * @param userId user id
     * @param dealId deal id
     */
    public FavouriteDeal(String userId, String dealId) {
        long now = System.currentTimeMillis();
        this.id = "fdid-" + UUID.randomUUID();
        this.userId = userId;
        this.dealId = dealId;
        this.createdAt = now;
        this.updatedAt = now;
    }
}


