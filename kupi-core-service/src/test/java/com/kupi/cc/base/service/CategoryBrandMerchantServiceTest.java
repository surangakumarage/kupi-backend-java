package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Brand;
import com.kupi.cc.base.domain.entity.Category;
import com.kupi.cc.base.domain.entity.CategoryBrandMerchant;
import com.kupi.cc.base.domain.entity.CategoryBrandMerchantIndex;
import com.kupi.cc.base.domain.response.BasicMerchantBusinessResponseDto;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.CategoryBrandMerchantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests the {@link CategoryBrandMerchantService} class
 */
class CategoryBrandMerchantServiceTest {

    private static final long MERCHANT_COUNT = 1L;
    private static final long ACTIVE_MERCHANT_COUNT = 0L;
    private static final String CATEGORY_ID = "cid-1235-1458-1785";
    private static final String MERCHANT_ID = "uid-1235-1458-1785";
    private static final String BRAND_ID = "bid-123456789";
    private static final String FAILED = "failed";
    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final String DEFAULT_SORT = "updatedAt";
    private static final String SEARCH_TERM = "Softlogic";
    private static final String ALL = "ALL";

    private CategoryBrandMerchantService categoryBrandMerchantService;

    @Mock
    private CategoryBrandMerchantRepository categoryBrandMerchantRepository;
    @Mock
    private CategoryBrandMerchantIndexService categoryBrandMerchantIndexService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private BrandService brandService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        categoryBrandMerchantService = new CategoryBrandMerchantService(categoryBrandMerchantRepository,
                categoryBrandMerchantIndexService, categoryService, brandService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnMerchantCount_When_ValidCategoryIdAndUserTypeAreProvided() {
        when(categoryBrandMerchantRepository
                .countDistinctMerchantIdByCategoriesAndUserType(CATEGORY_ID, UserType.MERCHANT))
                .thenReturn(MERCHANT_COUNT);
        assertEquals(MERCHANT_COUNT, categoryBrandMerchantService
                .getMerchantCountByCategoryIdAndUserType(CATEGORY_ID, UserType.MERCHANT));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetMerchantCountByCategoryIdAndUserTypeIsFailed() {
        when(categoryBrandMerchantRepository
                .countDistinctMerchantIdByCategoriesAndUserType(CATEGORY_ID, UserType.MERCHANT))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryBrandMerchantService
                        .getMerchantCountByCategoryIdAndUserType(CATEGORY_ID, UserType.MERCHANT)
        );
        assertEquals("Reading merchant count from the database by category id was failed",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnActiveMerchantCount_When_ValidCategoryIdAndUserTypeAreProvided() {
        when(categoryBrandMerchantRepository
                .countDistinctMerchantIdByCategoriesAndIsActiveMerchantTrueAndUserType(CATEGORY_ID, UserType.MERCHANT))
                .thenReturn(ACTIVE_MERCHANT_COUNT);
        assertEquals(ACTIVE_MERCHANT_COUNT, categoryBrandMerchantService
                .getActiveMerchantCountByCategoryIdAndUserType(CATEGORY_ID, UserType.MERCHANT));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetActiveMerchantCountByCategoryIdAndUserTypeIsFailed() {
        when(categoryBrandMerchantRepository
                .countDistinctMerchantIdByCategoriesAndIsActiveMerchantTrueAndUserType(CATEGORY_ID, UserType.MERCHANT))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryBrandMerchantService
                        .getActiveMerchantCountByCategoryIdAndUserType(CATEGORY_ID, UserType.MERCHANT)
        );
        assertEquals("Reading active merchant count from the database by category id was failed",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnTrue_When_CategoryAssociateWithCategoryBranchMerchant() {
        when(categoryBrandMerchantRepository.existsAllByCategories(CATEGORY_ID)).thenReturn(true);
        assertTrue(categoryBrandMerchantService.checkCategoryIdMapWithCategoryBrandMerchant(CATEGORY_ID));
    }

    @Test
    void Should_ThrowKupiCoreException_When_CheckCategoryIdMapWithCategoryBrandMerchantIsFailed() {
        when(categoryBrandMerchantRepository.existsAllByCategories(CATEGORY_ID))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryBrandMerchantService.checkCategoryIdMapWithCategoryBrandMerchant(CATEGORY_ID)
        );
        assertEquals("Checking category id is associated with categoryBrandMerchant" +
                " from the database was failed", kupiCoreException.getMessage());
    }

    /**
     * Update categories brands of merchant
     */
    @Test
    void Should_ReturnCategoryBrandMerchant_When_UpdateCategoriesBrandsOfMerchantForCategoryBrandMerchantIsPresent() {
        CategoryBrandMerchant categoryBrandMerchant = new CategoryBrandMerchant();
        categoryBrandMerchant.setMerchantId(MERCHANT_ID);
        when(categoryBrandMerchantRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional
                .of(categoryBrandMerchant));
        doNothing().when(categoryBrandMerchantIndexService).updateCategoryBrandMerchantIndexByMerchantId(
                eq(MERCHANT_ID), anyString(), anyString(), anyBoolean(), any(ApprovalStatus.class));
        when(categoryBrandMerchantRepository.save(categoryBrandMerchant)).thenReturn(categoryBrandMerchant);
        categoryBrandMerchantService.updateCategoriesBrandsOfMerchant(categoryBrandMerchant,
                new BasicMerchantBusinessResponseDto());
        verify(categoryBrandMerchantRepository).save(categoryBrandMerchant);
    }

    @Test
    void Should_ReturnCategoryBrandMerchant_When_UpdateCategoriesBrandsOfMerchantForCategoryBrandMerchantIsAbsent() {
        CategoryBrandMerchant categoryBrandMerchant = new CategoryBrandMerchant();
        when(categoryBrandMerchantRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.empty());
        doNothing().when(categoryBrandMerchantIndexService).save(any(CategoryBrandMerchantIndex.class));
        when(categoryBrandMerchantRepository.save(categoryBrandMerchant)).thenReturn(categoryBrandMerchant);
        categoryBrandMerchantService.updateCategoriesBrandsOfMerchant(categoryBrandMerchant,
                new BasicMerchantBusinessResponseDto());
        verify(categoryBrandMerchantRepository).save(categoryBrandMerchant);
    }

    @Test
    void Should_ThrowKupiCoreException_When_UpdateCategoriesBrandsOfMerchantForFailedToSaveCategoryMerchantToDB() {
        CategoryBrandMerchant categoryBrandMerchant = new CategoryBrandMerchant();
        BasicMerchantBusinessResponseDto basicMerchantBusinessResponseDto = new BasicMerchantBusinessResponseDto();
        when(categoryBrandMerchantRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.empty());
        when(categoryBrandMerchantRepository.save(categoryBrandMerchant)).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () ->
                categoryBrandMerchantService.updateCategoriesBrandsOfMerchant(categoryBrandMerchant,
                        basicMerchantBusinessResponseDto));
        assertEquals("Saving category merchant into database was failed.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_UpdatingCategoriesBrandsOfMerchantForFailedToUpdateCategoriesAndBrandsOfMerchantInDB() {
        CategoryBrandMerchant categoryBrandMerchant = new CategoryBrandMerchant();
        categoryBrandMerchant.setMerchantId(MERCHANT_ID);
        BasicMerchantBusinessResponseDto basicMerchantBusinessResponseDto = new BasicMerchantBusinessResponseDto();
        when(categoryBrandMerchantRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional
                .of(categoryBrandMerchant));
        doNothing().when(categoryBrandMerchantIndexService).updateCategoryBrandMerchantIndexByMerchantId(
                eq(MERCHANT_ID), anyString(), anyString(), anyBoolean(), any(ApprovalStatus.class));
        when(categoryBrandMerchantRepository.save(categoryBrandMerchant)).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () ->
                categoryBrandMerchantService.updateCategoriesBrandsOfMerchant(categoryBrandMerchant,
                        basicMerchantBusinessResponseDto));
        assertEquals("Updating Categories & brands of merchant in database was failed.", exception.getMessage());
    }

    /**
     * Get brands category mapper by merchantId
     */
    @Test
    void Should_ReturnOptionalCategoryBrandMerchant_When_GettingBrandsCategoryMapperByMerchantIdForCategoryBrandMerchantIsPresent() {
        Optional<CategoryBrandMerchant> optionalCategoryBrandMerchant = Optional.of(new CategoryBrandMerchant());
        when(categoryBrandMerchantRepository.findByMerchantId(anyString())).thenReturn(optionalCategoryBrandMerchant);
        assertEquals(optionalCategoryBrandMerchant, categoryBrandMerchantService
                .getBrandsCategoryMapperByMerchantId(MERCHANT_ID));
    }

    @Test
    void Should_ReturnOptionalCategoryBrandMerchant_When_GettingBrandsCategoryMapperByMerchantIdForCategoryBrandMerchantIsAbsent() {
        Optional<CategoryBrandMerchant> optionalCategoryBrandMerchant = Optional.empty();
        when(categoryBrandMerchantRepository.findByMerchantId(anyString())).thenReturn(optionalCategoryBrandMerchant);
        assertEquals(optionalCategoryBrandMerchant, categoryBrandMerchantService
                .getBrandsCategoryMapperByMerchantId(MERCHANT_ID));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingBrandsCategoryMapperByMerchantIdIsFailed() {
        when(categoryBrandMerchantRepository.findByMerchantId(anyString())).thenThrow(new DataAccessException("failed") {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () ->
                categoryBrandMerchantService.getBrandsCategoryMapperByMerchantId(MERCHANT_ID));
        assertEquals("Reading brandsCategoriesForMerchant from the database was failed.", exception.getMessage());
    }

    /**
     * Find all merchants by categoryId
     */
    @Test
    void Should_ReturnMerchantIdList_When_FindingAllMerchantsByCategoryIdForActiveMerchant() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantRepository.findAllByCategoriesAndIsActiveMerchant(any(Pageable.class),
                eq(CATEGORY_ID), eq(true))).thenReturn(getSampleCategoryBrandMerchantPage());

        assertEquals(MERCHANT_ID, categoryBrandMerchantService.findAllMerchantsByCategoryId(pageable,
                CATEGORY_ID, true).get(0));
    }

    @Test
    void Should_ReturnMerchantIdList_When_FindingAllMerchantsByCategoryIdForActiveMerchantForAllCategory() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantRepository.findAllByIsActiveMerchant(any(Pageable.class), eq(true)))
                .thenReturn(getSampleCategoryBrandMerchantPage());

        assertEquals(MERCHANT_ID, categoryBrandMerchantService.findAllMerchantsByCategoryId(pageable,
                ALL, true).get(0));
    }

    @Test
    void Should_ReturnMerchantIdList_When_FindingAllMerchantsByCategoryIdForNotActiveMerchant() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantRepository.findAllByCategories(any(Pageable.class), eq(CATEGORY_ID)))
                .thenReturn(getSampleCategoryBrandMerchantPage());

        assertEquals(MERCHANT_ID, categoryBrandMerchantService.findAllMerchantsByCategoryId(pageable,
                CATEGORY_ID, false).get(0));
    }

    @Test
    void Should_ReturnMerchantIdList_When_FindingAllMerchantsByCategoryIdForNotActiveMerchantForAllCategory() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantRepository.findAll(any(Pageable.class)))
                .thenReturn(getSampleCategoryBrandMerchantPage());

        assertEquals(MERCHANT_ID, categoryBrandMerchantService.findAllMerchantsByCategoryId(pageable,
                ALL, false).get(0));
    }

    @Test
    void Should_ThrowKupiCoreException_When_FindingAllMerchantsByCategoryIdIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantRepository.findAllByCategoriesAndIsActiveMerchant(any(Pageable.class),
                eq(CATEGORY_ID), eq(true))).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () ->
                categoryBrandMerchantService.findAllMerchantsByCategoryId(pageable, CATEGORY_ID, true));
        assertEquals("Reading merchantIds from database was failed.", exception.getMessage());
    }

    /**
     * Get active categories by ids and searchTerm
     */
    @Test
    void Should_ReturnCategoryPage_When_GettingActiveCategoriesByIdsAndSearchTermForActive() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryService.getBulkActiveCategoriesByIds(pageable, new HashSet<>(), SEARCH_TERM))
                .thenReturn(getSampleCategoryPage());
        assertEquals(CATEGORY_ID, categoryBrandMerchantService.getActiveCategoriesByIdsAndSearchTerm(pageable,
                new HashSet<>(), SEARCH_TERM, true).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnCategoryPage_When_GettingActiveCategoriesByIdsAndSearchTermForNonActive() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryService.getBulkCategoriesByIds(pageable, new HashSet<>(), SEARCH_TERM))
                .thenReturn(getSampleCategoryPage());
        assertEquals(CATEGORY_ID, categoryBrandMerchantService.getActiveCategoriesByIdsAndSearchTerm(pageable,
                new HashSet<>(), SEARCH_TERM, false).getContent().get(0).getId());
    }

    /**
     * Get brands by ids
     */
    @Test
    void Should_ReturnBrandPage() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(brandService.getBulkBrandsByIds(any(Pageable.class), anySet(), anyString()))
                .thenReturn(getSampleBrandPage());
        assertEquals(BRAND_ID, categoryBrandMerchantService.getBrandsByIds(pageable, new HashSet<>(), SEARCH_TERM)
                .getContent().get(0).getId());
    }

    @Test
    void Should_ReturnTrue_When_GivenBrandIdMapWithCategoryBrandMerchant() {
        when(categoryBrandMerchantRepository.existsByBrands(BRAND_ID)).thenReturn(true);
        assertTrue(categoryBrandMerchantService.checkBrandIdMapWithCategoryBrandMerchant(BRAND_ID));
    }

    @Test
    void Should_ReturnFalse_When_GivenBrandIdNotMapWithCategoryBrandMerchant() {
        when(categoryBrandMerchantRepository.existsByBrands(BRAND_ID)).thenReturn(false);
        assertFalse(categoryBrandMerchantService.checkBrandIdMapWithCategoryBrandMerchant(BRAND_ID));
    }

    @Test
    void Should_ThrowKupiCoreException_When_CheckBrandIdMapWIthCategoryBrandMerchantIsFailed() {
        when(categoryBrandMerchantRepository.existsByBrands(BRAND_ID)).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                categoryBrandMerchantService.checkBrandIdMapWithCategoryBrandMerchant(BRAND_ID));
        assertEquals("Checking brand id is associated with categoryBrandMerchant from the database was failed",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnMerchantCount_When_UserTypeIsMerchantForGetMerchantCountByBrandIdAndUserType() {
        when(categoryBrandMerchantRepository.countDistinctMerchantIdByBrandsAndUserType(BRAND_ID, UserType.MERCHANT))
                .thenReturn(MERCHANT_COUNT);
        assertEquals(MERCHANT_COUNT,
                categoryBrandMerchantService.getMerchantCountByBrandIdAndUserType(BRAND_ID, UserType.MERCHANT));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetMerchantCountFromDataBaseIsFailed() {
        when(categoryBrandMerchantRepository.countDistinctMerchantIdByBrandsAndUserType(BRAND_ID, UserType.MERCHANT))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () ->
                categoryBrandMerchantService.getMerchantCountByBrandIdAndUserType(BRAND_ID, UserType.MERCHANT));
        assertEquals("Reading merchant count from the database by Brand id was failed", exception.getMessage());
    }

    @Test
    void Should_ReturnActiveMerchantCount_When_UserTypeIsMerchantForGetActiveMerchantCountByBrandIdAndUserType() {
        when(categoryBrandMerchantRepository.countDistinctMerchantIdByBrandsAndIsActiveMerchantTrueAndUserType(BRAND_ID, UserType.MERCHANT))
                .thenReturn(MERCHANT_COUNT);
        assertEquals(MERCHANT_COUNT,
                categoryBrandMerchantService.getActiveMerchantCountByBrandIdAndUserType(BRAND_ID, UserType.MERCHANT));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetActiveMerchantCountFromDataBaseIsFailed() {
        when(categoryBrandMerchantRepository.countDistinctMerchantIdByBrandsAndIsActiveMerchantTrueAndUserType(BRAND_ID, UserType.MERCHANT))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () ->
                categoryBrandMerchantService.getActiveMerchantCountByBrandIdAndUserType(BRAND_ID, UserType.MERCHANT));
        assertEquals("Reading active merchant count from the database by brand id was failed", exception.getMessage());
    }

    /**
     * Update merchant info
     */
    @Test
    void Should_UpdateMerchantInfo_When_UpdatingMerchantInfoIsSuccessful() {
        when(categoryBrandMerchantRepository.getAllByMerchantIdIn(anyList()))
                .thenReturn(getSampleCategoryBrandMerchantList());
        categoryBrandMerchantService.updateMerchantInfo(getSampleMerchantBusinessResponseDtoMap());
        verify(categoryBrandMerchantRepository).saveAll(anyList());
    }

    @Test
    void Should_ThrowKupiCoreException_When_UpdatingMerchantInfoIsFailed() {
        Map<String, MerchantBusinessResponseDto> merchantBusinessResponseDtoMap =
                getSampleMerchantBusinessResponseDtoMap();

        when(categoryBrandMerchantRepository.getAllByMerchantIdIn(anyList()))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> categoryBrandMerchantService
                .updateMerchantInfo(merchantBusinessResponseDtoMap));
        assertEquals("Updating merchant info for categoryBrandMerchant was failed", exception.getMessage());
    }

    /**
     * This method returns a sample CategoryBrandMerchant
     *
     * @return CategoryBrandMerchant
     */
    private CategoryBrandMerchant getSampleCategoryBrandMerchant() {
        CategoryBrandMerchant categoryBrandMerchant = new CategoryBrandMerchant();
        categoryBrandMerchant.setMerchantId(MERCHANT_ID);
        return categoryBrandMerchant;
    }

    /**
     * This method returns a sample CategoryBrandMerchant list
     *
     * @return CategoryBrandMerchant List
     */
    private List<CategoryBrandMerchant> getSampleCategoryBrandMerchantList() {
        List<CategoryBrandMerchant> categoryBrandMerchantList = new ArrayList<>();
        categoryBrandMerchantList.add(getSampleCategoryBrandMerchant());
        return categoryBrandMerchantList;
    }

    /**
     * This method returns a sample CategoryBrandMerchant page
     *
     * @return CategoryBrandMerchant page
     */
    private Page<CategoryBrandMerchant> getSampleCategoryBrandMerchantPage() {
        List<String> merchantIdList = new ArrayList<>();
        merchantIdList.add(getSampleCategoryBrandMerchant().getMerchantId());
        return new PageImpl<>(getSampleCategoryBrandMerchantList());
    }

    /**
     * This method returns a sample Category page
     *
     * @return Category page
     */
    private Page<Category> getSampleCategoryPage() {
        var category = new Category();
        category.setId(CATEGORY_ID);
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(category);
        return new PageImpl<>(categoryList);
    }

    /**
     * This method returns a sample Brand page
     *
     * @return Brand page
     */
    private Page<Brand> getSampleBrandPage() {
        var brand = new Brand();
        brand.setId(BRAND_ID);
        var brandList = new ArrayList<Brand>();
        brandList.add(brand);
        return new PageImpl<>(brandList);
    }

    /**
     * This method returns a sample MerchantBusinessResponseDto map
     *
     * @return MerchantBusinessResponseDto Map
     */
    private Map<String, MerchantBusinessResponseDto> getSampleMerchantBusinessResponseDtoMap() {
        Map<String, MerchantBusinessResponseDto> map = new HashMap<>();
        map.put(MERCHANT_ID, getSampleMerchantBusinessResponseDto());
        return map;
    }

    /**
     * This method returns a sample MerchantBusinessResponseDto
     *
     * @return MerchantBusinessResponseDto
     */
    private MerchantBusinessResponseDto getSampleMerchantBusinessResponseDto() {
        MerchantBusinessResponseDto dto = new MerchantBusinessResponseDto();
        dto.setApprovalStatus(ApprovalStatus.APPROVED);
        dto.setActive(true);
        return dto;
    }
}