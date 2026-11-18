package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Category;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class FavouriteCategoryListResponseDto extends PageResponseDto {

    private final List<CategoryResponseDto> categories;

    public FavouriteCategoryListResponseDto(Page<Category> categoryPage, String timeZone) {
        super(categoryPage);
        this.categories = createFavouriteCategoryResponseList(categoryPage, timeZone);
    }

    public FavouriteCategoryListResponseDto(Page<Category> categoryPage, Set<String> favouriteCategoryIds,
                                            String timeZone) {
        super(categoryPage);
        this.categories = checkIfFavouriteCategory(categoryPage, favouriteCategoryIds, timeZone);
    }

    /**
     * This method creates favourite category response list.
     *
     * @param categoryPage categoryPage
     * @param timeZone     timeZone
     * @return category list.
     */
    private List<CategoryResponseDto> createFavouriteCategoryResponseList(Page<Category> categoryPage,
                                                                          String timeZone) {
        return categoryPage.toList()
                .stream()
                .map(category -> new FavouriteCategoryResponseDto(category, timeZone, true))
                .collect(Collectors.toList());
    }

    /**
     * This method checks if category is in favourite list.
     *
     * @param categoryPage         categoryPage
     * @param favouriteCategoryIds favouriteCategoryIds
     * @param timeZone             timeZone
     * @return category list.
     */
    private List<CategoryResponseDto> checkIfFavouriteCategory(Page<Category> categoryPage,
                                                               Set<String> favouriteCategoryIds, String timeZone) {
        return categoryPage.toList()
                .stream()
                .map(category -> new FavouriteCategoryResponseDto(category, timeZone,
                        favouriteCategoryIds.contains(category.getId())))
                .collect(Collectors.toList());
    }
}
