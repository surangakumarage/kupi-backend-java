package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.CategoryBrandMerchantIndex;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.InvalidUserException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.CategoryBrandMerchantIndexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

/**
 * This class should replace from a search engine.
 */
@Service
public class CategoryBrandMerchantIndexService {

    private static final String INVALID_MERCHANT_ID = "Invalid merchant Id: ";
    private static final String ALL = "ALL";
    private final CategoryBrandMerchantIndexRepository categoryBrandMerchantIndexRepository;

    @Autowired
    public CategoryBrandMerchantIndexService(CategoryBrandMerchantIndexRepository categoryBrandMerchantIndexRepository) {
        this.categoryBrandMerchantIndexRepository = categoryBrandMerchantIndexRepository;
    }


    /**
     * Create categoryBrandMerchantIndex
     *
     * @param categoryBrandMerchantIndex categoryBrandMerchantIndex
     */
    public void save(CategoryBrandMerchantIndex categoryBrandMerchantIndex) {
        try {
            categoryBrandMerchantIndexRepository.save(categoryBrandMerchantIndex);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving categoryBrandMerchantIndex to database was failed", e);
        }
    }


    /**
     * This method returns categoryBrandMerchantIndex page by category id, brand id, search term and checking merchant
     * active status.
     *
     * @param pageable           pageable
     * @param categoryId         category id
     * @param searchTerm         search term
     * @param onlyActiveMerchant merchant status : true/ false
     * @param userType           MERCHANT/ BANK
     * @return CategoryBrandMerchantIndex page
     */
    public Page<CategoryBrandMerchantIndex> getCategoryBrandMerchantIndex(Pageable pageable, String categoryId,
                                                                          String searchTerm, boolean onlyActiveMerchant,
                                                                          UserType userType) {
        return (onlyActiveMerchant) ?
                getCategoryBrandMerchantIndexPageByCategoryIdForActiveMerchantOrBankOnly(pageable, categoryId, searchTerm, userType) :
                getCategoryBrandMerchantIndexPageByCategoryId(pageable, categoryId, searchTerm, userType);
    }

    /**
     * This method return CategoryBrandMerchantIndex page filtering by brand Id and merchant name
     *
     * @param pageable   pageable
     * @param categoryId categoryId
     * @param searchTerm searchTerm/ ALL
     * @param userType   MERCHANT/ BANK
     * @return CategoryBrandMerchantIndex page
     */
    private Page<CategoryBrandMerchantIndex> getCategoryBrandMerchantIndexPageByCategoryId(
            Pageable pageable, String categoryId, String searchTerm, UserType userType) {
        try {
            if (!ALL.equals(categoryId) && !ALL.equals(searchTerm)) {
                return categoryBrandMerchantIndexRepository
                        .findAllByCategoryIdAndSearchTerm(pageable, categoryId, searchTerm, userType);
            } else if (!categoryId.equals(ALL) && searchTerm.equals(ALL)) {
                return categoryBrandMerchantIndexRepository.findAllByCategoryId(pageable, categoryId, userType);
            } else if (categoryId.equals(ALL) && !searchTerm.equals(ALL)) {
                return categoryBrandMerchantIndexRepository.findAllBySearch(pageable, searchTerm, userType);
            } else {
                return categoryBrandMerchantIndexRepository.findAllCategoryBrandMerchantIndex(pageable, userType);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading categoryBrandMerchantIndex from database was failed", e);

        }
    }

    /**
     * This method returns CategoryBrandMerchantIndex page which is filtered by brand Id and merchant name for mobile.
     *
     * @param pageable   pageable
     * @param categoryId categoryId/ ALL
     * @param searchTerm searchTerm/ ALL
     * @param userType   MERCHANT/ BANK
     * @return CategoryBrandMerchantIndex page
     */
    private Page<CategoryBrandMerchantIndex> getCategoryBrandMerchantIndexPageByCategoryIdForActiveMerchantOrBankOnly(
            Pageable pageable, String categoryId, String searchTerm, UserType userType) {
        try {
            if (!ALL.equals(categoryId) && !ALL.equals(searchTerm)) {
                return categoryBrandMerchantIndexRepository
                        .findAllByCategoryIdAndSearchTermAndActiveMerchant(pageable, categoryId, searchTerm, userType);
            } else if (!categoryId.equals(ALL) && searchTerm.equals(ALL)) {
                return categoryBrandMerchantIndexRepository
                        .findAllByCategoryIdAndActiveMerchant(pageable, categoryId, userType);
            } else if (categoryId.equals(ALL) && !searchTerm.equals(ALL)) {
                return categoryBrandMerchantIndexRepository
                        .findAllBySearchAndActiveMerchant(pageable, searchTerm, userType);
            } else {
                return categoryBrandMerchantIndexRepository
                        .findAllCategoryBrandMerchantIndexByActiveMerchant(pageable, userType);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading categoryBrandMerchantIndex from database was failed", e);
        }
    }


    /**
     * This method return CategoryBrandMerchantIndex page filtering by brand Id and merchant name
     *
     * @param pageable   pageable
     * @param brandId    brandId
     * @param searchTerm searchTerm/ ALl
     * @return CategoryBrandMerchantIndex page
     */
    public Page<CategoryBrandMerchantIndex> getCategoryBrandMerchantIndexPageByBrandId(
            Pageable pageable, String brandId, String searchTerm, UserType userType) {
        try {
            if (!ALL.equals(brandId) && !ALL.equals(searchTerm)) {
                return categoryBrandMerchantIndexRepository.findAllByBrandIdAndSearchTerm(pageable, brandId, searchTerm);
            } else if (!brandId.equals(ALL) && searchTerm.equals(ALL)) {
                return categoryBrandMerchantIndexRepository.findAllByBrandId(pageable, brandId);
            } else if (brandId.equals(ALL) && !searchTerm.equals(ALL)) {
                return categoryBrandMerchantIndexRepository.findAllBySearch(pageable, searchTerm, userType);
            } else {
                return categoryBrandMerchantIndexRepository.findAllCategoryBrandMerchantIndex(pageable, userType);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading categoryBrandMerchantIndex from database was failed", e);
        }
    }

    /**
     * This method update CategoryBrandMerchantIndex by merchant Id
     *
     * @param merchantId  merchantId
     * @param brandIds    brandIds
     * @param categoryIds categoryIds
     */
    public void updateCategoryBrandMerchantIndexByMerchantId(String merchantId, String brandIds, String categoryIds,
                                                             boolean isActiveMerchant, ApprovalStatus approvalStatus) {
        try {
            Optional<CategoryBrandMerchantIndex> optionalCategoryBrandMerchantIndex =
                    categoryBrandMerchantIndexRepository.findByMerchantId(merchantId);
            if (optionalCategoryBrandMerchantIndex.isPresent()) {
                var categoryBrandMerchantIndex = optionalCategoryBrandMerchantIndex.get();
                categoryBrandMerchantIndex.setCategoryIds(categoryIds);
                categoryBrandMerchantIndex.setActiveMerchant(isActiveMerchant);
                categoryBrandMerchantIndex.setMerchantApprovalStatus(approvalStatus);
                categoryBrandMerchantIndex.setBrandIds(brandIds);
                categoryBrandMerchantIndex.setUpdatedAt(System.currentTimeMillis());
                categoryBrandMerchantIndexRepository.save(categoryBrandMerchantIndex);
            } else {
                throw new InvalidUserException(INVALID_MERCHANT_ID + merchantId);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Updating categoryBrandMerchantIndex from database was failed", e);
        }
    }

    /**
     * This method will return all categoryBrandMerchantIndex by user type MERCHANT.
     *
     * @param pageable pageable
     * @param userType userType
     * @return CategoryBrandMerchantIndex page
     */
    public Page<CategoryBrandMerchantIndex> getAllCategoryBrandMerchantIndexByUserTypeMerchant(Pageable pageable, UserType userType) {
        return categoryBrandMerchantIndexRepository.getAllByUserType(pageable, userType);
    }

    /**
     * This method will update merchant info.
     *
     * @param changedMerchantMapData changedMerchantMapData
     */
    public void updateMerchantInfo(Map<String, MerchantBusinessResponseDto> changedMerchantMapData) {
        try {
            var categoryBrandMerchantIndexList =
                    categoryBrandMerchantIndexRepository.getAllByMerchantIdIn(new ArrayList<>(
                            changedMerchantMapData.keySet()));
            for (var categoryBrandMerchantIndex : categoryBrandMerchantIndexList) {
                categoryBrandMerchantIndex.setMerchantName(changedMerchantMapData
                        .get(categoryBrandMerchantIndex.getMerchantId()).getName());
                categoryBrandMerchantIndex.setMerchantImageUrl(changedMerchantMapData
                        .get(categoryBrandMerchantIndex.getMerchantId()).getImageUrl());
                categoryBrandMerchantIndex.setMerchantApprovalStatus(changedMerchantMapData
                        .get(categoryBrandMerchantIndex.getMerchantId()).getApprovalStatus());
                categoryBrandMerchantIndex.setActiveMerchant(changedMerchantMapData
                        .get(categoryBrandMerchantIndex.getMerchantId()).isActive());
            }
            categoryBrandMerchantIndexRepository.saveAll(categoryBrandMerchantIndexList);
        } catch (DataAccessException e) {
            throw new KupiCoreException(
                    "Updating merchant info for categoryBrandMerchantIndex was failed", e);
        }
    }
}
