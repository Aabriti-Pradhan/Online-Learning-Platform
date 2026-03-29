package com.finalyearproject.fyp.dto;

public record StudentProgressDTO(
        Long   userId,
        String username,
        String email,
        int    testsTaken,
        int    avgPct,
        int    totalCorrect,
        int    totalQuestions
) {}