package com.kupi.cc.auth.service;

import com.kupi.cc.auth.domain.entity.User;
import com.kupi.cc.auth.enums.ErrorResponseStatusType;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.exception.InvalidUserException;
import com.kupi.cc.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserVerificationService {
    private static final String FAILED_TO_VERIFY_EMAIL = "Failed to verify email";
    private static final String FAILED_TO_VERIFY_MOBILE = "Failed to verify mobile";

    private final UserRepository userRepository;

    public UserVerificationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public void markEmailVerified(String email) {
        try {
            if (email == null || email.isBlank()) {
                throw new InvalidUserException(ErrorResponseStatusType.NO_USER.getMessage());
            }
            User user = userRepository.findByEmail(email.trim());
            if (user == null) {
                throw new InvalidUserException(ErrorResponseStatusType.NO_USER.getMessage());
            }

            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                user.setUpdatedAt(new java.util.Date());
                userRepository.save(user);
            }
            log.debug("Email verified successfully for {}", email);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_VERIFY_EMAIL, e);
        }
    }

    public void markMobileVerified(String mobile) {
        try {
            if (mobile == null || mobile.isBlank()) {
                throw new InvalidUserException(ErrorResponseStatusType.NO_USER.getMessage());
            }
            log.info("Mobile verification for {}", mobile);
            User user = userRepository.findByMobileNo(mobile.trim());
            if (user == null) {
                throw new InvalidUserException(ErrorResponseStatusType.NO_USER.getMessage());
            }

            if (!user.isMobileVerified()) {
                user.setMobileVerified(true);
                user.setUpdatedAt(new java.util.Date());
                userRepository.save(user);
            }
            log.debug("Mobile verified successfully for {}", mobile);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_VERIFY_MOBILE, e);
        }
    }

}
