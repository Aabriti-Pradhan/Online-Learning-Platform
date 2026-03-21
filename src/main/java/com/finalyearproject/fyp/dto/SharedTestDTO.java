package com.finalyearproject.fyp.dto;

import java.time.LocalDateTime;

public record SharedTestDTO(
        Long          sharedTestId,
        Long          testId,
        String        testTitle,
        String        testType,
        Integer       timerMinutes,
        Long          courseId,
        String        courseName,
        Long          chapterId,
        Long          sharedByUserId,
        String        sharedByUsername,
        LocalDateTime sharedAt,
        long          questionCount
) {}