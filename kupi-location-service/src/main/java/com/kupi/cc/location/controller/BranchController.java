package com.kupi.cc.location.controller;

import com.kupi.cc.location.configuration.Translator;
import com.kupi.cc.location.domain.entity.ApprovedBranch;
import com.kupi.cc.location.domain.entity.Branch;
import com.kupi.cc.location.domain.entity.BranchSearchIndex;
import com.kupi.cc.location.domain.request.ApprovalStatusUpdateRequestDto;
import com.kupi.cc.location.domain.request.BranchRequestDto;
import com.kupi.cc.location.domain.request.BranchUpdateRequestDto;
import com.kupi.cc.location.domain.request.VerifyAddressContactRequestDto;
import com.kupi.cc.location.domain.response.*;
import com.kupi.cc.location.enums.*;
import com.kupi.cc.location.exception.*;
import com.kupi.cc.location.service.BranchService;
import com.kupi.cc.location.util.Validator;
import com.kupi.cc.location.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * Branch Controller
 */
@CrossOrigin
@Validated
@RestController
@Slf4j
@RequestMapping("/api/v1/branch")
public class BranchController extends Controller {

    private static final String SORT_UPDATED_AT = "updatedAt";
    private static final String SORT_NAME = "name";
    private final BranchService branchService;
    private final Validator validator;

    @Autowired
    public BranchController(Translator translator, BranchService branchService, Validator validator) {
        super(translator);
        this.branchService = branchService;
        this.validator = validator;
    }

    /**
     * This method get paginated branch list
     *
     * @param userId userId
     * @param page   page
     * @param size   size
     * @return branch list
     */
    @GetMapping("/{userId}/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getBranchList(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                         @Min(DEFAULT_PAGE) @PathVariable Integer page,
                                                         @Positive @Max(PAGE_MAX_SIZE) @PathVariable Integer size,
                                                         @PathVariable String searchTerm) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            Page<Branch> branchPage = branchService.getBranchListByUserId(userId, searchTerm, pageable);
            var branchSuccessDto = new BranchesListResponseDto(branchPage);
            log.debug("Successfully returned all branch list for page: {}, size {} for userId: {}",
                    page, size, userId);
            return getSuccessResponse(SuccessResponseStatusType.READ_BRANCH_LIST, branchSuccessDto);
        } catch (KupiLocationException e) {
            log.error("Returning all branch list was failed for page: {}, size {} for userId: {}", page, size, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns pending branch list.
     *
     * @param userType   MERCHANT/BANK
     * @param page       page
     * @param size       size
     * @param searchTerm searchTerm
     * @return pending branch list.
     */
    @GetMapping(path = "/{userType}/pending/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getPendingBranchList(@PathVariable UserType userType,
                                                                @Min(DEFAULT_PAGE) @PathVariable Integer page,
                                                                @Min(DEFAULT_PAGE) @Max(PAGE_MAX_SIZE)
                                                                @Positive @PathVariable Integer size,
                                                                @PathVariable String searchTerm) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, SORT_UPDATED_AT);
            Page<BranchSearchIndex> branchPage = branchService.getPendingBranchList(userType, searchTerm, pageable);
            PendingBranchPageResponseDto responseDto = new PendingBranchPageResponseDto(branchPage);
            log.debug("Successfully returned pending branch list. Page: {}, Size: {}, SearchTerm: {}",
                    page, size, searchTerm);
            return getSuccessResponse(SuccessResponseStatusType.BRANCH_LIST, responseDto);
        } catch (KupiLocationException e) {
            log.error("Getting pending branch list was failed. Page: {}, Size: {}, SearchTerm: {}",
                    page, size, searchTerm, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to approve/reject branch updates.
     *
     * @param approvalStatusUpdateRequestDto approvalStatusUpdateRequestDto
     * @return success/error response.
     */
    @PutMapping("/approve")
    public ResponseEntity<ResponseWrapper> approveOrRejectBranch(@RequestBody ApprovalStatusUpdateRequestDto
                                                                         approvalStatusUpdateRequestDto,
                                                                 HttpServletRequest request) {
        try {
            if (!approvalStatusUpdateRequestDto.isRequiredAvailable()) {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            branchService.approveOrRejectBranch(approvalStatusUpdateRequestDto, authToken);
            log.debug("Successfully approved/rejected branch. Branch id: {}, action: {}",
                    approvalStatusUpdateRequestDto.getId(), approvalStatusUpdateRequestDto.getAction());
            SuccessResponseStatusType statusType =
                    (approvalStatusUpdateRequestDto.getAction() == ApprovalAction.APPROVE) ?
                            SuccessResponseStatusType.APPROVED_BRANCH :
                            SuccessResponseStatusType.REJECTED_BRANCH;
            return getSuccessResponse(statusType, null);
        } catch (InvalidBranchException e) {
            log.error("Invalid branchId: {} to {}.", approvalStatusUpdateRequestDto.getId(),
                    approvalStatusUpdateRequestDto.getAction(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_BRANCH_ID);
        } catch (KupiLocationException e) {
            log.error("Approving/rejecting branch was failed. BranchId: {}, action: {}",
                    approvalStatusUpdateRequestDto.getId(), approvalStatusUpdateRequestDto.getAction(), e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to verify address or contact no.
     *
     * @param verifyAddressContactRequestDto verifyAddressContactRequestDto
     * @return success/error response.
     */
    @PutMapping("/{verificationType}/verify")
    public ResponseEntity<ResponseWrapper> verifyAddressContactNo(@PathVariable VerificationType verificationType,
                                                                  @RequestBody VerifyAddressContactRequestDto
                                                                          verifyAddressContactRequestDto) {
        try {
            if (!verifyAddressContactRequestDto.isRequiredAvailable()) {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            branchService.verifyAddressOrContactNo(verifyAddressContactRequestDto, verificationType);
            log.debug("Successfully updated the verification status of branch address/ contact number. Branch id: {}," +
                    " action: {}", verifyAddressContactRequestDto.getId(), verifyAddressContactRequestDto.getAction());
            SuccessResponseStatusType statusType =
                    (verifyAddressContactRequestDto.getAction() == VerificationStatus.VERIFIED) ?
                            SuccessResponseStatusType.VERIFIED : SuccessResponseStatusType.NOT_VERIFIED;
            return getSuccessResponse(statusType, null);
        } catch (InvalidBranchException e) {
            log.error("Invalid branchId: {} to {}.", verifyAddressContactRequestDto.getId(),
                    verifyAddressContactRequestDto.getAction(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_BRANCH_ID);
        } catch (KupiLocationException e) {
            log.error("Updating verification status for branch address/ contact number was failed. " +
                            "BranchId: {}, action: {}", verifyAddressContactRequestDto.getId(),
                    verifyAddressContactRequestDto.getAction(), e);
            return getInternalServerError();
        }
    }

    /**
     * This method creates new branch.
     *
     * @param userType         MERCHANT/BANK
     * @param userId           userId
     * @param branchRequestDto branchRequestDto
     * @return success/error response.
     */
    @PostMapping("/{userType}")
    public ResponseEntity<ResponseWrapper> createBranch(@PathVariable UserType userType,
                                                        @RequestHeader(name = USER_ID_HEADER) String userId,
                                                        @RequestBody BranchRequestDto branchRequestDto) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "create branch");
                return getBadRequestError(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            if (!branchRequestDto.isRequiredAvailable()) {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            if (!validator.isValidUrl(branchRequestDto.getLocation().getLocationMapUrl())) {
                return getBadRequestError(ErrorResponseStatusType.INVALID_MAP_URL);
            }
            if (!branchRequestDto.getLocation().isValidLatitude()) {
                return getBadRequestError(ErrorResponseStatusType.INVALID_LATITUDE);
            }
            if (!branchRequestDto.getLocation().isValidLongitude()) {
                return getBadRequestError(ErrorResponseStatusType.INVALID_LONGITUDE);
            }
            Branch branch = new Branch(branchRequestDto, userType);
            branchService.createBranch(branch, userType);
            BasicBranchResponseDto basicBranchResponseDto = new BasicBranchResponseDto(branch);
            log.debug("Successfully created the branch. userId: {}, branch response {}",
                    userId, basicBranchResponseDto.toLogJson());
            return getSuccessResponse(SuccessResponseStatusType.CREATE_BRANCH, basicBranchResponseDto);
        } catch (InvalidUserException e) {
            log.error("Business profile not found for userId: {}", userId, e);
            return getBadRequestError(ErrorResponseStatusType.BUSINESS_PROFILE_NOT_FOUND);
        } catch (ExistingNameException e) {
            log.error("Branch name exists in the database for userId: {}", userId, e);
            return getBadRequestError(ErrorResponseStatusType.EXISTING_BRANCH_NAME);
        } catch (ExistingAddressException e) {
            log.error("Branch address exists in the database for userId: {}", userId, e);
            return getBadRequestError(ErrorResponseStatusType.EXISTING_BRANCH_ADDRESS);
        } catch (KupiLocationException e) {
            log.error("Creating new branch was failed for userId: {}", userId, e);
            return getInternalServerError();
        }
    }


    /**
     * This method updates existing branch
     *
     * @param userId              userId
     * @param branchUpdateRequest branchUpdateRequest
     * @return updateBranchResponse
     */
    @PutMapping()
    public ResponseEntity<ResponseWrapper> updateBranch(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestBody BranchUpdateRequestDto branchUpdateRequest) {
        try {
            if (!branchUpdateRequest.isRequiredAvailable()) {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            if (!validator.isValidUrl(branchUpdateRequest.getLocation().getLocationMapUrl())) {
                return getBadRequestError(ErrorResponseStatusType.INVALID_MAP_URL);
            }
            if (!branchUpdateRequest.getLocation().isValidLatitude()) {
                return getBadRequestError(ErrorResponseStatusType.INVALID_LATITUDE);
            }
            if (!branchUpdateRequest.getLocation().isValidLongitude()) {
                return getBadRequestError(ErrorResponseStatusType.INVALID_LONGITUDE);
            }
            var updateBranch = branchService.updateBranch(branchUpdateRequest);
            DetailBranchResponseDto detailBranchResponseDto = new DetailBranchResponseDto(updateBranch);
            log.debug("Successfully updated the branch. userId: {}, branch response {}",
                    userId, detailBranchResponseDto.toLogJson());
            return getSuccessResponse(SuccessResponseStatusType.UPDATE_BRANCH, detailBranchResponseDto);
        } catch (InvalidBranchException e) {
            log.error("Invalid branchId: {} for userId: {}", branchUpdateRequest.getId(), userId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_BRANCH_ID);
        } catch (ExistingNameException e) {
            log.error("Branch name exists in the database for userId: {}", userId, e);
            return getBadRequestError(ErrorResponseStatusType.EXISTING_BRANCH_NAME);
        } catch (ExistingAddressException e) {
            log.error("Branch address exists in the database for userId: {}", userId, e);
            return getBadRequestError(ErrorResponseStatusType.EXISTING_BRANCH_ADDRESS);
        } catch (KupiLocationException e) {
            log.error("Updating existing branch was failed for userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns approved branch list.
     *
     * @param userId     userId
     * @param page       page
     * @param size       size
     * @param searchTerm searchTerm
     * @return approved branch list.
     */
    @GetMapping("/active/{userId}/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getActiveBranchList(@PathVariable String userId,
                                                               @Min(DEFAULT_PAGE) @PathVariable int page,
                                                               @Min(DEFAULT_PAGE) @Max(PAGE_MAX_SIZE) @Positive
                                                               @PathVariable int size,
                                                               @PathVariable String searchTerm) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(SORT_NAME).ascending());
            Page<ApprovedBranch> approvedBranchPage =
                    branchService.getApprovedBranchListByUserId(userId, searchTerm, pageable);
            BasicBranchListResponseDto branchResponseDto = new BasicBranchListResponseDto(approvedBranchPage);
            log.debug("Successfully returned approved branch list for userId: {}", userId);
            return getSuccessResponse(SuccessResponseStatusType.READ_BRANCH_LIST, branchResponseDto);
        } catch (KupiLocationException e) {
            log.error("Getting active branch list was failed for userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns branch details.
     *
     * @param userId   userId
     * @param branchId branchId
     * @return branch details
     */
    @GetMapping("/{branchId}")
    public ResponseEntity<ResponseWrapper> getDetailBranch(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                           @PathVariable String branchId) {
        try {
            DetailBranchResponseDto detailBranchResponseDto = branchService.getBranchDetailById(branchId);
            log.debug("Successfully returned branch detail for userId: {}", userId);
            return getSuccessResponse(SuccessResponseStatusType.READ_BRANCH_DETAIL, detailBranchResponseDto);
        } catch (InvalidBranchException e) {
            log.error("Invalid branchId: {} for userId: {}", branchId, userId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_BRANCH_ID);
        } catch (KupiLocationException e) {
            log.error("Getting branch details was failed for userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method deletes branch by Id.
     *
     * @param userId   userId
     * @param branchId branchId
     * @return SuccessResponse/ ErrorResponse
     */
    @DeleteMapping("/{branchId}")
    public ResponseEntity<ResponseWrapper> deleteBranch(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                        @PathVariable String branchId) {
        try {
            branchService.deteleBranch(branchId);
            return getSuccessResponse(SuccessResponseStatusType.DELETED_BRANCH, null);
        } catch (InvalidBranchException e) {
            log.error("Invalid brandId: {} to delete.", branchId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_BRANCH_ID);
        } catch (KupiLocationException e) {
            log.error("Deleting branch was failed for branch id : {}, user id :{}.", branchId, userId, e);
            return getInternalServerError();
        }
    }
}