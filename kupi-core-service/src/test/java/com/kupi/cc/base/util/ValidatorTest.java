package com.kupi.cc.base.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link Validator} class
 */
class ValidatorTest {

    private static final String EMAIL = "rengy@tokoin.io";
    private static final String MOBILE_NO = "+94-771234567";
    private static final String NIC = "123456789v";

    private final Validator validator = new Validator();

    /**
     * Email validity check
     */
    @Test
    void Should_ReturnTrue_When_EmailIsValid() {
        assertTrue(validator.isValidEmail(EMAIL));
    }

    @Test
    void Should_ReturnFalse_When_EmailIsWithoutDot() {
        assertFalse(validator.isValidEmail("rengy@tokoinio"));
    }

    @Test
    void Should_ReturnFalse_When_EmailIsWithoutAt() {
        assertFalse(validator.isValidEmail("rengytokoin.io"));
    }

    /**
     * NIC validity check
     */
    @Test
    void Should_ReturnTrue_When_NICIsValid() {
        assertTrue(validator.isValidNIC(NIC));
    }

    @Test
    void Should_ReturnFalse_When_NICIsWithoutV() {
        assertFalse(validator.isValidNIC("0123456789"));
    }

    @Test
    void Should_ReturnFalse_When_NICIsNotTenCharacters() { assertFalse(validator.isValidNIC("123456v")); }

    /**
     * Mobile No validity check
     */
    @Test
    void Should_ReturnTrue_When_MobileNoIsValid() {
        assertTrue(validator.isValidMobileNoWithCountryCode(MOBILE_NO));
    }

    @Test
    void Should_ReturnFalse_When_MobileNoWithoutCountryCode() {
        assertFalse(validator.isValidMobileNoWithCountryCode("081123123"));
    }

    @Test
    void Should_ReturnFalse_When_MobileNoWithoutAllDigits() {
        assertFalse(validator.isValidMobileNoWithCountryCode("+6281"));
    }

    @Test
    void Should_ReturnFalse_When_MobileNoHasSpaceInTheMiddle() {
        assertFalse(validator.isValidMobileNoWithCountryCode("+947233 45345"));
    }

    @Test
    void Should_ReturnFalse_For_NumberWithMoreThanFiveDigitCountryCode() {
        assertFalse(validator.isValidMobileNoWithCountryCode("+623452-808733998"));
    }
}