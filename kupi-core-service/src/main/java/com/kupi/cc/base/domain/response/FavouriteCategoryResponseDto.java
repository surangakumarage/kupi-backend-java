package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Category;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavouriteCategoryResponseDto extends CategoryResponseDto{

    private boolean isFavourite;

    public FavouriteCategoryResponseDto(Category category, String timeZone, boolean isFavourite) {
        super(category, timeZone);
        this.isFavourite = isFavourite;
    }
}
