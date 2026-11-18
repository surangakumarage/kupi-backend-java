package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.enums.ActiveStatusType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DealReportResponseDto extends ResponseDto {

    private String id;
    private String title;
    private String coverImage;
    private ActiveStatusType activeStatus;

    public DealReportResponseDto(DealSearchIndex dealSearchIndex) {
        this.id = dealSearchIndex.getId();
        this.title = dealSearchIndex.getTitle();
        this.coverImage = dealSearchIndex.getCoverImage();
        this.activeStatus = dealSearchIndex.getExpiredOn() > System.currentTimeMillis() ?
                ActiveStatusType.ACTIVE : ActiveStatusType.INACTIVE;
    }
}
