package com.kupi.cc.auth.repository;

import com.kupi.cc.auth.domain.entity.ApprovedBankBusiness;
import com.kupi.cc.auth.domain.entity.Business;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Approved Bank Business Repository
 */
public interface ApprovedBankBusinessRepository extends JpaRepository<ApprovedBankBusiness, String> {

    /**
     * Used to delete approved business by bank id.
     *
     * @param bankId bankId
     */
    void deleteByBankId(String bankId);

    /**
     * Used to get approved business by bank id.
     *
     * @param bankId bankId
     * @return approved business.
     */
    Optional<ApprovedBankBusiness> findByBankId(String bankId);

    /**
     * Used to get list of approvedBankBusiness by bank id list.
     *
     * @param bankIds bank id list
     * @return ApprovedBankBusiness list.
     */
    Optional<List<ApprovedBankBusiness>> findAllByBankIdIn(List<String> bankIds);

    /**
     * Used to get active banks count.
     *
     * @return active banks count.
     */
    @Query("SELECT COUNT(ab) FROM ApprovedBankBusiness ab WHERE ab.bankId IN " +
            " (SELECT u.id FROM User u WHERE u.approvalStatus = 'APPROVED' OR u.approvalStatus = 'UNBLOCKED')")
    int getActiveBankCount();

    /**
     * This method returns active bank business profiles.
     *
     * @param pageable pageable
     * @return business profiles page.
     */
    @Query("SELECT ab.bankBusiness FROM ApprovedBankBusiness ab WHERE ab.bankId IN " +
            " (SELECT u.id FROM User u WHERE u.approvalStatus = 'APPROVED' OR u.approvalStatus = 'UNBLOCKED')")
    Page<Business> getActiveBankProfiles(Pageable pageable);

    /**
     * This method returns active bank business profiles with searchTerm.
     *
     * @param searchTerm searchTerm
     * @param pageable   pageable
     * @return business profiles page.
     */
    @Query("SELECT ab.bankBusiness FROM ApprovedBankBusiness ab " +
            " WHERE ab.businessName LIKE %:searchTerm% AND ab.bankId IN " +
            " (SELECT u.id FROM User u WHERE u.approvalStatus = 'APPROVED' OR u.approvalStatus = 'UNBLOCKED')")
    Page<Business> getActiveBankProfilesWithSearchTerm(String searchTerm, Pageable pageable);
}
