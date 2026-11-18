package com.kupi.cc.auth.domain.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "otp_token")
@NoArgsConstructor
public class OTPToken {

    @Transient
    private static final String OTP_TOKEN_PREFIX = "otp-";
    private static final String TOKEN_FORMATTER = "%06d";
    private static final int TOKEN_EXPIRATION_TIME = 2;
    private static final int BOUND_LIMIT = 999999;

    @Id
    private String id;
    @Column(nullable = false, unique = true)
    private String token;
    @Column(nullable = false)
    private String mobileNo;
    @Column(name = "user_id")
    private String userId;
    private Date createdAt;
    private Date expiryDate;

    public OTPToken(String mobileNo) {
        this.id = OTP_TOKEN_PREFIX + UUID.randomUUID();
        SecureRandom secureRandom = new SecureRandom();
        int randomDigit = secureRandom.nextInt(BOUND_LIMIT);
        this.token = String.format(TOKEN_FORMATTER, randomDigit);
        this.mobileNo = mobileNo;
        this.createdAt = new Date();
        setExpiration(TOKEN_EXPIRATION_TIME);
    }

    public void setExpiration(int minutes) {
        final Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, minutes);
        this.expiryDate = now.getTime();
    }
}
