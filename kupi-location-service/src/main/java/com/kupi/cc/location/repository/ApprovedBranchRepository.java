package com.kupi.cc.location.repository;

import com.kupi.cc.location.domain.entity.ApprovedBranch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Approved branch repository
 */
public interface ApprovedBranchRepository extends JpaRepository<ApprovedBranch, String> {

    /**
     * This method returns approved branches by userId.
     *
     * @param userId   userId
     * @param pageable pageable
     * @return approved branch page.
     */
    Page<ApprovedBranch> findByUserId(String userId, Pageable pageable);

    /**
     * This method returns approved branches by userId & with searchTerm.
     *
     * @param userId     userId
     * @param searchTerm searchTerm
     * @param pageable   pageable
     * @return approved branch page.
     */
    @Query("SELECT a FROM ApprovedBranch a WHERE a.userId = :userId AND (a.name LIKE %:searchTerm% OR " +
            " a.address LIKE %:searchTerm%)")
    Page<ApprovedBranch> getBranchesByUserIdAndSearchTerm(String userId, String searchTerm, Pageable pageable);
}
