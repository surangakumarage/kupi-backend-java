package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.BankMerchantSearchIndex;
import com.kupi.cc.base.domain.entity.CategoryBrandMerchant;
import com.kupi.cc.base.domain.entity.CategoryBrandMerchantIndex;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.request.CategoryBrandMerchantRequestDto;
import com.kupi.cc.base.domain.response.*;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.service.*;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Merchant controller
 */
@Slf4j
@RestController
@Validated
@RequestMapping("api/v1/{userType}")
public class MerchantController extends Controller {
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final CategoryBrandMerchantService categoryBrandMerchantService;
    private final AuthUserService authUserService;
    private final CategoryBrandMerchantIndexService categoryBrandMerchantIndexService;
    private final MerchantBankSearchIndexService merchantBankSearchIndexService;
    private final SummaryService summaryService;

    public MerchantController(CategoryService categoryService, BrandService brandService,
                              CategoryBrandMerchantService categoryBrandMerchantService,
                              CategoryBrandMerchantIndexService categoryBrandMerchantIndexService,
                              AuthUserService authUserService, Translator translator,
                              MerchantBankSearchIndexService merchantBankSearchIndexService,
                              SummaryService summaryService) {
        super(translator);
        this.categoryService = categoryService;
        this.brandService = brandService;
        this.categoryBrandMerchantService = categoryBrandMerchantService;
        this.authUserService = authUserService;
        this.categoryBrandMerchantIndexService = categoryBrandMerchantIndexService;
        this.merchantBankSearchIndexService = merchantBankSearchIndexService;
        this.summaryService = summaryService;
    }

    /**
     * This method update categories for merchant
     *
     * @param timeZone                        timeZone
     * @param userId                          userId
     * @param categoryBrandMerchantRequestDto categoriesForMerchantRequestDto
     * @return ResponseEntity
     */
    @PutMapping("")
    public ResponseEntity<ResponseWrapper> updateCategoriesAndBrandsForMerchant(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @PathVariable UserType userType,
            @RequestBody CategoryBrandMerchantRequestDto categoryBrandMerchantRequestDto,
            HttpServletRequest request) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userId, timeZone);
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            if (!categoryBrandMerchantRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            var merchantBusiness = getBusinessProfile(userType, authToken,
                    categoryBrandMerchantRequestDto.getMerchantId());
            final Set<BasicBrandResponseDto> basicBrandResponseDtoSet = new HashSet<>();
            final Set<BasicCategoryResponseDto> basicCategoryResponseDtoSet = new HashSet<>();
            var categorySet = categoryService.getCategorySetByIds(
                    categoryBrandMerchantRequestDto.getCategories());
            categorySet.forEach(category -> basicCategoryResponseDtoSet.add(new BasicCategoryResponseDto(category)));
            if (categoryBrandMerchantRequestDto.isBrandIdsAvailable()) {
                var brandSet = brandService.getBrandSetByIdList(categoryBrandMerchantRequestDto.getBrands());
                brandSet.forEach(brand -> basicBrandResponseDtoSet.add(new BasicBrandResponseDto(brand)));
            }
            var categoryBrandMerchant = new CategoryBrandMerchant(categoryBrandMerchantRequestDto,
                    merchantBusiness.getApprovalStatus(), merchantBusiness.isActive(), userType);
            var updatedCategoriesBrandsOfMerchant =
                    categoryBrandMerchantService.updateCategoriesBrandsOfMerchant(categoryBrandMerchant,
                            new BasicMerchantBusinessResponseDto(merchantBusiness));
            log.debug("Successfully updated mapping of categories and brands for Id: {}, userType: {}. "
                            + "By userId: {} at: {}", categoryBrandMerchantRequestDto.getMerchantId(), userType, userId,
                    updatedCategoriesBrandsOfMerchant.getUpdatedAt());
            return getSuccessResponse(new BrandsAndCategoriesForMerchantResponseDto(updatedCategoriesBrandsOfMerchant,
                            basicCategoryResponseDtoSet, basicBrandResponseDtoSet, timeZone),
                    SuccessResponseStatusType.UPDATE_CATEGORIES_BRANDS_FOR_MERCHANT);
        } catch (InvalidUserException e) {
            log.error("Invalid merchant Id: {} to mapping categories and brands.", userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (InvalidCategoryException e) {
            log.error("Invalid category Id(s) to mapping categories and brands.", e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (InvalidBrandException e) {
            log.error("Invalid brand Id(s) to mapping categories and brands.", e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_BRAND_ID);
        } catch (KupiCoreException e) {
            log.error("Updating brands and categories was failed for Id: {}, userType: {}. By userId: {}",
                    categoryBrandMerchantRequestDto.getMerchantId(), userType, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method gets business profile from auth service.
     *
     * @param userType  userType
     * @param authToken authToken
     * @param userId    userId
     * @return merchant/bank business profile.
     */
    private BusinessMerchantResponseDto getBusinessProfile(UserType userType, String authToken, String userId) {
        return userType.equals(UserType.MERCHANT) ?
                authUserService.getMerchantBusinessByMerchantId(authToken, userId) :
                authUserService.getBankBusinessByBankId(authToken, userId);
    }

    /**
     * This method returns categories for multiple merchants in bulk.
     * This is an internal endpoint for location service to get categories for multiple merchants efficiently.
     *
     * @param bulkUserRequestDto list of merchant IDs
     * @return BulkMerchantCategoriesResponseDto containing map of merchant ID to categories
     */
    @PostMapping("/bulk-categories")
    public ResponseEntity<ResponseWrapper> getBulkCategoriesForMerchants(
            @PathVariable UserType userType,
            @RequestBody BulkUserRequestDto bulkUserRequestDto) {
        try {
            log.info("Bulk categories endpoint called for userType: {}, merchantIds count: {}", 
                    userType, bulkUserRequestDto != null && bulkUserRequestDto.getUserIds() != null 
                    ? bulkUserRequestDto.getUserIds().size() : 0);
            
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.warn("Unsupported user type for bulk categories: {}", userType);
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }

            if (!bulkUserRequestDto.isRequiredAvailable()) {
                log.warn("Missing required fields in bulk categories request");
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }

            if (!bulkUserRequestDto.isRequestSizeAcceptable()) {
                log.warn("Request size not acceptable for bulk categories: {}", 
                        bulkUserRequestDto.getUserIds() != null ? bulkUserRequestDto.getUserIds().size() : 0);
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }

            log.info("Calling service to get bulk categories for {} merchants", bulkUserRequestDto.getUserIds().size());
            BulkMerchantCategoriesResponseDto response = 
                    categoryBrandMerchantService.getBulkCategoriesForMerchants(bulkUserRequestDto.getUserIds());

            log.info("Successfully returned bulk categories for {} merchants. Response contains {} merchant entries", 
                    bulkUserRequestDto.getUserIds().size(),
                    response != null && response.getMerchantCategories() != null 
                    ? response.getMerchantCategories().size() : 0);

            return getSuccessResponse(response, SuccessResponseStatusType.READ_BULK_MERCHANT_CATEGORIES);
        } catch (KupiCoreException e) {
            log.error("Returning bulk categories for merchants failed", e);
            return getInternalServerError();
        } catch (Exception e) {
            log.error("Unexpected error in bulk categories endpoint", e);
            return getInternalServerError();
        }
    }

    /**
     * THis method give categories & brands mapping by merchantId
     *
     * @param userId     userId
     * @param timeZone   time zone
     * @param merchantId merchantId
     * @param request    HttpServletRequest
     * @return ResponseEntity
     */
    @GetMapping("/{merchantId}")
    public ResponseEntity<ResponseWrapper> getCategoriesAndBrandsForMerchant(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable UserType userType,
            @PathVariable String merchantId, HttpServletRequest request) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "search merchants by brand id");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            getBusinessProfile(userType, authToken, merchantId);
            var categoryMerchant = categoryBrandMerchantService.getBrandsCategoryMapperByMerchantId(merchantId);
            Set<BasicBrandResponseDto> basicBrandResponseDtoSet = new HashSet<>();
            Set<BasicCategoryResponseDto> basicCategoryResponseDtoSet = new HashSet<>();
            BrandsAndCategoriesForMerchantResponseDto brandsAndCategoriesForMerchantResponseDto = null;
            if (categoryMerchant.isPresent()) {
                var categoryList = new ArrayList<>(categoryMerchant.get().getCategories());
                var brandList = new ArrayList<>(categoryMerchant.get().getBrands());
                var categorySet = categoryService.getCategorySetByIds(categoryList);
                categorySet.forEach(category -> basicCategoryResponseDtoSet.add(
                        new BasicCategoryResponseDto(category)));
                var brandSet = brandService.getBrandSetByIdList(brandList);
                brandSet.forEach(brand -> basicBrandResponseDtoSet.add(new BasicBrandResponseDto(brand)));
                brandsAndCategoriesForMerchantResponseDto = new BrandsAndCategoriesForMerchantResponseDto(
                        categoryMerchant.get(), basicCategoryResponseDtoSet, basicBrandResponseDtoSet, timeZone);
            }
            return getSuccessResponse(brandsAndCategoriesForMerchantResponseDto,
                    SuccessResponseStatusType.READ_CATEGORIES_BRANDS_FOR_MERCHANT);
        } catch (InvalidUserException e) {
            log.error("Invalid merchant Id: {} to read map of categories and brands. userId: {}",
                    merchantId, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (KupiCoreException e) {
            log.error("Returning brands and categories was failed for merchantId: {}. userId: {}",
                    merchantId, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Get merchant list by categoryId
     *
     * @param userId userId
     * @param page   page
     * @param size   size
     * @return merchantListResponseDto
     */
    @GetMapping("/{page}/{size}/category/{categoryId}")
    public ResponseEntity<ResponseWrapper> getMerchantOrBankListByCategoryId(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String categoryId) {
        return getMerchantOrBankListByCategoryId(userId, page, size, categoryId, false);
    }

    /**
     * Get active merchant list by categoryId.
     *
     * @param userId userId
     * @param page   page
     * @param size   size
     * @return merchantListResponseDto
     */
    @GetMapping("active/{page}/{size}/category/{categoryId}")
    public ResponseEntity<ResponseWrapper> getActiveMerchantListByCategoryId(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String categoryId) {
        return getMerchantOrBankListByCategoryId(userId, page, size, categoryId, true);
    }

    /**
     * Returns merchant list according to isActiveMerchant status.
     *
     * @param userId             user id
     * @param page               page
     * @param size               size
     * @param categoryId         category id
     * @param onlyActiveMerchant merchant status True or false
     * @return merchantListResponseDto
     */
    private ResponseEntity<ResponseWrapper> getMerchantOrBankListByCategoryId(
            String userId,
            int page,
            int size,
            String categoryId,
            boolean onlyActiveMerchant
    ) {
        try {
            if (!categoryId.equals(ALL)) {
                categoryService.getCategoryById(categoryId);
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            var merchantResponseDtoPage = createMerchantResponseDtoPage(
                    pageable, categoryId, onlyActiveMerchant);
            var merchantListResponseDto = new MerchantListResponseDto(merchantResponseDtoPage);
            log.debug("Successfully return the merchant list for page: {}, size {}, userId {}", page, size, userId);
            return getSuccessResponse(merchantListResponseDto, SuccessResponseStatusType.READ_MERCHANT_LIST);
        } catch (InvalidCategoryException e) {
            log.error("Invalid category Id(s) to get merchant List by category Id.", e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            log.error("Returning merchant list was failed for page: {}, size {}, userId{}", page, size, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This Endpoint returns List Merchant By Brand ID with searchTerm.
     *
     * @param timeZone   time zone
     * @param page       page
     * @param size       size
     * @param brandId    brand id
     * @param searchTerm searchTerm/ALL
     * @return ResponseEntity
     */
    @GetMapping("/{page}/{size}/brand/{brandId}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getMerchantListByBrandId(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String brandId,
            @PathVariable String searchTerm,
            @PathVariable UserType userType) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "search merchants by brand id");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            if (!brandId.equals(ALL)) {
                brandService.validateBrandId(brandId);
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            Page<CategoryBrandMerchantIndex> categoryBrandMerchantIndex =
                    categoryBrandMerchantIndexService.getCategoryBrandMerchantIndexPageByBrandId(
                            pageable, brandId, searchTerm, userType);
            var basicMerchantResponseDtoPage =
                    createBasicMerchantResponseDtoPage(categoryBrandMerchantIndex, userType);
            var merchantListResponseDto = new MerchantListResponseDto(basicMerchantResponseDtoPage, categoryBrandMerchantIndex);
            return getSuccessResponse(merchantListResponseDto, SuccessResponseStatusType.READ_MERCHANT_LIST);
        } catch (InvalidBrandException e) {
            log.error("Invalid brand Id: {} to get merchant List by Brand Id.", brandId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_BRAND_ID);
        } catch (KupiCoreException e) {
            log.error("Returning merchant list was failed for page: {}, size {}, userId{}, brandId: {}",
                    page, size, userId, brandId, e);
            return getInternalServerError();
        }
    }

    /**
     * This Endpoint returns Merchant List By Category ID with searchTerm.
     *
     * @param userId     userId
     * @param page       page
     * @param size       size
     * @param categoryId categoryID
     * @param searchTerm searchTerm/ All
     * @param userType   MERCHANT/ BANK
     * @return ResponseEntity
     */
    @GetMapping("/{page}/{size}/category/{categoryId}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getMerchantOrBankListByCategoryId(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String categoryId,
            @PathVariable String searchTerm,
            @PathVariable UserType userType) {
        return getMerchantOrBankListByCategoryId(userId, page, size, categoryId, searchTerm,
                false, userType);
    }

    /**
     * This Endpoint returns Merchant List By Category ID with searchTerm for mobile.
     *
     * @param userId     userId
     * @param page       page
     * @param size       size
     * @param categoryId categoryID
     * @param searchTerm searchTerm/ All
     * @return ResponseEntity
     */
    @GetMapping("active/{page}/{size}/category/{categoryId}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getActiveMerchantListByCategoryIdAndSearchTerm(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable UserType userType,
            @PathVariable String categoryId,
            @PathVariable String searchTerm) {
        return getMerchantOrBankListByCategoryId(userId, page, size, categoryId, searchTerm, true,
                userType);
    }

    /**
     * This Endpoint returns active Merchant List By Category ID with searchTerm for UI, enriched with
     * slider images, categories and active deal counts.
     *
     * Reference endpoint (kept unchanged):
     * GET /api/v1/{userType}/active/{page}/{size}/category/{categoryId}/search/{searchTerm}
     *
     * New endpoint:
     * GET /api/v1/{userType}/active/{page}/{size}/category/{categoryId}/search/{searchTerm}/ui
     *
     * @param userId     userId
     * @param page       page
     * @param size       size
     * @param categoryId categoryID
     * @param searchTerm searchTerm/ All
     * @param userType   MERCHANT (banks not supported for this UI endpoint)
     * @return enriched merchant list response
     */
    @GetMapping("active/{page}/{size}/category/{categoryId}/search/{searchTerm}/MERCHANT")
    public ResponseEntity<ResponseWrapper> getActiveMerchantListByCategoryIdAndSearchTermForUi(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable UserType userType,
            @PathVariable String categoryId,
            @PathVariable String searchTerm) {
        try {
            if (userType != UserType.MERCHANT) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType,
                        "search merchants by category id for UI");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            if (!ALL.equals(categoryId)) {
                categoryService.validateCategoryId(categoryId);
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            var categoryBrandMerchantIndexPage =
                    categoryBrandMerchantIndexService.getCategoryBrandMerchantIndex(
                            pageable, categoryId, searchTerm, true, userType);

            var merchantList = createBasicMerchantResponseDtoPage(categoryBrandMerchantIndexPage, userType);
            MerchantEnrichedListResponseDto responseDto =
                    buildEnrichedMerchantListForUi(categoryId, page, size, merchantList);
            return getSuccessResponse(responseDto, SuccessResponseStatusType.READ_MERCHANT_LIST);
        } catch (InvalidCategoryException e) {
            log.error("Invalid category Id: {} to get merchant List by category Id for UI.", categoryId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            log.error("Returning enriched merchant list was failed for categoryId: {}, page: {}, size {}, userId{}",
                    categoryId, page, size, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Builds enriched merchant list for UI, including categories, slider images and active deal counts,
     * and filters out merchants without active deals.
     */
    private MerchantEnrichedListResponseDto buildEnrichedMerchantListForUi(
            String categoryId,
            int page,
            int size,
            List<MerchantBusinessResponseDto> merchantList) {

        if (merchantList == null || merchantList.isEmpty()) {
            return new MerchantEnrichedListResponseDto(new ArrayList<>(), page, size);
        }

        // Merchant IDs
        List<String> merchantIds = merchantList.stream()
                .map(MerchantBusinessResponseDto::getId)
                .collect(Collectors.toList());

        // Categories per merchant
        BulkMerchantCategoriesResponseDto bulkCategories =
                categoryBrandMerchantService.getBulkCategoriesForMerchants(merchantIds);
        Map<String, List<BasicCategoryResponseDto>> categoriesMap =
                (bulkCategories != null && bulkCategories.getMerchantCategories() != null)
                        ? bulkCategories.getMerchantCategories()
                        : new HashMap<>();

        // Slider images per merchant (from auth service) - bulk call
        Map<String, List<MerchantSliderImageItemResponseDto>> sliderImagesMap = new HashMap<>();
        try {
            BulkMerchantSliderImageListResponseDto bulkSliderImages =
                    authUserService.getSliderImagesForMerchants(merchantIds);
            if (bulkSliderImages != null && bulkSliderImages.getMerchantSliderImages() != null) {
                sliderImagesMap.putAll(bulkSliderImages.getMerchantSliderImages());
            }
        } catch (KupiCoreException e) {
            log.error("Failed to get bulk slider images for merchants", e);
        }

        // Active deal counts per merchant for the given category
        Map<String, Integer> activeDealsCountMap = new HashMap<>();
        for (String merchantId : merchantIds) {
            int count;
            if (ALL.equals(categoryId)) {
                // When category is ALL, count all active deals for the merchant
                count = summaryService.getCurrentActiveDealsCountForMerchant(merchantId);
            } else {
                // Otherwise, count only active deals that belong to the given category
                count = summaryService.getCurrentActiveDealsCountForMerchantAndCategory(merchantId, categoryId);
            }
            activeDealsCountMap.put(merchantId, count);
        }

        // Build enriched merchant list and exclude merchants with zero active deals
        List<MerchantBusinessWithCategoriesResponseDto> enrichedMerchants = new ArrayList<>();
        for (MerchantBusinessResponseDto merchant : merchantList) {
            int dealsCount = activeDealsCountMap.getOrDefault(merchant.getId(), 0);
            if (dealsCount <= 0) {
                // Skip merchants without active deals for this category
                continue;
            }
            List<BasicCategoryResponseDto> categories =
                    categoriesMap.getOrDefault(merchant.getId(), new ArrayList<>());
            MerchantBusinessWithCategoriesResponseDto enriched =
                    new MerchantBusinessWithCategoriesResponseDto(merchant, categories);
            enriched.setSliderImages(
                    sliderImagesMap.getOrDefault(merchant.getId(), new ArrayList<>()));
            enriched.setActiveDealsCount(dealsCount);
            enrichedMerchants.add(enriched);
        }

        return new MerchantEnrichedListResponseDto(enrichedMerchants, page, size);
    }

    /**
     * @param userId             user id
     * @param page               page
     * @param size               size
     * @param categoryId         category id
     * @param searchTerm         searchTerm/ All
     * @param onlyActiveMerchant true/false
     * @param userType           MERCHANT/ BANK
     * @return merchantListResponseDto
     */
    private ResponseEntity<ResponseWrapper> getMerchantOrBankListByCategoryId(
            String userId, int page,
            int size, String categoryId,
            String searchTerm, boolean onlyActiveMerchant, UserType userType) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "search merchants/banks by category id");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            if (!ALL.equals(categoryId)) {
                categoryService.validateCategoryId(categoryId);
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            var categoryBrandMerchantIndexPageByCategoryId =
                    categoryBrandMerchantIndexService.getCategoryBrandMerchantIndex(
                            pageable, categoryId, searchTerm, onlyActiveMerchant, userType);
            var basicMerchantResponseDtoPage =
                    createBasicMerchantResponseDtoPage(categoryBrandMerchantIndexPageByCategoryId, userType);
            var merchantListResponseDto = new MerchantListResponseDto(basicMerchantResponseDtoPage, categoryBrandMerchantIndexPageByCategoryId);
            return getSuccessResponse(merchantListResponseDto, SuccessResponseStatusType.READ_MERCHANT_LIST);
        } catch (InvalidCategoryException e) {
            log.error("Invalid category Id: {} to get merchant List by category Id.", categoryId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            log.error("Returning merchant list was failed for categoryId: {}, page: {}, size {}, userId{}",
                    categoryId, page, size, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This Endpoint returns Category List By merchant id with searchTerm.
     *
     * @param userId     user id
     * @param timeZone   time zone
     * @param page       page
     * @param size       size
     * @param merchantId merchant id
     * @param searchTerm search term
     * @return basicCategoryListResponseDto
     */
    @GetMapping("{merchantId}/category/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getAllCategoryListByMerchant(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable UserType userType,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String merchantId,
            @PathVariable String searchTerm, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
        return getCategoryListByMerchant(userId,
                timeZone, userType, pageable, merchantId, searchTerm, false, request);
    }

    /**
     * This Endpoint returns Category List By merchant id with searchTerm.
     *
     * @param userId     user id
     * @param timeZone   time zone
     * @param page       page
     * @param size       size
     * @param merchantId merchant id
     * @param searchTerm search term
     * @return basicCategoryListResponseDto
     */
    @GetMapping("{merchantId}/active/category/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getAllActiveCategoryListByMerchant(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable UserType userType,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String merchantId,
            @PathVariable String searchTerm, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
        return getCategoryListByMerchant(userId,
                timeZone, userType, pageable, merchantId, searchTerm, true, request);
    }

    /**
     * This method returns Category List.
     *
     * @param userId     user id
     * @param timeZone   time zone
     * @param userType   user type
     * @param merchantId merchant id
     * @param searchTerm search term
     * @param isActive   isActive
     * @param request    request
     * @return basicCategoryListResponseDto
     */
    public ResponseEntity<ResponseWrapper> getCategoryListByMerchant(
            String userId,
            String timeZone,
            UserType userType,
            Pageable pageable,
            String merchantId,
            String searchTerm,
            boolean isActive,
            HttpServletRequest request) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "search categories by merchant/bank id");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            getBusinessProfile(userType, authToken, merchantId);
            var categoryBrandMerchant = categoryBrandMerchantService
                    .getBrandsCategoryMapperByMerchantId(merchantId);
            var basicCategoryListResponseDto = new BasicCategoryListResponseDto(new PageImpl<>(new ArrayList<>()));
            if (categoryBrandMerchant.isPresent()) {
                var categorySet = categoryBrandMerchant.get().getCategories();
                var categoriesByIds = categoryBrandMerchantService
                        .getActiveCategoriesByIdsAndSearchTerm(pageable, categorySet, searchTerm, isActive);
                basicCategoryListResponseDto = new BasicCategoryListResponseDto(categoriesByIds);
            }
            return getSuccessResponse(basicCategoryListResponseDto, SuccessResponseStatusType.READ_CATEGORIES_FOR_MERCHANT);
        } catch (InvalidUserException e) {
            log.error("Invalid merchant Id: {} to get merchant's category list", merchantId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (KupiCoreException e) {
            log.error("Returning merchant's category list was failed for merchantId: {},"
                            + " page: {}, size: {}, userId: {}, time zone: {}",
                    merchantId, pageable.getPageNumber(), pageable.getPageSize(), userId, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * This Endpoint returns Brand List By merchant id with searchTerm.
     *
     * @param userId     user id
     * @param timeZone   time zone
     * @param page       page
     * @param size       size
     * @param merchantId merchant id
     * @param searchTerm search term
     * @return basicBrandListResponseDto
     */
    @GetMapping("{merchantId}/brand/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getBrandListByMerchant(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                                  @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                  @PathVariable UserType userType, @Min(DEFAULT_PAGE)
                                                                  @PathVariable int page, @Positive @Max(PAGE_MAX_SIZE)
                                                                  @PathVariable int size, @PathVariable String merchantId,
                                                                  @PathVariable String searchTerm, HttpServletRequest request) {
        try {
            if (userType != UserType.MERCHANT && userType != UserType.BANK) {
                log.debug(UNSUPPORTED_USER_TYPE_ACTION_LOG, userType, "search brands by merchant/bank id");
                return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION);
            }
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            getBusinessProfile(userType, authToken, merchantId);
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            var categoryBrandMerchant = categoryBrandMerchantService
                    .getBrandsCategoryMapperByMerchantId(merchantId);
            var basicBrandListResponseDto = new BasicBrandListResponseDto(new PageImpl<>(new ArrayList<>()));
            if (categoryBrandMerchant.isPresent()) {
                var brands = categoryBrandMerchant.get().getBrands();
                var brandsByIds = categoryBrandMerchantService.getBrandsByIds(pageable, brands, searchTerm);
                basicBrandListResponseDto = new BasicBrandListResponseDto(brandsByIds);
            }
            return getSuccessResponse(basicBrandListResponseDto, SuccessResponseStatusType.READ_BRAND_FOR_MERCHANT);
        } catch (InvalidUserException e) {
            log.error("Invalid merchant Id: {} to get merchant's brand list.", merchantId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (KupiCoreException e) {
            log.error("Returning merchant's brand list was failed for merchantId: {}," +
                    " page: {}, size: {}, userId: {}, time zone: {}", merchantId, page, size, userId, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * Get Bank List By MerchantId.
     *
     * @param userId     user id
     * @param timeZone   time zone
     * @param page       page
     * @param size       size
     * @param merchantId merchant id
     * @param searchTerm search term
     * @param request    request
     * @return Bank List.
     */
    @GetMapping("/{merchantId}/bank/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getBanksListByMerchantId(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String merchantId,
            @PathVariable String searchTerm,
            HttpServletRequest request) {
        return getBanksListByMerchantId(userId, page, size, merchantId, searchTerm, request, false);
    }

    /**
     * Get active Bank List By MerchantId.
     *
     * @param userId     user id
     * @param timeZone   time zone
     * @param page       page
     * @param size       size
     * @param merchantId merchant id
     * @param searchTerm search term
     * @param request    request
     * @return Bank List.
     */
    @GetMapping("/active/{merchantId}/bank/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getActiveBanksListByMerchantId(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String merchantId,
            @PathVariable String searchTerm,
            HttpServletRequest request) {
        return getBanksListByMerchantId(userId, page, size, merchantId, searchTerm, request, true);
    }

    /**
     * This method return bank list by merchant id, search term and isActiveBank.
     *
     * @param userId     user id
     * @param page       page
     * @param size       size
     * @param merchantId merchant id
     * @param searchTerm search term
     * @param request    request
     * @return Bank List.
     */
    public ResponseEntity<ResponseWrapper> getBanksListByMerchantId(
            String userId,
            int page,
            int size,
            String merchantId,
            String searchTerm,
            HttpServletRequest request,
            boolean onlyActiveBank) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            authUserService.getMerchantBusinessByMerchantId(authToken, merchantId);
            Pageable pageable = PageRequest.of(page, size);
            var bankDealSearchIndex = merchantBankSearchIndexService
                    .getBankMerchantSearchIndexByMerchantIdAndSearchTerm(pageable, merchantId, searchTerm, onlyActiveBank);
            var basicMerchantResponseDtoPage =
                    createBasicMerchantResponseDtoPageForMerchantOrBank(bankDealSearchIndex, UserType.BANK);
            var merchantListResponseDto = new MerchantListResponseDto(basicMerchantResponseDtoPage, bankDealSearchIndex);
            return getSuccessResponse(merchantListResponseDto, SuccessResponseStatusType.BANK_LIST);
        } catch (InvalidUserException e) {
            log.error("Invalid merchant Id: {} to get bank list.", merchantId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (KupiCoreException e) {
            log.error("Returning bank list was failed for merchant id: {}, page: {}, size: {}, user id: {}.",
                    merchantId, page, size, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Get Merchant List By BankId .
     *
     * @param userId     user id
     * @param timeZone   time zone
     * @param page       page
     * @param size       size
     * @param bankId     bank id
     * @param searchTerm search term
     * @param request    request
     * @return List of merchants
     */
    @GetMapping("/{bankId}/merchant/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getMerchantListByBankId(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                                   @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                   @Min(DEFAULT_PAGE) @PathVariable int page,
                                                                   @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
                                                                   @PathVariable String bankId,
                                                                   @PathVariable String searchTerm,
                                                                   HttpServletRequest request) {
        return getMerchantListByBankId(userId, page, size, bankId, searchTerm, request, false);
    }

    /**
     * Get active Merchant List By Bank Id.
     *
     * @param userId     user id
     * @param timeZone   time zone
     * @param page       page
     * @param size       size
     * @param bankId     bank id
     * @param searchTerm search term
     * @param request    request
     * @return List of merchants
     */
    @GetMapping("/active/{bankId}/merchant/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getActiveMerchantListByBankId(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String bankId, @PathVariable String searchTerm,
            @PathVariable("userType") String toUserType,
            HttpServletRequest request) {
        return getMerchantListByBankId(userId, page, size, bankId, searchTerm, request, true);
    }

    /**
     * Get Merchant List By BankId and isActiveMerchant.
     *
     * @param userId     user id
     * @param page       page
     * @param size       size
     * @param bankId     bank id
     * @param searchTerm search term
     * @param request    request
     * @return List of merchants
     */
    public ResponseEntity<ResponseWrapper> getMerchantListByBankId(
            String userId,
            int page,
            int size,
            String bankId,
            String searchTerm,
            HttpServletRequest request,
            boolean onlyActiveMerchant) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            authUserService.getBankBusinessByBankId(authToken, bankId);
            Pageable pageable = PageRequest.of(page, size);
            var bankDealSearchIndexPage = merchantBankSearchIndexService.
                    getBankDealSearchIndexByBankIdAndSearchTerm(pageable, bankId, searchTerm, onlyActiveMerchant);
            var basicMerchantResponseDtoPage =
                    createBasicMerchantResponseDtoPageForMerchantOrBank(bankDealSearchIndexPage,
                            UserType.MERCHANT);
            var merchantListResponseDto = new MerchantListResponseDto(basicMerchantResponseDtoPage, bankDealSearchIndexPage);
            return getSuccessResponse(merchantListResponseDto, SuccessResponseStatusType.READ_MERCHANT_LIST);
        } catch (InvalidUserException e) {
            log.error("Invalid bank Id: {} to get merchant list.", bankId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (KupiCoreException e) {
            log.error("Returning merchant list failed for bank id: {}, page: {}, size: {}, user id: {}.",
                    bankId, page, size, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method get bulk users from {@link AuthUserService} accordingly user type and create merchant response dto.
     *
     * @param bankDealSearchIndexPage bankDealSearchIndex page
     * @param userType                user type
     * @return BasicMerchantBusinessResponseDto page
     */
    private List<MerchantBusinessResponseDto> createBasicMerchantResponseDtoPageForMerchantOrBank(
            Page<BankMerchantSearchIndex> bankDealSearchIndexPage, UserType userType) {
        var merchantBankIdList = new ArrayList<String>();
        var merchantResponseDtoList = new ArrayList<MerchantBusinessResponseDto>();
        if (!bankDealSearchIndexPage.isEmpty()) {
            if (UserType.MERCHANT.equals(userType)) {
                for (var bankDealSearchIndex : bankDealSearchIndexPage) {
                    merchantBankIdList.add(bankDealSearchIndex.getMerchantId());
                }
                var bulkUserRequestDto = new BulkUserRequestDto(merchantBankIdList);
                var bulkUserResponseDto =
                        authUserService.getMerchantMap(bulkUserRequestDto, userType);
                return new ArrayList<>(bulkUserResponseDto.values());
            } else if (UserType.BANK.equals(userType)) {
                for (var bankDealSearchIndex : bankDealSearchIndexPage) {
                    merchantBankIdList.add(bankDealSearchIndex.getBankId());
                }
                var bulkUserRequestDto = new BulkUserRequestDto(merchantBankIdList);
                var bulkUserResponseDto =
                        authUserService.getMerchantMap(bulkUserRequestDto, userType);
                return new ArrayList<>(bulkUserResponseDto.values());
            } else {
                throw new UnsupportedUserTypeException("This user type: " + userType + "not allowed to do this action");
            }
        }
        return merchantResponseDtoList;
    }

    /**
     * This method get bulk users from {@link AuthUserService} and create merchant response dto.
     *
     * @param categoryId categoryId
     * @return Page<BasicMerchantBusinessResponseDto>
     */
    private Page<BasicMerchantBusinessResponseDto> createMerchantResponseDtoPage(
            Pageable pageable, String categoryId, boolean onlyActiveMerchant) {
        var merchantIdList = categoryBrandMerchantService.
                findAllMerchantsByCategoryId(pageable, categoryId, onlyActiveMerchant);
        var merchantResponseDtoList = new ArrayList<BasicMerchantBusinessResponseDto>();
        if (!merchantIdList.isEmpty()) {
            var bulkUserRequestDto = new BulkUserRequestDto(merchantIdList);
            var bulkUserResponseDto =
                    authUserService.getMerchantMap(bulkUserRequestDto, UserType.MERCHANT);
            merchantResponseDtoList.addAll(bulkUserResponseDto.values());
        }
        return new PageImpl<>(merchantResponseDtoList);
    }

    /**
     * This method get bulk users from {@link AuthUserService} and create merchant response dto for given
     * CategoryBrandMerchantIndex List
     *
     * @param categoryBrandMerchantIndexes categoryBrandMerchantIndex List
     * @param userType                     MERCHANT/BANK
     * @return BasicMerchantResponseDto Page
     */
    private List<MerchantBusinessResponseDto> createBasicMerchantResponseDtoPage(
            Page<CategoryBrandMerchantIndex> categoryBrandMerchantIndexes, UserType userType) {
        var merchantIdList = new ArrayList<String>();
        var merchantResponseDtoList = new ArrayList<MerchantBusinessResponseDto>();
        if (!categoryBrandMerchantIndexes.isEmpty()) {
            for (var categoryBrandMerchantIndex : categoryBrandMerchantIndexes) {
                merchantIdList.add(categoryBrandMerchantIndex.getMerchantId());
            }
            var bulkUserRequestDto = new BulkUserRequestDto(merchantIdList);
            var bulkUserResponseDto = authUserService.getMerchantMap(
                    bulkUserRequestDto, userType);
            return new ArrayList<>(bulkUserResponseDto.values());
        } else {
            return merchantResponseDtoList;
        }
    }
}
