package com.finalyearproject.fyp.dto;

public record AttemptResultDTO(
        Long   attemptId,
        String studentName,
        String studentEmail,
        int    score,
        int    total
) {}