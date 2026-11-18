package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.Category;
import com.kupi.cc.base.domain.request.CategoryRequestDto;
import com.kupi.cc.base.domain.request.CategoryUpdateRequestDto;
import com.kupi.cc.base.domain.response.*;
import com.kupi.cc.base.enums.CategoryType;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.service.CategoryBrandMerchantService;
import com.kupi.cc.base.service.CategoryService;
import com.kupi.cc.base.service.DealSearchService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.Set;

import static com.kupi.cc.base.enums.SuccessResponseStatusType.CATEGORY_DELETE_SUCCESSFUL;

/**
 * Category Controller
 */
@Slf4j
@RestController
@Validated
@RequestMapping("api/v1/categories")
public class CategoryController extends Controller {

    private final CategoryService categoryService;
    private final CategoryBrandMerchantService categoryBrandMerchantService;
    private final DealSearchService dealSearchService;


    Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    public CategoryController(CategoryService categoryService, CategoryBrandMerchantService categoryBrandMerchantService,
                              Translator translator, DealSearchService dealSearchService) {
        super(translator);
        this.categoryService = categoryService;
        this.categoryBrandMerchantService = categoryBrandMerchantService;
        this.dealSearchService = dealSearchService;
    }

    /**
     * Get paginated category list
     *
     * @return category List Response
     */
    @GetMapping("/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getCategoryList(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String searchTerm
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            Page<Category> categoryPage = categoryService.listAllCategories(pageable, searchTerm);
            var categoryListResponseDto = new CategoryListResponseDto(categoryPage, timeZone);
            logger.debug("Successfully return the category list for userId: {}, page: {}, size {}",
                    userId, page, size);
            return getSuccessResponse(categoryListResponseDto, SuccessResponseStatusType.READ_CATEGORY_LIST);
        } catch (KupiCoreException e) {
            logger.error("Returning category list was failed for userId: {}, page: {}, size {}",
                    userId, page, size, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get active category list.
     *
     * @param userId     userId
     * @param page       page
     * @param timeZone   timeZone
     * @param size       size
     * @param searchTerm searchTerm
     * @return active category list
     */
    @GetMapping("/active/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getActiveCategoryList(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String searchTerm
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            Page<Category> categoryPage = categoryService.getActiveCategories(pageable, searchTerm);
            var categoryListResponseDto = new CategoryListResponseDto(categoryPage, timeZone);
            logger.debug("Successfully returned active category list for userId: {}, page: {}, size {}",
                    userId, page, size);
            return getSuccessResponse(categoryListResponseDto, SuccessResponseStatusType.READ_CATEGORY_LIST);
        } catch (KupiCoreException e) {
            logger.error("Returning active category list was failed for userId: {}, page: {}, size {}",
                    userId, page, size, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get active category list for mobile users.
     *
     * @param userId     userId
     * @param page       page
     * @param timeZone   timeZone
     * @param size       size
     * @param searchTerm searchTerm
     * @return active category list.
     */
    @GetMapping("/active/user/{userId}/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getActiveCategoryListForMobileUsers(
            @PathVariable String userId,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String searchTerm
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            Page<Category> categoryPage = categoryService.getActiveCategories(pageable, searchTerm);
            Set<String> favouriteCategoryIds = categoryService.getFavouriteCategoryIdList(userId);
            FavouriteCategoryListResponseDto favouriteCategoryListResponseDto =
                    new FavouriteCategoryListResponseDto(categoryPage, favouriteCategoryIds, timeZone);
            logger.debug("Successfully returned active category list for userId: {}, page: {}, size {}",
                    userId, page, size);
            return getSuccessResponse(favouriteCategoryListResponseDto, SuccessResponseStatusType.READ_CATEGORY_LIST);
        } catch (KupiCoreException e) {
            logger.error("Returning active category list was failed for userId: {}, page: {}, size {}",
                    userId, page, size, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get expired category list.
     *
     * @param userId     userId
     * @param page       page
     * @param timeZone   timeZone
     * @param size       size
     * @param searchTerm searchTerm
     * @return active category list
     */
    @GetMapping("/expired/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> getExpiredCategoryList(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String searchTerm
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            Page<Category> categoryPage = categoryService.getExpiredCategories(pageable, searchTerm);
            var categoryListResponseDto = new CategoryListResponseDto(categoryPage, timeZone);
            logger.debug("Successfully returned expired category list for userId: {}, page: {}, size {}",
                    userId, page, size);
            return getSuccessResponse(categoryListResponseDto, SuccessResponseStatusType.READ_EXPIRED_CATEGORY_LIST);
        } catch (KupiCoreException e) {
            logger.error("Returning expired category list was failed for userId: {}, page: {}, size {}",
                    userId, page, size, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get category list by category type.
     *
     * @param userId       userId
     * @param page         page
     * @param timeZone     timeZone
     * @param categoryType NORMAL/SEASONAL
     * @param size         size
     * @param searchTerm   searchTerm
     * @param excludeId    categoryId
     * @return list of categories for category type.
     */
    @GetMapping(value = {"/{categoryType}/{page}/{size}/search/{searchTerm}/{excludeId}",
            "/{categoryType}/{page}/{size}/search/{searchTerm}"})
    public ResponseEntity<ResponseWrapper> getCategoryListByType(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable CategoryType categoryType,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @PathVariable String searchTerm,
            @PathVariable(required = false) String excludeId) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            Page<Category> categoryPage =
                    categoryService.getCategoriesForCategoryType(categoryType, pageable, searchTerm, excludeId);
            var categoryListResponseDto = new CategoryListResponseDto(categoryPage, timeZone);
            logger.debug("Successfully return the category list for userId: {}, page: {}, size {}",
                    userId, page, size);
            return getSuccessResponse(categoryListResponseDto, SuccessResponseStatusType.READ_CATEGORY_LIST);
        } catch (KupiCoreException e) {
            logger.error("Returning category list was failed for userId: {}, page: {}, size {}",
                    userId, page, size, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get popular category list for mobile user.
     *
     * @param userId   userId
     * @param page     page
     * @param timeZone timeZone
     * @param size     size
     * @return list of categories.
     */
    @GetMapping("/popular/{page}/{size}")
    public ResponseEntity<ResponseWrapper> getPopularCategoryListForMobileUser(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).ascending());
            Page<Category> categoryPage = categoryService.listAllPopularCategories(pageable);
            Set<String> favouriteCategoryIds = categoryService.getFavouriteCategoryIdList(userId);
            FavouriteCategoryListResponseDto favouriteCategoryListResponseDto =
                    new FavouriteCategoryListResponseDto(categoryPage, favouriteCategoryIds, timeZone);
            logger.debug("Successfully return the popular category list for userId: {}, page: {}, size {}",
                    userId, page, size);
            return getSuccessResponse(favouriteCategoryListResponseDto, SuccessResponseStatusType.READ_CATEGORY_LIST);
        } catch (KupiCoreException e) {
            logger.error("Returning popular category list was failed for userId: {}, page: {}, size {}",
                    userId, page, size, e);
            return getInternalServerError();
        }
    }


    /**
     * Get paginated basic category list
     *
     * @return basic category List Response
     */
    @GetMapping("/basic/{page}/{size}")
    public ResponseEntity<ResponseWrapper> getBasicCategoryList(
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            Page<Category> categoryPage = categoryService.listAllCategories(pageable, ALL);
            var basicCategoryListResponseDto = new BasicCategoryListResponseDto(categoryPage);
            logger.debug("Successfully return the basic category list for page: {}, size {}", page, size);
            return getSuccessResponse(basicCategoryListResponseDto, SuccessResponseStatusType.READ_CATEGORY_LIST);
        } catch (KupiCoreException e) {
            logger.error("Returning basic category list was failed for page: {}, size {}", page, size, e);
            return getInternalServerError();
        }
    }

    /**
     * create new category
     *
     * @param categoryRequestDto categoryRequestDto
     * @return CategoryCreateResponseDto
     */
    @Secured({ADMIN_ROLE})
    @PostMapping("")
    public ResponseEntity<ResponseWrapper> createCategory(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestBody CategoryRequestDto categoryRequestDto) {
        try {
            if (!categoryRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            if (categoryRequestDto.getCategoryType().equals(CategoryType.SEASONAL) &&
                    !categoryRequestDto.isValidDateRange(timeZone)) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_START_END_DATE);
            }
            var relatedCategorySet = categoryService.
                    getCategorySetByIds(categoryRequestDto.getRelatedCategories());
            var category = new Category(categoryRequestDto, relatedCategorySet);
            categoryService.createCategory(category);
            var categoryCreateResponseDto = new CategoryCreateResponseDto(category, timeZone);
            String objectToJson = categoryCreateResponseDto.toLogJson();
            logger.debug("Successfully create category. userId: {}, category response {}",
                    userId, objectToJson);
            return getSuccessResponse(categoryCreateResponseDto, SuccessResponseStatusType.CREATE_CATEGORY);
        } catch (ExistingNameException e) {
            logger.error("Existing category category name :{} for create category.", categoryRequestDto.getName(), e);
            return getErrorResponse(ErrorResponseStatusType.EXISTING_CATEGORY_NAME);
        } catch (InvalidCategoryException e) {
            logger.error("Invalid category Id(s) for related categories. userId: {}", userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            logger.error("Creating new category was failed. userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Get Category Detail by CategoryId
     *
     * @param userId     userid
     * @param timeZone   time zone
     * @param categoryId category id
     * @return categoryResponseDto
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<ResponseWrapper> getDetailCategory(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                             @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                             @PathVariable String categoryId) {
        try {
            var category = categoryService.getCategoryById(categoryId);
            logger.info("brand details: {}", category);
            long dealsCount = dealSearchService.getDealsCountByCategoryId(categoryId);
            long activeDealsCount = dealSearchService.getActiveDealsCountByCategoryId(categoryId);
            long merchantCount = categoryBrandMerchantService
                    .getMerchantCountByCategoryIdAndUserType(categoryId, UserType.MERCHANT);
            long activeMerchantCount = categoryBrandMerchantService
                    .getActiveMerchantCountByCategoryIdAndUserType(categoryId, UserType.MERCHANT);
            long bankCount = categoryBrandMerchantService
                    .getMerchantCountByCategoryIdAndUserType(categoryId, UserType.BANK);
            long activeBankCount = categoryBrandMerchantService
                    .getActiveMerchantCountByCategoryIdAndUserType(categoryId, UserType.BANK);
            var categoryDetailResponseDto =
                    new CategoryDetailResponseDto(category,
                            dealsCount, merchantCount, timeZone, activeMerchantCount, activeDealsCount);
            categoryDetailResponseDto.setNoOfBanks(bankCount);
            categoryDetailResponseDto.setNoOfActiveBanks(activeBankCount);
            return getSuccessResponse(categoryDetailResponseDto, SuccessResponseStatusType.READ_CATEGORY_DETAIL);
        } catch (InvalidCategoryException e) {
            logger.error("Invalid categoryId: {} to request category detail for user Id: {}", categoryId, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            logger.error("Failed to return category detail by category id: {} userId: {}", categoryId, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Update Category.
     *
     * @param userId                   user id
     * @param timeZone                 time zone
     * @param categoryUpdateRequestDto categoryUpdateRequestDto
     * @return updated category.
     */
    @Secured({ADMIN_ROLE})
    @PutMapping("")
    public ResponseEntity<ResponseWrapper> updateCategory(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestBody CategoryUpdateRequestDto categoryUpdateRequestDto) {
        try {
            if (!categoryUpdateRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            if (categoryUpdateRequestDto.getCategoryType().equals(CategoryType.SEASONAL) &&
                    !categoryUpdateRequestDto.isValidDateRangeForUpdateCategory()) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_START_END_DATE);
            }
            var updateCategory = categoryService.updateCategory(categoryUpdateRequestDto);
            var categoryUpdateResponseDto = new CategoryCreateResponseDto(updateCategory, timeZone);
            String objectToJson = categoryUpdateResponseDto.toLogJson();
            logger.debug("Successfully update the category. userId: {}, category response {}",
                    userId, objectToJson);
            return getSuccessResponse(categoryUpdateResponseDto, SuccessResponseStatusType.UPDATE_CATEGORY);
        } catch (ExistingNameException e) {
            logger.error("Existing category category name :{} for update category.", categoryUpdateRequestDto.getName(), e);
            return getErrorResponse(ErrorResponseStatusType.EXISTING_CATEGORY_NAME);
        } catch (InvalidCategoryException e) {
            logger.error("Invalid category Id(s) to get related categories for userId: {}", userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            logger.error("Updating existing category by categoryId: {} and userId: {} was failed.", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Delete category permanently when category is not map with categoryBrandMerchant.
     *
     * @param userId     user id
     * @param timeZone   time zone
     * @param categoryId category id
     * @return SuccessResponse / ErrorResponse
     */
    @DeleteMapping("/delete/{categoryId}")
    public ResponseEntity<ResponseWrapper> deleteCategory(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable String categoryId) {
        try {
            var isCategoryMapWithCategoryBrandMerchant = categoryBrandMerchantService
                    .checkCategoryIdMapWithCategoryBrandMerchant(categoryId);
            categoryService.deleteCategory(categoryId, isCategoryMapWithCategoryBrandMerchant);
            return getSuccessResponse(null, CATEGORY_DELETE_SUCCESSFUL);
        } catch (InvalidCategoryException e) {
            logger.error("Invalid categoryId :{} to delete.", categoryId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (UnsupportedDeleteAction e) {
            logger.error("Category cannot be deleted by category id: {}" +
                    " because it is already mapped with merchants or another category", categoryId, e);
            return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_CATEGORY_DELETE);
        } catch (KupiCoreException e) {
            logger.error("Deleting category by category id was failed for category id : {}, user id :{}.", categoryId, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Get related categories by category id.
     *
     * @param page       page
     * @param size       size
     * @param userId     user id
     * @param timeZone   time zone
     * @param categoryId category id
     * @return BasicCategoryListResponseDto
     */
    @GetMapping("/related/{categoryId}/{page}/{size}")
    public ResponseEntity<ResponseWrapper> getRelatedCategoryList(
            @Min(DEFAULT_PAGE) @PathVariable int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable String categoryId) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            var relatedCategoryListForCategory =
                    categoryService.getRelatedCategoryListForCategory(pageable, categoryId);
            var basicCategoryListResponseDto = new BasicCategoryListResponseDto(relatedCategoryListForCategory);
            return getSuccessResponse(basicCategoryListResponseDto, SuccessResponseStatusType.READ_RELATED_CATEGORIES);
        } catch (UnsupportedCategoryTypeException e) {
            logger.error("Unsupported category type.", e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_FOR_RELATED_CATEGORIES);
        } catch (InvalidCategoryException e) {
            logger.error("Invalid category Id: {} for get related categories for userId: {}", categoryId, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            logger.error("Returning Related categories" +
                    " list was failed for category id: {}, page: {}, size {}", categoryId, page, size, e);
            return getInternalServerError();
        }
    }

    /**
     * This method adds favourite category for a user.
     *
     * @param userId     userId
     * @param categoryId categoryId
     * @return SuccessResponse / ErrorResponse.
     */
    @PostMapping("/{categoryId}/favourite")
    public ResponseEntity<ResponseWrapper> addFavouriteCategory(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                                @PathVariable String categoryId) {
        try {
            categoryService.addFavouriteCategory(userId, categoryId);
            logger.debug("Successfully added favourite category: {} for userId: {}.", categoryId, userId);
            return getSuccessResponse(null, SuccessResponseStatusType.FAVOURITE_CATEGORY_ADDED);
        } catch (InvalidCategoryException e) {
            logger.error("Invalid category Id: {} to add favourite. UserId: {}", categoryId, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            logger.error("Failed to add favourite category: {} for userId: {}.", categoryId, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method removes a category from favourite list for user.
     *
     * @param userId     userId
     * @param categoryId categoryId
     * @return SuccessResponse / ErrorResponse
     */
    @PutMapping("/{categoryId}/favourite/remove")
    public ResponseEntity<ResponseWrapper> removeFavouriteCategory(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                                   @PathVariable String categoryId) {
        try {
            categoryService.removeFavouriteCategory(userId, categoryId);
            logger.debug("Successfully removed favourite category: {} for userId: {}.", categoryId, userId);
            return getSuccessResponse(null, SuccessResponseStatusType.FAVOURITE_CATEGORY_REMOVED);
        } catch (KupiCoreException e) {
            logger.error("Failed to remove favourite category: {} for userId: {}.", categoryId, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns favourite category list for a user.
     *
     * @param userId   userId
     * @param timeZone timeZone
     * @param page     page
     * @param size     size
     * @return favourite category list.
     */
    @GetMapping("/favourite/{page}/{size}")
    public ResponseEntity<ResponseWrapper> getFavouriteCategoryList(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                                    @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                    @PathVariable int page, @PathVariable int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).ascending());
            Page<Category> categoryPage = categoryService.getFavouriteCategory(userId, pageable);
            FavouriteCategoryListResponseDto favouriteCategoryListResponseDto =
                    new FavouriteCategoryListResponseDto(categoryPage, timeZone);
            logger.debug("Successfully returned favourite category list for userId: {}.", userId);
            return getSuccessResponse(favouriteCategoryListResponseDto, SuccessResponseStatusType.READ_CATEGORY_LIST);
        } catch (KupiCoreException e) {
            logger.error("Failed to get favourite category list for userId: {}.", userId, e);
            return getInternalServerError();
        }
    }
}
