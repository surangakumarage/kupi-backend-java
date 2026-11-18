package com.kupi.otp.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link OtpGenerator} class
 */
class OtpGeneratorTest {

    private Validator validator;
    private OtpGenerator otpGenerator;

    @BeforeEach
    void setUp() {
        otpGenerator = new OtpGenerator();
        validator = new Validator();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_GenerateValidOtp() {
        int otp = otpGenerator.generateOtp();
        assertTrue(validator.isValidOtp(String.valueOf(otp)));
    }
}