//package com.kupi.cc.base.service;
//
//import com.kupi.cc.base.domain.entity.*;
//import com.kupi.cc.base.domain.request.DealApprovalStatusUpdateRequestDto;
//import com.kupi.cc.base.domain.request.DealRequestDto;
//import com.kupi.cc.base.domain.request.DealUpdateRequestDto;
//import com.kupi.cc.base.domain.response.BasicMerchantBusinessResponseDto;
//import com.kupi.cc.base.domain.response.BusinessMerchantResponseDto;
//import com.kupi.cc.base.enums.*;
//import com.kupi.cc.base.exception.ExpiredDealApprovalException;
//import com.kupi.cc.base.exception.InvalidDealException;
//import com.kupi.cc.base.exception.InvalidUserException;
//import com.kupi.cc.base.exception.KupiCoreException;
//import com.kupi.cc.base.repository.BankDealRepository;
//import com.kupi.cc.base.repository.DealRepository;
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
// * This class tests the {@link DealService } class.
// */
//class DealServiceTest {
//
//    private static final String DEAL_ID = "did-92be0c67-3810-47c2-9e28-615f81efad6a";
//    private static final String INVALID_DEAL = "Invalid deal Id: did-92be0c67-3810-47c2-9e28-615f81efad6a";
//    private static final int SIZE = 1;
//    private static final int PAGE = 1;
//    private static final String DEFAULT_SORT = "updated_at";
//    private static final String MERCHANT_ID = "MERCHANT";
//    private static final String SEARCH_TERM = "SEARCH";
//    private static final String CATEGORY_ID = "CATEGORY";
//    private static final String BRAND_ID = "BRAND";
//    private static final String BANK_ID = "uid-04af1815-988b-4fc7-b00b-14ee57a788df";
//    private static final String TIME_ZONE = "Asia/Colombo";
//    private static final String SUBSCRIPTION_ID = "sid-1458-8652-7854-2596";
//    private static final String ACCESS_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
//    private DealService dealService;
//    @Mock
//    private DealRepository dealRepository;
//    @Mock
//    private DealSearchService dealSearchService;
//    @Mock
//    private DealApprovalService dealApprovalService;
//    @Mock
//    private BrandService brandService;
//    @Mock
//    private CategoryService categoryService;
//    @Mock
//    private BankDealRepository bankDealRepository;
//    @Mock
//    private MerchantBankSearchIndexService merchantBankSearchIndexService;
//    @Mock
//    private DealCodeService dealCodeService;
//    @Mock
//    private DealSearchIndexRepository dealSearchIndexRepository;
//    @Mock
//    private SubscriberService subscriberService;
//    @Mock
//    private PromoTypeService promoTypeService;
//
//
//    @BeforeEach
//    void setUp() {
//        initMocks(this);
//        dealService = new DealService(dealRepository, bankDealRepository, dealSearchService, dealApprovalService,
//                brandService, categoryService, merchantBankSearchIndexService, dealCodeService,
//                dealSearchIndexRepository, subscriberService,promoTypeService);
//    }
//
//    @AfterEach
//    void tearDown() {
//    }
//
//    @Test
//    void Should_SaveTheDeal() {
//        BasicMerchantBusinessResponseDto basicMerchantBusinessResponseDto = getSampleBasicMerchantBusinessResponseDto();
//        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
//        Deal deal = getSampleDeal();
//        dealService.createDeal(deal, basicMerchantBusinessResponseDto,
//                UserType.MERCHANT, getSampleDealRequestDto(), businessMerchantResponseDto);
//        verify(dealRepository, times(1)).save(deal);
//    }
//
//    @Test
//    void Should_SaveDeal_When_UserTypeIsNotMerchant() {
//        BasicMerchantBusinessResponseDto basicMerchantBusinessResponseDto = getSampleBasicMerchantBusinessResponseDto();
//        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
//        Deal deal = getSampleDeal();
//        BankDeal bankDeal = new BankDeal(deal);
//        when(bankDealRepository.save(any(BankDeal.class))).thenReturn(bankDeal);
//        merchantBankSearchIndexService.save(bankDeal, basicMerchantBusinessResponseDto, businessMerchantResponseDto);
//        dealService.createDeal(deal, basicMerchantBusinessResponseDto,
//                UserType.BANK, getSampleDealRequestDto(), businessMerchantResponseDto);
//        verify(dealSearchService, times(1)).save(any(DealSearchIndex.class));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_when_SavingDealWithEmptyId() {
//        BasicMerchantBusinessResponseDto basicMerchantBusinessResponseDto = getSampleBasicMerchantBusinessResponseDto();
//        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
//        Deal deal = getSampleDeal();
//        DealRequestDto dealRequestDto = getSampleDealRequestDto();
//        when(dealRepository.save(any(Deal.class))).thenThrow(new DataAccessException("FAILED") {
//        });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealService.createDeal(deal, basicMerchantBusinessResponseDto,
//                        UserType.MERCHANT, dealRequestDto, businessMerchantResponseDto));
//        assertEquals("Saving deal to database was failed", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_DeleteDeal_When_OptionalDealPresent() {
//        Deal deal = getSampleDeal();
//        when(dealRepository.findDealById(DEAL_ID)).thenReturn(Optional.of(deal));
//        dealService.deleteDeal(DEAL_ID, UserType.MERCHANT);
//        verify(dealRepository, times(1)).save(deal);
//    }
//
//    @Test
//    void Should_DeleteDeal_When_UserTypeIsBank() {
//        BankDeal bankDeal = new BankDeal(getSampleDeal());
//        when(bankDealRepository.findDealById(DEAL_ID)).thenReturn(Optional.of(bankDeal));
//        dealService.deleteDeal(DEAL_ID, UserType.BANK);
//        verify(bankDealRepository, times(1)).save(any(BankDeal.class));
//    }
//
//    @Test
//    void Should_ThrowException_When_WhileDeletingADeal() {
//        when(dealRepository.findPendingDealById(DEAL_ID)).thenReturn(Optional.empty());
//        InvalidDealException invalidDealException = assertThrows(InvalidDealException.class, () ->
//                dealService.deleteDeal(DEAL_ID, UserType.MERCHANT));
//        assertEquals(INVALID_DEAL, invalidDealException.getMessage());
//    }
//
//    @Test
//    void Should_ThrowException_When_WhileGettingDeal() {
//        when(dealRepository.findDealById(DEAL_ID)).thenThrow(new DataAccessException("ERROR") {
//        });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealService.deleteDeal(DEAL_ID, UserType.MERCHANT));
//        assertEquals("Reading deal from the database was failed.", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_ReturnDeal_When_UserTypeIsMerchantAndDealIdIsProvided() {
//        Deal deal = getSampleDeal();
//        when(dealRepository.findDealById(DEAL_ID)).thenReturn(Optional.of(deal));
//        assertEquals(deal, dealService.getDeal(DEAL_ID, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDeal_When_UserTypeIsBankAndDealIdIsProvided() {
//        BankDeal bankDeal = getSampleBankDeal();
//        Deal deal = getSampleDeal();
//        when(bankDealRepository.findDealById(DEAL_ID)).thenReturn(Optional.of(bankDeal));
//        assertEquals(deal.getId(), dealService.getDeal(DEAL_ID, UserType.BANK).getId());
//        assertEquals(deal.getMerchantId(), dealService.getDeal(DEAL_ID, UserType.BANK).getMerchantId());
//        assertEquals(deal.getDealCode(), dealService.getDeal(DEAL_ID, UserType.BANK).getDealCode());
//        assertEquals(deal.getDescription(), dealService.getDeal(DEAL_ID, UserType.BANK).getDescription());
//    }
//
//    @Test
//    void Should_ThrowInvalidDealException_When_InvalidDealIdIsProvided() {
//        when(dealRepository.findDealById(DEAL_ID)).thenReturn(Optional.empty());
//        when(bankDealRepository.findDealById(DEAL_ID)).thenReturn(Optional.empty());
//        InvalidDealException invalidDealException = assertThrows(InvalidDealException.class, () ->
//                dealService.getDeal(DEAL_ID, UserType.MERCHANT));
//        assertEquals("Invalid deal Id: " + DEAL_ID, invalidDealException.getMessage());
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_GetDealIsFailed() {
//        when(dealRepository.findDealById(DEAL_ID)).thenThrow(new DataAccessException("ERROR") {
//        });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealService.getDeal(DEAL_ID, UserType.MERCHANT));
//        assertEquals("Reading deal from the database was failed.", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_ReturnDealPage_When_ValidPageableIsProvided() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<Deal> dealPage = getSampleDealPage();
//        when(dealRepository.findAllDeals(pageable)).thenReturn(dealPage);
//        assertEquals(dealPage, dealService.listAllDeals(pageable));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_ListAllDealsIsFailed() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        when(dealRepository.findAllDeals(pageable)).thenThrow(new DataAccessException("ERROR") {
//        });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealService.listAllDeals(pageable));
//        assertEquals("Read deals from database was failed.", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_ValidFieldsAreProvided() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchService.searchDeals(pageable, MERCHANT_ID, SEARCH_TERM, UserType.MERCHANT))
//                .thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealService.searchDeals(pageable, MERCHANT_ID, SEARCH_TERM, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_SearchDealsIsFailed() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        when(dealSearchService.searchDeals(pageable, MERCHANT_ID, SEARCH_TERM, UserType.MERCHANT))
//                .thenThrow(new DataAccessException("ERROR") {
//                });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealService.searchDeals(pageable, MERCHANT_ID, SEARCH_TERM, UserType.MERCHANT));
//        assertEquals("Read deals from database was failed.", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_ReturnPendingDealsPage_When_ValidFieldsAreProvided() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealPage = getSampleDealSearchIndexPage();
//        when(dealSearchService.getPendingSearchDeals(pageable, SEARCH_TERM, UserType.MERCHANT))
//                .thenReturn(dealPage);
//        assertEquals(dealPage, dealService.getPendingDeals(pageable, SEARCH_TERM, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_GetPendingDealsIsFailed() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        when(dealSearchService.getPendingSearchDeals(pageable, SEARCH_TERM, UserType.MERCHANT))
//                .thenThrow(new DataAccessException("ERROR") {
//                });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealService.getPendingDeals(pageable, SEARCH_TERM, UserType.MERCHANT));
//        assertEquals("Read pending deals from database was failed.", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_ReturnDealPage_When_MerchantIdIsProvidedForListAllDealsByMerchant() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<Deal> dealPage = getSampleDealPage();
//        when(dealRepository.findAllDealsByMerchantId(pageable, MERCHANT_ID)).thenReturn(dealPage);
//        assertEquals(dealPage, dealService.listAllDealsByMerchant(pageable, MERCHANT_ID));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_AllDealsByMerchantIsFailed() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        when(dealRepository.findAllDealsByMerchantId(pageable, MERCHANT_ID))
//                .thenThrow(new DataAccessException("ERROR") {
//                });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealService.listAllDealsByMerchant(pageable, MERCHANT_ID));
//        assertEquals("Reading deal from the database was failed.", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_ValidFieldsAreProvideForGetRecentlyExpireDeal() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchService.getAllSearchRecentlyExpireDeals(pageable, CATEGORY_ID, MERCHANT_ID, BRAND_ID,
//                SEARCH_TERM, UserType.MERCHANT)).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealService.getRecentlyExpireDeals(pageable, CATEGORY_ID, MERCHANT_ID, BRAND_ID,
//                SEARCH_TERM, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_GetRecentlyExpireDealIsFailed() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        when(dealSearchService.getAllSearchRecentlyExpireDeals(pageable, CATEGORY_ID, MERCHANT_ID, BRAND_ID,
//                SEARCH_TERM, UserType.MERCHANT)).thenThrow(new DataAccessException("ERROR") {
//        });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealService.getRecentlyExpireDeals(pageable, CATEGORY_ID, MERCHANT_ID, BRAND_ID, SEARCH_TERM, UserType.MERCHANT));
//        assertEquals("Reading dealSearchIndex from the database was failed.", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_BothDealAndMerchantAreActiveForGetAllSearchDeals() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchService.getAllActiveDealsForActiveMerchant(pageable, CATEGORY_ID, MERCHANT_ID, BRAND_ID,
//                SEARCH_TERM, UserType.MERCHANT)).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealService.getAllSearchDeals(pageable, CATEGORY_ID, MERCHANT_ID, BRAND_ID,
//                SEARCH_TERM, true, true, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_EitherDealOrMerchantIsNotActiveForGetAllSearchDeals() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
//        when(dealSearchService.getAllSearchDeals(pageable, CATEGORY_ID, MERCHANT_ID, BRAND_ID,
//                SEARCH_TERM, UserType.MERCHANT.name())).thenReturn(dealSearchIndexPage);
//        assertEquals(dealSearchIndexPage, dealService.getAllSearchDeals(pageable, CATEGORY_ID, MERCHANT_ID, BRAND_ID,
//                SEARCH_TERM, false, true, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnActiveDealsIdList_When_ActiveBankIdAndMerchantIdAreProvided() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        List<String> dealsIdList = new ArrayList<>();
//        dealsIdList.add(DEAL_ID);
//        Page<BankDeal> bankDeals = getSampleBankDealPage();
//        when(dealSearchService.getActiveDealsForActiveBank(BANK_ID)).thenReturn(dealsIdList);
//        when(bankDealRepository.findByMerchantIdAndIdIn(pageable, MERCHANT_ID, dealsIdList)).thenReturn(bankDeals);
//        assertEquals(bankDeals, dealService.getActiveDealsForActiveBankAndMerchant(pageable, BANK_ID, MERCHANT_ID));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_GetActiveDealsForActiveBankAndMerchantIsFailed() {
//        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
//        when(bankDealRepository.findByMerchantIdAndIdIn(any(Pageable.class), anyString(), anyList())).thenThrow(new DataAccessException("ERROR") {
//        });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealService.getActiveDealsForActiveBankAndMerchant(pageable, BANK_ID, MERCHANT_ID));
//        assertEquals("Getting active deals for active bank & merchant was failed.", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_ReturnBulkMerchantList_When_ValidMerchantIdsAreProvided() {
//        List<String> merchantIdsList = new ArrayList<>();
//        merchantIdsList.add(MERCHANT_ID);
//        List<DealSearchIndex> dealSearchIndexList = getSampleDealList();
//        when(dealSearchIndexRepository.getApprovedValidDealsByMerchantIds(anyLong(), anyList()))
//                .thenReturn(dealSearchIndexList);
//        assertEquals(dealSearchIndexList, dealService.getDealsForBulkMerchantIds(merchantIdsList, TIME_ZONE));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_GetDealsForBulkMerchantIdsIsFailed() {
//        List<String> merchantIdsList = new ArrayList<>();
//        merchantIdsList.add(MERCHANT_ID);
//        when(dealSearchIndexRepository.getApprovedValidDealsByMerchantIds(anyLong(), anyList()))
//                .thenThrow(new DataAccessException("ERROR") {
//                });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealService.getDealsForBulkMerchantIds(merchantIdsList, TIME_ZONE));
//        assertEquals("Getting deals for bulk merchantIds was failed. ", kupiCoreException.getMessage());
//    }
//
//    @Test
//    void Should_ReturnDeal_When_UserTypeIsMerchantForUpdateApprovalOfDeal() {
//        Deal deal = getSampleDeal();
//        deal.setExpiredOn(System.currentTimeMillis() + 10000);
//        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
//        dealApprovalStatusUpdateRequestDto.setApprovalStatus(ApprovalStatus.APPROVED);
//        SubscriptionRenewal subscriptionRenewal = getSampleSubscriptionRenewal();
//        when(dealRepository.findPendingDealById(DEAL_ID)).thenReturn(Optional.of(deal));
//        when(subscriberService.checkSubscriptionPlanForDeal(anyString())).thenReturn(subscriptionRenewal);
//        dealService.updateApprovalOfDeal(dealApprovalStatusUpdateRequestDto, ACCESS_TOKEN, UserType.MERCHANT);
//        verify(dealSearchService, times(1)).updateApprovalStatus(dealApprovalStatusUpdateRequestDto);
//        verify(dealApprovalService).sendMailAndSmsToMerchant(deal, dealApprovalStatusUpdateRequestDto, ACCESS_TOKEN);
//        assertEquals(deal, dealService.updateApprovalOfDeal(dealApprovalStatusUpdateRequestDto, ACCESS_TOKEN, UserType.MERCHANT));
//    }
//
//    @Test
//    void Should_ReturnDeal_When_UserTypeIsBankForUpdateApprovalOfDeal() {
//        Deal deal = getSampleDeal();
//        deal.setMerchantId(BANK_ID);
//        deal.setExpiredOn(System.currentTimeMillis() + 10000);
//        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
//        dealApprovalStatusUpdateRequestDto.setApprovalStatus(ApprovalStatus.APPROVED);
//        SubscriptionRenewal subscriptionRenewal = getSampleSubscriptionRenewal();
//        when(bankDealRepository.findPendingDealById(DEAL_ID)).thenReturn(Optional.of(deal));
//        when(subscriberService.checkSubscriptionPlanForDeal(anyString())).thenReturn(subscriptionRenewal);
//        dealService.updateApprovalOfDeal(dealApprovalStatusUpdateRequestDto, ACCESS_TOKEN, UserType.BANK);
//        verify(dealSearchService, times(1)).updateApprovalStatus(dealApprovalStatusUpdateRequestDto);
//        verify(bankDealRepository).save(any(BankDeal.class));
//        verify(dealApprovalService).sendMailAndSmsToMerchant(deal, dealApprovalStatusUpdateRequestDto, ACCESS_TOKEN);
//        assertEquals(deal, dealService.updateApprovalOfDeal(dealApprovalStatusUpdateRequestDto, ACCESS_TOKEN, UserType.BANK));
//    }
//
//    @Test
//    void Should_ThrowExpiredDealApprovalException_When_DealIsAlreadyExpiredForUpdateApprovalOfDeal() {
//        Deal deal = getSampleDeal();
//        deal.setMerchantId(BANK_ID);
//        deal.setExpiredOn(System.currentTimeMillis() - 10000);
//        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
//        dealApprovalStatusUpdateRequestDto.setApprovalStatus(ApprovalStatus.APPROVED);
//        SubscriptionRenewal subscriptionRenewal = getSampleSubscriptionRenewal();
//        when(bankDealRepository.findPendingDealById(DEAL_ID)).thenReturn(Optional.of(deal));
//        when(subscriberService.checkSubscriptionPlanForDeal(anyString())).thenReturn(subscriptionRenewal);
//        ExpiredDealApprovalException expiredDealApprovalException = assertThrows(ExpiredDealApprovalException.class, () ->
//                dealService.updateApprovalOfDeal(dealApprovalStatusUpdateRequestDto, ACCESS_TOKEN, UserType.BANK));
//        assertEquals("Unable to approve expired deal of deal id: " + DEAL_ID, expiredDealApprovalException.getMessage());
//    }
//
//    @Test
//    void Should_ThrowInvalidDealException_When_InvalidDealIdIsProvidedForUpdateApprovalOfDeal() {
//        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
//        dealApprovalStatusUpdateRequestDto.setApprovalStatus(ApprovalStatus.APPROVED);
//        when(bankDealRepository.findPendingDealById(DEAL_ID)).thenReturn(Optional.empty());
//        InvalidDealException invalidDealException = assertThrows(InvalidDealException.class, () ->
//                dealService.updateApprovalOfDeal(dealApprovalStatusUpdateRequestDto, ACCESS_TOKEN, UserType.BANK));
//        assertEquals("Invalid deal Id: " + DEAL_ID, invalidDealException.getMessage());
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_UpdateApprovalOfDealIsFailed() {
//        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
//        dealApprovalStatusUpdateRequestDto.setApprovalStatus(ApprovalStatus.APPROVED);
//        when(bankDealRepository.findPendingDealById(DEAL_ID)).thenThrow(new DataAccessException("ERROR") {
//        });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealService.updateApprovalOfDeal(dealApprovalStatusUpdateRequestDto, ACCESS_TOKEN, UserType.BANK));
//        assertEquals("Read/Write deal from database was failed.", kupiCoreException.getMessage());
//    }
//
//
//    @Test
//    void Should_UpdateDeal_When_UserTypeIsMerchantForUpdateDeal() {
//        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
//        Deal deal = getSampleDeal();
//        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
//        businessMerchantResponseDto.setProfileType(UserType.MERCHANT.name());
//        Set<Category> categorySet = getSampleCategorySet();
//        Set<Brand> brandSet = getSampleBrandSet();
//        when(categoryService.getCategorySetByIds(dealUpdateRequestDto.getCategoryIds())).thenReturn(categorySet);
//        when(brandService.getBrandSetByIdList(dealUpdateRequestDto.getBrandIds())).thenReturn(brandSet);
//        when(dealRepository.save(deal)).thenReturn(deal);
//        assertEquals(deal, dealService.updateDeal(dealUpdateRequestDto, UserType.MERCHANT, deal, businessMerchantResponseDto));
//    }
//
//    @Test
//    void Should_UpdateDeal_When_UserTypeIsBankForUpdateDeal() {
//        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
//        Deal deal = getSampleDeal();
//        BankDeal bankDeal = getSampleBankDeal();
//        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
//        businessMerchantResponseDto.setProfileType(UserType.BANK.name());
//        Set<Category> categorySet = getSampleCategorySet();
//        Set<Brand> brandSet = getSampleBrandSet();
//        when(categoryService.getCategorySetByIds(dealUpdateRequestDto.getCategoryIds())).thenReturn(categorySet);
//        when(brandService.getBrandSetByIdList(dealUpdateRequestDto.getBrandIds())).thenReturn(brandSet);
//        when(bankDealRepository.save(bankDeal)).thenReturn(bankDeal);
//        assertEquals(deal, dealService.updateDeal(dealUpdateRequestDto, UserType.BANK, deal, businessMerchantResponseDto));
//    }
//
//    @Test
//    void Should_ThrowInvalidUserException_When_UserTypeMisMatchWithBusinessMerchantProfile() {
//        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
//        Deal deal = getSampleDeal();
//        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
//        businessMerchantResponseDto.setProfileType(UserType.BANK.name());
//        InvalidUserException invalidUserException = assertThrows(InvalidUserException.class, () ->
//                dealService.updateDeal(dealUpdateRequestDto, UserType.MERCHANT, deal, businessMerchantResponseDto));
//        assertEquals("User type doesn't match with user id.", invalidUserException.getMessage());
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_UpdateDealIsFailed() {
//        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
//        Deal deal = getSampleDeal();
//        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
//        businessMerchantResponseDto.setProfileType(UserType.MERCHANT.name());
//        Set<Category> categorySet = getSampleCategorySet();
//        Set<Brand> brandSet = getSampleBrandSet();
//        when(categoryService.getCategorySetByIds(dealUpdateRequestDto.getCategoryIds())).thenReturn(categorySet);
//        when(brandService.getBrandSetByIdList(dealUpdateRequestDto.getBrandIds())).thenReturn(brandSet);
//        when(dealRepository.save(deal)).thenThrow(new DataAccessException("ERROR") {
//        });
//        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
//                dealService.updateDeal(dealUpdateRequestDto, UserType.MERCHANT, deal, businessMerchantResponseDto));
//        assertEquals("Update deal was failed.", kupiCoreException.getMessage());
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
//
//    /**
//     * This method creates sample BasicMerchantBusinessResponseDto
//     *
//     * @return BasicMerchantBusinessResponseDto
//     */
//    private BasicMerchantBusinessResponseDto getSampleBasicMerchantBusinessResponseDto() {
//        BasicMerchantBusinessResponseDto basicMerchantBusinessResponseDto = new BasicMerchantBusinessResponseDto();
//        basicMerchantBusinessResponseDto.setId("uid-001");
//        basicMerchantBusinessResponseDto.setName("Roy");
//        basicMerchantBusinessResponseDto.setImageUrl("goo.le/image");
//        basicMerchantBusinessResponseDto.setApprovalStatus(ApprovalStatus.APPROVED);
//        basicMerchantBusinessResponseDto.setActive(true);
//        return basicMerchantBusinessResponseDto;
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
////        return new DealRequestDto(
////                "New Year Deal!", "for new year",
////                "The Description", "No Terms And Conditions",
////                imgUrls, "https://github.com/cover.png",
////                12.25, 50,
////                DeductionType.AMOUNT, 2.22, 5.00, 0.0,
////                1667217882000L, 1672488282000L,
////                null, null, // startTime, endTime
////                "uid-0022", brandList, categoryList, null, "uid-2222", false, false);
////    }
//
//    /**
//     * This method creates sample BankDeal
//     *
//     * @return BankDeal
//     */
//    private BankDeal getSampleBankDeal() {
//        Deal deal = getSampleDeal();
//        return new BankDeal(deal);
//    }
//
//    /**
//     * This method creates sample deal page
//     *
//     * @return dealPage
//     */
//    private Page<Deal> getSampleDealPage() {
//        Deal deal = getSampleDeal();
//        List<Deal> dealList = new ArrayList<>();
//        dealList.add(deal);
//        return new PageImpl<>(dealList);
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
//        dealSearchIndex.setSearchImageUrls("[https://objects-kupi-dev.s3.ap-southeast-1.amazonaws.com/fid-a61e4565-dd03-4bb1-a123-90460ca3def5.png]");
//        dealSearchIndex.setSubTitle("bank deal");
//        dealSearchIndex.setTermsAndConditions("no conditions ");
//        List<DealSearchIndex> dealSearchIndexList = new ArrayList<>();
//        dealSearchIndexList.add(dealSearchIndex);
//        return new PageImpl<>(dealSearchIndexList);
//    }
//
//    /**
//     * This method creates the sample bank deal page
//     *
//     * @return bankDeal page
//     */
//    private Page<BankDeal> getSampleBankDealPage() {
//        List<BankDeal> bankDealList = new ArrayList<>();
//        Deal deal = getSampleDeal();
//        bankDealList.add(new BankDeal(deal));
//        return new PageImpl<>(bankDealList);
//    }
//
//    /**
//     * This method creates the deal list
//     *
//     * @return DealSearchIndex list
//     */
//    private List<DealSearchIndex> getSampleDealList() {
//        DealSearchIndex dealSearchIndex = new DealSearchIndex();
//        dealSearchIndex.setId("did-02104ca3-41e4-4cb2-8a06-8ae0805ef6dc");
//        dealSearchIndex.setApprovalStatus(ApprovalStatus.APPROVED);
//        dealSearchIndex.setDeleted(false);
//        dealSearchIndex.setTitle("Bank deal to be approved");
//        dealSearchIndex.setActiveMerchant(true);
//        dealSearchIndex.setSearchImageUrls("[https://objects-kupi-dev.s3.ap-southeast-1.amazonaws.com/fid-a61e4565-dd03-4bb1-a123-90460ca3def5.png]");
//        dealSearchIndex.setSubTitle("bank deal");
//        dealSearchIndex.setTermsAndConditions("no conditions ");
//        List<DealSearchIndex> dealSearchIndexList = new ArrayList<>();
//        dealSearchIndexList.add(dealSearchIndex);
//        return dealSearchIndexList;
//    }
//
//    /**
//     * This method creates the sample DealApprovalStatusUpdateRequestDto
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
//     * This method creates sample subscription renewal
//     *
//     * @return SubscriptionRenewal
//     */
//    private SubscriptionRenewal getSampleSubscriptionRenewal() {
//        SubscriptionRenewal subscriptionRenewal = new SubscriptionRenewal();
//        subscriptionRenewal.setId(SUBSCRIPTION_ID);
//        subscriptionRenewal.setSubscriptionPlanId("spid-c9d5b89a-5125-4838-b3cb-58bc8619589f");
//        subscriptionRenewal.setPlanType(PlanType.MONTHLY);
//        subscriptionRenewal.setUnsubscribed(false);
//        subscriptionRenewal.setRemainingDealsPerMonth(10);
//        return subscriptionRenewal;
//    }
//
//    /**
//     * This method creates sample deal update request dto,
//     *
//     * @return DealUpdateRequestDto
//     */
//    private DealUpdateRequestDto getSampleDealUpdateRequestDto() {
//        List<String> brandList = new ArrayList<>();
//        brandList.add("bid-2225");
//        DealUpdateRequestDto dealUpdateRequestDto = new DealUpdateRequestDto(DEAL_ID);
//        dealUpdateRequestDto.setTitle("New Year Deal");
//        dealUpdateRequestDto.setOriginalPrice(12.25);
//        dealUpdateRequestDto.setQuantity(50);
//        dealUpdateRequestDto.setDeductionType(DeductionType.AMOUNT);
//        dealUpdateRequestDto.setValidFrom(1667217882000L);
//        dealUpdateRequestDto.setExpiredOn(1672488282000L);
//        dealUpdateRequestDto.setMerchantId("uid-0022");
//        dealUpdateRequestDto.setBrandIds(brandList);
//        return dealUpdateRequestDto;
//    }
//
//    /**
//     * This method creates sample category set
//     *
//     * @return categorySet
//     */
//    private Set<Category> getSampleCategorySet() {
//        Set<Category> categorySet = new HashSet<>();
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
//                1667217882000L,                // updatedAt
//                true                           // isPopular
//        ));
//
//        return categorySet;
//    }
//
//    /**
//     * This method creates sample brand set
//     *
//     * @return brandSet
//     */
//    private Set<Brand> getSampleBrandSet() {
//        Set<Brand> brandSet = new HashSet<>();
//        brandSet.add(new Brand("bid-001", "new Brand", "description",
//                "http://google.com", 1667217882000L, 1672488282000L));
//        return brandSet;
//    }
//}