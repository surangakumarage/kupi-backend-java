package com.kupi.util.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mail request dto
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class MailRequestDto extends RequestDto {

    private String recipientAddress;
    private String title;
    private String content;
    private boolean htmlBody;

    @Override
    public String toLogJson() {
        return toJson();
    }

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(recipientAddress) && isNonEmpty(title) && isNonEmpty(content);
    }
}
