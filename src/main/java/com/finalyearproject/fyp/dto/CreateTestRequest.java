package com.finalyearproject.fyp.dto;

import java.util.List;

public record CreateTestRequest(
        String            testTitle,
        String            testType,
        Integer           timerMinutes,
        List<QuestionDTO> questions
) {}