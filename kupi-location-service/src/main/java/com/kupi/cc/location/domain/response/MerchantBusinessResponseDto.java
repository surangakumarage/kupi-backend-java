package com.kupi.cc.location.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Merchant response Dto
 */

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantBusinessResponseDto extends BusinessResponseDto {

    private DateResponseDto joinedOn;
}