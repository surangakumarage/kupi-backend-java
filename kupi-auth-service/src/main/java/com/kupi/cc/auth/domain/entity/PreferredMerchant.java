package com.kupi.cc.auth.domain.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "preferred_merchant")
@Data
@NoArgsConstructor

public class PreferredMerchant {

    @Transient
    private static final String PREFERRED_MERCHANT_ID_PREFIX = "pmid-";

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String merchantId;

    @Column(name = "createdAt")
    private Date createdAt;


    /**
     * Create new PreferredMerchant instance
     */
    public PreferredMerchant(String userId, String merchantId) {
        this.id = PREFERRED_MERCHANT_ID_PREFIX + UUID.randomUUID();
        this.userId = userId;
        this.merchantId = merchantId;
        this.createdAt = new Date();
    }
}
