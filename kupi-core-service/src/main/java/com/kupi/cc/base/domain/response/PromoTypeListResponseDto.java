package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.PromoType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class PromoTypeListResponseDto extends ResponseDto {

    private List<PromoTypeResponseDto> promoTypes;
    private int currentPage;
    private long totalItems;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public PromoTypeListResponseDto(Page<PromoType> promoTypePage, String timeZone) {
        this.promoTypes = promoTypePage.getContent()
                .stream()
                .map(promoType -> new PromoTypeResponseDto(promoType, timeZone))
                .collect(Collectors.toList());
        this.currentPage = promoTypePage.getNumber();
        this.totalItems = promoTypePage.getTotalElements();
        this.totalPages = promoTypePage.getTotalPages();
        this.hasNext = promoTypePage.hasNext();
        this.hasPrevious = promoTypePage.hasPrevious();
    }

    public PromoTypeListResponseDto(List<PromoType> promoTypes, String timeZone) {
        this.promoTypes = promoTypes.stream()
                .map(promoType -> new PromoTypeResponseDto(promoType, timeZone))
                .collect(Collectors.toList());
        this.currentPage = 0;
        this.totalItems = promoTypes.size();
        this.totalPages = 1;
        this.hasNext = false;
        this.hasPrevious = false;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
