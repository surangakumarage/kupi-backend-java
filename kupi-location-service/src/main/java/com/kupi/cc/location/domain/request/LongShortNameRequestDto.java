package com.kupi.cc.location.domain.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Long & Short Name RequestDto
 */

@Getter
@Setter
@NoArgsConstructor
public class LongShortNameRequestDto extends RequestDto {

    private static final String CONNECTOR = ", ";
    private String longName;
    private String shortName;

    /**
     * This method combines long name and short name
     *
     * @return combined name
     */
    public String getCombinedName() {
        return longName + CONNECTOR + shortName;
    }

    /**
     * This method checks whether the required fields are not null
     *
     * @return true/false
     */
    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(longName) && isNonEmpty(shortName);
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