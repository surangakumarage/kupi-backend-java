package com.kupi.cc.location.domain.entity;

import com.kupi.cc.location.enums.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * Approved branch
 */
@Getter
@Setter
@Entity
@Table(name = "approved_branch")
@NoArgsConstructor
public class ApprovedBranch {

    @Id
    private String id;
    private String name;
    private String address;
    private String proofUrl;
    private Boolean addressVerified = false;
    private String contactNo;
    private Boolean contactNoVerified = false;
    private String mapUrl;
    private Double latitude;
    private Double longitude;
    private String globalCode;
    private String city;
    private String country;
    private String userId;
    private long createdAt;
    private long updatedAt;
    @Enumerated(EnumType.STRING)
    private UserType userType;

    public ApprovedBranch(Branch branch) {
        this.id = branch.getId();
        this.name = branch.getName();
        this.address = branch.getAddress();
        this.proofUrl = branch.getProofUrl();
        this.addressVerified = branch.getAddressVerified();
        this.contactNo = branch.getContactNo();
        this.contactNoVerified = branch.getContactNoVerified();
        this.mapUrl = branch.getMapUrl();
        this.latitude = branch.getLatitude();
        this.longitude = branch.getLongitude();
        this.globalCode = branch.getGlobalCode();
        this.city = branch.getCity();
        this.country = branch.getCountry();
        this.userId = branch.getUserId();
        this.createdAt = branch.getCreatedAt();
        this.updatedAt = branch.getUpdatedAt();
        this.userType = branch.getUserType();
    }

    public void update(Branch branch) {
        this.name = branch.getName();
        this.address = branch.getAddress();
        this.proofUrl = branch.getProofUrl();
        this.addressVerified = branch.getAddressVerified();
        this.contactNo = branch.getContactNo();
        this.contactNoVerified = branch.getContactNoVerified();
        this.mapUrl = branch.getMapUrl();
        this.latitude = branch.getLatitude();
        this.longitude = branch.getLongitude();
        this.globalCode = branch.getGlobalCode();
        this.city = branch.getCity();
        this.country = branch.getCountry();
        this.userId = branch.getUserId();
        this.updatedAt = branch.getUpdatedAt();
    }
}
