package com.kupi.cc.location.domain.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link BranchUpdateRequestDto} class
 */
class BranchUpdateRequestDtoTest {

    private static final String BRANCH_ID = "brid-123456789";
    private static final String NAME = "NAME";
    private static final String USER_ID = "uid-123456789";
    private static final String LOCATION_MAP_URL = "LOCATION_MAP_URL";
    private static final String GLOBAL_CODE = "GLOBAL_CODE";
    private static final String SHORT_NAME = "SHORT_NAME";
    private static final String LONG_NAME = "LONG_NAME";
    private static final String VALUE = "VALUE";
    private static final Double LATITUDE = 123456789.0;
    private static final Double LONGITUDE = 123456789.0;

    /**
     * Required fields existence check
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(BRANCH_ID, NAME, USER_ID,
                LOCATION_MAP_URL, LATITUDE, LONGITUDE, GLOBAL_CODE, SHORT_NAME, LONG_NAME, VALUE);
        assertTrue(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_IdIsEmpty() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto("", NAME, USER_ID,
                LOCATION_MAP_URL, LATITUDE, LONGITUDE, GLOBAL_CODE, SHORT_NAME, LONG_NAME, VALUE);
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_IdIsNull() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(null, NAME, USER_ID,
                LOCATION_MAP_URL, LATITUDE, LONGITUDE, GLOBAL_CODE, SHORT_NAME, LONG_NAME, VALUE);
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NameIsEmpty() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(BRANCH_ID, "", USER_ID,
                LOCATION_MAP_URL, LATITUDE, LONGITUDE, GLOBAL_CODE, SHORT_NAME, LONG_NAME, VALUE);
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NameIsNull() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(BRANCH_ID, null, USER_ID,
                LOCATION_MAP_URL, LATITUDE, LONGITUDE, GLOBAL_CODE, SHORT_NAME, LONG_NAME, VALUE);
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdIsEmpty() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(BRANCH_ID, NAME, "",
                LOCATION_MAP_URL, LATITUDE, LONGITUDE, GLOBAL_CODE, SHORT_NAME, LONG_NAME, VALUE);
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_UserIdIsNull() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(BRANCH_ID, NAME, null,
                LOCATION_MAP_URL, LATITUDE, LONGITUDE, GLOBAL_CODE, SHORT_NAME, LONG_NAME, VALUE);
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocationMapUrlIsEmpty() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(BRANCH_ID, NAME, USER_ID,
                "", LATITUDE, LONGITUDE, GLOBAL_CODE, SHORT_NAME, LONG_NAME, VALUE);
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocationMapUrlIsNull() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(BRANCH_ID, NAME, USER_ID,
                null, LATITUDE, LONGITUDE, GLOBAL_CODE, SHORT_NAME, LONG_NAME, VALUE);
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_GlobalCodeIsEmpty() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(BRANCH_ID, NAME, USER_ID,
                LOCATION_MAP_URL, LATITUDE, LONGITUDE, "", SHORT_NAME, LONG_NAME, VALUE);
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_GlobalCodeIsNull() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(BRANCH_ID, NAME, USER_ID,
                LOCATION_MAP_URL, LATITUDE, LONGITUDE, null, SHORT_NAME, LONG_NAME, VALUE);
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ShortNameIsEmpty() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(BRANCH_ID, NAME, USER_ID,
                LOCATION_MAP_URL, LATITUDE, LONGITUDE, GLOBAL_CODE, "", LONG_NAME, VALUE);
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ShortNameIsNull() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(BRANCH_ID, NAME, USER_ID,
                LOCATION_MAP_URL, LATITUDE, LONGITUDE, GLOBAL_CODE, null, LONG_NAME, VALUE);
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LongNameIsEmpty() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(BRANCH_ID, NAME, USER_ID,
                LOCATION_MAP_URL, LATITUDE, LONGITUDE, GLOBAL_CODE, SHORT_NAME, "", VALUE);
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LongNameIsNull() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(BRANCH_ID, NAME, USER_ID,
                LOCATION_MAP_URL, LATITUDE, LONGITUDE, GLOBAL_CODE, SHORT_NAME, null, VALUE);
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ValueIsEmpty() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(BRANCH_ID, NAME, USER_ID,
                LOCATION_MAP_URL, LATITUDE, LONGITUDE, GLOBAL_CODE, SHORT_NAME, LONG_NAME, "");
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ValueIsNull() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(BRANCH_ID, NAME, USER_ID,
                LOCATION_MAP_URL, LATITUDE, LONGITUDE, GLOBAL_CODE, SHORT_NAME, LONG_NAME, null);
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LatitudeIsNull() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(BRANCH_ID, NAME, USER_ID,
                LOCATION_MAP_URL, null, LONGITUDE, GLOBAL_CODE, SHORT_NAME, LONG_NAME, VALUE);
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LongitudeIsNull() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto(BRANCH_ID, NAME, USER_ID,
                LOCATION_MAP_URL, LATITUDE, null, GLOBAL_CODE, SHORT_NAME, LONG_NAME, VALUE);
        assertFalse(branchUpdateRequestDto.isRequiredAvailable());
    }

    private BranchUpdateRequestDto getSampleBranchUpdateRequestDto(String id, String name, String userId,
                                                                   String locationMapUrl, Double latitude,
                                                                   Double longitude, String globalCode,
                                                                   String shortName, String longName, String value) {
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
        AddressRequestDto addressRequestDto = new AddressRequestDto();
        addressRequestDto.setValue(value);

        BranchUpdateRequestDto branchUpdateRequestDto = new BranchUpdateRequestDto();
        branchUpdateRequestDto.setId(id);
        branchUpdateRequestDto.setName(name);
        branchUpdateRequestDto.setUserId(userId);
        branchUpdateRequestDto.setLocation(locationRequestDto);
        branchUpdateRequestDto.setAddress(addressRequestDto);
        return branchUpdateRequestDto;
    }
}