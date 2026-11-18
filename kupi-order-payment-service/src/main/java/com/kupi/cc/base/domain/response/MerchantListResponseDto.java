package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MerchantListResponseDto extends ResponseDto{

    private BasicBusinessResponseDto business;
}