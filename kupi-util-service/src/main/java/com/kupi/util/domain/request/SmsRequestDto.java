package com.kupi.util.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SMS request Dto
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SmsRequestDto extends RequestDto {

    private MobileNoRequestDto recipientNo;
    private String message;

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(message) && recipientNo.isRequiredAvailable();
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
