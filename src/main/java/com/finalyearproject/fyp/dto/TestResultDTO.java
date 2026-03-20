package com.finalyearproject.fyp.dto;

public record TestResultDTO(
        Long    attemptId,
        int     score,
        int     total
) {}