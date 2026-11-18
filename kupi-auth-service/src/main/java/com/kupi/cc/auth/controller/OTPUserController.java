package com.kupi.cc.auth.controller;

import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.domain.entity.Role;
import com.kupi.cc.auth.domain.entity.User;
import com.kupi.cc.auth.domain.request.*;
import com.kupi.cc.auth.domain.response.OTPLoginResponseDto;
import com.kupi.cc.auth.domain.response.OTPRegistrationResponseDto;
import com.kupi.cc.auth.enums.ErrorResponseStatusType;
import com.kupi.cc.auth.enums.SuccessResponseStatusType;
import com.kupi.cc.auth.exception.*;
import com.kupi.cc.auth.service.OTPTokenService;
import com.kupi.cc.auth.service.UserService;
import com.kupi.cc.auth.util.Validator;
import com.kupi.cc.auth.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * OTP User Controller - Handles OTP-based user registration and login
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/otp/users")
public class OTPUserController extends Controller {

    private static final String INVALID_OTP_TOKEN = "Invalid OTP token";
    private final UserService userService;
    private final OTPTokenService otpTokenService;
    private final Validator validator;
    private final DefaultTokenServices defaultTokenServices;
    private final String clientId;

    @Autowired
    public OTPUserController(UserService userService, OTPTokenService otpTokenService,
                             Validator validator,
                             @Qualifier("tokenServices") DefaultTokenServices defaultTokenServices,
                             @Value("${application.client-id}") String clientId,
                             Translator translator) {
        super(translator);
        this.userService = userService;
        this.otpTokenService = otpTokenService;
        this.validator = validator;
        this.defaultTokenServices = defaultTokenServices;
        this.clientId = clientId;
    }

    /**
     * Request OTP for mobile number.
     * Can be used for both new user registration and existing user login.
     *
     * @param timeZone user's time zone
     * @param mobileNoRequestDto mobile number
     * @return success/error response
     */
    @PostMapping(path = "/request-otp", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> requestOTP(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                       @RequestBody MobileNoRequestDto mobileNoRequestDto) {
        try {
            if (mobileNoRequestDto.isRequiredAvailable()) {
                if (validator.isValidMobileNoWithCountryCode(mobileNoRequestDto.getNo())) {
                    otpTokenService.requestOTP(mobileNoRequestDto);
                    log.debug("OTP requested for mobile: {}", mobileNoRequestDto.getNo());
                    return getSuccessResponse(SuccessResponseStatusType.OTP_SENT, null, "OTP has been sent to your mobile number.");
                } else {
                    return getBadRequestError(ErrorResponseStatusType.INVALID_MOBILE_NO);
                }
            } else {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (Exception e) {
            log.error("Requesting OTP failed: {}", e.getMessage(), e);
            return getInternalServerError();
        }
    }

    /**
     * Verify OTP and register new user.
     *
     * @param timeZone user's time zone
     * @param otpRegistrationDto OTP registration request with user details
     * @return user data with auth token, or error response
     */
    @PostMapping(path = "/verify-and-register", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> verifyAndRegister(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                              @RequestBody OTPUserRegistrationDto otpRegistrationDto) {
        try {
            if (otpRegistrationDto.isRequiredAvailable()) {
                // Validate OTP token
                try {
                    otpTokenService.verifyOTP(otpRegistrationDto.getToken());
                } catch (InvalidTokenException e) {
                    log.warn("Invalid OTP token provided for registration", e);
                    return getBadRequestError(ErrorResponseStatusType.INVALID_OTP);
                }

                // Validate mobile number format
                if (!validator.isValidMobileNoWithCountryCode(otpRegistrationDto.getMobileNo().getNo())) {
                    return getBadRequestError(ErrorResponseStatusType.INVALID_MOBILE_NO);
                }

                // Validate password if provided (optional for OTP registration)
                if (otpRegistrationDto.getPassword() != null &&
                    !otpRegistrationDto.getPassword().trim().isEmpty() &&
                    !validator.isValidPassword(otpRegistrationDto.getPassword())) {
                    return getBadRequestError(ErrorResponseStatusType.INVALID_PASSWORD);
                }

                // Validate email if provided
                if (otpRegistrationDto.getEmail() != null &&
                    !otpRegistrationDto.getEmail().trim().isEmpty() &&
                    !validator.isValidEmail(otpRegistrationDto.getEmail())) {
                    return getBadRequestError(ErrorResponseStatusType.INVALID_EMAIL);
                }

                // Check if mobile or email already exists
                if (userService.isMobileNoExist(otpRegistrationDto.getMobileNo().getNo())) {
                    return getBadRequestError(ErrorResponseStatusType.EXISTING_MOBILE_NO);
                }

                if (otpRegistrationDto.getEmail() != null &&
                    !otpRegistrationDto.getEmail().trim().isEmpty() &&
                    userService.isEmailExist(otpRegistrationDto.getEmail())) {
                    return getBadRequestError(ErrorResponseStatusType.EXISTING_EMAIL);
                }

                // Create user
                User createdUser = userService.createOTPUser(otpRegistrationDto, timeZone);

                // Generate auth token
                OAuth2AccessToken accessToken = generateAuthToken(createdUser);

                // Delete OTP token after successful registration
                otpTokenService.deleteOTP(otpRegistrationDto.getToken());

                OTPRegistrationResponseDto responseDto = new OTPRegistrationResponseDto(createdUser, timeZone, accessToken);
                log.debug("User registered via OTP: {}", createdUser.getId());
                return getSuccessResponse(SuccessResponseStatusType.CREATE_USER, responseDto);

            } else {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (AuthServiceException e) {
            log.error("Registering user via OTP failed: {}", e.getMessage(), e);
            return getInternalServerError();
        }
    }

    /**
     * Verify OTP and login existing user.
     *
     * @param timeZone user's time zone
     * @param otpLoginDto OTP login request with mobile number
     * @return user data with auth token, or error response
     */
    @PostMapping(path = "/verify-and-login", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> verifyAndLogin(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                           @RequestBody OTPLoginDto otpLoginDto) {
        try {
            if (otpLoginDto.isRequiredAvailable()) {
                // Validate OTP token
                try {
                    otpTokenService.verifyOTP(otpLoginDto.getToken());
                } catch (InvalidTokenException e) {
                    log.warn("Invalid OTP token provided for login", e);
                    return getBadRequestError(ErrorResponseStatusType.INVALID_OTP);
                }

                // Validate mobile number format
                if (!validator.isValidMobileNoWithCountryCode(otpLoginDto.getMobileNo().getNo())) {
                    return getBadRequestError(ErrorResponseStatusType.INVALID_MOBILE_NO);
                }

                // Check if user exists
                try {
                    User user = userService.getUserByMobileNo(otpLoginDto.getMobileNo().getNo());

                    // Generate auth token
                    OAuth2AccessToken accessToken = generateAuthToken(user);

                    // Delete OTP token after successful login
                    otpTokenService.deleteOTP(otpLoginDto.getToken());

                    OTPLoginResponseDto responseDto = new OTPLoginResponseDto(user, accessToken);
                    log.debug("User logged in via OTP: {}", user.getId());
                    return getSuccessResponse(SuccessResponseStatusType.LOGGED_IN, responseDto);
                } catch (InvalidUserException e) {
                    log.warn("User not found for mobile: {}", otpLoginDto.getMobileNo().getNo(), e);
                    return getBadRequestError(ErrorResponseStatusType.NO_USER);
                }

            } else {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (AuthServiceException e) {
            log.error("Login via OTP failed: {}", e.getMessage(), e);
            return getInternalServerError();
        }
    }

    /**
     * Generate OAuth2 auth token for user.
     *
     * @param user user entity
     * @return OAuth2AccessToken
     */
    private OAuth2AccessToken generateAuthToken(User user) {
        try {
            // Create granted authorities from user's role and permissions
            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            Role role = user.getRole();
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
            if (role.getPermissions() != null) {
                role.getPermissions().forEach(permission ->
                        grantedAuthorities.add(new SimpleGrantedAuthority(permission.getName())));
            }

            // Create OAuth2Request and authentication
            Set<String> scopes = new HashSet<>();
            Set<String> resourceIdSet = new HashSet<>();
            OAuth2Request authorizationRequest = new OAuth2Request(null, clientId, grantedAuthorities,
                    true, scopes, resourceIdSet, "", null, null);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(user.getId(), null, grantedAuthorities);

            OAuth2Authentication authenticationRequest = new OAuth2Authentication(authorizationRequest, authenticationToken);
            authenticationRequest.setAuthenticated(true);

            // Create and return access token
            return defaultTokenServices.createAccessToken(authenticationRequest);
        } catch (Exception e) {
            log.error("Failed to generate auth token for OTP user: {}", user.getId(), e);
            throw new AuthServiceException("Failed to generate auth token", e);
        }
    }
}
