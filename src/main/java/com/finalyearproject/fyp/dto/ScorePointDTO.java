package com.finalyearproject.fyp.dto;

public record ScorePointDTO(
        String testTitle,
        int    score,
        int    total,
        int    pct,
        String takenAt
) {}