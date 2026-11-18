package com.kupi.cc.auth.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "business_menu_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessMenuImage {

    @Transient
    private static final String BUSINESS_MENU_IMAGE_ID_PREFIX = "bmid-";

    @Id
    private String id;

    private String merchantId;

    private String menuImageUrl;

    private Date createdAt;
    private Date updatedAt;

    public BusinessMenuImage(String merchantId, String imageUrl) {
        this.id = BUSINESS_MENU_IMAGE_ID_PREFIX + UUID.randomUUID();
        this.merchantId = merchantId;
        this.menuImageUrl = imageUrl;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
}

