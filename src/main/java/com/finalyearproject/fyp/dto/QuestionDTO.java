package com.finalyearproject.fyp.dto;

public record QuestionDTO(
        Long   questionId,
        String questionText,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String correctAns
) {}