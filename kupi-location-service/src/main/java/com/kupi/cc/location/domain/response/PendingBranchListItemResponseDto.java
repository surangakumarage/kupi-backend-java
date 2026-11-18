package com.kupi.cc.location.domain.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kupi.cc.location.domain.entity.BranchSearchIndex;
import com.kupi.cc.location.enums.UserType;
import lombok.Getter;

/**
 * Pending branch list item response Dto
 */
@Getter
public class PendingBranchListItemResponseDto implements ResponseDto {

    private final String id;
    private final String name;
    private final String address;
    private final String contactNo;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BasicMerchantOrBankResponseDto merchant;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BasicMerchantOrBankResponseDto bank;

    public PendingBranchListItemResponseDto(BranchSearchIndex branchSearchIndex) {
        this.id = branchSearchIndex.getId();
        this.name = branchSearchIndex.getName();
        this.address = branchSearchIndex.getAddress();
        this.contactNo = branchSearchIndex.getContactNo();
        BasicMerchantOrBankResponseDto businessInfo =
                new BasicMerchantOrBankResponseDto(branchSearchIndex.getUserId(),
                        branchSearchIndex.getBusinessName(), branchSearchIndex.getBusinessImageUrl());
        if (branchSearchIndex.getUserType().equals(UserType.MERCHANT))
            this.merchant = businessInfo;
        else
            this.bank = businessInfo;
    }

    /**
     * This method converts object to json string for logging purpose.
     * PII data should be obfuscated.
     *
     * @return json string
     */
    @Override
    public String toLogJson() {
        return null;
    }
}
