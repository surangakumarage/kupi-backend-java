package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.SubscribeSearchIndex;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SubscribersListResponseDto extends PageResponseDto {

    private List<SubscriberResponseDto> users;

    public SubscribersListResponseDto(Page<SubscribeSearchIndex> page,
                                      Map<String, Long> subscribedOnMap,
                                      BulkUserResponseDto bulkUserResponseDto, String timeZone) {
        super(page);
        this.users = createSubscriberListResponseDto(subscribedOnMap, bulkUserResponseDto, timeZone);
    }

    /**
     * This method used to create a subscribersResponse list.
     *
     * @param subscribedOnMap     subscribedOnMap
     * @param bulkUserResponseDto bulkUserResponseDto
     * @param timeZone            timeZone
     * @return subscribersResponse List
     */
    private List<SubscriberResponseDto> createSubscriberListResponseDto(Map<String,
            Long> subscribedOnMap, BulkUserResponseDto bulkUserResponseDto, String timeZone) {
        List<SubscriberResponseDto> subscribers = new ArrayList<>();
        bulkUserResponseDto.getUsers().forEach(merchantBusinessResponseDto -> {

            var subscribersResponseDto =
                    new SubscriberResponseDto(merchantBusinessResponseDto,
                            subscribedOnMap.get(merchantBusinessResponseDto.getId()), timeZone);
            subscribers.add(subscribersResponseDto);
        });
        return subscribers;
    }
}