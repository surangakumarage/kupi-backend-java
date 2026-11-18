package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.PromoType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromoTypeRepository extends JpaRepository<PromoType, String> {

    /**
     * Check if promo type exists by name
     *
     * @param name promo type name
     * @return true if exists, false otherwise
     */
    boolean existsByNameAndIsActiveTrue(String name);

    /**
     * Find all promo types by category ID
     *
     * @param categoryId category ID
     * @return list of promo types
     */
    java.util.List<PromoType> findByCategoryIdAndIsActiveTrue(String categoryId);

    /**
     * Check if promo type exists by name and category
     *
     * @param name promo type name
     * @param categoryId category ID
     * @return true if exists, false otherwise
     */
    boolean existsByNameAndCategoryIdAndIsActiveTrue(String name, String categoryId);

}
