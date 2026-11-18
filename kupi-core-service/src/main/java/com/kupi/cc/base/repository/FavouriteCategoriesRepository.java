package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.FavouriteCategories;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Favourite categories repository
 */
public interface FavouriteCategoriesRepository extends JpaRepository<FavouriteCategories, String> {

}
