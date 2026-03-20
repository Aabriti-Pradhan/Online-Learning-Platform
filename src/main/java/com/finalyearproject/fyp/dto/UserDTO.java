package com.finalyearproject.fyp.dto;

public record UserDTO(
        Long   userId,
        String username,
        String email,
        String role
) {}