package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Brand;
import com.kupi.cc.base.domain.entity.Category;
import com.kupi.cc.base.domain.entity.CategoryBrandMerchant;
import com.kupi.cc.base.domain.entity.CategoryBrandMerchantIndex;
import com.kupi.cc.base.domain.response.BasicCategoryResponseDto;
import com.kupi.cc.base.domain.response.BasicMerchantBusinessResponseDto;
import com.kupi.cc.base.domain.response.BulkMerchantCategoriesResponseDto;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.InvalidBrandException;
import com.kupi.cc.base.exception.InvalidCategoryException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.CategoryBrandMerchantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Categories for merchant service
 */
@Slf4j
@Service
public class CategoryBrandMerchantService {

    private static final String ALL = "ALL";
    private static final String READ_BRANDS_CATEGORIES = "Reading brandsCategoriesForMerchant from the database" +
            " was failed.";
    private final CategoryBrandMerchantRepository categoryBrandMerchantRepository;
    private final CategoryBrandMerchantIndexService categoryBrandMerchantIndexService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    @Autowired
    public CategoryBrandMerchantService(CategoryBrandMerchantRepository categoryBrandMerchantRepository,
                                        CategoryBrandMerchantIndexService categoryBrandMerchantIndexService,
                                        CategoryService categoryService, BrandService brandService) {
        this.categoryBrandMerchantRepository = categoryBrandMerchantRepository;
        this.categoryBrandMerchantIndexService = categoryBrandMerchantIndexService;
        this.categoryService = categoryService;
        this.brandService = brandService;
    }


    /**
     * This method create brands and categories mapping for merchant
     *
     * @param categoryBrandMerchant            categoryBrandMerchant
     * @param basicMerchantBusinessResponseDto basicMerchantBusinessResponseDto
     * @return CategoryBrandMerchant
     */
    public CategoryBrandMerchant createCategoriesForMerchant(CategoryBrandMerchant categoryBrandMerchant,
                                                             BasicMerchantBusinessResponseDto basicMerchantBusinessResponseDto) {
        try {
            categoryBrandMerchantIndexService.save(new CategoryBrandMerchantIndex(categoryBrandMerchant,
                    basicMerchantBusinessResponseDto));
            categoryBrandMerchant.setActiveMerchant(basicMerchantBusinessResponseDto.isActive());
            return categoryBrandMerchantRepository.save(categoryBrandMerchant);

        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving category merchant into database was failed.", e);
        }
    }

    /**
     * This method returns CategoryBrandMerchant by merchant id.
     *
     * @param merchantId merchantId
     * @return optional CategoryBrandMerchant
     */
    public Optional<CategoryBrandMerchant> getBrandsCategoryMapperByMerchantId(String merchantId) {
        try {
            return categoryBrandMerchantRepository.findByMerchantId(merchantId);
        } catch (DataAccessException e) {
            throw new KupiCoreException(READ_BRANDS_CATEGORIES, e);
        }
    }

    /**
     * This method return merchants Id list.
     *
     * @param categoryId         categoryId
     * @param onlyActiveMerchant onlyActiveMerchant ture or false
     * @return MerchantListResponseDto
     */
    public List<String> findAllMerchantsByCategoryId(
            Pageable pageable,
            String categoryId,
            boolean onlyActiveMerchant) {
        try {
            Page<CategoryBrandMerchant> categoryMerchants = null;

            if (onlyActiveMerchant) {
                categoryMerchants = categoryId.equals(ALL) ?
                        categoryBrandMerchantRepository.findAllByIsActiveMerchant(pageable, true) :
                        categoryBrandMerchantRepository.findAllByCategoriesAndIsActiveMerchant(pageable, categoryId, true);
            } else {
                categoryMerchants = categoryId.equals(ALL) ?
                        categoryBrandMerchantRepository.findAll(pageable) :
                        categoryBrandMerchantRepository.findAllByCategories(pageable, categoryId);
            }

            List<String> merchantIds = new ArrayList<>();
            categoryMerchants.forEach(categoryBrandMerchant -> merchantIds.add(categoryBrandMerchant.getMerchantId()));
            return merchantIds;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading merchantIds from database was failed.", e);
        }
    }

    /**
     * Update CategoriesBrandsOfMerchant and CategoriesBrandsOfMerchantIndex
     *
     * @param categoryBrandMerchant categoryBrandMerchant
     */
    public CategoryBrandMerchant updateCategoriesBrandsOfMerchant(CategoryBrandMerchant categoryBrandMerchant,
                                                                  BasicMerchantBusinessResponseDto
                                                                          basicMerchantBusinessResponseDto) {
        try {

            Optional<CategoryBrandMerchant> optionalCategoryMerchant =
                    categoryBrandMerchantRepository.findByMerchantId(categoryBrandMerchant.getMerchantId());
            if (optionalCategoryMerchant.isPresent()) {
                var categoryBrandMerchantFromDb = optionalCategoryMerchant.get();
                categoryBrandMerchantFromDb.getBrands().clear();
                categoryBrandMerchantFromDb.getCategories().clear();
                categoryBrandMerchantFromDb.getBrands().addAll(categoryBrandMerchant.getBrands());
                categoryBrandMerchantFromDb.getCategories().addAll(categoryBrandMerchant.getCategories());
                categoryBrandMerchantFromDb.setUpdatedAt(System.currentTimeMillis());
                categoryBrandMerchantFromDb
                        .setActiveMerchant(basicMerchantBusinessResponseDto.isActive());
                categoryBrandMerchantIndexService.updateCategoryBrandMerchantIndexByMerchantId(
                        categoryBrandMerchant.getMerchantId(),
                        categoryBrandMerchant.getBrands().toString(),
                        categoryBrandMerchant.getCategories().toString(),
                        basicMerchantBusinessResponseDto.isActive(),
                        basicMerchantBusinessResponseDto.getApprovalStatus());
                return categoryBrandMerchantRepository.save(categoryBrandMerchantFromDb);
            } else {
                return createCategoriesForMerchant(categoryBrandMerchant, basicMerchantBusinessResponseDto);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Updating Categories & brands of merchant in database was failed.", e);
        }
    }

    /**
     * This method return number of merchants for specific brand
     *
     * @param brandId  brandId
     * @param userType BANK/MERCHANT
     * @return count merchants Count By BrandId
     */
    public long getMerchantCountByBrandIdAndUserType(String brandId, UserType userType) {
        try {
            return categoryBrandMerchantRepository.countDistinctMerchantIdByBrandsAndUserType(brandId, userType);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading merchant count from the database by Brand id was failed", e);
        }

    }


    /**
     * This method returns count of merchant for specific category id
     *
     * @param categoryId category id
     * @param userType   BANK/MERCHANT
     * @return count merchants Count By Category id
     */
    public long getMerchantCountByCategoryIdAndUserType(String categoryId, UserType userType) {

        try {
            return categoryBrandMerchantRepository.countDistinctMerchantIdByCategoriesAndUserType(categoryId, userType);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading merchant count from the database by category id was failed", e);
        }
    }

    /**
     * This method will return active merchant count for category id.
     *
     * @param categoryId category id
     * @param userType   BANK/MERCHANT
     * @return active merchant count
     */
    public long getActiveMerchantCountByCategoryIdAndUserType(String categoryId, UserType userType) {
        try {
            return categoryBrandMerchantRepository
                    .countDistinctMerchantIdByCategoriesAndIsActiveMerchantTrueAndUserType(categoryId, userType);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading active merchant count from the database by category id was failed", e);
        }
    }

    /**
     * This method will return active merchant count for brand id.
     *
     * @param brandId  brand id
     * @param userType BANK/MERCHANT
     * @return active merchant count
     */
    public long getActiveMerchantCountByBrandIdAndUserType(String brandId, UserType userType) {
        try {
            return categoryBrandMerchantRepository
                    .countDistinctMerchantIdByBrandsAndIsActiveMerchantTrueAndUserType(brandId, userType);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading active merchant count from the database by brand id was failed", e);
        }
    }

    /**
     * This method returns category page by category id set based on isActive flag.
     *
     * @param pageable   pageable
     * @param ids        ids
     * @param searchTerm search term
     * @return Category page
     */
    public Page<Category> getActiveCategoriesByIdsAndSearchTerm(Pageable pageable,
                                                                Set<String> ids, String searchTerm, boolean isActive) {
        return (isActive) ? categoryService.getBulkActiveCategoriesByIds(pageable, ids, searchTerm)
                : categoryService.getBulkCategoriesByIds(pageable, ids, searchTerm);
    }

    /**
     * This method returns Brand page by category id set.
     *
     * @param pageable   pageable
     * @param ids        ids
     * @param searchTerm search term
     * @return Brand Page
     */
    public Page<Brand> getBrandsByIds(Pageable pageable, Set<String> ids, String searchTerm) {
        return brandService.getBulkBrandsByIds(pageable, ids, searchTerm);
    }

    /**
     * This method returns true when brand id mapped with categoryBrandMerchant.
     *
     * @param brandId brand id
     * @return true/ false
     */
    public boolean checkBrandIdMapWithCategoryBrandMerchant(String brandId) {
        try {
            return categoryBrandMerchantRepository.existsByBrands(brandId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Checking brand id is associated with categoryBrandMerchant" +
                    " from the database was failed", e);
        }
    }

    /**
     * This method returns true if specific category associate with categoryBrandMerchant.
     *
     * @param categoryId category id
     * @return true/ false
     */
    public boolean checkCategoryIdMapWithCategoryBrandMerchant(String categoryId) {
        try {
            return categoryBrandMerchantRepository.existsAllByCategories(categoryId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Checking category id is associated with categoryBrandMerchant" +
                    " from the database was failed", e);
        }
    }

    /**
     * This method will update merchant info.
     *
     * @param merchantMap merchant id, MerchantBusinessResponseDto Map
     */
    public void updateMerchantInfo(Map<String, MerchantBusinessResponseDto> merchantMap) {
        try {
            var categoryBrandMerchantList =
                    categoryBrandMerchantRepository.getAllByMerchantIdIn(new ArrayList<>(merchantMap.keySet()));
            for (var categoryBrandMerchant : categoryBrandMerchantList) {
                categoryBrandMerchant.setMerchantApprovalStatus(merchantMap
                        .get(categoryBrandMerchant.getMerchantId()).getApprovalStatus());
                categoryBrandMerchant.setActiveMerchant(merchantMap.get(categoryBrandMerchant.getMerchantId()).isActive());
            }
            categoryBrandMerchantRepository.saveAll(categoryBrandMerchantList);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Updating merchant info for categoryBrandMerchant was failed", e);
        }
    }

    /**
     * This method returns categories for multiple merchants in bulk.
     *
     * @param merchantIds list of merchant IDs
     * @return BulkMerchantCategoriesResponseDto containing map of merchant ID to categories
     */
    public BulkMerchantCategoriesResponseDto getBulkCategoriesForMerchants(List<String> merchantIds) {
        try {
            if (merchantIds == null || merchantIds.isEmpty()) {
                log.debug("Empty merchant IDs list provided for bulk categories");
                return new BulkMerchantCategoriesResponseDto(new HashMap<>());
            }

            log.debug("Getting bulk categories for {} merchants", merchantIds.size());

            // Get all CategoryBrandMerchant records for the given merchant IDs
            List<CategoryBrandMerchant> categoryBrandMerchants = 
                    categoryBrandMerchantRepository.getAllByMerchantIdIn(merchantIds);
            
            log.debug("Found {} CategoryBrandMerchant records", categoryBrandMerchants.size());

            // Collect all unique category IDs
            Set<String> allCategoryIds = new HashSet<>();
            for (CategoryBrandMerchant cbm : categoryBrandMerchants) {
                if (cbm.getCategories() != null) {
                    allCategoryIds.addAll(cbm.getCategories());
                }
            }

            // Get all Category entities - handle missing categories gracefully
            Set<Category> allCategories = new HashSet<>();
            if (!allCategoryIds.isEmpty()) {
                // Fetch categories one by one to handle missing ones gracefully
                for (String categoryId : allCategoryIds) {
                    try {
                        Category category = categoryService.getCategoryById(categoryId);
                        if (category != null) {
                            allCategories.add(category);
                        }
                    } catch (Exception e) {
                        // Log but continue - some categories might be deleted or invalid
                        log.warn("Category not found or error fetching category: {}", categoryId, e);
                    }
                }
            }

            // Create a map of category ID to BasicCategoryResponseDto
            Map<String, BasicCategoryResponseDto> categoryMap = new HashMap<>();
            for (Category category : allCategories) {
                categoryMap.put(category.getId(), new BasicCategoryResponseDto(category));
            }

            // Build the result map: merchantId -> List<BasicCategoryResponseDto>
            Map<String, List<BasicCategoryResponseDto>> merchantCategoriesMap = new HashMap<>();
            
            // Initialize all merchant IDs with empty lists
            for (String merchantId : merchantIds) {
                merchantCategoriesMap.put(merchantId, new ArrayList<>());
            }

            // Populate categories for merchants that have them
            for (CategoryBrandMerchant cbm : categoryBrandMerchants) {
                String merchantId = cbm.getMerchantId();
                List<BasicCategoryResponseDto> categories = new ArrayList<>();
                
                if (cbm.getCategories() != null && !cbm.getCategories().isEmpty()) {
                    log.debug("Merchant {} has {} category IDs", merchantId, cbm.getCategories().size());
                    for (String categoryId : cbm.getCategories()) {
                        BasicCategoryResponseDto categoryDto = categoryMap.get(categoryId);
                        if (categoryDto != null) {
                            categories.add(categoryDto);
                        } else {
                            log.warn("Category {} not found in categoryMap for merchant {}", categoryId, merchantId);
                        }
                    }
                    log.debug("Merchant {} mapped to {} categories", merchantId, categories.size());
                } else {
                    log.debug("Merchant {} has no categories", merchantId);
                }
                
                merchantCategoriesMap.put(merchantId, categories);
            }

            log.debug("Returning bulk categories for {} merchants", merchantCategoriesMap.size());
            return new BulkMerchantCategoriesResponseDto(merchantCategoriesMap);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Getting bulk categories for merchants from database was failed", e);
        }
    }
}
