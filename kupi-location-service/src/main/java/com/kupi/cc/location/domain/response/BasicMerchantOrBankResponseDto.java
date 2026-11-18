package com.kupi.cc.location.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * BasicMerchantOrBankResponseDto
 */
@Getter
@Setter
@AllArgsConstructor
public class BasicMerchantOrBankResponseDto implements ResponseDto {

    private String id;
    private String name;
    private String imageUrl;

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
