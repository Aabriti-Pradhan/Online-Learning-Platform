package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.dto.*;
import com.finalyearproject.fyp.entity.Question;
import com.finalyearproject.fyp.entity.Test;

import java.util.List;
import java.util.Map;

public interface TestService {
    List<TestSummaryDTO> getTestsForTeacher(Long chapterId);
    List<StudentTestDTO> getTestsForStudent(Long chapterId, String userEmail);
    TestSummaryDTO       createTest(Long courseId, Long chapterId, String userEmail, CreateTestRequest request);
    TestSummaryDTO       updateTest(Long courseId, Long chapterId, Long testId, String userEmail, CreateTestRequest request);
    void                 deleteTest(Long courseId, Long testId);
    List<Question>       getQuestionsForTest(Long testId);
    Test                 getTestById(Long testId);
    TestResultDTO        submitTest(Long courseId, Long chapterId, Long testId, String userEmail, Map<String, String> answers);
    TestResultData       getResultPage(Long testId, Long attemptId);
    List<AttemptResultDTO> getAttemptResults(Long testId);

    record TestResultData(
            Test             test,
            List<QuestionResult> results,
            int              score,
            int              total,
            int              pct
    ) {}

    record QuestionResult(
            Question question,
            String   selected,
            boolean  correct
    ) {}
}