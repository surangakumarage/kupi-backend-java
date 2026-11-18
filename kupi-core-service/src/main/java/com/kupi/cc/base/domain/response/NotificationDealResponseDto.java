package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.enums.UserType;
import lombok.Getter;

/**
 * Notification Deal ResponseDto
 */
@Getter
public class NotificationDealResponseDto {

    private final String id;
    private final String title;
    private final String subTitle;
    private final String coverImage;
    private final UserType dealSource;

    public NotificationDealResponseDto(DealSearchIndex dealSearchIndex) {
        this.id = dealSearchIndex.getId();
        this.title = dealSearchIndex.getTitle();
        this.subTitle = dealSearchIndex.getSubTitle();
        this.coverImage = dealSearchIndex.getCoverImage();
        this.dealSource = dealSearchIndex.getDealSource();
    }
}
