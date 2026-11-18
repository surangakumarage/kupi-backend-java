package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

/**
 * Base class for paging dto
 */
@Setter
@Getter
@AllArgsConstructor
public class PageResponseDto extends ResponseDto {

    private static final int ZERO = 0;
    private static final int ONE = 1;
    private final long totalItems;
    private final int totalPages;
    private final int page;
    private final int size;

    public PageResponseDto(Page<?> page) {
        this.totalItems = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.page = page.getNumber();
        this.size = page.getSize();
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}