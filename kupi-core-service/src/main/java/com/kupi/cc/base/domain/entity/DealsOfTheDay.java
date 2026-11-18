package com.kupi.cc.base.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.sql.Date;
import java.util.UUID;

/**
 * Deals of the day entity
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "deals_of_the_day")
public class DealsOfTheDay {
    private static final String DEALS_OF_THE_DAY_ID_PREFIX = "dodid-";
    @Id
    private String id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "dealId")
    private Deal deal;

    @Column(nullable = false)
    private Date applyOn;
    private boolean isActive;
    private boolean isDealOfTheDay;

    public DealsOfTheDay(Deal deal) {
        this.id = DEALS_OF_THE_DAY_ID_PREFIX + UUID.randomUUID();
        this.deal = deal;
        this.applyOn = new Date(System.currentTimeMillis());
        this.isActive = true;
        this.isDealOfTheDay = true;
    }

    public DealsOfTheDay(Deal deal, Date applyOn) {
        this.id = DEALS_OF_THE_DAY_ID_PREFIX + UUID.randomUUID();
        this.deal = deal;
        this.applyOn = applyOn;
        this.isActive = true;
        this.isDealOfTheDay = true;
    }
}
