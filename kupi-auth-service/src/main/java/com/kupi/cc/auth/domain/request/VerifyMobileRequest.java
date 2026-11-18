package com.kupi.cc.auth.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerifyMobileRequest extends RequestDto {

    private MobileNoRequestDto mobileNo;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
