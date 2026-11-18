package com.kupi.cc.location.domain.entity;


import com.kupi.cc.location.domain.response.BusinessResponseDto;
import com.kupi.cc.location.enums.ApprovalStatus;
import com.kupi.cc.location.enums.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "branch_search_index")
public class BranchSearchIndex {

    @Id
    private String id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String address;
    private String proofUrl;
    @Column(nullable = false)
    private Boolean addressVerified;
    private String contactNo;
    private Boolean contactNoVerified;
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;
    @Column(nullable = false)
    private String mapUrl;
    @Column(nullable = false)
    private Double latitude;
    @Column(nullable = false)
    private Double longitude;
    @Column(nullable = false)
    private String globalCode;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String country;
    @Column(nullable = false)
    private String userId;
    private long createdAt;
    private long updatedAt;
    private boolean isDeleted;
    @Enumerated(EnumType.STRING)
    private UserType userType;
    private String businessName;
    private String businessImageUrl;
    private String userApprovalStatus;

    public BranchSearchIndex(Branch branch) {
        this.id = branch.getId();
        this.name = branch.getName();
        this.address = branch.getAddress();
        this.proofUrl = branch.getProofUrl();
        this.addressVerified = branch.getAddressVerified();
        this.contactNo = branch.getContactNo();
        this.contactNoVerified = branch.getContactNoVerified();
        this.approvalStatus = branch.getApprovalStatus();
        this.mapUrl = branch.getMapUrl();
        this.latitude = branch.getLatitude();
        this.longitude = branch.getLongitude();
        this.globalCode = branch.getGlobalCode();
        this.city = branch.getCity();
        this.country = branch.getCountry();
        this.userId = branch.getUserId();
        this.createdAt = branch.getCreatedAt();
        this.updatedAt = branch.getUpdatedAt();
        this.isDeleted = branch.isDeleted();
        this.userType = branch.getUserType();
    }

    public BranchSearchIndex(Branch branch, BusinessResponseDto businessResponseDto) {
        this(branch);
        this.businessName = businessResponseDto.getName();
        this.businessImageUrl = businessResponseDto.getImageUrl();
        this.userApprovalStatus = businessResponseDto.getApprovalStatus();
    }

    /**
     * This method updates branch search index.
     *
     * @param branch branch.
     */
    public void update(Branch branch) {
        this.name = branch.getName();
        this.address = branch.getAddress();
        this.proofUrl = branch.getProofUrl();
        this.addressVerified = branch.getAddressVerified();
        this.contactNo = branch.getContactNo();
        this.contactNoVerified = branch.getContactNoVerified();
        this.approvalStatus = branch.getApprovalStatus();
        this.mapUrl = branch.getMapUrl();
        this.latitude = branch.getLatitude();
        this.longitude = branch.getLongitude();
        this.globalCode = branch.getGlobalCode();
        this.city = branch.getCity();
        this.country = branch.getCountry();
        this.updatedAt = branch.getUpdatedAt();
        this.isDeleted = branch.isDeleted();
    }
}
