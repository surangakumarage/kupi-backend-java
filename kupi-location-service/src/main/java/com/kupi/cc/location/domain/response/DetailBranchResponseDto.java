package com.kupi.cc.location.domain.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kupi.cc.location.domain.entity.Branch;
import lombok.Getter;
import lombok.Setter;

/**
 * Detail Branch ResponseDto
 */

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetailBranchResponseDto extends BasicBranchResponseDto {

    private LocationResponseDto location;
    private String comment;

    public DetailBranchResponseDto(Branch branch) {
        super(branch);
        this.location = new LocationResponseDto(branch);
    }
}
