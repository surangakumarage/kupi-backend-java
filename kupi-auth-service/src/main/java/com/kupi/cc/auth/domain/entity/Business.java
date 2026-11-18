package com.kupi.cc.auth.domain.entity;

import com.kupi.cc.auth.domain.request.BusinessRequestDto;
import com.kupi.cc.auth.domain.request.OpeningHoursRequestDto;
import com.kupi.cc.auth.enums.ApprovalStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "business")
@Getter
@Setter
@NoArgsConstructor
public class Business {

    @Transient
    private static final String BUSINESS_ID_PREFIX = "bisid-";
    @Id
    private String id;
    @ManyToOne
    @JoinColumn(name = "merchantId")
    private User merchant;
    private String businessName;
    private String ownerName;
    private String mobileNo;
    private String telephone;
    @Column(length = 150)
    private String description;
    private String email;
    private String businessRegNo;
    private String address;
    private String imageUrl;
    private String webSite;
    private String facebook;
    private String instagram;
    private Date createdAt;
    private Date updatedAt;
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;
    @Embedded
    private OpeningHours openingHours;
    private String youtube;
    private String x;
    private String tiktok;

    public Business(BusinessRequestDto businessRequestDto, User user) {
        this.id = BUSINESS_ID_PREFIX + UUID.randomUUID();
        this.merchant = user;
        this.businessName = businessRequestDto.getBusinessName();
        this.ownerName = businessRequestDto.getOwnerName();
        this.mobileNo = businessRequestDto.getMobileNo().getNo();
        this.telephone = (businessRequestDto.getTelephone() != null)
                ? businessRequestDto.getTelephone().getNo() : null;
        this.email = businessRequestDto.getEmail();
        this.businessRegNo = businessRequestDto.getBusinessRegNo();
        this.description=businessRequestDto.getDescription();
        this.address = businessRequestDto.getAddress();
        this.imageUrl = businessRequestDto.getImageUrl();
        this.webSite = businessRequestDto.getWebSite();
        this.facebook = businessRequestDto.getFacebook();
        this.instagram = businessRequestDto.getInstagram();
        this.createdAt = new Date();
        this.updatedAt = this.createdAt;
        this.approvalStatus = ApprovalStatus.PENDING;
        this.openingHours = toOpeningHours(businessRequestDto.getOpeningHours());
        this.youtube = businessRequestDto.getYoutube();
        this.x = businessRequestDto.getX();
        this.tiktok = businessRequestDto.getTiktok();

    }

    public Business(BankBusiness bankBusiness) {
        this.id = bankBusiness.getId();
        this.merchant = bankBusiness.getBank();
        this.businessName = bankBusiness.getBusinessName();
        this.ownerName = bankBusiness.getOwnerName();
        this.mobileNo = bankBusiness.getMobileNo();
        this.telephone = bankBusiness.getTelephone();
        this.email = bankBusiness.getEmail();
        this.businessRegNo = bankBusiness.getBusinessRegNo();
        this.address = bankBusiness.getAddress();
        this.imageUrl = bankBusiness.getImageUrl();
        this.webSite = bankBusiness.getWebSite();
        this.facebook = bankBusiness.getFacebook();
        this.instagram = bankBusiness.getInstagram();
        this.createdAt = bankBusiness.getCreatedAt();
        this.updatedAt = bankBusiness.getUpdatedAt();
        this.approvalStatus = bankBusiness.getApprovalStatus();
    }

    /**
     * Used to update business profile.
     *
     * @param businessRequestDto businessRequestDto
     */
    public void update(BusinessRequestDto businessRequestDto) {
        this.businessName = businessRequestDto.getBusinessName();
        this.ownerName = businessRequestDto.getOwnerName();
        this.mobileNo = businessRequestDto.getMobileNo().getNo();
        this.telephone = (businessRequestDto.getTelephone() != null)
                ? businessRequestDto.getTelephone().getNo() : null;
        this.email = businessRequestDto.getEmail();
        this.businessRegNo = businessRequestDto.getBusinessRegNo();
        this.address = businessRequestDto.getAddress();
        this.description= businessRequestDto.getDescription();
        this.imageUrl = businessRequestDto.getImageUrl();
        this.webSite = businessRequestDto.getWebSite();
        this.facebook = businessRequestDto.getFacebook();
        this.instagram = businessRequestDto.getInstagram();
        this.updatedAt = new Date();
        this.openingHours = toOpeningHours(businessRequestDto.getOpeningHours());
        this.youtube = businessRequestDto.getYoutube();
        this.x = businessRequestDto.getX();
        this.tiktok = businessRequestDto.getTiktok();
    }

    private OpeningHours toOpeningHours(OpeningHoursRequestDto dto) {
        OpeningHours oh = new OpeningHours();
        oh.setMonday(dto.getMonday());
        oh.setTuesday(dto.getTuesday());
        oh.setWednesday(dto.getWednesday());
        oh.setThursday(dto.getThursday());
        oh.setFriday(dto.getFriday());
        oh.setSaturday(dto.getSaturday());
        oh.setSunday(dto.getSunday());
        return oh;
    }

}
