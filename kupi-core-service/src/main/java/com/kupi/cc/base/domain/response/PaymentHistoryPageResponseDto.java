package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Wrapper response DTO for payment history with statistics
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryPageResponseDto extends ResponseDto {

    private List<PaymentHistoryResponseDto> payments;
    private PaymentStatisticsResponseDto statistics;
    private PaginationInfoDto pagination;

    public static PaymentHistoryPageResponseDto from(Page<?> page, PaymentStatisticsResponseDto stats) {
        PaymentHistoryPageResponseDto response = new PaymentHistoryPageResponseDto();
        response.setStatistics(stats);

        PaginationInfoDto paginationInfo = new PaginationInfoDto();
        paginationInfo.setCurrentPage(page.getNumber());
        paginationInfo.setPageSize(page.getSize());
        paginationInfo.setTotalPages(page.getTotalPages());
        paginationInfo.setTotalElements(page.getTotalElements());
        response.setPagination(paginationInfo);

        return response;
    }

    /**
     * Nested class for pagination info
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfoDto {
        private int currentPage;
        private int pageSize;
        private int totalPages;
        private long totalElements;
    }
}
