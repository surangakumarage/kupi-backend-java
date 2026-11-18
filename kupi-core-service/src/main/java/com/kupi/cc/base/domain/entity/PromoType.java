package com.kupi.cc.base.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * PromoType entity - Subcategories for categories
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "promo_type")
public class PromoType {
    @Transient
    private static final String PROMO_TYPE_ID_PREFIX = "ptid-";

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private long createdAt;
    private long updatedAt;
    private boolean isActive;

    public PromoType(String name, Category category) {
        this.id = PROMO_TYPE_ID_PREFIX + UUID.randomUUID();
        this.name = name;
        this.category = category;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isActive = true;
    }

    public void update(String name) {
        this.name = name;
        this.updatedAt = System.currentTimeMillis();
    }
}
