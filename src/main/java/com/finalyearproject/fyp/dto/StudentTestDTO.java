package com.finalyearproject.fyp.dto;

import java.util.List;

public record StudentTestDTO(
        Long             testId,
        String           testTitle,
        String           testType,
        Integer          timerMinutes,
        long             questionCount,
        List<AttemptDTO> pastAttempts
) {}