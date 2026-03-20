package com.finalyearproject.fyp.dto;

import java.time.LocalDateTime;

public record TestSummaryDTO(
        Long          testId,
        String        testTitle,
        String        testType,
        LocalDateTime createdAt,
        long          questionCount,
        long          attemptCount
) {}