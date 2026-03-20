package com.finalyearproject.fyp.dto;

public record FriendDTO(
        Long   userId,
        String username,
        String email,
        String role,
        Long   friendshipId
) {}