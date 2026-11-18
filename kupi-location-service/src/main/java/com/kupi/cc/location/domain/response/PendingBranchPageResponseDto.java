package com.kupi.cc.location.domain.response;

import com.kupi.cc.location.domain.entity.BranchSearchIndex;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Pending branch page response Dto
 */
@Getter
public class PendingBranchPageResponseDto extends PageResponseDto {

    private final List<PendingBranchListItemResponseDto> branches = new ArrayList<>();

    public PendingBranchPageResponseDto(Page<BranchSearchIndex> branchPage) {
        super(branchPage);
        for (BranchSearchIndex branchSearchIndex : branchPage) {
            branches.add(new PendingBranchListItemResponseDto(branchSearchIndex));
        }
    }
}
