package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.AuthUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Merchant response Dto
 */

@Setter
@Getter
@NoArgsConstructor
public class MerchantBusinessResponseDto extends BasicMerchantBusinessResponseDto {

    private DateResponseDto joinedOn;
    private String email;

    public MerchantBusinessResponseDto(String merchantId, AuthUser authUser) {
        super(merchantId, authUser);
    }

}