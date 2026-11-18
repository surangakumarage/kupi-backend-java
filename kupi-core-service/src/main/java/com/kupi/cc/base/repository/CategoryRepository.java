package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.Category;
import com.kupi.cc.base.enums.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface CategoryRepository extends JpaRepository<Category, String> {

    String ACTIVE_CATEGORIES = " c.categoryType = 'NORMAL' OR " +
            "(c.categoryType = 'SEASONAL' AND c.startDate <= :currentTimeStamp AND c.expiryDate >= :currentTimeStamp) ";

    String EXPIRED_CATEGORIES = " (c.categoryType = 'SEASONAL' AND c.expiryDate <= :currentTimeStamp) ";

    /**
     * This method is used to get popular active categories.
     *
     * @param currentTimeStamp currentTimeStamp
     * @param pageable         pageable
     * @return active popular category page.
     */
    @Query(value = "SELECT c FROM Category c WHERE c.isPopular = true AND ( " + ACTIVE_CATEGORIES + " )")
    Page<Category> getActivePopularCategories(long currentTimeStamp, Pageable pageable);

    /**
     * This method returns categories search by name
     *
     * @param pageable pageable
     * @param name     category name
     * @return category page
     */
    Page<Category> findAllByNameContaining(Pageable pageable, String name);

    /**
     * This method is used get active categories by ids and search term.
     *
     * @param ids              category ids
     * @param searchTerm       searchTerm
     * @param currentTimeStamp currentTimeStamp
     * @param pageable         pageable
     * @return page of active categories
     */
    @Query(value = "SELECT c FROM Category c WHERE c.id in :ids AND c.name like %:searchTerm% AND ( " +
            ACTIVE_CATEGORIES + " )")
    Page<Category> getActiveCategoriesByIdsAndSearchTerm(Set<String> ids, String searchTerm, long currentTimeStamp,
                                                         Pageable pageable);

    /**
     * This method is used get active categories by ids.
     *
     * @param ids              category ids
     * @param currentTimeStamp currentTimeStamp
     * @param pageable         pageable
     * @return page of active categories
     */
    @Query(value = "SELECT c FROM Category c WHERE c.id in :ids AND ( " + ACTIVE_CATEGORIES + " )")
    Page<Category> getActiveCategoriesByIds(Set<String> ids, long currentTimeStamp, Pageable pageable);

    /**
     * This method is used get categories by ids and search term.
     *
     * @param ids        category ids
     * @param searchTerm searchTerm
     * @param pageable   pageable
     * @return page of active categories
     */
    @Query(value = "SELECT c FROM Category c WHERE c.id in :ids AND c.name like %:searchTerm%")
    Page<Category> getCategoriesByIdsAndSearchTerm(Set<String> ids, String searchTerm, Pageable pageable);

    /**
     * This method returns true when category name exist in database.
     *
     * @param name name
     * @return true/ false
     */
    boolean existsByName(String name);

    /**
     * This method returns true when updating name exist with other category.
     *
     * @param name category name
     * @param id   category id
     * @return true/ false
     */
    boolean existsByNameAndIdNot(String name, String id);

    /**
     * This method returns a category id set.
     *
     * @param categoryId category id
     * @return String set
     */
    @Query(value = "select c.related_category_id from category_relations c where c.category_id=:categoryId", nativeQuery = true)
    Set<String> getRelatedCategoriesById(String categoryId);

    /**
     * This method returns true when category is a relatedCategory for another category.
     *
     * @param category category
     * @return true/ false
     */
    boolean existsCategoryByRelatedCategories(Category category);

    /**
     * This method is used to get total number of active categories.
     *
     * @param currentTimeInMillis currentTimeInMillis
     * @return total number of active categories.
     */
    int countByExpiryDateIsNullOrExpiryDateGreaterThan(Long currentTimeInMillis);

    /***
     * This method is used to get total number of category.
     * @param ccurrentTimeInMillis ccurrentTimeInMillis
     * @return total number of new category
     */
    long countByCreatedAtGreaterThanEqual(long ccurrentTimeInMillis);

    /**
     * This method is used to get categories by its type.
     *
     * @param categoryType SEASONAL/NORMAL
     * @param pageable     pageable
     * @return page of categories.
     */
    Page<Category> findAllByCategoryType(CategoryType categoryType, Pageable pageable);

    /**
     * This method is used to search categories by its type and name.
     *
     * @param categoryType SEASONAL/NORMAL
     * @param searchTerm   searchTerm
     * @param pageable     pageable
     * @return page of categories.
     */
    Page<Category> findAllByCategoryTypeAndNameContaining(CategoryType categoryType, String searchTerm, Pageable pageable);

    /**
     * This method is used to get all categories by its type excluding given categoryId.
     *
     * @param categoryType SEASONAL/NORMAL
     * @param categoryId   categoryId
     * @param pageable     pageable
     * @return page of categories.
     */
    Page<Category> findAllByCategoryTypeAndIdNotContaining(CategoryType categoryType, String categoryId, Pageable pageable);

    /**
     * This method is used to search categories by its type and name excluding given categoryId.
     *
     * @param categoryType SEASONAL/NORMAL
     * @param searchTerm   searchTerm
     * @param categoryId   categoryId
     * @param pageable     pageable
     * @return page of categories.
     */
    Page<Category> findAllByCategoryTypeAndNameContainingAndIdNotContaining(CategoryType categoryType,
                                                                            String searchTerm, String categoryId,
                                                                            Pageable pageable);

    /**
     * This method is used get active categories.
     *
     * @param currentTimeStamp currentTimeStamp
     * @param pageable         pageable
     * @return page of categories
     */
    @Query(value = "SELECT c FROM Category c WHERE " + ACTIVE_CATEGORIES)
    Page<Category> getActiveCategories(long currentTimeStamp, Pageable pageable);

    /**
     * This method is used get active categories with search term
     *
     * @param currentTimeStamp currentTimeStamp
     * @param searchTerm       searchTerm
     * @param pageable         pageable
     * @return page of categories
     */
    @Query(value = "SELECT c FROM Category c WHERE c.name LIKE %:searchTerm% AND ( " + ACTIVE_CATEGORIES + " )")
    Page<Category> getActiveCategoriesWithSearchTerm(long currentTimeStamp, String searchTerm, Pageable pageable);

    /**
     * This method is used get expired categories.
     *
     * @param currentTimeStamp currentTimeStamp
     * @param pageable         pageable
     * @return page of categories
     */
    @Query(value = "SELECT c FROM Category c WHERE " + EXPIRED_CATEGORIES)
    Page<Category> getExpiredCategories(long currentTimeStamp, Pageable pageable);

    /**
     * This method is used get expired categories with search term
     *
     * @param currentTimeStamp currentTimeStamp
     * @param searchTerm       searchTerm
     * @param pageable         pageable
     * @return page of categories
     */
    @Query(value = "SELECT c FROM Category c WHERE c.name LIKE %:searchTerm% AND ( " + EXPIRED_CATEGORIES + " )")
    Page<Category> getExpiredCategoriesWithSearchTerm(long currentTimeStamp, String searchTerm, Pageable pageable);
}
