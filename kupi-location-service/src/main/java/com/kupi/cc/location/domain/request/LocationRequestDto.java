package com.kupi.cc.location.domain.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Location ResponseDto
 */
@Getter
@Setter
@NoArgsConstructor
public class LocationRequestDto extends RequestDto {
    private static final Double MIN_LATITUDE = -90.0000;
    private static final Double MAX_LATITUDE = 90.0000;
    private static final Double MIN_LONGITUDE = -180.0000;
    private static final Double MAX_LONGITUDE = 180.0000;

    private String locationMapUrl;
    private Double latitude;
    private Double longitude;
    private String globalCode;
    private LongShortNameRequestDto city;
    private LongShortNameRequestDto country;

    /**
     * This method checks if the given latitude is in correct range
     *
     * @return true/false
     */
    public boolean isValidLatitude() {
        return (latitude >= MIN_LATITUDE && latitude <= MAX_LATITUDE);
    }

    /**
     * This method checks if the given longitude is in correct range
     *
     * @return true/false
     */
    public boolean isValidLongitude() {
        return (longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE);
    }

    /**
     * This method checks whether the required fields are not null
     *
     * @return true/false
     */
    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(locationMapUrl) && latitude != null && longitude != null && isNonEmpty(globalCode) &&
                city.isRequiredAvailable() && country.isRequiredAvailable();
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
