package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.DealsOfTheDayEventSearchIndex;
import com.kupi.cc.base.enums.UserType;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

/**
 * Deals of the day event response dto
 */
@Getter
@Setter
public class DealsOfTheDayEventResponseDto extends ResponseDto {

    private String id;
    private BasicUserResponseDto businessProfile;
    private UserType userType;
    private DateResponseDto validOn;
    private DateResponseDto createdAt;
    private DateResponseDto updatedAt;
    private String dealTitles;

    public DealsOfTheDayEventResponseDto(DealsOfTheDayEventSearchIndex dealsOfTheDayEventSearchIndex,
                                         String timeZone) {
        this.id = dealsOfTheDayEventSearchIndex.getId();
        this.userType = dealsOfTheDayEventSearchIndex.getUserType();
        this.validOn = new DateResponseDto(dealsOfTheDayEventSearchIndex.getValidOn().getTime(), timeZone,
                dealsOfTheDayEventSearchIndex.getValidOn());
        this.createdAt = new DateResponseDto(dealsOfTheDayEventSearchIndex.getCreatedAt(), timeZone,
                new Date(dealsOfTheDayEventSearchIndex.getCreatedAt()));
        this.updatedAt = new DateResponseDto(dealsOfTheDayEventSearchIndex.getUpdatedAt(), timeZone,
                new Date(dealsOfTheDayEventSearchIndex.getUpdatedAt()));
        this.dealTitles = dealsOfTheDayEventSearchIndex.getDealTitles();
        this.businessProfile = new BasicUserResponseDto(dealsOfTheDayEventSearchIndex.getUserId(),
                dealsOfTheDayEventSearchIndex.getUserName(), dealsOfTheDayEventSearchIndex.getUserImageUrl());
    }
}
