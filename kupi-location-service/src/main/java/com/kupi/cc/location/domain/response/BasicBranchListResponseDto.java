package com.kupi.cc.location.domain.response;

import com.kupi.cc.location.domain.entity.ApprovedBranch;
import com.kupi.cc.location.enums.ApprovalStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic Branch List ResponseDto
 */
@Getter
@Setter
public class BasicBranchListResponseDto extends PageResponseDto {

    private List<BasicBranchResponseDto> branches = new ArrayList<>();

    public BasicBranchListResponseDto(Page<ApprovedBranch> approvedBranchPage) {
        super(approvedBranchPage);
        for (ApprovedBranch approvedBranch : approvedBranchPage) {
            AddressResponseDto addressResponseDto = new AddressResponseDto(approvedBranch.getAddress(),
                    approvedBranch.getProofUrl(), approvedBranch.getAddressVerified());
            ContactNoResponseDto contactNoResponseDto =
                    new ContactNoResponseDto(approvedBranch.getContactNo(), approvedBranch.getContactNoVerified());
            BasicBranchResponseDto basicBranchResponseDto = new BasicBranchResponseDto(approvedBranch,
                    addressResponseDto, contactNoResponseDto, ApprovalStatus.APPROVED);
            branches.add(basicBranchResponseDto);
        }
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
