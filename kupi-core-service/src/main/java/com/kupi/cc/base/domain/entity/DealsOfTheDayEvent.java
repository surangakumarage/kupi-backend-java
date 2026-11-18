package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.domain.request.DealsOfTheDayEventRequestDto;
import com.kupi.cc.base.enums.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.sql.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Deals of the day event entity
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "deals_of_the_day_event")
public class DealsOfTheDayEvent {

    @Transient
    private static final String DEALS_OF_THE_DAY_ID_PREFIX = "dodeid-";

    @Id
    private String id;
    private String userId;
    @Enumerated(EnumType.STRING)
    private UserType userType;
    private Date validOn;
    private long createdAt;
    private long updatedAt;
    @ElementCollection
    private Set<String> deals = new HashSet<>();

    public DealsOfTheDayEvent(DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto, String userId,
                              UserType userType) {
        this.id = DEALS_OF_THE_DAY_ID_PREFIX + UUID.randomUUID();
        this.userId = userId;
        this.userType = userType;
        long timeStamp = dealsOfTheDayEventRequestDto.getValidOn();
        this.validOn = new Date(timeStamp);
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.deals = dealsOfTheDayEventRequestDto.getDealIds();
    }
}
