package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Category;
import com.kupi.cc.base.domain.entity.CategoryBrandMerchant;
import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.domain.response.*;
import com.kupi.cc.base.domain.response.dashboard.AuthSummaryResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.AdminDashboardSummaryResponseDto;
import com.kupi.cc.base.domain.response.dashboardV2.MerchantSummaryResponseDto;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.CategoryType;
import com.kupi.cc.base.enums.SubscriptionStatus;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link SummaryService} class
 */
class SummaryServiceTest {

    private static final String USER_ID = "uid-123456789";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String AUTH_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String TODAY_SUMMARY_TITLE = "Today's Summary";
    private static final String TOTAL_SUMMARY_TITLE = "My Summary";
    private static final String TOTAL_SUMMARY_TITLE_ADMIN = "App Summary";
    private static final String FAILED = "failed";
    private static final String CATEGORY = "CATEGORY";
    private static final int ANY_INT = 20;
    private static final long ANY_LONG = 20;

    @Mock
    private DealRepository dealRepository;
    @Mock
    private RequestADealRepository requestADealRepository;
    @Mock
    private BrandRepository brandRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private DealSearchIndexRepository dealSearchIndexRepository;
    @Mock
    private AuthUserService authUserService;
    @Mock
    private CategoryBrandMerchantRepository categoryBrandMerchantRepository;
    @Mock
    private CreditCardRepository creditCardRepository;
    @Mock
    private BankDealRepository bankDealRepository;
    @Mock
    private SubscriberRepository subscriberRepository;
    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock
    private DealsOfTheDayEventRepository dealsOfTheDayEventRepository;
    @Mock
    private CouponRepository couponRepository;
    @Mock
    private DealService dealService;
    @Mock
    private CouponStatisticsService couponStatisticsService;
    @Mock
    private DealViewService dealViewService;
    private SummaryService summaryService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        summaryService = new SummaryService(dealRepository, requestADealRepository, brandRepository, categoryRepository,
                dealSearchIndexRepository, dealService, couponStatisticsService, authUserService,
                categoryBrandMerchantRepository, creditCardRepository, bankDealRepository, subscriberRepository,
                subscriptionPlanRepository, dealsOfTheDayEventRepository, couponRepository, dealViewService);
    }


    /**
     * Get full summary
     */
//    @Test
    void Should_ReturnFullSummaryResponseDto_When_GettingFullSummaryForAdminIsSuccessful() {
        when(authUserService.getTodaySummaryResponse(anyString(), anyString(), any(UserType.class)))
                .thenReturn(getSampleAuthSummaryResponseDto());
        getTodaySubscriptionsStubbingMethods();
        getTodayDealSummaryStubbingMethodsForAdmin();
        getTodayCategoryStubbingMethodsForAdmin();
        getTotalSubscriptionsStubbingMethods();
        getTotalDealStubbingMethodsForAdmin();
        getTotalCategoryStubbingMethodsForAdmin();

        assertEquals(TODAY_SUMMARY_TITLE, summaryService.getFullSummary(TIME_ZONE, UserType.ADMIN, USER_ID, AUTH_TOKEN)
                .getTodaySummary().getTitle());
        assertEquals(TOTAL_SUMMARY_TITLE_ADMIN, summaryService.getFullSummary(TIME_ZONE, UserType.ADMIN, USER_ID,
                AUTH_TOKEN).getTotalSummary().getTitle());
    }

    @Test
    void Should_ReturnFullSummaryResponseDto_When_GettingFullSummaryForBankIsSuccessful() {
        when(authUserService.getTodaySummaryResponse(anyString(), anyString(), any(UserType.class)))
                .thenReturn(getSampleAuthSummaryResponseDto());
        getTodaySubscriptionsStubbingMethods();
        getTodayDealSummaryStubbingMethodsForBank();
        getTodayCategoryStubbingMethodsForBank();
        getTotalSubscriptionsStubbingMethods();
        getTotalDealStubbingMethodsForBank();
        when(categoryBrandMerchantRepository.findByMerchantId(anyString()))
                .thenReturn(Optional.of(new CategoryBrandMerchant()));
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenReturn(getSampleBusinessMerchantResponseDto());

        assertEquals(TODAY_SUMMARY_TITLE, summaryService.getFullSummary(TIME_ZONE, UserType.BANK, USER_ID, AUTH_TOKEN)
                .getTodaySummary().getTitle());
        assertEquals(TOTAL_SUMMARY_TITLE, summaryService.getFullSummary(TIME_ZONE, UserType.BANK, USER_ID, AUTH_TOKEN)
                .getTotalSummary().getTitle());
    }

    @Test
    void Should_ReturnFullSummaryResponseDto_When_GettingFullSummaryForMerchantIsSuccessful() {
        when(authUserService.getTodaySummaryResponse(anyString(), anyString(), any(UserType.class)))
                .thenReturn(getSampleAuthSummaryResponseDto());
        getTodaySubscriptionsStubbingMethods();
        getTodayDealSummaryStubbingMethodsForMerchant();
        getTodayCategoryStubbingMethodsForMerchant();
        getTotalSubscriptionsStubbingMethods();
        getTotalDealStubbingMethodsForMerchant();
        getTotalCategoryStubbingMethodsForMerchant();
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString()))
                .thenReturn(getSampleBusinessMerchantResponseDto());

        assertEquals(TODAY_SUMMARY_TITLE, summaryService.getFullSummary(TIME_ZONE, UserType.MERCHANT, USER_ID, AUTH_TOKEN)
                .getTodaySummary().getTitle());
        assertEquals(TOTAL_SUMMARY_TITLE, summaryService.getFullSummary(TIME_ZONE, UserType.MERCHANT, USER_ID, AUTH_TOKEN)
                .getTotalSummary().getTitle());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingFullSummaryForGettingTodaySubscriptionIsFailed() {
        when(authUserService.getTodaySummaryResponse(anyString(), anyString(), any(UserType.class)))
                .thenReturn(getSampleAuthSummaryResponseDto());
        when(subscriptionPlanRepository.countSubscriptionPlansByUserRoleAndCreatedAtGreaterThanEqual(any(UserType.class),
                anyLong())).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> summaryService
                .getFullSummary(TIME_ZONE, UserType.ADMIN, USER_ID, AUTH_TOKEN));
        assertEquals("Returning today subscriptions from database was failed.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingFullSummaryForGettingTodayDealSummaryIsFailed() {
        when(authUserService.getTodaySummaryResponse(anyString(), anyString(), any(UserType.class)))
                .thenReturn(getSampleAuthSummaryResponseDto());
        getTodaySubscriptionsStubbingMethods();
        when(dealRepository.countByCreatedAtGreaterThanEqualAndIsDeletedFalse(anyLong())).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> summaryService
                .getFullSummary(TIME_ZONE, UserType.ADMIN, USER_ID, AUTH_TOKEN));
        assertEquals("Returning today deal from database was failed.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingFullSummaryForGettingTodayCategoryIsFailed() {
        when(authUserService.getTodaySummaryResponse(anyString(), anyString(), any(UserType.class)))
                .thenReturn(getSampleAuthSummaryResponseDto());
        getTodaySubscriptionsStubbingMethods();
        getTodayDealSummaryStubbingMethodsForAdmin();
        when(creditCardRepository.countByCreatedAtGreaterThanEqual(anyLong())).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> summaryService
                .getFullSummary(TIME_ZONE, UserType.ADMIN, USER_ID, AUTH_TOKEN));
        assertEquals("Returning today category count from database was failed.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingFullSummaryForGettingTotalSubscriptionIsFailed() {
        when(authUserService.getTodaySummaryResponse(anyString(), anyString(), any(UserType.class)))
                .thenReturn(getSampleAuthSummaryResponseDto());
        getTodaySubscriptionsStubbingMethods();
        getTodayDealSummaryStubbingMethodsForAdmin();
        getTodayCategoryStubbingMethodsForAdmin();
        when(subscriptionPlanRepository.countSubscriptionPlansByUserRole(any(UserType.class)))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> summaryService
                .getFullSummary(TIME_ZONE, UserType.ADMIN, USER_ID, AUTH_TOKEN));
        assertEquals("Returning total subscriptions from database was failed.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingFullSummaryForGettingTotalDealIsFailed() {
        when(authUserService.getTodaySummaryResponse(anyString(), anyString(), any(UserType.class)))
                .thenReturn(getSampleAuthSummaryResponseDto());
        getTodaySubscriptionsStubbingMethods();
        getTodayDealSummaryStubbingMethodsForAdmin();
        getTodayCategoryStubbingMethodsForAdmin();
        getTotalSubscriptionsStubbingMethods();
        when(dealRepository.countByIsDeletedFalse()).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> summaryService
                .getFullSummary(TIME_ZONE, UserType.ADMIN, USER_ID, AUTH_TOKEN));
        assertEquals("Returning total deal from database was failed.", exception.getMessage());
    }

    //    @Test
    void Should_ThrowKupiCoreException_When_GettingFullSummaryForGettingTotalCategoryIsFailed() {
        when(authUserService.getTodaySummaryResponse(anyString(), anyString(), any(UserType.class)))
                .thenReturn(getSampleAuthSummaryResponseDto());
        getTodaySubscriptionsStubbingMethods();
        getTodayDealSummaryStubbingMethodsForAdmin();
        getTodayCategoryStubbingMethodsForAdmin();
        getTotalSubscriptionsStubbingMethods();
        getTotalDealStubbingMethodsForAdmin();
        when(categoryBrandMerchantRepository.findByMerchantId(USER_ID)).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> summaryService
                .getFullSummary(TIME_ZONE, UserType.ADMIN, USER_ID, AUTH_TOKEN));
        assertEquals("Returning total category from database was failed.", exception.getMessage());
    }

    private AuthSummaryResponseDto getSampleAuthSummaryResponseDto() {
        return new AuthSummaryResponseDto(ANY_LONG, ANY_LONG, ANY_LONG, ANY_LONG, ANY_LONG, ANY_LONG, ANY_LONG,
                ANY_LONG);
    }

    private BusinessMerchantResponseDto getSampleBusinessMerchantResponseDto() {
        BusinessMerchantResponseDto businessMerchantResponseDto = new BusinessMerchantResponseDto();
        businessMerchantResponseDto.setCreatedAt(getSampleDateResponseDto());
        return businessMerchantResponseDto;
    }

    private DateResponseDto getSampleDateResponseDto() {
        DateResponseDto dateResponseDto = new DateResponseDto();
        dateResponseDto.setDisplayDate("");
        return dateResponseDto;
    }

    private CategoryBrandMerchant getSampleCategoryBrandMerchant() {
        CategoryBrandMerchant categoryBrandMerchant = new CategoryBrandMerchant();
        categoryBrandMerchant.setCategories(getSampleCategorySet());
        return categoryBrandMerchant;
    }

    private Set<String> getSampleCategorySet() {
        Set<String> categorySet = new HashSet<>();
        categorySet.add(CATEGORY);
        return categorySet;
    }

    private Category getSampleCategory() {
        Category category = new Category();
        category.setCategoryType(CategoryType.NORMAL);
        return category;
    }

    private void getTodaySubscriptionsStubbingMethods() {
        when(subscriptionPlanRepository.countSubscriptionPlansByUserRoleAndCreatedAtGreaterThanEqual(any(UserType.class),
                anyLong())).thenReturn(ANY_LONG);
        when(subscriberRepository.countSubscribesByUserTypeAndCreatedAtGreaterThanEqualAndIsDeletedFalse(any(UserType.class), anyLong()))
                .thenReturn(ANY_LONG);
        when(subscriberRepository.countSubscribesByUserTypeAndUpdatedAtGreaterThanEqualAndIsDeletedTrue(any(UserType.class), anyLong()))
                .thenReturn(ANY_LONG);
    }

    private void getTodayDealSummaryStubbingMethodsForAdmin() {
        when(dealRepository.countByCreatedAtGreaterThanEqualAndIsDeletedFalse(anyLong())).thenReturn(ANY_INT);
        when(bankDealRepository.countByCreatedAtGreaterThanEqual(anyLong())).thenReturn(ANY_LONG);
        when(dealRepository.countByExpiredOnGreaterThanEqualAndExpiredOnLessThan(anyLong(), anyLong()))
                .thenReturn(ANY_INT);
        when(bankDealRepository.countByExpiredOnGreaterThanEqualAndExpiredOnLessThan(anyLong(), anyLong()))
                .thenReturn(ANY_LONG);
        when(requestADealRepository.countByCreatedAtGreaterThanEqual(anyLong())).thenReturn(ANY_LONG);
    }

    private void getTodayCategoryStubbingMethodsForAdmin() {
        when(creditCardRepository.countByCreatedAtGreaterThanEqual(anyLong())).thenReturn(ANY_LONG);
        when(categoryRepository.countByCreatedAtGreaterThanEqual(anyLong())).thenReturn(ANY_LONG);
        when(brandRepository.countByCreatedAtGreaterThanEqual(anyLong())).thenReturn(ANY_LONG);
    }

    private void getTotalSubscriptionsStubbingMethods() {
        when(subscriptionPlanRepository.countSubscriptionPlansByUserRole(any(UserType.class))).thenReturn(ANY_LONG);
        when(subscriptionPlanRepository.countSubscriptionPlansByUserRoleAndStatus(any(UserType.class),
                any(SubscriptionStatus.class))).thenReturn(ANY_LONG);
        when(subscriberRepository.countSubscribesByUserTypeAndIsDeletedFalse(any(UserType.class))).thenReturn(ANY_LONG);
        when(subscriberRepository.countSubscribesByUserTypeAndIsDeletedTrue(any(UserType.class))).thenReturn(ANY_LONG);
    }

    private void getTotalDealStubbingMethodsForAdmin() {
        when(dealRepository.count()).thenReturn(ANY_LONG);
        when(bankDealRepository.count()).thenReturn(ANY_LONG);
        when(dealSearchIndexRepository.getActiveDealsCount(anyLong(), any(UserType.class))).thenReturn(ANY_LONG);
        when(requestADealRepository.count()).thenReturn(ANY_LONG);
    }

    private void getTotalCategoryStubbingMethodsForAdmin() {
        when(categoryBrandMerchantRepository.findByMerchantId(anyString()))
                .thenReturn(Optional.of(new CategoryBrandMerchant()));
        when(categoryRepository.count()).thenReturn(ANY_LONG);
        when(categoryRepository.countByExpiryDateIsNullOrExpiryDateGreaterThan(anyLong())).thenReturn(ANY_INT);
        when(brandRepository.count()).thenReturn(ANY_LONG);
        when(creditCardRepository.count()).thenReturn(ANY_LONG);
    }

    private void getTodayDealSummaryStubbingMethodsForBank() {
        when(bankDealRepository.countByBankIdAndCreatedAtGreaterThanEqual(anyString(), anyLong())).thenReturn(ANY_LONG);
        when(bankDealRepository.countByBankIdAndExpiredOnGreaterThanEqualAndExpiredOnLessThan(anyString(), anyLong(),
                anyLong())).thenReturn(ANY_LONG);
        when(requestADealRepository.countByMerchantIdAndCreatedAtGreaterThanEqual(anyString(), anyLong()))
                .thenReturn(ANY_LONG);
        when(dealRepository.countByCreatedAtGreaterThanEqualAndIsDeletedFalse(anyLong())).thenReturn(ANY_INT);
    }

    private void getTodayCategoryStubbingMethodsForBank() {
        when(creditCardRepository.countByBankIdAndCreatedAtGreaterThanEqual(anyString(), anyLong())).thenReturn(ANY_LONG);
        when(categoryRepository.countByCreatedAtGreaterThanEqual(anyLong())).thenReturn(ANY_LONG);
        when(brandRepository.countByCreatedAtGreaterThanEqual(anyLong())).thenReturn(ANY_LONG);
    }

    private void getTotalDealStubbingMethodsForBank() {
        when(dealSearchIndexRepository.getActiveDealsCount(anyLong(), any(UserType.class))).thenReturn(ANY_LONG);
        when(dealRepository.count()).thenReturn(ANY_LONG);
        when(dealSearchIndexRepository.getTotalNumberOfActiveDealsForMerchantId(anyString(), anyLong()))
                .thenReturn(ANY_INT);
        when(requestADealRepository.countByMerchantId(anyString())).thenReturn(ANY_LONG);
        when(bankDealRepository.countByBankId(anyString())).thenReturn(ANY_LONG);
    }

    private void getTodayDealSummaryStubbingMethodsForMerchant() {
        when(dealRepository.countByMerchantIdAndCreatedAtGreaterThanEqualAndIsDeletedFalse(anyString(), anyLong()))
                .thenReturn(ANY_INT);
        when(dealRepository.countByMerchantIdAndExpiredOnGreaterThanEqualAndExpiredOnLessThan(anyString(), anyLong(),
                anyLong())).thenReturn(ANY_INT);
        when(requestADealRepository.countByMerchantIdAndCreatedAtGreaterThanEqual(anyString(), anyLong()))
                .thenReturn(ANY_LONG);
    }

    private void getTodayCategoryStubbingMethodsForMerchant() {
        when(categoryRepository.countByCreatedAtGreaterThanEqual(anyLong())).thenReturn(ANY_LONG);
        when(brandRepository.countByCreatedAtGreaterThanEqual(anyLong())).thenReturn(ANY_LONG);
    }

    private void getTotalDealStubbingMethodsForMerchant() {
        when(dealSearchIndexRepository.getTotalNumberOfActiveDealsForMerchantId(anyString(), anyLong()))
                .thenReturn(ANY_INT);
        when(requestADealRepository.countByMerchantId(anyString())).thenReturn(ANY_LONG);
        when(dealRepository.count()).thenReturn(ANY_LONG);
    }

    private void getTotalCategoryStubbingMethodsForMerchant() {
        when(categoryBrandMerchantRepository.findByMerchantId(anyString()))
                .thenReturn(Optional.of(getSampleCategoryBrandMerchant()));
        when(categoryRepository.findById(anyString())).thenReturn(Optional.of(getSampleCategory()));
    }

    /**
     * Get admin summary
     */
//    @Test
    void Should_ReturnAdminDashboardSummaryResponseDto_When_GettingAdminSummaryIsSuccessful() {
        when(authUserService.getTodaySummaryResponse(anyString(), anyString(), any(UserType.class)))
                .thenReturn(getSampleAuthSummaryResponseDto());
        getTotalDealStubbingMethodsForAdmin();
        when(couponRepository.countActiveCoupons(anyLong())).thenReturn(ANY_LONG);

        AdminDashboardSummaryResponseDto result = summaryService.getAdminSummary(USER_ID, AUTH_TOKEN, UserType.ADMIN, TIME_ZONE);

        assertEquals(ANY_LONG, result.getTotalMerchants());
        assertEquals(ANY_LONG, result.getTotalActiveMobileUsers());
        assertEquals(ANY_LONG, result.getTotalLiveDeals());
        assertEquals(ANY_LONG, result.getTotalRedeemCoupons());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingAdminSummaryForAuthUserServiceIsFailed() {
        when(authUserService.getTodaySummaryResponse(anyString(), anyString(), any(UserType.class)))
                .thenThrow(new DataAccessException(FAILED) {
                });

        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> summaryService
                .getAdminSummary(USER_ID, AUTH_TOKEN, UserType.ADMIN, TIME_ZONE));
        assertEquals("Fetching admin's dashboard values failed.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingAdminSummaryForGetTotalDealIsFailed() {
        when(authUserService.getTodaySummaryResponse(anyString(), anyString(), any(UserType.class)))
                .thenReturn(getSampleAuthSummaryResponseDto());
        when(dealRepository.countByIsDeletedFalse()).thenThrow(new DataAccessException(FAILED) {
        });

        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> summaryService
                .getAdminSummary(USER_ID, AUTH_TOKEN, UserType.ADMIN, TIME_ZONE));
        assertEquals("Returning total deal from database was failed.", exception.getMessage());
    }

    //    @Test
    void Should_ThrowKupiCoreException_When_GettingAdminSummaryForCountActiveCouponsIsFailed() {
        when(authUserService.getTodaySummaryResponse(anyString(), anyString(), any(UserType.class)))
                .thenReturn(getSampleAuthSummaryResponseDto());
        getTotalDealStubbingMethodsForAdmin();
        when(couponRepository.countActiveCoupons(anyLong())).thenThrow(new DataAccessException(FAILED) {
        });

        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> summaryService
                .getAdminSummary(USER_ID, AUTH_TOKEN, UserType.ADMIN, TIME_ZONE));
        assertEquals("Fetching admin's dashboard values failed.", exception.getMessage());
    }

    /**
     * Get merchant and deal count
     */
    @Test
    void Should_ReturnMerchantTableListResponseDto_When_GettingMerchantAndDealCountIsSuccessful() {
        String searchTerm = "test";
        int page = 0;
        int size = 10;
        List<AuthMerchantListResponseDto> merchants = new ArrayList<>();
        merchants.add(getSampleAuthMerchantListResponseDto());
        AllMerchantListResponseDto allMerchantListResponseDto = new AllMerchantListResponseDto(1L, page, size, merchants);

        when(authUserService.getAllMerchants(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(allMerchantListResponseDto);
        when(dealRepository.countByMerchantIdAndIsDeletedFalse(anyString())).thenReturn(ANY_LONG);
        when(dealSearchIndexRepository.getTotalNumberOfActiveDealsForMerchantId(anyString(), anyLong()))
                .thenReturn(ANY_INT);

        MerchantTableListResponseDto result = summaryService.getMerchantAndDealCount(searchTerm, page, size, USER_ID, TIME_ZONE);

        assertEquals(1L, result.getTotalItems());
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(1, result.getMerchants().size());
    }

    @Test
    void Should_ReturnMerchantTableListResponseDtoWithEmptyMerchants_When_GettingMerchantAndDealCountWithEmptyMerchants() {
        String searchTerm = "test";
        int page = 0;
        int size = 10;
        AllMerchantListResponseDto allMerchantListResponseDto = new AllMerchantListResponseDto(0L, page, size, new ArrayList<>());

        when(authUserService.getAllMerchants(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(allMerchantListResponseDto);

        MerchantTableListResponseDto result = summaryService.getMerchantAndDealCount(searchTerm, page, size, USER_ID, TIME_ZONE);

        assertEquals(0L, result.getTotalItems());
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(0, result.getMerchants().size());
    }

    @Test
    void Should_ReturnMerchantTableListResponseDto_When_GettingMerchantAndDealCountWithNullSearchTermIsSuccessful() {
        int page = 0;
        int size = 10;
        List<AuthMerchantListResponseDto> merchants = new ArrayList<>();
        merchants.add(getSampleAuthMerchantListResponseDto());
        AllMerchantListResponseDto allMerchantListResponseDto = new AllMerchantListResponseDto(1L, page, size, merchants);

        when(authUserService.getAllMerchants(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(allMerchantListResponseDto);
        when(dealRepository.countByMerchantIdAndIsDeletedFalse(anyString())).thenReturn(ANY_LONG);
        when(dealSearchIndexRepository.getTotalNumberOfActiveDealsForMerchantId(anyString(), anyLong()))
                .thenReturn(ANY_INT);

        MerchantTableListResponseDto result = summaryService.getMerchantAndDealCount(null, page, size, USER_ID, TIME_ZONE);

        assertEquals(1L, result.getTotalItems());
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(1, result.getMerchants().size());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingMerchantAndDealCountForGetAllMerchantsIsFailed() {
        String searchTerm = "test";
        int page = 0;
        int size = 10;

        when(authUserService.getAllMerchants(anyString(), anyInt(), anyInt(), anyString()))
                .thenThrow(new KupiCoreException(FAILED));

        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> summaryService
                .getMerchantAndDealCount(searchTerm, page, size, USER_ID, TIME_ZONE));
        assertEquals("Retrieving merchants and deals count was failed.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingMerchantAndDealCountForCountByMerchantIdIsFailed() {
        String searchTerm = "test";
        int page = 0;
        int size = 10;
        List<AuthMerchantListResponseDto> merchants = new ArrayList<>();
        merchants.add(getSampleAuthMerchantListResponseDto());
        AllMerchantListResponseDto allMerchantListResponseDto = new AllMerchantListResponseDto(1L, page, size, merchants);

        when(authUserService.getAllMerchants(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(allMerchantListResponseDto);
        when(dealRepository.countByMerchantIdAndIsDeletedFalse(anyString()))
                .thenThrow(new KupiCoreException(FAILED));

        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> summaryService
                .getMerchantAndDealCount(searchTerm, page, size, USER_ID, TIME_ZONE));
        assertEquals("Retrieving merchants and deals count was failed.", exception.getMessage());
    }

    private AuthMerchantListResponseDto getSampleAuthMerchantListResponseDto() {
        AuthMerchantListResponseDto merchant = new AuthMerchantListResponseDto();
        merchant.setProfile(getSampleAuthBasicUserResponseDto());
        merchant.setBusiness(getSampleAuthBasicBusinessResponseDto());
        return merchant;
    }

    private AuthBasicUserResponseDto getSampleAuthBasicUserResponseDto() {
        AuthBasicUserResponseDto profile = new AuthBasicUserResponseDto();
        profile.setId(USER_ID);
        profile.setName("Test User");
        profile.setImageUrl("http://example.com/image.jpg");
        profile.setApprovalStatus(ApprovalStatus.APPROVED);
        profile.setJoinedOn(getSampleDateResponseDto());
        profile.setActive(true);
        return profile;
    }

    private AuthBasicBusinessResponseDto getSampleAuthBasicBusinessResponseDto() {
        AuthBasicBusinessResponseDto business = new AuthBasicBusinessResponseDto();
        business.setId("bid-123456789");
        business.setName("Test Business");
        business.setImageUrl("http://example.com/business.jpg");
        business.setApprovalStatus(ApprovalStatus.APPROVED);
        return business;
    }

    /**
     * Get merchant summary
     */
    @Test
    void Should_ReturnMerchantSummaryResponseDto_When_GettingMerchantSummaryIsSuccessful() {
        String merchantId = "mid-123456789";
        long startMillis = System.currentTimeMillis() - 86400000L; // 1 day ago
        long endMillis = System.currentTimeMillis();
        int activeDeals = 10;
        int dealsOfTheDay = 5;
        int couponCount = 8;
        int redeemedCouponCount = 3;

        // Mock active deals with coupon-based filtering
        Page<DealSearchIndex> activeDealsPage = createDealSearchIndexPage(activeDeals);
        when(dealSearchIndexRepository.getActiveMerchantDeals(any(Pageable.class), anyString(), anyLong()))
                .thenReturn(activeDealsPage);
        when(couponStatisticsService.getCouponStatisticsByDealIdList(anyList()))
                .thenReturn(new ArrayList<>());
        when(dealService.removeCouponNotAvailableDeals(any(Page.class), anyList()))
                .thenReturn(activeDealsPage);

        when(dealsOfTheDayEventRepository.getDealsOfTheDayCountsInRange(anyString(), any(java.sql.Date.class), any(java.sql.Date.class)))
                .thenReturn(List.of(dealsOfTheDay));
        when(couponRepository.countActiveCouponsOverlappingRange(anyString(), anyLong(), anyLong()))
                .thenReturn((long) couponCount);
        when(couponRepository.countRedeemedCouponsByMerchantIdAndDateRange(anyString(), anyLong(), anyLong()))
                .thenReturn((long) redeemedCouponCount);

        MerchantSummaryResponseDto result = summaryService.getMerchantSummary(merchantId, startMillis, endMillis, TIME_ZONE);

        assertEquals(activeDeals, result.getTotalActiveDeals());
        assertEquals(dealsOfTheDay, result.getDealsOfTheDay());
        assertEquals(couponCount, result.getActiveCoupon());
        assertEquals(redeemedCouponCount, result.getRedeemedCoupon());
    }

    @Test
    void Should_ReturnMinusOneForActiveDeals_When_EndMillisIsBeforeTodayStart() {
        String merchantId = "mid-123456789";
        long todayStartMillis = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        long startMillis = todayStartMillis - 86400000L; // 1 day before today start
        long endMillis = todayStartMillis - 1; // Just before today start
        int dealsOfTheDay = 0;
        int couponCount = 0;
        int redeemedCouponCount = 0;

        // No active deals will be counted as endMillis < todayStartMillis, so no repo interaction needed
        when(dealsOfTheDayEventRepository.getDealsOfTheDayCountsInRange(anyString(), any(java.sql.Date.class), any(java.sql.Date.class)))
                .thenReturn(List.of(dealsOfTheDay));
        when(couponRepository.countActiveCouponsOverlappingRange(anyString(), anyLong(), anyLong()))
                .thenReturn((long) couponCount);
        when(couponRepository.countRedeemedCouponsByMerchantIdAndDateRange(anyString(), anyLong(), anyLong()))
                .thenReturn((long) redeemedCouponCount);

        MerchantSummaryResponseDto result = summaryService.getMerchantSummary(merchantId, startMillis, endMillis, TIME_ZONE);

        assertEquals(-1, result.getTotalActiveDeals());
        assertEquals(dealsOfTheDay, result.getDealsOfTheDay());
        assertEquals(couponCount, result.getActiveCoupon());
        assertEquals(redeemedCouponCount, result.getRedeemedCoupon());
    }

    @Test
    void Should_ReturnCountFromDateRange_When_StartMillisIsAfterNow() {
        String merchantId = "mid-123456789";
        long now = System.currentTimeMillis();
        long startMillis = now + 86400000L; // 1 day in future
        long endMillis = now + 172800000L; // 2 days in future
        long countFromRange = 15L;
        int dealsOfTheDay = 2;
        int couponCount = 5;
        int redeemedCouponCount = 1;
        // Future range path: use findActiveDealsByMerchantIdAndDateRange + coupon filtering
        List<DealSearchIndex> futureDeals = createDealSearchIndexList((int) countFromRange);
        when(dealSearchIndexRepository.findActiveDealsByMerchantIdAndDateRange(anyString(), anyLong(), anyLong()))
                .thenReturn(futureDeals);
        when(couponStatisticsService.getCouponStatisticsByDealIdList(anyList()))
                .thenReturn(new ArrayList<>());
        when(dealService.removeCouponNotAvailableDeals(any(Page.class), anyList()))
                .thenReturn(new PageImpl<>(futureDeals));
        when(dealsOfTheDayEventRepository.getDealsOfTheDayCountsInRange(anyString(), any(java.sql.Date.class), any(java.sql.Date.class)))
                .thenReturn(List.of(dealsOfTheDay));
        when(couponRepository.countActiveCouponsOverlappingRange(anyString(), anyLong(), anyLong()))
                .thenReturn((long) couponCount);
        when(couponRepository.countRedeemedCouponsByMerchantIdAndDateRange(anyString(), anyLong(), anyLong()))
                .thenReturn((long) redeemedCouponCount);

        MerchantSummaryResponseDto result = summaryService.getMerchantSummary(merchantId, startMillis, endMillis, TIME_ZONE);

        assertEquals((int) countFromRange, result.getTotalActiveDeals());
        assertEquals(dealsOfTheDay, result.getDealsOfTheDay());
        assertEquals(couponCount, result.getActiveCoupon());
        assertEquals(redeemedCouponCount, result.getRedeemedCoupon());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingMerchantSummaryForGetTotalActiveDealsCountIsFailed() {
        String merchantId = "mid-123456789";
        long startMillis = System.currentTimeMillis() - 86400000L;
        long endMillis = System.currentTimeMillis();

        when(dealSearchIndexRepository.getActiveMerchantDeals(any(Pageable.class), anyString(), anyLong()))
                .thenThrow(new DataAccessException(FAILED) {
                });

        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> summaryService
                .getMerchantSummary(merchantId, startMillis, endMillis, TIME_ZONE));
        assertEquals("Fetching active deal count failed.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingMerchantSummaryForGetDealsOfTheDayCountIsFailed() {
        String merchantId = "mid-123456789";
        long startMillis = System.currentTimeMillis() - 86400000L;
        long endMillis = System.currentTimeMillis();

        // Active deals succeed
        Page<DealSearchIndex> activeDealsPage = createDealSearchIndexPage(ANY_INT);
        when(dealSearchIndexRepository.getActiveMerchantDeals(any(Pageable.class), anyString(), anyLong()))
                .thenReturn(activeDealsPage);
        when(couponStatisticsService.getCouponStatisticsByDealIdList(anyList()))
                .thenReturn(new ArrayList<>());
        when(dealService.removeCouponNotAvailableDeals(any(Page.class), anyList()))
                .thenReturn(activeDealsPage);

        when(dealsOfTheDayEventRepository.getDealsOfTheDayCountsInRange(anyString(), any(java.sql.Date.class), any(java.sql.Date.class)))
                .thenThrow(new DataAccessException(FAILED) {
                });

        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> summaryService
                .getMerchantSummary(merchantId, startMillis, endMillis, TIME_ZONE));
        assertEquals("Fetching active deal count failed.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingMerchantSummaryForGetCouponCountIsFailed() {
        String merchantId = "mid-123456789";
        long startMillis = System.currentTimeMillis() - 86400000L;
        long endMillis = System.currentTimeMillis();

        // Active deals succeed
        Page<DealSearchIndex> activeDealsPage = createDealSearchIndexPage(ANY_INT);
        when(dealSearchIndexRepository.getActiveMerchantDeals(any(Pageable.class), anyString(), anyLong()))
                .thenReturn(activeDealsPage);
        when(couponStatisticsService.getCouponStatisticsByDealIdList(anyList()))
                .thenReturn(new ArrayList<>());
        when(dealService.removeCouponNotAvailableDeals(any(Page.class), anyList()))
                .thenReturn(activeDealsPage);

        when(dealsOfTheDayEventRepository.getDealsOfTheDayCountsInRange(anyString(), any(java.sql.Date.class), any(java.sql.Date.class)))
                .thenReturn(List.of(0));
        when(couponRepository.countActiveCouponsOverlappingRange(anyString(), anyLong(), anyLong()))
                .thenThrow(new DataAccessException(FAILED) {
                });

        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> summaryService
                .getMerchantSummary(merchantId, startMillis, endMillis, TIME_ZONE));
        assertEquals("Fetching coupon count failed.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingMerchantSummaryForGetRedeemedCouponCountIsFailed() {
        String merchantId = "mid-123456789";
        long startMillis = System.currentTimeMillis() - 86400000L;
        long endMillis = System.currentTimeMillis();

        // Active deals succeed
        Page<DealSearchIndex> activeDealsPage = createDealSearchIndexPage(ANY_INT);
        when(dealSearchIndexRepository.getActiveMerchantDeals(any(Pageable.class), anyString(), anyLong()))
                .thenReturn(activeDealsPage);
        when(couponStatisticsService.getCouponStatisticsByDealIdList(anyList()))
                .thenReturn(new ArrayList<>());
        when(dealService.removeCouponNotAvailableDeals(any(Page.class), anyList()))
                .thenReturn(activeDealsPage);

        when(dealsOfTheDayEventRepository.getDealsOfTheDayCountsInRange(anyString(), any(java.sql.Date.class), any(java.sql.Date.class)))
                .thenReturn(List.of(0));
        when(couponRepository.countActiveCouponsOverlappingRange(anyString(), anyLong(), anyLong()))
                .thenReturn(0L);
        when(couponRepository.countRedeemedCouponsByMerchantIdAndDateRange(anyString(), anyLong(), anyLong()))
                .thenThrow(new DataAccessException(FAILED) {
                });

        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> summaryService
                .getMerchantSummary(merchantId, startMillis, endMillis, TIME_ZONE));
        assertEquals("Fetching redeemed coupon count failed.", exception.getMessage());
    }

    @Test
    void Should_SumMultipleDealsOfTheDayCounts_When_GettingMerchantSummary() {
        String merchantId = "mid-123456789";
        long startMillis = System.currentTimeMillis() - 86400000L;
        long endMillis = System.currentTimeMillis();
        int activeDeals = 10;
        List<Integer> dealsOfTheDayCounts = List.of(2, 3, 5); // Should sum to 10
        int couponCount = 8;
        int redeemedCouponCount = 3;
        // Active deals succeed and match expected activeDeals count
        Page<DealSearchIndex> activeDealsPage = createDealSearchIndexPage(activeDeals);
        when(dealSearchIndexRepository.getActiveMerchantDeals(any(Pageable.class), anyString(), anyLong()))
                .thenReturn(activeDealsPage);
        when(couponStatisticsService.getCouponStatisticsByDealIdList(anyList()))
                .thenReturn(new ArrayList<>());
        when(dealService.removeCouponNotAvailableDeals(any(Page.class), anyList()))
                .thenReturn(activeDealsPage);
        when(dealsOfTheDayEventRepository.getDealsOfTheDayCountsInRange(anyString(), any(java.sql.Date.class), any(java.sql.Date.class)))
                .thenReturn(dealsOfTheDayCounts);
        when(couponRepository.countActiveCouponsOverlappingRange(anyString(), anyLong(), anyLong()))
                .thenReturn((long) couponCount);
        when(couponRepository.countRedeemedCouponsByMerchantIdAndDateRange(anyString(), anyLong(), anyLong()))
                .thenReturn((long) redeemedCouponCount);

        MerchantSummaryResponseDto result = summaryService.getMerchantSummary(merchantId, startMillis, endMillis, TIME_ZONE);

        assertEquals(activeDeals, result.getTotalActiveDeals());
        assertEquals(10, result.getDealsOfTheDay()); // Sum of 2 + 3 + 5
        assertEquals(couponCount, result.getActiveCoupon());
        assertEquals(redeemedCouponCount, result.getRedeemedCoupon());
    }

    private List<DealSearchIndex> createDealSearchIndexList(int count) {
        List<DealSearchIndex> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            DealSearchIndex dsi = new DealSearchIndex();
            dsi.setId("deal-" + i);
            list.add(dsi);
        }
        return list;
    }

    private Page<DealSearchIndex> createDealSearchIndexPage(int count) {
        return new PageImpl<>(createDealSearchIndexList(count));
    }
}