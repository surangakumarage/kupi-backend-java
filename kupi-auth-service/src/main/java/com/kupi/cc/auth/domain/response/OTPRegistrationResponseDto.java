package com.kupi.cc.auth.domain.response;

import com.kupi.cc.auth.domain.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

/**
 * OTP Registration Response DTO - includes user data and auth token
 */
@Getter
@Setter
public class OTPRegistrationResponseDto implements ResponseDto {

    private UserResponseDto user;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";

    public OTPRegistrationResponseDto(User user, String timeZone, OAuth2AccessToken oAuth2AccessToken) {
        this.user = new UserResponseDto(user, timeZone);
        this.accessToken = oAuth2AccessToken.getValue();
        if (oAuth2AccessToken.getRefreshToken() != null) {
            this.refreshToken = oAuth2AccessToken.getRefreshToken().getValue();
        }
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
