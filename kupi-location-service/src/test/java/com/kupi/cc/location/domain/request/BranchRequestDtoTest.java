package com.kupi.cc.location.domain.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link BranchRequestDto} class
 */
class BranchRequestDtoTest {

    private static final String NAME = "NAME";
    private static final String USER_ID = "uid-123456789";
    private static final String ADDRESS = "ADDRESS";
    private static final String LOCATION_MAP_URL = "LOCATION_MAP_URL";
    private static final String GLOBAL_CODE = "GLOBAL_CODE";
    private static final String LONG_NAME = "LONG_NAME";
    private static final String SHORT_NAME = "SHORT_NAME";
    private static final Double LATITUDE = 123456789.0;
    private static final Double LONGITUDE = 123456789.0;

    /**
     * Required fields existence check
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto(NAME, USER_ID, ADDRESS, LOCATION_MAP_URL, LATITUDE,
                LONGITUDE, GLOBAL_CODE, LONG_NAME, SHORT_NAME);
        assertTrue(branchRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NameIsNull() {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto(null, USER_ID, ADDRESS, LOCATION_MAP_URL, LATITUDE,
                LONGITUDE, GLOBAL_CODE, LONG_NAME, SHORT_NAME);
        assertFalse(branchRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NameIsEmpty() {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto("", USER_ID, ADDRESS, LOCATION_MAP_URL, LATITUDE,
                LONGITUDE, GLOBAL_CODE, LONG_NAME, SHORT_NAME);
        assertFalse(branchRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdIsNull() {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto(NAME, null, ADDRESS, LOCATION_MAP_URL, LATITUDE,
                LONGITUDE, GLOBAL_CODE, LONG_NAME, SHORT_NAME);
        assertFalse(branchRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdIsEmpty() {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto(NAME, "", ADDRESS, LOCATION_MAP_URL, LATITUDE,
                LONGITUDE, GLOBAL_CODE, LONG_NAME, SHORT_NAME);
        assertFalse(branchRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_AddressIsNull() {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto(NAME, USER_ID, null, LOCATION_MAP_URL, LATITUDE,
                LONGITUDE, GLOBAL_CODE, LONG_NAME, SHORT_NAME);
        assertFalse(branchRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_AddressIsEmpty() {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto(NAME, USER_ID, "", LOCATION_MAP_URL, LATITUDE,
                LONGITUDE, GLOBAL_CODE, LONG_NAME, SHORT_NAME);
        assertFalse(branchRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocationMapUrlIsNull() {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto(NAME, USER_ID, ADDRESS, null, LATITUDE,
                LONGITUDE, GLOBAL_CODE, LONG_NAME, SHORT_NAME);
        assertFalse(branchRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocationMapUrlIsEmpty() {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto(NAME, USER_ID, ADDRESS, "", LATITUDE,
                LONGITUDE, GLOBAL_CODE, LONG_NAME, SHORT_NAME);
        assertFalse(branchRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LatitudeIsNull() {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto(NAME, USER_ID, ADDRESS, LOCATION_MAP_URL, null,
                LONGITUDE, GLOBAL_CODE, LONG_NAME, SHORT_NAME);
        assertFalse(branchRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LongitudeIsNull() {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto(NAME, USER_ID, ADDRESS, LOCATION_MAP_URL, LATITUDE,
                null, GLOBAL_CODE, LONG_NAME, SHORT_NAME);
        assertFalse(branchRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_GlobalCodeIsNull() {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto(NAME, USER_ID, ADDRESS, LOCATION_MAP_URL, LATITUDE,
                LONGITUDE, null, LONG_NAME, SHORT_NAME);
        assertFalse(branchRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_GlobalCodeIsEmpty() {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto(NAME, USER_ID, ADDRESS, LOCATION_MAP_URL, LATITUDE,
                LONGITUDE, "", LONG_NAME, SHORT_NAME);
        assertFalse(branchRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LongNameIsNull() {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto(NAME, USER_ID, ADDRESS, LOCATION_MAP_URL, LATITUDE,
                LONGITUDE, GLOBAL_CODE, null, SHORT_NAME);
        assertFalse(branchRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LongNameIsEmpty() {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto(NAME, USER_ID, ADDRESS, LOCATION_MAP_URL, LATITUDE,
                LONGITUDE, GLOBAL_CODE, "", SHORT_NAME);
        assertFalse(branchRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ShortNameIsNull() {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto(NAME, USER_ID, ADDRESS, LOCATION_MAP_URL, LATITUDE,
                LONGITUDE, GLOBAL_CODE, LONG_NAME, null);
        assertFalse(branchRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ShortNameIsEmpty() {
        BranchRequestDto branchRequestDto = getSampleBranchRequestDto(NAME, USER_ID, ADDRESS, LOCATION_MAP_URL, LATITUDE,
                LONGITUDE, GLOBAL_CODE, LONG_NAME, "");
        assertFalse(branchRequestDto.isRequiredAvailable());
    }

    private BranchRequestDto getSampleBranchRequestDto(String name, String userId, String address, String locationMapUrl,
                                                       Double latitude, Double longitude, String globalCode,
                                                       String longName, String shortName) {
        AddressRequestDto addressRequestDto = new AddressRequestDto();
        addressRequestDto.setValue(address);

        LongShortNameRequestDto longShortNameRequestDto = new LongShortNameRequestDto();
        longShortNameRequestDto.setShortName(shortName);
        longShortNameRequestDto.setLongName(longName);

        LocationRequestDto locationRequestDto = new LocationRequestDto();
        locationRequestDto.setLocationMapUrl(locationMapUrl);
        locationRequestDto.setLatitude(latitude);
        locationRequestDto.setLongitude(longitude);
        locationRequestDto.setGlobalCode(globalCode);
        locationRequestDto.setCity(longShortNameRequestDto);
        locationRequestDto.setCountry(longShortNameRequestDto);

        BranchRequestDto branchRequestDto = new BranchRequestDto();
        branchRequestDto.setName(name);
        branchRequestDto.setUserId(userId);
        branchRequestDto.setAddress(addressRequestDto);
        branchRequestDto.setLocation(locationRequestDto);
        return branchRequestDto;
    }
}