package com.kupi.cc.location.domain.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Address ResponseDto
 */
@Getter
@Setter
@NoArgsConstructor
public class AddressResponseDto implements ResponseDto {

    private String value;
    private String proofUrl;
    private boolean verified;

    public AddressResponseDto(String value, String proofUrl, boolean verified) {
        this.value = value;
        this.proofUrl = proofUrl;
        this.verified = verified;
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
