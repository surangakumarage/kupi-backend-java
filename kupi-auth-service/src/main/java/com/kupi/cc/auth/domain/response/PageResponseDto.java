package com.kupi.cc.auth.domain.response;

import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/**
 * PageResponseDto List
 */
@Getter
public class PageResponseDto implements ResponseDto {

    private static final int ZERO = 0;
    private static final int ONE = 1;
    private final long totalItems;
    private final long totalPages;
    private final int page;
    private final int size;

    public PageResponseDto(Page<?> page) {
        this.totalItems = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        Pageable pageable = page.getPageable();
        this.page = pageable.getPageNumber();
        this.size = pageable.getPageSize();
    }

    public PageResponseDto(int totalItems, int page, int size) {
        this.totalItems = totalItems;
        this.totalPages = calculateTotalPages(totalItems, size);
        this.page = page;
        this.size = size;
    }

    /**
     * This method is used to calculate total pages for pagination
     *
     * @param totalItems totalItems
     * @param size       size
     * @return number of pages.
     */
    private int calculateTotalPages(int totalItems, int size) {
        if (totalItems == ZERO)
            return ZERO;
        if (totalItems <= size)
            return ONE;
        if (totalItems % size == ZERO)
            return totalItems / size;
        return totalItems / size + ONE;
    }

    /**
     * This method converts this object to json string for logging purpose.
     * All fields are obfuscated.
     *
     * @return json string
     */
    @Override
    public String toLogJson() {
        return toJson();
    }

}
