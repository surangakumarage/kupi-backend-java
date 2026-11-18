package com.kupi.otp.controller;

import com.kupi.otp.configuration.Translator;
import com.kupi.otp.domain.request.OtpRequestDto;
import com.kupi.otp.domain.request.OtpVerifyRequestDto;
import com.kupi.otp.enums.ErrorResponseStatusType;
import com.kupi.otp.enums.SuccessResponseStatusType;
import com.kupi.otp.exception.InvalidOtpKeyException;
import com.kupi.otp.exception.OTPServiceException;
import com.kupi.otp.service.AuthService;
import com.kupi.otp.service.EmailOtpService;
import com.kupi.otp.service.RateLimitService;
import com.kupi.otp.service.SmsOtpService;
import com.kupi.otp.util.OtpGenerator;
import com.kupi.otp.util.Validator;
import com.kupi.otp.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OtpController
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/otp")
public class OtpController extends Controller {

    private final SmsOtpService smsOtpService;
    private final EmailOtpService emailOtpService;
    private final Validator validator;
    private final OtpGenerator otpGenerator;
    private final AuthService authService;
    private final RateLimitService rateLimitService;

    @Autowired
    public OtpController(SmsOtpService smsOtpService, EmailOtpService emailOtpService, Validator validator,
                         OtpGenerator otpGenerator, Translator translator, AuthService authService,
                         RateLimitService rateLimitService) {
        super(translator);
        this.smsOtpService = smsOtpService;
        this.emailOtpService = emailOtpService;
        this.validator = validator;
        this.otpGenerator = otpGenerator;
        this.authService = authService;
        this.rateLimitService = rateLimitService;
    }

    /**
     * Generate the Otp and send via sms/email
     *
     * @param otpRequestDto otpRequestDto
     * @return ResponseEntity
     */
    @PostMapping(value = "/generate", consumes = APPLICATION_JSON,
            produces = APPLICATION_JSON)
    public ResponseEntity<ResponseWrapper> generateOtp(@RequestBody OtpRequestDto otpRequestDto) {
        try {
            if (otpRequestDto.getMobileNo() != null && otpRequestDto.getEmail() != null) {
                return getBadRequestError(ErrorResponseStatusType.MULTIPLE_TYPES_NOT_SUPPORTED);
            }
            if (otpRequestDto.isRequiredAvailable()) {
                log.debug("Inputs for generate otp : {} ", otpRequestDto.toLogJson());
                if (otpRequestDto.getMobileNo() != null) {
                    return generateSmsResponseEntity(otpRequestDto);
                } else {
                    return generateEmailResponseEntity(otpRequestDto);
                }
            } else {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (OTPServiceException e) {
            log.error("OTP generation was failed for: {} ", otpRequestDto.toLogJson(), e);
            return getInternalServerError();
        }

    }

    /**
     * Verify the Otp sent by user
     *
     * @param otpVerifyRequestDto otpVerifyRequestDto
     * @return ResponseEntity
     */
    @PostMapping(value = "/verify", consumes = APPLICATION_JSON,
            produces = APPLICATION_JSON)
    public ResponseEntity<ResponseWrapper> verifyOtp(@RequestBody OtpVerifyRequestDto otpVerifyRequestDto) {
        try {
            if (otpVerifyRequestDto.getMobileNo() != null && otpVerifyRequestDto.getEmail() != null) {
                return getBadRequestError(ErrorResponseStatusType.MULTIPLE_TYPES_NOT_SUPPORTED);
            }
            if (otpVerifyRequestDto.isRequiredAvailable()) {
                log.debug("Verify Otp inputs: {} ", otpVerifyRequestDto.toJson());
                if (otpVerifyRequestDto.getMobileNo() != null && otpVerifyRequestDto.getOtp() != null) {
                    return smsVerifyResponseEntity(otpVerifyRequestDto);
                } else {
                    return emailVerifyResponseEntity(otpVerifyRequestDto);
                }
            } else {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (OTPServiceException e) {
            log.error("OTP Verification was failed for: {} ", otpVerifyRequestDto.toLogJson(), e);
            return getInternalServerError();
        }

    }

    /**
     * Save the email and otp to cache and send email
     *
     * @param otpRequestDto otp request parameters
     * @return ResponseEntity
     */
    private ResponseEntity<ResponseWrapper> generateEmailResponseEntity(OtpRequestDto otpRequestDto) {
        if (validator.isValidEmail(otpRequestDto.getEmail().trim())) {
            String email = otpRequestDto.getEmail().trim();
            if (!rateLimitService.isEmailOtpGenerationAllowed(email)) {
                log.warn("OTP generation rate limit exceeded for email: {}", email);
                return getBadRequestError(ErrorResponseStatusType.OTP_GENERATION_RATE_LIMIT_EXCEEDED);
            }
            int otp = otpGenerator.generateOtp();
            emailOtpService.sendOtp(email, otp);
            log.debug("Sent otp: {} for email: {}", otp, email);
            return getSuccessResponse(SuccessResponseStatusType.SENT_OTP);
        } else {
            return getBadRequestError(ErrorResponseStatusType.INVALID_EMAIL);
        }
    }

    /**
     * Save the mobileNo to cache and send sms
     *
     * @return ResponseEntity
     */
    private ResponseEntity<ResponseWrapper> generateSmsResponseEntity(OtpRequestDto otpRequestDto) {
        if (validator.isValidMobileNo(otpRequestDto.getMobileNo().getNo())) {
            String mobileNo = otpRequestDto.getMobileNo().getNo().replace("-", "");
            if (!rateLimitService.isMobileOtpGenerationAllowed(mobileNo)) {
                log.warn("OTP generation rate limit exceeded for mobile: {}", mobileNo);
                return getBadRequestError(ErrorResponseStatusType.OTP_GENERATION_RATE_LIMIT_EXCEEDED);
            }
            int otp = otpGenerator.generateOtp();
            smsOtpService.sendOtp(otpRequestDto.getMobileNo(), mobileNo, otp);
            log.debug("Sent otp: {} for mobile: {}", otp, mobileNo);
            return getSuccessResponse(SuccessResponseStatusType.SENT_OTP);
        } else {
            log.error("Invalid mobile no: {}", otpRequestDto.getMobileNo().getNo());
            return getBadRequestError(ErrorResponseStatusType.INVALID_MOBILE_NO);
        }
    }

    /**
     * Read the cache and get the saved otp for email
     *
     * @param otpVerifyRequestDto otpVerifyRequestDto
     * @return ResponseEntity
     */
    private ResponseEntity<ResponseWrapper> emailVerifyResponseEntity(OtpVerifyRequestDto otpVerifyRequestDto) {
        if (validator.isValidEmail(otpVerifyRequestDto.getEmail())) {
            if (validator.isValidOtp(otpVerifyRequestDto.getOtp())) {
                return verifyEmailOtp(otpVerifyRequestDto);
            } else {
                return getBadRequestError(ErrorResponseStatusType.INVALID_OTP);
            }
        } else {
            return getBadRequestError(ErrorResponseStatusType.INVALID_EMAIL);
        }
    }

    /**
     * Call to emailOtpService and verify the otp
     *
     * @param otpVerifyRequestDto otpVerifyRequestDto
     * @return ResponseEntity
     */
    private ResponseEntity<ResponseWrapper> verifyEmailOtp(OtpVerifyRequestDto otpVerifyRequestDto) {
        try {
            if (emailOtpService.verifyOtp(otpVerifyRequestDto.getEmail(), otpVerifyRequestDto.getOtp())) {
                String oldEmail = otpVerifyRequestDto.getOldEmail();
//                authService.notifyEmailVerified(oldEmail != null ? oldEmail.trim() : otpVerifyRequestDto.getEmail());
                return getSuccessResponse(SuccessResponseStatusType.VERIFIED_OTP);
            } else {
                return getBadRequestError(ErrorResponseStatusType.INVALID_OTP);
            }
        } catch (InvalidOtpKeyException e) {
            log.error("Invalid mobile number or expired otp {} ", otpVerifyRequestDto.toLogJson(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_EMAIL_OR_EXPIRED_OTP);
        }
    }

    /**
     * Read the cache and get the saved otp for sms
     *
     * @param otpVerifyRequestDto otpVerifyRequestDto
     * @return ResponseEntity
     */
    private ResponseEntity<ResponseWrapper> smsVerifyResponseEntity(OtpVerifyRequestDto otpVerifyRequestDto) {
        if (validator.isValidMobileNo(otpVerifyRequestDto.getMobileNo().getNo())) {
            if (validator.isValidOtp(otpVerifyRequestDto.getOtp())) {
                return verifySmsOtp(otpVerifyRequestDto);
            } else {
                return getBadRequestError(ErrorResponseStatusType.INVALID_OTP);
            }
        } else {
            return getBadRequestError(ErrorResponseStatusType.INVALID_MOBILE_NO);
        }
    }

    /**
     * Call to smsOtpService and verify the otp
     *
     * @param otpVerifyRequestDto otpVerifyRequestDto
     * @return ResponseEntity
     */
    private ResponseEntity<ResponseWrapper> verifySmsOtp(OtpVerifyRequestDto otpVerifyRequestDto) {
        try {
            String mobileNo = otpVerifyRequestDto.getMobileNo().getNo().replace("-", "");

            if (smsOtpService.verifyOtp(mobileNo, otpVerifyRequestDto.getOtp())) {
                if (otpVerifyRequestDto.isMobileUpdate()){
                    authService.notifyMobileVerified(otpVerifyRequestDto.getMobileNo());
                }
                return getSuccessResponse(SuccessResponseStatusType.VERIFIED_OTP);
            } else {
                return getBadRequestError(ErrorResponseStatusType.INVALID_OTP);
            }
        } catch (InvalidOtpKeyException e) {
            log.error("Invalid mobile number or expired otp {} ", otpVerifyRequestDto.toLogJson(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_MOBILE_NUMBER_OR_EXPIRED_OTP);
        }
    }
}
