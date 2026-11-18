package com.kupi.cc.auth.domain.entity;

import com.kupi.cc.auth.domain.request.SliderImageCreateRequestDto;
import com.kupi.cc.auth.domain.request.SliderImageItemDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "business_slider_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessSliderImage {

    @Transient
    private static final String BUSINESS_SLIDER_IMAGE_ID_PREFIX = "bsiid-";

    @Id
    private String id;

    private String merchantId;

    private String imageUrl;
    private int displayOrder;

    private Date createdAt;
    private Date updatedAt;


    public BusinessSliderImage(String merchantId, SliderImageItemDto dto) {
        this.id = BUSINESS_SLIDER_IMAGE_ID_PREFIX + UUID.randomUUID();
        this.merchantId = merchantId;
        this.imageUrl = dto.getImageUrl();
        this.displayOrder = dto.getDisplayOrder();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

}


