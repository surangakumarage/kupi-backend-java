package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.PromoType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Setter
@Getter
@NoArgsConstructor
public class PromoTypeResponseDto extends ResponseDto {

    private String id;
    private String name;
    private String categoryId;
    private String categoryName;
    private DateResponseDto createdAt;
    private DateResponseDto updatedAt;
    private boolean isActive;

    public PromoTypeResponseDto(PromoType promoType, String timeZone) {
        this.id = promoType.getId();
        this.name = promoType.getName();
        this.categoryId = promoType.getCategory().getId();
        this.categoryName = promoType.getCategory().getName();
        this.createdAt = new DateResponseDto(promoType.getCreatedAt(), timeZone, new Date(promoType.getCreatedAt()));
        this.updatedAt = new DateResponseDto(promoType.getUpdatedAt(), timeZone, new Date(promoType.getUpdatedAt()));
        this.isActive = promoType.isActive();
    }

}
