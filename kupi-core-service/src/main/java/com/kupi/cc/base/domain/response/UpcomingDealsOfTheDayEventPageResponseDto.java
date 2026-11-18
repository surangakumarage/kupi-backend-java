package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.DealsOfTheDayEventSearchIndex;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Upcoming deals of the day event page response dto
 */
@Getter
public class UpcomingDealsOfTheDayEventPageResponseDto extends PageResponseDto {

    List<DealsOfTheDayEventResponseDto> events;

    public UpcomingDealsOfTheDayEventPageResponseDto(Page<DealsOfTheDayEventSearchIndex> page, String timeZone) {
        super(page);
        this.events = convertToListResponseDto(page, timeZone);
    }

    /**
     * This method will convert page to response list.
     *
     * @param page     page
     * @param timeZone timeZone
     * @return events list.
     */
    private List<DealsOfTheDayEventResponseDto> convertToListResponseDto(
            Page<DealsOfTheDayEventSearchIndex> page, String timeZone) {
        List<DealsOfTheDayEventResponseDto> responseList = new ArrayList<>();
        page.getContent().forEach(event -> responseList.add(
                new DealsOfTheDayEventResponseDto(event, timeZone)));
        return responseList;
    }
}
