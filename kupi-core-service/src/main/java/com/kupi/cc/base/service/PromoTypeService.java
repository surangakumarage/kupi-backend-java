package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Category;
import com.kupi.cc.base.domain.entity.PromoType;
import com.kupi.cc.base.domain.request.PromoTypeRequestDto;
import com.kupi.cc.base.domain.request.PromoTypeUpdateRequestDto;
import com.kupi.cc.base.exception.ExistingNameException;
import com.kupi.cc.base.exception.InvalidCategoryException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.CategoryRepository;
import com.kupi.cc.base.repository.PromoTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PromoTypeService {

    private final PromoTypeRepository promoTypeRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    @Autowired
    public PromoTypeService(PromoTypeRepository promoTypeRepository, CategoryRepository categoryRepository, CategoryService categoryService) {
        this.promoTypeRepository = promoTypeRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
    }


    /**
     * Get promo type by id
     *
     * @param promoTypeId promo type id
     * @return promo type
     */
    public PromoType getPromoTypeById(String promoTypeId) {
        try {
            Optional<PromoType> promoType = promoTypeRepository.findById(promoTypeId);
            if (promoType.isEmpty() || !promoType.get().isActive()) {
                throw new InvalidCategoryException("Invalid promo type Id: " + promoTypeId);
            }
            return promoType.get();
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading promo type by id from database was failed.", e);
        }
    }

    /**
     * Check if promo type exists by name
     *
     * @param name promo type name
     * @return true if exists, false otherwise
     */
    public boolean existsByName(String name) {
        try {
            return promoTypeRepository.existsByNameAndIsActiveTrue(name);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Checking promo type existence by name from database was failed.", e);
        }
    }

    /**
     * Check if promo type exists by name within a specific category
     *
     * @param name promo type name
     * @param categoryId category ID
     * @return true if exists, false otherwise
     */
    public boolean existsByNameAndCategory(String name, String categoryId) {
        try {
            return promoTypeRepository.existsByNameAndCategoryIdAndIsActiveTrue(name, categoryId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Checking promo type existence by name and category from database was failed.", e);
        }
    }

    /**
     * Create a new promo type
     *
     * @param promoTypeRequestDto promo type request
     * @return created promo type
     */
    public PromoType createPromoType(PromoTypeRequestDto promoTypeRequestDto) {
        try {
            if (existsByNameAndCategory(promoTypeRequestDto.getName(), promoTypeRequestDto.getCategoryId())) {
                throw new ExistingNameException("Promo type with name '" + promoTypeRequestDto.getName() + "' already exists in this category.");
            }

            Category category = categoryService.getCategoryById(promoTypeRequestDto.getCategoryId());
            PromoType promoType = new PromoType(promoTypeRequestDto.getName(), category);
            PromoType savedPromoType = promoTypeRepository.save(promoType);
            
            // Add the promo type to the category's promo types set
            category.getPromoTypes().add(savedPromoType);
            categoryRepository.save(category);

            return savedPromoType;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Creating promo type in database was failed.", e);
        }
    }

    /**
     * Update an existing promo type
     *
     * @param promoTypeUpdateRequestDto promo type update request
     * @return updated promo type
     */
    public PromoType updatePromoType(PromoTypeUpdateRequestDto promoTypeUpdateRequestDto) {
        try {
            PromoType existingPromoType = getPromoTypeById(promoTypeUpdateRequestDto.getId());

            if (existsByNameAndCategory(promoTypeUpdateRequestDto.getName(), promoTypeUpdateRequestDto.getCategoryId()) &&
                    !existingPromoType.getName().equals(promoTypeUpdateRequestDto.getName())) {
                throw new ExistingNameException("Promo type with name '" + promoTypeUpdateRequestDto.getName() + "' already exists in this category.");
            }

            Category newCategory = categoryService.getCategoryById(promoTypeUpdateRequestDto.getCategoryId());
            Category oldCategory = existingPromoType.getCategory();

            // Remove from old category if different
            if (!oldCategory.getId().equals(newCategory.getId())) {
                oldCategory.getPromoTypes().remove(existingPromoType);
                categoryRepository.save(oldCategory);
            }

            // Update promo type
            existingPromoType.setCategory(newCategory);
            existingPromoType.update(promoTypeUpdateRequestDto.getName());

            PromoType savedPromoType = promoTypeRepository.save(existingPromoType);

            // Add to new category if different
            if (!oldCategory.getId().equals(newCategory.getId())) {
                newCategory.getPromoTypes().add(savedPromoType);
                categoryRepository.save(newCategory);
            }

            return savedPromoType;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Updating promo type in database was failed.", e);
        }
    }

    /**
     * Get all promo types by category ID
     *
     * @param categoryId category ID
     * @return list of promo types
     */
    public List<PromoType> getPromoTypesByCategoryId(String categoryId) {
        try {
            return promoTypeRepository.findByCategoryIdAndIsActiveTrue(categoryId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading promo types by category ID from database was failed.", e);
        }
    }

}
