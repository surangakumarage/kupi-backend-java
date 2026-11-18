package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.enums.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.sql.Date;

/**
 * Deals of the day event search index entity.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "deals_of_the_day_event_search_index")
public class DealsOfTheDayEventSearchIndex {
    @Transient
    private static final String SEPARATOR = ", ";
    @Transient
    private static final String EMPTY_TEXT = "";

    @Id
    private String id;
    private String userId;
    private String userName;
    private String userImageUrl;
    @Enumerated(EnumType.STRING)
    private UserType userType;
    private Date validOn;
    private long createdAt;
    private long updatedAt;
    private String dealIds = EMPTY_TEXT;
    private String dealTitles = EMPTY_TEXT;

    public DealsOfTheDayEventSearchIndex(DealsOfTheDayEvent dealsOfTheDayEvent, String userName, String dealTitles,
                                         String imageUrl) {
        this.id = dealsOfTheDayEvent.getId();
        this.userId = dealsOfTheDayEvent.getUserId();
        this.userName = userName;
        this.userType = dealsOfTheDayEvent.getUserType();
        this.validOn = dealsOfTheDayEvent.getValidOn();
        this.createdAt = dealsOfTheDayEvent.getCreatedAt();
        this.updatedAt = dealsOfTheDayEvent.getUpdatedAt();
        dealsOfTheDayEvent.getDeals().forEach(dealId -> this.dealIds += dealId + SEPARATOR);
        this.dealTitles = dealTitles;
        this.userImageUrl = imageUrl;
    }

    /**
     * This method will update deals of the day event.
     *
     * @param dealsOfTheDayEvent dealsOfTheDayEvent
     * @param dealTitles         dealTitles
     */
    public void update(DealsOfTheDayEvent dealsOfTheDayEvent, String dealTitles) {
        this.updatedAt = dealsOfTheDayEvent.getUpdatedAt();
        this.dealIds = EMPTY_TEXT;
        dealsOfTheDayEvent.getDeals().forEach(dealId -> this.dealIds += dealId + SEPARATOR);
        this.dealTitles = dealTitles;
    }

}
