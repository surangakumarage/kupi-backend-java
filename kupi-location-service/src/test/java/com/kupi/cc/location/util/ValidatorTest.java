package com.kupi.cc.location.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This classt tests {@link Validator} class
 */
class ValidatorTest {

    private static final String URL = "https://www.google.com";

    private final Validator validator = new Validator();

    /**
     * Is valid URL check
     */
    @Test
    void Should_ReturnTrue_When_UrlWithHttpsProtocol() {
        assertTrue(validator.isValidUrl(URL));
    }

    @Test
    void Should_ReturnTrue_When_UrlWithHttpProtocol() {
        assertTrue(validator.isValidUrl("http://www.google.com"));
    }

    @Test
    void Should_ReturnTrue_When_UrlWithoutWWW() {
        assertTrue(validator.isValidUrl("http://google.com"));
    }

    @Test
    void Should_ReturnFalse_When_UrlIsWithoutHttpOrHttpsProtocol() {
        assertFalse(validator.isValidUrl("htt:www.google.com"));
    }

    @Test
    void Should_ReturnFalse_When_UrlIsWithoutColon() {
        assertFalse(validator.isValidUrl("httpswww.google.com"));
    }

    @Test
    void Should_ReturnFalse_When_UrlIsWithout2ForwardSlash() {
        assertFalse(validator.isValidUrl("https:/www.google.com"));
    }

    @Test
    void Should_ReturnFalse_When_DomainNameIsNotPresent() {
        assertFalse(validator.isValidUrl("https://.com"));
    }

    @Test
    void Should_ReturnFalse_When_TopLevelDomainIsNotPresent() {
        assertFalse(validator.isValidUrl("https://google"));
    }
}