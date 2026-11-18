package com.kupi.cc.base.domain.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.DeductionType;
import com.kupi.cc.base.enums.PrimaryType;
import com.kupi.cc.base.enums.SecondaryType;
import lombok.Getter;
import lombok.Setter;

import com.kupi.cc.base.domain.request.ValidityPeriodDto;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Deal response dto
 */
@Setter
@Getter
public class DealResponseDto extends BasicDealResponseDto {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BasicMerchantBusinessResponseDto bank;
    private String description;
    private String dealCode;
    private String termsAndConditions;
    private PrimaryType primaryType;
    private SecondaryType secondaryType;
    private DeductionType deductionType;
    private DeductionPriceResponseDto deductionAmount;
    private Double deductionPercentage;
    private ApprovalStatus approvalStatus;
    private Set<BrandResponseDto> brands = new HashSet<>();
    private Set<CategoryResponseDto> categories = new HashSet<>();
    private PromoTypeResponseDto promoType;
    private DateResponseDto createdAt;
    private DateResponseDto updatedAt;
    private boolean isDealOfTheDay;
    /**
     * Total view count for this deal (all time).
     */
    private Long viewCount;
    /**
     * Total earnings for this deal (all time).
     * Calculated as redeemed coupon count * (originalPrice - deductionAmount).
     */
    private BigDecimal totalEarning;
    /**
     * Validity periods for this deal (reconstructed from coupons).
     * Only populated if couponAvailable is true and coupons exist.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ValidityPeriodDto> validityPeriods;

    public DealResponseDto(Deal deal, String timeZone, BasicMerchantBusinessResponseDto basicMerchantBusinessResponseDto) {
        super(deal, basicMerchantBusinessResponseDto, timeZone);
        this.description = deal.getDescription();
        this.dealCode = deal.getDealCode();
        this.termsAndConditions = deal.getTermsAndConditions();
        this.primaryType = deal.getPrimaryType();
        this.secondaryType = deal.getSecondaryType();
        this.deductionAmount = new DeductionPriceResponseDto(deal.getDeductionAmount());
        this.deductionPercentage = deal.getDeductionPercentage();
        this.approvalStatus = deal.getApprovalStatus();
        this.deductionType = deal.getDeductionType();
        deal.getRelatedCategories().forEach(category -> categories.add(new CategoryResponseDto(category, timeZone)));
        deal.getRelatedBrands().forEach(brand -> brands.add(new BrandResponseDto(brand, timeZone)));
        if (deal.getPromoType() != null) {
            this.promoType = new PromoTypeResponseDto(deal.getPromoType(), timeZone);
        }
        this.createdAt = new DateResponseDto(deal.getCreatedAt(), timeZone, new Date(deal.getCreatedAt()));
        this.updatedAt = new DateResponseDto(deal.getUpdatedAt(), timeZone, new Date(deal.getUpdatedAt()));
        this.isDealOfTheDay = deal.isDealOfTheDay();
    }

    public DealResponseDto(Deal deal, String timeZone, BasicMerchantBusinessResponseDto basicMerchantBusinessResponseDto,
                           BasicMerchantBusinessResponseDto basicBankBusinessResponseDto) {
        super(deal, basicMerchantBusinessResponseDto, timeZone);
        this.bank = basicBankBusinessResponseDto;
        this.description = deal.getDescription();
        this.dealCode = deal.getDealCode();
        this.termsAndConditions = deal.getTermsAndConditions();
        this.primaryType = deal.getPrimaryType();
        this.secondaryType = deal.getSecondaryType();
        this.deductionAmount = new DeductionPriceResponseDto(deal.getDeductionAmount());
        this.deductionPercentage = deal.getDeductionPercentage();
        this.approvalStatus = deal.getApprovalStatus();
        this.deductionType = deal.getDeductionType();
        deal.getRelatedCategories().forEach(category -> categories.add(new CategoryResponseDto(category, timeZone)));
        deal.getRelatedBrands().forEach(brand -> brands.add(new BrandResponseDto(brand, timeZone)));
        if (deal.getPromoType() != null) {
            this.promoType = new PromoTypeResponseDto(deal.getPromoType(), timeZone);
        }
        this.createdAt = new DateResponseDto(deal.getCreatedAt(), timeZone, new Date(deal.getCreatedAt()));
        this.updatedAt = new DateResponseDto(deal.getUpdatedAt(), timeZone, new Date(deal.getUpdatedAt()));
        this.isDealOfTheDay = deal.isDealOfTheDay();
    }


    @Override
    public String toLogJson() {
        return toJson();
    }
}
