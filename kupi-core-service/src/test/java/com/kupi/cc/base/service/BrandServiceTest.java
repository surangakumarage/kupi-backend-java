package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Brand;
import com.kupi.cc.base.domain.request.BrandUpdateRequestDto;
import com.kupi.cc.base.exception.InvalidBrandException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.exception.UnsupportedDeleteAction;
import com.kupi.cc.base.repository.BrandRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link BrandService} class
 */
class BrandServiceTest {

    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final String SEARCH_TERM = "SEARCH_TERM";
    private static final String ALL = "ALL";
    private static final String UPDATED_AT = "updatedAt";
    private static final String BRAND_ID = "did-02a2022f-98c5-4237-a9c7-c1faf989a4a5";
    private static final String BRAND_NAME = "DELL";
    private static final String ERROR = "ERROR";
    private BrandService brandService;
    @Mock
    private BrandRepository brandRepository;
    @Mock
    private CategoryBrandMerchantService categoryBrandMerchantService;
    @Mock
    private BrandUpdateService brandUpdateService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        brandService = new BrandService(brandRepository, categoryBrandMerchantService, brandUpdateService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ValidateCategoryId() {
        Brand brand = getSampleBrand();
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
        brandService.validateBrandId(BRAND_ID);
        verify(brandRepository, times(1)).findById(BRAND_ID);
    }

    @Test
    void Should_ThrowInvalidCategoryException_When_InvalidCategoryIdIsProvidedForValidateCategoryId() {
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.empty());
        InvalidBrandException invalidBrandException = assertThrows(InvalidBrandException.class, () ->
                brandService.validateBrandId(BRAND_ID));
        assertEquals("Invalid brand Id:" + BRAND_ID, invalidBrandException.getMessage());
    }

    @Test
    void Should_ReturnBrandSet_When_ValidBrandIdListIsProvided() {
        List<String> brandIdList = getSampleBrandIdList();
        Set<Brand> brandSet = getSampleBrandSet();
        Brand brand = getSampleBrand();
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
        assertEquals(brandSet.stream().findFirst().get().getId(),
                brandService.getBrandSetByIdList(brandIdList).stream().findFirst().get().getId());
    }

    @Test
    void Should_ThrowInvalidBrandException_When_InvalidBrandIdIsProvided() {
        List<String> brandIdList = getSampleBrandIdList();
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.empty());
        InvalidBrandException invalidBrandException = assertThrows(InvalidBrandException.class, () ->
                brandService.getBrandSetByIdList(brandIdList));
        assertEquals("Invalid brand Id:" + BRAND_ID, invalidBrandException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetBrandSetByIdListIsFailed() {
        List<String> brandIdList = getSampleBrandIdList();
        when(brandRepository.findById(BRAND_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                brandService.getBrandSetByIdList(brandIdList));
        assertEquals("Reading brand from database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnBrandPage_When_SearchTermIsEqualToAllForListAllBrands() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.Direction.DESC, UPDATED_AT);
        Page<Brand> brandPage = getSampleBrandPage();
        when(brandRepository.findAll(pageable)).thenReturn(brandPage);
        assertEquals(brandPage, brandService.listAllBrands(pageable, ALL));
    }

    @Test
    void Should_ReturnBrandPage_When_SearchTermIsNotEqualToAllForListAllBrands() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.Direction.DESC, UPDATED_AT);
        Page<Brand> brandPage = getSampleBrandPage();
        when(brandRepository.findAllByNameContaining(pageable, SEARCH_TERM)).thenReturn(brandPage);
        assertEquals(brandPage, brandService.listAllBrands(pageable, SEARCH_TERM));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetBrandPageFromDataBaseIsFailedForListAllBrands() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.Direction.DESC, UPDATED_AT);
        Page<Brand> brandPage = getSampleBrandPage();
        when(brandRepository.findAll(pageable)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                brandService.listAllBrands(pageable, ALL));
        assertEquals("Read brands from database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_CreateBrand() {
        Brand brand = getSampleBrand();
        brandService.createBrand(brand);
        verify(brandRepository, times(1)).save(brand);
    }

    @Test
    void Should_ThrowKupiCoreException_When_SaveBrandInDataBaseIsFailed() {
        Brand brand = getSampleBrand();
        when(brandRepository.save(brand)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                brandService.createBrand(brand));
        assertEquals("Saving brand into database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnTrue_When_GivenBrandNameAlreadyExists() {
        when(brandRepository.existsByName(BRAND_NAME)).thenReturn(true);
        assertTrue(brandService.checkBrandNameExist(BRAND_NAME));
    }

    @Test
    void Should_ReturnFalse_When_GivenBrandNameNotExistsInDataBase() {
        when(brandRepository.existsByName(BRAND_NAME)).thenReturn(false);
        assertFalse(brandService.checkBrandNameExist(BRAND_NAME));
    }

    @Test
    void Should_ThrowKupiCoreException_When_CheckingBrandNameInDataBaseIsFailed() {
        when(brandRepository.existsByName(BRAND_NAME)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                brandService.checkBrandNameExist(BRAND_NAME));
        assertEquals("Checking Brand Name is exist from the database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_DeleteBrand_When_BrandNotMapWIthCategoryBrandMerchant() {
        Brand brand = getSampleBrand();
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
        when(categoryBrandMerchantService.checkBrandIdMapWithCategoryBrandMerchant(BRAND_ID)).thenReturn(false);
        brandService.deleteBrand(BRAND_ID);
        verify(brandRepository).delete(brand);
    }

    @Test
    void Should_ThrowUnsupportedDeleteAction_When_BrandMapWIthCategoryBrandMerchant() {
        Brand brand = getSampleBrand();
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
        when(categoryBrandMerchantService.checkBrandIdMapWithCategoryBrandMerchant(BRAND_ID)).thenReturn(true);
        UnsupportedDeleteAction unsupportedDeleteAction = assertThrows(UnsupportedDeleteAction.class, () ->
                brandService.deleteBrand(BRAND_ID));
        assertEquals("Brand cannot be deleted.", unsupportedDeleteAction.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_DeleteBrandIsFailed() {
        Brand brand = getSampleBrand();
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
        when(categoryBrandMerchantService.checkBrandIdMapWithCategoryBrandMerchant(BRAND_ID)).thenReturn(false);
        doThrow(new DataAccessException("ERROR") {
        }).when(brandRepository).delete(brand);
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                brandService.deleteBrand(BRAND_ID));
        assertEquals("Deleting brand from the database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnBrand_When_ValidBrandIdIsProvidedForGetBrandById() {
        Brand brand = getSampleBrand();
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
        assertEquals(brand, brandService.getBrandById(BRAND_ID));
    }

    @Test
    void Should_ThrowInvalidBrandException_When_InvalidBrandIdIsProvidedForGetBrandById() {
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.empty());
        InvalidBrandException invalidBrandException = assertThrows(InvalidBrandException.class, () ->
                brandService.getBrandById(BRAND_ID));
        assertEquals("Invalid brand Id:" + BRAND_ID, invalidBrandException.getMessage());
    }

    @Test
    void Should_ThrowInvalidBrandException_When_GetBrandFromDataBaseIsFailed() {
        when(brandRepository.findById(BRAND_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                brandService.getBrandById(BRAND_ID));
        assertEquals("Reading brand from database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnTrue_When_GivenBrandNameIsExistWithOtherBrand() {
        when(brandRepository.existsByNameAndIdNot(BRAND_NAME, BRAND_ID)).thenReturn(true);
        assertTrue(brandService.checkBrandNameNotExistForOtherIDs(BRAND_ID, BRAND_NAME));
    }

    @Test
    void Should_ReturnFalse_When_GivenBrandNameIsNotExistWithOtherBrand() {
        when(brandRepository.existsByNameAndIdNot(BRAND_NAME, BRAND_ID)).thenReturn(false);
        assertFalse(brandService.checkBrandNameNotExistForOtherIDs(BRAND_ID, BRAND_NAME));
    }

    @Test
    void Should_ThrowKupiCoreException_When_CheckBrandNameWithOtherBrandIdIsFailed() {
        when(brandRepository.existsByNameAndIdNot(BRAND_NAME, BRAND_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                brandService.checkBrandNameNotExistForOtherIDs(BRAND_ID, BRAND_NAME));
        assertEquals("Checking Brand Name is exists for other id(s) from the database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnBrand_When_BrandSuccessfullyUpdated() {
        BrandUpdateRequestDto brandUpdateRequestDto = getSampleBrandUpdateRequestDto();
        Brand brand = getSampleBrand();
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
        when(brandRepository.save(any(Brand.class))).thenReturn(brand);
        assertEquals(brand, brandService.updateBrand(brandUpdateRequestDto));
    }

    @Test
    void Should_ThrowInvalidBrandException_When_InvalidBrandIdIsProvidedForUpdateBrand() {
        BrandUpdateRequestDto brandUpdateRequestDto = getSampleBrandUpdateRequestDto();
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.empty());
        InvalidBrandException invalidBrandException = assertThrows(InvalidBrandException.class, () ->
                brandService.updateBrand(brandUpdateRequestDto));
        assertEquals("Invalid brand Id:" + BRAND_ID, invalidBrandException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetBrandFromDataBaseIsFailed() {
        BrandUpdateRequestDto brandUpdateRequestDto = getSampleBrandUpdateRequestDto();
        when(brandRepository.findById(BRAND_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                brandService.updateBrand(brandUpdateRequestDto));
        assertEquals("Reading brand from database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_SavingUpdatedBrandIntoDataBaseIsFailed() {
        BrandUpdateRequestDto brandUpdateRequestDto = getSampleBrandUpdateRequestDto();
        Brand brand = getSampleBrand();
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
        when(brandRepository.save(any(Brand.class))).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                brandService.updateBrand(brandUpdateRequestDto));
        assertEquals("Updating brand by id: " + brandUpdateRequestDto.getId() + " from database was failed.",
                kupiCoreException.getMessage());
    }

    /**
     * Start of tests for get bulk brands by ids
     */
    @Test
    void Should_ReturnBrandPage_When_GettingBulkBrandsByIdsForAllSearchTermIsSuccessful() {
        Page<Brand> brandPage = getSampleBrandPage();

        when(brandRepository.findByBrandIds(any(Pageable.class), anySet())).thenReturn(brandPage);
        assertEquals(brandPage, brandService.getBulkBrandsByIds(getSamplePageable(), getSampleStringSet(), ALL));
    }

    @Test
    void Should_ReturnBrandPage_When_GettingBulkBrandsByIdsForSearchTermIsSuccessful() {
        Page<Brand> brandPage = getSampleBrandPage();

        when(brandRepository.findByBrandIdsBySearch(any(Pageable.class), anySet(), anyString())).thenReturn(brandPage);
        assertEquals(brandPage, brandService.getBulkBrandsByIds(getSamplePageable(), getSampleStringSet(), SEARCH_TERM));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingBulkBrandsByIdsIsFailed() {
        Pageable pageable = getSamplePageable();
        Set<String> stringSet = getSampleStringSet();

        when(brandRepository.findByBrandIdsBySearch(any(Pageable.class), anySet(), anyString()))
                .thenThrow(new DataAccessException(ERROR) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> brandService
                .getBulkBrandsByIds(pageable, stringSet, SEARCH_TERM));
        assertEquals("Returning bulk Brand by Id(s) from database was failed.", exception.getMessage());
    }

    /**
     * This method creates sample brand
     *
     * @return Brand
     */
    private Brand getSampleBrand() {
        Brand brand = new Brand();
        brand.setId(BRAND_ID);
        brand.setName("Brand name");
        brand.setDescription("Brand description");
        brand.setImageUrl("https://objects-kupi-dev.s3.ap-southeast-1.amazonaws.com/fid-3338ebed-e3d3-4f86-8b3e-92887f27e51d.jpg");
        return brand;
    }

    /**
     * This method creates sample brand list
     *
     * @return brandList
     */
    private List<String> getSampleBrandIdList() {
        List<String> brandIdList = new ArrayList<>();
        brandIdList.add(BRAND_ID);
        return brandIdList;
    }

    /**
     * This method creates sample brand set
     *
     * @return brandSet
     */
    private Set<Brand> getSampleBrandSet() {
        Brand brand = getSampleBrand();
        Set<Brand> brandSet = new HashSet<>();
        brandSet.add(brand);
        return brandSet;
    }

    /**
     * This method creates BrandPage
     *
     * @return BrandPage
     */
    private Page<Brand> getSampleBrandPage() {
        Brand brand = getSampleBrand();
        List<Brand> brandList = new ArrayList<>();
        brandList.add(brand);
        return new PageImpl<>(brandList);
    }

    /**
     * This method creates sample BrandUpdateRequestDto
     *
     * @return BrandUpdateRequestDto
     */
    private BrandUpdateRequestDto getSampleBrandUpdateRequestDto() {
        BrandUpdateRequestDto brandUpdateRequestDto = new BrandUpdateRequestDto();
        brandUpdateRequestDto.setId(BRAND_ID);
        brandUpdateRequestDto.setName("Brand name");
        brandUpdateRequestDto.setDescription("Brand description");
        brandUpdateRequestDto.setImageUrl("https://objects-kupi-dev.s3.ap-southeast-1.amazonaws.com/fid-3338ebed-e3d3-4f86-8b3e-92887f27e51d.jpg");
        return brandUpdateRequestDto;
    }

    /**
     * This method returns a sample Pageable object
     *
     * @return Pageable
     */
    private Pageable getSamplePageable() {
        return PageRequest.of(PAGE, SIZE);
    }

    /**
     * This method returns a sample string set
     *
     * @return Set of Strings
     */
    private Set<String> getSampleStringSet() {
        Set<String> stringSet = new HashSet<>();
        stringSet.add(BRAND_ID);
        return stringSet;
    }
}