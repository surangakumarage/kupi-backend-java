package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.DealsOfTheDayEventSearchIndex;
import com.kupi.cc.base.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

/**
 * DealsOfTheDayEventSearchIndexRepository
 */
public interface DealsOfTheDayEventSearchIndexRepository extends JpaRepository<DealsOfTheDayEventSearchIndex, String> {

    /**
     * This method returns upcoming events by user type.
     *
     * @param pageable pageable
     * @param date     date
     * @param userType MERCHANT/BANK
     * @return page of events.
     */
    Page<DealsOfTheDayEventSearchIndex> findByValidOnGreaterThanAndUserType(
            Pageable pageable, Date date, UserType userType);

    /**
     * This method returns upcoming events by user id & user type.
     *
     * @param pageable pageable
     * @param date     date
     * @param userId   userId
     * @param userType MERCHANT/BANK
     * @return page of events.
     */
    Page<DealsOfTheDayEventSearchIndex> findByValidOnGreaterThanAndUserIdAndUserType(
            Pageable pageable, Date date, String userId, UserType userType);

    /**
     * This method returns upcoming events by user type, deal titles & by username.
     *
     * @param pageable  pageable
     * @param date      date
     * @param userType  MERCHANT/BANK
     * @param dealTitle dealTitle
     * @param userName  userName
     * @return page of events.
     */
    Page<DealsOfTheDayEventSearchIndex> findByValidOnGreaterThanAndUserTypeAndDealTitlesContainsOrUserNameContains(
            Pageable pageable, Date date, UserType userType, String dealTitle, String userName);

    /**
     * This method returns upcoming events by user id & by search term.
     *
     * @param pageable   pageable
     * @param date       date
     * @param userId     userId
     * @param userType   MERCHANT/BANK
     * @param searchTerm searchTerm
     * @return page of events.
     */
    Page<DealsOfTheDayEventSearchIndex> findByValidOnGreaterThanAndUserIdAndUserTypeAndDealTitlesContains(
            Pageable pageable, Date date, String userId, UserType userType, String searchTerm);

    /**
     * This method counts by userType for upcoming events.
     *
     * @param validOn  validOn
     * @param userType MERCHANT/BANK
     * @return total count of events.
     */
    long countByValidOnGreaterThanAndUserTypeEquals(Date validOn, UserType userType);

    /**
     * This method counts users by userType for upcoming DOD events.
     *
     * @param userType MERCHANT/BANK
     * @param validOn  validOn
     * @return total count of events.
     */
    @Query("SELECT COUNT (DISTINCT d.userId) FROM DealsOfTheDayEventSearchIndex d WHERE d.userType = ?1 " +
            " AND d.validOn > ?2")
    long getUserCountByUserType(UserType userType, Date validOn);
}
