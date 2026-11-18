package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.enums.ApprovalStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthBasicUserResponseDto {

    private String id;
    private String name;
    private String imageUrl;
    private ApprovalStatus approvalStatus;
    private DateResponseDto joinedOn;
    private boolean isActive;
    private MobileNoResponseDto mobileNo;
}