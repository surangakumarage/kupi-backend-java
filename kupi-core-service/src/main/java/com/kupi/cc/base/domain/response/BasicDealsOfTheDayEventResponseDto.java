package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.DealsOfTheDayEvent;
import com.kupi.cc.base.enums.UserType;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.Set;

/**
 * BasicDealsOfTheDayEventResponseDto
 */
@Getter
@Setter
public class BasicDealsOfTheDayEventResponseDto extends ResponseDto {

    private String id;
    private String userId;
    private UserType userType;
    private Date validOn;
    private Set<String> deals;

    public BasicDealsOfTheDayEventResponseDto(DealsOfTheDayEvent dealsOfTheDayEvent) {
        this.id = dealsOfTheDayEvent.getId();
        this.userId = dealsOfTheDayEvent.getUserId();
        this.userType = dealsOfTheDayEvent.getUserType();
        this.validOn = dealsOfTheDayEvent.getValidOn();
        this.deals = dealsOfTheDayEvent.getDeals();
    }
}
