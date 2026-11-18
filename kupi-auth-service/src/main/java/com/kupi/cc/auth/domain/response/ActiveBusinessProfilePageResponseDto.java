package com.kupi.cc.auth.domain.response;

import com.kupi.cc.auth.domain.entity.Business;
import com.kupi.cc.auth.enums.RoleType;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Active Business Profile Page ResponseDto
 */
@Getter
public class ActiveBusinessProfilePageResponseDto extends PageResponseDto {

    private final List<BasicMerchantResponseDto> merchants = new ArrayList<>();

    public ActiveBusinessProfilePageResponseDto(Page<Business> businessPage, String timeZone, RoleType roleType) {
        super(businessPage);
        List<Business> businessList = businessPage.toList();
        businessList.forEach(business -> merchants.add(new BasicMerchantResponseDto(business, timeZone, roleType)));
    }
}
