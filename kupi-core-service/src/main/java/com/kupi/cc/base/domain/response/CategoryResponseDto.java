package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Category;
import com.kupi.cc.base.domain.entity.PromoType;
import com.kupi.cc.base.enums.ActiveStatusType;
import com.kupi.cc.base.enums.CategoryType;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class CategoryResponseDto extends ResponseDto {

    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private CategoryType categoryType;
    private Long startDate;
    private Long expiryDate;
    private DateResponseDto createdAt;
    private DateResponseDto updatedAt;
    private boolean isPopular;
    private ActiveStatusType status;
    private List<PromoTypeResponseDto> promoTypes;

    public CategoryResponseDto(Category category, String timeZone) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.imageUrl = category.getImageUrl();
        this.categoryType = category.getCategoryType();
        this.startDate = category.getStartDate();
        this.expiryDate = category.getExpiryDate();
        this.createdAt = new DateResponseDto(category.getCreatedAt(), timeZone, new Date(category.getCreatedAt()));
        this.updatedAt = new DateResponseDto(category.getUpdatedAt(), timeZone, new Date(category.getUpdatedAt()));
        this.isPopular = category.isPopular();
        this.status = (categoryType == CategoryType.SEASONAL && expiryDate <= System.currentTimeMillis()) ?
                ActiveStatusType.EXPIRED : ActiveStatusType.ACTIVE;
        this.promoTypes = category.getPromoTypes() != null ? 
                category.getPromoTypes().stream()
                        .filter(PromoType::isActive)
                        .map(promoType -> new PromoTypeResponseDto(promoType, timeZone))
                        .collect(Collectors.toList()) : null;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
