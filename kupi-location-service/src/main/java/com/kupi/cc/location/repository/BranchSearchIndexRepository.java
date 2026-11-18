package com.kupi.cc.location.repository;

import com.kupi.cc.location.domain.entity.BranchSearchIndex;
import com.kupi.cc.location.enums.ApprovalStatus;
import com.kupi.cc.location.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * Branch repository
 */
public interface BranchSearchIndexRepository extends JpaRepository<BranchSearchIndex, String> {

    /**
     * This method returns all branches by approvalStatus and userType.
     *
     * @param approvalStatus approvalStatus
     * @param userType       userType
     * @param pageable       pageable
     * @return page of branches.
     */
    Page<BranchSearchIndex> findAllByApprovalStatusAndUserTypeAndIsDeleted(ApprovalStatus approvalStatus, UserType userType,
                                                               Pageable pageable, boolean isDeleted);

    /**
     * This method returns all branches by approvalStatus, userType and by searchTerm.
     * SearchTerm can be branch name, address or business name.
     *
     * @param approvalStatus approvalStatus
     * @param userType       userType
     * @param searchTerm     searchTerm
     * @param pageable       pageable
     * @return page of branches.
     */
    @Query("SELECT b FROM BranchSearchIndex b WHERE b.approvalStatus = :approvalStatus AND b.userType = :userType " +
            "AND b.isDeleted = :isDeleted AND (b.name LIKE %:searchTerm% OR b.address LIKE %:searchTerm% " +
            "OR b.businessName LIKE %:searchTerm%)")
    Page<BranchSearchIndex> getBranchListByApprovalStatusUserTypeAndBySearchTerm(ApprovalStatus approvalStatus,
                                                                                 UserType userType, String searchTerm,
                                                                                 Pageable pageable, boolean isDeleted);

    /**
     * This method returns distinct userIds for userType.
     *
     * @param pageable pageable
     * @param userType MERCHANT/BANK
     * @return page of userIds.
     */
    @Query("SELECT DISTINCT b.userId FROM BranchSearchIndex b WHERE b.userType = :userType")
    Page<String> getDistinctUserIds(Pageable pageable, UserType userType);

    /**
     * This method is used to update business profile information for branches.
     *
     * @param businessName   businessName
     * @param imageUrl       imageUrl
     * @param approvalStatus approvalStatus
     * @param userId         userId
     */
    @Modifying
    @Transactional
    @Query("UPDATE BranchSearchIndex SET businessName = :businessName, businessImageUrl = :imageUrl, " +
            " userApprovalStatus = :approvalStatus WHERE userId = :userId")
    void setBusinessProfileInfo(String businessName, String imageUrl, String approvalStatus, String userId);
}
