package com.kupi.otp.service;

import com.kupi.otp.domain.request.MobileNoRequestDto;
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
 * This class tests {@link SmsOtpService} class
 */
class SmsOtpServiceTest {

    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "771109101";
    private static final String MOBILE_NUMBER = "+94-771109101";
    private static final int OTP_NO = 123456;
    private static final String KEY = "key value";

    @Mock
    private CacheRepository cacheRepository;
    @Mock
    private NotificationService notificationService;
    private SmsOtpService smsOtpService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        smsOtpService = new SmsOtpService(cacheRepository, notificationService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_SendOtp() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        smsOtpService.sendOtp(mobileNoRequestDto, MOBILE_NUMBER, OTP_NO);
        verify(cacheRepository, times(1)).put(MOBILE_NUMBER, OTP_NO);
        verify(notificationService, times(1)).sendSms(mobileNoRequestDto, String.valueOf(OTP_NO));
    }

    @Test
    void Should_ReturnTrue_When_OtpIsVerified() {
        when(cacheRepository.get(KEY)).thenReturn(Optional.of(String.valueOf(OTP_NO)));
        assertTrue(smsOtpService.verifyOtp(KEY, String.valueOf(OTP_NO)));
    }

    @Test
    void Should_ReturnFalse_When_OtpNotVerified() {
        when(cacheRepository.get(KEY)).thenReturn(Optional.of(String.valueOf(125487)));
        assertFalse(smsOtpService.verifyOtp(KEY, String.valueOf(OTP_NO)));
    }

    @Test
    void Should_ThrowInvalidOtpKeyException_When_OtpValueIsEmptyFromCacheRepository() {
        when(cacheRepository.get(KEY)).thenReturn(Optional.empty());
        InvalidOtpKeyException exception = assertThrows(InvalidOtpKeyException.class, () ->
                smsOtpService.verifyOtp(KEY, String.valueOf(OTP_NO)));
        assertEquals("Invalid otp key ", exception.getMessage());
    }

    /**
     * This method creates sample MobileNoRequestDto
     *
     * @return MobileNoRequestDto
     */
    private MobileNoRequestDto getSampleMobileNoRequestDto() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto();
        mobileNoRequestDto.setCountryCode(COUNTRY_CODE);
        mobileNoRequestDto.setLocalNumber(LOCAL_NUMBER);
        return mobileNoRequestDto;
    }
}