package com.kupi.cc.location.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Bulk Business Response Dto
 */
@Getter
@Setter
public class BulkBusinessResponseDto implements ResponseDto {

    private List<BusinessResponseDto> merchants;

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
