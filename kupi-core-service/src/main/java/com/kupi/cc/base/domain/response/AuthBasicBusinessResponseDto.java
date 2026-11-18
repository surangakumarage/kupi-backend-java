package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.enums.ApprovalStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Basic business response DTO from auth service
 * This matches the structure returned by auth service's BasicBusinessResponseDto
 */
@Getter
@Setter
@NoArgsConstructor
public class AuthBasicBusinessResponseDto extends ResponseDto {

    private String id;
    private String name;
    private ApprovalStatus approvalStatus;
    private String imageUrl;
}