package com.kupi.cc.location.domain.entity;

import com.kupi.cc.location.domain.request.BranchRequestDto;
import com.kupi.cc.location.domain.request.BranchUpdateRequestDto;
import com.kupi.cc.location.enums.ApprovalStatus;
import com.kupi.cc.location.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Branch entity
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "branch")
public class Branch {
    @Transient
    private static final String BRANCH_ID_PREFIX = "brid-";
    @Transient
    private String branchId;

    @Id
    private String id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String address;
    private String proofUrl;
    @Column(nullable = false)
    private Boolean addressVerified = false;
    private String contactNo;
    private Boolean contactNoVerified = false;
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

    public Branch(BranchRequestDto branchRequestDto, UserType userType) {
        this.id = BRANCH_ID_PREFIX + UUID.randomUUID();
        this.name = branchRequestDto.getName();
        this.address = branchRequestDto.getAddress().getValue();
        this.proofUrl = branchRequestDto.getAddress().getProofUrl();
        this.contactNo = branchRequestDto.getContactNo();
        this.approvalStatus = ApprovalStatus.PENDING;
        this.mapUrl = branchRequestDto.getLocation().getLocationMapUrl();
        this.latitude = branchRequestDto.getLocation().getLatitude();
        this.longitude = branchRequestDto.getLocation().getLongitude();
        this.globalCode = branchRequestDto.getLocation().getGlobalCode();
        this.city = branchRequestDto.getLocation().getCity().getCombinedName();
        this.country = branchRequestDto.getLocation().getCountry().getCombinedName();
        this.userId = branchRequestDto.getUserId();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isDeleted = false;
        this.userType = userType;
    }


    public void update(BranchUpdateRequestDto branchUpdateRequestDto) {
        this.name = branchUpdateRequestDto.getName();
        this.address = branchUpdateRequestDto.getAddress().getValue();
        this.proofUrl = branchUpdateRequestDto.getAddress().getProofUrl();
        this.addressVerified = (branchUpdateRequestDto.getAddress() != null &&
                branchUpdateRequestDto.getAddress().getVerified() != null) ?
                branchUpdateRequestDto.getAddress().getVerified() : addressVerified;
        this.contactNo = branchUpdateRequestDto.getContactNo().getValue();
        this.contactNoVerified = (branchUpdateRequestDto.getContactNo() != null &&
                branchUpdateRequestDto.getContactNo().getVerified() != null) ?
                branchUpdateRequestDto.getContactNo().getVerified() : contactNoVerified;
        this.approvalStatus = ApprovalStatus.PENDING;
        this.mapUrl = branchUpdateRequestDto.getLocation().getLocationMapUrl();
        this.latitude = branchUpdateRequestDto.getLocation().getLatitude();
        this.longitude = branchUpdateRequestDto.getLocation().getLongitude();
        this.globalCode = branchUpdateRequestDto.getLocation().getGlobalCode();
        this.city = branchUpdateRequestDto.getLocation().getCity().getCombinedName();
        this.country = branchUpdateRequestDto.getLocation().getCountry().getCombinedName();
        this.userId = branchUpdateRequestDto.getUserId();
        this.updatedAt = System.currentTimeMillis();
        this.isDeleted = false;
    }
}
