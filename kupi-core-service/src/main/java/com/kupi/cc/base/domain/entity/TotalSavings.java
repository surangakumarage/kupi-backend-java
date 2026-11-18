package com.kupi.cc.base.domain.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Total savings entity to store total savings for all users
 */
@Entity
@Table(name = "total_savings")
@Getter
@Setter
@NoArgsConstructor
public class TotalSavings {
    @Id
    private String id;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalSavings;
    
    @Column(nullable = false)
    private long lastCalculatedAt;
    
    @Column(nullable = false)
    private long createdAt;
    
    @Column(nullable = false)
    private long updatedAt;
    
    private static final String TOTAL_SAVINGS_ID = "totsav-ALL";
    
    public TotalSavings(BigDecimal totalSavings) {
        this.id = TOTAL_SAVINGS_ID;
        this.totalSavings = totalSavings;
        long now = System.currentTimeMillis();
        this.lastCalculatedAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    public void updateTotalSavings(BigDecimal totalSavings) {
        this.totalSavings = totalSavings;
        this.lastCalculatedAt = System.currentTimeMillis();
        this.updatedAt = this.lastCalculatedAt;
    }
}

