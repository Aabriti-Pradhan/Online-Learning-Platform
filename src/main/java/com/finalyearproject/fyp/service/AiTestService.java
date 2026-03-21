package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.dto.TestSummaryDTO;

public interface AiTestService {

    /**
     * Generate a test from all PDF resources in a chapter.
     * Saves it exactly like a manual test — same tables, same flow.
     */
    TestSummaryDTO generateTestFromChapter(
            Long   courseId,
            Long   chapterId,
            String userEmail,
            String testTitle,
            int    questionCount
    ) throws Exception;
}