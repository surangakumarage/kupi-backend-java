package com.kupi.cc.base.domain.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * All merchant list response dto
 */
@Getter
@Setter
public class AllMerchantListResponseDto extends PageResponseDto {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<AuthMerchantListResponseDto> merchants;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<AuthMerchantListResponseDto> banks;

    /**
     * Constructor for Jackson deserialization from auth service response
     */
    @JsonCreator
    public AllMerchantListResponseDto(
            @JsonProperty("totalItems") long totalItems,
            @JsonProperty("totalPages") int totalPages,
            @JsonProperty("page") int page,
            @JsonProperty("size") int size,
            @JsonProperty("merchants") List<AuthMerchantListResponseDto> merchants,
            @JsonProperty("banks") List<AuthMerchantListResponseDto> banks) {
        super((int) totalItems, page, size);
        this.merchants = merchants;
        this.banks = banks;
    }

    public AllMerchantListResponseDto(long totalItems, int page, int size, List<AuthMerchantListResponseDto> merchants) {
        super((int) totalItems, page, size);
        this.merchants = merchants;
        this.banks = null;
    }
}

