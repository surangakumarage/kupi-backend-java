package com.kupi.cc.base.domain.response.dashboardV2;

import com.kupi.cc.base.domain.response.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Deal Performance List Response DTO
 * Contains paginated list of deal performance metrics
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DealPerformanceListResponseDto extends ResponseDto {

    private List<DealPerformanceResponseDto> deals;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;

    public DealPerformanceListResponseDto(Page<DealPerformanceResponseDto> dealPerformancePage) {
        this.deals = dealPerformancePage.getContent();
        this.totalElements = dealPerformancePage.getTotalElements();
        this.totalPages = dealPerformancePage.getTotalPages();
        this.currentPage = dealPerformancePage.getNumber();
        this.pageSize = dealPerformancePage.getSize();
    }
}

