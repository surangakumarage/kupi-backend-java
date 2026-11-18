package com.kupi.cc.base.domain.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.UUID;

/**
 * Offer Type entity
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "offer_type")
public class OfferType {

    @Transient
    private static final String REQUEST_A_DEAL_ID_PREFIX = "otid-";

    @Id
    private String id;
    private String name;
    private long createdAt;
    private long updatedAt;
    private boolean isDeleted;

    public OfferType(String name) {
        this.id = REQUEST_A_DEAL_ID_PREFIX + UUID.randomUUID();
        this.name = name;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isDeleted = false;
    }
}
