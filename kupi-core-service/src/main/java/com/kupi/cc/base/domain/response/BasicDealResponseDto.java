package com.kupi.cc.base.domain.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kupi.cc.base.domain.entity.AuthUser;
import com.kupi.cc.base.domain.entity.BankDeal;
import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.UserType;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic deal request dto
 */
@Setter
@Getter
public class BasicDealResponseDto extends ResponseDto {

    private String id;
    private String title;
    private String subTitle;
    private String dealCode;
    private List<String> imageUrls = new ArrayList<>();
    private String coverImage;
    private int totalQuantity;
    private int remainingQuantity;
    private PriceResponseDto price;
    private Double deductionPercentage;
    private Double couponPrice;
    private DateResponseDto validFrom;
    private DateResponseDto expiredOn;
    private String startTime;
    private String endTime;
    private ApprovalStatus approvalStatus;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BasicMerchantBusinessResponseDto merchant;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BasicMerchantBusinessResponseDto bank;
    private String dealSource;
    private boolean couponAvailable;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DealCouponResponseDto couponDetails;
    private boolean isDealOfTheDay;
    private boolean expired;
    private boolean freeCoupons;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PromoTypeResponseDto promoType;
    private boolean isFavorite;

    public BasicDealResponseDto(Deal deal, BasicMerchantBusinessResponseDto basicMerchantBusinessResponseDto, String timeZone) {
        this.id = deal.getId();
        this.title = deal.getTitle();
        this.subTitle = deal.getSubTitle();
        this.imageUrls = deal.getImageUrls();
        this.coverImage = deal.getCoverImage();
        this.totalQuantity = deal.getQuantity();
        this.remainingQuantity = deal.getQuantity();
        this.price = (deal.getOriginalPrice() != null) ? new PriceResponseDto(deal.getOriginalPrice(), deal.getDeductionAmount(),
                deal.getDeductionPercentage()) : null;
        this.deductionPercentage = deal.getDeductionPercentage();
        this.couponPrice = deal.getCouponPrice();
        this.validFrom = new DateResponseDto(deal.getValidFrom(), timeZone, new Date(deal.getValidFrom()));
        this.expiredOn = new DateResponseDto(deal.getExpiredOn(), timeZone, new Date(deal.getExpiredOn()));
        this.startTime = deal.getStartTime();
        this.endTime = deal.getEndTime();
        if (basicMerchantBusinessResponseDto != null) {
            this.dealSource = basicMerchantBusinessResponseDto.getUserType();
            if (dealSource.equals(UserType.MERCHANT.toString()))
                this.merchant = basicMerchantBusinessResponseDto;
            else
                this.bank = basicMerchantBusinessResponseDto;
        }
        this.approvalStatus = deal.getApprovalStatus();
        this.dealCode = deal.getDealCode();
        this.isDealOfTheDay = deal.isDealOfTheDay();
        this.couponAvailable = deal.isCouponAvailable();
        this.freeCoupons = deal.isFreeCoupons();
        this.couponDetails = this.couponAvailable ? new DealCouponResponseDto(deal.getCouponPrice(),
                deal.getCouponStatistics(), timeZone, deal.getExpiredOn(), deal.getValidFrom()) : null;
        this.expired = this.expiredOn.getMilliseconds() < System.currentTimeMillis();
        if (deal.getPromoType() != null) {
            this.promoType = new PromoTypeResponseDto(deal.getPromoType(), timeZone);
        }
    }

    public BasicDealResponseDto(DealSearchIndex dealSearchIndex, String timeZone) {
        this(dealSearchIndex, timeZone, false);
    }

    public BasicDealResponseDto(DealSearchIndex dealSearchIndex, String timeZone, boolean isFavorite) {
        this.id = dealSearchIndex.getId();
        this.title = dealSearchIndex.getTitle();
        this.subTitle = dealSearchIndex.getSubTitle();
        this.imageUrls.addAll(stringToListImages(dealSearchIndex.getSearchImageUrls()));
        this.coverImage = dealSearchIndex.getCoverImage();
        this.totalQuantity = dealSearchIndex.getQuantity();
        this.remainingQuantity = dealSearchIndex.getQuantity();
        this.deductionPercentage = dealSearchIndex.getDeductionPercentage();
        this.couponPrice = dealSearchIndex.getCouponPrice();
        this.price = (dealSearchIndex.getOriginalPrice() != null) ?
                new PriceResponseDto(dealSearchIndex.getOriginalPrice(), dealSearchIndex.getDeductionAmount(),
                        dealSearchIndex.getDeductionPercentage()) : null;
        this.validFrom = new DateResponseDto(dealSearchIndex.getValidFrom(), timeZone,
                new Date(dealSearchIndex.getValidFrom()));
        this.expiredOn = new DateResponseDto(dealSearchIndex.getExpiredOn(), timeZone,
                new Date(dealSearchIndex.getExpiredOn()));
        this.startTime = dealSearchIndex.getStartTime();
        this.endTime = dealSearchIndex.getEndTime();
        this.dealSource = dealSearchIndex.getDealSource().toString();
        if (dealSource.equals(UserType.MERCHANT.toString()))
            this.merchant = new BasicMerchantBusinessResponseDto(dealSearchIndex.getMerchantId(),
                    new AuthUser(dealSearchIndex.getMerchantName(), dealSearchIndex.getMerchantImageUrl()));
        else
            this.bank = new BasicMerchantBusinessResponseDto(dealSearchIndex.getMerchantId(),
                    new AuthUser(dealSearchIndex.getMerchantName(), dealSearchIndex.getMerchantImageUrl()));
        this.approvalStatus = dealSearchIndex.getApprovalStatus();
        this.dealCode = dealSearchIndex.getDealCode();
        this.isDealOfTheDay = dealSearchIndex.isDealOfTheDay();
        this.couponAvailable = dealSearchIndex.isCouponAvailable();
        this.expired = this.expiredOn.getMilliseconds() < System.currentTimeMillis();
        this.couponDetails = new DealCouponResponseDto(dealSearchIndex, timeZone);
        this.isFavorite = isFavorite;
        if (dealSearchIndex.getPromoTypeId() != null && dealSearchIndex.getPromoTypeName() != null) {
            this.promoType = new PromoTypeResponseDto();
            this.promoType.setId(dealSearchIndex.getPromoTypeId());
            this.promoType.setName(dealSearchIndex.getPromoTypeName());
            this.promoType.setCategoryId(dealSearchIndex.getPromoTypeCategoryId());
            this.promoType.setCategoryName(dealSearchIndex.getPromoTypeCategoryName());
        }
    }

    public BasicDealResponseDto(BankDeal bankDeal, BasicMerchantBusinessResponseDto merchantBusinessResponseDto,
                                BasicMerchantBusinessResponseDto bankBusinessResponseDto, String timeZone) {
        this.id = bankDeal.getId();
        this.title = bankDeal.getTitle();
        this.subTitle = bankDeal.getSubTitle();
        this.imageUrls = bankDeal.getImageUrls();
        this.coverImage = bankDeal.getCoverImage();
        this.totalQuantity = bankDeal.getQuantity();
        this.remainingQuantity = bankDeal.getQuantity();
        this.price = (bankDeal.getOriginalPrice() != null) ?
                new PriceResponseDto(bankDeal.getOriginalPrice(), bankDeal.getDeductionAmount(),
                        bankDeal.getDeductionPercentage()) : null;
        this.deductionPercentage = bankDeal.getDeductionPercentage();
        this.validFrom = new DateResponseDto(bankDeal.getValidFrom(), timeZone, new Date(bankDeal.getValidFrom()));
        this.expiredOn = new DateResponseDto(bankDeal.getExpiredOn(), timeZone, new Date(bankDeal.getExpiredOn()));
        this.startTime = bankDeal.getStartTime();
        this.endTime = bankDeal.getEndTime();
        this.merchant = merchantBusinessResponseDto;
        this.bank = bankBusinessResponseDto;
        this.dealSource = UserType.BANK.toString();
        this.approvalStatus = bankDeal.getApprovalStatus();
        this.dealCode = bankDeal.getDealCode();
        this.isDealOfTheDay = bankDeal.isDealOfTheDay();
        this.couponAvailable = bankDeal.isCouponAvailable();
        this.couponDetails = null;
        this.expired = this.expiredOn.getMilliseconds() < System.currentTimeMillis();
    }

    /**
     * This method convert string to string list
     *
     * @param images images string
     * @return images list
     */
    private List<String> stringToListImages(String images) {
        if (images == null) return List.of();
        String s = images.trim();
        if (!s.isEmpty() && s.charAt(0) == '[') s = s.substring(1);
        if (!s.isEmpty() && s.charAt(s.length() - 1) == ']') s = s.substring(0, s.length() - 1);
        String[] parts = s.split(","); // comma is not special; this is safe and fast
        List<String> out = new ArrayList<>(parts.length);
        for (String p : parts) {
            String v = p.trim();
            if (!v.isEmpty()) out.add(v);
        }
        return out;
    }
    @Override
    public String toLogJson() {
        return toJson();
    }
}
