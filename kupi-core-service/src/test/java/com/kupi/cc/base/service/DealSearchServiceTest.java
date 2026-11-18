//package com.kupi.cc.base.service;
//
//import com.kupi.cc.base.domain.entity.Brand;
//import com.kupi.cc.base.domain.entity.Category;
//import com.kupi.cc.base.domain.entity.Deal;
//import com.kupi.cc.base.domain.entity.DealSearchIndex;
//import com.kupi.cc.base.domain.request.DealApprovalStatusUpdateRequestDto;
//import com.kupi.cc.base.domain.request.DealRequestDto;
//import com.kupi.cc.base.domain.response.BusinessMerchantResponseDto;
//import com.kupi.cc.base.enums.ApprovalStatus;
//import com.kupi.cc.base.enums.CategoryType;
//import com.kupi.cc.base.enums.DeductionType;
//import com.kupi.cc.base.enums.UserType;
//import com.kupi.cc.base.exception.InvalidDealException;
//import com.kupi.cc.base.exception.KupiCoreException;
//import com.kupi.cc.base.repository.DealSearchIndexRepository;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.springframework.dao.DataAccessException;
//import org.springframework.data.domain.*;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.*;
//import static org.mockito.MockitoAnnotations.initMocks;
//
///**
// * This class tests the {@link DealSearchService} class
// */
//class DealSearchServiceTest {
//
//    private static final String CATEGORY_ID = "cid-1235-1458-1785";
//    private static final long DEALS_COUNT = 1L;
//    private static final long ACTIVE_DEALS_COUNT = 0L;
//    private static final long CURRENT_MILLIS = System.currentTimeMillis();
//    private static final String ALL = "ALL";
//    private static final String MERCHANT_ID = "MERCHANT";
//    private static final String SEARCH_TERM = "SEARCH";
//    private static final int SIZE = 1;
//    private static final int PAGE = 1;
//    private static final String DEAL_SOURCE = UserType.MERCHANT.toString();
//    private static final String DEFAULT_SORT = "updated_at";
//    private static final String BRAND_ID = "BRAND";
//    private static final String DEAL_ID = "did-02a2022f-98c5-4237-a9c7-c1faf989a4a5";
//    private static final String BANK_ID = "uid-04af1815-988b-4fc7-b00b-14ee57a788df";
//    private DealSearchService dealSearchService;
//
//    @Mock
//    private DealSearchIndexRepository dealSearchIndexRepository;
//
//    @BeforeEach
//    void setUp() {
//        initMocks(this);
//        dealSearchService = new DealSearchService(dealSearchIndexRepository);
//    }
//
//    @AfterEach
//    void tearDown() {
//    }
//
//    @Test
//    void Should_ReturnDealsCount_When_ValidCategoryIdIsProvided() {
//        when(dealSearchIndexRepository.countByRelatedCategoriesContainsAndIsDeletedFalse(CATEGORY_ID)).thenReturn(DEALS_COUNT);
//        assertEquals(DEALS_COUNT, dealSearchService.getDealsCountByCategoryId(CATEGORY_ID));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_GetDealsCountByCategoryIdIsFailed() {
//        when(dealSearchIndexRepository.countByRelatedCategoriesContainsAndIsDeletedFalse(CATEGORY_ID))
//                .thenThrow(new DataAccessException("ERROR") {
//                });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealSearchService.getDealsCountByCategoryId(CATEGORY_ID)
//        );
//        assertEquals("Reading deal count from the database by category id was failed",
//                kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_ReturnActiveDealsCount_When_ValidCategoryIdIsProvided() {
//        when(dealSearchIndexRepository.getActiveValidDealsCountByCategoryId(CURRENT_MILLIS, CATEGORY_ID))
//                .thenReturn(ACTIVE_DEALS_COUNT);
//        assertEquals(ACTIVE_DEALS_COUNT, dealSearchService.getActiveDealsCountByCategoryId(CATEGORY_ID));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_GetActiveDealsCountByCategoryIdIsFailed() {
//        when(dealSearchIndexRepository.getActiveValidDealsCountByCategoryId(anyLong(), anyString()))
//                .thenThrow(new DataAccessException("ERROR") {
//                });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealSearchService.getActiveDealsCountByCategoryId(CATEGORY_ID)
//        );
//        assertEquals("Reading deal from search index was failed.",
//                kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_MerchantIdAndSearchTermAreEqualsToAllForSearchDeals() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllNotDeletedDeals(pageable, UserType.MERCHANT))
//                .thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.searchDeals(pageable, ALL, ALL, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_MerchantIdIsEqualsToAllForSearchDeals() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllNotDeletedDealsBySearchTerm(pageable, SEARCH_TERM, UserType.MERCHANT))
//                .thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.searchDeals(pageable, ALL, SEARCH_TERM, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_SearchTermIsEqualsToAllForSearchDeals() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllNotDeletedDealsByMerchantId(pageable, MERCHANT_ID))
//                .thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.searchDeals(pageable, MERCHANT_ID, ALL, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_MerchantIdAndSearchTermAreNotEqualsToAllForSearchDeals() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllNotDeletedDealsByMerchantIdAndSearchTerm(pageable, MERCHANT_ID, SEARCH_TERM))
//                .thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage,
//                dealSearchService.searchDeals(pageable, MERCHANT_ID, SEARCH_TERM, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_searchDealsIsFailed() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        when(dealSearchIndexRepository.findAllNotDeletedDealsByMerchantIdAndSearchTerm(pageable, MERCHANT_ID, SEARCH_TERM))
//                .thenThrow(new DataAccessException("ERROR") {
//                });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealSearchService.searchDeals(pageable, MERCHANT_ID, SEARCH_TERM, UserType.MERCHANT)
//        );
//        assertEquals("Reading all dealSearchIndexes from the database was failed.", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_ReturnPendingDealSearchPage_When_SearchTermIsEqualToAll() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository
//                .findAllByApprovalStatusAndDealSourceAndIsDeletedFalse(pageable, ApprovalStatus.PENDING, UserType.MERCHANT))
//                .thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getPendingSearchDeals(pageable, ALL, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnPendingDealSearchPage_When_SearchTermIsNotEqualToAll() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllPendingDealsBySearchTerm(pageable, SEARCH_TERM, UserType.MERCHANT.name()))
//                .thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getPendingSearchDeals(pageable, SEARCH_TERM, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_GetPendingSearchDealsIsFailed() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        when(dealSearchIndexRepository.findAllPendingDealsBySearchTerm(pageable, SEARCH_TERM, UserType.MERCHANT.name()))
//                .thenThrow(new DataAccessException("ERROR") {
//                });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealSearchService.getPendingSearchDeals(pageable, SEARCH_TERM, UserType.MERCHANT)
//        );
//        assertEquals("Reading pending dealSearchIndexes from the database was failed.", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_MerchantIdCategoryIdAndSearchTermAreProvidedForGetRecentlyExpireDeal() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllValidDealsByMerchantIdCategoryIdSearchTerm(any(Pageable.class),
//                anyString(), anyString(), anyString(), anyLong(), any(UserType.class))).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchRecentlyExpireDeals(pageable, CATEGORY_ID,
//                MERCHANT_ID, ALL, SEARCH_TERM, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_MerchantIdAndSearchTermAreProvidedForGetRecentlyExpireDeal() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllValidDealsByMerchantIdAndSearchTerm(any(Pageable.class),
//                anyString(), anyString(), anyLong(), any(UserType.class))).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchRecentlyExpireDeals(pageable, ALL,
//                MERCHANT_ID, ALL, SEARCH_TERM, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_CategoryIdAndMerchantIdAreProvidedForGetRecentlyExpireDeal() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllValidDealsByMerchantIdAndCategoryId(any(Pageable.class),
//                anyString(), anyString(), anyLong(), any(UserType.class))).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchRecentlyExpireDeals(pageable, CATEGORY_ID,
//                MERCHANT_ID, ALL, ALL, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_CategoryIdAndSearchTermAreProvidedForGetRecentlyExpireDeal() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllValidDealsByCategoryIdAndSearchTerm(any(Pageable.class),
//                anyString(), anyString(), anyLong(), any(UserType.class))).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchRecentlyExpireDeals(pageable, CATEGORY_ID,
//                ALL, ALL, SEARCH_TERM, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_CategoryIdIsProvidedForGetRecentlyExpireDeal() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllValidDealsByCategoryId(any(Pageable.class),
//                anyString(), anyLong(), any(UserType.class))).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchRecentlyExpireDeals(pageable, CATEGORY_ID,
//                ALL, ALL, ALL, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_MerchantIdIsProvidedForGetRecentlyExpireDeal() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllValidDealsByMerchantId(any(Pageable.class), anyString(),
//                anyLong(), any(UserType.class))).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchRecentlyExpireDeals(pageable, ALL,
//                MERCHANT_ID, ALL, ALL, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_SearchTermIsProvidedForGetRecentlyExpireDeal() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllValidDealsBySearchTerm(any(Pageable.class),
//                anyString(), anyLong(), any(UserType.class))).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchRecentlyExpireDeals(pageable, ALL, ALL,
//                ALL, SEARCH_TERM, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_AllFieldsAreProvidedForGetRecentlyExpireDeal() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllValidDeals(any(Pageable.class), anyLong(),
//                any(UserType.class))).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchRecentlyExpireDeals(pageable, ALL, ALL,
//                ALL, ALL, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_GetRecentlyExpireDealIsFailed() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        when(dealSearchIndexRepository.findAllValidDeals(any(Pageable.class), anyLong(),
//                any(UserType.class))).thenThrow(new DataAccessException("ERROR") {
//        });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealSearchService.getAllSearchRecentlyExpireDeals(pageable, ALL, ALL, ALL, ALL, UserType.MERCHANT)
//        );
//        assertEquals("Reading deal from search index was failed.", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_MerchantIdCategoryIdAndSearchTermAreProvidedForGetAllSearchDeals() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllByMerchantIdCategoryIdSearchTerm(pageable, MERCHANT_ID, CATEGORY_ID,
//                SEARCH_TERM, UserType.MERCHANT.name()))
//                .thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchDeals(pageable, CATEGORY_ID, MERCHANT_ID, ALL,
//                SEARCH_TERM, UserType.MERCHANT.name()));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_MerchantIdAndSearchTermAreProvidedForGetAllSearchDeals() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllByMerchantIdAndSearchTerm(pageable, MERCHANT_ID, SEARCH_TERM,
//                UserType.MERCHANT.name())).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchDeals(pageable, ALL, MERCHANT_ID, ALL,
//                SEARCH_TERM, UserType.MERCHANT.name()));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_CategoryIdAndMerchantIdAreProvidedForGetAllSearchDeals() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllByMerchantIdAndCategoryId(pageable, MERCHANT_ID, CATEGORY_ID,
//                UserType.MERCHANT.name())).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchDeals(pageable, CATEGORY_ID, MERCHANT_ID, ALL,
//                ALL, UserType.MERCHANT.name()));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_CategoryIdAndSearchTermAreProvidedForGetAllSearchDeals() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllByCategoryIdAndSearchTerm(pageable, CATEGORY_ID, SEARCH_TERM,
//                UserType.MERCHANT.name())).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchDeals(pageable, CATEGORY_ID, ALL, ALL,
//                SEARCH_TERM, UserType.MERCHANT.name()));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_CategoryIdIsProvidedForGetAllSearchDeals() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllByCategoryId(pageable, CATEGORY_ID, UserType.MERCHANT.name()))
//                .thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchDeals(pageable, CATEGORY_ID, ALL, ALL, ALL,
//                UserType.MERCHANT.name()));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_MerchantIdIsProvidedForGetAllSearchDeals() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllByMerchantId(pageable, MERCHANT_ID, UserType.MERCHANT.name()))
//                .thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchDeals(pageable, ALL, MERCHANT_ID, ALL, ALL,
//                UserType.MERCHANT.name()));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_SearchTermIsProvidedForGetAllSearchDeals() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllApprovedDealsBySearchTerm(pageable, SEARCH_TERM, UserType.MERCHANT.name()))
//                .thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchDeals(pageable, ALL, ALL, ALL, SEARCH_TERM,
//                UserType.MERCHANT.name()));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_BrandIdIsProvidedForGetAllSearchDeals() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllByBrandId(pageable, BRAND_ID, UserType.MERCHANT.name()))
//                .thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchDeals(pageable, ALL, ALL, BRAND_ID, ALL,
//                UserType.MERCHANT.name()));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_BrandIdAndSearchTermAreProvidedForGetAllSearchDeals() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllByBrandIdAndSearchTerm(pageable, BRAND_ID, SEARCH_TERM,
//                UserType.MERCHANT.name())).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchDeals(pageable, ALL, ALL, BRAND_ID,
//                SEARCH_TERM, UserType.MERCHANT.name()));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_AllFieldsAreProvidedForGetAllSearchDeals() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllSearchDeals(pageable, UserType.MERCHANT.name()))
//                .thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllSearchDeals(pageable, ALL, ALL, ALL, ALL,
//                UserType.MERCHANT.name()));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_GetAllSearchDealsIsFailed() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        when(dealSearchIndexRepository.findAllSearchDeals(pageable, UserType.MERCHANT.name()))
//                .thenThrow(new DataAccessException("ERROR") {
//                });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealSearchService.getAllSearchDeals(pageable, ALL, ALL, ALL, ALL, DEAL_SOURCE)
//        );
//        assertEquals("Reading deal from search index was failed.", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_MerchantIdCategoryIdAndSearchTermAreProvidedForGetAllActiveDealsForActiveMerchant() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllActiveDealsByActiveMerchantIdCategoryIdSearchTerm(any(Pageable.class),
//                anyString(), anyString(), anyString(), anyLong(), any(UserType.class))).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllActiveDealsForActiveMerchant(pageable, CATEGORY_ID,
//                MERCHANT_ID, ALL, SEARCH_TERM, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_MerchantIdAndSearchTermAreProvidedForGetAllActiveDealsForActiveMerchant() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllActiveDealsByActiveMerchantIdAndSearchTerm(any(Pageable.class),
//                anyString(), anyString(), anyLong(), any(UserType.class))).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllActiveDealsForActiveMerchant(pageable, ALL,
//                MERCHANT_ID, ALL, SEARCH_TERM, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_CategoryIdAndMerchantIdAreProvidedForGetAllActiveDealsForActiveMerchant() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllActiveDealsByActiveMerchantIdAndCategoryId(any(Pageable.class),
//                anyString(), anyString(), anyLong(), any(UserType.class))).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllActiveDealsForActiveMerchant(pageable, CATEGORY_ID,
//                MERCHANT_ID, ALL, ALL, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_CategoryIdAndSearchTermAreProvidedForGetAllActiveDealsForActiveMerchant() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllActiveDealsCategoryIdAndSearchTermAndActiveMerchant(any(Pageable.class),
//                anyString(), anyString(), anyLong(), any(UserType.class))).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllActiveDealsForActiveMerchant(pageable, CATEGORY_ID,
//                ALL, ALL, SEARCH_TERM, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_CategoryIdIsProvidedForGetAllActiveDealsForActiveMerchant() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllActiveDealsByCategoryIdAndActiveMerchant(any(Pageable.class),
//                anyString(), anyLong(), any(UserType.class))).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllActiveDealsForActiveMerchant(pageable, CATEGORY_ID,
//                ALL, ALL, ALL, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_MerchantIdIsProvidedForGetAllActiveDealsForActiveMerchant() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllActiveDealsByActiveMerchantId(any(Pageable.class), anyString(),
//                anyLong(), any(UserType.class))).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllActiveDealsForActiveMerchant(pageable, ALL,
//                MERCHANT_ID, ALL, ALL, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_SearchTermIsProvidedForGetAllActiveDealsForActiveMerchant() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllApprovedActiveDealsBySearchTermAndActiveMerchant(any(Pageable.class),
//                anyString(), anyLong(), any(UserType.class))).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllActiveDealsForActiveMerchant(pageable, ALL, ALL,
//                ALL, SEARCH_TERM, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_AllFieldsAreProvidedForGetAllActiveDealsForActiveMerchant() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchIndexRepository.findAllActiveDealsWithActiveMerchant(any(Pageable.class), anyLong(),
//                any(UserType.class))).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealSearchService.getAllActiveDealsForActiveMerchant(pageable, ALL, ALL,
//                ALL, ALL, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_GetAllActiveDealsForActiveMerchantIsFailed() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        when(dealSearchIndexRepository.findAllActiveDealsWithActiveMerchant(any(Pageable.class), anyLong(),
//                any(UserType.class))).thenThrow(new DataAccessException("ERROR") {
//        });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealSearchService.getAllActiveDealsForActiveMerchant(pageable, ALL, ALL, ALL, ALL, UserType.MERCHANT)
//        );
//        assertEquals("Reading deal from search index was failed.", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_ReturnActiveDealsIdListList_When_ValidBankIdProvidedForGetActiveDealsForActiveBank() {
//        List<String> dealsIdList = new ArrayList<>();
//        dealsIdList.add(DEAL_ID);
//        when(dealSearchIndexRepository.getActiveDealsByActiveBankId(anyString(), anyLong())).thenReturn(dealsIdList);
//        assertEquals(dealsIdList, dealSearchService.getActiveDealsForActiveBank(BANK_ID));
//    }
//
//    @Test
//    void Should_UpdateApprovalForFilteredDeal() {
//        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
//        DealSearchIndex dealSearchIndex = getSampleDealSearchIndex();
//        when(dealSearchIndexRepository.findPendingDealById(DEAL_ID)).thenReturn(Optional.of(dealSearchIndex));
//        dealSearchService.updateApprovalStatus(dealApprovalStatusUpdateRequestDto);
//        verify(dealSearchIndexRepository, times(1)).save(dealSearchIndex);
//    }
//
//    @Test
//    void Should_ThrowInvalidDealException_When_InvalidIdIsProvidedForUpdateApprovalStatus() {
//        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
//        when(dealSearchIndexRepository.findPendingDealById(DEAL_ID)).thenReturn(Optional.empty());
//        InvalidDealException invalidDealException = assertThrows(InvalidDealException.class, () ->
//                dealSearchService.updateApprovalStatus(dealApprovalStatusUpdateRequestDto));
//        assertEquals("Invalid deal Id:" + DEAL_ID, invalidDealException.getMessage());
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_updateApprovalStatusIsFailed() {
//        DealSearchIndex dealSearchIndex = getSampleDealSearchIndex();
//        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
//        when(dealSearchIndexRepository.findPendingDealById(DEAL_ID)).thenReturn(Optional.of(dealSearchIndex));
//        when(dealSearchIndexRepository.save(dealSearchIndex)).thenThrow(new DataAccessException("ERROR") {
//        });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealSearchService.updateApprovalStatus(dealApprovalStatusUpdateRequestDto));
//        assertEquals("Reading deal from search index was failed.", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndex_When_ValidDealIdIsProvidedForFindById() {
//        DealSearchIndex dealSearchIndex = getSampleDealSearchIndex();
//        when(dealSearchIndexRepository.findById(DEAL_ID)).thenReturn(Optional.of(dealSearchIndex));
//        assertEquals(dealSearchIndex, dealSearchService.findById(DEAL_ID));
//    }
//
//    @Test
//    void Should_ThrowInvalidDealException_When_InvalidDealIdIsProvidedForFindById() {
//        when(dealSearchIndexRepository.findById(DEAL_ID)).thenReturn(Optional.empty());
//        InvalidDealException invalidDealException = assertThrows(InvalidDealException.class, () ->
//                dealSearchService.findById(DEAL_ID));
//        assertEquals("Invalid deal Id:" + DEAL_ID, invalidDealException.getMessage());
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_FindByIdIsFailed() {
//        when(dealSearchIndexRepository.findById(DEAL_ID)).thenThrow(new DataAccessException("ERROR") {
//        });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealSearchService.findById(DEAL_ID));
//        assertEquals("Saving deals search index to database was failed", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_SaveDealSearchIndex() {
//        DealSearchIndex dealSearchIndex = getSampleDealSearchIndex();
//        dealSearchService.save(dealSearchIndex);
//        verify(dealSearchIndexRepository, times(1)).save(dealSearchIndex);
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_SaveDealSearchIndexIsFailed() {
//        DealSearchIndex dealSearchIndex = getSampleDealSearchIndex();
//        when(dealSearchIndexRepository.save(dealSearchIndex)).thenThrow(new DataAccessException("ERROR") {
//        });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealSearchService.save(dealSearchIndex));
//        assertEquals("Saving deals search index to database was failed", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_UpdateDealSearchIndex() {
//        DealSearchIndex dealSearchIndex = getSampleDealSearchIndex();
//        Deal deal = getSampleDeal();
//        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
//        when(dealSearchIndexRepository.findById(DEAL_ID)).thenReturn(Optional.of(dealSearchIndex));
//        dealSearchIndex.update(deal, businessMerchantResponseDto);
//        dealSearchService.updateDealSearchIndex(deal, businessMerchantResponseDto);
//        verify(dealSearchIndexRepository, times(1)).save(dealSearchIndex);
//    }
//
//    @Test
//    void Should_DeleteDeal() {
//        DealSearchIndex dealSearchIndex = getSampleDealSearchIndex();
//        when(dealSearchIndexRepository.getNotDeletedDealById(DEAL_ID)).thenReturn(Optional.of(dealSearchIndex));
//        dealSearchService.deleteDeal(DEAL_ID);
//        verify(dealSearchIndexRepository).save(dealSearchIndex);
//    }
//
//    @Test
//    void Should_ThrowInvalidDealException_When_InvalidDealIdIsProvidedForDeleteDeal() {
//        when(dealSearchIndexRepository.findPendingDealById(DEAL_ID)).thenReturn(Optional.empty());
//        InvalidDealException invalidDealException = assertThrows(InvalidDealException.class, () ->
//                dealSearchService.deleteDeal(DEAL_ID));
//        assertEquals("Invalid deal Id:" + DEAL_ID, invalidDealException.getMessage());
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_DeleteDealIsFailed() {
//        DealSearchIndex dealSearchIndex = getSampleDealSearchIndex();
//        when(dealSearchIndexRepository.getNotDeletedDealById(DEAL_ID)).thenReturn(Optional.of(dealSearchIndex));
//        when(dealSearchIndexRepository.save(dealSearchIndex)).thenThrow(new DataAccessException("ERROR") {
//        });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealSearchService.deleteDeal(DEAL_ID));
//        assertEquals("Reading deal from search index was failed.", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_Return_DealCount_When_ValidBrandIdIsProvidedForGetDealsCountByBrandId() {
//        when(dealSearchIndexRepository.countByRelatedBrandsContainsAndIsDeletedFalse(BRAND_ID)).thenReturn(DEALS_COUNT);
//        assertEquals(DEALS_COUNT, dealSearchService.getDealsCountByBrandId(BRAND_ID));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_GetDealsCountByBrandIdFromDataBaseIsFailed() {
//        when(dealSearchIndexRepository.countByRelatedBrandsContainsAndIsDeletedFalse(BRAND_ID))
//                .thenThrow(new DataAccessException("ERROR") {
//                });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealSearchService.getDealsCountByBrandId(BRAND_ID));
//        assertEquals("Reading deal count from the database by Brand id was failed", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_Return_ActiveDealCount_When_ValidBrandIdIsProvidedForGetActiveDealsCountByBrandId() {
//        when(dealSearchIndexRepository.getActiveValidDealsCountByBrandId(CURRENT_MILLIS, BRAND_ID))
//                .thenReturn(ACTIVE_DEALS_COUNT);
//        assertEquals(ACTIVE_DEALS_COUNT, dealSearchService.getActiveDealsCountByBrandId(BRAND_ID));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_GetActiveDealsCountByBrandIdFromDataBaseIsFailed() {
//        when(dealSearchIndexRepository.getActiveValidDealsCountByBrandId(anyLong(), anyString()))
//                .thenThrow(new DataAccessException("ERROR") {
//                });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealSearchService.getActiveDealsCountByBrandId(BRAND_ID));
//        assertEquals("Reading dealSearchIndex by brand id from the database was failed.", kupiCoreException.getMessage());
//    }
//
//    /**
//     * Get deal list by dealId list
//     */
//    @Test
//    void Should_ReturnDealSearchIndexList_When_GettingDealListByDealIdListIsSuccessful() {
//        List<DealSearchIndex> dealSearchIndexList = new ArrayList<>();
//        dealSearchIndexList.add(getSampleDealSearchIndex());
//
//        when(dealSearchIndexRepository.findByIdIn(anyList())).thenReturn(dealSearchIndexList);
//
//        assertEquals("did-02104ca3-41e4-4cb2-8a06-8ae0805ef6dc", dealSearchService
//                .getDealListByDealIdList(new ArrayList<>()).get(0).getId());
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_GettingDealListByDealIdListIsFailed() {
//        List<String> dealIdList = new ArrayList<>();
//
//        when(dealSearchIndexRepository.findByIdIn(anyList())).thenThrow(new DataAccessException("failed") {
//        });
//        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> dealSearchService
//                .getDealListByDealIdList(dealIdList));
//        assertEquals("Returning deal search indexes by deal id(s) was failed.", exception.getMessage());
//    }
//
//    /**
//     * This method creates sample DealSearchIndex page
//     *
//     * @return dealSearchIndexPage
//     */
//    private Page<DealSearchIndex> getSampleDealSearchIndexPage() {
//        DealSearchIndex dealSearchIndex = new DealSearchIndex();
//        dealSearchIndex.setId("did-02104ca3-41e4-4cb2-8a06-8ae0805ef6dc");
//        dealSearchIndex.setApprovalStatus(ApprovalStatus.APPROVED);
//        dealSearchIndex.setDeleted(false);
//        dealSearchIndex.setTitle("Bank deal to be approved");
//        dealSearchIndex.setActiveMerchant(true);
//        dealSearchIndex.setBrandNames("Brand");
//        dealSearchIndex.setDealSource(UserType.MERCHANT);
//        dealSearchIndex.setCategoryNames("category");
//        dealSearchIndex.setSearchImageUrls("[https://objects-kupi-dev.s3.ap-southeast-1.amazonaws.com/fid-a61e4565-dd03-4bb1-a123-90460ca3def5.png]");
//        dealSearchIndex.setSubTitle("bank deal");
//        dealSearchIndex.setTermsAndConditions("no conditions ");
//        List<DealSearchIndex> dealSearchIndexList = new ArrayList<>();
//        dealSearchIndexList.add(dealSearchIndex);
//        return new PageImpl<>(dealSearchIndexList);
//    }
//
//    /**
//     * This method creates sample DealApprovalStatusUpdateRequestDto
//     *
//     * @return DealApprovalStatusUpdateRequestDto
//     */
//    private DealApprovalStatusUpdateRequestDto getSampleDealApprovalStatusUpdateRequestDto() {
//        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = new DealApprovalStatusUpdateRequestDto();
//        dealApprovalStatusUpdateRequestDto.setApprovalStatus(ApprovalStatus.APPROVED);
//        dealApprovalStatusUpdateRequestDto.setId(DEAL_ID);
//        dealApprovalStatusUpdateRequestDto.setComment("Comment");
//        return dealApprovalStatusUpdateRequestDto;
//    }
//
//    /**
//     * This method creates sample DealSearchIndex
//     *
//     * @return DealSearchIndex
//     */
//    private DealSearchIndex getSampleDealSearchIndex() {
//        DealSearchIndex dealSearchIndex = new DealSearchIndex();
//        dealSearchIndex.setId("did-02104ca3-41e4-4cb2-8a06-8ae0805ef6dc");
//        dealSearchIndex.setApprovalStatus(ApprovalStatus.APPROVED);
//        dealSearchIndex.setDeleted(false);
//        dealSearchIndex.setTitle("Bank deal to be approved");
//        dealSearchIndex.setActiveMerchant(true);
//        dealSearchIndex.setBrandNames("Brand");
//        dealSearchIndex.setDealSource(UserType.MERCHANT);
//        dealSearchIndex.setCategoryNames("category");
//        dealSearchIndex.setSearchImageUrls("[https://objects-kupi-dev.s3.ap-southeast-1.amazonaws.com/fid-a61e4565-dd03-4bb1-a123-90460ca3def5.png]");
//        dealSearchIndex.setSubTitle("bank deal");
//        dealSearchIndex.setTermsAndConditions("no conditions ");
//        return dealSearchIndex;
//    }
//
//    /**
//     * This method creates BusinessMerchantResponseDto
//     *
//     * @return BusinessMerchantResponseDto
//     */
//    private BusinessMerchantResponseDto getSampleBusinessMerchantResponseDto() {
//        BusinessMerchantResponseDto businessMerchantResponseDto = new BusinessMerchantResponseDto();
//        businessMerchantResponseDto.setBusinessId("uid-001");
//        businessMerchantResponseDto.setBusinessName("Roy");
//        businessMerchantResponseDto.setImageUrl("goo.le/image");
//        businessMerchantResponseDto.setOwnerName("Roy Owner");
//        businessMerchantResponseDto.setAddress("Colombo");
//        businessMerchantResponseDto.setWebSite("goo.le/image2");
//        businessMerchantResponseDto.setApprovalStatus(ApprovalStatus.APPROVED);
//        businessMerchantResponseDto.setActive(true);
//        return businessMerchantResponseDto;
//    }
//
//    /**
//     * This method creates sample DealRequestDto
//     *
//     * @return DealRequestDto
//     */
//    private DealRequestDto getSampleDealRequestDto() {
//        List<String> imgUrls = new ArrayList<>();
//        imgUrls.add("https://github.com/1.png");
//        imgUrls.add("https://github.com/2.png");
//        imgUrls.add("https://github.com/3.png");
//
//        List<String> brandList = new ArrayList<>();
//        brandList.add("bid-2225");
//
//        List<String> categoryList = new ArrayList<>();
//        categoryList.add("cid-266565");
//
//        return new DealRequestDto(
//                "New Year Deal!", "for new year",
//                "The Description", "No Terms And Conditions",
//                imgUrls, "https://github.com/cover.png",
//                12.25, 50,
//                DeductionType.AMOUNT, 2.22, 5.00, 0.0,
//                1667217882000L, 1672488282000L,
//                null, null, // startTime, endTime
//                "uid-0022", brandList, categoryList, null, "uid-2222", false, false);
//    }
//
//    /**
//     * This method creates sample deal
//     *
//     * @return Deal
//     */
//    private Deal getSampleDeal() {
//        DealRequestDto dealRequestDto = getSampleDealRequestDto();
//        Set<Brand> brandSet = new HashSet<>();
//        Set<Category> categorySet = new HashSet<>();
//
//        brandSet.add(new Brand(
//                "bid-001",
//                "new Brand",
//                "description",
//                "http://google.com",
//                1667217882000L,
//                1672488282000L
//        ));
//
//        categorySet.add(new Category(
//                "cid-001",
//                "hot category",
//                "description",
//                "http://go.lk",
//                CategoryType.NORMAL,
//                new HashSet<>(),
//                new HashSet<>(),
//                null,
//                null,
//                1667217882000L,
//                1667217882000L,
//                true
//        ));
//
//        Deal deal = new Deal(dealRequestDto, brandSet, categorySet, null, "DLM-2203120015");
//        deal.setId(DEAL_ID);
//        return deal;
//    }
//}
