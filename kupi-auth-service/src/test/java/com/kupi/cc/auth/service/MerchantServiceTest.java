package com.kupi.cc.auth.service;

import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.domain.BusinessProfile;
import com.kupi.cc.auth.domain.entity.*;
import com.kupi.cc.auth.domain.request.*;
import com.kupi.cc.auth.domain.response.PendingContactResponseDto;
import com.kupi.cc.auth.enums.ApprovalAction;
import com.kupi.cc.auth.enums.ApprovalStatus;
import com.kupi.cc.auth.enums.MerchantInfo;
import com.kupi.cc.auth.enums.RoleType;
import com.kupi.cc.auth.exception.*;
import com.kupi.cc.auth.repository.*;
import com.kupi.cc.auth.service.factories.MerchantProfileRepositoryFactory;
import com.kupi.cc.auth.service.factories.MerchantStatusRepositoryFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests the {@link MerchantService } class.
 */
class MerchantServiceTest {

    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final int ROLE_ID = 3;
    private static final int ACTIVE_MERCHANT_COUNT = 5;
    private static final String MERCHANT_ID = "uid-bd0c8894-8ddb-4b1d-8383-de44bfaa297f";
    private static final String SEARCH_TERM = "SEARCH TERM";
    private static final String UPDATED_AT = "updatedAt";
    private static final String ALL = "ALL";
    private static final String BANK_ID = "bid-bd0c8894-8ddb-4b1d-8383-de44bfaa297f";
    private static final String COMMENT = "COMMENT";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String SMS_MESSAGE = "message";
    private static final String ROLE_TYPE_MERCHANT = RoleType.MERCHANT.toString();
    private static final String APPROVE_MERCHANT_SUBJECT = "kupi <ROLE-TYPE> Account Approval";
    private static final String UNBLOCK_MERCHANT_SUBJECT = "kupi <ROLE-TYPE> Profile Unblock Notification";
    private static final String BLOCK_MERCHANT_SUBJECT = "kupi <ROLE-TYPE> Profile Block Notification";
    private static final String REJECT_MERCHANT_SUBJECT = "kupi <ROLE-TYPE> Account Rejection";
    private static final String APPROVE_MERCHANT_BODY = "<MERCHANT-NAME>,\\nThis is to inform that your account has been approved and eligible to post deals in kupi.\\nThis is a system generated email. Please do not reply.\\n \\nThank you,\\n \\nkupi Team";
    private static final String UNBLOCK_MERCHANT_BODY = "Hi <MERCHANT-NAME>,\\n\\nThis is to inform that your profile has been reactivated.\\nThis is a system generated email. Please do not reply.\\n\\nThank you,\\n\\nkupi Team";
    private static final String BLOCK_MERCHANT_BODY = "Hi <MERCHANT-NAME>,\\n\\nThis is to inform that your profile is suspended temporally. Please contact our support team to get more details and reactivate your profile.\\nThis is a system generated email. Please do not reply.\\n\\nThank you,\\n\\nkupi Team";
    private static final String REJECT_MERCHANT_BODY = "Hi <MERCHANT-NAME>,\\nThis is to inform that your account has been rejected. Kindly signup again with valid information or \\ncontact out support team for more details.\\nThis is a system generated email. Please do not reply.\\n\\nThank you,\\n\\nkupi Team\n";

    private MerchantService merchantService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private MerchantBankSearchIndexRepository merchantBankSearchIndexRepository;
    @Mock
    private ApprovedBankBusinessRepository approvedBankBusinessRepository;
    @Mock
    private RejectedProfileUpdatesRepository rejectedProfileUpdatesRepository;
    @Mock
    private ApprovedBusinessRepository approvedBusinessRepository;
    @Mock
    private BlockedMerchantRepository blockedMerchantRepository;
    @Mock
    private ContactRepository contactRepository;
    @Mock
    private BankBusinessRepository bankBusinessRepository;
    @Mock
    private Translator translator;

    @BeforeEach
    void setUp() {
        initMocks(this);
        MerchantStatusRepositoryFactory merchantStatusRepositoryFactory = new MerchantStatusRepositoryFactory(
                rejectedProfileUpdatesRepository, approvedBusinessRepository, blockedMerchantRepository);
        MerchantProfileRepositoryFactory merchantProfileRepositoryFactory = new MerchantProfileRepositoryFactory(
                contactRepository, userRepository, businessRepository, bankBusinessRepository);
        merchantService = new MerchantService(notificationService, merchantBankSearchIndexRepository,
                merchantStatusRepositoryFactory, merchantProfileRepositoryFactory,
                approvedBankBusinessRepository, translator);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_CreateOrUpdateBusiness_WhenBusinessRequestDtoIsGivenWithAllFieldsForExistingBusiness() {
        BusinessRequestDto businessRequestDto = new BusinessRequestDto();
        businessRequestDto.setMerchantId(MERCHANT_ID);
        businessRequestDto.setBusinessName("Singer");
        businessRequestDto.setOwnerName("Owner");
        businessRequestDto.setMobileNo(new MobileNoRequestDto("+94", "713321911"));
        businessRequestDto.setTelephone(new MobileNoRequestDto("+94", "72993391"));
        businessRequestDto.setImageUrl("https://www.singersl.com/image.jpg");
        businessRequestDto.setEmail("singerlk@gmail.com");
        businessRequestDto.setBusinessRegNo("SL123");
        businessRequestDto.setAddress("N0.20, Station road, Dehiwala.");
        businessRequestDto.setWebSite("https://www.singersl.com/");
        businessRequestDto.setFacebook("https://www.facebook.com/SingerSL/");
        businessRequestDto.setInstagram("https://www.instagram.com/singer_srilanka/?hl=en");
        businessRequestDto.setOpeningHours(new OpeningHoursRequestDto());

        User merchant = new User();
        merchant.setId(MERCHANT_ID);
        merchant.setRole(new Role(RoleType.MERCHANT));

        Business existingBusiness = new Business();
        existingBusiness.setId("bisid-123");
        existingBusiness.setMerchant(merchant);

        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        when(businessRepository.findByApprovalStatusAndMerchantId(ApprovalStatus.PENDING, MERCHANT_ID))
                .thenReturn(Optional.of(existingBusiness));

        Business business = merchantService.createOrUpdateBusiness(businessRequestDto, RoleType.MERCHANT);
        verify(businessRepository).save(business);
        assertEquals(MERCHANT_ID, business.getMerchant().getId());
        assertEquals("Singer", business.getBusinessName());
        assertEquals("Owner", business.getOwnerName());
        assertEquals("+94-72993391", business.getTelephone());
        assertEquals("https://www.singersl.com/image.jpg", business.getImageUrl());
        assertEquals("singerlk@gmail.com", business.getEmail());
        assertEquals("SL123", business.getBusinessRegNo());
        assertEquals("N0.20, Station road, Dehiwala.", business.getAddress());
        assertEquals("https://www.singersl.com/", business.getWebSite());
        assertEquals("https://www.facebook.com/SingerSL/", business.getFacebook());
        assertEquals("https://www.instagram.com/singer_srilanka/?hl=en", business.getInstagram());
    }

    @Test
    void Should_CreateOrUpdateBusiness_WhenBusinessRequestDtoIsGivenWithAllFieldsForExistingBankBusiness() {
        BusinessRequestDto businessRequestDto = new BusinessRequestDto();
        businessRequestDto.setMerchantId(BANK_ID);
        businessRequestDto.setBusinessName("Singer");
        businessRequestDto.setOwnerName("Owner");
        businessRequestDto.setMobileNo(new MobileNoRequestDto("+94", "713321911"));
        businessRequestDto.setTelephone(new MobileNoRequestDto("+94", "72993391"));
        businessRequestDto.setImageUrl("https://www.singersl.com/image.jpg");
        businessRequestDto.setEmail("singerlk@gmail.com");
        businessRequestDto.setBusinessRegNo("SL123");
        businessRequestDto.setAddress("N0.20, Station road, Dehiwala.");
        businessRequestDto.setWebSite("https://www.singersl.com/");
        businessRequestDto.setFacebook("https://www.facebook.com/SingerSL/");
        businessRequestDto.setInstagram("https://www.instagram.com/singer_srilanka/?hl=en");
        businessRequestDto.setOpeningHours(new OpeningHoursRequestDto());


        User bank = new User();
        bank.setId(BANK_ID);
        bank.setRole(new Role(RoleType.BANK));

        Business existingBusiness = new Business();
        existingBusiness.setId("bisid-123");
        existingBusiness.setMerchant(bank);
        BankBusiness bankBusiness = new BankBusiness(existingBusiness);
        when(userRepository.findById(BANK_ID)).thenReturn(Optional.of(bank));
        when(bankBusinessRepository.findByApprovalStatusAndBankId(ApprovalStatus.PENDING, BANK_ID))
                .thenReturn(Optional.of(bankBusiness));

        Business business = merchantService.createOrUpdateBusiness(businessRequestDto, RoleType.BANK);
        verify(bankBusinessRepository).save(any(BankBusiness.class));
        assertEquals(BANK_ID, business.getMerchant().getId());
        assertEquals("Singer", business.getBusinessName());
        assertEquals("Owner", business.getOwnerName());
        assertEquals("+94-72993391", business.getTelephone());
        assertEquals("https://www.singersl.com/image.jpg", business.getImageUrl());
        assertEquals("singerlk@gmail.com", business.getEmail());
        assertEquals("SL123", business.getBusinessRegNo());
        assertEquals("N0.20, Station road, Dehiwala.", business.getAddress());
        assertEquals("https://www.singersl.com/", business.getWebSite());
        assertEquals("https://www.facebook.com/SingerSL/", business.getFacebook());
        assertEquals("https://www.instagram.com/singer_srilanka/?hl=en", business.getInstagram());
    }

    @Test
    void Should_CreateOrUpdateBusiness_WhenBusinessRequestDtoIsGivenWithoutOptionalFieldsForExistingBusiness() {
        BusinessRequestDto businessRequestDto = new BusinessRequestDto();
        businessRequestDto.setMerchantId(MERCHANT_ID);
        businessRequestDto.setBusinessName("Singer");
        businessRequestDto.setOwnerName("Owner");
        businessRequestDto.setMobileNo(new MobileNoRequestDto("+94", "713321911"));
        businessRequestDto.setTelephone(new MobileNoRequestDto("+94", "72993391"));
        businessRequestDto.setImageUrl("https://www.singersl.com/image.jpg");
        businessRequestDto.setOpeningHours(new OpeningHoursRequestDto());


        User merchant = new User();
        merchant.setId(MERCHANT_ID);
        merchant.setRole(new Role(RoleType.MERCHANT));

        Business existingBusiness = new Business();
        existingBusiness.setId("bisid-123");
        existingBusiness.setMerchant(merchant);

        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        when(businessRepository.findByApprovalStatusAndMerchantId(ApprovalStatus.PENDING, MERCHANT_ID))
                .thenReturn(Optional.of(existingBusiness));

        Business business = merchantService.createOrUpdateBusiness(businessRequestDto, RoleType.MERCHANT);
        verify(businessRepository).save(business);
        assertEquals(MERCHANT_ID, business.getMerchant().getId());
        assertEquals("Singer", business.getBusinessName());
        assertEquals("Owner", business.getOwnerName());
        assertEquals("+94-72993391", business.getTelephone());
        assertEquals("https://www.singersl.com/image.jpg", business.getImageUrl());
    }

    @Test
    void Should_CreateOrUpdateBusiness_WhenBusinessRequestDtoIsGivenWithAllFieldsForNewBusiness() {
        BusinessRequestDto businessRequestDto = new BusinessRequestDto();
        businessRequestDto.setMerchantId(MERCHANT_ID);
        businessRequestDto.setBusinessName("Singer");
        businessRequestDto.setOwnerName("Owner");
        businessRequestDto.setMobileNo(new MobileNoRequestDto("+94", "713321911"));
        businessRequestDto.setTelephone(new MobileNoRequestDto("+94", "72993391"));
        businessRequestDto.setImageUrl("https://www.singersl.com/image.jpg");
        businessRequestDto.setEmail("singerlk@gmail.com");
        businessRequestDto.setBusinessRegNo("SL123");
        businessRequestDto.setAddress("N0.20, Station road, Dehiwala.");
        businessRequestDto.setWebSite("https://www.singersl.com/");
        businessRequestDto.setFacebook("https://www.facebook.com/SingerSL/");
        businessRequestDto.setInstagram("https://www.instagram.com/singer_srilanka/?hl=en");
        businessRequestDto.setOpeningHours(new OpeningHoursRequestDto());

        User merchant = new User();
        merchant.setId(MERCHANT_ID);
        merchant.setRole(new Role(RoleType.MERCHANT));

        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        when(businessRepository.findByApprovalStatusAndMerchantId(ApprovalStatus.PENDING, MERCHANT_ID))
                .thenReturn(Optional.empty());

        Business business = merchantService.createOrUpdateBusiness(businessRequestDto, RoleType.MERCHANT);
        verify(businessRepository).save(business);
        assertEquals(MERCHANT_ID, business.getMerchant().getId());
        assertEquals("Singer", business.getBusinessName());
        assertEquals("Owner", business.getOwnerName());
        assertEquals("+94-72993391", business.getTelephone());
        assertEquals("https://www.singersl.com/image.jpg", business.getImageUrl());
        assertEquals("singerlk@gmail.com", business.getEmail());
        assertEquals("SL123", business.getBusinessRegNo());
        assertEquals("N0.20, Station road, Dehiwala.", business.getAddress());
        assertEquals("https://www.singersl.com/", business.getWebSite());
        assertEquals("https://www.facebook.com/SingerSL/", business.getFacebook());
        assertEquals("https://www.instagram.com/singer_srilanka/?hl=en", business.getInstagram());
    }

    @Test
    void Should_CreateOrUpdateBusiness_WhenBusinessRequestDtoIsGivenWithoutOptionalFieldsForNewBusiness() {
        BusinessRequestDto businessRequestDto = new BusinessRequestDto();
        businessRequestDto.setMerchantId(MERCHANT_ID);
        businessRequestDto.setBusinessName("Singer");
        businessRequestDto.setOwnerName("Owner");
        businessRequestDto.setMobileNo(new MobileNoRequestDto("+94", "713321911"));
        businessRequestDto.setTelephone(new MobileNoRequestDto("+94", "72993391"));
        businessRequestDto.setImageUrl("https://www.singersl.com/image.jpg");
        businessRequestDto.setOpeningHours(new OpeningHoursRequestDto());

        User merchant = new User();
        merchant.setId(MERCHANT_ID);
        merchant.setRole(new Role(RoleType.MERCHANT));

        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        when(businessRepository.findByApprovalStatusAndMerchantId(ApprovalStatus.PENDING, MERCHANT_ID))
                .thenReturn(Optional.empty());

        Business business = merchantService.createOrUpdateBusiness(businessRequestDto, RoleType.MERCHANT);
        verify(businessRepository).save(business);
        assertEquals(MERCHANT_ID, business.getMerchant().getId());
        assertEquals("Singer", business.getBusinessName());
        assertEquals("Owner", business.getOwnerName());
        assertEquals("+94-72993391", business.getTelephone());
        assertEquals("https://www.singersl.com/image.jpg", business.getImageUrl());
    }

    @Test
    void Should_ThrowInvalidUserException_When_CreatingOrUpdatingBusinessForInvalidUserId() {
        BusinessRequestDto businessRequestDto = new BusinessRequestDto();
        businessRequestDto.setMerchantId(MERCHANT_ID);
        when(userRepository.findById(businessRequestDto.getMerchantId())).thenReturn(Optional.empty());
        InvalidUserException exception = assertThrows(InvalidUserException.class, () ->
            merchantService.createOrUpdateBusiness(businessRequestDto, RoleType.MERCHANT));
        assertEquals("Invalid userId: " + MERCHANT_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidUserException_When_CreatingOrUpdatingBusinessForInvalidUserRole() {
        BusinessRequestDto businessRequestDto = new BusinessRequestDto();
        businessRequestDto.setMerchantId(MERCHANT_ID);
        User merchant = new User();
        merchant.setRole(new Role(RoleType.BANK));
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        InvalidUserException exception = assertThrows(InvalidUserException.class, () ->
                merchantService.createOrUpdateBusiness(businessRequestDto, RoleType.MERCHANT));
        assertEquals("Invalid user role for userId: " + MERCHANT_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_CreatingOrUpdatingBusinessFailedWhileGettingPendingBusinessProfiles() {
        BusinessRequestDto businessRequestDto = new BusinessRequestDto();
        User merchant = new User();
        merchant.setId(MERCHANT_ID);
        merchant.setRole(new Role(RoleType.MERCHANT));
        when(userRepository.findById(businessRequestDto.getMerchantId())).thenReturn(Optional.of(merchant));
        when(businessRepository
                .findByApprovalStatusAndMerchantId(ApprovalStatus.PENDING, merchant.getId()))
                .thenThrow(new DataAccessException("Failed") {
                });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
            merchantService.createOrUpdateBusiness(businessRequestDto, RoleType.MERCHANT));
        assertEquals("Failed to get pending business profile from database for userId: " +
                MERCHANT_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_CreatingOrUpdatingBusinessFailedToSaveOnDatabase() {
        BusinessRequestDto businessRequestDto = new BusinessRequestDto();
        businessRequestDto.setMerchantId(MERCHANT_ID);
        businessRequestDto.setBusinessName("Singer");
        businessRequestDto.setOwnerName("Owner");
        businessRequestDto.setMobileNo(new MobileNoRequestDto("+94", "713321911"));
        businessRequestDto.setTelephone(new MobileNoRequestDto("+94", "72993391"));
        businessRequestDto.setImageUrl("https://www.singersl.com/image.jpg");
        businessRequestDto.setOpeningHours(new OpeningHoursRequestDto());

        User merchant = new User();
        merchant.setRole(new Role(RoleType.MERCHANT));
        merchant.setId(MERCHANT_ID);
        Business existingBusiness = new Business();
        existingBusiness.setMerchant(merchant);

        when(userRepository.findById(businessRequestDto.getMerchantId())).thenReturn(Optional.of(merchant));
        when(businessRepository
                .findByApprovalStatusAndMerchantId(ApprovalStatus.PENDING, merchant.getId()))
                .thenReturn(Optional.of(existingBusiness));
        when(businessRepository.save(existingBusiness)).thenThrow(new DataAccessException("Failed") {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
            merchantService.createOrUpdateBusiness(businessRequestDto, RoleType.MERCHANT));
        assertEquals("Saving business data to database was failed for userId: " +
                MERCHANT_ID, exception.getMessage());
    }

    @Test
    void Should_ReturnPendingMerchantPage_When_SearchTermIsAllForGetPendingMerchant() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.Direction.DESC, UPDATED_AT);
        Page<User> merchantPage = getSampleMerchantPage();
        when(userRepository.findByRoleIdAndApprovalStatus(ROLE_ID, ApprovalStatus.PENDING, pageable))
                .thenReturn(merchantPage);
        assertEquals(merchantPage, merchantService.getPendingMerchant(PAGE, SIZE, ALL));
    }

    @Test
    void Should_ReturnPendingMerchantPage_When_SearchTermNotEqualToAllForGetPendingMerchant() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.Direction.DESC, UPDATED_AT);
        Page<User> merchantPage = getSampleMerchantPage();
        when(userRepository.findByApprovalStatusAndFullNameContaining(ApprovalStatus.PENDING, SEARCH_TERM, pageable))
                .thenReturn(merchantPage);
        assertEquals(merchantPage, merchantService.getPendingMerchant(PAGE, SIZE, SEARCH_TERM));
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetPendingMerchantIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.Direction.DESC, UPDATED_AT);
        when(userRepository.findByApprovalStatusAndFullNameContaining(ApprovalStatus.PENDING, SEARCH_TERM, pageable))
                .thenThrow(new DataAccessException("ERROR") {
                });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.getPendingMerchant(PAGE, SIZE, SEARCH_TERM));
        assertEquals("Failed to get pending merchants page from database", authServiceException.getMessage());
    }

    @Test
    void Should_ReturnBusiness_When_RoleTypeIsMerchantForGetBusinessInfoByBusinessId() {
        Business business = getSampleBusiness();
        when(businessRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(business));
        assertEquals(business, merchantService.getBusinessInfoByBusinessId(MERCHANT_ID, RoleType.MERCHANT));
    }

    @Test
    void Should_ReturnBusiness_When_RoleTypeIsBankForGetBusinessInfoByBusinessId() {
        Business business = getSampleBusiness();
        BankBusiness bankBusiness = new BankBusiness(business);
        when(bankBusinessRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(bankBusiness));
        assertEquals(business.getId(), merchantService.getBusinessInfoByBusinessId(MERCHANT_ID, RoleType.BANK).getId());
    }

    @Test
    void Should_ThrowBusinessProfileException_When_InvalidMerchantIdIsProvidedForGetBusinessInfoByBusinessId() {
        when(businessRepository.findById(MERCHANT_ID)).thenReturn(Optional.empty());
        BusinessProfileException businessProfileException = assertThrows(BusinessProfileException.class, () ->
                merchantService.getBusinessInfoByBusinessId(MERCHANT_ID, RoleType.MERCHANT));
        assertEquals("Invalid business id", businessProfileException.getMessage());
    }

    @Test
    void Should_ThrowBusinessProfileException_When_GetBusinessInfoByBusinessIdIsFailed() {
        when(businessRepository.findById(MERCHANT_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.getBusinessInfoByBusinessId(MERCHANT_ID, RoleType.MERCHANT));
        assertEquals("Failed to get business profile from database for businessId: " + MERCHANT_ID, authServiceException.getMessage());
    }

    @Test
    void Should_ReturnBusiness_When_RoleTypeIsMerchantForGetLatestBusinessInfoByUserId() {
        Business business = getSampleBusiness();
        Optional<Business> optionalBusiness = Optional.of(business);
        when(businessRepository.getLatestBusinessByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(business));
        assertEquals(optionalBusiness, merchantService.getLatestBusinessInfoByUserId(MERCHANT_ID, RoleType.MERCHANT));
    }

    @Test
    void Should_ReturnBusiness_When_RoleTypeIsBankForGetLatestBusinessInfoByUserId() {
        Business business = getSampleBusiness();
        BankBusiness bankBusiness = new BankBusiness(business);
        Optional<Business> optionalBusiness = Optional.of(business);
        when(bankBusinessRepository.getLatestBusinessByBankId(BANK_ID)).thenReturn(Optional.of(bankBusiness));
        var receivedBusiness = merchantService.getLatestBusinessInfoByUserId(BANK_ID, RoleType.BANK);
        String businessId = null;
        if (receivedBusiness.isPresent()) {
            businessId = receivedBusiness.get().getId();
        }
        assertEquals(optionalBusiness.get().getId(), businessId);
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetLatestBusinessInfoByUserIdIsFailed() {
        when(businessRepository.getLatestBusinessByMerchantId(MERCHANT_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.getLatestBusinessInfoByUserId(MERCHANT_ID, RoleType.MERCHANT));
        assertEquals("Failed to get latest business profile from database for userId: " + MERCHANT_ID,
                authServiceException.getMessage());
    }

    @Test
    void Should_validateMerchant_When_ValidMerchantIdIsProvided() {
        User merchant = getSampleMerchant();
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        merchantService.validateMerchantId(MERCHANT_ID);
        verify(userRepository).findById(MERCHANT_ID);
    }

    @Test
    void Should_ThrowInvalidUserException_When_InvalidMerchantIdIsProvided() {
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.empty());
        InvalidUserException invalidUserException = assertThrows(InvalidUserException.class, () ->
                merchantService.validateMerchantId(MERCHANT_ID));
        assertEquals("Invalid merchant id: " + MERCHANT_ID, invalidUserException.getMessage());

    }

    @Test
    void Should_UpdateMerchantApprovalStatus_When_ApprovalStatusActionIsPendingApprove() {
        User merchant = getSampleMerchant();
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        MerchantBankSearchIndex merchantBankSearchIndex = new MerchantBankSearchIndex(merchant);
        when(merchantBankSearchIndexRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchantBankSearchIndex));
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        NotificationMetaData notificationMetaData = new NotificationMetaData(ApprovalStatus.APPROVED, merchant, TIME_ZONE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getSms())).thenReturn(SMS_MESSAGE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailSubject())).thenReturn(APPROVE_MERCHANT_SUBJECT);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailBody())).thenReturn(APPROVE_MERCHANT_BODY);
        merchantService.updateMerchantApprovalStatus(merchantStatusUpdateRequestDto, TIME_ZONE);
        verify(notificationService).sendSms(any(MobileNoRequestDto.class), anyString(), anyString());
        verify(merchantBankSearchIndexRepository).save(merchantBankSearchIndex);
    }

    @Test
    void Should_UpdateMerchantApprovalStatus_When_ApprovalStatusActionIsBlockUnblock() {
        User merchant = getSampleMerchant();
        merchant.setApprovalStatus(ApprovalStatus.BLOCKED);
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setAction(ApprovalAction.UNBLOCK);
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        NotificationMetaData notificationMetaData = new NotificationMetaData(ApprovalStatus.UNBLOCKED, merchant, TIME_ZONE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getSms())).thenReturn(SMS_MESSAGE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailSubject())).thenReturn(UNBLOCK_MERCHANT_SUBJECT);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailBody())).thenReturn(UNBLOCK_MERCHANT_BODY);
        merchantService.updateMerchantApprovalStatus(merchantStatusUpdateRequestDto, TIME_ZONE);
        verify(notificationService).sendSms(any(MobileNoRequestDto.class), anyString(), anyString());
    }

    @Test
    void Should_UpdateMerchantApprovalStatus_When_ApprovalStatusActionIsUnblockBlock() {
        User merchant = getSampleMerchant();
        merchant.setApprovalStatus(ApprovalStatus.UNBLOCKED);
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setAction(ApprovalAction.BLOCK);
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        NotificationMetaData notificationMetaData = new NotificationMetaData(ApprovalStatus.BLOCKED, merchant, TIME_ZONE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getSms())).thenReturn(SMS_MESSAGE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailSubject())).thenReturn(BLOCK_MERCHANT_SUBJECT);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailBody())).thenReturn(BLOCK_MERCHANT_BODY);
        merchantService.updateMerchantApprovalStatus(merchantStatusUpdateRequestDto, TIME_ZONE);
        verify(notificationService).sendSms(any(MobileNoRequestDto.class), anyString(), anyString());
    }

    @Test
    void Should_UpdateMerchantApprovalStatus_When_ApprovalStatusActionIsApproveBlock() {
        User merchant = getSampleMerchant();
        merchant.setApprovalStatus(ApprovalStatus.APPROVED);
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setAction(ApprovalAction.BLOCK);
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        NotificationMetaData notificationMetaData = new NotificationMetaData(ApprovalStatus.BLOCKED, merchant, TIME_ZONE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getSms())).thenReturn(SMS_MESSAGE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailSubject())).thenReturn(BLOCK_MERCHANT_SUBJECT);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailBody())).thenReturn(BLOCK_MERCHANT_BODY);
        merchantService.updateMerchantApprovalStatus(merchantStatusUpdateRequestDto, TIME_ZONE);
        verify(notificationService).sendSms(any(MobileNoRequestDto.class), anyString(), anyString());
    }

    @Test
    void Should_ThrowAuthServiceException_When_BlockedMerchantSaveIntoDataBaseIsFailedForApproveBlock() {
        User merchant = getSampleMerchant();
        merchant.setApprovalStatus(ApprovalStatus.APPROVED);
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setAction(ApprovalAction.BLOCK);
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        when(blockedMerchantRepository.save(any(BlockedMerchant.class))).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                merchantService.updateMerchantApprovalStatus(merchantStatusUpdateRequestDto, TIME_ZONE));
        assertEquals("Saving blocked-merchant comment to database was failed for merchantId: " + MERCHANT_ID,
                exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidActionException_When_InvalidApprovalStateActionIsProvidedForUpdateMerchantApprovalStatus() {
        User merchant = getSampleMerchant();
        merchant.setApprovalStatus(ApprovalStatus.PENDING);
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setAction(ApprovalAction.BLOCK);
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        InvalidActionException invalidActionException = assertThrows(InvalidActionException.class, () ->
                merchantService.updateMerchantApprovalStatus(merchantStatusUpdateRequestDto, TIME_ZONE));
        assertEquals("Invalid action: " + ApprovalAction.BLOCK.name(), invalidActionException.getMessage());
    }

    @Test
    void Should_ThrowInvalidUserException_When_InvalidMerchantIdIsProvidedForUpdateMerchantApprovalStatus() {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setAction(ApprovalAction.BLOCK);
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.empty());
        InvalidUserException invalidUserException = assertThrows(InvalidUserException.class, () ->
                merchantService.updateMerchantApprovalStatus(merchantStatusUpdateRequestDto, TIME_ZONE));
        assertEquals("Invalid user id: " + MERCHANT_ID, invalidUserException.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GettingMerchantBankSearchIndexIsFailedForUpdateMerchantApprovalStatus() {
        User merchant = getSampleMerchant();
        merchant.setApprovalStatus(ApprovalStatus.PENDING);
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setAction(ApprovalAction.APPROVE);
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        when(merchantBankSearchIndexRepository.findById(MERCHANT_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.updateMerchantApprovalStatus(merchantStatusUpdateRequestDto, TIME_ZONE));
        assertEquals("Getting user from MerchantBankSearchIndex table was failed for userId: " + MERCHANT_ID, authServiceException.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GettingMerchantIsFailedForUpdateMerchantApprovalStatus() {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setAction(ApprovalAction.BLOCK);
        when(userRepository.findById(MERCHANT_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.updateMerchantApprovalStatus(merchantStatusUpdateRequestDto, TIME_ZONE));
        assertEquals("Retrieving data failed for approval status update for userId: " + MERCHANT_ID, authServiceException.getMessage());
    }

    @Test
    void Should_ReturnBusinessPage_When_RoleTypeIsMerchantAndSearchTermIsAllForGetActiveBusinessProfiles() {
        Page<Business> businesses = getSampleBusinessPage();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(approvedBusinessRepository.getActiveMerchantProfiles(pageable)).thenReturn(businesses);
        assertEquals(businesses, merchantService.getActiveBusinessProfiles(RoleType.MERCHANT, pageable, ALL));
    }

    @Test
    void Should_ReturnBusinessPage_When_RoleTypeIsMerchantAndSearchTermNotEqualToAllForGetActiveBusinessProfiles() {
        Page<Business> businesses = getSampleBusinessPage();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(approvedBusinessRepository.getActiveMerchantProfilesWithSearchTerm(SEARCH_TERM, pageable))
                .thenReturn(businesses);
        assertEquals(businesses, merchantService.getActiveBusinessProfiles(RoleType.MERCHANT, pageable, SEARCH_TERM));
    }

    @Test
    void Should_ReturnBusinessPage_When_RoleTypeIsBankAndSearchTermIsAllForGetActiveBusinessProfiles() {
        Page<Business> businesses = getSampleBusinessPage();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(approvedBankBusinessRepository.getActiveBankProfiles(pageable)).thenReturn(businesses);
        assertEquals(businesses, merchantService.getActiveBusinessProfiles(RoleType.BANK, pageable, ALL));
    }

    @Test
    void Should_ReturnBusinessPage_When_RoleTypeIsBankAndSearchTermNotEqualToAllForGetActiveBusinessProfiles() {
        Page<Business> businesses = getSampleBusinessPage();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(approvedBankBusinessRepository.getActiveBankProfilesWithSearchTerm(SEARCH_TERM, pageable))
                .thenReturn(businesses);
        assertEquals(businesses, merchantService.getActiveBusinessProfiles(RoleType.BANK, pageable, SEARCH_TERM));
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetActiveBusinessProfilesIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(approvedBankBusinessRepository.getActiveBankProfilesWithSearchTerm(SEARCH_TERM, pageable))
                .thenThrow(new DataAccessException("ERROR") {
                });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.getActiveBusinessProfiles(RoleType.BANK, pageable, SEARCH_TERM));
        assertEquals("Failed to get active business profile page from database", authServiceException.getMessage());
    }

    @Test
    void Should_ReturnContact_When_ExistingContactIsPresentForCreateOrUpdateContact() {
        Contact contact = getSampleContact();
        User merchant = getSampleMerchant();
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        when(contactRepository.getPendingContactByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(contact));
        assertEquals(contact, merchantService.createOrUpdateContact(contactRequestDto, RoleType.MERCHANT));
    }

    @Test
    void Should_ReturnContact_When_CreatingNewContactForCreateOrUpdateContact() {
        User merchant = getSampleMerchant();
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        when(contactRepository.getPendingContactByMerchantId(MERCHANT_ID)).thenReturn(Optional.empty());
        assertEquals(merchant, merchantService.createOrUpdateContact(contactRequestDto,
                RoleType.MERCHANT).getMerchant());
    }

    @Test
    void Should_ThrowInvalidUserException_When_MerchantNotAvailableInDataBaseForCreateOrUpdateContact() {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.empty());
        InvalidUserException invalidUserException = assertThrows(InvalidUserException.class, () ->
                merchantService.createOrUpdateContact(contactRequestDto, RoleType.MERCHANT));
        assertEquals("Invalid userId: " + MERCHANT_ID, invalidUserException.getMessage());
    }

    @Test
    void Should_ThrowInvalidUserException_When_UrlRoleTypeNotMatchWithUserForCreateOrUpdateContact() {
        User merchant = getSampleMerchant();
        merchant.getRole().setName(RoleType.ADMIN.name());
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        InvalidUserException invalidUserException = assertThrows(InvalidUserException.class, () ->
                merchantService.createOrUpdateContact(contactRequestDto, RoleType.MERCHANT));
        assertEquals("URL roleType didn't match with user roleType for userId: " + MERCHANT_ID,
                invalidUserException.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetExistingContactIsFailedForCreateOrUpdateContact() {
        User merchant = getSampleMerchant();
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        when(contactRepository.getPendingContactByMerchantId(MERCHANT_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.createOrUpdateContact(contactRequestDto, RoleType.MERCHANT));
        assertEquals("Failed to get merchant/bank contact info from database for userId: " + MERCHANT_ID,
                authServiceException.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_SavingCreatedOrUpdatedContactIsFailedForCreateOrUpdateContact() {
        User merchant = getSampleMerchant();
        Contact contact = getSampleContact();
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        when(contactRepository.getPendingContactByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(contact));
        when(contactRepository.save(contact)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.createOrUpdateContact(contactRequestDto, RoleType.MERCHANT));
        assertEquals("Saving contact data to database was failed for userId: " + MERCHANT_ID,
                authServiceException.getMessage());
    }

    @Test
    void Should_ReturnContact_When_ValidContactIdIsProvidedForGetContactInfoByContactId() {
        Contact contact = getSampleContact();
        when(contactRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(contact));
        assertEquals(contact, merchantService.getContactInfoByContactId(MERCHANT_ID));
    }

    @Test
    void Should_ThrowContactProfileException_When_InvalidContactIdIsProvidedForGetContactInfoByContactId() {
        when(contactRepository.findById(MERCHANT_ID)).thenReturn(Optional.empty());
        ContactProfileException contactProfileException = assertThrows(ContactProfileException.class, () ->
                merchantService.getContactInfoByContactId(MERCHANT_ID));
        assertEquals("Invalid contactId: " + MERCHANT_ID, contactProfileException.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetContactFromDataBaseIsFailedForGetContactInfoByContactId() {
        when(contactRepository.findById(MERCHANT_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.getContactInfoByContactId(MERCHANT_ID));
        assertEquals("Failed to get contact profile from database for contactId: " + MERCHANT_ID,
                authServiceException.getMessage());
    }

    @Test
    void Should_ReturnOptionalContact_When_ValidMerchantIdIsProvidedForGetContactInfoByMerchantId() {
        Contact contact = getSampleContact();
        Optional<Contact> optionalContact = Optional.of(contact);
        when(contactRepository.getLastUpdatedContactByMerchantId(MERCHANT_ID)).thenReturn(optionalContact);
        assertEquals(optionalContact, merchantService.getContactInfoByMerchantId(MERCHANT_ID));
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetOptionalContactFromDataBaseIsFailed() {
        when(contactRepository.getLastUpdatedContactByMerchantId(MERCHANT_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.getContactInfoByMerchantId(MERCHANT_ID));
        assertEquals("Failed to get merchant/bank contact info from database for userId: " + MERCHANT_ID,
                authServiceException.getMessage());
    }

    @Test
    void Should_ReturnBusinessPage_When_SearchTermIsEqualToAllForGetPendingBusinessInfoList() {
        Page<Business> businessPage = getSampleBusinessPage();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(businessRepository.findByApprovalStatus(ApprovalStatus.PENDING, pageable)).thenReturn(businessPage);
        assertEquals(businessPage, merchantService.getPendingBusinessInfoList(PAGE, SIZE, ALL));
    }

    @Test
    void Should_ReturnBusinessPage_When_SearchTermIsNotEqualToAllForGetPendingBusinessInfoList() {
        Page<Business> businessPage = getSampleBusinessPage();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(businessRepository.findByApprovalStatusAndBusinessNameContaining(ApprovalStatus.PENDING, SEARCH_TERM, pageable))
                .thenReturn(businessPage);
        assertEquals(businessPage, merchantService.getPendingBusinessInfoList(PAGE, SIZE, SEARCH_TERM));
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetBusinessPageFromDataBaseIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(businessRepository.findByApprovalStatusAndBusinessNameContaining(ApprovalStatus.PENDING, SEARCH_TERM, pageable))
                .thenThrow(new DataAccessException("ERROR") {
                });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.getPendingBusinessInfoList(PAGE, SIZE, SEARCH_TERM));
        assertEquals("Failed to get pending business info page from database.", authServiceException.getMessage());
    }

    @Test
    void Should_ReturnBankBusinessPage_When_SearchTermIsEqualToAllForGetPendingBankBusinessInfoListt() {
        Page<BankBusiness> bankBusinessPage = getSampleBankBusinessPage();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(bankBusinessRepository.findByApprovalStatus(ApprovalStatus.PENDING, pageable)).thenReturn(bankBusinessPage);
        assertEquals(bankBusinessPage, merchantService.getPendingBankBusinessInfoList(PAGE, SIZE, ALL));
    }

    @Test
    void Should_ReturnBankBusinessPage_When_SearchTermIsNotEqualToAllForGetPendingBankBusinessInfoList() {
        Page<BankBusiness> bankBusinessPage = getSampleBankBusinessPage();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(bankBusinessRepository.findByApprovalStatusAndBusinessNameContaining(ApprovalStatus.PENDING, SEARCH_TERM, pageable))
                .thenReturn(bankBusinessPage);
        assertEquals(bankBusinessPage, merchantService.getPendingBankBusinessInfoList(PAGE, SIZE, SEARCH_TERM));
    }

    @Test
    void Should_ThrowAuthServiceException_When_BankBusinessPageFromDataBaseIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(bankBusinessRepository.findByApprovalStatusAndBusinessNameContaining(ApprovalStatus.PENDING, SEARCH_TERM, pageable))
                .thenThrow(new DataAccessException("ERROR") {
                });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.getPendingBankBusinessInfoList(PAGE, SIZE, SEARCH_TERM));
        assertEquals("Failed to get pending bank business info page from database.", authServiceException.getMessage());
    }

    @Test
    void Should_UpdateMerchantBusinessApprovalStatus_When_RoleTypeIsMerchantAndApprovalActionIsApprove() {
        Business business = getSampleBusiness();
        RejectedProfileUpdates rejectedProfileUpdates = getSampleRejectedProfileUpdates();
        MerchantBankSearchIndex merchantBankSearchIndex = getSampleMerchantBankSearchIndex();
        business.setApprovalStatus(ApprovalStatus.PENDING);
        when(businessRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(business));
        when(merchantBankSearchIndexRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchantBankSearchIndex));
        NotificationMetaData notificationMetaData = new NotificationMetaData(ApprovalStatus.APPROVED, business.getMerchant(), TIME_ZONE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getSms())).thenReturn(SMS_MESSAGE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailSubject())).thenReturn(APPROVE_MERCHANT_SUBJECT);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailBody())).thenReturn(APPROVE_MERCHANT_BODY);
        merchantService.updateMerchantBusinessApprovalStatus(rejectedProfileUpdates, ApprovalAction.APPROVE, TIME_ZONE, RoleType.MERCHANT);
        verify(notificationService).sendSms(any(MobileNoRequestDto.class), anyString(), anyString());
        verify(approvedBusinessRepository).deleteByMerchantId(MERCHANT_ID);
    }

    @Test
    void Should_UpdateMerchantBusinessApprovalStatus_When_RoleTypeIsBankAndApprovalActionIsApprove() {
        BankBusiness bankBusiness = getSampleBankBusiness();
        RejectedProfileUpdates rejectedProfileUpdates = getSampleRejectedProfileUpdates();
        MerchantBankSearchIndex merchantBankSearchIndex = getSampleMerchantBankSearchIndex();
        bankBusiness.setApprovalStatus(ApprovalStatus.PENDING);
        when(bankBusinessRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(bankBusiness));
        when(merchantBankSearchIndexRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchantBankSearchIndex));
        NotificationMetaData notificationMetaData = new NotificationMetaData(ApprovalStatus.APPROVED, bankBusiness.getBank(), TIME_ZONE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getSms())).thenReturn(SMS_MESSAGE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailSubject())).thenReturn(APPROVE_MERCHANT_SUBJECT);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailBody())).thenReturn(APPROVE_MERCHANT_BODY);
        merchantService.updateMerchantBusinessApprovalStatus(rejectedProfileUpdates, ApprovalAction.APPROVE, TIME_ZONE, RoleType.BANK);
        verify(notificationService).sendSms(any(MobileNoRequestDto.class), anyString(), anyString());
        verify(approvedBankBusinessRepository).deleteByBankId(MERCHANT_ID);
    }

    @Test
    void Should_UpdateMerchantBusinessApprovalStatus_When_RoleTypeIsMerchantAndApprovalActionIsReject() {
        Business business = getSampleBusiness();
        RejectedProfileUpdates rejectedProfileUpdates = getSampleRejectedProfileUpdates();
        MerchantBankSearchIndex merchantBankSearchIndex = getSampleMerchantBankSearchIndex();
        business.setApprovalStatus(ApprovalStatus.PENDING);
        when(businessRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(business));
        when(merchantBankSearchIndexRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchantBankSearchIndex));
        NotificationMetaData notificationMetaData = new NotificationMetaData(ApprovalStatus.REJECTED, business.getMerchant(), TIME_ZONE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getSms())).thenReturn(SMS_MESSAGE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailSubject())).thenReturn(REJECT_MERCHANT_SUBJECT);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailBody())).thenReturn(REJECT_MERCHANT_BODY);
        merchantService.updateMerchantBusinessApprovalStatus(rejectedProfileUpdates, ApprovalAction.REJECT, TIME_ZONE, RoleType.MERCHANT);
        verify(notificationService).sendSms(any(MobileNoRequestDto.class), anyString(), anyString());
        verify(rejectedProfileUpdatesRepository).save(rejectedProfileUpdates);
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetMerchantBankSearchIndexFromDataBaseIdIsFailed() {
        Business business = getSampleBusiness();
        RejectedProfileUpdates rejectedProfileUpdates = getSampleRejectedProfileUpdates();
        business.setApprovalStatus(ApprovalStatus.PENDING);
        when(businessRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(business));
        when(merchantBankSearchIndexRepository.findById(MERCHANT_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.updateMerchantBusinessApprovalStatus(rejectedProfileUpdates, ApprovalAction.APPROVE, TIME_ZONE, RoleType.MERCHANT));
        assertEquals("Updating merchant bank search index failed. ", authServiceException.getMessage());
    }

    @Test
    void Should_ThrowInvalidActionException_When_InvalidApprovalActionIsProvidedForUpdateMerchantBusinessApprovalStatus() {
        Business business = getSampleBusiness();
        RejectedProfileUpdates rejectedProfileUpdates = getSampleRejectedProfileUpdates();
        business.setApprovalStatus(ApprovalStatus.BLOCKED);
        when(businessRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(business));
        InvalidActionException invalidActionException = assertThrows(InvalidActionException.class, () ->
                merchantService.updateMerchantBusinessApprovalStatus(rejectedProfileUpdates, ApprovalAction.BLOCK, TIME_ZONE, RoleType.MERCHANT));
        assertEquals("Invalid action: " + ApprovalAction.BLOCK.name(), invalidActionException.getMessage());
    }

    @Test
    void Should_ThrowInvalidUserException_When_InvalidApprovalActionIsProvidedForUpdateMerchantBusinessApprovalStatus() {
        RejectedProfileUpdates rejectedProfileUpdates = getSampleRejectedProfileUpdates();
        when(businessRepository.findById(MERCHANT_ID)).thenReturn(Optional.empty());
        InvalidUserException invalidUserException = assertThrows(InvalidUserException.class, () ->
                merchantService.updateMerchantBusinessApprovalStatus(rejectedProfileUpdates, ApprovalAction.APPROVE, TIME_ZONE, RoleType.MERCHANT));
        assertEquals("Invalid businessId: " + rejectedProfileUpdates.getId(), invalidUserException.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetBusinessFromDataBaseIsFailedForUpdateMerchantBusinessApprovalStatus() {
        RejectedProfileUpdates rejectedProfileUpdates = getSampleRejectedProfileUpdates();
        when(businessRepository.findById(MERCHANT_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.updateMerchantBusinessApprovalStatus(rejectedProfileUpdates, ApprovalAction.APPROVE, TIME_ZONE, RoleType.MERCHANT));
        assertEquals("Failed to update business approval status for businessId: " + rejectedProfileUpdates.getId(), authServiceException.getMessage());
    }

    @Test
    void Should_ReturnBusiness_When_RoleTypeIsEqualToMerchantForGetApprovedBusinessInfoByUserId() {
        ApprovedBusiness approvedBusiness = getSampleApprovedBusiness();
        when(approvedBusinessRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(approvedBusiness));
        assertEquals(approvedBusiness.getBusiness(), merchantService.getApprovedBusinessInfoByUserId(MERCHANT_ID, RoleType.MERCHANT));
    }

    @Test
    void Should_ReturnBusiness_When_RoleTypeIsEqualToBankForGetApprovedBusinessInfoByUserId() {
        ApprovedBankBusiness approvedBankBusiness = getSampleApprovedBankBusiness();
        when(approvedBankBusinessRepository.findByBankId(BANK_ID)).thenReturn(Optional.of(approvedBankBusiness));
        assertEquals(approvedBankBusiness.getBankBusiness().getId(), merchantService.getApprovedBusinessInfoByUserId(BANK_ID, RoleType.BANK).getId());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetApprovedBusinessFromDatabaseIsFailedForGetApprovedBusinessInfoByUserId() {
        ApprovedBusiness approvedBusiness = getSampleApprovedBusiness();
        when(approvedBusinessRepository.findByMerchantId(MERCHANT_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.getApprovedBusinessInfoByUserId(MERCHANT_ID, RoleType.MERCHANT));
        assertEquals("Failed to get latest approved business profile from database for userId: " + MERCHANT_ID,
                authServiceException.getMessage());
    }

    @Test
    void Should_ReturnPendingContactResponseDtoList_When_RoleTypeIsEqualToMerchantForGetLatestBusinessProfileByMerchantId() {
        PendingContactResponseDto pendingContactResponseDto = getSamplePendingContactResponseDto();
        Business business = getSampleBusiness();
        ApprovedBusiness approvedBusiness = getSampleApprovedBusiness();
        Page<Contact> contactPage = getSampleContactPage();
        List<PendingContactResponseDto> pendingContactResponseDtoList = new ArrayList<>();
        pendingContactResponseDtoList.add(pendingContactResponseDto);
        when(approvedBusinessRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(approvedBusiness));
        when(businessRepository.getLatestBusinessByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(business));
        assertEquals(pendingContactResponseDtoList.get(0).getMerchant().getId(),
                merchantService.getBusinessDetailsForContact(contactPage, RoleType.MERCHANT.name(), TIME_ZONE).get(0).getMerchant().getId());
    }

    @Test
    void Should_ReturnPendingContactResponseDtoList_When_RoleTypeIsEqualToBankForGetLatestBusinessProfileByMerchantId() {
        PendingContactResponseDto pendingContactResponseDto = getSamplePendingContactResponseDto();
        Business business = getSampleBusiness();
        ApprovedBankBusiness approvedBankBusiness = getSampleApprovedBankBusiness();
        Page<Contact> contactPage = getSampleContactPage();
        List<PendingContactResponseDto> pendingContactResponseDtoList = new ArrayList<>();
        pendingContactResponseDtoList.add(pendingContactResponseDto);
        when(approvedBankBusinessRepository.findByBankId(MERCHANT_ID)).thenReturn(Optional.of(approvedBankBusiness));
        when(businessRepository.getLatestBusinessByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(business));
        assertEquals(pendingContactResponseDtoList.get(0).getMerchant().getId(),
                merchantService.getBusinessDetailsForContact(contactPage, RoleType.BANK.name(), TIME_ZONE).get(0).getMerchant().getId());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetApproveBusinessFromDataBaseIsFailedForGetLatestBusinessProfileByMerchantId() {
        Page<Contact> contactPage = getSampleContactPage();
        when(approvedBusinessRepository.findByMerchantId(MERCHANT_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.getBusinessDetailsForContact(contactPage, ROLE_TYPE_MERCHANT, TIME_ZONE));
        assertEquals("Failed to get latest business profile from database for userId: " + MERCHANT_ID, authServiceException.getMessage());
    }

    @Test
    void Should_ReturnContactPage_When_SearchTermIsEqualToAllForGetPendingContactInfoList() {
        Page<Contact> contactPage = getSampleContactPage();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.Direction.ASC, UPDATED_AT);
        when(contactRepository.findByApprovalStatusAndRoleType(ApprovalStatus.PENDING, RoleType.MERCHANT, pageable))
                .thenReturn(contactPage);
        assertEquals(contactPage, merchantService.getPendingContactInfoList(PAGE, SIZE, ALL, RoleType.MERCHANT));
    }

    @Test
    void Should_ReturnContactPage_When_SearchTermIsNotEqualToAllForGetPendingContactInfoList() {
        Page<Contact> contactPage = getSampleContactPage();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.Direction.ASC, UPDATED_AT);
        when(contactRepository.findByApprovalStatusAndRoleTypeAndNameContaining(ApprovalStatus.PENDING,
                RoleType.MERCHANT, SEARCH_TERM, pageable)).thenReturn(contactPage);
        assertEquals(contactPage, merchantService.getPendingContactInfoList(PAGE, SIZE, SEARCH_TERM, RoleType.MERCHANT));
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetContactPageFromDataBaseIsFailedForGetPendingContactInfoList() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.Direction.ASC, UPDATED_AT);
        when(contactRepository.findByApprovalStatusAndRoleType(ApprovalStatus.PENDING, RoleType.MERCHANT, pageable))
                .thenThrow(new DataAccessException("ERROR") {
                });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.getPendingContactInfoList(PAGE, SIZE, ALL, RoleType.MERCHANT));
        assertEquals("Failed to get pending contact info page from database.", authServiceException.getMessage());
    }

    @Test
    void Should_ReturnBusinessList_When_RoleTypeIsEqualToMerchant() {
        List<Business> businessList = getSampleBusinessList();
        List<String> merchantIdList = getSampleMerchantIdList();
        ApprovedBusiness approvedBusiness = getSampleApprovedBusiness();
        when(approvedBusinessRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(approvedBusiness));
        assertEquals(businessList.get(0).getBusinessName(),
                merchantService.getBulkMerchantList(merchantIdList, RoleType.MERCHANT).get(0).getBusinessName());
    }

    @Test
    void Should_ReturnBusinessList_When_RoleTypeIsEqualToBank() {
        List<Business> businessList = getSampleBusinessList();
        List<String> merchantIdList = getSampleMerchantIdList();
        ApprovedBankBusiness approvedBankBusiness = getSampleApprovedBankBusiness();
        List<ApprovedBankBusiness> approvedBankBusinessList = new ArrayList<>();
        approvedBankBusinessList.add(approvedBankBusiness);
        when(approvedBankBusinessRepository.findAllByBankIdIn(merchantIdList)).thenReturn(Optional.of(approvedBankBusinessList));
        assertEquals(businessList.get(0).getBusinessName(),
                merchantService.getBulkMerchantList(merchantIdList, RoleType.BANK).get(0).getBusinessName());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetApprovedBusinessFromDataBaseIsFailed() {
        List<String> merchantIdList = getSampleMerchantIdList();
        when(approvedBusinessRepository.findByMerchantId(MERCHANT_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.getBulkMerchantList(merchantIdList, RoleType.MERCHANT));
        assertEquals("Error getting bulk merchant list", authServiceException.getMessage());
    }

    @Test
    void Should_UpdateMerchantContactApprovalStatus_When_ApprovalActionIsEqualToApprove() {
        Contact contact = getSampleContact();
        contact.setApprovalStatus(ApprovalStatus.PENDING);
        User merchant = getSampleMerchant();
        RejectedProfileUpdates rejectedProfileUpdates = getSampleRejectedProfileUpdates();
        when(contactRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(contact));
        NotificationMetaData notificationMetaData = new NotificationMetaData(ApprovalStatus.APPROVED, merchant, TIME_ZONE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getSms())).thenReturn(SMS_MESSAGE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailSubject())).thenReturn(APPROVE_MERCHANT_SUBJECT);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailBody())).thenReturn(APPROVE_MERCHANT_BODY);
        merchantService.updateMerchantContactApprovalStatus(rejectedProfileUpdates, ApprovalAction.APPROVE, TIME_ZONE);
        verify(notificationService).sendSms(any(MobileNoRequestDto.class), anyString(), anyString());
        verify(contactRepository).save(contact);
    }

    @Test
    void Should_UpdateMerchantContactApprovalStatus_When_ApprovalActionIsEqualToReject() {
        Contact contact = getSampleContact();
        contact.setApprovalStatus(ApprovalStatus.PENDING);
        User merchant = getSampleMerchant();
        RejectedProfileUpdates rejectedProfileUpdates = getSampleRejectedProfileUpdates();
        when(contactRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(contact));
        NotificationMetaData notificationMetaData = new NotificationMetaData(ApprovalStatus.REJECTED, merchant, TIME_ZONE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getSms())).thenReturn(SMS_MESSAGE);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailSubject())).thenReturn(REJECT_MERCHANT_SUBJECT);
        when(translator.toLocale(notificationMetaData.getApprovalStatus().getEmailBody())).thenReturn(REJECT_MERCHANT_BODY);
        merchantService.updateMerchantContactApprovalStatus(rejectedProfileUpdates, ApprovalAction.REJECT, TIME_ZONE);
        verify(notificationService).sendSms(any(MobileNoRequestDto.class), anyString(), anyString());
        verify(rejectedProfileUpdatesRepository).save(rejectedProfileUpdates);
    }

    @Test
    void Should_ThrowInvalidActionException_When_InvalidApproveActionIsProvided() {
        Contact contact = getSampleContact();
        contact.setApprovalStatus(ApprovalStatus.BLOCKED);
        RejectedProfileUpdates rejectedProfileUpdates = getSampleRejectedProfileUpdates();
        when(contactRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(contact));
        InvalidActionException invalidActionException = assertThrows(InvalidActionException.class, () ->
                merchantService.updateMerchantContactApprovalStatus(rejectedProfileUpdates, ApprovalAction.BLOCK, TIME_ZONE));
        assertEquals("Invalid action: " + ApprovalAction.BLOCK.name(), invalidActionException.getMessage());
    }

    @Test
    void Should_ThrowInvalidUserException_When_InvalidContactIdIsProvided() {
        RejectedProfileUpdates rejectedProfileUpdates = getSampleRejectedProfileUpdates();
        when(contactRepository.findById(MERCHANT_ID)).thenReturn(Optional.empty());
        InvalidUserException invalidUserException = assertThrows(InvalidUserException.class, () ->
                merchantService.updateMerchantContactApprovalStatus(rejectedProfileUpdates, ApprovalAction.BLOCK, TIME_ZONE));
        assertEquals("Invalid contactId: " + rejectedProfileUpdates.getId(), invalidUserException.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_UpdateContactApprovalStatusIsFailed() {
        Contact contact = getSampleContact();
        contact.setApprovalStatus(ApprovalStatus.PENDING);
        RejectedProfileUpdates rejectedProfileUpdates = getSampleRejectedProfileUpdates();
        when(contactRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(contact));
        when(contactRepository.save(contact)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException authServiceException = assertThrows(AuthServiceException.class, () ->
                merchantService.updateMerchantContactApprovalStatus(rejectedProfileUpdates, ApprovalAction.APPROVE, TIME_ZONE));
        assertEquals("Failed to update contact approval status for contactId: " + rejectedProfileUpdates.getId(), authServiceException.getMessage());
    }

    /**
     * Validate business profile with merchantId
     */
    @Test
    void Should_ValidateBusinessProfileWithMerchantIdForMerchant() {
        when(approvedBusinessRepository.findByMerchantId(MERCHANT_ID))
                .thenReturn(Optional.of(getSampleApprovedBusiness()));
        merchantService.validateBusinessProfileWithMerchantId(MERCHANT_ID, RoleType.MERCHANT);
    }

    @Test
    void Should_ValidateBusinessProfileWithMerchantIdForNonMerchant() {
        when(approvedBankBusinessRepository.findByBankId(MERCHANT_ID))
                .thenReturn(Optional.of(getSampleApprovedBankBusiness()));
        merchantService.validateBusinessProfileWithMerchantId(MERCHANT_ID, RoleType.BANK);
    }

    @Test
    void Should_ThrowInvalidUserException_When_ValidateBusinessProfileWithMerchantIdForInvalidMerchantId() {
        when(approvedBusinessRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.empty());
        InvalidUserException exception = assertThrows(InvalidUserException.class, () -> merchantService
                .validateBusinessProfileWithMerchantId(MERCHANT_ID, RoleType.MERCHANT));
        assertEquals("Invalid merchant id: " + MERCHANT_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidUserException_When_ValidateBusinessProfileWithMerchantIdForInvalidBankId() {
        when(approvedBankBusinessRepository.findByBankId(MERCHANT_ID)).thenReturn(Optional.empty());
        InvalidUserException exception = assertThrows(InvalidUserException.class, () -> merchantService
                .validateBusinessProfileWithMerchantId(MERCHANT_ID, RoleType.BANK));
        assertEquals("Invalid bank id: " + MERCHANT_ID, exception.getMessage());
    }

    @Test
    void Should_ReturnActiveMerchantCount() {
        when(approvedBusinessRepository.getActiveMerchantCount()).thenReturn(ACTIVE_MERCHANT_COUNT);
        assertEquals(ACTIVE_MERCHANT_COUNT, merchantService.getActiveMerchantCount());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetActiveMerchantCountFromDatabaseIsFailed() {
        when(approvedBusinessRepository.getActiveMerchantCount()).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                merchantService.getActiveMerchantCount());
        assertEquals("Failed to get active merchant count from the db.", exception.getMessage());
    }

    @Test
    void Should_ReturnTrue_When_MerchantIsActive() {
        User merchant = getSampleMerchant();
        merchant.setApprovalStatus(ApprovalStatus.APPROVED);
        ApprovedBusiness approvedMerchantBusiness = getSampleApprovedBusiness();
        when(userRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        when(approvedBusinessRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(approvedMerchantBusiness));
        assertTrue(merchantService.isMerchantOrBankActive(MERCHANT_ID));
    }

    @Test
    void Should_ReturnTrue_When_BankIsActive() {
        User bank = getSampleBank();
        bank.setApprovalStatus(ApprovalStatus.APPROVED);
        ApprovedBankBusiness approvedBankBusiness = getSampleApprovedBankBusiness();
        when(userRepository.findById(BANK_ID)).thenReturn(Optional.of(bank));
        when(approvedBankBusinessRepository.findByBankId(BANK_ID)).thenReturn(Optional.of(approvedBankBusiness));
        assertTrue(merchantService.isMerchantOrBankActive(BANK_ID));
    }

    @Test
    void Should_ReturnFalse_When_ProvidedInvalidMerchantOrBankId() {
        when(userRepository.findById(BANK_ID)).thenReturn(Optional.empty());
        assertFalse(merchantService.isMerchantOrBankActive(BANK_ID));
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetApprovedBusinessFromDataBaseIsFailedForIsActiveMerchantOrBankActive() {
        User bank = getSampleBank();
        bank.setApprovalStatus(ApprovalStatus.APPROVED);
        ApprovedBankBusiness approvedBankBusiness = getSampleApprovedBankBusiness();
        when(userRepository.findById(BANK_ID)).thenReturn(Optional.of(bank));
        when(approvedBankBusinessRepository.findByBankId(BANK_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                merchantService.isMerchantOrBankActive(BANK_ID));
        assertEquals("Failed to read user/approvedBusiness from the db to " +
                "verify isActive for merchantId: " + BANK_ID, exception.getMessage());
    }

    @Test
    void Should_ReturnBusinessProfile_When_ApprovedBusinessHasBusinessForMerchantIsLogin() {
        ApprovedBusiness approvedBusiness = getSampleApprovedBusiness();
        when(approvedBusinessRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(approvedBusiness));
        BusinessProfile businessProfile = merchantService.getMerchantOrBankLoginResponse(MERCHANT_ID, RoleType.MERCHANT.toString());
        assertTrue(businessProfile.isUpdated());
    }

    @Test
    void Should_ReturnBusinessProfile_When_ApprovedBusinessHasNotBusinessForMerchantIsLogin() {
        ApprovedBusiness approvedBusiness = getSampleApprovedBusiness();
        Business business = getSampleBusiness();
        approvedBusiness.setBusiness(null);
        when(approvedBusinessRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(approvedBusiness));
        when(businessRepository.getLatestBusinessByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(business));
        BusinessProfile businessProfile = merchantService.getMerchantOrBankLoginResponse(MERCHANT_ID, RoleType.MERCHANT.toString());
        assertTrue(businessProfile.isUpdated());
    }

    @Test
    void Should_ReturnBusinessProfile_When_EmptyBusinessIsReturnedForMerchantIsLogin() {
        ApprovedBusiness approvedBusiness = getSampleApprovedBusiness();
        approvedBusiness.setBusiness(null);
        when(approvedBusinessRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(approvedBusiness));
        when(businessRepository.getLatestBusinessByMerchantId(MERCHANT_ID)).thenReturn(Optional.empty());
        BusinessProfile businessProfile = merchantService.getMerchantOrBankLoginResponse(MERCHANT_ID, RoleType.MERCHANT.toString());
        assertFalse(businessProfile.isUpdated());
    }

    /**
     * This method creates sample BusinessRequestDto
     *
     * @return BusinessRequestDto
     */
    private BusinessRequestDto getSampleBusinessRequestDto() {
        BusinessRequestDto businessRequestDto = new BusinessRequestDto();
        businessRequestDto.setMerchantId(MERCHANT_ID);
        businessRequestDto.setBusinessName("Singer");
        businessRequestDto.setOwnerName("Owner");
        businessRequestDto.setOpeningHours(new OpeningHoursRequestDto());
        businessRequestDto.setMobileNo(new MobileNoRequestDto("+94", "713321911"));
        businessRequestDto.setTelephone(new MobileNoRequestDto("+94", "72993391"));
        businessRequestDto.setImageUrl("https://www.singersl.com/image.jpg");
        return businessRequestDto;
    }

    /**
     * This method creates sample Merchant
     *
     * @return User
     */
    private User getSampleMerchant() {
        User merchant = new User();
        merchant.setId(MERCHANT_ID);
        merchant.setFullName("Full name");
        merchant.setEmail("merchant@kupi.lk");
        merchant.setMobileNo("94-771109101");
        merchant.setApprovalStatus(ApprovalStatus.PENDING);
        merchant.setRegisteredUser(true);
        merchant.setCreatedAt(new Date(System.currentTimeMillis()));
        Role role = new Role();
        role.setId(3);
        role.setName(RoleType.MERCHANT.name());
        merchant.setRole(role);
        return merchant;
    }

    /**
     * This method creates sample bank
     *
     * @return User
     */
    private User getSampleBank() {
        User bank = new User();
        bank.setId(BANK_ID);
        bank.setFullName("Full name");
        bank.setEmail("merchant@kupi.lk");
        bank.setMobileNo("94-771109101");
        bank.setApprovalStatus(ApprovalStatus.PENDING);
        bank.setRegisteredUser(true);
        bank.setCreatedAt(new Date(System.currentTimeMillis()));
        Role role = new Role();
        role.setId(3);
        role.setName(RoleType.BANK.name());
        bank.setRole(role);
        return bank;
    }

    /**
     * This method creates sample Business
     *
     * @return Business
     */
    private Business getSampleBusiness() {
        BusinessRequestDto businessRequestDto = getSampleBusinessRequestDto();
        User merchant = getSampleMerchant();
        return new Business(businessRequestDto, merchant);
    }

    /**
     * This method creates sample business page
     *
     * @return BusinessPage
     */
    Page<Business> getSampleBusinessPage() {
        Business business = getSampleBusiness();
        List<Business> businesses = new ArrayList<>();
        businesses.add(business);
        return new PageImpl<>(businesses);
    }

    /**
     * This method creates sample MerchantStatusUpdateRequestDto
     *
     * @return MerchantStatusUpdateRequestDto
     */
    private MerchantStatusUpdateRequestDto getSampleMerchantStatusUpdateRequestDto() {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = new MerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setMerchantId(MERCHANT_ID);
        merchantStatusUpdateRequestDto.setAction(ApprovalAction.APPROVE);
        merchantStatusUpdateRequestDto.setComment(COMMENT);
        return merchantStatusUpdateRequestDto;
    }

    /**
     * This method creates sample merchant page
     *
     * @return MerchantPage
     */
    private Page<User> getSampleMerchantPage() {
        User merchant = getSampleMerchant();
        List<User> merchantList = new ArrayList<>();
        merchantList.add(merchant);
        return new PageImpl<>(merchantList);
    }

    /**
     * This method creates sample ContactRequestDto
     *
     * @return ContactRequestDto
     */
    private ContactRequestDto getSampleContactRequestDto() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto("+94", "771107101");
        return new ContactRequestDto(MERCHANT_ID, "NAME", "DESIGNATION", mobileNoRequestDto, "EMAIL");
    }

    /**
     * This method creates sample Contact
     *
     * @return Contact
     */
    private Contact getSampleContact() {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        User merchant = getSampleMerchant();
        return new Contact(contactRequestDto, merchant, RoleType.MERCHANT);
    }

    /**
     * This method creates sample BankBusiness
     *
     * @return BankBusiness
     */
    private BankBusiness getSampleBankBusiness() {
        Business business = getSampleBusiness();
        return new BankBusiness(business);
    }

    /**
     * This method creates sample BankBusinessPage
     *
     * @return BankBusinessPage
     */
    private Page<BankBusiness> getSampleBankBusinessPage() {
        BankBusiness bankBusiness = getSampleBankBusiness();
        List<BankBusiness> bankBusinessList = new ArrayList<>();
        bankBusinessList.add(bankBusiness);
        return new PageImpl<>(bankBusinessList);
    }

    /**
     * This method creates sample RejectedProfileUpdates
     *
     * @return RejectedProfileUpdates
     */
    private RejectedProfileUpdates getSampleRejectedProfileUpdates() {
        RejectedProfileUpdates rejectedProfileUpdates = new RejectedProfileUpdates();
        rejectedProfileUpdates.setId("rpuid-12589-7895-87458-8965");
        rejectedProfileUpdates.setComment(COMMENT);
        rejectedProfileUpdates.setMerchantInfoType(MerchantInfo.BUSINESS);
        rejectedProfileUpdates.setRoleType(RoleType.MERCHANT);
        rejectedProfileUpdates.setReferenceId(MERCHANT_ID);
        return rejectedProfileUpdates;
    }

    /**
     * This method creates sample MerchantBankSearchIndex
     *
     * @return MerchantBankSearchIndex
     */
    private MerchantBankSearchIndex getSampleMerchantBankSearchIndex() {
        User merchant = getSampleMerchant();
        return new MerchantBankSearchIndex(merchant);
    }

    /**
     * This method creates sample ApprovedBusiness
     *
     * @return ApprovedBusiness
     */
    private ApprovedBusiness getSampleApprovedBusiness() {
        Business business = getSampleBusiness();
        return new ApprovedBusiness(business);
    }

    /**
     * This method creates sample ApprovedBankBusiness
     *
     * @return ApprovedBankBusiness
     */
    private ApprovedBankBusiness getSampleApprovedBankBusiness() {
        Business business = getSampleBusiness();
        return new ApprovedBankBusiness(business);
    }

    /**
     * THis method created sample PendingContactResponseDto
     *
     * @return PendingContactResponseDto
     */
    private PendingContactResponseDto getSamplePendingContactResponseDto() {
        Contact contact = getSampleContact();
        Business business = getSampleBusiness();
        return new PendingContactResponseDto(contact, TIME_ZONE, business);
    }

    /**
     * This method creates sample Contact Page
     *
     * @return ContactPage
     */
    private Page<Contact> getSampleContactPage() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.Direction.ASC, UPDATED_AT);
        Contact contact = getSampleContact();
        List<Contact> contactList = new ArrayList<>();
        contactList.add(contact);
        return new PageImpl<>(contactList, pageable, 1);
    }

    /**
     * This method creates sample BusinessList
     *
     * @return BusinessList
     */
    private List<Business> getSampleBusinessList() {
        Business business = getSampleBusiness();
        List<Business> businessList = new ArrayList<>();
        businessList.add(business);
        return businessList;
    }

    /**
     * This method creates sample merchantId list
     *
     * @return MerchantIdList
     */
    private List<String> getSampleMerchantIdList() {
        List<String> merchantList = new ArrayList<>();
        merchantList.add(MERCHANT_ID);
        return merchantList;
    }
}
