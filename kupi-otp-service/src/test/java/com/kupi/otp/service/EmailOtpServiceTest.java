package com.kupi.otp.service;

import com.kupi.otp.exception.InvalidOtpKeyException;
import com.kupi.otp.repository.CacheRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link EmailOtpService} class
 */
class EmailOtpServiceTest {

    private static final String EMAIL = "example@kupigroup.cpm.au";
    private static final String KEY = "key value";
    private static final int OTP = 123456;

    @Mock
    private CacheRepository cacheRepository;
    @Mock
    private NotificationService notificationService;
    private EmailOtpService emailOtpService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        emailOtpService = new EmailOtpService(cacheRepository, notificationService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_SendOtp() {
        emailOtpService.sendOtp(EMAIL, OTP);
        verify(cacheRepository, times(1)).put(EMAIL, OTP);
        verify(notificationService, times(1)).sendMail(EMAIL, String.valueOf(OTP));
    }

    @Test
    void Should_ReturnTrue_When_EmailOtpIsVerified() {
        when(cacheRepository.get(KEY)).thenReturn(Optional.of(String.valueOf(OTP)));
        assertTrue(emailOtpService.verifyOtp(KEY, String.valueOf(OTP)));
    }

    @Test
    void Should_ReturnFalse_When_EmailOtpIsNotVerified() {
        when(cacheRepository.get(KEY)).thenReturn(Optional.of(String.valueOf(124587)));
        assertFalse(emailOtpService.verifyOtp(KEY, String.valueOf(OTP)));
    }

    @Test
    void Should_Throw_When_EmailOtpIsEmpty() {
        when(cacheRepository.get(KEY)).thenReturn(Optional.empty());
        InvalidOtpKeyException exception = assertThrows(InvalidOtpKeyException.class, () ->
                emailOtpService.verifyOtp(KEY, String.valueOf(OTP)));
        assertEquals("Invalid otp key ", exception.getMessage());
    }
}