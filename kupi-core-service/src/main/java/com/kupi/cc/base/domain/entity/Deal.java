package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.domain.request.DealRequestDto;
import com.kupi.cc.base.domain.request.DealUpdateRequestDto;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.DeductionType;
import com.kupi.cc.base.enums.PrimaryType;
import com.kupi.cc.base.enums.SecondaryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kupi.cc.base.domain.request.ValidityPeriodDto;
import com.kupi.cc.base.exception.KupiCoreException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Deal entity
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "deal")
public class Deal {
    @Transient
    private static final String DEAL_ID_PREFIX = "did-";
    @Transient
    private static final int DESCRIPTION_MAX_LENGTH = 500;
    @Transient
    private String storeId;

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
    @ElementCollection
    private List<String> imageUrls;
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
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "deal_category",
            joinColumns = @JoinColumn(name = "dealId"),
            inverseJoinColumns = @JoinColumn(name = "categoryId"))
    private Set<Category> relatedCategories = new HashSet<>();
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "deal_brand",
            joinColumns = @JoinColumn(name = "dealId"),
            inverseJoinColumns = @JoinColumn(name = "brandId"))
    private Set<Brand> relatedBrands = new HashSet<>();
    @ManyToOne
    @JoinColumn(name = "promo_type_id")
    private PromoType promoType;
    private String dealCode;
    private long createdAt;
    private long updatedAt;
    private boolean isDealOfTheDay;
    private boolean isDeleted;
    @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL)
    private Set<Coupon> coupons;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "coupon_statistics_id")
    private CouponStatistics couponStatistics;
    @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL)
    private Set<CouponStatisticDetail> couponStatisticDetail;
    private boolean isCouponAvailable;
    private boolean couponActive;
    private boolean freeCoupons;
    /**
     * Store original validity periods as JSON string.
     * This preserves the exact periods sent during creation/update.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String validityPeriodsJson;

    public Deal(DealRequestDto dealRequestDto, Set<Brand> brands, Set<Category> categories, PromoType promoType, String dealCode) {
        this.id = DEAL_ID_PREFIX + UUID.randomUUID();
        this.title = dealRequestDto.getTitle();
        this.subTitle = dealRequestDto.getSubTitle();
        this.description = dealRequestDto.getDescription();
        this.termsAndConditions = dealRequestDto.getTermsAndConditions();
        this.imageUrls = dealRequestDto.getImageUrls();
        this.coverImage = dealRequestDto.getCoverImage();
        this.primaryType = PrimaryType.READY_MADE;
        this.secondaryType = SecondaryType.DEAL;
        this.quantity = dealRequestDto.getQuantity();
        this.originalPrice = dealRequestDto.getOriginalPrice();
        this.deductionType = dealRequestDto.getDeductionType();
        this.deductionAmount = dealRequestDto.getDeductionAmount();
        this.deductionPercentage = dealRequestDto.getDeductionPercentage();
        this.couponPrice = dealRequestDto.getCouponPrice();
        this.validFrom = dealRequestDto.getValidFrom();
        this.expiredOn = dealRequestDto.getExpiredOn();
        this.startTime = dealRequestDto.getStartTime();
        this.endTime = dealRequestDto.getEndTime();
        // Set approval status based on publishAction: "PUBLISH" -> APPROVED (active), otherwise DRAFT
        // For backward compatibility, if publishAction is null/empty, default to DRAFT
        if (dealRequestDto.getPublishAction() != null && 
            "PUBLISH".equalsIgnoreCase(dealRequestDto.getPublishAction().trim())) {
            this.approvalStatus = ApprovalStatus.APPROVED;
        } else {
            this.approvalStatus = ApprovalStatus.DRAFT;
        }
        this.merchantId = dealRequestDto.getMerchantId();
        this.relatedBrands = brands;
        this.relatedCategories = categories;
        this.promoType = promoType;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isDeleted = false;
        this.dealCode = dealCode;
        this.storeId = dealRequestDto.getShopId();
        this.isCouponAvailable = dealRequestDto.isCouponAvailable();
        this.couponActive = dealRequestDto.isCouponAvailable();
        this.freeCoupons = dealRequestDto.isFreeCoupons();
        // Store original validity periods as JSON
        if (dealRequestDto.getValidityPeriods() != null && !dealRequestDto.getValidityPeriods().isEmpty()) {
            this.validityPeriodsJson = serializeValidityPeriods(dealRequestDto.getValidityPeriods());
        }
    }

    public Deal(BankDeal bankDeal) {
        this.id = bankDeal.getId();
        this.title = bankDeal.getTitle();
        this.subTitle = bankDeal.getSubTitle();
        this.description = bankDeal.getDescription();
        this.termsAndConditions = bankDeal.getTermsAndConditions();
        this.imageUrls = bankDeal.getImageUrls();
        this.coverImage = bankDeal.getCoverImage();
        this.primaryType = bankDeal.getPrimaryType();
        this.secondaryType = bankDeal.getSecondaryType();
        this.quantity = bankDeal.getQuantity();
        this.originalPrice = bankDeal.getOriginalPrice();
        this.deductionType = bankDeal.getDeductionType();
        this.deductionAmount = bankDeal.getDeductionAmount();
        this.deductionPercentage = bankDeal.getDeductionPercentage();
        this.validFrom = bankDeal.getValidFrom();
        this.expiredOn = bankDeal.getExpiredOn();
        this.approvalStatus = bankDeal.getApprovalStatus();
        this.comment = bankDeal.getComment();
        this.merchantId = bankDeal.getBankId();
        this.relatedBrands = bankDeal.getRelatedBrands();
        this.relatedCategories = bankDeal.getRelatedCategories();
        this.createdAt = bankDeal.getCreatedAt();
        this.updatedAt = bankDeal.getUpdatedAt();
        this.isDeleted = bankDeal.isDeleted();
        this.storeId = bankDeal.getMerchantId();
        this.dealCode = bankDeal.getDealCode();
        this.isDealOfTheDay = bankDeal.isDealOfTheDay();
        this.isCouponAvailable = bankDeal.isCouponAvailable();
    }

    /**
     * Update deal.
     *
     * @param dealUpdateRequestDto dealUpdateRequestDto
     * @param categories           categories list
     * @param brands               brands list
     */
    public void update(DealUpdateRequestDto dealUpdateRequestDto, Set<Category> categories, Set<Brand> brands, PromoType promoType) {
        this.title = dealUpdateRequestDto.getTitle();
        this.subTitle = dealUpdateRequestDto.getSubTitle();
        this.description = dealUpdateRequestDto.getDescription();
        this.termsAndConditions = dealUpdateRequestDto.getTermsAndConditions();
        this.imageUrls = dealUpdateRequestDto.getImageUrls();
        this.coverImage = dealUpdateRequestDto.getCoverImage();
        this.quantity = dealUpdateRequestDto.getQuantity();
        this.deductionType = dealUpdateRequestDto.getDeductionType();
        if (dealUpdateRequestDto.getDeductionType() == DeductionType.PERCENTAGE) {
            this.deductionAmount = null;
            this.deductionPercentage = dealUpdateRequestDto.getDeductionPercentage();
        }
        if (dealUpdateRequestDto.getDeductionType() == DeductionType.AMOUNT) {
            this.deductionAmount = dealUpdateRequestDto.getDeductionAmount();
            this.deductionPercentage = null;
        }
        this.couponPrice = dealUpdateRequestDto.getCouponPrice();
        this.originalPrice = dealUpdateRequestDto.getOriginalPrice();
        this.validFrom = dealUpdateRequestDto.getValidFrom();
        this.expiredOn = dealUpdateRequestDto.getExpiredOn();
        this.startTime = dealUpdateRequestDto.getStartTime();
        this.endTime = dealUpdateRequestDto.getEndTime();
        this.relatedBrands = brands;
        this.relatedCategories = categories;
        this.promoType = promoType;
        this.merchantId = dealUpdateRequestDto.getMerchantId();
        this.updatedAt = System.currentTimeMillis();
        this.storeId = dealUpdateRequestDto.getShopId();
        // Handle publishAction: if "PUBLISH" is provided, change status to APPROVED (active)
        // Otherwise preserve existing status (DRAFT stays DRAFT, APPROVED stays APPROVED, PENDING stays PENDING)
        if (dealUpdateRequestDto.getPublishAction() != null && 
            "PUBLISH".equalsIgnoreCase(dealUpdateRequestDto.getPublishAction().trim())) {
            this.approvalStatus = ApprovalStatus.APPROVED;
        }
        // Note: If publishAction is null/empty or not "PUBLISH", status remains unchanged
        this.isCouponAvailable = dealUpdateRequestDto.isCouponAvailable();
        this.couponActive = dealUpdateRequestDto.isCouponAvailable();
        this.freeCoupons = dealUpdateRequestDto.isFreeCoupons();
        // Store original validity periods as JSON
        if (dealUpdateRequestDto.getValidityPeriods() != null && !dealUpdateRequestDto.getValidityPeriods().isEmpty()) {
            this.validityPeriodsJson = serializeValidityPeriods(dealUpdateRequestDto.getValidityPeriods());
        } else {
            // If no validity periods provided, clear the stored periods
            this.validityPeriodsJson = null;
        }
    }
    
    /**
     * Serialize validity periods to JSON string.
     *
     * @param validityPeriods list of validity periods
     * @return JSON string representation
     */
    private String serializeValidityPeriods(List<ValidityPeriodDto> validityPeriods) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(validityPeriods);
        } catch (Exception e) {
            throw new KupiCoreException("Failed to serialize validity periods to JSON", e);
        }
    }
    
    /**
     * Deserialize validity periods from JSON string.
     *
     * @return list of validity periods, or empty list if JSON is null/empty
     */
    public List<ValidityPeriodDto> getValidityPeriods() {
        if (validityPeriodsJson == null || validityPeriodsJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Configure to ignore unknown properties (like "valid" field that might be added by isValid() method)
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(validityPeriodsJson, new TypeReference<List<ValidityPeriodDto>>() {});
        } catch (Exception e) {
            throw new KupiCoreException("Failed to deserialize validity periods from JSON", e);
        }
    }

    /**
     * Explicit getter for originalPrice to ensure availability in Spring Boot 3.x
     */
    public Double getOriginalPrice() {
        return this.originalPrice;
    }

    /**
     * Explicit getter for deductionType to ensure availability in Spring Boot 3.x
     */
    public DeductionType getDeductionType() {
        return this.deductionType;
    }

    /**
     * Explicit getter for deductionAmount to ensure availability in Spring Boot 3.x
     */
    public Double getDeductionAmount() {
        return this.deductionAmount;
    }

    /**
     * Explicit getter for deductionPercentage to ensure availability in Spring Boot 3.x
     */
    public Double getDeductionPercentage() {
        return this.deductionPercentage;
    }
}
