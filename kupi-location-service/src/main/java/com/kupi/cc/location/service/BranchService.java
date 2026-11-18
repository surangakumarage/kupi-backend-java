package com.kupi.cc.location.service;

import com.kupi.cc.location.domain.entity.*;
import com.kupi.cc.location.domain.request.ApprovalStatusUpdateRequestDto;
import com.kupi.cc.location.domain.request.BranchUpdateRequestDto;
import com.kupi.cc.location.domain.request.BulkBusinessProfileRequestDto;
import com.kupi.cc.location.domain.request.VerifyAddressContactRequestDto;
import com.kupi.cc.location.domain.response.BusinessResponseDto;
import com.kupi.cc.location.domain.response.DetailBranchResponseDto;
import com.kupi.cc.location.enums.*;
import com.kupi.cc.location.exception.*;
import com.kupi.cc.location.repository.ApprovedBranchRepository;
import com.kupi.cc.location.repository.BranchRepository;
import com.kupi.cc.location.repository.BranchSearchIndexRepository;
import com.kupi.cc.location.repository.RejectedBranchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Branch Service
 */
@Service
@Slf4j
public class BranchService {

    private static final String ALL = "ALL";
    private static final String BRANCH_ID = "branchId:";
    private static final String ADMIN = "Admin";
    private static final String CRON = "0 0 */1 * * *";
    private static final String TIME_ZONE = "GMT +05:30";
    private static final int ZERO = 0;
    private final BranchRepository branchRepository;
    private final BranchSearchIndexRepository branchSearchIndexRepository;
    private final ApprovedBranchRepository approvedBranchRepository;
    private final RejectedBranchRepository rejectedBranchRepository;
    private final AuthUserService authUserService;
    private final MailService mailService;


    @Autowired
    public BranchService(BranchRepository branchRepository, BranchSearchIndexRepository branchSearchIndexRepository,
                         ApprovedBranchRepository approvedBranchRepository, AuthUserService authUserService,
                         RejectedBranchRepository rejectedBranchRepository, MailService mailService) {
        this.branchRepository = branchRepository;
        this.branchSearchIndexRepository = branchSearchIndexRepository;
        this.approvedBranchRepository = approvedBranchRepository;
        this.rejectedBranchRepository = rejectedBranchRepository;
        this.authUserService = authUserService;
        this.mailService = mailService;
    }

    /**
     * This scheduler method updates branch search index table.
     * Runs every hour.
     */
    @Scheduled(cron = CRON, zone = TIME_ZONE)
    private void updateBranchTables() {
        log.debug("Scheduled branch tables update started.");
        updateBusinessProfileInfoForBranches(UserType.MERCHANT);
        updateBusinessProfileInfoForBranches(UserType.BANK);
        log.debug("Scheduled branch tables update finished successfully.");
    }

    /**
     * This method updates business profile information for branches.
     *
     * @param userType MERCHANT/BANK
     */
    private void updateBusinessProfileInfoForBranches(UserType userType) {
        try {
            int totalPages = 1;
            Page<String> userIdPage;
            List<BusinessResponseDto> businessResponseDtoList;
            for (int page = 0; page < totalPages; page++) {
                Pageable pageable = PageRequest.of(page, 250);
                userIdPage = branchSearchIndexRepository.getDistinctUserIds(pageable, userType);
                BulkBusinessProfileRequestDto businessProfileRequestDto =
                        new BulkBusinessProfileRequestDto(userIdPage.toList());
                businessResponseDtoList = authUserService
                        .getBulkBusinessResponse(ADMIN, businessProfileRequestDto, userType).getMerchants();
                businessResponseDtoList.forEach(business ->
                        branchSearchIndexRepository.setBusinessProfileInfo(business.getName(), business.getImageUrl(),
                                business.getApprovalStatus(), business.getId()));
                totalPages = userIdPage.getTotalPages();
            }
        } catch (DataAccessException e) {
            log.error("Updating business profile information for branches was failed for userType: {}", userType, e);
        }
    }

    /**
     * This method saves new branch into database
     *
     * @param branch   branch
     * @param userType userType
     */
    public void createBranch(Branch branch, UserType userType) {
        try {
            boolean existsByName = isBranchNameExist(branch.getName(), branch.getId(),
                    branch.getUserId());
            boolean existsByAddress = isBranchAddressExist(branch.getAddress(), branch.getId(),
                    branch.getUserId());
            if (existsByName) {
                throw new ExistingNameException("Branch name exists in the database for userId:" + branch.getUserId());
            }
            if (existsByAddress) {
                throw new ExistingAddressException("Branch address exists in the database for userId:"
                        + branch.getUserId());
            }
            var branchSearchIndex = getBusinessProfile(branch, userType);
            branchRepository.save(branch);
            branchSearchIndexRepository.save(branchSearchIndex);
        } catch (DataAccessException e) {
            throw new KupiLocationException("Saving branch into database was failed for userId:" +
                    branch.getUserId(), e);
        }
    }


    /**
     * This method returns business profile.
     *
     * @param branch   branch
     * @param userType userType
     * @return BranchSearchIndex
     */
    private BranchSearchIndex getBusinessProfile(Branch branch, UserType userType) {
        List<String> userIdList = new ArrayList<>();
        userIdList.add(branch.getUserId());
        BulkBusinessProfileRequestDto bulkBusinessProfileRequestDto = new BulkBusinessProfileRequestDto(userIdList);
        var authUser = authUserService.getBulkBusinessResponse(branch.getUserId(),
                bulkBusinessProfileRequestDto, userType);
        if (authUser.getMerchants().isEmpty())
            throw new InvalidUserException("Business profile not found for id: " + branch.getUserId());
        return new BranchSearchIndex(branch, authUser.getMerchants().get(ZERO));
    }


    /**
     * This method update existing branch.
     *
     * @param branchUpdateRequestDto branchUpdateRequestDto
     * @return Branch
     */
    public Branch updateBranch(BranchUpdateRequestDto branchUpdateRequestDto) {
        try {
            var branch = getBranchById(branchUpdateRequestDto.getId());
            boolean existsByName = isBranchNameExist(branchUpdateRequestDto.getName(), branch.getId(),
                    branch.getUserId());
            boolean existsByAddress = isBranchAddressExist(branchUpdateRequestDto.getAddress().getValue(),
                    branch.getId(), branch.getUserId());
            if (existsByName) {
                throw new ExistingNameException("Branch name exists in the database for userId:" + branch.getUserId());
            }
            if (existsByAddress) {
                throw new ExistingAddressException("Branch address exists in the database for userId:"
                        + branch.getUserId());
            }
            branch.update(branchUpdateRequestDto);
            var branchSearchIndex = getBusinessProfile(branch, branch.getUserType());
            branchSearchIndex.update(branch);
            branchSearchIndexRepository.save(branchSearchIndex);
            return branchRepository.save(branch);
        } catch (DataAccessException e) {
            throw new KupiLocationException("Updating branch from the database was failed for branchId: "
                    + branchUpdateRequestDto.getId(), e);
        }
    }


    /**
     * This method checks duplication branch names.
     *
     * @param branchName branchName
     * @param userId     userId
     * @param branchId   branchId
     * @return true/false
     */
    private boolean isBranchNameExist(String branchName, String branchId, String userId) {
        try {
            return branchRepository.existsByNameAndUserIdAndIdNotAndIsDeletedFalse(branchName, userId, branchId);
        } catch (DataAccessException e) {
            throw new KupiLocationException("Checking branch name is exists from the database was failed for userId:"
                    + userId + BRANCH_ID + branchId, e);
        }
    }

    /**
     * This method checks duplication branch address.
     *
     * @param branchAddress branchAddress
     * @param branchId      branchId
     * @param userId        userId
     * @return true/false
     */
    private boolean isBranchAddressExist(String branchAddress, String branchId, String userId) {
        try {
            return branchRepository.existsByAddressAndIdNotAndIsDeletedFalse(branchAddress, branchId);
        } catch (DataAccessException e) {
            throw new KupiLocationException("Checking branch address is exists from the database was failed for userId:"
                    + userId + BRANCH_ID + branchId, e);
        }
    }

    /**
     * This method returns pending branch list.
     *
     * @param userType   userType
     * @param searchTerm searchTerm
     * @param pageable   pageable
     * @return pending branch page.
     */
    public Page<BranchSearchIndex> getPendingBranchList(UserType userType, String searchTerm, Pageable pageable) {
        try {
            if (searchTerm.equals(ALL))
                return branchSearchIndexRepository.findAllByApprovalStatusAndUserTypeAndIsDeleted(ApprovalStatus.PENDING,
                        userType, pageable, false);
            return branchSearchIndexRepository.getBranchListByApprovalStatusUserTypeAndBySearchTerm(ApprovalStatus.PENDING,
                    userType, searchTerm, pageable, false);
        } catch (DataAccessException e) {
            throw new KupiLocationException("Getting pending branch list from database was failed", e);
        }
    }

    /**
     * This method is used to approve/reject branch updates.
     *
     * @param approvalStatusUpdateRequestDto approvalStatusUpdateRequestDto
     */
    public void approveOrRejectBranch(ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto, String authToken) {
        try {
            Branch branch = getBranchById(approvalStatusUpdateRequestDto.getId());
            ApprovalStatus approvalStatus = approvalStatusUpdateRequestDto.getAction() == ApprovalAction.APPROVE ?
                    ApprovalStatus.APPROVED : ApprovalStatus.REJECTED;
            branch.setApprovalStatus(approvalStatus);
            branch.setUpdatedAt(System.currentTimeMillis());
            if (approvalStatus == ApprovalStatus.APPROVED)
                approvedBranchRepository.save(new ApprovedBranch(branch));
            else
                rejectedBranchRepository
                        .save(new RejectedBranch(branch, approvalStatusUpdateRequestDto.getComment()));
            updateBranchSearchIndex(branch);
            branchRepository.save(branch);
            mailService.sendMailToUser(branch.getUserId(), approvalStatusUpdateRequestDto, authToken);
        } catch (DataAccessException e) {
            throw new KupiLocationException("Saving branch update to database was failed for branchId: " +
                    approvalStatusUpdateRequestDto.getId() + ", action: " +
                    approvalStatusUpdateRequestDto.getAction(), e);
        }
    }


    /**
     * This method is used to verify branch address/contactNo updates.
     *
     * @param verifyAddressContactRequestDto verifyAddressContactRequestDto
     * @param verificationType               verificationType
     */
    public void verifyAddressOrContactNo(VerifyAddressContactRequestDto verifyAddressContactRequestDto,
                                         VerificationType verificationType) {
        try {
            Branch branch = getBranchById(verifyAddressContactRequestDto.getId());
            VerificationStatus verificationStatus = verifyAddressContactRequestDto.getAction();

            if (verificationType == VerificationType.ADDRESS) {
                branch.setAddressVerified(verificationStatus == VerificationStatus.VERIFIED);
            } else {
                branch.setContactNoVerified(verificationStatus == VerificationStatus.VERIFIED &&
                        verificationType == VerificationType.CONTACT);
            }
            branch.setUpdatedAt(System.currentTimeMillis());
            updateBranchSearchIndex(branch);
            updateApprovedBranch(branch);
            branchRepository.save(branch);
        } catch (DataAccessException e) {
            throw new KupiLocationException("Updating verification status for address/ contact number was failed" +
                    " for branchId:" + verifyAddressContactRequestDto.getId() + ", action: " +
                    verifyAddressContactRequestDto.getAction(), e);
        }
    }

    /**
     * This method updates approval branch.
     *
     * @param branch branch
     */
    private void updateApprovedBranch(Branch branch) {
        try {
            Optional<ApprovedBranch> optionalApprovedBranch = approvedBranchRepository.findById(branch.getId());
            if (optionalApprovedBranch.isPresent()) {
                optionalApprovedBranch.get().update(branch);
                approvedBranchRepository.save(optionalApprovedBranch.get());
            }
        } catch (DataAccessException e) {
            throw new KupiLocationException("Updating approved branch was failed for branchId: " + branch.getId(), e);
        }
    }

    /**
     * This method returns branch by id.
     *
     * @param branchId branchId
     * @return branch.
     */
    private Branch getBranchById(String branchId) {
        try {
            Optional<Branch> optionalBranch = branchRepository.findById(branchId);
            if (optionalBranch.isPresent())
                return optionalBranch.get();
            else
                throw new InvalidBranchException("Invalid branchId: " + branchId);
        } catch (DataAccessException e) {
            throw new KupiLocationException("Getting branch from database was failed for branchId: " + branchId, e);
        }
    }

    /**
     * This method returns comment for Id.
     *
     * @param branchId branchId
     * @return comment
     */
    public String getRejectedBranchById(String branchId) {
        try {
            var optionalRejectedBranch = rejectedBranchRepository.findById(branchId);
            if (optionalRejectedBranch.isPresent()) {
                return optionalRejectedBranch.get().getComment();
            } else
                throw new InvalidBranchException("Invalid rejected branchId: " + branchId);
        } catch (DataAccessException e) {
            throw new KupiLocationException("Getting rejected branch from database was failed for branchId: "
                    + branchId, e);
        }
    }

    /**
     * This method returns branch detail for branchId.
     *
     * @param branchId branchId
     * @return DetailBranchResponseDto
     */
    public DetailBranchResponseDto getBranchDetailById(String branchId) {
        Branch branch = getBranchById(branchId);
        DetailBranchResponseDto detailBranchResponseDto = new DetailBranchResponseDto(branch);
        if (branch.getApprovalStatus() == ApprovalStatus.REJECTED) {
            var comment = getRejectedBranchById(branchId);
            detailBranchResponseDto.setComment(comment);
        }
        return detailBranchResponseDto;
    }

    /**
     * This method updates branch search index.
     *
     * @param branch branch
     */
    private void updateBranchSearchIndex(Branch branch) {
        try {
            Optional<BranchSearchIndex> optionalBranchSearchIndex = branchSearchIndexRepository.findById(branch.getId());
            if (optionalBranchSearchIndex.isPresent()) {
                optionalBranchSearchIndex.get().update(branch);
                branchSearchIndexRepository.save(optionalBranchSearchIndex.get());
            } else
                branchSearchIndexRepository.save(new BranchSearchIndex(branch));
        } catch (DataAccessException e) {
            throw new KupiLocationException("Updating branch search index was failed for branchId: "
                    + branch.getId(), e);
        }
    }

    /**
     * This method returns approved branch list for userId.
     *
     * @param userId     userId
     * @param searchTerm searchTerm
     * @param pageable   pageable
     * @return approved branch page.
     */
    public Page<ApprovedBranch> getApprovedBranchListByUserId(String userId, String searchTerm, Pageable pageable) {
        try {
            if (searchTerm.equals(ALL))
                return approvedBranchRepository.findByUserId(userId, pageable);
            return approvedBranchRepository.getBranchesByUserIdAndSearchTerm(userId, searchTerm, pageable);
        } catch (DataAccessException e) {
            throw new KupiLocationException("Getting approved branch list was failed for userId: " + userId, e);
        }
    }

    /**
     * This method returns all branch list for userId.
     *
     * @param userId     userId
     * @param searchTerm searchTerm
     * @param pageable   pageable
     * @return branch page
     */
    public Page<Branch> getBranchListByUserId(String userId, String searchTerm, Pageable pageable) {
        try {
            if (searchTerm.equals(ALL))
                return branchRepository.findByUserId(userId, pageable);
            return branchRepository.getBranchesByUserIdAndSearchTerm(userId, searchTerm, pageable);
        } catch (DataAccessException e) {
            throw new KupiLocationException("Getting branch list was failed for userId: " + userId, e);
        }
    }

    /**
     * This method update branch deleted flag
     *
     * @param branchId branchId
     */
    public void deteleBranch(String branchId) {
        try {
            var branch = getBranchById(branchId);
            branch.setDeleted(true);
            branch.setUpdatedAt(System.currentTimeMillis());
            branchRepository.save(branch);
            branchSearchIndexRepository.save(new BranchSearchIndex(branch));
        } catch (DataAccessException e) {
            throw new KupiLocationException("Deleting branch from the database was failed for branchId: "
                    + branchId, e);
        }
    }

    /**
     * This method returns Approved merchant's geoLocation page by geoCode.
     *
     * @param pageable pageable
     * @param geoCode  geoCode
     * @return GeoLocation Page
     */
    public Page<GeoLocation> getMerchantIdListByGeoCode(Pageable pageable, String geoCode) {
        try {
            return branchRepository
                    .getDistinctByUserIdAndUserTypeAndGlobalCode(pageable, UserType.MERCHANT, ApprovalStatus.APPROVED,
                            geoCode);
        } catch (DataAccessException e) {
            throw new KupiLocationException("Getting branch list by geo code " + geoCode + " was failed.", e);
        }
    }


    public Page<String> getPreferredMerchantIdListByLocation(Pageable pageable,
                                                             double latitude,
                                                             double longitude,
                                                             double radiusKm,
                                                             List<String> preferredMerchantIds) {
        try {
            double radiusMeters = radiusKm * 1000;
            return branchRepository.getPreferredMerchantIdListByLocation(
                    pageable,
                    latitude,
                    longitude,
                    radiusMeters,
                    UserType.MERCHANT.name(),
                    ApprovalStatus.APPROVED.name(),
                    preferredMerchantIds
            );
        } catch (DataAccessException e) {
            throw new KupiLocationException("Getting preferred nearby merchant list failed for (" +
                    latitude + "," + longitude + ")", e);
        }
    }

    /**
     * This method returns specific merchant IDs by location for newly added preferred merchants.
     * Only searches for the specified merchant IDs within the radius.
     *
     * @param pageable pageable
     * @param latitude latitude
     * @param longitude longitude
     * @param radiusKm radius in kilometers
     * @param specificMerchantIds list of specific merchant IDs to search for
     * @return Page of merchant IDs
     */
    public Page<String> getSpecificMerchantIdListByLocation(Pageable pageable,
                                                           double latitude,
                                                           double longitude,
                                                           double radiusKm,
                                                           List<String> specificMerchantIds) {
        try {
            double radiusMeters = radiusKm * 1000;
            return branchRepository.getSpecificMerchantIdListByLocation(
                    pageable,
                    latitude,
                    longitude,
                    radiusMeters,
                    UserType.MERCHANT.name(),
                    ApprovalStatus.APPROVED.name(),
                    specificMerchantIds
            );
        } catch (DataAccessException e) {
            throw new KupiLocationException("Getting specific nearby merchant list failed for (" +
                    latitude + "," + longitude + ") with merchants: " + specificMerchantIds, e);
        }
    }

    /**
     * This method returns all merchant IDs within the specified radius.
     * Unlike getPreferredMerchantIdListByLocation, this doesn't filter by preferred merchants.
     *
     * @param pageable pageable
     * @param latitude latitude
     * @param longitude longitude
     * @param radiusKm radius in kilometers
     * @return Page of merchant IDs
     */
    public Page<String> getAllMerchantIdListByLocation(Pageable pageable,
                                                      double latitude,
                                                      double longitude,
                                                      double radiusKm) {
        try {
            double radiusMeters = radiusKm * 1000;
            return branchRepository.getAllMerchantIdListByLocation(
                    pageable,
                    latitude,
                    longitude,
                    radiusMeters,
                    UserType.MERCHANT.name(),
                    ApprovalStatus.APPROVED.name()
            );
        } catch (DataAccessException e) {
            throw new KupiLocationException("Getting all nearby merchant list failed for (" +
                    latitude + "," + longitude + ")", e);
        }
    }

    /**
     * This method returns branches for given merchant IDs within the specified radius.
     *
     * @param latitude latitude
     * @param longitude longitude
     * @param radiusKm radius in kilometers
     * @param merchantIds list of merchant IDs
     * @return List of branches
     */
    public List<Branch> getBranchesByMerchantIdsAndLocation(double latitude,
                                                            double longitude,
                                                            double radiusKm,
                                                            List<String> merchantIds) {
        try {
            if (merchantIds == null || merchantIds.isEmpty()) {
                return new ArrayList<>();
            }
            double radiusMeters = radiusKm * 1000;
            return branchRepository.getBranchesByMerchantIdsAndLocation(
                    latitude,
                    longitude,
                    radiusMeters,
                    UserType.MERCHANT.name(),
                    ApprovalStatus.APPROVED.name(),
                    merchantIds
            );
        } catch (DataAccessException e) {
            throw new KupiLocationException("Getting branches by merchant IDs and location failed for (" +
                    latitude + "," + longitude + ") with merchants: " + merchantIds, e);
        }
    }

}