//package com.kupi.cc.base.service;
//
//import com.kupi.cc.base.domain.entity.AuthUser;
//import com.kupi.cc.base.domain.entity.Brand;
//import com.kupi.cc.base.domain.entity.Category;
//import com.kupi.cc.base.domain.entity.Deal;
//import com.kupi.cc.base.domain.request.DealApprovalStatusUpdateRequestDto;
//import com.kupi.cc.base.domain.request.DealRequestDto;
//import com.kupi.cc.base.domain.request.MobileNoRequestDto;
//import com.kupi.cc.base.domain.response.MobileNoResponseDto;
//import com.kupi.cc.base.enums.ApprovalStatus;
//import com.kupi.cc.base.enums.CategoryType;
//import com.kupi.cc.base.enums.DeductionType;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import static org.mockito.Mockito.*;
//import static org.mockito.MockitoAnnotations.initMocks;
//
///**
// * This class tests the {@link DealApprovalService} class.
// */
//class DealApprovalServiceTest {
//
//    private static final String ACCESS_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
//    private static final String DEAL_ID = "did-02a2022f-98c5-4237-a9c7-c1faf989a4a5";
//    private static final String MERCHANT_ID = "uid-0022";
//    private static final String EMAIL_ID = "merchant@kupi.lk";
//    private DealApprovalService dealApprovalService;
//    @Mock
//    private AuthUserService authUserService;
//    @Mock
//    private NotificationService notificationService;
//
//    @BeforeEach
//    void setUp() {
//        initMocks(this);
//        dealApprovalService = new DealApprovalService(authUserService, notificationService);
//    }
//
//    @AfterEach
//    void tearDown() {
//    }
//
//    @Test
//    void Should_SendMailAndSmsTOMerchant_When_BothEmailAndMobileNoAreProvidedForSendMailAndSmsToMerchant() {
//        AuthUser authUser = getSampleAuthUser();
//        Deal deal = getSampleDeal();
//        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
//        when(authUserService.getUserByUserId(MERCHANT_ID, ACCESS_TOKEN)).thenReturn(authUser);
//        dealApprovalService.sendMailAndSmsToMerchant(deal, dealApprovalStatusUpdateRequestDto, ACCESS_TOKEN);
//        verify(notificationService, times(1)).sendMail(EMAIL_ID, ApprovalStatus.APPROVED,
//                authUser.getFullName());
//        verify(notificationService, times(1))
//                .sendSms(any(MobileNoRequestDto.class), any(ApprovalStatus.class));
//    }
//
//    @Test
//    void Should_SendMailAndSmsTOMerchant_When_EmailOnlyProvidedForSendMailAndSmsToMerchant() {
//        AuthUser authUser = getSampleAuthUser();
//        authUser.setMobileNo(null);
//        Deal deal = getSampleDeal();
//        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
//        when(authUserService.getUserByUserId(MERCHANT_ID, ACCESS_TOKEN)).thenReturn(authUser);
//        dealApprovalService.sendMailAndSmsToMerchant(deal, dealApprovalStatusUpdateRequestDto, ACCESS_TOKEN);
//        verify(notificationService, times(1)).sendMail(EMAIL_ID, ApprovalStatus.APPROVED,
//                authUser.getFullName());
//    }
//
//    @Test
//    void Should_SendMailAndSmsTOMerchant_When_MobileNoOnlyProvidedForSendMailAndSmsToMerchant() {
//        AuthUser authUser = getSampleAuthUser();
//        authUser.setEmail(null);
//        Deal deal = getSampleDeal();
//        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
//        when(authUserService.getUserByUserId(MERCHANT_ID, ACCESS_TOKEN)).thenReturn(authUser);
//        dealApprovalService.sendMailAndSmsToMerchant(deal, dealApprovalStatusUpdateRequestDto, ACCESS_TOKEN);
//        verify(notificationService, times(1))
//                .sendSms(any(MobileNoRequestDto.class), any(ApprovalStatus.class));
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
//
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
//     * This method creates sample AuthUser
//     *
//     * @return AuthUser
//     */
//    private AuthUser getSampleAuthUser() {
//        AuthUser authUser = new AuthUser();
//        authUser.setActive(true);
//        authUser.setFullName("full name");
//        authUser.setRegisteredUser(true);
//        authUser.setEmail(EMAIL_ID);
//        authUser.setApprovalStatus(ApprovalStatus.APPROVED);
//        MobileNoResponseDto mobileNoResponseDto = new MobileNoResponseDto();
//        mobileNoResponseDto.setCountryCode("+94");
//        mobileNoResponseDto.setLocalNumber("771109101");
//        mobileNoResponseDto.setDisplayNumber("+94771109101");
//        authUser.setMobileNo(mobileNoResponseDto);
//        return authUser;
//    }
//}