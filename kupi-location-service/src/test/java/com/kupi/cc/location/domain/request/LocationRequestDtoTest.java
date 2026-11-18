package com.kupi.cc.location.domain.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link LocationRequestDto} class
 */
class LocationRequestDtoTest {

    private static final String LOCATION_MAP_URL = "LOCATION_MAP_URL";
    private static final Double LATITUDE = 0.00;
    private static final Double LONGITUDE = 0.00;
    private static final String GLOBAL_CODE = "GLOBAL_CODE";
    private static final String SHORT_NAME = "SHORT_NAME";
    private static final String LONG_NAME = "LONG_NAME";

    /**
     * Required fields existance check
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        assertTrue(locationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocationMapUrlIsEmpty() {
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setLocationMapUrl("");
        assertFalse(locationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocationMapUrlIsNull() {
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setLocationMapUrl(null);
        assertFalse(locationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LatitudeIsNull() {
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setLatitude(null);
        assertFalse(locationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LongitudeIsNull() {
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setLongitude(null);
        assertFalse(locationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_GlobalCodelIsEmpty() {
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setGlobalCode("");
        assertFalse(locationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_GlobalCodelIsNull() {
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setGlobalCode(null);
        assertFalse(locationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CityShortNameIsEmpty() {
        LongShortNameRequestDto city = getSampleLongShortNameRequestDto();
        city.setShortName("");
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setCity(city);
        assertFalse(locationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CityShortNameIsNull() {
        LongShortNameRequestDto city = getSampleLongShortNameRequestDto();
        city.setShortName(null);
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setCity(city);
        assertFalse(locationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CityLongNameIsEmpty() {
        LongShortNameRequestDto city = getSampleLongShortNameRequestDto();
        city.setLongName("");
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setCity(city);
        assertFalse(locationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CityLongNameIsNull() {
        LongShortNameRequestDto city = getSampleLongShortNameRequestDto();
        city.setLongName(null);
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setCity(city);
        assertFalse(locationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CountryShortNameIsEmpty() {
        LongShortNameRequestDto country = getSampleLongShortNameRequestDto();
        country.setShortName("");
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setCountry(country);
        assertFalse(locationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CountryShortNameIsNull() {
        LongShortNameRequestDto country = getSampleLongShortNameRequestDto();
        country.setShortName(null);
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setCountry(country);
        assertFalse(locationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CountryLongNameIsEmpty() {
        LongShortNameRequestDto country = getSampleLongShortNameRequestDto();
        country.setLongName("");
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setCountry(country);
        assertFalse(locationRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CountryLongNameIsNull() {
        LongShortNameRequestDto country = getSampleLongShortNameRequestDto();
        country.setLongName(null);
        LocationRequestDto locationRequestDto = getSampleLocationRequestDto();
        locationRequestDto.setCountry(country);
        assertFalse(locationRequestDto.isRequiredAvailable());
    }

    /**
     * Latitude validity check
     */
    @Test
    void Should_ReturnTrue_When_LatitudeIsValid() {
        LocationRequestDto locationRequestDto = new LocationRequestDto();
        locationRequestDto.setLatitude(LATITUDE);
        assertTrue(locationRequestDto.isValidLatitude());
    }

    @Test
    void Should_ReturnFalse_When_LatitudeIsNotValid() {
        LocationRequestDto locationRequestDto = new LocationRequestDto();
        locationRequestDto.setLatitude(LATITUDE + 91.0);
        assertFalse(locationRequestDto.isValidLatitude());
    }

    /**
     * Longitude validity check
     */
    @Test
    void Should_ReturnTrue_When_LongitudeIsValid() {
        LocationRequestDto locationRequestDto = new LocationRequestDto();
        locationRequestDto.setLongitude(LONGITUDE);
        assertTrue(locationRequestDto.isValidLongitude());
    }

    @Test
    void Should_ReturnFalse_When_LongitudeIsNotValid() {
        LocationRequestDto locationRequestDto = new LocationRequestDto();
        locationRequestDto.setLongitude(LONGITUDE + 181.0);
        assertFalse(locationRequestDto.isValidLongitude());
    }

    private LocationRequestDto getSampleLocationRequestDto() {
        LocationRequestDto locationRequestDto = new LocationRequestDto();
        locationRequestDto.setLocationMapUrl(LOCATION_MAP_URL);
        locationRequestDto.setLatitude(LATITUDE);
        locationRequestDto.setLongitude(LONGITUDE);
        locationRequestDto.setGlobalCode(GLOBAL_CODE);
        locationRequestDto.setCity(getSampleLongShortNameRequestDto());
        locationRequestDto.setCountry(getSampleLongShortNameRequestDto());
        return locationRequestDto;
    }

    private LongShortNameRequestDto getSampleLongShortNameRequestDto() {
        LongShortNameRequestDto longShortNameRequestDto = new LongShortNameRequestDto();
        longShortNameRequestDto.setShortName(SHORT_NAME);
        longShortNameRequestDto.setLongName(LONG_NAME);
        return longShortNameRequestDto;
    }
}