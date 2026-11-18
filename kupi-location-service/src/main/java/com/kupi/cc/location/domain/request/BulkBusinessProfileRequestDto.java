package com.kupi.cc.location.domain.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Bulk Business Profile RequestDto
 */
@Getter
@Setter
@AllArgsConstructor
public class BulkBusinessProfileRequestDto extends RequestDto {

    private List<String> userIds;

    /**
     * This method converts this object to json string for logging purpose.
     * All fields are obfuscated.
     *
     * @return json string
     */
    @Override
    public String toLogJson() {
        return toJson();
    }
}
