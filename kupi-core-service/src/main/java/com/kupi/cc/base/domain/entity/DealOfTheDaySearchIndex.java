package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.enums.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.sql.Date;

/**
 * This class should replace from a search engine - real-time update
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "search_deals_of_the_day")
public class DealOfTheDaySearchIndex {
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
    private long createdAt;
    private long updatedAt;
    private boolean isDeleted;
    private String brandNames;
    private String categoryNames;
    private String merchantName;
    private String merchantImageUrl;
    @Enumerated(EnumType.STRING)
    private UserType dealSource;
    private boolean isActiveMerchant;
    private String dealCode;
    private boolean isDealOfTheDay = false;
    @Column(nullable = false)
    private Date applyOn;

    public DealOfTheDaySearchIndex(DealSearchIndex dealSearchIndex) {
        this(dealSearchIndex, new Date(System.currentTimeMillis()));
    }

    public DealOfTheDaySearchIndex(DealSearchIndex dealSearchIndex, Date applyOn) {

        this.id = dealSearchIndex.getId();
        this.title = dealSearchIndex.getTitle();
        this.subTitle = dealSearchIndex.getSubTitle();
        this.description = dealSearchIndex.getDescription();
        this.termsAndConditions = dealSearchIndex.getTermsAndConditions();
        this.searchImageUrls = dealSearchIndex.getSearchImageUrls();
        this.coverImage = dealSearchIndex.getCoverImage();
        this.primaryType = dealSearchIndex.getPrimaryType();
        this.secondaryType = dealSearchIndex.getSecondaryType();
        this.quantity = dealSearchIndex.getQuantity();
        this.originalPrice = dealSearchIndex.getOriginalPrice();
        this.deductionType = dealSearchIndex.getDeductionType();
        this.deductionAmount = dealSearchIndex.getDeductionAmount();
        this.deductionPercentage = dealSearchIndex.getDeductionPercentage();
        this.categoryNames = dealSearchIndex.getCategoryNames();
        this.brandNames = dealSearchIndex.getBrandNames();
        this.relatedCategories = dealSearchIndex.getRelatedCategories();
        this.relatedBrands = dealSearchIndex.getRelatedBrands();
        this.validFrom = dealSearchIndex.getValidFrom();
        this.expiredOn = dealSearchIndex.getExpiredOn();
        this.startTime = dealSearchIndex.getStartTime();
        this.endTime = dealSearchIndex.getEndTime();
        this.approvalStatus = dealSearchIndex.getApprovalStatus();
        this.merchantId = dealSearchIndex.getMerchantId();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isDeleted = false;
        this.merchantName = dealSearchIndex.getMerchantName();
        this.dealSource = dealSearchIndex.getDealSource();
        this.isActiveMerchant = dealSearchIndex.isActiveMerchant();
        this.dealCode = dealSearchIndex.getDealCode();
        this.isDealOfTheDay = true;
        this.applyOn = applyOn;
    }
}
