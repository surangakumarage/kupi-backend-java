package com.kupi.cc.base.domain.response.dashboardV2;

import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Activity Log Response DTO
 * Contains paginated list of activity log entries
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponseDto extends ResponseDto {

    private List<ActivityLogEntryResponseDto> activities;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;

    public ActivityLogResponseDto(Page<ActivityLogEntryResponseDto> activityPage) {
        this.activities = activityPage.getContent();
        this.totalElements = activityPage.getTotalElements();
        this.totalPages = activityPage.getTotalPages();
        this.currentPage = activityPage.getNumber();
        this.pageSize = activityPage.getSize();
    }
}

