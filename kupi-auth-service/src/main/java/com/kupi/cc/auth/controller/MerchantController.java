package com.kupi.cc.auth.controller;

import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.domain.entity.*;
import com.kupi.cc.auth.domain.request.*;
import com.kupi.cc.auth.domain.response.*;
import com.kupi.cc.auth.enums.ApprovalAction;
import com.kupi.cc.auth.enums.ErrorResponseStatusType;
import com.kupi.cc.auth.enums.RoleType;
import com.kupi.cc.auth.enums.SuccessResponseStatusType;
import com.kupi.cc.auth.exception.*;
import com.kupi.cc.auth.service.BusinessMenuImageService;
import com.kupi.cc.auth.service.MerchantService;
import com.kupi.cc.auth.service.SliderImageService;
import com.kupi.cc.auth.service.UserService;
import com.kupi.cc.auth.util.Validator;
import com.kupi.cc.auth.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Optional;

/**
 * Merchant controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users/{roleType}")
public class MerchantController extends Controller {

    private static final int MAX_USER_COUNT = 250;
    private final MerchantService merchantService;
    private final Validator validator;
    private final UserService userService;
    Translator translator;
    private final SliderImageService sliderImageService;
    private final BusinessMenuImageService businessMenuImageService;


    public MerchantController(Translator translator, MerchantService merchantService, Validator validator,
                              UserService userService, SliderImageService sliderImageService,
                              BusinessMenuImageService businessMenuImageService) {
        super(translator);
        this.translator = translator;
        this.merchantService = merchantService;
        this.validator = validator;
        this.userService = userService;
        this.sliderImageService = sliderImageService;
        this.businessMenuImageService = businessMenuImageService;
    }

    /**
     * This method is used to block/unblock merchant.
     *
     * @param userId                         userId
     * @param merchantStatusUpdateRequestDto requestDto
     * @return success/failure response
     */
    @Secured({ADMIN_ROLE})
    @PutMapping("/block")
    public ResponseEntity<ResponseWrapper> blockOrUnblockMerchant(@RequestHeader(name = HEADER_USER_ID) String userId,
                                                                  @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                  @RequestBody MerchantStatusUpdateRequestDto
                                                                          merchantStatusUpdateRequestDto) {
        try {
            if (!merchantStatusUpdateRequestDto.isRequiredAvailable()) {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            merchantService.updateMerchantApprovalStatus(merchantStatusUpdateRequestDto, timeZone);
            log.debug("Successfully blocked/unblocked merchant: {} by: {}, action: {}",
                    merchantStatusUpdateRequestDto.getMerchantId(), userId, merchantStatusUpdateRequestDto.getAction());
            SuccessResponseStatusType successMessage =
                    merchantStatusUpdateRequestDto.getAction().equals(ApprovalAction.BLOCK) ?
                            SuccessResponseStatusType.BLOCKED_USER : SuccessResponseStatusType.UNBLOCKED_USER;
            return getSuccessResponse(successMessage, null);
        } catch (InvalidActionException e) {
            log.error("Invalid action: {} for merchantId: {} to block/unblock",
                    merchantStatusUpdateRequestDto.getAction(), merchantStatusUpdateRequestDto.getMerchantId(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_ACTION);
        } catch (InvalidUserException e) {
            log.error("Invalid merchantId: {} to block/unblock", merchantStatusUpdateRequestDto.getMerchantId(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_USER_ID);
        } catch (AuthServiceException e) {
            log.error("Failed to block/unblock merchant: {} by admin: {}, action: {}",
                    merchantStatusUpdateRequestDto.getMerchantId(), userId,
                    merchantStatusUpdateRequestDto.getAction(), e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to approve/reject merchant.
     *
     * @param userId                         userId
     * @param merchantStatusUpdateRequestDto requestDto
     * @return success/failure response
     */
    @Secured({ADMIN_ROLE})
    @PutMapping("/approve")
    public ResponseEntity<ResponseWrapper> approveOrRejectMerchant(@RequestHeader(name = HEADER_USER_ID) String userId,
                                                                   @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                   @RequestBody MerchantStatusUpdateRequestDto
                                                                           merchantStatusUpdateRequestDto) {
        try {
            if (!merchantStatusUpdateRequestDto.isRequiredAvailable()) {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            merchantService.updateMerchantApprovalStatus(merchantStatusUpdateRequestDto, timeZone);
            log.debug("Successfully approved/rejected merchant: {} by: {}, action: {}",
                    merchantStatusUpdateRequestDto.getMerchantId(), userId, merchantStatusUpdateRequestDto.getAction());
            SuccessResponseStatusType successMessage =
                    merchantStatusUpdateRequestDto.getAction().equals(ApprovalAction.APPROVE) ?
                            SuccessResponseStatusType.APPROVED_USER : SuccessResponseStatusType.REJECTED_USER;
            return getSuccessResponse(successMessage, null);
        } catch (InvalidActionException e) {
            log.error("Invalid action: {} for merchantId: {} to approved/rejected",
                    merchantStatusUpdateRequestDto.getAction(), merchantStatusUpdateRequestDto.getMerchantId(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_ACTION);
        } catch (InvalidUserException e) {
            log.error("Invalid merchantId: {} to approved/rejected", merchantStatusUpdateRequestDto.getMerchantId(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_USER_ID);
        } catch (AuthServiceException e) {
            log.error("Failed to approve/reject merchant: {} by admin: {}, action: {}",
                    merchantStatusUpdateRequestDto.getMerchantId(), userId,
                    merchantStatusUpdateRequestDto.getAction(), e);
            return getInternalServerError();
        }
    }

    /**
     * This method lists all pending merchants.
     *
     * @param userId     userId
     * @param timeZone   timeZone
     * @param page       page
     * @param size       size
     * @param searchTerm searchTerm
     * @return list of pending merchants
     */
    @Secured({ADMIN_ROLE})
    @GetMapping(path = "/PENDING/{page}/{size}/search/{searchTerm}", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> listPendingMerchant(@RequestHeader(name = HEADER_USER_ID) String userId,
                                                               @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                               @Min(DEFAULT_PAGE) @PathVariable("page") Integer page,
                                                               @Min(DEFAULT_PAGE) @Max(PAGE_MAX_SIZE)
                                                               @Positive @PathVariable("size") Integer size,
                                                               @PathVariable("searchTerm") String searchTerm) {
        try {
            Page<User> pendingMerchantPage = merchantService.getPendingMerchant(page, size, searchTerm);
            AllUserListResponseDto allUserListResponseDto = new AllUserListResponseDto(pendingMerchantPage, timeZone);
            log.debug("Successfully returned pending merchant list for userId: {}. Page: {}, Size: {}, SearchTerm: {}",
                    userId, page, size, searchTerm);
            return getSuccessResponse(SuccessResponseStatusType.GET_PENDING_MERCHANTS, allUserListResponseDto);
        } catch (AuthServiceException e) {
            log.error("Getting pending merchant list failed for userId: {}. Page: {}, Size: {}, SearchTerm: {}",
                    userId, page, size, searchTerm, e);
            return getInternalServerError();
        }
    }

    /**
     * This method will return basic merchant information.
     *
     * @param userId     userId
     * @param timeZone   timeZone
     * @param merchantId merchantId
     * @return merchant summary
     */
    @Secured({ADMIN_ROLE, MERCHANT_ROLE})
    @GetMapping(path = "/{merchantId}", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getMerchantSummary(@RequestHeader(name = HEADER_USER_ID) String userId,
                                                              @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                              @PathVariable("merchantId") String merchantId) {
        try {
            User merchant = userService.getUserByUserId(merchantId);
            UserResponseDto userResponseDto = new UserResponseDto(merchant, timeZone);
            log.debug("Successfully returned merchant summary for merchantId: {} by admin: {}. Data: {}", merchantId,
                    userId, userResponseDto.toLogJson());
            return getSuccessResponse(SuccessResponseStatusType.GET_MERCHANT, userResponseDto);
        } catch (InvalidUserException e) {
            log.error("Invalid merchantId: {}. Failed to get merchant summary.", merchantId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (AuthServiceException e) {
            log.error("Getting merchant summary failed for merchantId: {} by admin: {}", merchantId, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method create/updates business profile.
     *
     * @param timeZone           timeZone
     * @param roleType           roleType
     * @param businessRequestDto requestDto
     * @return created/updated business info
     */
    @Secured({ADMIN_ROLE, MERCHANT_ROLE})
    @PutMapping(path = "/business", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> createOrUpdateBusinessInfo(@RequestHeader(name = TIME_ZONE_HEADER)
                                                                      String timeZone,
                                                                      @PathVariable RoleType roleType,
                                                                      @RequestBody
                                                                      BusinessRequestDto businessRequestDto) {
        try {
            if (!businessRequestDto.validateTelephoneNumberIfPresent(validator)) {
                return getBadRequestError(ErrorResponseStatusType.INVALID_TELEPHONE_NUMBER);
            }
            if (businessRequestDto.isRequiredAvailable()) {
                if (!businessRequestDto.isValidMobileNo(validator)) {
                    return getBadRequestError(ErrorResponseStatusType.INVALID_MOBILE_NUMBER);
                }
                if (!businessRequestDto.isContainingValidEmail(validator)) {
                    return getBadRequestError(ErrorResponseStatusType.INVALID_EMAIL);
                }
                if (!businessRequestDto.isContainingValidUrls(validator)) {
                    return getBadRequestError(ErrorResponseStatusType.INVALID_URLS);
                }
                if (!businessRequestDto.isValidDescription()) {
                    return getBadRequestError(ErrorResponseStatusType.INVALID_DESCRIPTION);
                }
                Business business = merchantService.createOrUpdateBusiness(businessRequestDto, roleType);
                BusinessResponseDto businessResponseDto = new BusinessResponseDto(business, timeZone);
                log.debug("Updated business info for merchantId: {}", businessRequestDto.getMerchantId());
                return getSuccessResponse(SuccessResponseStatusType.UPDATED_BUSINESS_PROFILE, businessResponseDto);
            } else {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (InvalidUserException e) {
            log.error("Invalid merchantId: {} to create/update merchant's business information",
                    businessRequestDto.getMerchantId(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (AuthServiceException e) {
            log.error("Updating business info was failed for merchantId: {}",
                    businessRequestDto.getMerchantId(), e);
            return getInternalServerError();
        }
    }

    @Secured({ADMIN_ROLE, MERCHANT_ROLE})
    @PutMapping(path = "/business/menu-image", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> saveMenuImage(@RequestBody MenuImageRequestDto requestDto,
                                                         @PathVariable RoleType roleType,
                                                         @RequestHeader(name = TIME_ZONE_HEADER) String timeZone) {

        try {
            if (!requestDto.isRequiredAvailable()) {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }

            if (requestDto.getImageUrl() != null && !requestDto.getImageUrl().isEmpty()
                    && !validator.isValidUrl(requestDto.getImageUrl())) {
                    return getBadRequestError(ErrorResponseStatusType.INVALID_URLS);
                }

            MenuImageResponseDto responseDto = businessMenuImageService.saveMenuImage(requestDto);
            return getSuccessResponse(SuccessResponseStatusType.MERCHANT_MENU_IMAGE_SAVED, responseDto);

        } catch (InvalidUserException e) {
            log.error("Invalid merchantId: {} while saving menu image", requestDto.getMerchantId(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_MERCHANT_ID);

        } catch (AuthServiceException e) {
            log.error("Failed to save menu image for merchantId: {}, roleType: {}, timeZone: {}", requestDto.getMerchantId(), roleType, timeZone, e);
            return getInternalServerError();
        }
    }

    @Secured({ADMIN_ROLE, MERCHANT_ROLE})
    @GetMapping(path = "/business/menu-image/{merchantId}", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getMenuImage(@PathVariable RoleType roleType,
                                                        @PathVariable("merchantId") String merchantId,
                                                        @RequestHeader(name = TIME_ZONE_HEADER) String timeZone) {

        try {
            MenuImageResponseDto responseDto = businessMenuImageService.getMenuImage(merchantId);
            return getSuccessResponse(SuccessResponseStatusType.GET_MENU_IMAGE, responseDto);
        } catch (InvalidUserException e) {
            log.error("Invalid merchantId: {} while fetching menu image", merchantId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_MERCHANT_ID);

        } catch (AuthServiceException e) {
            log.error("Failed to get menu image for merchantId: {}", merchantId, e);
            return getInternalServerError();
        }
    }


    /**
     * This endpoint used to add merchant's profile slider images.
     *
     * @param sliderImageCreateRequestDto sliderImageCreateRequestDto
     * @param roleType                    role type
     * @param timeZone                    time zone
     * @return SliderImageListResponseDto
     */
    @Secured({ADMIN_ROLE, MERCHANT_ROLE})
    @PutMapping(path = "/business/slider-images", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> addBusinessSliderImages(@RequestBody SliderImageCreateRequestDto sliderImageCreateRequestDto,
                                                                   @PathVariable RoleType roleType,
                                                                   @RequestHeader(name = TIME_ZONE_HEADER) String timeZone) {
        try {

            if (sliderImageCreateRequestDto.getImages() == null) {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }

            if (!sliderImageCreateRequestDto.getImages().isEmpty()) {
                if (sliderImageCreateRequestDto.getImages().size() > 3) {
                    return getBadRequestError(ErrorResponseStatusType.MAX_SLIDER_IMAGES_EXCEEDED);
                }

                for (SliderImageItemDto img : sliderImageCreateRequestDto.getImages()) {
                    if (!validator.isValidUrl(img.getImageUrl())) {
                        return getBadRequestError(ErrorResponseStatusType.INVALID_URLS);
                    }
                }
            }
            SliderImageListResponseDto sliderImageListResponseDto = sliderImageService
                    .addSliderImages(sliderImageCreateRequestDto);
            return getSuccessResponse(SuccessResponseStatusType.SLIDER_IMAGES_ADDED, sliderImageListResponseDto);

        } catch (SliderImageLimitExceededException e) {
            log.error("Slider image limit exceeded for merchantId: {}", sliderImageCreateRequestDto.getMerchantId(), e);

            return getBadRequestError(ErrorResponseStatusType.MAX_SLIDER_IMAGES_EXCEEDED);
        } catch (InvalidUserException e) {
            log.error("Invalid businessId: {} while adding slider images",
                    sliderImageCreateRequestDto.getMerchantId(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_MERCHANT_ID);

        } catch (AuthServiceException e) {
            log.error("Failed to add slider images for businessId: {} & user type: {} for timeZone: {}",
                    sliderImageCreateRequestDto.getMerchantId(), roleType, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get merchant slider images.
     *
     * @param timeZone   time zone
     * @param roleType   role type
     * @param merchantId merchant id
     * @return SliderImageListResponseDto
     */
    @Secured({ADMIN_ROLE, MERCHANT_ROLE})
    @GetMapping(path = "/business/slider-images/{merchantId}", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getBusinessSliderImages(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                   @PathVariable RoleType roleType,
                                                                   @PathVariable("merchantId") String merchantId) {

        try {
            merchantService.validateMerchantId(merchantId);
            SliderImageListResponseDto responseDto =
                    sliderImageService.getSliderImagesByMerchantId(merchantId);

            log.debug("Successfully returned slider images for merchantId: {}", merchantId);
            return getSuccessResponse(SuccessResponseStatusType.GET_SLIDER_IMAGES, responseDto);

        } catch (InvalidUserException e) {
            log.error("Invalid merchantId: {} for retrieving slider images", merchantId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (AuthServiceException e) {
            log.error("Failed to retrieve slider images for merchantId: {}, user type: {}, time zone: {}"
                    , merchantId, roleType, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * Internal endpoint to get merchant slider images for other services (e.g. core service).
     * Access is protected via app-key header and ApiKeyFilter.
     *
     * @param roleType   role type (expects MERCHANT)
     * @param merchantId merchant id
     * @param timeZone   time zone (optional for logging)
     * @return SliderImageListResponseDto
     */
    @GetMapping(path = "/business/slider-images/internal/{merchantId}", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getBusinessSliderImagesInternal(
            @RequestHeader(name = TIME_ZONE_HEADER, required = false) String timeZone,
            @PathVariable RoleType roleType,
            @PathVariable("merchantId") String merchantId) {

        try {
            if (!isUserTypeMerchantOrBank(roleType))
                return getBadRequestError(ErrorResponseStatusType.INVALID_USER_TYPE);

            merchantService.validateMerchantId(merchantId);
            SliderImageListResponseDto responseDto =
                    sliderImageService.getSliderImagesByMerchantId(merchantId);

            log.debug("Successfully returned slider images (internal) for merchantId: {}", merchantId);
            return getSuccessResponse(SuccessResponseStatusType.GET_SLIDER_IMAGES, responseDto);

        } catch (InvalidUserException e) {
            log.error("Invalid merchantId: {} for retrieving slider images (internal)", merchantId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (AuthServiceException e) {
            log.error("Failed to retrieve slider images (internal) for merchantId: {}, user type: {}, time zone: {}",
                    merchantId, roleType, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * Internal bulk endpoint to get merchant slider images for multiple merchants in one call.
     * Access is protected via app-key header and ApiKeyFilter.
     *
     * @param timeZone          time zone (optional for logging)
     * @param roleType          role type (expects MERCHANT)
     * @param bulkUserRequestDto list of merchant ids
     * @return BulkSliderImageListResponseDto
     */
    @PostMapping(path = "/business/slider-images/internal/bulk", consumes = APPLICATION_JSON_UTF_8,
            produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getBusinessSliderImagesInternalBulk(
            @RequestHeader(name = TIME_ZONE_HEADER, required = false) String timeZone,
            @PathVariable RoleType roleType,
            @RequestBody BulkUserRequestDto bulkUserRequestDto) {

        try {
            if (!isUserTypeMerchantOrBank(roleType))
                return getBadRequestError(ErrorResponseStatusType.INVALID_USER_TYPE);

            if (!bulkUserRequestDto.isRequiredAvailable()) {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }

            BulkSliderImageListResponseDto responseDto =
                    sliderImageService.getSliderImagesByMerchantIds(bulkUserRequestDto.getUserIds());

            log.debug("Successfully returned bulk slider images (internal) for {} merchants",
                    bulkUserRequestDto.getUserIds().size());
            return getSuccessResponse(SuccessResponseStatusType.GET_SLIDER_IMAGES, responseDto);

        } catch (AuthServiceException e) {
            log.error("Failed to retrieve bulk slider images (internal) for role type: {}, time zone: {}",
                    roleType, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get merchant business profile by toUserId.
     *
     * @param userId   userId
     * @param timeZone timeZone
     * @param roleType roleType
     * @param toUserId toUserId
     * @return business profile
     */
    @Secured({ADMIN_ROLE, MERCHANT_ROLE})
    @GetMapping(path = "/business/merchant/{toUserId}", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getBusinessProfile(@RequestHeader(name = HEADER_USER_ID) String userId,
                                                              @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                              @PathVariable RoleType roleType,
                                                              @PathVariable("toUserId") String toUserId) {
        try {
            if (!isUserTypeMerchantOrBank(roleType))
                return getBadRequestError(ErrorResponseStatusType.INVALID_USER_TYPE);
            merchantService.validateMerchantId(toUserId);
            Optional<Business> business = merchantService.getLatestBusinessInfoByUserId(toUserId, roleType);
            if (business.isPresent()) {
                BusinessResponseDto businessResponseDto = new BusinessResponseDto(business.get(), timeZone);
                log.debug("Successfully returned business profile for toUserId: {} by user: {} roleType: {}. Data: {}",
                        toUserId, userId, roleType, businessResponseDto.toLogJson());
                return getSuccessResponse(SuccessResponseStatusType.GET_BUSINESS_PROFILE, businessResponseDto);
            } else {
                log.debug("No existing business info available for toUserId: {}.", toUserId);
                return getSuccessResponse(SuccessResponseStatusType.NO_BUSINESS_INFO_FOUND, null);
            }
        } catch (InvalidUserException e) {
            log.error("Invalid toUserId: {}. Failed to get merchant business profile.", toUserId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (AuthServiceException e) {
            log.error("Getting business profile failed for toUserId: {} by user: {} roleType: {}",
                    toUserId, userId, roleType, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get merchant business profile by businessId.
     *
     * @param userId     userId
     * @param timeZone   timeZone
     * @param roleType   roleType
     * @param businessId businessId
     * @return business profile
     */
    @Secured({ADMIN_ROLE, MERCHANT_ROLE})
    @GetMapping(path = "/business/{businessId}", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getBusinessByBusinessId(@RequestHeader(name = HEADER_USER_ID) String userId,
                                                                   @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                   @PathVariable RoleType roleType,
                                                                   @PathVariable("businessId") String businessId) {
        try {
            if (!isUserTypeMerchantOrBank(roleType))
                return getBadRequestError(ErrorResponseStatusType.INVALID_USER_TYPE);
            Business business = merchantService.getBusinessInfoByBusinessId(businessId, roleType);
            BusinessResponseDto businessResponseDto = new BusinessResponseDto(business, timeZone);
            log.debug("Successfully returned business profile for businessId: {} by user: {} roleType: {}",
                    businessId, userId, roleType);
            return getSuccessResponse(SuccessResponseStatusType.GET_BUSINESS_PROFILE, businessResponseDto);
        } catch (BusinessProfileException e) {
            log.error("Invalid businessId: {}.", businessId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_BUSINESS_ID);
        } catch (AuthServiceException e) {
            log.error("Getting business profile failed for businessId: {} by user: {} roleType: {}",
                    businessId, userId, roleType, e);
            return getInternalServerError();
        }
    }

    /**
     * This method checks if user type is merchant or bank.
     *
     * @param roleType roleType
     * @return true/false
     */
    private boolean isUserTypeMerchantOrBank(RoleType roleType) {
        return roleType.equals(RoleType.MERCHANT) || roleType.equals(RoleType.BANK);
    }

    /**
     * This method is used to create/update merchant contact information.
     *
     * @param timeZone          timeZone
     * @param contactRequestDto contactRequestDto
     * @return created/updated contact info
     */
    @Secured({ADMIN_ROLE, MERCHANT_ROLE})
    @PutMapping(path = "/contact", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> createOrUpdateContactInfo(@RequestHeader(name = TIME_ZONE_HEADER)
                                                                     String timeZone,
                                                                     @PathVariable RoleType roleType,
                                                                     @RequestBody ContactRequestDto contactRequestDto) {
        try {
            if (!isUserTypeMerchantOrBank(roleType))
                return getBadRequestError(ErrorResponseStatusType.INVALID_USER_TYPE);
            if (contactRequestDto.isRequiredAvailable()) {
                if (!validator.isValidMobileNoWithCountryCode(contactRequestDto.getTelephone().getNo())) {
                    return getBadRequestError(ErrorResponseStatusType.INVALID_TELEPHONE_NUMBER);
                }
                if (!contactRequestDto.isContainingValidEmail(validator)) {
                    return getBadRequestError(ErrorResponseStatusType.INVALID_EMAIL);
                } else {
                    Contact contact = merchantService.createOrUpdateContact(contactRequestDto, roleType);
                    ContactResponseDto contactResponseDto = new ContactResponseDto(contact, timeZone);
                    log.debug("Updated contact info for merchantId: {}", contactResponseDto.getMerchantId());
                    return getSuccessResponse(SuccessResponseStatusType.UPDATED_CONTACT_PROFILE, contactResponseDto);
                }
            } else {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (InvalidUserException e) {
            log.error("Invalid merchantId: {} to create/update merchant's contact information",
                    contactRequestDto.getMerchantId(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (AuthServiceException e) {
            log.error("Updating contact info was failed for merchantId: {}",
                    contactRequestDto.getMerchantId(), e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used get merchant's contact information.
     *
     * @param userId     userId
     * @param timeZone   timeZone
     * @param merchantId merchantId
     * @return merchant contact profile
     */
    @Secured({ADMIN_ROLE, MERCHANT_ROLE})
    @GetMapping(path = "/contact/merchant/{merchantId}", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getContactProfile(@RequestHeader(name = HEADER_USER_ID) String userId,
                                                             @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                             @PathVariable("merchantId") String merchantId) {
        try {
            merchantService.validateMerchantId(merchantId);
            Optional<Contact> contact = merchantService.getContactInfoByMerchantId(merchantId);
            if (contact.isPresent()) {
                ContactResponseDto contactResponseDto = new ContactResponseDto(contact.get(), timeZone);
                log.debug("Successfully returned contact information for merchantId: {} by user: {}.",
                        merchantId, userId);
                return getSuccessResponse(SuccessResponseStatusType.GET_CONTACT_PROFILE, contactResponseDto);
            } else {
                log.debug("No existing contact info available for merchantId: {}.", merchantId);
                return getSuccessResponse(SuccessResponseStatusType.NO_CONTACT_INFO_FOUND, null);
            }
        } catch (InvalidUserException e) {
            log.error("Invalid merchantId: {}. Failed to get merchant contact info.", merchantId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (AuthServiceException e) {
            log.error("Getting contact info failed for merchantId: {} by user: {}", merchantId, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to list pending business information for merchants.
     *
     * @param userId     userId
     * @param timeZone   timeZone
     * @param page       page
     * @param size       size
     * @param searchTerm searchTerm
     * @return list of pending business info
     */
    @Secured({ADMIN_ROLE})
    @GetMapping(path = "/business/PENDING/{page}/{size}/search/{searchTerm}", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> listPendingBusinessInfo(
            @RequestHeader(name = HEADER_USER_ID) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable RoleType roleType,
            @Min(DEFAULT_PAGE) @PathVariable("page") Integer page,
            @Min(DEFAULT_PAGE) @Max(PAGE_MAX_SIZE)
            @Positive @PathVariable("size") Integer size,
            @PathVariable("searchTerm") String searchTerm) {

        try {
            if (!roleType.equals(RoleType.MERCHANT) && !roleType.equals(RoleType.BANK))
                return getBadRequestError(ErrorResponseStatusType.INVALID_USER_TYPE);
            PendingBusinessResponseListDto businessResponseListDto;
            if (roleType.equals(RoleType.MERCHANT)) {
                Page<Business> pendingBusinessPage = merchantService.getPendingBusinessInfoList(page, size, searchTerm);
                businessResponseListDto = new PendingBusinessResponseListDto(pendingBusinessPage, timeZone);
            } else {
                Page<BankBusiness> pendingBusinessPage =
                        merchantService.getPendingBankBusinessInfoList(page, size, searchTerm);
                businessResponseListDto = new PendingBusinessResponseListDto(timeZone, pendingBusinessPage);
            }
            log.debug("Successfully returned pending business info for userId: {}. Page: {}, Size: {}, SearchTerm: {}",
                    userId, page, size, searchTerm);
            return getSuccessResponse(SuccessResponseStatusType.GET_PENDING_BUSINESS_INFO, businessResponseListDto);
        } catch (AuthServiceException e) {
            log.error("Getting pending business info list failed for userId: {}. Page: {}, Size: {}, SearchTerm: {}",
                    userId, page, size, searchTerm, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to list pending merchants contact information.
     *
     * @param userId     userId
     * @param timeZone   timeZone
     * @param page       page
     * @param size       size
     * @param searchTerm searchTerm
     * @return list of pending contact info
     */
    @Secured({ADMIN_ROLE})
    @GetMapping(path = "/contact/PENDING/{page}/{size}/search/{searchTerm}", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> listPendingContactInfo(
            @RequestHeader(name = HEADER_USER_ID) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable RoleType roleType,
            @Min(DEFAULT_PAGE) @PathVariable("page") Integer page,
            @Min(DEFAULT_PAGE) @Max(PAGE_MAX_SIZE)
            @Positive @PathVariable("size") Integer size,
            @PathVariable("searchTerm") String searchTerm) {

        try {
            Page<Contact> pendingContactPage = merchantService.getPendingContactInfoList(page, size, searchTerm, roleType);
            List<PendingContactResponseDto> pendingContactList =
                    merchantService.getBusinessDetailsForContact(pendingContactPage, roleType.toString(), timeZone);
            var contactResponseListDto = new PendingContactResponseListDto(pendingContactPage, pendingContactList);
            log.debug("Successfully returned pending contact info for userId: {}. Page: {}, Size: {}, SearchTerm: {}",
                    userId, page, size, searchTerm);
            return getSuccessResponse(SuccessResponseStatusType.GET_PENDING_CONTACT_PROFILE, contactResponseListDto);
        } catch (AuthServiceException e) {
            log.error("Getting pending contact info list failed for userId: {}. Page: {}, Size: {}, SearchTerm: {}",
                    userId, page, size, searchTerm, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to approve/reject merchant's business info.
     *
     * @param userId                             userId
     * @param timeZone                           timeZone
     * @param roleType                           MERCHANT/BANK
     * @param merchantInfoStatusUpdateRequestDto merchantInfoStatusUpdateRequestDto
     * @return success/failure response
     */
    @Secured({ADMIN_ROLE})
    @PutMapping("/business/approve")
    public ResponseEntity<ResponseWrapper> approveOrRejectBusiness(@RequestHeader(name = HEADER_USER_ID) String userId,
                                                                   @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                   @PathVariable RoleType roleType,
                                                                   @RequestBody MerchantInfoStatusUpdateRequestDto
                                                                           merchantInfoStatusUpdateRequestDto) {
        try {
            if (!isUserTypeMerchantOrBank(roleType))
                return getBadRequestError(ErrorResponseStatusType.INVALID_USER_TYPE);
            if (!merchantInfoStatusUpdateRequestDto.isRequiredAvailable()) {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            var rejectedProfileUpdates = new RejectedProfileUpdates(merchantInfoStatusUpdateRequestDto);
            merchantService.updateMerchantBusinessApprovalStatus(rejectedProfileUpdates,
                    merchantInfoStatusUpdateRequestDto.getAction(), timeZone, roleType);
            log.debug("Successfully approved/rejected business info by: {}, action: {}, roleType: {}",
                    userId, merchantInfoStatusUpdateRequestDto.getAction(), roleType);
            SuccessResponseStatusType successMessage =
                    merchantInfoStatusUpdateRequestDto.getAction().equals(ApprovalAction.APPROVE) ?
                            SuccessResponseStatusType.APPROVED_BUSINESS_INFO : SuccessResponseStatusType.REJECTED_BUSINESS_INFO;
            return getSuccessResponse(successMessage, null);
        } catch (InvalidActionException e) {
            log.error("Invalid action: {} for businessId: {} to approved/rejected",
                    merchantInfoStatusUpdateRequestDto.getAction(), merchantInfoStatusUpdateRequestDto.getId(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_ACTION);
        } catch (InvalidUserException e) {
            log.error("Invalid businessId: {} to approved/rejected", merchantInfoStatusUpdateRequestDto.getId(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_BUSINESS_ID);
        } catch (AuthServiceException e) {
            log.error("Failed to approve/reject business: {} by admin: {}, for action: {}, roleType: {}",
                    merchantInfoStatusUpdateRequestDto.getId(), userId, merchantInfoStatusUpdateRequestDto.getAction(),
                    roleType, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to approve/reject merchant's contact info.
     *
     * @param userId                             userId
     * @param merchantInfoStatusUpdateRequestDto merchantInfoStatusUpdateRequestDto
     * @return success/failure response
     */
    @Secured({ADMIN_ROLE})
    @PutMapping("/contact/approve")
    public ResponseEntity<ResponseWrapper> approveOrRejectContact(@RequestHeader(name = HEADER_USER_ID) String userId,
                                                                  @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                  @PathVariable RoleType roleType,
                                                                  @RequestBody MerchantInfoStatusUpdateRequestDto
                                                                          merchantInfoStatusUpdateRequestDto) {
        try {
            if (!isUserTypeMerchantOrBank(roleType))
                return getBadRequestError(ErrorResponseStatusType.INVALID_USER_TYPE);
            if (!merchantInfoStatusUpdateRequestDto.isRequiredAvailable()) {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            var rejectedProfileUpdates = new RejectedProfileUpdates(merchantInfoStatusUpdateRequestDto);
            merchantService.updateMerchantContactApprovalStatus(rejectedProfileUpdates,
                    merchantInfoStatusUpdateRequestDto.getAction(), timeZone);
            log.debug("Successfully approved/rejected contact info by: {}, action: {}",
                    userId, merchantInfoStatusUpdateRequestDto.getAction());
            SuccessResponseStatusType successMessage =
                    merchantInfoStatusUpdateRequestDto.getAction().equals(ApprovalAction.APPROVE) ?
                            SuccessResponseStatusType.APPROVED_CONTACT_PROFILE : SuccessResponseStatusType.REJECTED_CONTACT_PROFILE;
            return getSuccessResponse(successMessage, null);
        } catch (InvalidActionException e) {
            log.error("Invalid action: {} for contactId: {} to approved/rejected",
                    merchantInfoStatusUpdateRequestDto.getAction(), merchantInfoStatusUpdateRequestDto.getId(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_ACTION);
        } catch (InvalidUserException e) {
            log.error("Invalid contactId: {} to approved/rejected", merchantInfoStatusUpdateRequestDto.getId(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_CONTACT_ID);
        } catch (AuthServiceException e) {
            log.error("Failed to approve/reject contact: {} by admin: {}, for action: {}",
                    merchantInfoStatusUpdateRequestDto.getId(), userId,
                    merchantInfoStatusUpdateRequestDto.getAction(), e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get merchant contact profile by contactId.
     *
     * @param userId    userId
     * @param timeZone  timeZone
     * @param contactId contactId
     * @return contact profile
     */
    @Secured({ADMIN_ROLE, MERCHANT_ROLE})
    @GetMapping(path = "/contact/{contactId}", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getContactByContactId(@RequestHeader(name = HEADER_USER_ID) String userId,
                                                                 @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                 @PathVariable RoleType roleType,
                                                                 @PathVariable("contactId") String contactId) {
        try {
            if (!isUserTypeMerchantOrBank(roleType))
                return getBadRequestError(ErrorResponseStatusType.INVALID_USER_TYPE);
            Contact contact = merchantService.getContactInfoByContactId(contactId);
            ContactResponseDto contactResponseDto = new ContactResponseDto(contact, timeZone);
            log.debug("Successfully returned contact profile for contactId: {} by user: {}.", contactId, userId);
            return getSuccessResponse(SuccessResponseStatusType.GET_CONTACT_PROFILE, contactResponseDto);
        } catch (ContactProfileException e) {
            log.error("Invalid contactId: {}.", contactId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_CONTACT_ID);
        } catch (AuthServiceException e) {
            log.error("Getting contact profile failed for contactId: {} by user: {}", contactId, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns bulk merchant/ bank information with business data.
     *
     * @param bulkUserRequestDto bulkUserRequestDto
     * @param roleType           roleType
     * @param timeZone           timeZone
     * @return bulk merchant info.
     */
    @PostMapping(value = "/bulk-info", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getBulkMerchants(@RequestBody BulkUserRequestDto bulkUserRequestDto,
                                                            @PathVariable RoleType roleType,
                                                            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone) {
        try {
            if (bulkUserRequestDto.isRequiredAvailable()) {
                if (bulkUserRequestDto.getUserIds().size() > MAX_USER_COUNT) {
                    return getBadRequestError(ErrorResponseStatusType.MAX_USER_REQUEST_COUNT);
                }
                List<Business> businessList = merchantService.getBulkMerchantList(bulkUserRequestDto.getUserIds(), roleType);
                return getSuccessResponse(SuccessResponseStatusType.GET_MERCHANT,
                        new BulkMerchantResponseDto(businessList, timeZone, roleType));
            } else {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (AuthServiceException e) {
            log.error("Getting bulk merchant list failed ", e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get latest approved business information by merchant/bank id.
     *
     * @param userId   userId
     * @param timeZone timeZone
     * @param roleType MERCHANT/BANK
     * @param toUserId merchant/bank id
     * @return latest approved business profile.
     */
    @Secured({ADMIN_ROLE, MERCHANT_ROLE})
    @GetMapping(path = "/business/merchant/{toUserId}/APPROVED", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getApprovedBusinessProfile(
            @RequestHeader(name = HEADER_USER_ID) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable RoleType roleType,
            @PathVariable String toUserId) {
        try {
            if (!isUserTypeMerchantOrBank(roleType))
                return getBadRequestError(ErrorResponseStatusType.INVALID_USER_TYPE);
            merchantService.validateMerchantId(toUserId);
            Business approvedBusiness = merchantService.getApprovedBusinessInfoByUserId(toUserId, roleType);
            if (approvedBusiness != null) {
                BusinessResponseDto businessResponseDto = new BusinessResponseDto(approvedBusiness, timeZone);
                log.debug("Successfully returned business profile for userId: {} by user: {} roleType: {}. Data: {}",
                        toUserId, userId, roleType, businessResponseDto.toLogJson());
                return getSuccessResponse(SuccessResponseStatusType.GET_BUSINESS_PROFILE, businessResponseDto);
            } else {
                log.debug("No existing approved business information available for userId: {}.", toUserId);
                return getBadRequestError(ErrorResponseStatusType.NO_APPROVED_BUSINESS_INFO_FOUND);
            }
        } catch (InvalidUserException e) {
            log.error("Invalid userId: {}. Failed to get merchant business profile.", toUserId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (AuthServiceException e) {
            log.error("Getting business profile failed for userId: {} by user: {} roleType: {}.",
                    toUserId, userId, roleType, e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns active merchants/banks page.
     *
     * @param userId     userId
     * @param timeZone   timeZone
     * @param roleType   MERCHANT/BANK
     * @param page       page
     * @param size       size
     * @param searchTerm searchTerm
     * @return merchants/banks page.
     */
    @GetMapping(path = "/active/{page}/{size}/search/{searchTerm}", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> listActiveMerchantOrBank(@RequestHeader(name = HEADER_USER_ID) String userId,
                                                                    @RequestHeader(name = TIME_ZONE_HEADER)
                                                                    String timeZone,
                                                                    @PathVariable RoleType roleType,
                                                                    @Min(DEFAULT_PAGE) @PathVariable int page,
                                                                    @Min(DEFAULT_PAGE) @Max(PAGE_MAX_SIZE)
                                                                    @Positive @PathVariable int size,
                                                                    @PathVariable String searchTerm) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Business> activeBusinessProfilesPage =
                    merchantService.getActiveBusinessProfiles(roleType, pageable, searchTerm);
            log.debug("Successfully returned active merchant/bank page for userId: {}. " +
                    "Page: {}, Size: {}, SearchTerm: {}", userId, page, size, searchTerm);
            return getSuccessResponse(SuccessResponseStatusType.ACTIVE_BUSINESS_PROFILES,
                    new ActiveBusinessProfilePageResponseDto(activeBusinessProfilesPage, timeZone, roleType));
        } catch (AuthServiceException e) {
            log.error("Getting active business profile page was failed for userId: {}. " +
                    "Page: {}, Size: {}, SearchTerm: {}", userId, page, size, searchTerm, e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns demoVideoWatched status for a user
     *
     * @param userId userId from header
     * @return demoVideoWatched status
     */
    @Secured({USER_ROLE, MERCHANT_ROLE})
    @GetMapping(path = "/demo-video-watched", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getDemoVideoWatched(@RequestHeader(name = HEADER_USER_ID) String userId) {
        try {
            User user = userService.getUserByUserId(userId);
            DemoVideoWatchedResponseDto videoWatchedResponseDto = new DemoVideoWatchedResponseDto(user.isDemoVideoWatched());
            log.debug("Successfully returned demoVideoWatched status for userId: {}, value: {}", 
                    userId, user.isDemoVideoWatched());
            return getSuccessResponse(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_DEMO_VIDEO_WATCH_STATUS, videoWatchedResponseDto);
        } catch (InvalidUserException e) {
            log.error("Invalid userId: {} to get demoVideoWatched status", userId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_USER_ID);
        } catch (AuthServiceException e) {
            log.error("Getting demoVideoWatched status failed for userId: {}", userId, e);
            return getInternalServerError();
        }
    }

}
