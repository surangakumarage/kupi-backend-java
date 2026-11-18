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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests the {@link CategoryService} class
 */
class CategoryServiceTest {
    private static final int PAGE_NO = 0;
    private static final int SIZE = 1;
    private static final String CATEGORY_ID = "cid-1235-1458-1785";
    private static final String USER_ID = "uid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String EXCLUDE_ID = "cid-1235-1458-7856";
    private static final String NULL_EXCLUDE_ID = null;
    private static final String CATEGORY_NAME = "New Year";
    private static final String UPDATED_CATEGORY_NAME = "Christmas";
    private static final String UPDATED_CATEGORY_DESCRIPTION = "Cake";
    private static final String SEARCH_TERM = "New";
    private static final String ALL_SEARCH_TERM = "ALL";
    private static final String UPDATED_AT = "updatedAt";
    private static final String FAILED = "failed";
    private final Category category = getSampleCategory();
    private final CategoryUpdateRequestDto categoryUpdateRequestDto = getSampleUpdateCategoryRequestDto();
    private final Set<Category> categorySet = getSampleCategorySet();

    private CategoryService categoryService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryUpdateService categoryUpdateService;
    @Mock
    private FavouriteCategoriesRepository favouriteCategoriesRepository;

    @BeforeEach
    void setUp() {
        initMocks(this);
        categoryService = new CategoryService(categoryRepository, categoryUpdateService, favouriteCategoriesRepository);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnCategoriesSet_When_ValidCategoryIdsAreProvided() {
        List<String> categoryIdList = new ArrayList<>();
        categoryIdList.add(CATEGORY_ID);
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        assertEquals(categorySet.stream().findFirst().get().getId(),
                categoryService.getCategorySetByIds(categoryIdList).stream().findFirst().get().getId());
        assertEquals(categorySet.stream().findFirst().get().getName(),
                categoryService.getCategorySetByIds(categoryIdList).stream().findFirst().get().getName());
        assertEquals(categorySet.stream().findFirst().get().getImageUrl(),
                categoryService.getCategorySetByIds(categoryIdList).stream().findFirst().get().getImageUrl());
    }

    @Test
    void Should_ThrowInvalidCategoryException_When_InvalidCategoryIdIsProvided() {
        List<String> categoryIdList = new ArrayList<>();
        categoryIdList.add(CATEGORY_ID);
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());
        InvalidCategoryException invalidCategoryException = assertThrows(InvalidCategoryException.class, () ->
                categoryService.getCategorySetByIds(categoryIdList)
        );
        assertEquals("Invalid category Id: " + CATEGORY_ID, invalidCategoryException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetCategorySetByIdsIsFailed() {
        List<String> categoryIdList = new ArrayList<>();
        categoryIdList.add(CATEGORY_ID);
        when(categoryRepository.findById(CATEGORY_ID)).thenThrow(new DataAccessException("failed") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryService.getCategorySetByIds(categoryIdList)
        );
        assertEquals("Reading category by id from database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_SaveCategory() {
        when(categoryRepository.existsByName(CATEGORY_NAME)).thenReturn(false);
        categoryService.createCategory(category);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void Should_ThrowExistingNameException_When_ExistingCategoryNameIsProvided() {
        when(categoryRepository.existsByName(CATEGORY_NAME)).thenReturn(true);
        ExistingNameException existingNameException = assertThrows(ExistingNameException.class, () ->
                categoryService.createCategory(category)
        );
        assertEquals("Category name: " + CATEGORY_NAME + " exists in the database.",
                existingNameException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_IsCategoryNameExistFailed() {
        when(categoryRepository.existsByName(CATEGORY_NAME)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryService.createCategory(category)
        );
        assertEquals("Checking category Name is exists from the database was failed.",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_CreateCategoryIsFailed() {
        when(categoryRepository.save(category)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryService.createCategory(category)
        );
        assertEquals("Saving category into database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnCategory_When_ValidCategoryIdIsProvided() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        assertEquals(category, categoryService.getCategoryById(CATEGORY_ID));
    }

    @Test
    void Should_ThrowInvalidCategoryException_When_InvalidCategoryIdIsProvidedForGetCategoryById() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());
        InvalidCategoryException invalidCategoryException = assertThrows(InvalidCategoryException.class, () ->
                categoryService.getCategoryById(CATEGORY_ID)
        );
        assertEquals("Invalid category Id: " + CATEGORY_ID, invalidCategoryException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetCategoryByIdIsFailed() {
        when(categoryRepository.findById(CATEGORY_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryService.getCategoryById(CATEGORY_ID)
        );
        assertEquals("Reading category from database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnCategoryList_When_SearchTermIsEqualToAll() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        assertEquals(categoryPage, categoryService.listAllCategories(pageable, ALL_SEARCH_TERM));
    }

    @Test
    void Should_ReturnCategoryList_When_SearchTermIsNotEqualToAll() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryRepository.findAllByNameContaining(pageable, SEARCH_TERM)).thenReturn(categoryPage);
        assertEquals(categoryPage, categoryService.listAllCategories(pageable, SEARCH_TERM));
    }

    @Test
    void Should_ThrowKupiCoreException_When_ListAllCategoriesIsFailed() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        when(categoryRepository.findAllByNameContaining(pageable, SEARCH_TERM))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryService.listAllCategories(pageable, SEARCH_TERM));
        assertEquals("Reading category list from database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnCategoryPage_When_SearchItemIsEqualToAllForGetActiveCategories() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryRepository.getActiveCategories(anyLong(), any(Pageable.class))).thenReturn(categoryPage);
        assertEquals(categoryPage, categoryService.getActiveCategories(pageable, ALL_SEARCH_TERM));
    }

    @Test
    void Should_ReturnCategoryPage_When_SearchItemIsNotEqualToAllForGetActiveCategories() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryRepository.getActiveCategoriesWithSearchTerm(anyLong(), anyString(), any(Pageable.class)))
                .thenReturn(categoryPage);
        assertEquals(categoryPage, categoryService.getActiveCategories(pageable, SEARCH_TERM));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetActiveCategoryListIsFailed() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        when(categoryRepository.getActiveCategories(anyLong(), any(Pageable.class)))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryService.getActiveCategories(pageable, ALL_SEARCH_TERM));
        assertEquals("Reading active category list from database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnCategoryPage_When_ValidPageableIsProvidedForGetPopularCategoryList() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).ascending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryRepository.getActivePopularCategories(anyLong(), any(Pageable.class))).thenReturn(categoryPage);
        assertEquals(categoryPage, categoryService.listAllPopularCategories(pageable));
    }

    @Test
    void Should_ThrowKupiCoreException_When_ListAllPopularCategoriesIsFailed() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).ascending());
        when(categoryRepository.getActivePopularCategories(anyLong(), any(Pageable.class)))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryService.listAllPopularCategories(pageable));
        assertEquals("Reading popular category list from database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnCategoryPage_When_SearchTermIsEqualToAllAndExcludeIdIsNull() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryRepository.findAllByCategoryType(CategoryType.SEASONAL, pageable)).thenReturn(categoryPage);
        assertEquals(categoryPage, categoryService.getCategoriesForCategoryType(CategoryType.SEASONAL, pageable,
                ALL_SEARCH_TERM, NULL_EXCLUDE_ID));
    }

    @Test
    void Should_ReturnCategoryPage_When_SearchTermIsNotEqualToAllAndExcludeIdIsNull() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryRepository.findAllByCategoryTypeAndNameContaining(CategoryType.SEASONAL, SEARCH_TERM, pageable))
                .thenReturn(categoryPage);
        assertEquals(categoryPage, categoryService.getCategoriesForCategoryType(CategoryType.SEASONAL, pageable,
                SEARCH_TERM, NULL_EXCLUDE_ID));
    }

    @Test
    void Should_ReturnCategoryPage_When_SearchTermIsEqualToAllAndExcludeIdIsNotNull() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryRepository.findAllByCategoryTypeAndIdNotContaining(CategoryType.SEASONAL, EXCLUDE_ID, pageable))
                .thenReturn(categoryPage);
        assertEquals(categoryPage, categoryService.getCategoriesForCategoryType(CategoryType.SEASONAL, pageable,
                ALL_SEARCH_TERM, EXCLUDE_ID));
    }

    @Test
    void Should_ReturnCategoryPage_When_SearchTermIsNotEqualToAllAndExcludeIdIsNotNull() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryRepository.findAllByCategoryTypeAndNameContainingAndIdNotContaining(CategoryType.SEASONAL,
                SEARCH_TERM, EXCLUDE_ID, pageable)).thenReturn(categoryPage);
        assertEquals(categoryPage, categoryService.getCategoriesForCategoryType(CategoryType.SEASONAL, pageable,
                SEARCH_TERM, EXCLUDE_ID));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetCategoriesForCategoryTypeIsFailed() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        when(categoryRepository.findAllByCategoryType(CategoryType.SEASONAL, pageable))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryService.getCategoriesForCategoryType(CategoryType.SEASONAL, pageable,
                        ALL_SEARCH_TERM, NULL_EXCLUDE_ID));
        assertEquals("Getting category list for category type from database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnRelateCategoryPage_When_ValidCategoryIdIsProvided() {
        Set<String> relatedCategoryIdsSet = new HashSet<>();
        relatedCategoryIdsSet.add(CATEGORY_ID);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.getRelatedCategoriesById(CATEGORY_ID)).thenReturn(relatedCategoryIdsSet);
        when(categoryRepository.getActiveCategoriesByIds(anySet(), anyLong(), any(Pageable.class)))
                .thenReturn(categoryPage);
        assertEquals(categoryPage, categoryService.getRelatedCategoryListForCategory(pageable, CATEGORY_ID));
    }

    @Test
    void Should_ThrowUnsupportedCategoryTypeException_When_CategoryTypeIsNonSeasonalWhichReceivedFromDB() {
        Set<String> relatedCategoryIdsSet = new HashSet<>();
        relatedCategoryIdsSet.add(CATEGORY_ID);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        category.setCategoryType(CategoryType.NORMAL);
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        UnsupportedCategoryTypeException exception = assertThrows(UnsupportedCategoryTypeException.class, () ->
                categoryService.getRelatedCategoryListForCategory(pageable, CATEGORY_ID));
        assertEquals("Invalid Category for retuning related categories.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetBulkActiveCategoriesByIdsIsFailed() {
        Set<String> relatedCategoryIdsSet = new HashSet<>();
        relatedCategoryIdsSet.add(CATEGORY_ID);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.getRelatedCategoriesById(CATEGORY_ID)).thenReturn(relatedCategoryIdsSet);
        when(categoryRepository.getActiveCategoriesByIds(anySet(), anyLong(), any(Pageable.class)))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryService.getRelatedCategoryListForCategory(pageable, CATEGORY_ID));
        assertEquals("Returning bulk Category by Id(s) from database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetRelatedCategoryListForCategoryIsFailed() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.getRelatedCategoriesById(CATEGORY_ID))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryService.getRelatedCategoryListForCategory(pageable, CATEGORY_ID));
        assertEquals("Returning related categories for a category id from the database was failed.",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnCategory_When_ValidCategoryIdAndNameAreProvided() {
        Category updatedCategory = getSampleUpdatedCategory();
        when(categoryRepository.existsByNameAndIdNot(UPDATED_CATEGORY_NAME, CATEGORY_ID)).thenReturn(false);
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
        assertEquals(updatedCategory, categoryService.updateCategory(categoryUpdateRequestDto));
    }

    @Test
    void Should_ThrowExistingNameException_When_GivenCategoryNameIsAlreadyExisted() {
        when(categoryRepository.existsByNameAndIdNot(UPDATED_CATEGORY_NAME, CATEGORY_ID)).thenReturn(true);
        ExistingNameException existingNameException = assertThrows(ExistingNameException.class, () ->
                categoryService.updateCategory(categoryUpdateRequestDto));
        assertEquals("Category name: " + categoryUpdateRequestDto.getName() + " exists in the database.",
                existingNameException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_CheckExistingCategoryNameWasFailed() {
        when(categoryRepository.existsByNameAndIdNot(UPDATED_CATEGORY_NAME, CATEGORY_ID))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryService.updateCategory(categoryUpdateRequestDto));
        assertEquals("Checking category Name is exists from the database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_UpdateCategoryIsFailed() {
        when(categoryRepository.existsByNameAndIdNot(UPDATED_CATEGORY_NAME, CATEGORY_ID)).thenReturn(false);
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryService.updateCategory(categoryUpdateRequestDto));
        assertEquals("Updating category into database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_DeleteCategory() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.existsCategoryByRelatedCategories(category)).thenReturn(false);
        categoryService.deleteCategory(CATEGORY_ID, false);
        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void Should_ThrowUnsupportedDeleteAction_When_CategoryAssociateWithCategoryBranchMerchant() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.existsCategoryByRelatedCategories(category)).thenReturn(false);
        UnsupportedDeleteAction unsupportedDeleteAction = assertThrows(UnsupportedDeleteAction.class, () ->
                categoryService.deleteCategory(CATEGORY_ID, true));
        assertEquals("Category cannot be deleted.", unsupportedDeleteAction.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_CheckRelatedCategoriesFromDataBaseIsFailed() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.existsCategoryByRelatedCategories(category)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryService.deleteCategory(CATEGORY_ID, false));
        assertEquals("Checking category is exists in related categories from the database was failed.",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_DeleteCategoryIsFailed() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.existsCategoryByRelatedCategories(category)).thenReturn(false);
        doThrow(new DataAccessException("ERROR") {
        }).when(categoryRepository).delete(category);
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryService.deleteCategory(CATEGORY_ID, false));
        assertEquals("Deleting Category from the database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ValidateCategoryId() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        categoryService.validateCategoryId(CATEGORY_ID);
        verify(categoryRepository, times(1)).findById(CATEGORY_ID);
    }

    @Test
    void Should_ThrowInvalidCategoryException_When_InvalidCategoryIdIsProvidedForValidateCategoryId() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());
        InvalidCategoryException invalidCategoryException = assertThrows(InvalidCategoryException.class, () ->
                categoryService.validateCategoryId(CATEGORY_ID));
        assertEquals("Invalid category Id: " + CATEGORY_ID, invalidCategoryException.getMessage());
    }

    /**
     * Get bulk categories by ids
     */
    @Test
    void Should_ReturnCategoryPage_When_GettingBulkCategoriesByIdsForAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());

        when(categoryRepository.getActiveCategoriesByIds(anySet(), anyLong(), any(Pageable.class)))
                .thenReturn(getSampleCategoryPage());
        assertEquals(CATEGORY_ID, categoryService.getBulkCategoriesByIds(pageable, new HashSet<>(), ALL_SEARCH_TERM)
                .getContent().get(0).getId());
    }

    @Test
    void Should_ReturnCategoryPage_When_GettingBulkCategoriesByIdsForSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());

        when(categoryRepository.getActiveCategoriesByIdsAndSearchTerm(anySet(), anyString(), anyLong(),
                any(Pageable.class))).thenReturn(getSampleCategoryPage());
        assertEquals(CATEGORY_ID, categoryService.getBulkCategoriesByIds(pageable, new HashSet<>(), SEARCH_TERM)
                .getContent().get(0).getId());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingBulkCategoriesByIdsIsFailed() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());

        when(categoryRepository.getActiveCategoriesByIds(anySet(), anyLong(), any(Pageable.class)))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () ->
                categoryService.getBulkCategoriesByIds(pageable, new HashSet<>(), ALL_SEARCH_TERM));
        assertEquals("Returning bulk Category by Id(s) from database was failed.", exception.getMessage());
    }

    @Test
    void Should_ReturnCategoryIds_When_ValidUserIdIsProvidedForGetFavouriteCategoryIdList() {
        FavouriteCategories favouriteCategories = getSampleFavouriteCategories();
        when(favouriteCategoriesRepository.findById(USER_ID)).thenReturn(Optional.of(favouriteCategories));
        assertEquals(favouriteCategories.getCategoryIds(), categoryService.getFavouriteCategoryIdList(USER_ID));
    }

    @Test
    void Should_ReturnEmptyCategoryIds_When_InvalidUserIdIsProvidedForGetFavouriteCategoryIdList() {
        when(favouriteCategoriesRepository.findById(USER_ID)).thenReturn(Optional.empty());
        assertEquals(new HashSet<String>(), categoryService.getFavouriteCategoryIdList(USER_ID));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingFavouriteCategoriesFromDataBaseIsFailed() {
        when(favouriteCategoriesRepository.findById(USER_ID)).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () ->
                categoryService.getFavouriteCategoryIdList(USER_ID));
        assertEquals("Failed to get favourite categoryIds list for userId: " + USER_ID, exception.getMessage());
    }

    @Test
    void Should_ReturnCategoryPage_When_ValidUserIdIsProvidedForGetFavouriteCategory() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        FavouriteCategories favouriteCategories = getSampleFavouriteCategories();
        Page<Category> categoryPage = getSampleCategoryPage();
        when(favouriteCategoriesRepository.findById(USER_ID)).thenReturn(Optional.of(favouriteCategories));
        when(categoryRepository.getActiveCategoriesByIds(anySet(), anyLong(), any(Pageable.class)))
                .thenReturn(categoryPage);
        assertEquals(categoryPage, categoryService.getFavouriteCategory(USER_ID, pageable));
    }

    @Test
    void Should_ReturnEmptyCategoryPage_When_InvalidUserIdIsProvidedForGetFavouriteCategory() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        when(favouriteCategoriesRepository.findById(USER_ID)).thenReturn(Optional.empty());
        assertEquals(Page.empty(pageable), categoryService.getFavouriteCategory(USER_ID, pageable));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetCategoryPageFromDataBaseIsFailed() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        FavouriteCategories favouriteCategories = getSampleFavouriteCategories();
        when(favouriteCategoriesRepository.findById(USER_ID)).thenReturn(Optional.of(favouriteCategories));
        when(categoryRepository.getActiveCategoriesByIds(anySet(), anyLong(), any(Pageable.class)))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () ->
                categoryService.getFavouriteCategory(USER_ID, pageable));
        assertEquals("Failed to get favourite category list for userId: " + USER_ID, exception.getMessage());
    }

    @Test
    void Should_ReturnCategoryViewCountResponseDtoList_When_CategoryViewListIsProvided() {
        List<CategoryViewCountResponseDto> categoryViewCountResponseDtoList = getSampleCategoryViewCountResponseDtoList();
        List<ViewCountAnalyticResponseDto> categoryViewsList = getSampleViewCountAnalyticResponseDtoList();
        Category category = getSampleCategory();
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        assertEquals(categoryViewCountResponseDtoList.get(0).getCategory().getId(),
                categoryService.getCategoriesForReport(categoryViewsList, TIME_ZONE).get(0).getCategory().getId());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetCategoryFromDataBaseIsFailed() {
        List<ViewCountAnalyticResponseDto> categoryViewsList = getSampleViewCountAnalyticResponseDtoList();
        when(categoryRepository.findById(CATEGORY_ID)).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryService.getCategoriesForReport(categoryViewsList, TIME_ZONE));
        assertEquals("Reading deal by id from database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_RemoveFavouriteCategory_When_ValidUserIdIsProvidedForRemoveFavouriteCategory() {
        FavouriteCategories favouriteCategories = getSampleFavouriteCategories();
        when(favouriteCategoriesRepository.findById(USER_ID)).thenReturn(Optional.of(favouriteCategories));
        categoryService.removeFavouriteCategory(USER_ID, CATEGORY_ID);
        verify(favouriteCategoriesRepository).save(favouriteCategories);
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetFavouriteCategoryFromDataBaseIsFailed() {
        when(favouriteCategoriesRepository.findById(USER_ID)).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () ->
                categoryService.removeFavouriteCategory(USER_ID, CATEGORY_ID));
        assertEquals("Failed to remove favourite category for userId: " + USER_ID, exception.getMessage());
    }

    @Test
    void Should_AddFavouriteCategory_When_FavouriteCategoryAlreadyExists() {
        Category category = getSampleCategory();
        FavouriteCategories favouriteCategories = getSampleFavouriteCategories();
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(favouriteCategoriesRepository.findById(USER_ID)).thenReturn(Optional.of(favouriteCategories));
        categoryService.addFavouriteCategory(USER_ID, CATEGORY_ID);
        verify(favouriteCategoriesRepository).save(favouriteCategories);
    }

    @Test
    void Should_AddFavouriteCategory_When_FavouriteCategoryNotExists() {
        Category category = getSampleCategory();
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(favouriteCategoriesRepository.findById(USER_ID)).thenReturn(Optional.empty());
        categoryService.addFavouriteCategory(USER_ID, CATEGORY_ID);
        verify(favouriteCategoriesRepository).save(any(FavouriteCategories.class));
    }

    @Test
    void Should_ThrowKupiCoreException_When_SaveFavouriteCategoryIntoDataBaseIsFailed() {
        Category category = getSampleCategory();
        FavouriteCategories favouriteCategories = getSampleFavouriteCategories();
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(favouriteCategoriesRepository.findById(USER_ID)).thenReturn(Optional.of(favouriteCategories));
        when(favouriteCategoriesRepository.save(favouriteCategories)).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () ->
                categoryService.addFavouriteCategory(USER_ID, CATEGORY_ID));
        assertEquals("Failed to add favourite category for userId: " + USER_ID, exception.getMessage());
    }

    @Test
    void Should_ReturnCategoryPage_When_SearchTermIsEqualToAllForGetExpiredCategories() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryRepository.getExpiredCategories(anyLong(), any(Pageable.class))).thenReturn(categoryPage);
        assertEquals(categoryPage, categoryService.getExpiredCategories(pageable, ALL_SEARCH_TERM));
    }

    @Test
    void Should_ReturnCategoryPage_When_SearchTermIsNotEqualToAllForGetExpiredCategories() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryRepository.getExpiredCategoriesWithSearchTerm(anyLong(), anyString(), any(Pageable.class)))
                .thenReturn(categoryPage);
        assertEquals(categoryPage, categoryService.getExpiredCategories(pageable, SEARCH_TERM));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetExpiredCategoriesFromDataBaseIsFailed() {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        when(categoryRepository.getExpiredCategories(anyLong(), any(Pageable.class)))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () ->
                categoryService.getExpiredCategories(pageable, ALL_SEARCH_TERM));
        assertEquals("Reading expired category list from database was failed.", exception.getMessage());
    }

    /**
     * This method creates sample category
     *
     * @return category
     */
    private Category getSampleCategory() {
        Set<Category> relationalCategories = new HashSet<>();

        Category category = new Category();
        category.setId("cid-1235-1458-1785");
        category.setName(CATEGORY_NAME);
        category.setDescription("snacks and sweets");
        category.setImageUrl("https://picsum.photos/200/300");
        category.setCategoryType(CategoryType.SEASONAL);
        category.setStartDate(1654021800000L);
        category.setExpiryDate(1670697000000L);
        category.setRelatedCategories(relationalCategories);
        category.setPopular(true);
        return category;
    }

    /**
     * This method creates sample category set
     *
     * @return categorySet
     */
    private Set<Category> getSampleCategorySet() {
        Set<Category> categorySet = new HashSet<>();
        Category category = getSampleCategory();
        categorySet.add(category);
        return categorySet;
    }

    /**
     * This method creates sample category page
     *
     * @return categoryPage
     */
    private Page<Category> getSampleCategoryPage() {
        List<Category> categoryList = new ArrayList<>();
        Category sampleCategory = getSampleCategory();
        categoryList.add(sampleCategory);
        return new PageImpl<>(categoryList);
    }

    private Category getSampleUpdatedCategory() {
        Set<Category> relationalCategories = new HashSet<>();

        Category category = new Category();
        category.setId(CATEGORY_ID);
        category.setName(UPDATED_CATEGORY_NAME);
        category.setDescription(UPDATED_CATEGORY_DESCRIPTION);
        category.setImageUrl("https://picsum.photos/200/300");
        category.setCategoryType(CategoryType.SEASONAL);
        category.setStartDate(1654021800000L);
        category.setExpiryDate(1670697000000L);
        category.setRelatedCategories(relationalCategories);
        category.setPopular(true);
        return category;
    }

    /**
     * This method creates sample update category request dto
     *
     * @return CategoryUpdateRequestDto
     */
    private CategoryUpdateRequestDto getSampleUpdateCategoryRequestDto() {
        List<String> relatedCategories = new ArrayList<>();

        CategoryUpdateRequestDto categoryRequestDto = new CategoryUpdateRequestDto();
        categoryRequestDto.setId(CATEGORY_ID);
        categoryRequestDto.setCategoryType(CategoryType.SEASONAL);
        categoryRequestDto.setDescription(UPDATED_CATEGORY_DESCRIPTION);
        categoryRequestDto.setName(UPDATED_CATEGORY_NAME);
        categoryRequestDto.setPopular(true);
        category.setStartDate(1654021800000L);
        category.setExpiryDate(1670697000000L);
        categoryRequestDto.setRelatedCategories(relatedCategories);
        categoryRequestDto.setImageUrl("https://picsum.photos/200/300");
        return categoryRequestDto;
    }

    /**
     * This method creates sample CategoryIds set
     *
     * @return CategoryIdsSet
     */
    private Set<String> getSampleCategoryIds() {
        Set<String> categoryIds = new HashSet<>();
        categoryIds.add(CATEGORY_ID);
        return categoryIds;
    }

    /**
     * This method creates sample FavouriteCategories
     *
     * @return FavouriteCategories
     */
    private FavouriteCategories getSampleFavouriteCategories() {
        Set<String> categoryIds = getSampleCategoryIds();
        FavouriteCategories favouriteCategories = new FavouriteCategories();
        favouriteCategories.setUserId(USER_ID);
        favouriteCategories.setCategoryIds(categoryIds);
        return favouriteCategories;
    }

    /**
     * This method creates sample FavouriteCategories page
     *
     * @return FavouriteCategoriesPage
     */
    private Page<FavouriteCategories> getSampleFavouriteCategoriesPage() {
        FavouriteCategories favouriteCategories = getSampleFavouriteCategories();
        List<FavouriteCategories> favouriteCategoriesList = new ArrayList<>();
        favouriteCategoriesList.add(favouriteCategories);
        return new PageImpl<>(favouriteCategoriesList);
    }

    private List<CategoryViewCountResponseDto> getSampleCategoryViewCountResponseDtoList() {
        Category category = getSampleCategory();
        CategoryResponseDto categoryResponseDto = new CategoryResponseDto(category, TIME_ZONE);
        String displayDate = new Date(System.currentTimeMillis()).toString();
        CategoryViewCountResponseDto categoryViewCountResponseDto = new CategoryViewCountResponseDto(categoryResponseDto,
                5, displayDate, 2);
        List<CategoryViewCountResponseDto> categoryViewCountResponseDtoList = new ArrayList<>();
        categoryViewCountResponseDtoList.add(categoryViewCountResponseDto);
        return categoryViewCountResponseDtoList;
    }

    private List<ViewCountAnalyticResponseDto> getSampleViewCountAnalyticResponseDtoList() {
        ViewCountAnalyticResponseDto viewCountAnalyticResponseDto = new ViewCountAnalyticResponseDto();
        viewCountAnalyticResponseDto.setCategoryId(CATEGORY_ID);
        viewCountAnalyticResponseDto.setViewCount(5);
        String displayDate = new Date(System.currentTimeMillis()).toString();
        viewCountAnalyticResponseDto.setDisplayDate(displayDate);
        viewCountAnalyticResponseDto.setTotalRows(2);
        List<ViewCountAnalyticResponseDto> viewCountAnalyticResponseDtoList = new ArrayList<>();
        viewCountAnalyticResponseDtoList.add(viewCountAnalyticResponseDto);
        return viewCountAnalyticResponseDtoList;
    }
}