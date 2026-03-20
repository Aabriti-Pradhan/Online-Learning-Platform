package com.finalyearproject.fyp.dto;

import java.time.LocalDateTime;

public record FriendRequestDTO(
        Long          friendshipId,
        Long          userId,
        String        username,
        String        email,
        String        role,
        LocalDateTime since
) {}