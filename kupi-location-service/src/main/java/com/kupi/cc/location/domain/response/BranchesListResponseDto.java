package com.kupi.cc.location.domain.response;

import com.kupi.cc.location.domain.entity.Branch;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Branches List ResponseDto
 */
@Getter
@Setter
public class BranchesListResponseDto extends PageResponseDto {

    private List<BasicBranchResponseDto> branches;

    public BranchesListResponseDto(Page<Branch> branchPage) {
        super(branchPage);
        this.branches = new ArrayList<>();
        branchPage.getContent().forEach(branch -> {
            var basicBranchResponseDto = new BasicBranchResponseDto(branch);
            branches.add(basicBranchResponseDto);
        });
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