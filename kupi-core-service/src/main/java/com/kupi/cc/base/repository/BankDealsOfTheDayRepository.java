package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.BankDealsOfTheDay;
import com.kupi.cc.base.domain.entity.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Bank Deals of the date repository.
 */
public interface BankDealsOfTheDayRepository extends JpaRepository<BankDealsOfTheDay, String> {

    /**
     * Update all active deals of the days to false
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE BankDealsOfTheDay SET isActive = false WHERE isActive = true ")
    void disableActiveDeals();

    /**
     * This method will find deals of the day by applied date.
     *
     * @param date date
     * @return deals of the day.
     */
    @Query("SELECT dod.bankDeal FROM BankDealsOfTheDay dod WHERE dod.applyOn = :date")
    Page<Deal> getDealByAppliedOn(Pageable pageable, Date date);

    /**
     * This method returns deals for bankId & for applied on.
     *
     * @param bankId bankId
     * @param date   date
     * @return deals of the day list.
     */
    @Query("SELECT dod.bankDeal FROM BankDealsOfTheDay dod WHERE dod.applyOn = :date AND dod.bankDeal.bankId = :bankId")
    Page<Deal> getDealsByBankIdAndAppliedOn(Pageable pageable, String bankId, Date date);

    /**
     * This method is used to get deals of the day by date & search term.
     *
     * @param searchTerm searchTerm
     * @param date       date
     * @return deals of the day.
     */
    @Query("SELECT dod.bankDeal FROM BankDealsOfTheDay dod WHERE dod.applyOn = :date AND dod.bankDeal.id IN " +
            " (SELECT d.id FROM DealSearchIndex d WHERE (d.title LIKE %:searchTerm% OR " +
            " d.subTitle LIKE %:searchTerm% OR d.description LIKE %:searchTerm% OR " +
            " d.merchantName LIKE %:searchTerm% OR d.brandNames LIKE %:searchTerm% OR " +
            " d.categoryNames LIKE %:searchTerm% ))")
    Page<Deal> getDealsOfTheDayByDateAndSearchTerm(Pageable pageable, String searchTerm, Date date);

    /**
     * This method is used to get deals of the day by bankId, searchTerm & by date.
     *
     * @param bankId     bankId
     * @param searchTerm searchTerm
     * @param date       date
     * @return deals of the day.
     */
    @Query("SELECT dod.bankDeal FROM BankDealsOfTheDay dod WHERE dod.applyOn = :date AND dod.bankDeal.bankId = :bankId " +
            " AND dod.bankDeal.id IN " +
            " (SELECT d.id FROM DealSearchIndex d WHERE (d.title LIKE %:searchTerm% OR " +
            " d.subTitle LIKE %:searchTerm% OR d.description LIKE %:searchTerm% OR " +
            " d.merchantName LIKE %:searchTerm% OR d.brandNames LIKE %:searchTerm% OR " +
            " d.categoryNames LIKE %:searchTerm% ))")
    Page<Deal> getDealsOfTheDayByBankIdDateAndSearchTerm(Pageable pageable, String bankId, String searchTerm, Date date);
}