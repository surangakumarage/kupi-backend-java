package com.kupi.cc.base.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BasicUserResponseDto extends ResponseDto {

    private String id;
    private String name;
    private String imageUrl;
    private MobileNoResponseDto mobileNo;

    public BasicUserResponseDto(BasicMerchantBusinessResponseDto merchantBusinessResponseDto) {
        this.id = merchantBusinessResponseDto.getId();
        this.name = merchantBusinessResponseDto.getName();
        this.imageUrl = merchantBusinessResponseDto.getImageUrl();
    }

    public BasicUserResponseDto(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }
}
