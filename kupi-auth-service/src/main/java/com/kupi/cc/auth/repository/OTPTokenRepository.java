package com.kupi.cc.auth.repository;

import com.kupi.cc.auth.domain.entity.OTPToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OTPTokenRepository extends JpaRepository<OTPToken, String> {

    /**
     * Find OTP token by token value
     *
     * @param token OTP token value
     * @return OTPToken
     */
    OTPToken findByToken(String token);

    /**
     * Find OTP token by mobile number
     *
     * @param mobileNo mobile number in format +countryCode-localNumber
     * @return OTPToken
     */
    OTPToken findByMobileNo(String mobileNo);

    /**
     * Delete OTP token by mobile number
     *
     * @param mobileNo mobile number
     */
    void deleteByMobileNo(String mobileNo);
}
