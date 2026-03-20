package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.entity.UserNotification;
import com.finalyearproject.fyp.entity.UserNotificationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, UserNotificationId> {

    // Latest 10 for the bell dropdown
    @Query("SELECT un FROM UserNotification un WHERE un.user = :user ORDER BY un.notification.createdAt DESC")
    List<UserNotification> findTop10ByUser(@Param("user") User user);

    // Unread count for badge
    @Query("SELECT COUNT(un) FROM UserNotification un WHERE un.user = :user AND un.isRead = false")
    long countUnreadByUser(@Param("user") User user);

    // Mark all as read
    @Modifying
    @Transactional
    @Query("UPDATE UserNotification un SET un.isRead = true WHERE un.user = :user AND un.isRead = false")
    void markAllReadByUser(@Param("user") User user);

    // Mark single as read
    @Modifying
    @Transactional
    @Query("UPDATE UserNotification un SET un.isRead = true WHERE un.user = :user AND un.notification.notificationId = :notifId")
    void markOneReadByUser(@Param("user") User user, @Param("notifId") Long notifId);
}