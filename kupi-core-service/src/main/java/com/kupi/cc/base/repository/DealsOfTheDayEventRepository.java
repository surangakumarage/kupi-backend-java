package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.DealsOfTheDayEvent;
import com.kupi.cc.base.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Deals Of The Day Event Repository
 */
public interface DealsOfTheDayEventRepository extends JpaRepository<DealsOfTheDayEvent, String> {

    /**
     * This method finds deals of the day event with userId & validOn.
     *
     * @param userId  userId
     * @param validOn validOn
     * @return deals of the day event.
     */
    Optional<DealsOfTheDayEvent> findByUserIdAndValidOn(String userId, Date validOn);

    /**
     * This method finds all deals of the day event within the date range for userId.
     *
     * @param userId    userId
     * @param startDate startDate
     * @param endDate   endDate
     * @return deals of the day event.
     */
    List<DealsOfTheDayEvent> findByUserIdAndValidOnGreaterThanEqualAndValidOnLessThanEqual(String userId, Date startDate,
                                                                                           Date endDate);

    /**
     * This method finds all deals of the day event for a particular date & userType.
     *
     * @param pageable pageable
     * @param validOn  validOn
     * @param userType userType
     * @return deals of the day event.
     */
    Page<DealsOfTheDayEvent> findByValidOnAndUserType(Pageable pageable, Date validOn, UserType userType);

    /**
     * This method returns deals of the day events on a particular date for the userType.
     *
     * @param validOn  validOn
     * @param userType userType
     * @return events list.
     */
    List<DealsOfTheDayEvent> findByValidOnAndUserType(Date validOn, UserType userType);

    @Query("SELECT SIZE(d.deals) FROM DealsOfTheDayEvent d " +
            "WHERE d.userId = :userId " +
            "AND FUNCTION('DATE', d.validOn) >= :startDate " +
            "AND FUNCTION('DATE', d.validOn) <= :endDate")
    List<Integer> getDealsOfTheDayCountsInRange(
            @Param("userId") String userId,
            @Param("startDate") java.sql.Date startDate,
            @Param("endDate") java.sql.Date endDate);

}
