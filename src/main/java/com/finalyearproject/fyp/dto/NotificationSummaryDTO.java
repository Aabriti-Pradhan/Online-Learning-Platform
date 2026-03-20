package com.finalyearproject.fyp.dto;

import java.util.List;

public record NotificationSummaryDTO(
        long                  unreadCount,
        List<NotificationDTO> notifications
) {}