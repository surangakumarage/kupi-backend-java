package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Wrapper for UI merchant list table response
 */
@Getter
@Setter
public class MerchantTableListResponseDto extends ResponseDto {
    private static final long serialVersionUID = 1L;

    private long totalItems;
    private int totalPages;
    private int page;
    private int size;
    private List<MerchantTableResponseDto> merchants;

    public MerchantTableListResponseDto(AllMerchantListResponseDto allMerchantListResponseDto, List<MerchantTableResponseDto> merchants) {
        this.totalItems = allMerchantListResponseDto.getTotalItems();
        this.totalPages = allMerchantListResponseDto.getTotalPages();
        this.page = allMerchantListResponseDto.getPage();
        this.size = allMerchantListResponseDto.getSize();
        this.merchants = merchants;
    }
}
