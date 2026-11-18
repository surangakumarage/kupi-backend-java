package com.kupi.cc.auth.service;

import com.kupi.cc.auth.domain.entity.OTPToken;
import com.kupi.cc.auth.domain.request.MobileNoRequestDto;
import com.kupi.cc.auth.exception.InvalidTokenException;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.repository.OTPTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@Transactional
public class OTPTokenService {

    private final OTPTokenRepository otpTokenRepository;
    private final NotificationService notificationService;
    private final boolean dummyOtpEnabled;
    private final String dummyOtpPhoneNumber;
    private final String dummyOtpCode;

    @Autowired
    public OTPTokenService(OTPTokenRepository otpTokenRepository,
                           NotificationService notificationService,
                           @Value("${otp.dummy.enabled:false}") boolean dummyOtpEnabled,
                           @Value("${otp.dummy.phone-number:}") String dummyOtpPhoneNumber,
                           @Value("${otp.dummy.code:123456}") String dummyOtpCode) {
        this.otpTokenRepository = otpTokenRepository;
        this.notificationService = notificationService;
        this.dummyOtpEnabled = dummyOtpEnabled;
        this.dummyOtpPhoneNumber = dummyOtpPhoneNumber;
        this.dummyOtpCode = dummyOtpCode;
    }

    /**
     * Request OTP for mobile number. Generates and sends OTP via SMS.
     *
     * @param mobileNo mobile number
     * @return generated OTP token
     * @throws AuthServiceException if OTP generation or sending fails
     */
    public OTPToken requestOTP(MobileNoRequestDto mobileNo) throws AuthServiceException {
        try {
            // Delete any existing OTP for this mobile number
            String mobileNoFormatted = mobileNo.getNo();
            otpTokenRepository.deleteByMobileNo(mobileNoFormatted);

            // If dummy OTP feature is enabled and the mobile number matches, generate a fixed OTP and skip SMS sending
            if (dummyOtpEnabled && isDummyMobileNo(mobileNoFormatted)) {
                // Ensure uniqueness of the dummy OTP token by removing any existing record
                OTPToken existingByToken = otpTokenRepository.findByToken(dummyOtpCode);
                if (existingByToken != null) {
                    otpTokenRepository.delete(existingByToken);
                }

                OTPToken otpToken = new OTPToken(mobileNoFormatted);
                otpToken.setToken(dummyOtpCode);
                otpTokenRepository.save(otpToken);
                log.debug("Generated dummy OTP token for mobile: {}", mobileNoFormatted);
                return otpToken;
            }

            // Create new OTP token
            OTPToken otpToken = new OTPToken(mobileNoFormatted);
            otpTokenRepository.save(otpToken);
            log.debug("Generated OTP token for mobile: {}", mobileNoFormatted);

            // Send OTP via SMS
            try {
                notificationService.sendOtpSms(mobileNo, otpToken.getToken());
                log.debug("Sent OTP to mobile: {}", mobileNoFormatted);
            } catch (Exception e) {
                log.error("Failed to send OTP to mobile: {}", mobileNoFormatted, e);
                // Delete the token if SMS sending fails
                otpTokenRepository.delete(otpToken);
                throw new AuthServiceException("Failed to send OTP via SMS", e);
            }

            return otpToken;
        } catch (AuthServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error requesting OTP: {}", e.getMessage(), e);
            throw new AuthServiceException("Error requesting OTP", e);
        }
    }

    /**
     * Verify OTP token and return it if valid and not expired.
     *
     * @param token OTP token value
     * @return OTPToken if valid
     * @throws InvalidTokenException if token is invalid or expired
     */
    public OTPToken verifyOTP(String token) throws InvalidTokenException {
        try {
            OTPToken otpToken = otpTokenRepository.findByToken(token);

            if (otpToken == null) {
                // If no token in DB, but dummy OTP is enabled and token matches dummy code,
                // allow it for non-production flows (e.g. App Store review) without DB lookup.
                if (dummyOtpEnabled && dummyOtpCode != null && dummyOtpCode.equals(token)) {
                    log.warn("Dummy OTP token used without DB record for token: {}", token);
                    OTPToken dummyToken = new OTPToken();
                    dummyToken.setToken(dummyOtpCode);
                    dummyToken.setMobileNo(dummyOtpPhoneNumber);
                    dummyToken.setExpiration(2);
                    return dummyToken;
                }

                log.warn("OTP token not found: {}", token);
                throw new InvalidTokenException("Invalid OTP token");
            }

            if (isTokenExpired(otpToken)) {
                log.warn("OTP token expired: {}", token);
                throw new InvalidTokenException("OTP token has expired");
            }

            return otpToken;
        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying OTP token: {}", e.getMessage(), e);
            throw new InvalidTokenException("Error verifying OTP token");
        }
    }

    /**
     * Delete OTP token after it has been used.
     *
     * @param token OTP token value
     */
    public void deleteOTP(String token) {
        try {
            OTPToken otpToken = otpTokenRepository.findByToken(token);
            if (otpToken != null) {
                otpTokenRepository.delete(otpToken);
                log.debug("Deleted OTP token: {}", token);
            }
        } catch (Exception e) {
            log.error("Error deleting OTP token: {}", token, e);
        }
    }

    /**
     * Check if OTP token is expired.
     *
     * @param otpToken OTP token
     * @return true if expired, false otherwise
     */
    private boolean isTokenExpired(OTPToken otpToken) {
        return otpToken.getExpiryDate().before(new Date());
    }

    private boolean isDummyMobileNo(String mobileNoFormatted) {
        if (dummyOtpPhoneNumber == null || mobileNoFormatted == null) {
            return false;
        }
        String normalizedRequestNo = mobileNoFormatted.replace(" ", "").replace("-", "");
        String normalizedDummyNo = dummyOtpPhoneNumber.replace(" ", "").replace("-", "");
        return normalizedRequestNo.equals(normalizedDummyNo);
    }
}
