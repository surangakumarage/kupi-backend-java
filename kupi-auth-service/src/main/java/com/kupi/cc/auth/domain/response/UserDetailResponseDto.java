package com.kupi.cc.auth.domain.response;

import com.kupi.cc.auth.domain.entity.User;
import com.kupi.cc.auth.enums.ApprovalStatus;
import com.kupi.cc.auth.enums.Language;
import com.kupi.cc.auth.enums.RoleType;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * User Details DTO for Response
 */
@Getter
@NoArgsConstructor
public class UserDetailResponseDto implements ResponseDto {

    private String userId;
    private String fullName;
    private String email;
    private MobileNoResponseDto mobileNo;
    private String imageUrl;
    private Language language;
    private boolean isRegisteredUser;
    private ApprovalStatus approvalStatus;
    private boolean isActive;
    private boolean isEmailVerified;
    private boolean isMobileVerified;
    private RoleType userType;
    private boolean demoVideoWatched;
    private boolean hasSeenEarlyBirdOffer;

    public UserDetailResponseDto(User user) {
        if (user != null) {
            this.userId = user.getId();
            this.fullName = user.getFullName();
            this.email = user.getEmail();
            this.mobileNo = new MobileNoResponseDto(user.getMobileNo());
            this.imageUrl = user.getImageUrl();
            this.language = user.getLanguage();
            this.isRegisteredUser = user.isRegisteredUser();
            this.approvalStatus = user.getApprovalStatus();
            this.isMobileVerified = user.isMobileVerified();
            this.isEmailVerified = user.isEmailVerified();
            this.isActive = true;
            this.userType = RoleType.valueOf(user.getRole().getName());
            this.demoVideoWatched= user.isDemoVideoWatched();
            this.hasSeenEarlyBirdOffer = user.isHasSeenEarlyBirdOffer();
        }
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
