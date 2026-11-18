package com.kupi.cc.location.domain.entity;

import com.kupi.cc.location.domain.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Auth User
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser implements BaseDto {

    private String fullName;
    private String email;
    private String imageUrl;
    private String language;
    private boolean registeredUser;
    private boolean isActive;
    private String userType;

    /**
     * This method converts object to json string for logging purpose.
     * PII data should be obfuscated.
     *
     * @return json string
     */
    @Override
    public String toLogJson() {return toJson();
    }
}