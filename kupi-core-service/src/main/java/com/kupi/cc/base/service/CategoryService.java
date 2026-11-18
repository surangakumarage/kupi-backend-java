package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Category;
import com.kupi.cc.base.domain.entity.FavouriteCategories;
import com.kupi.cc.base.domain.request.CategoryUpdateRequestDto;
import com.kupi.cc.base.domain.response.CategoryResponseDto;
import com.kupi.cc.base.domain.response.CategoryViewCountResponseDto;
import com.kupi.cc.base.domain.response.ViewCountAnalyticResponseDto;
import com.kupi.cc.base.enums.CategoryType;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.repository.CategoryRepository;
import com.kupi.cc.base.repository.FavouriteCategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CategoryService {

    private static final String ALL = "ALL";
    private static final String INVALID_CATEGORY = "Invalid category Id: ";
    private final CategoryRepository categoryRepository;
    private final CategoryUpdateService categoryUpdateService;
    private final FavouriteCategoriesRepository favouriteCategoriesRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, CategoryUpdateService categoryUpdateService,
                           FavouriteCategoriesRepository favouriteCategoriesRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryUpdateService = categoryUpdateService;
        this.favouriteCategoriesRepository = favouriteCategoriesRepository;
    }

    /**
     * Get list of categories
     *
     * @param pageable pageable with page, size and sort
     * @return category page
     */
    public Page<Category> listAllCategories(Pageable pageable, String searchTerm) {
        try {
            if (searchTerm.equals(ALL))
                return categoryRepository.findAll(pageable);
            else
                return categoryRepository.findAllByNameContaining(pageable, searchTerm);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading category list from database was failed.", e);
        }
    }

    /**
     * This method is used to get active categories.
     *
     * @param pageable   pageable
     * @param searchTerm searchTerm
     * @return page of categories
     */
    public Page<Category> getActiveCategories(Pageable pageable, String searchTerm) {
        try {
            if (searchTerm.equals(ALL))
                return categoryRepository.getActiveCategories(System.currentTimeMillis(), pageable);
            else
                return categoryRepository
                        .getActiveCategoriesWithSearchTerm(System.currentTimeMillis(), searchTerm, pageable);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading active category list from database was failed.", e);
        }
    }

    /**
     * This method is used to get expired categories.
     *
     * @param pageable   pageable
     * @param searchTerm searchTerm
     * @return page of categories
     */
    public Page<Category> getExpiredCategories(Pageable pageable, String searchTerm) {
        try {
            if (searchTerm.equals(ALL))
                return categoryRepository.getExpiredCategories(System.currentTimeMillis(), pageable);
            else
                return categoryRepository
                        .getExpiredCategoriesWithSearchTerm(System.currentTimeMillis(), searchTerm, pageable);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading expired category list from database was failed.", e);
        }
    }

    /**
     * This method is used to get category list by category type.
     *
     * @param categoryType NORMAL/SEASONAL
     * @param pageable     pageable
     * @param searchTerm   searchTerm
     * @param excludeId    optional categoryId
     * @return page of categories.
     */
    public Page<Category> getCategoriesForCategoryType(CategoryType categoryType, Pageable pageable,
                                                       String searchTerm, String excludeId) {
        try {
            if (searchTerm.equals(ALL) && excludeId == null)
                return categoryRepository.findAllByCategoryType(categoryType, pageable);
            if (!searchTerm.equals(ALL) && excludeId == null)
                return categoryRepository.findAllByCategoryTypeAndNameContaining(categoryType, searchTerm, pageable);
            if (searchTerm.equals(ALL))
                return categoryRepository
                        .findAllByCategoryTypeAndIdNotContaining(categoryType, excludeId, pageable);
            return categoryRepository.findAllByCategoryTypeAndNameContainingAndIdNotContaining(
                    categoryType, searchTerm, excludeId, pageable);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Getting category list for category type from database was failed.", e);
        }
    }

    /**
     * Get list of popular categories
     *
     * @param pageable pageable with page, size and sort
     * @return category page
     */
    public Page<Category> listAllPopularCategories(Pageable pageable) {
        try {
            return categoryRepository.getActivePopularCategories(System.currentTimeMillis(), pageable);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading popular category list from database was failed.", e);
        }
    }

    /**
     * Read categories by Ids
     *
     * @param categoryIdList categoryIdList
     * @return Category set
     */
    public Set<Category> getCategorySetByIds(List<String> categoryIdList) {
        try {
            Set<Category> relatedCategories = new HashSet<>();
            for (String id : categoryIdList) {
                Optional<Category> optionalCategoryFromDb = categoryRepository.findById(id);
                if (optionalCategoryFromDb.isPresent()) {
                    relatedCategories.add(optionalCategoryFromDb.get());
                } else {
                    throw new InvalidCategoryException(INVALID_CATEGORY + id);
                }
            }
            return relatedCategories;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading category by id from database was failed.", e);
        }
    }

    /**
     * Get category by Id
     *
     * @param categoryId category Id
     * @return category
     */
    public Category getCategoryById(String categoryId) {
        try {
            Optional<Category> optionalCategoryFromDb = categoryRepository.findById(categoryId);
            if (optionalCategoryFromDb.isPresent()) {
                return optionalCategoryFromDb.get();
            } else {
                throw new InvalidCategoryException(INVALID_CATEGORY + categoryId);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading category from database was failed.", e);
        }
    }

    /**
     * Save new category into database
     *
     * @param category category
     */
    public void createCategory(Category category) {
        try {
            boolean existsByName = isCategoryNameExist(category.getName(), null);
            if (existsByName) {
                throw new ExistingNameException("Category name: " + category.getName() + " exists in the database.");
            }
            categoryRepository.save(category);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving category into database was failed.", e);
        }
    }

    /**
     * This method validate the given categoryId.
     *
     * @param categoryId categoryId
     */
    public void validateCategoryId(String categoryId) {
        Optional<Category> categoryFromDb = categoryRepository.findById(categoryId);
        if (categoryFromDb.isEmpty()) {
            throw new InvalidCategoryException(INVALID_CATEGORY + categoryId);
        }
    }

    /**
     * This method update the existing category
     *
     * @param categoryUpdateRequestDto categoryUpdateRequestDto
     * @return category
     */
    public Category updateCategory(CategoryUpdateRequestDto categoryUpdateRequestDto) {
        try {
            boolean existsByName =
                    isCategoryNameExist(categoryUpdateRequestDto.getName(), categoryUpdateRequestDto.getId());
            if (existsByName) {
                throw new ExistingNameException("Category name: "
                        + categoryUpdateRequestDto.getName() + " exists in the database.");
            }
            var categoryFromDb = getCategoryById(categoryUpdateRequestDto.getId());
            var previousCategoryName = categoryFromDb.getName();
            var categorySet = getCategorySetByIds(categoryUpdateRequestDto.getRelatedCategories());
            categoryFromDb.update(categoryUpdateRequestDto, categorySet);
            categoryUpdateService.updateCategoryData(categoryFromDb, previousCategoryName);
            return categoryRepository.save(categoryFromDb);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Updating category into database was failed.", e);
        }
    }

    /**
     * This method returns active category page by category ids set.
     *
     * @param pageable pageable
     * @param ids      category id set
     * @return category page
     */
    public Page<Category> getBulkActiveCategoriesByIds(Pageable pageable, Set<String> ids, String searchTerm) {
        try {
            long currentTimeStamp = System.currentTimeMillis();
            return ((ALL.equals(searchTerm)) ?
                    categoryRepository.getActiveCategoriesByIds(ids, currentTimeStamp, pageable) :
                    categoryRepository
                            .getActiveCategoriesByIdsAndSearchTerm(ids, searchTerm, currentTimeStamp, pageable));
        } catch (DataAccessException e) {
            throw new KupiCoreException("Returning bulk Category by Id(s) from database was failed.", e);
        }
    }

    /**
     * This method returns a category page by category ids set.
     *
     * @param pageable pageable
     * @param ids      category id set
     * @return category page
     */
    public Page<Category> getBulkCategoriesByIds(Pageable pageable, Set<String> ids, String searchTerm) {
        try {
            long currentTimeStamp = System.currentTimeMillis();
            return ((ALL.equals(searchTerm)) ?
                    categoryRepository.getActiveCategoriesByIds(ids, currentTimeStamp, pageable) :
                    categoryRepository
                            .getActiveCategoriesByIdsAndSearchTerm(ids, searchTerm, currentTimeStamp, pageable));
        } catch (DataAccessException e) {
            throw new KupiCoreException("Returning bulk Category by Id(s) from database was failed.", e);
        }
    }

    /**
     * This method returns true when category name is exist in the database.
     *
     * @param categoryName category name
     * @return true/false
     */
    private boolean isCategoryNameExist(String categoryName, String categoryId) {
        try {
            return (categoryId == null) ? categoryRepository.existsByName(categoryName) :
                    categoryRepository.existsByNameAndIdNot(categoryName, categoryId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Checking category Name is exists from the database was failed.", e);
        }
    }

    /**
     * This method deletes a category.
     *
     * @param categoryId categoryId
     */
    public void deleteCategory(String categoryId, boolean isCategoryMapWithCategoryBrandMerchant) {
        try {
            var category = getCategoryById(categoryId);
            boolean isCategoryARelatedCategory = checkCategoryInRelatedCategories(category);
            if (!isCategoryMapWithCategoryBrandMerchant && !isCategoryARelatedCategory) {
                categoryRepository.delete(category);
            } else {
                throw new UnsupportedDeleteAction("Category cannot be deleted.");
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Deleting Category from the database was failed.", e);
        }
    }

    /**
     * This method returns true when category is a relatedCategory for another category.
     *
     * @param category category
     * @return true/ false
     */
    public boolean checkCategoryInRelatedCategories(Category category) {
        try {
            return categoryRepository.existsCategoryByRelatedCategories(category);
        } catch (DataAccessException e) {
            throw new KupiCoreException(
                    "Checking category is exists in related categories from the database was failed.", e);
        }
    }

    /**
     * This method used to get page of related categories for a category.
     *
     * @param pageable   pageable
     * @param categoryId category id
     * @return Category page
     */
    public Page<Category> getRelatedCategoryListForCategory(Pageable pageable, String categoryId) {
        try {
            Category category = getCategoryById(categoryId);
            if (category.getCategoryType().equals(CategoryType.SEASONAL)) {
                Set<String> relatedCategoriesById = categoryRepository.getRelatedCategoriesById(categoryId);
                return getBulkActiveCategoriesByIds(pageable, relatedCategoriesById, ALL);
            } else {
                throw new UnsupportedCategoryTypeException("Invalid Category for retuning related categories.");
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Returning related categories for a category id from the database was failed.", e);
        }
    }

    /**
     * This method is used to get categories for category report.
     *
     * @param categoryViewsList categoryViewsList
     * @return categories with view count.
     */
    public List<CategoryViewCountResponseDto> getCategoriesForReport(List<ViewCountAnalyticResponseDto>
                                                                             categoryViewsList, String timeZone) {
        try {
            List<CategoryViewCountResponseDto> categoryAndViews = new ArrayList<>();
            for (ViewCountAnalyticResponseDto categoryIdAndViews : categoryViewsList) {
                Optional<Category> optionalCategory = categoryRepository.findById(categoryIdAndViews.getCategoryId());
                optionalCategory.ifPresent(category -> categoryAndViews.add(
                        new CategoryViewCountResponseDto(new CategoryResponseDto(category, timeZone),
                                categoryIdAndViews.getViewCount(), categoryIdAndViews.getDisplayDate(),
                                categoryIdAndViews.getTotalRows())));
            }
            return categoryAndViews;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading deal by id from database was failed.", e);
        }
    }

    /**
     * This method adds favourite category for a user.
     *
     * @param userId     userId
     * @param categoryId categoryId
     */
    public void addFavouriteCategory(String userId, String categoryId) {
        try {
            getCategoryById(categoryId);
            Optional<FavouriteCategories> optionalFavouriteCategories = favouriteCategoriesRepository.findById(userId);
            FavouriteCategories favouriteCategories;
            if (optionalFavouriteCategories.isPresent()) {
                favouriteCategories = optionalFavouriteCategories.get();
                favouriteCategories.getCategoryIds().add(categoryId);
            } else {
                Set<String> categoryIdSet = new HashSet<>();
                categoryIdSet.add(categoryId);
                favouriteCategories = new FavouriteCategories(userId, categoryIdSet);
            }
            favouriteCategoriesRepository.save(favouriteCategories);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to add favourite category for userId: " + userId, e);
        }
    }

    /**
     * This method removes favourite category for a user.
     *
     * @param userId     userId
     * @param categoryId categoryId
     */
    public void removeFavouriteCategory(String userId, String categoryId) {
        try {
            Optional<FavouriteCategories> optionalFavouriteCategories = favouriteCategoriesRepository.findById(userId);
            if (optionalFavouriteCategories.isPresent()) {
                FavouriteCategories favouriteCategories = optionalFavouriteCategories.get();
                favouriteCategories.getCategoryIds().remove(categoryId);
                favouriteCategoriesRepository.save(favouriteCategories);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to remove favourite category for userId: " + userId, e);
        }
    }

    /**
     * This method returns favourite category page for a user.
     *
     * @param userId   userId
     * @param pageable pageable
     * @return favourite category page.
     */
    public Page<Category> getFavouriteCategory(String userId, Pageable pageable) {
        try {
            Optional<FavouriteCategories> optionalFavouriteCategories = favouriteCategoriesRepository.findById(userId);
            if (optionalFavouriteCategories.isPresent())
                return categoryRepository.getActiveCategoriesByIds(optionalFavouriteCategories.get().getCategoryIds(),
                        System.currentTimeMillis(), pageable);
            return Page.empty(pageable);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to get favourite category list for userId: " + userId, e);
        }
    }

    /**
     * This method returns favourite categoryIds for a user.
     *
     * @param userId userId
     * @return set of favourite categoryIds.
     */
    public Set<String> getFavouriteCategoryIdList(String userId) {
        try {
            Optional<FavouriteCategories> optionalFavouriteCategories = favouriteCategoriesRepository.findById(userId);
            return optionalFavouriteCategories.isPresent() ?
                    optionalFavouriteCategories.get().getCategoryIds() : new HashSet<>();
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to get favourite categoryIds list for userId: " + userId, e);
        }
    }

}
