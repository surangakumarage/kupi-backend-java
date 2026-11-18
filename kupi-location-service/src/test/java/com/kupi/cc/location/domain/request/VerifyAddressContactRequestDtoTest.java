package com.kupi.cc.location.domain.request;

import com.kupi.cc.location.enums.VerificationStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link VerifyAddressContactRequestDto} class
 */
class VerifyAddressContactRequestDtoTest {

    private static final String BRANCH_ID = "brid-123456789";

    /**
     * Required fields existence check
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        VerifyAddressContactRequestDto verifyAddressContactRequestDto = new VerifyAddressContactRequestDto();
        verifyAddressContactRequestDto.setId(BRANCH_ID);
        verifyAddressContactRequestDto.setAction(VerificationStatus.VERIFIED);
        assertTrue(verifyAddressContactRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_IdIsNull() {
        VerifyAddressContactRequestDto verifyAddressContactRequestDto = new VerifyAddressContactRequestDto();
        verifyAddressContactRequestDto.setId(null);
        verifyAddressContactRequestDto.setAction(VerificationStatus.VERIFIED);
        assertFalse(verifyAddressContactRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_IdIsEmpty() {
        VerifyAddressContactRequestDto verifyAddressContactRequestDto = new VerifyAddressContactRequestDto();
        verifyAddressContactRequestDto.setId("");
        verifyAddressContactRequestDto.setAction(VerificationStatus.VERIFIED);
        assertFalse(verifyAddressContactRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ActionIsNull() {
        VerifyAddressContactRequestDto verifyAddressContactRequestDto = new VerifyAddressContactRequestDto();
        verifyAddressContactRequestDto.setId(BRANCH_ID);
        verifyAddressContactRequestDto.setAction(null);
        assertFalse(verifyAddressContactRequestDto.isRequiredAvailable());
    }
}