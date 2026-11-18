package com.kupi.cc.location.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class MailRequestDto extends RequestDto {

    private String recipientAddress;
    private String title;
    private String content;

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(recipientAddress) && isNonEmpty(title) && isNonEmpty(content);
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