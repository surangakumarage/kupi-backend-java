package com.kupi.cc.base.domain.response;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Merchant list response DTO from auth service
 * This matches the structure returned by auth service's MerchantListResponseDto
 */
@Getter
@Setter
@NoArgsConstructor
public class AuthMerchantListResponseDto extends ResponseDto {

    private AuthBasicUserResponseDto profile;
    private AuthBasicBusinessResponseDto business;
}
