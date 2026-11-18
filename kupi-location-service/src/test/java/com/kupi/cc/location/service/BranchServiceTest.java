package com.kupi.cc.location.service;

import com.kupi.cc.location.domain.entity.ApprovedBranch;
import com.kupi.cc.location.domain.entity.Branch;
import com.kupi.cc.location.domain.entity.BranchSearchIndex;
import com.kupi.cc.location.domain.entity.RejectedBranch;
import com.kupi.cc.location.domain.request.*;
import com.kupi.cc.location.domain.response.BulkBusinessResponseDto;
import com.kupi.cc.location.domain.response.BusinessResponseDto;
import com.kupi.cc.location.enums.*;
import com.kupi.cc.location.exception.*;
import com.kupi.cc.location.repository.ApprovedBranchRepository;
import com.kupi.cc.location.repository.BranchRepository;
import com.kupi.cc.location.repository.BranchSearchIndexRepository;
import com.kupi.cc.location.repository.RejectedBranchRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link BranchService} class
 */
class BranchServiceTest {

    private static final String BRANCH_ID = "brid-123456789";
    private static final String USER_ID = "uid-123456789";
    private static final String SEARCH_TERM = "Softlogic";
    private static final String COMMENT = "COMMENT";
    private static final String ALL = "ALL";
    private static final String DEFAULT_SORT = "updatedAt";
    private static final String SORT_NAME = "name";
    private static final String FAILED = "failed";
    private static final String BRANCH_NAME = "Arpico";
    private static final String BRANCH_ADDRESS = "BRANCH_ADDRESS";
    private static final String AUTH_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final int PAGE = 0;
    private static final int SIZE = 1;

    private BranchService branchService;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private BranchSearchIndexRepository branchSearchIndexRepository;
    @Mock
    private ApprovedBranchRepository approvedBranchRepository;
    @Mock
    private AuthUserService authUserService;
    @Mock
    private RejectedBranchRepository rejectedBranchRepository;
    @Mock
    private MailService mailService;
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        initMocks(this);
        branchService = new BranchService(branchRepository, branchSearchIndexRepository, approvedBranchRepository,
                authUserService, rejectedBranchRepository, mailService);
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Get branch list by userId
     */
    @Test
    void Should_ReturnBranchPage_When_GettingBranchListByUserIdForAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        when(branchRepository.findByUserId(anyString(), any(Pageable.class))).thenReturn(getSampleBranchPage());
        assertEquals(BRANCH_ID, branchService.getBranchListByUserId(USER_ID, ALL, pageable).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnBranchPage_When_GettingBranchListByUserIdForSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        when(branchRepository.getBranchesByUserIdAndSearchTerm(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(getSampleBranchPage());
        assertEquals(BRANCH_ID, branchService.getBranchListByUserId(USER_ID, SEARCH_TERM, pageable).getContent().get(0)
                .getId());
    }

    @Test
    void Should_ThrowKupiLocationException_When_GettingBranchListByUserIdIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        when(branchRepository.getBranchesByUserIdAndSearchTerm(anyString(), anyString(), any(Pageable.class)))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .getBranchListByUserId(USER_ID, SEARCH_TERM, pageable));
        assertEquals("Getting branch list was failed for userId: " + USER_ID, exception.getMessage());
    }

    /**
     * Get pending branch list
     */
    @Test
    void Should_ReturnBranchSearchIndexPage_When_GettingPendingBranchListForAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.Direction.DESC, DEFAULT_SORT);
        when(branchSearchIndexRepository.findAllByApprovalStatusAndUserTypeAndIsDeleted(any(ApprovalStatus.class), any(UserType.class),
                any(Pageable.class), anyBoolean())).thenReturn(getSampleBranchSearchIndexPage());
        assertEquals(BRANCH_ID, branchService.getPendingBranchList(UserType.MERCHANT, ALL, pageable).getContent().get(0)
                .getId());
    }

    @Test
    void Should_ReturnBranchSearchIndexPage_When_GettingPendingBranchListForSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.Direction.DESC, DEFAULT_SORT);
        when(branchSearchIndexRepository.getBranchListByApprovalStatusUserTypeAndBySearchTerm(any(ApprovalStatus.class), any(UserType.class),
                anyString(), any(Pageable.class), anyBoolean())).thenReturn(getSampleBranchSearchIndexPage());
        assertEquals(BRANCH_ID, branchService.getPendingBranchList(UserType.MERCHANT, SEARCH_TERM, pageable).getContent()
                .get(0).getId());
    }

    @Test
    void Should_ThrowKupiLocationException_When_GettingPendingBranchListIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.Direction.DESC, DEFAULT_SORT);
        when(branchSearchIndexRepository.getBranchListByApprovalStatusUserTypeAndBySearchTerm(any(ApprovalStatus.class), any(UserType.class),
                anyString(), any(Pageable.class), anyBoolean())).thenThrow(new DataAccessException(FAILED) {
        });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .getPendingBranchList(UserType.MERCHANT, SEARCH_TERM, pageable));
        assertEquals("Getting pending branch list from database was failed", exception.getMessage());
    }

    /**
     * approve or reject branch
     */
    @Test
    void Should_ApproveOrRejectBranchForApprovedStatusAndExistingBankSearchIndex() {
        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranch()));
        when(branchSearchIndexRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranchSearchIndex()));
        doNothing().when(mailService).sendMailToUser(anyString(), any(ApprovalStatusUpdateRequestDto.class), anyString());

        branchService.approveOrRejectBranch(getSampleApprovalStatusUpdateRequestDto(), AUTH_TOKEN);

        verify(approvedBranchRepository).save(any(ApprovedBranch.class));
        verify(branchSearchIndexRepository).save(any(BranchSearchIndex.class));
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void Should_ApproveOrRejectBranchForApprovedStatusAndNonExistingBankSearchIndex() {
        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranch()));
        when(branchSearchIndexRepository.findById(BRANCH_ID)).thenReturn(Optional.empty());
        doNothing().when(mailService).sendMailToUser(anyString(), any(ApprovalStatusUpdateRequestDto.class), anyString());

        branchService.approveOrRejectBranch(getSampleApprovalStatusUpdateRequestDto(), AUTH_TOKEN);

        verify(approvedBranchRepository).save(any(ApprovedBranch.class));
        verify(branchSearchIndexRepository).save(any(BranchSearchIndex.class));
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void Should_ApproveOrRejectBranchForPendingOrRejectedStatusAndExistingBankSearchIndex() {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = getSampleApprovalStatusUpdateRequestDto();
        approvalStatusUpdateRequestDto.setAction(ApprovalAction.REJECT);

        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranch()));
        when(branchSearchIndexRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranchSearchIndex()));
        doNothing().when(mailService).sendMailToUser(anyString(), any(ApprovalStatusUpdateRequestDto.class), anyString());

        branchService.approveOrRejectBranch(approvalStatusUpdateRequestDto, AUTH_TOKEN);

        verify(rejectedBranchRepository).save(any(RejectedBranch.class));
        verify(branchSearchIndexRepository).save(any(BranchSearchIndex.class));
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void Should_ApproveOrRejectBranchForPendingOrRejectedStatusAndNonExistingBankSearchIndex() {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = getSampleApprovalStatusUpdateRequestDto();
        approvalStatusUpdateRequestDto.setAction(ApprovalAction.REJECT);

        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranch()));
        when(branchSearchIndexRepository.findById(BRANCH_ID)).thenReturn(Optional.empty());
        doNothing().when(mailService).sendMailToUser(anyString(), any(ApprovalStatusUpdateRequestDto.class), anyString());

        branchService.approveOrRejectBranch(approvalStatusUpdateRequestDto, AUTH_TOKEN);

        verify(rejectedBranchRepository).save(any(RejectedBranch.class));
        verify(branchSearchIndexRepository).save(any(BranchSearchIndex.class));
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void Should_ThrowInvalidBranchException_When_ApproveOrRejectBranchForInvalidBranchId() {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = getSampleApprovalStatusUpdateRequestDto();

        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.empty());
        InvalidBranchException exception = assertThrows(InvalidBranchException.class, () -> branchService
                .approveOrRejectBranch(approvalStatusUpdateRequestDto, AUTH_TOKEN));
        assertEquals("Invalid branchId: " + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_ApproveOrRejectBranchForGettingBranchFromDBIsFailed() {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = getSampleApprovalStatusUpdateRequestDto();

        when(branchRepository.findById(BRANCH_ID)).thenThrow(new DataAccessException(FAILED) {
        });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .approveOrRejectBranch(approvalStatusUpdateRequestDto, AUTH_TOKEN));
        assertEquals("Getting branch from database was failed for branchId: " + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_ApproveOrRejectBranchForUpdatingBranchSearchIndexIsFailed() {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = getSampleApprovalStatusUpdateRequestDto();

        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranch()));
        when(branchSearchIndexRepository.findById(BRANCH_ID)).thenThrow(new DataAccessException(FAILED) {
        });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .approveOrRejectBranch(approvalStatusUpdateRequestDto, AUTH_TOKEN));
        assertEquals("Updating branch search index was failed for branchId: " + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_ApproveOrRejectBranchIsFailed() {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = getSampleApprovalStatusUpdateRequestDto();

        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranch()));
        when(branchSearchIndexRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranchSearchIndex()));

        branchService.approveOrRejectBranch(getSampleApprovalStatusUpdateRequestDto(), AUTH_TOKEN);

        verify(approvedBranchRepository).save(any(ApprovedBranch.class));
        verify(branchSearchIndexRepository).save(any(BranchSearchIndex.class));
        when(branchRepository.save(any(Branch.class))).thenThrow(new DataAccessException(FAILED) {
        });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .approveOrRejectBranch(approvalStatusUpdateRequestDto, AUTH_TOKEN));
        assertEquals("Saving branch update to database was failed for branchId: " + BRANCH_ID + ", action: " +
                ApprovalAction.APPROVE, exception.getMessage());
    }

    /**
     * Verify address or contactNo
     */
    @Test
    void Should_VerifyAddressOrContactNoForContactVerificationTypeForExistingBranchSearchIndex() {
        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranch()));
        when(branchSearchIndexRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranchSearchIndex()));
        when(approvedBranchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleApprovedBranch()));

        branchService.verifyAddressOrContactNo(getSampleVerifyAddressContactRequestDto(), VerificationType.CONTACT);

        verify(branchSearchIndexRepository).save(any(BranchSearchIndex.class));
        verify(approvedBranchRepository).save(any(ApprovedBranch.class));
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void Should_VerifyAddressOrContactNoForAddressVerificationTypeForExistingBranchSearchIndex() {
        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranch()));
        when(branchSearchIndexRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranchSearchIndex()));
        when(approvedBranchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleApprovedBranch()));

        branchService.verifyAddressOrContactNo(getSampleVerifyAddressContactRequestDto(), VerificationType.ADDRESS);

        verify(branchSearchIndexRepository).save(any(BranchSearchIndex.class));
        verify(approvedBranchRepository).save(any(ApprovedBranch.class));
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void Should_VerifyAddressOrContactNoForNonExistingBranchSearchIndex() {
        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranch()));
        when(branchSearchIndexRepository.findById(BRANCH_ID)).thenReturn(Optional.empty());
        when(approvedBranchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleApprovedBranch()));

        branchService.verifyAddressOrContactNo(getSampleVerifyAddressContactRequestDto(), VerificationType.CONTACT);

        verify(branchSearchIndexRepository).save(any(BranchSearchIndex.class));
        verify(approvedBranchRepository).save(any(ApprovedBranch.class));
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void Should_ThrowInvalidBranchException_When_VerifyAddressOrContactNoForInvalidBranchId() {
        VerifyAddressContactRequestDto verifyAddressContactRequestDto = getSampleVerifyAddressContactRequestDto();

        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.empty());
        InvalidBranchException exception = assertThrows(InvalidBranchException.class, () -> branchService
                .verifyAddressOrContactNo(verifyAddressContactRequestDto, VerificationType.CONTACT));
        assertEquals("Invalid branchId: " + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_VerifyAddressOrContactNoForGettingBranchFromDBIsFailed() {
        VerifyAddressContactRequestDto verifyAddressContactRequestDto = getSampleVerifyAddressContactRequestDto();

        when(branchRepository.findById(BRANCH_ID)).thenThrow(new DataAccessException(FAILED) {
        });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .verifyAddressOrContactNo(verifyAddressContactRequestDto, VerificationType.CONTACT));
        assertEquals("Getting branch from database was failed for branchId: " + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_VerifyAddressOrContactNoForUpdatingBranchSearchIndexIsFailed() {
        VerifyAddressContactRequestDto verifyAddressContactRequestDto = getSampleVerifyAddressContactRequestDto();

        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranch()));
        when(branchSearchIndexRepository.findById(BRANCH_ID)).thenThrow(new DataAccessException(FAILED) {
        });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .verifyAddressOrContactNo(verifyAddressContactRequestDto, VerificationType.CONTACT));
        assertEquals("Updating branch search index was failed for branchId: " + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_VerifyAddressOrContactNoForUpdatingApprovedBranchIsFailed() {
        VerifyAddressContactRequestDto verifyAddressContactRequestDto = getSampleVerifyAddressContactRequestDto();

        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranch()));
        when(branchSearchIndexRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranchSearchIndex()));
        when(approvedBranchRepository.findById(BRANCH_ID)).thenThrow(new DataAccessException(FAILED) {
        });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .verifyAddressOrContactNo(verifyAddressContactRequestDto, VerificationType.CONTACT));
        assertEquals("Updating approved branch was failed for branchId: " + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_VerifyAddressOrContactNoIsFailed() {
        VerifyAddressContactRequestDto verifyAddressContactRequestDto = getSampleVerifyAddressContactRequestDto();

        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranch()));
        when(branchSearchIndexRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranchSearchIndex()));
        when(approvedBranchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleApprovedBranch()));

        branchService.verifyAddressOrContactNo(getSampleVerifyAddressContactRequestDto(), VerificationType.CONTACT);

        verify(branchSearchIndexRepository).save(any(BranchSearchIndex.class));
        verify(approvedBranchRepository).save(any(ApprovedBranch.class));
        when(branchRepository.save(any(Branch.class))).thenThrow(new DataAccessException(FAILED) {
        });

        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .verifyAddressOrContactNo(verifyAddressContactRequestDto, VerificationType.CONTACT));
        assertEquals("Updating verification status for address/ contact number was failed" + " for branchId:" +
                BRANCH_ID + ", action: " + VerificationStatus.VERIFIED, exception.getMessage());
    }

    /**
     * Create branch
     */
    @Test
    void Should_CreateBranch() {
        when(branchRepository.existsByNameAndUserIdAndIdNotAndIsDeletedFalse(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(branchRepository.existsByAddressAndIdNotAndIsDeletedFalse(anyString(), anyString())).thenReturn(false);
        when(authUserService.getBulkBusinessResponse(anyString(), any(BulkBusinessProfileRequestDto.class),
                any(UserType.class))).thenReturn(getSampleBulkBusinessResponseDto());
        when(branchRepository.save(any(Branch.class))).thenReturn(getSampleBranch());
        when(branchSearchIndexRepository.save(any(BranchSearchIndex.class))).thenReturn(getSampleBranchSearchIndex());

        branchService.createBranch(getSampleBranch(), UserType.MERCHANT);

        verify(branchRepository).save(any(Branch.class));
        verify(branchSearchIndexRepository).save(any(BranchSearchIndex.class));
    }

    @Test
    void Should_ThrowKupiLocationException_When_CreatingBranchForExistingBranchNameCheckingIsFailed() {
        Branch branch = getSampleBranch();
        when(branchRepository.existsByNameAndUserIdAndIdNotAndIsDeletedFalse(anyString(), anyString(), anyString()))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .createBranch(branch, UserType.MERCHANT));
        assertEquals("Checking branch name is exists from the database was failed for userId:" + USER_ID +
                "branchId:" + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_CreatingBranchForExistingBranchAddressCheckingIsFailed() {
        Branch branch = getSampleBranch();
        when(branchRepository.existsByNameAndUserIdAndIdNotAndIsDeletedFalse(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(branchRepository.existsByAddressAndIdNotAndIsDeletedFalse(anyString(), anyString()))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .createBranch(branch, UserType.MERCHANT));
        assertEquals("Checking branch address is exists from the database was failed for userId:" + USER_ID +
                "branchId:" + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowExistingNameException_When_CreatingBranchForExistingBranchName() {
        Branch branch = getSampleBranch();
        when(branchRepository.existsByNameAndUserIdAndIdNotAndIsDeletedFalse(anyString(), anyString(), anyString()))
                .thenReturn(true);
        ExistingNameException exception = assertThrows(ExistingNameException.class, () -> branchService
                .createBranch(branch, UserType.MERCHANT));
        assertEquals("Branch name exists in the database for userId:" + USER_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowExistingAddressException_When_CreatingBranchForExistingBranchAddress() {
        Branch branch = getSampleBranch();
        when(branchRepository.existsByNameAndUserIdAndIdNotAndIsDeletedFalse(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(branchRepository.existsByAddressAndIdNotAndIsDeletedFalse(anyString(), anyString()))
                .thenReturn(true);
        ExistingAddressException exception = assertThrows(ExistingAddressException.class, () -> branchService
                .createBranch(branch, UserType.MERCHANT));
        assertEquals("Branch address exists in the database for userId:" + USER_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidUserException_When_CreatingBranchForBusinessProfileNotFound() {
        Branch branch = getSampleBranch();
        BulkBusinessResponseDto bulkBusinessResponseDto = getSampleBulkBusinessResponseDto();
        bulkBusinessResponseDto.setMerchants(new ArrayList<>());

        when(branchRepository.existsByNameAndUserIdAndIdNotAndIsDeletedFalse(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(branchRepository.existsByAddressAndIdNotAndIsDeletedFalse(anyString(), anyString())).thenReturn(false);
        when(authUserService.getBulkBusinessResponse(anyString(), any(BulkBusinessProfileRequestDto.class),
                any(UserType.class))).thenReturn(bulkBusinessResponseDto);

        InvalidUserException exception = assertThrows(InvalidUserException.class, () -> branchService
                .createBranch(branch, UserType.MERCHANT));
        assertEquals("Business profile not found for id: " + USER_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_CreatingBranchIsFailed() {
        Branch branch = getSampleBranch();
        when(branchRepository.existsByNameAndUserIdAndIdNotAndIsDeletedFalse(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(branchRepository.existsByAddressAndIdNotAndIsDeletedFalse(anyString(), anyString())).thenReturn(false);
        when(authUserService.getBulkBusinessResponse(anyString(), any(BulkBusinessProfileRequestDto.class),
                any(UserType.class))).thenReturn(getSampleBulkBusinessResponseDto());
        when(branchRepository.save(any(Branch.class))).thenThrow(new DataAccessException(FAILED) {
        });

        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .createBranch(branch, UserType.MERCHANT));
        assertEquals("Saving branch into database was failed for userId:" + USER_ID, exception.getMessage());
    }

    /**
     * Update branch
     */
    @Test
    void Should_ReturnBranch_When_UpdatingBranchIsSuccessful() {
        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(getSampleBranch()));
        when(branchRepository.existsByNameAndUserIdAndIdNotAndIsDeletedFalse(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(branchRepository.existsByAddressAndIdNotAndIsDeletedFalse(anyString(), anyString())).thenReturn(false);
        when(authUserService.getBulkBusinessResponse(anyString(), any(BulkBusinessProfileRequestDto.class),
                any(UserType.class))).thenReturn(getSampleBulkBusinessResponseDto());
        when(branchSearchIndexRepository.save(any(BranchSearchIndex.class))).thenReturn(getSampleBranchSearchIndex());
        when(branchRepository.save(any(Branch.class))).thenReturn(getSampleBranch());

        assertEquals(BRANCH_ID, branchService.updateBranch(getSampleBranchUpdateRequestDto()).getId());
        verify(branchSearchIndexRepository).save(any(BranchSearchIndex.class));
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void Should_ThrowInvalidBranchException_When_UpdatingBranchForInvalidBranchId() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto();
        when(branchRepository.findById(anyString())).thenReturn(Optional.empty());
        InvalidBranchException exception = assertThrows(InvalidBranchException.class, () -> branchService
                .updateBranch(branchUpdateRequestDto));
        assertEquals("Invalid branchId: " + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_UpdatingBranchForGettingBranchFromDBIsFailed() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto();
        when(branchRepository.findById(anyString())).thenThrow(new DataAccessException(FAILED) {
        });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .updateBranch(branchUpdateRequestDto));
        assertEquals("Getting branch from database was failed for branchId: " + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_UpdatingBranchForExistingBranchNameCheckIsFailed() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto();
        when(branchRepository.findById(anyString())).thenReturn(Optional.of(getSampleBranch()));
        when(branchRepository.existsByNameAndUserIdAndIdNotAndIsDeletedFalse(anyString(), anyString(), anyString()))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .updateBranch(branchUpdateRequestDto));
        assertEquals("Checking branch name is exists from the database was failed for userId:" + USER_ID +
                "branchId:" + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_UpdatingBranchForExistingBranchAddressCheckIsFailed() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto();
        when(branchRepository.findById(anyString())).thenReturn(Optional.of(getSampleBranch()));
        when(branchRepository.existsByNameAndUserIdAndIdNotAndIsDeletedFalse(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(branchRepository.existsByAddressAndIdNotAndIsDeletedFalse(anyString(), anyString()))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .updateBranch(branchUpdateRequestDto));
        assertEquals("Checking branch address is exists from the database was failed for userId:" + USER_ID +
                "branchId:" + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowExistingNameException_When_UpdatingBranchForExistingBranchName() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto();
        when(branchRepository.findById(anyString())).thenReturn(Optional.of(getSampleBranch()));
        when(branchRepository.existsByNameAndUserIdAndIdNotAndIsDeletedFalse(anyString(), anyString(), anyString()))
                .thenReturn(true);
        ExistingNameException exception = assertThrows(ExistingNameException.class, () -> branchService
                .updateBranch(branchUpdateRequestDto));
        assertEquals("Branch name exists in the database for userId:" + USER_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowExistingAddressException_When_UpdatingBranchForExistingBranchAddress() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto();
        when(branchRepository.findById(anyString())).thenReturn(Optional.of(getSampleBranch()));
        when(branchRepository.existsByNameAndUserIdAndIdNotAndIsDeletedFalse(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(branchRepository.existsByAddressAndIdNotAndIsDeletedFalse(anyString(), anyString()))
                .thenReturn(true);
        ExistingAddressException exception = assertThrows(ExistingAddressException.class, () -> branchService
                .updateBranch(branchUpdateRequestDto));
        assertEquals("Branch address exists in the database for userId:" + USER_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidUserException_When_UpdatingBranchForBusinessProfileNotFound() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto();
        BulkBusinessResponseDto bulkBusinessResponseDto = getSampleBulkBusinessResponseDto();
        bulkBusinessResponseDto.setMerchants(new ArrayList<>());

        when(branchRepository.findById(anyString())).thenReturn(Optional.of(getSampleBranch()));
        when(branchRepository.existsByNameAndUserIdAndIdNotAndIsDeletedFalse(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(branchRepository.existsByAddressAndIdNotAndIsDeletedFalse(anyString(), anyString()))
                .thenReturn(false);
        when(authUserService.getBulkBusinessResponse(anyString(), any(BulkBusinessProfileRequestDto.class),
                any(UserType.class))).thenReturn(bulkBusinessResponseDto);
        InvalidUserException exception = assertThrows(InvalidUserException.class, () -> branchService
                .updateBranch(branchUpdateRequestDto));
        assertEquals("Business profile not found for id: " + USER_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_UpdatingBranchIsFailed() {
        BranchUpdateRequestDto branchUpdateRequestDto = getSampleBranchUpdateRequestDto();
        when(branchRepository.findById(anyString())).thenReturn(Optional.of(getSampleBranch()));
        when(branchRepository.existsByNameAndUserIdAndIdNotAndIsDeletedFalse(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(branchRepository.existsByAddressAndIdNotAndIsDeletedFalse(anyString(), anyString()))
                .thenReturn(false);
        when(authUserService.getBulkBusinessResponse(anyString(), any(BulkBusinessProfileRequestDto.class),
                any(UserType.class))).thenReturn(getSampleBulkBusinessResponseDto());
        when(branchSearchIndexRepository.save(any(BranchSearchIndex.class))).thenReturn(getSampleBranchSearchIndex());

        branchService.updateBranch(getSampleBranchUpdateRequestDto());

        when(branchRepository.save(any(Branch.class))).thenThrow(new DataAccessException(FAILED) {
        });

        verify(branchSearchIndexRepository).save(any(BranchSearchIndex.class));
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .updateBranch(branchUpdateRequestDto));
        assertEquals("Updating branch from the database was failed for branchId: " + BRANCH_ID,
                exception.getMessage());
    }

    /**
     * Get approved branch list by userId
     */
    @Test
    void Should_ReturnApprovedBranchPage_When_GettingApprovedBranchListByUserIdForAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(SORT_NAME).ascending());

        when(approvedBranchRepository.findByUserId(anyString(), any(Pageable.class)))
                .thenReturn(getSampleApprovedBranchPage());
        assertEquals(BRANCH_ID, branchService.getApprovedBranchListByUserId(USER_ID, ALL, pageable).getContent().get(0)
                .getId());
    }

    @Test
    void Should_ReturnApprovedBranchPage_When_GettingApprovedBranchListByUserIdForSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(SORT_NAME).ascending());

        when(approvedBranchRepository.getBranchesByUserIdAndSearchTerm(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(getSampleApprovedBranchPage());
        assertEquals(BRANCH_ID, branchService.getApprovedBranchListByUserId(USER_ID, SEARCH_TERM, pageable).getContent()
                .get(0).getId());
    }

    @Test
    void Should_ThrowKupiLocationException_When_GettingApprovedBranchListByUserIdIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(SORT_NAME).ascending());

        when(approvedBranchRepository.findByUserId(anyString(), any(Pageable.class)))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .getApprovedBranchListByUserId(USER_ID, ALL, pageable));
        assertEquals("Getting approved branch list was failed for userId: " + USER_ID, exception.getMessage());
    }

    /**
     * Get branch detail by id
     */
    @Test
    void Should_ReturnDetailBranchResponseDto_When_GettingBranchDetailByIdForPendingOrApprovedBranches() {
        Branch branch = getSampleBranch();
        branch.setApprovalStatus(ApprovalStatus.APPROVED);

        when(branchRepository.findById(anyString())).thenReturn(Optional.of(branch));
        assertEquals(BRANCH_ID, branchService.getBranchDetailById(BRANCH_ID).getId());
    }

    @Test
    void Should_ReturnDetailBranchResponseDto_When_GettingBranchDetailByIdForRejectedBranches() {
        Branch branch = getSampleBranch();
        branch.setApprovalStatus(ApprovalStatus.REJECTED);

        when(branchRepository.findById(anyString())).thenReturn(Optional.of(branch));
        when(rejectedBranchRepository.findById(anyString())).thenReturn(Optional.of(getSampleRejectedBranch()));
        assertEquals(BRANCH_ID, branchService.getBranchDetailById(BRANCH_ID).getId());
        assertEquals(COMMENT, branchService.getBranchDetailById(BRANCH_ID).getComment());
    }

    @Test
    void Should_ThrowInvalidBranchException_When_GettingBranchDetailByIdForInvalidBranchId() {
        when(branchRepository.findById(anyString())).thenReturn(Optional.empty());
        InvalidBranchException exception = assertThrows(InvalidBranchException.class, () -> branchService
                .getBranchDetailById(BRANCH_ID));
        assertEquals("Invalid branchId: " + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_GettingBranchDetailByIdForGettingBranchFromDBIsFailed() {
        when(branchRepository.findById(anyString())).thenThrow(new DataAccessException(FAILED) {
        });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .getBranchDetailById(BRANCH_ID));
        assertEquals("Getting branch from database was failed for branchId: " + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidBranchException_When_GettingBranchDetailByIdForInvalidRejectedBranchId() {
        Branch branch = getSampleBranch();
        branch.setApprovalStatus(ApprovalStatus.REJECTED);

        when(branchRepository.findById(anyString())).thenReturn(Optional.of(branch));
        when(rejectedBranchRepository.findById(anyString())).thenReturn(Optional.empty());
        InvalidBranchException exception = assertThrows(InvalidBranchException.class, () -> branchService
                .getBranchDetailById(BRANCH_ID));
        assertEquals("Invalid rejected branchId: " + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_GettingBranchDetailByIdForGettingRejectedBranchFromDBIsFailed() {
        Branch branch = getSampleBranch();
        branch.setApprovalStatus(ApprovalStatus.REJECTED);

        when(branchRepository.findById(anyString())).thenReturn(Optional.of(branch));
        when(rejectedBranchRepository.findById(anyString())).thenThrow(new DataAccessException(FAILED) {
        });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .getBranchDetailById(BRANCH_ID));
        assertEquals("Getting rejected branch from database was failed for branchId: " + BRANCH_ID, exception.getMessage());
    }

    /**
     * Detele branch
     */
    @Test
    void Should_DeleteBranch() {
        when(branchRepository.findById(anyString())).thenReturn(Optional.of(getSampleBranch()));
        when(branchRepository.save(any(Branch.class))).thenReturn(getSampleBranch());

        branchService.deteleBranch(BRANCH_ID);

        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void Should_ThrowInvalidBranchException_When_DeletingBranchForInvalidBranchId() {
        when(branchRepository.findById(anyString())).thenReturn(Optional.empty());
        InvalidBranchException exception = assertThrows(InvalidBranchException.class, () -> branchService
                .deteleBranch(BRANCH_ID));
        assertEquals("Invalid branchId: " + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_DeletingBranchForGettingBranchFromDBIsFailed() {
        when(branchRepository.findById(anyString())).thenThrow(new DataAccessException(FAILED) {
        });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .deteleBranch(BRANCH_ID));
        assertEquals("Getting branch from database was failed for branchId: " + BRANCH_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_DeletingBranchIsFailed() {
        when(branchRepository.findById(anyString())).thenReturn(Optional.of(getSampleBranch()));
        when(branchRepository.save(any(Branch.class))).thenThrow(new DataAccessException(FAILED) {
        });
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> branchService
                .deteleBranch(BRANCH_ID));
        assertEquals("Deleting branch from the database was failed for branchId: " + BRANCH_ID,
                exception.getMessage());
    }

    private Page<Branch> getSampleBranchPage() {
        List<Branch> branchList = new ArrayList<>();
        branchList.add(getSampleBranch());
        return new PageImpl<>(branchList);
    }

    private Branch getSampleBranch() {
        Branch branch = new Branch();
        branch.setName(BRANCH_NAME);
        branch.setId(BRANCH_ID);
        branch.setUserId(USER_ID);
        branch.setAddress(BRANCH_ADDRESS);
        branch.setUserType(UserType.MERCHANT);
        return branch;
    }

    private Page<BranchSearchIndex> getSampleBranchSearchIndexPage() {
        List<BranchSearchIndex> branchSearchIndexList = new ArrayList<>();
        branchSearchIndexList.add(getSampleBranchSearchIndex());
        return new PageImpl<>(branchSearchIndexList);
    }

    private BranchSearchIndex getSampleBranchSearchIndex() {
        BranchSearchIndex branchSearchIndex = new BranchSearchIndex();
        branchSearchIndex.setId(BRANCH_ID);
        return branchSearchIndex;
    }

    private ApprovalStatusUpdateRequestDto getSampleApprovalStatusUpdateRequestDto() {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = new ApprovalStatusUpdateRequestDto();
        approvalStatusUpdateRequestDto.setId(BRANCH_ID);
        approvalStatusUpdateRequestDto.setAction(ApprovalAction.APPROVE);
        approvalStatusUpdateRequestDto.setComment(COMMENT);
        return approvalStatusUpdateRequestDto;
    }

    private VerifyAddressContactRequestDto getSampleVerifyAddressContactRequestDto() {
        VerifyAddressContactRequestDto verifyAddressContactRequestDto = new VerifyAddressContactRequestDto();
        verifyAddressContactRequestDto.setId(BRANCH_ID);
        verifyAddressContactRequestDto.setAction(VerificationStatus.VERIFIED);
        return verifyAddressContactRequestDto;
    }

    private ApprovedBranch getSampleApprovedBranch() {
        ApprovedBranch approvedBranch = new ApprovedBranch();
        approvedBranch.setId(BRANCH_ID);
        return approvedBranch;
    }

    private BulkBusinessResponseDto getSampleBulkBusinessResponseDto() {
        BusinessResponseDto businessResponseDto = new BusinessResponseDto();
        businessResponseDto.setId(BRANCH_ID);
        List<BusinessResponseDto> businessResponseDtoList = new ArrayList<>();
        businessResponseDtoList.add(businessResponseDto);

        BulkBusinessResponseDto bulkBusinessResponseDto = new BulkBusinessResponseDto();
        bulkBusinessResponseDto.setMerchants(businessResponseDtoList);
        return bulkBusinessResponseDto;
    }

    private BranchUpdateRequestDto getSampleBranchUpdateRequestDto() {
        LocationRequestDto locationRequestDto = new LocationRequestDto();
        locationRequestDto.setCity(new LongShortNameRequestDto());
        locationRequestDto.setCountry(new LongShortNameRequestDto());
        BranchUpdateRequestDto branchUpdateRequestDto = new BranchUpdateRequestDto();
        branchUpdateRequestDto.setId(BRANCH_ID);
        branchUpdateRequestDto.setName(BRANCH_NAME);
        branchUpdateRequestDto.setUserId(USER_ID);
        AddressRequestDto addressRequestDto = new AddressRequestDto();
        addressRequestDto.setValue(BRANCH_ADDRESS);
        branchUpdateRequestDto.setAddress(addressRequestDto);
        branchUpdateRequestDto.setContactNo(new ContactNoRequestDto());
        branchUpdateRequestDto.setLocation(locationRequestDto);
        return branchUpdateRequestDto;
    }

    private Page<ApprovedBranch> getSampleApprovedBranchPage() {
        ApprovedBranch approvedBranch = new ApprovedBranch();
        approvedBranch.setId(BRANCH_ID);
        List<ApprovedBranch> approvedBranchList = new ArrayList<>();
        approvedBranchList.add(approvedBranch);
        return new PageImpl<>(approvedBranchList);
    }

    private RejectedBranch getSampleRejectedBranch() {
        RejectedBranch rejectedBranch = new RejectedBranch();
        rejectedBranch.setComment(COMMENT);
        return rejectedBranch;
    }
}