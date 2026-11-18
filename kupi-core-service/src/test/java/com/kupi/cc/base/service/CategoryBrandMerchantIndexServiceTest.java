package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.CategoryBrandMerchantIndex;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.InvalidUserException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.CategoryBrandMerchantIndexRepository;
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
 * This class tests {@link CategoryBrandMerchantIndexService} class
 */
class CategoryBrandMerchantIndexServiceTest {

    private static final String CATEGORY_BRAND_MERCHANT_INDEX_ID = "cbmid-123456789";
    private static final String MERCHANT_ID = "uid-123456789";
    private static final String CATEGORY_ID = "cid-123456789";
    private static final String BRAND_ID = "bid-123456789";
    private static final String SEARCH_TERM = "Softlogic";
    private static final String BUSINESS_NAME = "Computing Solutions";
    private static final String BUSINESS_IMAGE_URL = "BUSINESS_IMAGE_URL";
    private static final String ALL = "ALL";
    private static final String FAILED = "failed";
    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final String DEFAULT_SORT = "updatedAt";
    private static final UserType USER_TYPE_BANK = UserType.BANK;

    @Mock
    private CategoryBrandMerchantIndexRepository categoryBrandMerchantIndexRepository;
    private CategoryBrandMerchantIndexService categoryBrandMerchantIndexService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        categoryBrandMerchantIndexService = new CategoryBrandMerchantIndexService(categoryBrandMerchantIndexRepository);
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Create category brand merchant index
     */
    @Test
    void Should_SaveCategoryBrandMerchantIndex_When_SavingCategoryBrandMerchantIndexIsSuccessful() {
        when(categoryBrandMerchantIndexRepository.save(any(CategoryBrandMerchantIndex.class)))
                .thenReturn(getSampleCategoryBrandMerchantIndex());
        categoryBrandMerchantIndexService.save(getSampleCategoryBrandMerchantIndex());
        verify(categoryBrandMerchantIndexRepository).save(any(CategoryBrandMerchantIndex.class));
    }

    @Test
    void Should_ThrowKupiCoreException_When_SavingCategoryBrandMerchantIndexIsFailed() {
        CategoryBrandMerchantIndex categoryBrandMerchantIndex = getSampleCategoryBrandMerchantIndex();

        when(categoryBrandMerchantIndexRepository.save(any(CategoryBrandMerchantIndex.class)))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> categoryBrandMerchantIndexService
                .save(categoryBrandMerchantIndex));
        assertEquals("Saving categoryBrandMerchantIndex to database was failed", exception.getMessage());
    }

    /**
     * Get category brand merchant index
     */
    @Test
    void Should_ReturnCategoryBrandMerchantIndexPage_When_GettingCategoryBrandMerchantIndexForActiveMerchantCategoryIdAndSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantIndexRepository.findAllByCategoryIdAndSearchTermAndActiveMerchant(any(Pageable.class),
                anyString(), anyString(), any(UserType.class))).thenReturn(getSampleCategoryBrandMerchantIndexPage());
        assertEquals(CATEGORY_BRAND_MERCHANT_INDEX_ID, categoryBrandMerchantIndexService.getCategoryBrandMerchantIndex(
                pageable, CATEGORY_ID, SEARCH_TERM, true, USER_TYPE_BANK).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnCategoryBrandMerchantIndexPage_When_GettingCategoryBrandMerchantIndexForActiveMerchantCategoryIdAndAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantIndexRepository.findAllByCategoryIdAndActiveMerchant(any(Pageable.class),
                anyString(), any(UserType.class))).thenReturn(getSampleCategoryBrandMerchantIndexPage());
        assertEquals(CATEGORY_BRAND_MERCHANT_INDEX_ID, categoryBrandMerchantIndexService.getCategoryBrandMerchantIndex(
                pageable, CATEGORY_ID, ALL, true, USER_TYPE_BANK).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnCategoryBrandMerchantIndexPage_When_GettingCategoryBrandMerchantIndexForActiveMerchantAllCategoryIdAndSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantIndexRepository.findAllBySearchAndActiveMerchant(any(Pageable.class),
                anyString(), any(UserType.class))).thenReturn(getSampleCategoryBrandMerchantIndexPage());
        assertEquals(CATEGORY_BRAND_MERCHANT_INDEX_ID, categoryBrandMerchantIndexService.getCategoryBrandMerchantIndex(
                pageable, ALL, SEARCH_TERM, true, USER_TYPE_BANK).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnCategoryBrandMerchantIndexPage_When_GettingCategoryBrandMerchantIndexForActiveMerchantAllCategoryIdAndAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantIndexRepository.findAllCategoryBrandMerchantIndexByActiveMerchant(any(Pageable.class),
                any(UserType.class))).thenReturn(getSampleCategoryBrandMerchantIndexPage());
        assertEquals(CATEGORY_BRAND_MERCHANT_INDEX_ID, categoryBrandMerchantIndexService.getCategoryBrandMerchantIndex(
                pageable, ALL, ALL, true, USER_TYPE_BANK).getContent().get(0).getId());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingCategoryBrandMerchantIndexForActiveMerchantFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantIndexRepository.findAllByCategoryIdAndSearchTermAndActiveMerchant(any(Pageable.class),
                anyString(), anyString(), any(UserType.class))).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> categoryBrandMerchantIndexService
                .getCategoryBrandMerchantIndex(pageable, CATEGORY_ID, SEARCH_TERM, true, USER_TYPE_BANK));
        assertEquals("Reading categoryBrandMerchantIndex from database was failed", exception.getMessage());
    }

    @Test
    void Should_ReturnCategoryBrandMerchantIndexPage_When_GettingCategoryBrandMerchantIndexForNonActiveMerchantCategoryIdAndSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantIndexRepository.findAllByCategoryIdAndSearchTerm(any(Pageable.class), anyString(),
                anyString(), any(UserType.class))).thenReturn(getSampleCategoryBrandMerchantIndexPage());
        assertEquals(CATEGORY_BRAND_MERCHANT_INDEX_ID, categoryBrandMerchantIndexService.getCategoryBrandMerchantIndex(
                pageable, CATEGORY_ID, SEARCH_TERM, false, USER_TYPE_BANK).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnCategoryBrandMerchantIndexPage_When_GettingCategoryBrandMerchantIndexForNonActiveMerchantCategoryIdAndAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantIndexRepository.findAllByCategoryId(any(Pageable.class), anyString(),
                any(UserType.class))).thenReturn(getSampleCategoryBrandMerchantIndexPage());
        assertEquals(CATEGORY_BRAND_MERCHANT_INDEX_ID, categoryBrandMerchantIndexService.getCategoryBrandMerchantIndex(
                pageable, CATEGORY_ID, ALL, false, USER_TYPE_BANK).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnCategoryBrandMerchantIndexPage_When_GettingCategoryBrandMerchantIndexForNonActiveMerchantAllCategoryIdAndSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantIndexRepository.findAllBySearch(any(Pageable.class), anyString(), any(UserType.class)))
                .thenReturn(getSampleCategoryBrandMerchantIndexPage());
        assertEquals(CATEGORY_BRAND_MERCHANT_INDEX_ID, categoryBrandMerchantIndexService.getCategoryBrandMerchantIndex(
                pageable, ALL, SEARCH_TERM, false, USER_TYPE_BANK).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnCategoryBrandMerchantIndexPage_When_GettingCategoryBrandMerchantIndexForNonActiveMerchantAllCategoryIdAndAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantIndexRepository.findAllCategoryBrandMerchantIndex(any(Pageable.class),
                any(UserType.class))).thenReturn(getSampleCategoryBrandMerchantIndexPage());
        assertEquals(CATEGORY_BRAND_MERCHANT_INDEX_ID, categoryBrandMerchantIndexService.getCategoryBrandMerchantIndex(
                pageable, ALL, ALL, false, USER_TYPE_BANK).getContent().get(0).getId());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingCategoryBrandMerchantIndexForNonActiveMerchantFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantIndexRepository.findAllByCategoryIdAndSearchTerm(any(Pageable.class), anyString(),
                anyString(), any(UserType.class))).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> categoryBrandMerchantIndexService
                .getCategoryBrandMerchantIndex(pageable, CATEGORY_ID, SEARCH_TERM, false, USER_TYPE_BANK));
        assertEquals("Reading categoryBrandMerchantIndex from database was failed", exception.getMessage());
    }

    /**
     * Get category brand merchant index page by brandId
     */
    @Test
    void Should_ReturnCategoryBrandMerchantIndexPage_When_GettingCategoryBrandMerchantIndexPageByBrandIdForBrandIdAndSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantIndexRepository.findAllByBrandIdAndSearchTerm(any(Pageable.class), anyString(),
                anyString())).thenReturn(getSampleCategoryBrandMerchantIndexPage());
        assertEquals(CATEGORY_BRAND_MERCHANT_INDEX_ID, categoryBrandMerchantIndexService
                .getCategoryBrandMerchantIndexPageByBrandId(pageable, BRAND_ID, SEARCH_TERM, USER_TYPE_BANK).getContent()
                .get(0).getId());
    }

    @Test
    void Should_ReturnCategoryBrandMerchantIndexPage_When_GettingCategoryBrandMerchantIndexPageByBrandIdForBrandIdAndAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantIndexRepository.findAllByBrandId(any(Pageable.class), anyString()))
                .thenReturn(getSampleCategoryBrandMerchantIndexPage());
        assertEquals(CATEGORY_BRAND_MERCHANT_INDEX_ID, categoryBrandMerchantIndexService
                .getCategoryBrandMerchantIndexPageByBrandId(pageable, BRAND_ID, ALL, USER_TYPE_BANK).getContent()
                .get(0).getId());
    }

    @Test
    void Should_ReturnCategoryBrandMerchantIndexPage_When_GettingCategoryBrandMerchantIndexPageByBrandIdForAllBrandIdAndSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantIndexRepository.findAllBySearch(any(Pageable.class), anyString(), any(UserType.class)))
                .thenReturn(getSampleCategoryBrandMerchantIndexPage());
        assertEquals(CATEGORY_BRAND_MERCHANT_INDEX_ID, categoryBrandMerchantIndexService
                .getCategoryBrandMerchantIndexPageByBrandId(pageable, ALL, SEARCH_TERM, USER_TYPE_BANK).getContent()
                .get(0).getId());
    }

    @Test
    void Should_ReturnCategoryBrandMerchantIndexPage_When_GettingCategoryBrandMerchantIndexPageByBrandIdForAllBrandIdAndAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantIndexRepository.findAllCategoryBrandMerchantIndex(any(Pageable.class), any(UserType.class)))
                .thenReturn(getSampleCategoryBrandMerchantIndexPage());
        assertEquals(CATEGORY_BRAND_MERCHANT_INDEX_ID, categoryBrandMerchantIndexService
                .getCategoryBrandMerchantIndexPageByBrandId(pageable, ALL, ALL, USER_TYPE_BANK).getContent()
                .get(0).getId());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingCategoryBrandMerchantIndexPageByBrandIdIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(categoryBrandMerchantIndexRepository.findAllByBrandIdAndSearchTerm(any(Pageable.class), anyString(),
                anyString())).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () ->
                categoryBrandMerchantIndexService.getCategoryBrandMerchantIndexPageByBrandId(pageable, BRAND_ID,
                        SEARCH_TERM, USER_TYPE_BANK));
        assertEquals("Reading categoryBrandMerchantIndex from database was failed", exception.getMessage());
    }

    /**
     * Update category brand merchant index by merchant id
     */
    @Test
    void Should_UpdateCategoryBrandMerchantIndexByMerchantId() {
        when(categoryBrandMerchantIndexRepository.findByMerchantId(anyString()))
                .thenReturn(Optional.of(getSampleCategoryBrandMerchantIndex()));

        categoryBrandMerchantIndexService.updateCategoryBrandMerchantIndexByMerchantId(MERCHANT_ID,
                BRAND_ID, CATEGORY_ID, true, ApprovalStatus.APPROVED);
        verify(categoryBrandMerchantIndexRepository).save(any(CategoryBrandMerchantIndex.class));
    }

    @Test
    void Should_ThrowInvalidUserException_When_UpdatingCategoryBrandMerchantIndexByMerchantIdForInvalidMerchantId() {
        when(categoryBrandMerchantIndexRepository.findByMerchantId(anyString()))
                .thenReturn(Optional.empty());
        InvalidUserException exception = assertThrows(InvalidUserException.class, () -> categoryBrandMerchantIndexService
                .updateCategoryBrandMerchantIndexByMerchantId(MERCHANT_ID, BRAND_ID, CATEGORY_ID, true,
                        ApprovalStatus.APPROVED));
        assertEquals("Invalid merchant Id: " + MERCHANT_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_UpdatingCategoryBrandMerchantIndexByMerchantIdIsFailed() {
        when(categoryBrandMerchantIndexRepository.findByMerchantId(anyString()))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> categoryBrandMerchantIndexService
                .updateCategoryBrandMerchantIndexByMerchantId(MERCHANT_ID, BRAND_ID, CATEGORY_ID, true,
                        ApprovalStatus.APPROVED));
        assertEquals("Updating categoryBrandMerchantIndex from database was failed", exception.getMessage());
    }

    /**
     * Get all category brand merchant index by user type merchant
     */
    @Test
    void Should_ReturnCategoryBrandMerchantIndex_When_GettingAllCategoryBrandMerchantIndexByUserTypeMerchantIsSuccessful() {
        when(categoryBrandMerchantIndexRepository.getAllByUserType(any(Pageable.class), any(UserType.class)))
                .thenReturn(getSampleCategoryBrandMerchantIndexPage());
        assertEquals(CATEGORY_BRAND_MERCHANT_INDEX_ID, categoryBrandMerchantIndexService
                .getAllCategoryBrandMerchantIndexByUserTypeMerchant(getSamplePageable(), UserType.MERCHANT)
                .getContent().get(0).getId());
    }

    /**
     * Update merchant info
     */
    @Test
    void Should_UpdateMerchantInfo_When_UpdatingMerchantInfoIsSuccessful() {
        when(categoryBrandMerchantIndexRepository.getAllByMerchantIdIn(anyList()))
                .thenReturn(getSampleCategoryBrandMerchantIndexList());
        categoryBrandMerchantIndexService.updateMerchantInfo(getSampleMerchantBusinessResponseDtoMap());
        verify(categoryBrandMerchantIndexRepository).saveAll(anyList());
    }

    @Test
    void Should_ThrowKupiCoreException_When_UpdatingMerchantInfoIsFailed() {
        Map<String, MerchantBusinessResponseDto> merchantBusinessResponseDtoMap =
                getSampleMerchantBusinessResponseDtoMap();

        when(categoryBrandMerchantIndexRepository.getAllByMerchantIdIn(anyList()))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> categoryBrandMerchantIndexService
                .updateMerchantInfo(merchantBusinessResponseDtoMap));
        assertEquals("Updating merchant info for categoryBrandMerchantIndex was failed", exception.getMessage());
    }

    /**
     * This method returns a sample MerchantBusinessResponseDto Map
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
        MerchantBusinessResponseDto merchantBusinessResponseDto = new MerchantBusinessResponseDto();
        merchantBusinessResponseDto.setName(BUSINESS_NAME);
        merchantBusinessResponseDto.setImageUrl(BUSINESS_IMAGE_URL);
        merchantBusinessResponseDto.setApprovalStatus(ApprovalStatus.APPROVED);
        merchantBusinessResponseDto.setActive(true);
        return merchantBusinessResponseDto;
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
     * This method returns a sample CategoryBrandMerchantIndex Page
     *
     * @return CategoryBrandMerchantIndex Page
     */
    private Page<CategoryBrandMerchantIndex> getSampleCategoryBrandMerchantIndexPage() {
        return new PageImpl<>(getSampleCategoryBrandMerchantIndexList());
    }

    /**
     * This method returns a sample CategoryBrandMerchantIndex List
     *
     * @return CategoryBrandMerchantIndex List
     */
    private List<CategoryBrandMerchantIndex> getSampleCategoryBrandMerchantIndexList() {
        List<CategoryBrandMerchantIndex> list = new ArrayList<>();
        list.add(getSampleCategoryBrandMerchantIndex());
        return list;
    }

    /**
     * This method returns a sample CategoryBrandMerchantIndex
     *
     * @return CategoryBrandMerchantIndex
     */
    private CategoryBrandMerchantIndex getSampleCategoryBrandMerchantIndex() {
        CategoryBrandMerchantIndex index = new CategoryBrandMerchantIndex();
        index.setId(CATEGORY_BRAND_MERCHANT_INDEX_ID);
        index.setMerchantId(MERCHANT_ID);
        return index;
    }
}