package com.kupi.cc.auth.domain.response;

import com.kupi.cc.auth.domain.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

/**
 * OTP Login Response DTO - includes user detail data and auth token
 */
@Getter
@Setter
public class OTPLoginResponseDto implements ResponseDto {

    private UserDetailResponseDto user;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";

    public OTPLoginResponseDto(User user, OAuth2AccessToken oAuth2AccessToken) {
        this.user = new UserDetailResponseDto(user);
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
