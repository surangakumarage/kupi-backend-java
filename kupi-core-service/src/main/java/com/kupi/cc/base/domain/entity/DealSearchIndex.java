package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.domain.response.BasicMerchantBusinessResponseDto;
import com.kupi.cc.base.domain.response.BusinessMerchantResponseDto;
import com.kupi.cc.base.enums.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

/**
 * This class should replace from a search engine - real-time update
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "search_deal")
public class DealSearchIndex {
    @Transient
    private static final int DESCRIPTION_MAX_LENGTH = 500;
    @Transient
    private static final int TERMS_AND_CONDITION_MAX_LENGTH = 500;
    @Transient
    private static final int SEARCH_IMAGE_URLS_MAX = 1000;
    @Transient
    private static final int RELATED_BRANDS_MAX_LENGTH = 1000;
    @Transient
    private static final int RELATED_CATEGORIES_MAX_LENGTH = 1000;
    @Transient
    private static final String SEPARATOR = ",";
    @Transient
    private static final String EMPTY_TEXT = "";

    @Id
    private String id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String subTitle;
    @Size(max = DESCRIPTION_MAX_LENGTH)
    @Column(columnDefinition = "VARCHAR(500)")
    private String description;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String termsAndConditions;
    @Size(max = SEARCH_IMAGE_URLS_MAX)
    @Column(columnDefinition = "VARCHAR(1000)")
    private String searchImageUrls;
    @Column(nullable = false)
    private String coverImage;
    @Column(nullable = false)
    private PrimaryType primaryType;
    @Column(nullable = false)
    private SecondaryType secondaryType;
    @Column(nullable = false)
    private int quantity;
    private Double originalPrice;
    @Column(nullable = false)
    private DeductionType deductionType;
    private Double deductionAmount = null;
    private Double deductionPercentage = null;
    private Double couponPrice;
    @Column(nullable = false)
    private long validFrom;
    @Column(nullable = false)
    private long expiredOn;
    private String startTime;
    private String endTime;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;
    private String comment;
    @Column(nullable = false)
    private String merchantId;
    @Size(max = RELATED_CATEGORIES_MAX_LENGTH)
    @Column(columnDefinition = "VARCHAR(1000)")
    private String relatedCategories;
    @Size(max = RELATED_BRANDS_MAX_LENGTH)
    @Column(columnDefinition = "VARCHAR(1000)")
    private String relatedBrands;
    private String promoTypeId;
    private String promoTypeName;
    private String promoTypeCategoryId;
    private String promoTypeCategoryName;
    private long createdAt;
    private long updatedAt;
    private boolean isDeleted;
    private String brandNames = EMPTY_TEXT;
    private String categoryNames = EMPTY_TEXT;
    private String merchantName = EMPTY_TEXT;
    private String merchantImageUrl = EMPTY_TEXT;
    private boolean isActiveMerchant;
    @Enumerated(EnumType.STRING)
    private UserType dealSource;
    private String storeId;
    private String dealCode;
    private boolean isDealOfTheDay;
    private boolean isCouponAvailable;
    private boolean couponActive;
    private boolean freeCoupons;
    private int totalCoupons;
    private int remainingCoupons;

    public DealSearchIndex(Deal deal, BasicMerchantBusinessResponseDto basicMerchantBusinessResponseDto,
                           UserType userType) {
        this.id = deal.getId();
        this.title = deal.getTitle();
        this.subTitle = deal.getSubTitle();
        this.description = deal.getDescription();
        this.termsAndConditions = deal.getTermsAndConditions();
        this.searchImageUrls = deal.getImageUrls().toString();
        this.coverImage = deal.getCoverImage();
        this.primaryType = deal.getPrimaryType();
        this.secondaryType = deal.getSecondaryType();
        this.quantity = deal.getQuantity();
        this.originalPrice = deal.getOriginalPrice();
        this.deductionType = deal.getDeductionType();
        this.deductionAmount = deal.getDeductionAmount();
        this.deductionPercentage = deal.getDeductionPercentage();
        this.couponPrice = deal.getCouponPrice();
        this.validFrom = deal.getValidFrom();
        this.expiredOn = deal.getExpiredOn();
        this.startTime = deal.getStartTime();
        this.endTime = deal.getEndTime();
        // Use deal's approvalStatus (DRAFT, PUBLISHED, PENDING, etc.) instead of hardcoding PENDING
        this.approvalStatus = deal.getApprovalStatus();
        this.merchantId = deal.getMerchantId();
        deal.getRelatedBrands().forEach(brand -> {
            this.brandNames += brand.getName() + SEPARATOR;
            this.relatedBrands += brand.getId() + SEPARATOR;
        });
        deal.getRelatedCategories().forEach(category -> {
            this.categoryNames += category.getName() + SEPARATOR;
            this.relatedCategories += category.getId() + SEPARATOR;
        });
        if (deal.getPromoType() != null) {
            this.promoTypeId = deal.getPromoType().getId();
            this.promoTypeName = deal.getPromoType().getName();
            this.promoTypeCategoryId = deal.getPromoType().getCategory().getId();
            this.promoTypeCategoryName = deal.getPromoType().getCategory().getName();
        }
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isDeleted = false;
        this.merchantName = basicMerchantBusinessResponseDto.getName();
        this.merchantImageUrl = basicMerchantBusinessResponseDto.getImageUrl();
        this.isActiveMerchant = basicMerchantBusinessResponseDto.isActive();
        this.dealSource = userType;
        this.storeId = deal.getStoreId() != null ? deal.getStoreId() : deal.getMerchantId();
        this.dealCode = deal.getDealCode();
        this.isCouponAvailable = deal.isCouponAvailable();
        this.couponActive = deal.isCouponActive();
        this.freeCoupons = deal.isFreeCoupons();
    }

    /**
     * Update dealSearchIndex.
     *
     * @param deal                        deal
     * @param businessMerchantResponseDto businessMerchantResponseDto
     */
    public void update(Deal deal, BusinessMerchantResponseDto businessMerchantResponseDto) {
        this.title = deal.getTitle();
        this.subTitle = deal.getSubTitle();
        this.description = deal.getDescription();
        this.termsAndConditions = deal.getTermsAndConditions();
        this.searchImageUrls = deal.getImageUrls().toString();
        this.coverImage = deal.getCoverImage();
        this.primaryType = deal.getPrimaryType();
        this.secondaryType = deal.getSecondaryType();
        this.quantity = deal.getQuantity();
        this.originalPrice = deal.getOriginalPrice();
        this.deductionType = deal.getDeductionType();
        this.deductionAmount = deal.getDeductionAmount();
        this.deductionPercentage = deal.getDeductionPercentage();
        this.couponPrice = deal.getCouponPrice();
        this.validFrom = deal.getValidFrom();
        this.expiredOn = deal.getExpiredOn();
        this.startTime = deal.getStartTime();
        this.endTime = deal.getEndTime();
        this.brandNames = "";
        this.relatedBrands = "";
        deal.getRelatedBrands().forEach(brand -> {
            this.brandNames += brand.getName() + SEPARATOR;
            this.relatedBrands += brand.getId() + SEPARATOR;
        });
        this.categoryNames = "";
        this.relatedCategories = "";
        deal.getRelatedCategories().forEach(category -> {
            this.categoryNames += category.getName() + SEPARATOR;
            this.relatedCategories += category.getId() + SEPARATOR;
        });
        if (deal.getPromoType() != null) {
            this.promoTypeId = deal.getPromoType().getId();
            this.promoTypeName = deal.getPromoType().getName();
            this.promoTypeCategoryId = deal.getPromoType().getCategory().getId();
            this.promoTypeCategoryName = deal.getPromoType().getCategory().getName();
        } else {
            this.promoTypeId = null;
            this.promoTypeName = null;
            this.promoTypeCategoryId = null;
            this.promoTypeCategoryName = null;
        }
        this.updatedAt = System.currentTimeMillis();
        this.merchantName = businessMerchantResponseDto.getBusinessName();
        this.merchantImageUrl = businessMerchantResponseDto.getImageUrl();
        this.storeId = deal.getStoreId() != null ? deal.getStoreId() : deal.getMerchantId();
        this.approvalStatus = deal.getApprovalStatus();
        this.isCouponAvailable = deal.isCouponAvailable();
        this.couponActive = deal.isCouponActive();
        this.freeCoupons = deal.isFreeCoupons();
    }

    /**
     * This method is used to update coupon statistics.
     *
     * @param totalCoupons totalCoupons
     */
    public void updateTotalCoupons(int totalCoupons) {
        this.totalCoupons += totalCoupons;
        this.remainingCoupons += totalCoupons;
    }

    /**
     * This method updates remaining coupons when coupon is purchased.
     */
    public void updateRemainingCouponsOnPurchase() {
        remainingCoupons--;
        updatedAt = System.currentTimeMillis();
    }

    /**
     * This method updates remaining coupons when coupon purchase is cancelled.
     */
    public void updateRemainingCouponsOnPurchaseCancellation() {
        remainingCoupons++;
        updatedAt = System.currentTimeMillis();
    }

    /**
     * Explicit getter for validFrom to ensure availability in Spring Boot 3.x
     */
    public long getValidFrom() {
        return this.validFrom;
    }

    /**
     * Explicit getter for isCouponAvailable to ensure availability in Spring Boot 3.x
     */
    public boolean isCouponAvailable() {
        return this.isCouponAvailable;
    }
}
