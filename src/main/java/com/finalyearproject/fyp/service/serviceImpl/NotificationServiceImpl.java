package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.NotificationDTO;
import com.finalyearproject.fyp.dto.NotificationSummaryDTO;
import com.finalyearproject.fyp.entity.Notification;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.entity.UserNotification;
import com.finalyearproject.fyp.entity.UserNotificationId;
import com.finalyearproject.fyp.repository.NotificationRepository;
import com.finalyearproject.fyp.repository.UserNotificationRepository;
import com.finalyearproject.fyp.repository.UserRepository;
import com.finalyearproject.fyp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository     notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository             userRepository;

    // Send to one user

    @Override
    @Transactional
    public void sendToUser(Long userId, String title, String message, String type, Long referenceId) {
        sendToUsers(List.of(userId), title, message, type, referenceId);
    }

    // Send to many users

    @Override
    @Transactional
    public void sendToUsers(List<Long> userIds, String title, String message, String type, Long referenceId) {
        if (userIds == null || userIds.isEmpty()) return;

        // Create one notification event
        Notification notif = new Notification();
        notif.setTitle(title);
        notif.setMessage(message);
        notif.setType(type);
        notif.setReferenceId(referenceId);
        notif.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notif);

        // Create one UserNotification row per recipient
        for (Long userId : userIds) {
            userRepository.findById(userId).ifPresent(user -> {
                UserNotificationId id = new UserNotificationId();
                id.setNotificationId(notif.getNotificationId());
                id.setUserId(user.getUserId());

                UserNotification un = new UserNotification();
                un.setId(id);
                un.setNotification(notif);
                un.setUser(user);
                un.setRead(false);
                userNotificationRepository.save(un);
            });
        }
    }

    // Get summary for bell dropdown

    @Override
    public NotificationSummaryDTO getSummary(String userEmail) {
        User user = getUser(userEmail);

        long unreadCount = userNotificationRepository.countUnreadByUser(user);

        List<NotificationDTO> notifications = userNotificationRepository
                .findTop10ByUser(user)
                .stream()
                .map(un -> new NotificationDTO(
                        un.getNotification().getNotificationId(),
                        un.getNotification().getTitle(),
                        un.getNotification().getMessage(),
                        un.getNotification().getType(),
                        un.getNotification().getReferenceId(),
                        un.isRead(),
                        un.getNotification().getCreatedAt()
                )).toList();

        return new NotificationSummaryDTO(unreadCount, notifications);
    }

    // Mark single as read

    @Override
    @Transactional
    public void markAsRead(Long notificationId, String userEmail) {
        userNotificationRepository.markOneReadByUser(getUser(userEmail), notificationId);
    }

    // Mark all as read

    @Override
    @Transactional
    public void markAllAsRead(String userEmail) {
        userNotificationRepository.markAllReadByUser(getUser(userEmail));
    }

    // helper

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}