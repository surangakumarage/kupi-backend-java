package com.kupi.cc.auth.controller;

import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.domain.request.VerifyEmailRequest;
import com.kupi.cc.auth.domain.request.VerifyMobileRequest;
import com.kupi.cc.auth.enums.ErrorResponseStatusType;
import com.kupi.cc.auth.enums.SuccessResponseStatusType;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.exception.InvalidUserException;
import com.kupi.cc.auth.service.UserVerificationService;
import com.kupi.cc.auth.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping(path = "/api/v1/users/verification")
public class InternalUserVerificationController extends Controller {

    private final UserVerificationService userVerificationService;

    public InternalUserVerificationController(Translator translator, UserVerificationService userVerificationService) {
        super(translator);
        this.userVerificationService = userVerificationService;
    }

    @PostMapping(path = "/email", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> verifyEmail(@RequestBody VerifyEmailRequest verifyEmailRequest) {
        try {
            if (verifyEmailRequest == null || verifyEmailRequest
                    .getEmail() == null || verifyEmailRequest.getEmail().isBlank()) {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            userVerificationService.markEmailVerified(verifyEmailRequest.getEmail().trim());
            return getSuccessResponse(SuccessResponseStatusType.EMAIL_VERIFIED, null);
        } catch (InvalidUserException e) {
            return getBadRequestError(ErrorResponseStatusType.NO_USER);
        } catch (AuthServiceException e) {
            log.error("Email verification update failed for {}", verifyEmailRequest != null ?
                    verifyEmailRequest.getEmail() : "null", e);
            return getInternalServerError();
        } catch (Exception e) {
            log.error("Unexpected error in verifyEmail", e);
            return getInternalServerError();
        }
    }

    @PostMapping(path = "/mobile", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> verifyMobile(@RequestBody VerifyMobileRequest mobileRequest) {
        try {
            if (mobileRequest == null) {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            userVerificationService.markMobileVerified(mobileRequest.getMobileNo().getNo());
            return getSuccessResponse(SuccessResponseStatusType.MOBILE_VERIFIED, null);
        } catch (InvalidUserException e) {
            return getBadRequestError(ErrorResponseStatusType.NO_USER);
        } catch (AuthServiceException e) {
            log.error("Mobile verification update failed for {}", mobileRequest != null ?
                    mobileRequest.getMobileNo().getNo() : "null", e);

            return getInternalServerError();
        } catch (Exception e) {
            log.error("Unexpected error in verifyMobile", e);
            return getInternalServerError();
        }
    }
}
