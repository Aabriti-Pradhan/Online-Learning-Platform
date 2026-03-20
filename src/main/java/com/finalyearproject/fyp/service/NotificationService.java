package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.dto.NotificationSummaryDTO;

import java.util.List;

public interface NotificationService {

    /** Send a notification to a single user. */
    void sendToUser(Long userId, String title, String message, String type, Long referenceId);

    /** Send a notification to a list of users (e.g. all enrolled students). */
    void sendToUsers(List<Long> userIds, String title, String message, String type, Long referenceId);

    /** Fetch latest 10 notifications + unread count for the bell dropdown. */
    NotificationSummaryDTO getSummary(String userEmail);

    /** Mark a single notification as read. */
    void markAsRead(Long notificationId, String userEmail);

    /** Mark all notifications as read. */
    void markAllAsRead(String userEmail);
}