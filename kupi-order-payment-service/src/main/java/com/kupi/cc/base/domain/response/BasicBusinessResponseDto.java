package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

/**
 * BasicBusiness ResponseDto
 */

@Getter
@Setter
public class BasicBusinessResponseDto extends ResponseDto{
    private String id;
    private String name;
    private String imageUrl;
}