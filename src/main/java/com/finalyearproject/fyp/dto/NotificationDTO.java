package com.finalyearproject.fyp.dto;

import java.time.LocalDateTime;

public record NotificationDTO(
        Long          notificationId,
        String        title,
        String        message,
        String        type,
        Long          referenceId,
        boolean       isRead,
        LocalDateTime createdAt
) {}