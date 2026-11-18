package com.kupi.notification.domain.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Deal Group RequestDto
 */
@Getter
@Setter
public class DealGroupRequestDto extends RequestDto {

    private BasicUserRequestDto merchant;
    private List<BasicDealRequestDto> deals;

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
