package com.kupi.cc.location.domain.request;

import com.kupi.cc.location.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Basic Deal RequestDto
 */
@Getter
@Setter
@AllArgsConstructor
public class BasicDealRequestDto extends RequestDto {

    private String id;
    private String title;
    private String subTitle;
    private String imageUrl;
    private UserType dealSource;


    @Override
    public String toLogJson() {
        return toJson();
    }
}