package com.kupi.cc.auth.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kupi.cc.auth.domain.entity.BankBusiness;
import com.kupi.cc.auth.domain.entity.Business;
import com.kupi.cc.auth.enums.ApprovalStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Grouped profile views response dto
 */
@Getter
@Setter
public class GroupedProfileViewsResponseDto implements ResponseDto {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String merchantId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String bankId;
    private String businessName;
    private String imageUrl;
    private ApprovalStatus approvalStatus;
    private long totalViewCount;
    @JsonIgnore
    private int totalRows;

    public GroupedProfileViewsResponseDto(Business business, long totalViewCount, int totalRows) {
        this.merchantId = business.getMerchant().getId();
        this.businessName = business.getBusinessName();
        this.imageUrl = business.getImageUrl();
        this.approvalStatus = business.getApprovalStatus();
        this.totalViewCount = totalViewCount;
        this.totalRows = totalRows;
    }

    public GroupedProfileViewsResponseDto(BankBusiness bankBusiness, long totalViewCount, int totalRows) {
        this.merchantId = bankBusiness.getBank().getId();
        this.businessName = bankBusiness.getBusinessName();
        this.imageUrl = bankBusiness.getImageUrl();
        this.approvalStatus = bankBusiness.getApprovalStatus();
        this.totalViewCount = totalViewCount;
        this.totalRows = totalRows;
    }

    /**
     * This method converts object to json string for logging purpose.
     * PII data should be obfuscated.
     *
     * @return json string
     */
    @Override
    public String toLogJson() {
        return toJson();
    }
}
