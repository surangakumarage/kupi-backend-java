package com.kupi.notification.repository;

import com.kupi.notification.domain.entity.NotificationDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationDetail, String> {

    /**
     * This method return unread notifications by user id
     *
     * @param userId   userId
     * @param pageable pageable
     * @return page
     */
    Page<NotificationDetail> findByUserIdAndIsDeletedFalse(String userId, Pageable pageable);

    /**
     * This method returns unread notification count for userId.
     *
     * @param userId userId
     * @return unread notification count.
     */
    int countByUserIdAndIsReadFalseAndIsDeletedFalse(String userId);

    /**
     * This method updates notifications as deleted.
     *
     * @param notificationIds notificationIds
     */
    @Transactional
    @Modifying
    @Query("UPDATE NotificationDetail SET isDeleted = true WHERE id IN ?1")
    void setNotificationAsDeleted(List<String> notificationIds);
}
