package com.kupi.cc.auth.repository;

import com.kupi.cc.auth.domain.entity.ApprovedBusiness;
import com.kupi.cc.auth.domain.entity.Business;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Approved business repository
 */
public interface ApprovedBusinessRepository extends JpaRepository<ApprovedBusiness, String> {

    /**
     * Used to get approved business by merchant id.
     *
     * @param merchantId merchantId
     * @return approved business.
     */
    Optional<ApprovedBusiness> findByMerchantId(String merchantId);

    /**
     * Used to get approved business by business name.
     *
     * @param businessName businessName
     * @return approved business.
     */
    List<ApprovedBusiness> findByBusinessNameContaining(String businessName);

    /**
     * Used to delete approved business by merchant id.
     *
     * @param merchantId merchantId
     */
    void deleteByMerchantId(String merchantId);

    /**
     * Used to get active merchant count.
     *
     * @return active merchant count.
     */
    @Query("SELECT COUNT(ab) FROM ApprovedBusiness ab WHERE ab.merchantId IN " +
            " (SELECT u.id FROM User u WHERE u.approvalStatus = 'APPROVED' OR u.approvalStatus = 'UNBLOCKED')")
    int getActiveMerchantCount();

    /**
     * This method returns active merchant business profiles.
     *
     * @param pageable pageable
     * @return business profiles page.
     */
    @Query("SELECT ab.business FROM ApprovedBusiness ab WHERE ab.merchantId IN " +
            " (SELECT u.id FROM User u WHERE u.approvalStatus = 'APPROVED' OR u.approvalStatus = 'UNBLOCKED')")
    Page<Business> getActiveMerchantProfiles(Pageable pageable);

    /**
     * This method returns active merchant business profiles with searchTerm.
     *
     * @param searchTerm searchTerm
     * @param pageable   pageable
     * @return business profiles page.
     */
    @Query("SELECT ab.business FROM ApprovedBusiness ab WHERE ab.businessName LIKE %:searchTerm% AND ab.merchantId IN "
            + " (SELECT u.id FROM User u WHERE u.approvalStatus = 'APPROVED' OR u.approvalStatus = 'UNBLOCKED')")
    Page<Business> getActiveMerchantProfilesWithSearchTerm(String searchTerm, Pageable pageable);
}
