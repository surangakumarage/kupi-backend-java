package com.kupi.cc.location.domain.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kupi.cc.location.domain.entity.ApprovedBranch;
import com.kupi.cc.location.domain.entity.Branch;
import com.kupi.cc.location.enums.ApprovalStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Basic Branch ResponseDto
 */
@Getter
@Setter
@NoArgsConstructor
public class BasicBranchResponseDto implements ResponseDto {

    private String id;
    private String name;
    private AddressResponseDto address;
    private ContactNoResponseDto contactNo;
    private ApprovalStatus status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String comment = null;

    public BasicBranchResponseDto(Branch branch) {
        this.id = branch.getId();
        this.name = branch.getName();
        this.address = new AddressResponseDto(branch.getAddress(), branch.getProofUrl(), branch.getAddressVerified());
        this.contactNo = new ContactNoResponseDto(branch.getContactNo(), branch.getContactNoVerified());
        this.status = branch.getApprovalStatus();
    }

    public BasicBranchResponseDto(Branch branch, String comment) {
        this(branch);
        this.comment = comment;
    }

    public BasicBranchResponseDto(ApprovedBranch approvedBranch, AddressResponseDto address,
                                  ContactNoResponseDto contactNo, ApprovalStatus status) {
        this.id = approvedBranch.getId();
        this.name = approvedBranch.getName();
        this.address = address;
        this.contactNo = contactNo;
        this.status = status;
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