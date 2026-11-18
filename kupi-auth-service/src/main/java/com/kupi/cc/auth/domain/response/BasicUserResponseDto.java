package com.kupi.cc.auth.domain.response;

import com.kupi.cc.auth.domain.entity.MerchantBankSearchIndex;
import com.kupi.cc.auth.domain.entity.User;
import com.kupi.cc.auth.enums.ApprovalStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BasicUserResponseDto {

    private static final String JOINED_ON = "Joined on ";
    private String id;
    private String name;
    private String imageUrl;
    private ApprovalStatus approvalStatus;
    private DateResponseDto joinedOn;
    private boolean isActive;
    private MobileNoResponseDto mobileNo;

    public BasicUserResponseDto(User user, String timeZone) {
        this.id = user.getId();
        this.name = user.getFullName();
        this.imageUrl = user.getImageUrl();
        this.approvalStatus = user.getApprovalStatus();
        this.joinedOn = new DateResponseDto(user.getCreatedAt().getTime(), timeZone,
                user.getCreatedAt(), JOINED_ON);
        this.isActive = true;
        this.mobileNo = new MobileNoResponseDto(user.getMobileNo());
    }

    public BasicUserResponseDto(MerchantBankSearchIndex merchantBankSearchIndex, String timeZone) {
        this.id = merchantBankSearchIndex.getUserId();
        this.name = merchantBankSearchIndex.getFullName();
        this.imageUrl = merchantBankSearchIndex.getImageUrl();
        this.approvalStatus = merchantBankSearchIndex.getMerchantApprovalStatus() == ApprovalStatus.UNBLOCKED ?
                ApprovalStatus.ENABLED : merchantBankSearchIndex.getMerchantApprovalStatus();
        this.joinedOn = new DateResponseDto(merchantBankSearchIndex.getJoinedOn().getTime(), timeZone,
                merchantBankSearchIndex.getJoinedOn(), JOINED_ON);
        this.isActive = checkMerchantIsActive(merchantBankSearchIndex);
    }

    /**
     * This method is used to check merchant is active or not.
     *
     * @param merchantBankSearchIndex merchantBankSearchIndex
     * @return true/false
     */
    private boolean checkMerchantIsActive(MerchantBankSearchIndex merchantBankSearchIndex) {
        ApprovalStatus userApprovalStatus = merchantBankSearchIndex.getMerchantApprovalStatus();
        ApprovalStatus businessApprovalStatus = merchantBankSearchIndex.getBusinessApprovalStatus();
        return businessApprovalStatus == ApprovalStatus.APPROVED &&
                (userApprovalStatus == ApprovalStatus.APPROVED || userApprovalStatus == ApprovalStatus.UNBLOCKED);
    }
}
