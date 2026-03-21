package com.finalyearproject.fyp.service;

import java.util.List;

public interface HuggingFaceService {

    /** Send a prompt to Mistral and return the raw text response. */
    String prompt(String userPrompt) throws Exception;

    /** Extract plain text from a locally stored PDF. */
    String extractPdfText(String relativePath) throws Exception;

    /** Generate MCQ questions from chapter text content. */
    List<GeneratedQuestion> generateQuestionsFromText(String text, int count) throws Exception;

    record GeneratedQuestion(
            String questionText,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctAns
    ) {}
}