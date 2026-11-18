package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Brand;
import com.kupi.cc.base.domain.request.BrandUpdateRequestDto;
import com.kupi.cc.base.exception.InvalidBrandException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.exception.UnsupportedDeleteAction;
import com.kupi.cc.base.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Brand Service
 */
@Service
public class BrandService {

    private static final String ALL = "ALL";
    private static final String INVALID_BRAND = "Invalid brand Id:";
    private final BrandRepository brandRepository;
    private final CategoryBrandMerchantService categoryBrandMerchantService;
    private final BrandUpdateService brandUpdateService;


    @Autowired
    public BrandService(BrandRepository brandRepository,
                        @Lazy CategoryBrandMerchantService categoryBrandMerchantService, BrandUpdateService brandUpdateService) {
        this.brandRepository = brandRepository;
        this.categoryBrandMerchantService = categoryBrandMerchantService;
        this.brandUpdateService = brandUpdateService;
    }

    /**
     * Get list of brands
     *
     * @param pageable pageable
     * @return Brand page
     */
    public Page<Brand> listAllBrands(Pageable pageable, String searchTerm) {
        try {
            if (searchTerm.equals("ALL")) {
                return brandRepository.findAll(pageable);

            } else {
                return brandRepository.findAllByNameContaining(pageable, searchTerm);
            }

        } catch (DataAccessException e) {
            throw new KupiCoreException("Read brands from database was failed.", e);
        }
    }

    /**
     * Save new brand into database
     *
     * @param brand brand
     */
    public void createBrand(Brand brand) {
        try {
            brandRepository.save(brand);

        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving brand into database was failed.", e);
        }
    }

    /**
     * Get brand by Id
     *
     * @param brandId brandId
     * @return brand
     */
    public Brand getBrandById(String brandId) {
        try {
            Optional<Brand> optionalBrandFromDb = brandRepository.findById(brandId);

            if (optionalBrandFromDb.isPresent()) {
                return optionalBrandFromDb.get();
            } else {
                throw new InvalidBrandException(INVALID_BRAND + brandId);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading brand from database was failed.", e);
        }
    }

    /**
     * Get brand set by brandId list
     *
     * @param brandIdList brandId list
     * @return brand set
     */
    public Set<Brand> getBrandSetByIdList(List<String> brandIdList) {
        try {
            Set<Brand> relatedBrands = new HashSet<>();
            for (String id : brandIdList) {
                Optional<Brand> optionalBrandFromDb = brandRepository.findById(id);
                if (optionalBrandFromDb.isPresent()) {
                    relatedBrands.add(optionalBrandFromDb.get());
                } else {
                    throw new InvalidBrandException(INVALID_BRAND + id);
                }
            }
            return relatedBrands;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading brand from database was failed.", e);
        }
    }

    /**
     * This method validate the given brandId.
     *
     * @param brandId brandId
     */
    public void validateBrandId(String brandId) {
        Optional<Brand> brandFromDb = brandRepository.findById(brandId);
        if (brandFromDb.isEmpty()) {
            throw new InvalidBrandException(INVALID_BRAND + brandId);
        }
    }

    /**
     * This method update existing brand.
     *
     * @param brandUpdateRequestDto brandUpdateRequestDto
     * @return Brand
     */
    public Brand updateBrand(BrandUpdateRequestDto brandUpdateRequestDto) {

        try {
            var brandFromDb = getBrandById(brandUpdateRequestDto.getId());
            var previousBrandName = brandFromDb.getName();
            brandFromDb.update(brandUpdateRequestDto);
            brandUpdateService.updateBrandData(brandFromDb, previousBrandName);
            return brandRepository.save(brandFromDb);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Updating brand by id: " +
                    brandUpdateRequestDto.getId() + " from database was failed.", e);
        }
    }

    /**
     * This method returns Brand Page by brand id(s) with search term.
     *
     * @param pageable   pageable
     * @param ids        ids
     * @param searchTerm search term
     * @return Brand Page
     */
    public Page<Brand> getBulkBrandsByIds(Pageable pageable, Set<String> ids, String searchTerm) {
        try {
            return ((ALL.equals(searchTerm)) ? brandRepository.findByBrandIds(pageable, ids) :
                    brandRepository.findByBrandIdsBySearch(pageable, ids, searchTerm));

        } catch (DataAccessException e) {
            throw new KupiCoreException("Returning bulk Brand by Id(s) from database was failed.", e);
        }
    }

    /**
     * This method returns true when brand name is exist in the database.
     *
     * @param brandName brand name
     * @return true/ false
     */
    public boolean checkBrandNameExist(String brandName) {
        try {
            return brandRepository.existsByName(brandName);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Checking Brand Name is exist from the database was failed.", e);
        }
    }

    /**
     * This method returns true when updating name exist with other brand.
     *
     * @param id   brand id
     * @param name name
     * @return true/ false
     */
    public boolean checkBrandNameNotExistForOtherIDs(String id, String name) {
        try {
            return brandRepository.existsByNameAndIdNot(name, id);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Checking Brand Name is exists for other id(s) from the database was failed.", e);
        }
    }

    /**
     * This method used to delete a brand permanently.
     *
     * @param brandId brand id
     */
    public void deleteBrand(String brandId) {
        try {
            Brand brand = getBrandById(brandId);
            var isBrandMapWithCategoryBrandMerchant
                    = categoryBrandMerchantService.checkBrandIdMapWithCategoryBrandMerchant(brandId);
            if (!isBrandMapWithCategoryBrandMerchant) {
                brandRepository.delete(brand);
            } else {
                throw new UnsupportedDeleteAction("Brand cannot be deleted.");
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Deleting brand from the database was failed.", e);
        }
    }
}
