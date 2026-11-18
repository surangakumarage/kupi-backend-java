package com.kupi.cc.auth.domain.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ForgotPasswordRequestDto extends RequestDto {

    private MobileNoRequestDto mobileNo;

    /**
     * This method checks all required fields are available.
     *
     * @return true/ false
     */
    @Override
    public boolean isRequiredAvailable() {
        if (mobileNo != null) {
            return mobileNo.isRequiredAvailable();
        }
        return false;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
