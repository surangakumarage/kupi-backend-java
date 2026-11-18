//package com.kupi.cc.base.service;
//
//import com.kupi.cc.base.domain.entity.Coupon;
//import com.kupi.cc.base.domain.entity.CouponStatistics;
//import com.kupi.cc.base.domain.entity.Deal;
//import com.kupi.cc.base.domain.entity.DealSearchIndex;
//import com.kupi.cc.base.domain.request.BulkUserRequestDto;
//import com.kupi.cc.base.domain.request.CouponFileUploadRequestDto;
//import com.kupi.cc.base.domain.request.RedeemRequestDto;
//import com.kupi.cc.base.domain.response.BasicMerchantBusinessResponseDto;
//import com.kupi.cc.base.domain.response.BulkUserResponseDto;
//import com.kupi.cc.base.enums.ActiveStatusType;
//import com.kupi.cc.base.enums.CouponFilter;
//import com.kupi.cc.base.enums.CouponPurchaseStatus;
//import com.kupi.cc.base.enums.UserType;
//import com.kupi.cc.base.exception.CouponException;
//import com.kupi.cc.base.exception.CouponUploadFailedException;
//import com.kupi.cc.base.exception.InvalidDealException;
//import com.kupi.cc.base.exception.KupiCoreException;
//import com.kupi.cc.base.repository.CouponRepository;
//import com.kupi.cc.base.repository.CouponStatisticsRepository;
//import com.kupi.cc.base.repository.DealRepository;
//import com.kupi.cc.base.repository.DealSearchIndexRepository;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobExecution;
//import org.springframework.batch.core.JobParameters;
//import org.springframework.batch.core.JobParametersInvalidException;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
//import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
//import org.springframework.batch.core.repository.JobRestartException;
//import org.springframework.dao.DataAccessException;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.mockito.MockitoAnnotations.initMocks;
//
///**
// * This class tests {@link CouponService} class
// */
//class CouponServiceTest {
//
//    private static final String DEAL_ID = "did-123456789";
//    private static final String USER_ID = "uid-123456789";
//    private static final String COUPON_ID = "cid-123456789";
//    private static final String MERCHANT_ID = "mid-123456789";
//    private static final String COUPON_CODE = "COUPON_CODE";
//    private static final String SEARCH_TERM = "SEARCH_TERM";
//    private static final String TIME_ZONE = "Asia/Colombo";
//    private static final String COUPON_UPLOAD_FILE_NAME = "COUPON_UPLOAD_FILE_NAME";
//    private static final String COUPON_UPLOAD_FILE_URL = "COUPON_UPLOAD_FILE_URL";
//    private static final String ALL = "ALL";
//    private static final String ERROR = "ERROR";
//    private static final int PAGE = 0;
//    private static final int SIZE = 250;
//    private static final long DEAL_EXPIRY_TIME = 1_000_000_000;
//
//    @Mock
//    private DealSearchIndexRepository dealSearchIndexRepository;
//    @Mock
//    private CouponRepository couponRepository;
//    @Mock
//    private CouponStatisticsRepository couponStatisticsRepository;
//    @Mock
//    private FileDownloaderService fileDownloaderService;
//    @Mock
//    private JobLauncher jobLauncher;
//    @Mock
//    private Job job;
//    @Mock
//    private DealService dealService;
//    @Mock
//    private DealRepository dealRepository;
//    @Mock
//    private AuthUserService authUserService;
//    @Mock
//    private UserSubscriptionService userSubscriptionService;
//
//
//    private CouponService couponService;
//
//    @BeforeEach
//    void setUp() {
//        initMocks(this);
//        couponService = new CouponService(dealSearchIndexRepository, couponRepository, couponStatisticsRepository,
//                fileDownloaderService, jobLauncher, job, dealService, dealRepository, authUserService, userSubscriptionService);
//    }
//
//    @AfterEach
//    void tearDown() {
//    }
//
//    /**
//     * List coupons
//     */
//    @Test
//    void Should_ReturnCouponStatisticsPage_When_ListingCouponsIsSuccessful() {
//        when(couponStatisticsRepository.findAll(any(Pageable.class))).thenReturn(getSampleCouponStatisticsPage());
//        assertEquals(COUPON_ID, couponService.listCoupons(getSamplePageable()).getContent().get(0).getId());
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_ListingCouponsIsFailed() {
//        Pageable pageable = getSamplePageable();
//
//        when(couponStatisticsRepository.findAll(any(Pageable.class))).thenThrow(new DataAccessException(ERROR) {
//        });
//        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> couponService
//                .listCoupons(pageable));
//        assertEquals("Filed to list coupon statistics.", exception.getMessage());
//    }
//
//    /**
//     * List deals with coupons
//     */
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_ListingDealsWithCouponsIsForAllUserIdAllSearchTermAllFilter() {
//        when(dealSearchIndexRepository.getCouponDeals(any(Pageable.class))).thenReturn(getSampleDealSearchIndexPage());
//        assertEquals(DEAL_ID, couponService.listDealsWithCoupons(ALL, ALL, getSamplePageable(), CouponFilter.ALL)
//                .getContent().get(0).getId());
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_ListingDealsWithCouponsIsForUserIdAllSearchTermAllFilter() {
//        when(dealSearchIndexRepository.getCouponDealsByMerchantId(anyString(), any(Pageable.class)))
//                .thenReturn(getSampleDealSearchIndexPage());
//        assertEquals(DEAL_ID, couponService.listDealsWithCoupons(USER_ID, ALL, getSamplePageable(), CouponFilter.ALL)
//                .getContent().get(0).getId());
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_ListingDealsWithCouponsIsForAllUserIdSearchTermAllFilter() {
//        when(dealSearchIndexRepository.getCouponDealsWithSearchTerm(anyString(), any(Pageable.class)))
//                .thenReturn(getSampleDealSearchIndexPage());
//        assertEquals(DEAL_ID, couponService.listDealsWithCoupons(ALL, SEARCH_TERM, getSamplePageable(), CouponFilter.ALL)
//                .getContent().get(0).getId());
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_ListingDealsWithCouponsIsForUserIdSearchTermAllFilter() {
//        when(dealSearchIndexRepository.getCouponDealsWithMerchantIdAndSearchTerm(anyString(), anyString(),
//                any(Pageable.class))).thenReturn(getSampleDealSearchIndexPage());
//        assertEquals(DEAL_ID, couponService.listDealsWithCoupons(USER_ID, SEARCH_TERM, getSamplePageable(), CouponFilter.ALL)
//                .getContent().get(0).getId());
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_ListingDealsWithCouponsIsForAllUserIdAllSearchTermFilter() {
//        when(dealSearchIndexRepository.getCouponDealsByCouponType(anyBoolean(), any(Pageable.class)))
//                .thenReturn(getSampleDealSearchIndexPage());
//        assertEquals(DEAL_ID, couponService.listDealsWithCoupons(ALL, ALL, getSamplePageable(), CouponFilter.PAID)
//                .getContent().get(0).getId());
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_ListingDealsWithCouponsIsForUserIdAllSearchTermFilter() {
//        when(dealSearchIndexRepository.getCouponDealsByMerchantIdAndCouponType(anyString(), anyBoolean(),
//                any(Pageable.class))).thenReturn(getSampleDealSearchIndexPage());
//        assertEquals(DEAL_ID, couponService.listDealsWithCoupons(USER_ID, ALL, getSamplePageable(), CouponFilter.PAID)
//                .getContent().get(0).getId());
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_ListingDealsWithCouponsIsForAllUserIdSearchTermFilter() {
//        when(dealSearchIndexRepository.getCouponDealsWithSearchTermAndCouponType(anyString(), anyBoolean(),
//                any(Pageable.class))).thenReturn(getSampleDealSearchIndexPage());
//        assertEquals(DEAL_ID, couponService.listDealsWithCoupons(ALL, SEARCH_TERM, getSamplePageable(), CouponFilter.PAID)
//                .getContent().get(0).getId());
//    }
//
//    @Test
//    void Should_ReturnDealSearchIndexPage_When_ListingDealsWithCouponsIsForUserIdSearchTermFilter() {
//        when(dealSearchIndexRepository.getCouponDealsWithMerchantIdAndSearchTermAndCouponType(anyString(), anyString(),
//                anyBoolean(), any(Pageable.class))).thenReturn(getSampleDealSearchIndexPage());
//        assertEquals(DEAL_ID, couponService.listDealsWithCoupons(USER_ID, SEARCH_TERM, getSamplePageable(), CouponFilter.PAID)
//                .getContent().get(0).getId());
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_ListingDealsWithCouponsIsFailed() {
//        Pageable pageable = getSamplePageable();
//
//        when(dealSearchIndexRepository.getCouponDealsWithMerchantIdAndSearchTermAndCouponType(anyString(), anyString(),
//                anyBoolean(), any(Pageable.class))).thenThrow(new DataAccessException(ERROR) {
//        });
//        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> couponService
//                .listDealsWithCoupons(USER_ID, SEARCH_TERM, pageable, CouponFilter.PAID));
//        assertEquals("Failed to get coupon deal list from db.", exception.getMessage());
//    }
//
//    /**
//     * Get coupons by deal id
//     */
//    @Test
//    void Should_ReturnCouponPage_When_GettingCouponsByDealIdIsSuccessfulForAllSearchTerm() {
//        when(couponRepository.findByDealId(anyString(), any(Pageable.class))).thenReturn(getSampleCouponPage());
//        assertEquals(COUPON_ID, couponService.getCouponsByDealId(getSamplePageable(), DEAL_ID, ALL)
//                .getContent().get(0).getId());
//    }
//
//    @Test
//    void Should_ReturnCouponPage_When_GettingCouponsByDealIdIsSuccessfulForSearchTerm() {
//        when(couponRepository.findByDealIdAndCouponCodeContains(anyString(), anyString(), any(Pageable.class)))
//                .thenReturn(getSampleCouponPage());
//        assertEquals(COUPON_ID, couponService.getCouponsByDealId(getSamplePageable(), DEAL_ID, SEARCH_TERM)
//                .getContent().get(0).getId());
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_GettingCouponsByDealIdIsFailed() {
//        Pageable pageable = getSamplePageable();
//
//        when(couponRepository.findByDealIdAndCouponCodeContains(anyString(), anyString(), any(Pageable.class)))
//                .thenThrow(new DataAccessException(ERROR) {
//                });
//        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> couponService
//                .getCouponsByDealId(pageable, DEAL_ID, SEARCH_TERM));
//        assertEquals("Failed to get coupons from database for dealId: " + DEAL_ID, exception.getMessage());
//    }
//
//    /**
//     * Get new coupon code
//     */
//    @Test
//    void Should_ReturnCoupon_When_GettingNewCouponCodeIsSuccessful() {
//        when(couponStatisticsRepository.findByDealId(anyString())).thenReturn(Optional.of(getSampleCouponStatistics()));
//        when(couponRepository.getNewCoupon(anyString(), anyLong())).thenReturn(Optional.of(getSampleCoupon()));
//        when(dealSearchIndexRepository.findById(anyString())).thenReturn(Optional.of(getSampleDealSearchIndex()));
//
//        assertEquals(USER_ID, couponService.getNewCouponCode(DEAL_ID, USER_ID).getPurchasedBy());
//        verify(couponRepository, times(1)).save(any(Coupon.class));
//        verify(couponStatisticsRepository, times(1)).save(any(CouponStatistics.class));
//        verify(dealSearchIndexRepository, times(1)).save(any(DealSearchIndex.class));
//    }
//
//    @Test
//    void Should_ThrowCouponException_When_GettingNewCouponCodeForCouponsAreDisabledForDeal() {
//        CouponStatistics couponStatistics = getSampleCouponStatistics();
//        couponStatistics.getDeal().setCouponActive(false);
//        when(couponStatisticsRepository.findByDealId(anyString())).thenReturn(Optional.of(couponStatistics));
//        CouponException exception = assertThrows(CouponException.class, () -> couponService
//                .getNewCouponCode(DEAL_ID, USER_ID));
//        assertEquals("Coupons disabled for deal.", exception.getMessage());
//    }
//
//    @Test
//    void Should_ThrowInvalidDealException_When_GettingNewCouponCodeForDealNotFoundInSearchIndexTable() {
//        when(couponStatisticsRepository.findByDealId(anyString())).thenReturn(Optional.of(getSampleCouponStatistics()));
//        when(couponRepository.getNewCoupon(anyString(), anyLong())).thenReturn(Optional.of(getSampleCoupon()));
//        when(dealSearchIndexRepository.findById(anyString())).thenReturn(Optional.empty());
//        InvalidDealException exception = assertThrows(InvalidDealException.class, () -> couponService
//                .getNewCouponCode(DEAL_ID, USER_ID));
//        assertEquals("Deal not found in search index table. dealId: " + DEAL_ID, exception.getMessage());
//    }
//
//    @Test
//    void Should_ThrowCouponException_When_GettingNewCouponCodeForCouponNotFoundForDealId() {
//        when(couponStatisticsRepository.findByDealId(anyString())).thenReturn(Optional.empty());
//        CouponException exception = assertThrows(CouponException.class, () -> couponService
//                .getNewCouponCode(DEAL_ID, USER_ID));
//        assertEquals("Coupons not found for dealId: " + DEAL_ID, exception.getMessage());
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_GettingNewCouponCodeIsFailed() {
//        when(couponStatisticsRepository.findByDealId(anyString())).thenThrow(new DataAccessException(ERROR) {
//        });
//        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> couponService
//                .getNewCouponCode(DEAL_ID, USER_ID));
//        assertEquals("Failed to redeem coupon from database.", exception.getMessage());
//    }
//
//    /**
//     * Update coupon purchase status
//     */
//    @Test
//    void Should_UpdateCouponPurchaseStatus_When_UpdatingCouponPurchaseStatusIsSuccessful() {
//        when(couponRepository.findById(anyString())).thenReturn(Optional.of(getSampleCoupon()));
//        when(couponStatisticsRepository.findByDealId(anyString())).thenReturn(Optional.of(getSampleCouponStatistics()));
//        when(dealSearchIndexRepository.findById(anyString())).thenReturn(Optional.of(getSampleDealSearchIndex()));
//
//        couponService.updateCouponPurchaseStatus(COUPON_ID, DEAL_ID, CouponPurchaseStatus.NOT_PURCHASED);
//        verify(couponRepository, times(1)).save(any(Coupon.class));
//        verify(couponStatisticsRepository, times(1)).save(any(CouponStatistics.class));
//        verify(dealSearchIndexRepository, times(1)).save(any(DealSearchIndex.class));
//    }
//
//    @Test
//    void Should_ThrowInvalidDealException_When_UpdatingCouponPurchaseStatusForDealNotFoundInSearchIndexTable() {
//        InvalidDealException exception = assertThrows(InvalidDealException.class, () -> couponService
//                .updateCouponPurchaseStatus(COUPON_ID, DEAL_ID, CouponPurchaseStatus.NOT_PURCHASED));
//        assertEquals("Deal not found in search index table. dealId: " + DEAL_ID, exception.getMessage());
//    }
//
//    @Test
//    void Should_ThrowCouponException_When_UpdatingCouponPurchaseStatusForCouponNotFoundForCouponId() {
//        when(dealSearchIndexRepository.findById(anyString())).thenReturn(Optional.of(getSampleDealSearchIndex()));
//        CouponException exception = assertThrows(CouponException.class, () -> couponService
//                .updateCouponPurchaseStatus(COUPON_ID, DEAL_ID, CouponPurchaseStatus.NOT_PURCHASED));
//        assertEquals("Coupon not found for couponId: " + COUPON_ID, exception.getMessage());
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_UpdatingCouponPurchaseStatusIsFailed() {
//        when(couponRepository.findById(anyString())).thenThrow(new DataAccessException(ERROR) {
//        });
//        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> couponService
//                .updateCouponPurchaseStatus(COUPON_ID, DEAL_ID, CouponPurchaseStatus.NOT_PURCHASED));
//        assertEquals("Failed to update coupon purchase status for couponId: " + COUPON_ID, exception
//                .getMessage());
//    }
//
//    /**
//     * Redeem coupon
//     */
//    @Test
//    void Should_RedeemCoupon_When_RedeemingCouponIsSuccessful() {
//        RedeemRequestDto redeemRequestDto = getSampleRedeemRequestDto();
//        Coupon sampleCoupon = getSampleCoupon();
//        when(couponRepository.findByCouponCodeAndDeal_MerchantId(anyString(), anyString()))
//                .thenReturn(Optional.of(sampleCoupon));
//        BasicMerchantBusinessResponseDto mockUser = mock(BasicMerchantBusinessResponseDto.class);
//        BulkUserResponseDto mockResponse = new BulkUserResponseDto(Collections.singletonList(mockUser));
//        when(authUserService.getBulkUserResponse(anyString(), any(BulkUserRequestDto.class)))
//                .thenReturn(mockResponse);
//        couponService.redeemCoupon(redeemRequestDto, "Asia/Colombo");
//        verify(couponRepository, times(1)).save(any(Coupon.class));
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_RedeemingCouponIsFailed() {
//        RedeemRequestDto redeemRequestDto = getSampleRedeemRequestDto();
//
//        when(couponRepository.findByCouponCodeAndDeal_MerchantId(anyString(), anyString()))
//                .thenThrow(new DataAccessException(ERROR) {
//                });
//        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> couponService
//                .redeemCoupon(redeemRequestDto,"TIME_ZONE"));
//        assertEquals("Failed to redeem coupon for merchantId: " + MERCHANT_ID, exception.getMessage());
//    }
//
//    /**
//     * Upload coupons
//     */
//    @Test
//    void Should_UploadCoupons_When_UploadingCouponsIsSuccessful() throws JobInstanceAlreadyCompleteException,
//            JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
//        doNothing().when(fileDownloaderService).download(anyString(), anyString());
//        when(jobLauncher.run(any(), any(JobParameters.class))).thenReturn(new JobExecution(1L));
//
//        couponService.uploadCoupons(getSampleCouponFileUploadRequestDto(), TIME_ZONE, getSampleDeal());
//        verify(fileDownloaderService).download(anyString(), anyString());
//        verify(jobLauncher).run(any(), any(JobParameters.class));
//    }
//
//    @Test
//    void Should_ThrowCouponUploadFailedException_When_UploadingCouponsIsFailed() throws
//            JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException,
//            JobRestartException {
//        CouponFileUploadRequestDto couponFileUploadRequestDto = getSampleCouponFileUploadRequestDto();
//        Deal deal = getSampleDeal();
//
//        when(jobLauncher.run(any(), any(JobParameters.class))).thenThrow(new JobRestartException(ERROR));
//        CouponUploadFailedException exception = assertThrows(CouponUploadFailedException.class, () -> couponService
//                .uploadCoupons(couponFileUploadRequestDto, TIME_ZONE, deal));
//        assertEquals("Failed to upload coupons to the database.", exception.getMessage());
//    }
//
//    /**
//     * Update deal coupon status
//     */
//    @Test
//    void Should_UpdateDealCouponStatus_When_UpdatingDealCouponStatusIsSuccessful() {
//        couponService.updateDealCouponStatus(DEAL_ID, UserType.MERCHANT, ActiveStatusType.ACTIVE);
//        verify(dealService).updateDealCouponStatus(anyString(), any(UserType.class), any(ActiveStatusType.class));
//    }
//
//    /**
//     * Update coupon status
//     */
//    @Test
//    void Should_UpdateCouponStatus_When_UpdatingCouponStatusIsSuccessful() {
//        CouponStatistics couponStatistics = new CouponStatistics();
//        couponStatistics.setTotalCoupons(10);
//        couponStatistics.setRemainingCoupons(5);
//        Deal deal = new Deal();
//        deal.setId(DEAL_ID);
//        deal.setCouponStatistics(couponStatistics);
//        Coupon coupon = getSampleCoupon();
//        coupon.setDeal(deal);
//
//        when(couponRepository.findById(anyString())).thenReturn(Optional.of(coupon));
//        when(dealSearchIndexRepository.findById(anyString())).thenReturn(Optional.of(getSampleDealSearchIndex()));
//
//        couponService.updateCouponStatus(COUPON_ID, ActiveStatusType.INACTIVE);
//        verify(couponRepository).save(any(Coupon.class));
//        verify(dealSearchIndexRepository).save(any(DealSearchIndex.class));
//    }
//
//    @Test
//    void Should_ThrowCouponException_When_UpdatingCouponStatusForInvalidCouponId() {
//        CouponException exception = assertThrows(CouponException.class, () -> couponService
//                .updateCouponStatus(COUPON_ID, ActiveStatusType.ACTIVE));
//        assertEquals("Invalid coupon id: " + COUPON_ID, exception.getMessage());
//    }
//
//    @Test
//    void Should_ThrowCouponException_When_UpdatingCouponStatusForCouponAlreadyPurchased() {
//        Coupon coupon = getSampleCoupon();
//        coupon.setCouponPurchaseStatus(CouponPurchaseStatus.PURCHASED);
//
//        when(couponRepository.findById(anyString())).thenReturn(Optional.of(coupon));
//        CouponException exception = assertThrows(CouponException.class, () -> couponService
//                .updateCouponStatus(COUPON_ID, ActiveStatusType.INACTIVE));
//        assertEquals("Coupon already purchased.", exception.getMessage());
//    }
//
//    @Test
//    void Should_ThrowKupiCoreException_When_UpdatingCouponStatusIsFailed() {
//        when(couponRepository.findById(anyString())).thenThrow(new DataAccessException(ERROR) {
//        });
//        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> couponService
//                .updateCouponStatus(COUPON_ID, ActiveStatusType.ACTIVE));
//        assertEquals("Coupon status update is failed for coupon id: " + COUPON_ID, exception.getMessage());
//    }
//
//    /**
//     * This method returns a sample CouponFileUploadRequestDto
//     *
//     * @return CouponFileUploadRequestDto
//     */
//    private CouponFileUploadRequestDto getSampleCouponFileUploadRequestDto() {
//        CouponFileUploadRequestDto dto = new CouponFileUploadRequestDto();
//        dto.setFileName(COUPON_UPLOAD_FILE_NAME);
//        dto.setFileUrl(COUPON_UPLOAD_FILE_URL);
//        dto.setDealId(DEAL_ID);
//        return dto;
//    }
//
//    /**
//     * This method returns a sample RedeemRequestDto
//     *
//     * @return RedeemRequestDto
//     */
//    private RedeemRequestDto getSampleRedeemRequestDto() {
//        RedeemRequestDto redeemRequestDto = new RedeemRequestDto();
//        redeemRequestDto.setMerchantId(MERCHANT_ID);
//        redeemRequestDto.setCouponCode(COUPON_CODE);
//
//        return redeemRequestDto;
//    }
//
//    /**
//     * This method returns a sample Coupon page
//     *
//     * @return Coupon page
//     */
//    private Page<Coupon> getSampleCouponPage() {
//        return new PageImpl<>(getSampleCouponList());
//    }
//
//    /**
//     * This method returns a sample Coupon list
//     *
//     * @return Coupon list
//     */
//    private List<Coupon> getSampleCouponList() {
//        List<Coupon> list = new ArrayList<>();
//        list.add(getSampleCoupon());
//        return list;
//    }
//
//    /**
//     * This method returns a sample Coupon object
//     *
//     * @return Coupon
//     */
//    private Coupon getSampleCoupon() {
//        Coupon coupon = new Coupon();
//        coupon.setId(COUPON_ID);
//        coupon.setDeal(getSampleDeal());
//        coupon.setCouponActive(true);
//        coupon.setRedeemed(false);
//        coupon.setPurchasedBy("USER");
//        coupon.setCouponPurchaseStatus(CouponPurchaseStatus.NOT_PURCHASED);
//        return coupon;
//    }
//
//    /**
//     * This method returns a sample DealSearchIndex page
//     *
//     * @return DealSearchIndex page
//     */
//    private Page<DealSearchIndex> getSampleDealSearchIndexPage() {
//        return new PageImpl<>(getSampleDealSearchIndexList());
//    }
//
//    /**
//     * This method returns a sample DealSearchIndex list
//     *
//     * @return DealSearchIndex list
//     */
//    private List<DealSearchIndex> getSampleDealSearchIndexList() {
//        List<DealSearchIndex> list = new ArrayList<>();
//        list.add(getSampleDealSearchIndex());
//        return list;
//    }
//
//    /**
//     * This method returns a sample DealSearchIndex object
//     *
//     * @return DealSearchIndex
//     */
//    private DealSearchIndex getSampleDealSearchIndex() {
//        DealSearchIndex dealSearchIndex = new DealSearchIndex();
//        dealSearchIndex.setId(DEAL_ID);
//        return dealSearchIndex;
//    }
//
//    /**
//     * This method returns a sample CouponStatistics page
//     *
//     * @return CouponStatistics page
//     */
//    private Page<CouponStatistics> getSampleCouponStatisticsPage() {
//        return new PageImpl<>(getSampleCouponStatisticsList());
//    }
//
//    /**
//     * This method returns a sample CouponStatistics list
//     *
//     * @return CouponStatistics list
//     */
//    private List<CouponStatistics> getSampleCouponStatisticsList() {
//        List<CouponStatistics> list = new ArrayList<>();
//        list.add(getSampleCouponStatistics());
//        return list;
//    }
//
//    /**
//     * This method returns a sample CouponStatistics object
//     *
//     * @return CouponStatistics
//     */
//    private CouponStatistics getSampleCouponStatistics() {
//        CouponStatistics couponStatistics = new CouponStatistics();
//        couponStatistics.setId(COUPON_ID);
//        couponStatistics.setRemainingCoupons(10);
//        couponStatistics.setDeal(getSampleDeal());
//        return couponStatistics;
//    }
//
//    /**
//     * This method returns a sample Deal object
//     *
//     * @return Deal
//     */
//    private Deal getSampleDeal() {
//        Deal deal = new Deal();
//        deal.setId(DEAL_ID);
//        deal.setCouponActive(true);
//        deal.setMerchantId(MERCHANT_ID);
//        deal.setExpiredOn(System.currentTimeMillis() + DEAL_EXPIRY_TIME);
//        return deal;
//    }
//
//    /**
//     * This method returns a sample Pageable object
//     *
//     * @return Pageable
//     */
//    private Pageable getSamplePageable() {
//        return PageRequest.of(PAGE, SIZE);
//    }
//}