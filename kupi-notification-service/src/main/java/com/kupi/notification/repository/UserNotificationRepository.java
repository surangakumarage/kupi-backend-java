package com.kupi.notification.repository;

import com.kupi.notification.domain.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, String> {

    /**
     * Get firebaseUserToken by user Id
     *
     * @param userId userId
     * @return firebaseUserToken
     */
    Optional<UserNotification> findByUserId(String userId);

    /**
     * Remove token from UserNotification record by user id
     *
     * @param updatedAt updated timestamp
     * @param userId userId
     */
    @Transactional
    @Modifying
    @Query("UPDATE UserNotification SET token = null, updatedAt = ?1 WHERE userId = ?2")
    void deleteNotificationTokenForUserId(Date updatedAt, String userId);

    /**
     * This method enables or disables notifications for a given userId.
     *
     * @param isEnabled notification enabled/disabled
     * @param userId    userId
     */
    @Transactional
    @Modifying
    @Query("UPDATE UserNotification SET notificationEnabled = ?1 WHERE userId = ?2")
    void updateNotificationStatusForUserId(boolean isEnabled, String userId);

}
