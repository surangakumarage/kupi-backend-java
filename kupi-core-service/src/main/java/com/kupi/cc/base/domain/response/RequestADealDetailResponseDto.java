package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Brand;
import com.kupi.cc.base.domain.entity.Category;
import com.kupi.cc.base.domain.entity.RequestADeal;
import com.kupi.cc.base.enums.UserType;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class RequestADealDetailResponseDto extends ResponseDto {

    private String note;
    private String products;
    private OfferTypeResponseDto offerType;
    private BasicUserResponseDto user;
    private DateResponseDto requestedOn;
    private BasicBrandResponseDto brand;
    private BasicCategoryResponseDto category;
    private BasicMerchantBusinessResponseDto merchant;
    private UserType toUserType;

    public RequestADealDetailResponseDto(RequestADeal requestADeal, Category category, Brand brand,
                                         BusinessMerchantResponseDto merchant,
                                         BasicUserResponseDto basicUserResponseDto, String timeZone) {
        // If category/brand are not provided explicitly, fall back to the values from the entity.
        Category effectiveCategory = (category != null) ? category : requestADeal.getCategory();
        Brand effectiveBrand = (brand != null) ? brand : requestADeal.getBrand();

        this.note = requestADeal.getNote();
        this.products = requestADeal.getProducts();
        this.offerType = ((requestADeal.getOfferType() != null)
                ? new OfferTypeResponseDto(requestADeal.getOfferType().getId(), requestADeal.getOfferType().getName())
                : null);
        this.user = basicUserResponseDto;
        this.toUserType = requestADeal.getToUserType();
        this.requestedOn = new DateResponseDto(
                requestADeal.getCreatedAt(), timeZone, new Date(requestADeal.getCreatedAt()));
        this.brand = ((effectiveBrand != null)
                ? new BasicBrandResponseDto(effectiveBrand.getId(), effectiveBrand.getName())
                : null);
        this.category = new BasicCategoryResponseDto(effectiveCategory.getId(), effectiveCategory.getName());
        this.merchant = new BasicMerchantBusinessResponseDto(merchant);
    }
}
