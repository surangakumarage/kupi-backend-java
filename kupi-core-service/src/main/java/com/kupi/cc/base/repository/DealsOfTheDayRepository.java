package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.domain.entity.DealsOfTheDay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Deals of the date repository
 */
public interface DealsOfTheDayRepository extends JpaRepository<DealsOfTheDay, String> {

    /**
     * Update all active deals of the days to false
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM DealsOfTheDay d WHERE d.isActive = true")
    void disableActiveDeals();

    /**
     * Delete all active deals of the day for a specific date
     *
     * @param date date
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM DealsOfTheDay d WHERE d.isActive = true AND d.applyOn = :date")
    void disableActiveDealsForDate(@Param("date") Date date);

    /**
     * This method will find deals of the day by applied date.
     *
     * @param date date
     * @return deals of the day.
     */
    @Query("SELECT dod.deal FROM DealsOfTheDay dod WHERE dod.applyOn = :date")
    Page<Deal> getDealByAppliedOn(Pageable pageable, Date date);

    /**
     * This method returns deals for merchantId & for applied on.
     *
     * @param merchantId merchantId
     * @param date       date
     * @return deals of the day.
     */
    @Query("SELECT dod.deal FROM DealsOfTheDay dod WHERE dod.applyOn = :date AND dod.deal.merchantId = :merchantId")
    Page<Deal> getDealsByMerchantIdAndAppliedOn(Pageable pageable, String merchantId, Date date);

    /**
     * This method is used to get deals of the day by date & search term.
     *
     * @param searchTerm searchTerm
     * @param date       date
     * @return deals of the day.
     */
    @Query("SELECT dod.deal FROM DealsOfTheDay dod WHERE dod.applyOn = :date AND dod.deal.id IN " +
            " (SELECT d.id FROM DealSearchIndex d WHERE (d.title LIKE %:searchTerm% OR " +
            " d.subTitle LIKE %:searchTerm% OR d.description LIKE %:searchTerm% OR " +
            " d.merchantName LIKE %:searchTerm% OR d.brandNames LIKE %:searchTerm% OR " +
            " d.categoryNames LIKE %:searchTerm% ))")
    Page<Deal> getDealsOfTheDayByDateAndSearchTerm(Pageable pageable, String searchTerm, Date date);

    /**
     * This method is used to get deals of the day by merchantId, searchTerm & by date.
     *
     * @param merchantId merchantId
     * @param searchTerm searchTerm
     * @param date       date
     * @return deals of the day.
     */
    @Query("SELECT dod.deal FROM DealsOfTheDay dod WHERE dod.applyOn = :date AND dod.deal.merchantId = :merchantId " +
            " AND dod.deal.id IN " +
            " (SELECT d.id FROM DealSearchIndex d WHERE (d.title LIKE %:searchTerm% OR " +
            " d.subTitle LIKE %:searchTerm% OR d.description LIKE %:searchTerm% OR " +
            " d.merchantName LIKE %:searchTerm% OR d.brandNames LIKE %:searchTerm% OR " +
            " d.categoryNames LIKE %:searchTerm% ))")
    Page<Deal> getDealsOfTheDayByMerchantDateAndSearchTerm(Pageable pageable, String merchantId, String searchTerm,
                                                           Date date);
}

